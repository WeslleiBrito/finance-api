package com.project.financeapi.service;

import com.project.financeapi.entity.AccountType;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AccountService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public AccountService(TransactionRepository transactionRepository,
                          AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void transfer(AccountBase from, AccountBase to, BigDecimal amount, String description) {

        boolean isChecking = from.getType().equals(AccountType.CHECKING);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "O valor da transferência deve ser positivo.");
        }
        if (!isChecking && from.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Saldo insuficiente na conta de origem.");
        }

        from.withdraw(amount);
        to.deposit(amount);


        transactionRepository.save(new Transaction(from, TransactionType.TRANSFER, amount.negate(), LocalDateTime.now(),
                (description != null ? description + "\n" : "") + "Transferência para " + to.getName()));
        transactionRepository.save(new Transaction(to, TransactionType.TRANSFER, amount,LocalDateTime.now(),
                (description != null ? description + "\n" : "") + "Transferência recebida de " + from.getName()));

        accountRepository.save(from);
        accountRepository.save(to);
    }

}
