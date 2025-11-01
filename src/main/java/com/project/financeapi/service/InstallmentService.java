package com.project.financeapi.service;

import com.project.financeapi.dto.Installment.CreateInstallmentDTO;
import com.project.financeapi.dto.Installment.InstallmentDTO;
import com.project.financeapi.dto.Installment.InstallmentResponseDTO;
import com.project.financeapi.dto.transaction.TransactionResponseDTO;
import com.project.financeapi.dto.user.UserResponseDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.Invoice;
import com.project.financeapi.entity.Installment;
import com.project.financeapi.entity.User;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.InvoiceRepository;
import com.project.financeapi.repository.InstallmentRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class InstallmentService {
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final InstallmentRepository installmentRepository;
    private final JwtUtil jwtUtil;

    public InstallmentService(
            UserRepository userRepository,
            InvoiceRepository invoiceRepository,
            InstallmentRepository installmentRepository,
            JwtUtil jwtUtil
        )
    {
        this.userRepository = userRepository;
        this.invoiceRepository = invoiceRepository;
        this.installmentRepository = installmentRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public List<Installment> create(String token, CreateInstallmentDTO dto){

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        Invoice invoice = invoiceRepository.findById(dto.documentId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "O documento informado não foi localizado."));

        Installment[] installmentsList = new Installment[dto.installments().size()];

        int index = 0;

        for(InstallmentDTO item : dto.installments()) {

            Installment installment = new Installment(
                    item.amount(),
                    item.dueDate(),
                    dto.movementType(),
                    item.parcelNumber(),
                    user,
                    invoice
            );

            installmentRepository.save(installment);

            installmentsList[index] = installment;

            index ++;
        }

        return List.of(installmentsList);
    }

    public InstallmentResponseDTO toInstallmentResponseDTO(Installment installment) {

        // converter as transações da parcela
        List<TransactionResponseDTO> transactionDTOs = installment.getTransactions().stream()
                .map(transaction -> new TransactionResponseDTO(
                        transaction.getId(),
                        transaction.getInstallment().getId(),
                        transaction.getInstallment().getInvoice().getId(),
                        transaction.getAccount().getId(),
                        transaction.getMovementType(),
                        transaction.getAmount(),
                        transaction.getInstallment().getInvoice().getIssueDate(),
                        transaction.getInstallment().getDueDate(),
                        transaction.getSettlementDate(),
                        transaction.getObservations(),
                        transaction.getCreatedAt()
                ))
                .toList();

        return new InstallmentResponseDTO(
                installment.getId(),
                installment.getAmount(),
                installment.getCreatedAt(),
                installment.getDueDate(),
                installment.getMovementType(),
                installment.getStatus(),
                installment.getParcelNumber(),
                installment.getInvoice().getId(),
                new UserResponseDTO(
                        installment.getCreatedBy().getId(),
                        installment.getCreatedBy().getName(),
                        installment.getCreatedBy().getUserStatus()
                ),
                transactionDTOs
        );
    }
}
