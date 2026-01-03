package com.project.financeapi.service;

import com.project.financeapi.dto.Installment.CreateInstallmentDTO;
import com.project.financeapi.dto.Installment.InstallmentDTO;
import com.project.financeapi.dto.Installment.InstallmentResponseDTO;
import com.project.financeapi.dto.transaction.TransactionResponseDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.Invoice;
import com.project.financeapi.entity.Installment;
import com.project.financeapi.entity.Transaction;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.InvoiceRepository;
import com.project.financeapi.repository.InstallmentRepository;
import com.project.financeapi.repository.PaymentInstrumentRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@RequiredArgsConstructor
public class InstallmentService {
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final InstallmentRepository installmentRepository;
    private final JwtUtil jwtUtil;
    private final PaymentInstrumentRepository paymentInstrumentRepository;

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
            PaymentInstrumentBase paymentInstrument =
                    paymentInstrumentRepository.findByIdAndUser(item.instrument(), user.getId())
                            .orElseThrow(() ->
                                    new BusinessException(HttpStatus.NOT_FOUND, "Método de pagamento não encontrado.")
                            );
            Installment installment = new Installment(
                    item.amount(),
                    item.dueDate(),
                    dto.movementType(),
                    item.parcelNumber(),
                    user,
                    invoice,
                    paymentInstrument
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
                .map(Transaction::toResponse)
                .toList();

        return new InstallmentResponseDTO(
                installment.getId(),
                installment.getInvoice().getId(),
                installment.getParcelNumber(),
                installment.getAmount(),
                installment.getTotalPaid(),
                installment.getTotalInterest(),
                installment.getTotalFine(),
                installment.getTotalDiscount(),
                installment.getMovementType(),
                installment.isPaid(),
                installment.getDueDate(),
                installment.getCreatedAt(),
                installment.toResponse().transactions()
        );
    }
}
