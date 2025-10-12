package com.project.financeapi.service;

import com.project.financeapi.dto.Installment.InstallmentDTO;
import com.project.financeapi.dto.Installment.InstallmentResponseDTO;
import com.project.financeapi.dto.OperationType.OperationTypeResponseDTO;
import com.project.financeapi.dto.account.ResponseAccountDTO;
import com.project.financeapi.dto.invoice.CreateInvoiceRequestDTO;
import com.project.financeapi.dto.invoice.InvoiceResponseDTO;
import com.project.financeapi.dto.operationGroup.OperationGroupResponseDTO;
import com.project.financeapi.dto.transaction.TransactionResponseDTO;
import com.project.financeapi.dto.user.ResponseUserDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.Invoice;
import com.project.financeapi.entity.Installment;
import com.project.financeapi.entity.OperationType;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.*;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {
    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final InvoiceRepository invoiceRepository;
    private final AccountRepository accountRepository;
    private final InstallmentRepository installmentRepository;
    private final OperationTypeRepository operationTypeRepository;

    private final JwtUtil jwtUtil;

    public InvoiceService(
            UserRepository userRepository,
            PersonRepository personRepository,
            InvoiceRepository invoiceRepository,
            AccountRepository accountRepository,
            InstallmentRepository installmentRepository,
            OperationTypeRepository operationTypeRepository,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.invoiceRepository = invoiceRepository;
        this.accountRepository = accountRepository;
        this.installmentRepository = installmentRepository;
        this.operationTypeRepository = operationTypeRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public InvoiceResponseDTO create(String token, CreateInvoiceRequestDTO dto){

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "O usuário informado não existe"));

        PersonBase person = personRepository.findById(dto.personId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "A pessoa informada não existe."));

        AccountBase account = accountRepository.findById(dto.accountId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "A conta informada não existe."));

        OperationType operationType = operationTypeRepository.findById(dto.operationTypeId())
                .orElseThrow(
                        () -> new BusinessException(HttpStatus.NOT_FOUND, "O tipo de operação informada não existe.")
                );

        if(!operationType.getIsGlobal() && !operationType.getCreatedBy().equals(user)){
            throw  new BusinessException(
                    HttpStatus.NOT_FOUND, "Você não tem permissão para usar esse tipo de operação."
            );
        }

        BigDecimal subtotalInstallments = dto.installments().stream().map(InstallmentDTO::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        if(dto.totalAmount().compareTo(subtotalInstallments) < 0){
            throw new BusinessException(
                    HttpStatus.NOT_FOUND, "O valor do total do documento é MENOR que a soma das parcelas"
            );
        }

        if(dto.totalAmount().compareTo(subtotalInstallments) > 0){
            throw new BusinessException(
                    HttpStatus.NOT_FOUND, "O valor do total do documento é MAIOR que a soma das parcelas"
            );
        }

       Invoice invoice = invoiceRepository.save(new Invoice(
               dto.totalAmount(),
               user,
               person,
               account,
               operationType
       ));


        for(InstallmentDTO installmentDTO : dto.installments()) {

            Installment installment = new Installment(
                    installmentDTO.amount(),
                    installmentDTO.dueDate(),
                    invoice.getOperationType().getMovementType(),
                    installmentDTO.parcelNumber(),
                    user,
                    invoice
            );

            installmentRepository.save(installment);
        }

        for(Installment installment : installmentRepository.findByInvoice(invoice)){
            invoice.addInstallment(installment);
        }

        return this.toDocumentResponseDTO(invoice);

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

    public InvoiceResponseDTO findById(String token, String id) {

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
    public InvoiceResponseDTO toDocumentResponseDTO(Invoice invoice) {

        return new InvoiceResponseDTO(
                invoice.getId(),
                new OperationTypeResponseDTO(
                  invoice.getOperationType().getId(),
                  invoice.getOperationType().getName(),
                  invoice.getOperationType().getMovementType(),
                  invoice.getOperationType().getOperationStatus(),
                  invoice.getOperationType().getIsGlobal(),
                  new OperationGroupResponseDTO(
                          invoice.getOperationType().getGroup().getId(),
                          invoice.getOperationType().getGroup().getName(),
                          invoice.getOperationType().getGroup().getIsGlobal(),
                          invoice.getOperationType().getGroup().getOperationStatus()
                  )
                ),
                invoice.getIssueDate(),
                invoice.getStatus(),
                invoice.getQuantityInstallments(),
                invoice.getTotalAmount(),
                invoice.getTotalPaid(),
                invoice.getRemainingBalance(),
                new ResponseUserDTO(
                        invoice.getCreatedBy().getId(),
                        invoice.getCreatedBy().getName(),
                        invoice.getCreatedBy().getUserStatus()
                ),
                new ResponseAccountDTO(
                        invoice.getAccount().getId(),
                        invoice.getAccount().getName(),
                        invoice.getAccount().getType(),
                        invoice.getAccount().getBalance(),
                        invoice.getAccount().getStatus()
                ),
                invoice.getInstallments().stream().map(installment -> new InstallmentResponseDTO(
                        installment.getId(),
                        installment.getAmount(),
                        installment.getCreatedAt(),
                        installment.getDueDate(),
                        installment.getMovementType(),
                        installment.getStatus(),
                        installment.getParcelNumber(),
                        installment.getInvoice().getId(),
                        new ResponseUserDTO(
                                installment.getCreatedBy().getId(),
                                installment.getCreatedBy().getName(),
                                installment.getCreatedBy().getUserStatus()
                        ),
                        installment.getTransactions().stream().map(transaction -> new TransactionResponseDTO(
                                transaction.getId(),
                                transaction.getInstallment().getId(),
                                transaction.getInstallment().getInvoice().getId(),
                                transaction.getAccount().getId(),
                                transaction.getMovementType(),
                                transaction.getAmount(),
                                transaction.getIssueDate(),
                                transaction.getDueDate(),
                                transaction.getPaymentDate(),
                                new ResponseUserDTO(
                                        transaction.getCreatedBy().getId(),
                                        transaction.getCreatedBy().getName(),
                                        transaction.getCreatedBy().getUserStatus()
                                ),
                                transaction.getObservations(),
                                transaction.getCreatedAt()
                        )).toList()

                )).toList()
        );
    }

}
