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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

            // Consome o limite do cartão
            if (instrument instanceof CreditCard card) {
                BigDecimal totalAmountForCard = entry.getValue().stream()
                        .map(InstallmentDTO::amount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                card.consumeLimit(totalAmountForCard);
                paymentInstrumentRepository.save(card);
            }

            List<InstallmentDTO> processed =
                    instrument.process(entry.getValue(), dto.purchaseDate());

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
                            installmentAccount
                    );
                })
                .toList();

        installmentRepository.saveAll(installments);
        invoice.setInstallments(installments);

        return invoice.toResponse();
    }

    public Page<InvoiceResponseDTO> findAll(Pageable pageable) {
        User user = userContextService.getAuthenticatedUser();
        Page<Invoice> invoicesPage = invoiceRepository.findByCreatedBy(user, pageable);
        return invoicesPage.map(Invoice::toResponse);
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

        if (invoice.getTotalPaid().add(invoice.getTotalDiscount()).compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Não é possível excluir uma fatura que já possui transações (pagamentos ou descontos). Estorne as transações antes de excluir.");
        }

        // Liberar o limite de todos os cartões envolvidos antes de excluir
        for (Installment installment : invoice.getInstallments()) {
            if (installment.getPaymentInstrument() instanceof CreditCard card) {
                card.freeUpLimit(installment.getAmount());
                paymentInstrumentRepository.save(card);
            }
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

        if (installment.getTotalPaid().add(installment.getTotalDiscount()).compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Não é possível excluir uma parcela que possui saldo já amortizado. Faça o estorno dos pagamentos primeiro.");
        }

        Invoice invoice = installment.getInvoice();

        // Liberar limite do cartão caso essa parcela esteja vinculada a um
        if (installment.getPaymentInstrument() instanceof CreditCard card) {
            card.freeUpLimit(installment.getAmount());
            paymentInstrumentRepository.save(card);
        }

        if (invoice.getInstallments().size() == 1) {
            invoiceRepository.delete(invoice);
        } else {
            invoice.getInstallments().remove(installment);
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

        BigDecimal amortized = installment.getTotalPaid().add(installment.getTotalDiscount());
        if (dto.amount().compareTo(amortized) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "O novo valor (R$ " + dto.amount() + ") não pode ser menor que o valor já amortizado (R$ " + amortized + "). Estorne o pagamento primeiro.");
        }

        // 🌟 LÓGICA DE TROCA DE INSTRUMENTO E AJUSTE DE LIMITE 🌟
        PaymentInstrumentBase oldInstrument = installment.getPaymentInstrument();
        BigDecimal oldAmount = installment.getAmount();
        BigDecimal newAmount = dto.amount();

        PaymentInstrumentBase newInstrument = null;
        if (dto.paymentInstrumentId() != null) {
            newInstrument = paymentInstrumentRepository.findByIdAndUser(dto.paymentInstrumentId(), user.getId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Instrumento não encontrado."));
        }

        // Verifica se houve troca de instrumento (de A para B, de Nulo para A, ou de A para Nulo)
        boolean instrumentChanged = false;
        if (oldInstrument == null && newInstrument != null) instrumentChanged = true;
        else if (oldInstrument != null && newInstrument == null) instrumentChanged = true;
        else if (oldInstrument != null && newInstrument != null && !oldInstrument.getId().equals(newInstrument.getId())) instrumentChanged = true;

        if (instrumentChanged) {
            // 1. Devolve o limite para o instrumento ANTIGO (se era cartão)
            if (oldInstrument instanceof CreditCard oldCard) {
                oldCard.freeUpLimit(oldAmount);
                paymentInstrumentRepository.save(oldCard);
            }
            // 2. Consome o limite do instrumento NOVO (se for cartão)
            if (newInstrument instanceof CreditCard newCard) {
                newCard.consumeLimit(newAmount);
                paymentInstrumentRepository.save(newCard);
            }
        } else {
            // Instrumento não mudou. Se for cartão, ajusta apenas a diferença matemática
            if (oldInstrument instanceof CreditCard card) {
                BigDecimal difference = newAmount.subtract(oldAmount);

                if (difference.compareTo(BigDecimal.ZERO) > 0) {
                    card.consumeLimit(difference); // Aumentou o valor da parcela
                } else if (difference.compareTo(BigDecimal.ZERO) < 0) {
                    card.freeUpLimit(difference.abs()); // Diminuiu o valor da parcela
                }
                paymentInstrumentRepository.save(card);
            }
        }

        // Atualiza a Conta
        AccountBase account = accountRepository.findById(dto.accountId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada."));

        installment.setAccount(account);
        installment.setPaymentInstrument(newInstrument);
        installment.setAmount(newAmount);
        installment.setDueDate(dto.dueDate());

        return installmentRepository.save(installment).toResponse();
    }
}