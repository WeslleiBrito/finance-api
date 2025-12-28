package com.project.financeapi.service;

import com.project.financeapi.dto.Installment.InstallmentDTO;
import com.project.financeapi.dto.Installment.InstallmentResponseDTO;
import com.project.financeapi.dto.OperationType.OperationTypeResponseDTO;
import com.project.financeapi.dto.account.ResponseAccountDTO;
import com.project.financeapi.dto.invoice.CreateInvoiceRequestDTO;
import com.project.financeapi.dto.invoice.InvoiceResponseDTO;
import com.project.financeapi.dto.operationGroup.OperationGroupResponseDTO;
import com.project.financeapi.dto.transaction.TransactionResponseDTO;
import com.project.financeapi.dto.user.UserResponseDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.Invoice;
import com.project.financeapi.entity.Installment;
import com.project.financeapi.entity.OperationType;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.*;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final InvoiceRepository invoiceRepository;
    private final AccountRepository accountRepository;
    private final InstallmentRepository installmentRepository;
    private final OperationTypeRepository operationTypeRepository;
    private final PaymentInstrumentRepository paymentInstrumentRepository;
    private final JwtUtil jwtUtil;


    @Transactional
    public InvoiceResponseDTO create(String token, CreateInvoiceRequestDTO dto) {

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "Usuário não encontrado"
                ));

        PersonBase person = personRepository.findById(dto.personId())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "Pessoa não encontrada"
                ));

        AccountBase account = accountRepository.findById(dto.accountId())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "Conta não encontrada"
                ));

        OperationType operationType =
                operationTypeRepository.findByCreatedByAndId(user, dto.operationTypeId())
                        .orElseThrow(() -> new BusinessException(
                                HttpStatus.NOT_FOUND, "Tipo de operação inválido"
                        ));

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
                account,
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

        // 🔹 Cria entidades
        List<Installment> installments = processedInstallments.stream()
                .map(dtoItem -> new Installment(
                        dtoItem.amount(),
                        dtoItem.dueDate(),
                        operationType.getMovementType(),
                        dtoItem.parcelNumber(),
                        user,
                        invoice,
                        paymentInstrumentRepository.getReferenceById(dtoItem.instrument())
                ))
                .toList();

        installmentRepository.saveAll(installments);

        invoice.setInstallments(installments);

        return toDocumentResponseDTO(invoice);
    }


    public List<InvoiceResponseDTO> findAll(String token) {

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "O usuário informado não existe"));


        List<Invoice> invoices = invoiceRepository.findByCreatedBy(user);

        return invoices.stream()
                .map(this::toDocumentResponseDTO)
                .collect(Collectors.toList());
    }
    public InvoiceResponseDTO findById(String token, UUID id) {

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "O usuário informado não existe"));

        Invoice invoice = invoiceRepository.findByIdAndCreatedBy(id, user).orElseThrow(() -> new RuntimeException(
                "O documento informado não exite."
        ));

        return toDocumentResponseDTO(
                invoice
        );

    }
    public InvoiceResponseDTO toDocumentResponseDTO(@NotNull Invoice invoice) {

        return new InvoiceResponseDTO(
                invoice.getId(),
                invoice.getAccount().getId(),
                invoice.getIssueDate(),
                invoice.getPaymentStatus(),
                invoice.getQuantityInstallments(),
                invoice.getTotalAmount(),
                invoice.getTotalPaid(),
                invoice.getRemainingBalance(),
                new OperationTypeResponseDTO(
                        invoice.getOperationType().getId(),
                        invoice.getOperationType().getName(),
                        invoice.getOperationType().getMovementType(),
                        invoice.getOperationType().getOperationStatus(),
                        invoice.getOperationType().getIsGlobal(),
                        new OperationGroupResponseDTO(
                                invoice.getOperationType().getGroup().getId(),
                                invoice.getOperationType().getName(),
                                invoice.getOperationType().getGroup().getIsGlobal(),
                                invoice.getOperationType().getGroup().getOperationStatus()
                        )
                ),
                invoice.getInstallments().stream().map(
                        installment -> new InstallmentResponseDTO(
                                installment.getId(),
                                installment.getAmount(),
                                installment.getCreatedAt(),
                                installment.getDueDate(),
                                installment.getMovementType(),
                                installment.isPaid(),
                                installment.getParcelNumber(),
                                installment.getInvoice().getId(),
                                installment.getTransactions().stream().map(
                                        transaction -> new TransactionResponseDTO(
                                                transaction.getId(),
                                                transaction.getInstallment().getId(),
                                                transaction.getAccount().getId(),
                                                transaction.getAmount(),
                                                transaction.getInstallment().getMovementType(),
                                                transaction.getIsReversed(),
                                                transaction.getPaymentDate(),
                                                transaction.getCreatedAt(),
                                                transaction.getObservations()
                                        )
                                ).toList()
                        )
                ).toList()
        );
    }

}
