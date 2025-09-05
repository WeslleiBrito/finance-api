package com.project.financeapi.service;

import com.project.financeapi.entity.Transaction;
import com.project.financeapi.entity.TransactionType;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.AccountRepository;
import com.project.financeapi.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void reverseTransaction(String transactionId, String reason) {
        Transaction original = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Transação não encontrada."));

        if (original.getReversedTransaction() != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Esta transação já foi estornada.");
        }

        switch (original.getType()) {
            case TRANSFER -> reverseTransfer(original, reason);
            case PAYMENT -> reversePayment(original, reason);
            case DEPOSIT, WITHDRAW -> reverseSimpleTransaction(original, reason);
            default -> throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Tipo de transação não suportado para estorno.");
        }
    }

    // ------------------- REVERSÕES -------------------

    private void reverseSimpleTransaction(Transaction original, String reason) {
        AccountBase account = original.getAccount();
        BigDecimal reversedAmount = original.getAmount().negate();

        Transaction reversal = createReversal(original, account, reason);

        if (reversedAmount.compareTo(BigDecimal.ZERO) > 0) {
            account.deposit(reversedAmount);
        } else {
            account.withdraw(reversedAmount.abs());
        }

        transactionRepository.save(reversal);
        accountRepository.save(account);
    }

    private void reversePayment(Transaction original, String reason) {
        AccountBase account = original.getAccount();

        // Aqui poderia ter lógica para devolver para o fornecedor
        Transaction reversal = createReversal(original, account, reason);

        account.setBalance(account.getBalance().add(reversal.getAmount()));

        transactionRepository.save(reversal);
        accountRepository.save(account);
    }

    private void reverseTransfer(Transaction original, String reason) {
        AccountBase account = original.getAccount();

        // Localiza a conta de destino/origem (dependendo do sinal)
        // Supondo que tenha um campo "relatedAccount" na Transaction para transferências
        AccountBase relatedAccount = original.getRelatedAccount();

        // Reverter a conta original
        Transaction reversalOrigin = createReversal(original, account, reason);
        account.setBalance(account.getBalance().add(reversalOrigin.getAmount()));

        // Reverter a conta relacionada (faz a compensação)
        Transaction reversalDest = new Transaction(
                relatedAccount,
                TransactionType.REVERSAL,
                original.getAmount(), // sem negate, porque já era negativo/positivo conforme origem
                LocalDateTime.now(),
                (reason != null ? reason + " " : "") + "Estorno da transferência"
        );
        reversalDest.setReversedTransaction(original);

        relatedAccount.setBalance(relatedAccount.getBalance().add(reversalDest.getAmount()));

        transactionRepository.save(reversalOrigin);
        transactionRepository.save(reversalDest);
        accountRepository.save(account);
        accountRepository.save(relatedAccount);
    }

    // ------------------- UTILITÁRIO -------------------

    private Transaction createReversal(Transaction original, AccountBase account, String reason) {
        Transaction reversal = new Transaction(
                account,
                TransactionType.REVERSAL,
                original.getAmount().negate(),
                LocalDateTime.now(),
                (reason != null ? reason + " " : "") + "Estorno da transação " + original.getId()
        );
        reversal.setReversedTransaction(original);
        return reversal;
    }
}
