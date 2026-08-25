package com.project.financeapi.service;

import com.project.financeapi.dto.Installments.InstallmentDTO;
import com.project.financeapi.dto.Installments.InstallmentResponseDTO;
import com.project.financeapi.dto.Installments.UpdateInstallmentRequestDTO;
import com.project.financeapi.dto.invoice.CreateInvoiceRequestDTO;
import com.project.financeapi.dto.invoice.InvoiceResponseDTO;
import com.project.financeapi.entity.*;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final PersonRepository personRepository;
    private final InvoiceRepository invoiceRepository;
    private final AccountRepository accountRepository;
    private final InstallmentRepository installmentRepository;
    private final OperationTypeRepository operationTypeRepository;
    private final PaymentInstrumentRepository paymentInstrumentRepository;
    private final UserContextService userContextService;
    private final DeactivatedOperationTypeRepository deactivatedTypeRepo;
    private final DeactivatedOperationGroupRepository deactivatedGroupRepo;

    @Transactional
    public InvoiceResponseDTO create(CreateInvoiceRequestDTO dto) {

        User user = userContextService.getAuthenticatedUser();

        PersonBase person = personRepository.findByIdAndCreatedBy(dto.personId(), user)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "Pessoa não encontrada"
                ));

        OperationType operationType =
                operationTypeRepository.findByUserIdAndId(user.getId(), dto.operationTypeId())
                        .orElseThrow(() -> new BusinessException(
                                HttpStatus.NOT_FOUND, "Tipo de operação inválido"
                        ));

        boolean isTypeDeactivated = deactivatedTypeRepo.findByUserIdAndOperationTypeId(user.getId(), operationType.getId()).isPresent();
        boolean isGroupDeactivated = deactivatedGroupRepo.findByUserIdAndOperationGroupId(user.getId(), operationType.getGroup().getId()).isPresent();

        if (isTypeDeactivated || isGroupDeactivated) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Não é possível usar um Tipo ou Grupo de Operação inativado para criar uma fatura.");
        }

        BigDecimal sumInstallments = dto.installments().stream()
                .map(InstallmentDTO::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (dto.totalAmount().compareTo(sumInstallments) != 0) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "Total do documento diferente da soma das parcelas"
            );
        }

        Invoice invoice = invoiceRepository.save(new Invoice(
                user,
                person,
                operationType
        ));

        // 🔹 Agrupa por instrumento
        Map<UUID, List<InstallmentDTO>> groupedByInstrument =
                dto.installments().stream()
                        .collect(Collectors.groupingBy(
                                InstallmentDTO::instrument,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> list.stream()
                                                .sorted(Comparator.comparing(InstallmentDTO::parcelNumber))
                                                .toList()
                                )
                        ));

        List<InstallmentDTO> processedInstallments = new ArrayList<>();

        // 🔹 Processa por instrumento
        for (var entry : groupedByInstrument.entrySet()) {

            PaymentInstrumentBase instrument =
                    paymentInstrumentRepository
                            .findByIdAndUser(entry.getKey(), user.getId())
                            .orElseThrow(() -> new BusinessException(
                                    HttpStatus.NOT_FOUND,
                                    "Instrumento de pagamento inválido"
                            ));

            List<InstallmentDTO> processed =
                    instrument.process(entry.getValue());

            processedInstallments.addAll(processed);
        }

        List<Installment> installments = processedInstallments.stream()
                .map(dtoItem -> {
                    AccountBase installmentAccount = accountRepository.findById(dtoItem.accountId())
                            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada para a parcela " + dtoItem.parcelNumber()));

                    return new Installment(
                            dtoItem.amount(),
                            dtoItem.dueDate(),
                            operationType.getMovementType(),
                            dtoItem.movementDirection(),
                            dtoItem.parcelNumber(),
                            user,
                            invoice,
                            paymentInstrumentRepository.getReferenceById(dtoItem.instrument()),
                            installmentAccount // 🌟 Passando a conta!
                    );
                })
                .toList();

        installmentRepository.saveAll(installments);
        invoice.setInstallments(installments);

        return invoice.toResponse();
    }

    public List<InvoiceResponseDTO> findAll() {

        User user = userContextService.getAuthenticatedUser();

        List<Invoice> invoices = invoiceRepository.findByCreatedBy(user);

        return invoices.stream().map(Invoice::toResponse).toList();
    }

    public InvoiceResponseDTO findById(UUID id) {
        User user = userContextService.getAuthenticatedUser();
        Invoice invoice = invoiceRepository.findByIdAndCreatedBy(id, user).orElseThrow(() ->
                new BusinessException(HttpStatus.NOT_FOUND, "O documento informado não existe.")
        );
        return invoice.toResponse();
    }

    @Transactional
    public void deleteInvoice(UUID id) {
        User user = userContextService.getAuthenticatedUser();

        Invoice invoice = invoiceRepository.findByIdAndCreatedBy(id, user).orElseThrow(() ->
                new BusinessException(HttpStatus.NOT_FOUND, "O documento informado não existe.")
        );

        // 🌟 REGRA 3: Exclusão de Fatura inteira (só se não houver nenhuma transação em NENHUMA parcela)
        if (invoice.getTotalPaid().add(invoice.getTotalDiscount()).compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Não é possível excluir uma fatura que já possui transações (pagamentos ou descontos). Estorne as transações antes de excluir.");
        }

        invoiceRepository.delete(invoice);
    }

    @Transactional
    public void deleteInstallment(UUID installmentId) {
        User user = userContextService.getAuthenticatedUser();

        Installment installment = installmentRepository.findById(installmentId).orElseThrow(() ->
                new BusinessException(HttpStatus.NOT_FOUND, "A parcela informada não existe.")
        );

        if (!installment.getCreatedBy().getId().equals(user.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Você não tem permissão para excluir esta parcela.");
        }

        // 🌟 SUA REGRA DE OURO AQUI: Só bloqueia se o líquido amortizado for maior que zero!
        // Não importa se tem 100 transações de erro/estorno, se a soma deu zero, pode apagar.
        if (installment.getTotalPaid().add(installment.getTotalDiscount()).compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Não é possível excluir uma parcela que possui saldo já amortizado. Faça o estorno dos pagamentos primeiro.");
        }

        Invoice invoice = installment.getInvoice();

        if (invoice.getInstallments().size() == 1) {
            // Se for a última parcela, apaga a fatura inteira
            invoiceRepository.delete(invoice);
        } else {
            // 🌟 A SOLUÇÃO AQUI: Remove a parcela da lista da Fatura mãe!
            // Isso diz ao Hibernate: "Essa parcela foi desvinculada, pode apagar sem medo".
            invoice.getInstallments().remove(installment);

            // Agora o delete vai funcionar e enviar o comando SQL para o banco!
            installmentRepository.delete(installment);
        }
    }

    @Transactional
    public InstallmentResponseDTO updateInstallment(UUID installmentId, UpdateInstallmentRequestDTO dto) {
        User user = userContextService.getAuthenticatedUser();

        Installment installment = installmentRepository.findById(installmentId).orElseThrow(() ->
                new BusinessException(HttpStatus.NOT_FOUND, "A parcela informada não existe.")
        );

        if (!installment.getCreatedBy().getId().equals(user.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar esta parcela.");
        }

        // 🌟 TRAVA MATEMÁTICA: O novo valor não pode ser menor que o que já foi efetivamente pago.
        BigDecimal amortized = installment.getTotalPaid().add(installment.getTotalDiscount());
        if (dto.amount().compareTo(amortized) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "O novo valor (R$ " + dto.amount() + ") não pode ser menor que o valor já amortizado (R$ " + amortized + "). Estorne o pagamento primeiro.");
        }

        // Atualiza a Conta
        AccountBase account = accountRepository.findById(dto.accountId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada."));
        installment.setAccount(account);

        // Atualiza o Instrumento (opcional)
        if (dto.paymentInstrumentId() != null) {
            PaymentInstrumentBase instrument = paymentInstrumentRepository.findByIdAndUser(dto.paymentInstrumentId(), user.getId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Instrumento não encontrado."));
            installment.setPaymentInstrument(instrument);
        } else {
            installment.setPaymentInstrument(null); // Limpou o instrumento
        }

        // Atualiza os dados básicos
        installment.setAmount(dto.amount());
        installment.setDueDate(dto.dueDate());

        // Salva e retorna o DTO atualizado
        return installmentRepository.save(installment).toResponse();
    }

}
