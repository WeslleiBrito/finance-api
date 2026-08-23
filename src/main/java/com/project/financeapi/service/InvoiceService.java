package com.project.financeapi.service;

import com.project.financeapi.dto.Installments.InstallmentDTO;
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
                dto.totalAmount(),
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

}
