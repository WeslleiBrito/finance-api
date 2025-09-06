package com.project.financeapi.service;

import com.project.financeapi.dto.account.CreateAccountDTO;
import com.project.financeapi.dto.account.UpdateAccountDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.*;
import com.project.financeapi.enums.AccountType;
import com.project.financeapi.enums.TransactionType;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.AccountRepository;
import com.project.financeapi.repository.TransactionRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;


@Service
public class AccountService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(TransactionRepository transactionRepository,
                          AccountRepository accountRepository,
                          UserRepository userRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AccountBase createAccount(String token, CreateAccountDTO dto) {


        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        AccountBase account = null;

        switch (dto.type()) {
            case CHECKING -> account = new CheckingAccount(user);
            case SAVINGS -> account = new SavingsAccount(user);
            case PAYMENT -> account = new PaymentAccount(user);
            case WALLET -> account = new WalletAccount(user);
            case INVESTMENT -> account = new InvestmentAccount(user);
        }

        account.setName(dto.name());

        return accountRepository.save(account);

    }

    @Transactional
    public AccountBase updateAccount(String token, String accountId, UpdateAccountDTO dto) {

        JwtPayload userToken = JwtUtil.extractPayload(token);

        AccountBase account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada"));


        if (!account.getAccountHolder().getId().equals(userToken.id())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar esta conta");
        }

        if (dto.name() != null && !dto.name().isBlank()) {
            account.setName(dto.name());
        }

        if (dto.type() != null) {
            account.setType(dto.type());
        }

        if (dto.status() != null) {
            account.setStatus(dto.status());
        }

        return accountRepository.save(account);
    }

    @Transactional
    public void transfer(
            AccountBase from,
            AccountBase to,
            BigDecimal amount,
            LocalDate issueDate,
            LocalDate dueDate,
            String observations
    )
    {

        boolean isChecking = from.getType().equals(AccountType.CHECKING);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "O valor da transferência deve ser positivo.");
        }

        if (!isChecking && from.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Saldo insuficiente na conta de origem.");
        }

        transactionRepository.save(new Transaction(from, TransactionType.TRANSFER_IN, amount.negate(), issueDate, dueDate,
                (observations != null ? observations + "\n" : "") + "Transferência para " + to.getName()));
        transactionRepository.save(new Transaction(to, TransactionType.TRANSFER_IN, amount, issueDate, dueDate,
                (observations != null ? observations + "\n" : "") + "Transferência recebida de " + from.getName()));
    }

}
