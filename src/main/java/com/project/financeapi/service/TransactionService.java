package com.project.financeapi.service;

import com.project.financeapi.dto.transaction.TransactionDTO;
import com.project.financeapi.dto.transaction.TransactionRequestDTO;
import com.project.financeapi.dto.transaction.TransactionResponseDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.Document;
import com.project.financeapi.entity.Installment;
import com.project.financeapi.entity.Transaction;
import com.project.financeapi.entity.User;
import com.project.financeapi.enums.MovementType;
import com.project.financeapi.enums.TransactionType;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.*;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final InstallmentRepository installmentRepository;
    private final DocumentRepository documentRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository,
                              UserRepository userRepository, InstallmentRepository installmentRepository,
                              DocumentRepository documentRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.installmentRepository = installmentRepository;
        this.documentRepository = documentRepository;
    }

    @Transactional
    public List<TransactionResponseDTO> create(String token, TransactionRequestDTO dto) {
        JwtPayload payload = JwtUtil.extractPayload(token);
        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        Set<String> repetidasNoLote = new HashSet<>();
        for (TransactionDTO t : dto.transactions()) {
            if (!repetidasNoLote.add(t.installmentId())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "A parcela " + t.installmentId() + " foi informada mais de uma vez no mesmo lote.");
            }
        }

        List<Transaction> toPersist = new ArrayList<>();
        List<Installment> installmentsToUpdate = new ArrayList<>();

        for (TransactionDTO item : dto.transactions()) {
            Installment installment = installmentRepository.findById(item.installmentId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Parcela não encontrada"));

            if (!installment.getCreatedBy().getId().equals(user.getId())) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "Você não tem acesso a esta parcela.");
            }

            AccountBase account = accountRepository.findById(dto.accountId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada"));

            if (!account.getAccountHolder().getId().equals(user.getId())) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "Você não tem acesso a esta conta.");
            }


            BigDecimal remaining = installment.getRemainingBalance();
            if (item.amount().compareTo(remaining) > 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "Valor informado (" + item.amount() + ") ultrapassa o saldo da parcela (" + remaining + "). Parcela: " + installment.getId());
            }

            TransactionType type = (installment.getMovementType() == MovementType.CREDIT)
                    ? TransactionType.DEPOSIT
                    : TransactionType.PAYMENT;

            LocalDate paymentDate = (item.paymentDate() != null) ? item.paymentDate() : LocalDate.now();

            Transaction tx = new Transaction(
                    user,
                    account,
                    type,
                    item.amount(),
                    installment.getDocument().getIssueDate(),
                    installment.getDueDate(),
                    paymentDate,
                    item.observations()
            );
            tx.setInstallment(installment);
            tx.finalizeTransaction();

            account.getTransactions().add(tx);
            installment.getTransactions().add(tx);

            toPersist.add(tx);
            installmentsToUpdate.add(installment);
        }


        List<Transaction> saved = transactionRepository.saveAll(toPersist);
        installmentRepository.saveAll(installmentsToUpdate);
        documentRepository.saveAll(
                installmentsToUpdate.stream()
                        .map(Installment::getDocument)
                        .distinct()
                        .peek(Document::updateStatus)
                        .toList()
        );

        TransactionResponseDTO [] transactionResponse = new TransactionResponseDTO[saved.size()];

        int index = 0;

        for(Transaction item: saved){

            transactionResponse[index] = new TransactionResponseDTO(
                    item.getId(),
                    item.getInstallment().getId(),
                    item.getInstallment().getDocument().getId(),
                    item.getAccount().getId(),
                    item.getMovementType(),
                    item.getAmount(),
                    item.getIssueDate(),
                    item.getDueDate(),
                    item.getPaymentDate(),
                    item.getObservations() != null ? item.getObservations() : "",
                    item.getCreatedAt()
            );

            index ++;
        }

        return List.of(transactionResponse);
    }


}
