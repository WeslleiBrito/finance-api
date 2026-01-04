package com.project.financeapi.service;

import com.project.financeapi.dto.Installment.InstallmentDTO;
import com.project.financeapi.dto.invoice.CreateInvoiceRequestDTO;
import com.project.financeapi.dto.invoice.InvoiceResponseDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.*;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.*;
import com.project.financeapi.util.JwtUtil;
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

        return invoice.toResponse();
    }

    public List<InvoiceResponseDTO> findAll(String token) {

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "O usuário informado não existe"));


        List<Invoice> invoices = invoiceRepository.findByCreatedBy(user);

        return invoices.stream().map(Invoice::toResponse).toList();
    }

    public InvoiceResponseDTO findById(String token, UUID id) {

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "O usuário informado não existe"));

        Invoice invoice = invoiceRepository.findByIdAndCreatedBy(id, user).orElseThrow(() -> new RuntimeException(
                "O documento informado não exite."
        ));

        return invoice.toResponse();

    }

}
