package com.project.financeapi.service;

import com.project.financeapi.dto.account.CreateAccountRequestDTO;
import com.project.financeapi.dto.account.UpdateAccountRequestDTO;
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
    public AccountBase create(String token, CreateAccountRequestDTO dto) {

        JwtPayload payload = JwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        AccountBase account = null;

        switch (dto.type()) {
            case CHECKING -> account = new CheckingAccount(user, dto.initialValue());
            case SAVINGS -> account = new SavingsAccount(user, dto.initialValue());
            case PAYMENT -> account = new PaymentAccount(user, dto.initialValue());
            case WALLET -> account = new WalletAccount(user, dto.initialValue());
            case INVESTMENT -> account = new InvestmentAccount(user, dto.initialValue());
        }

        account.setName(dto.name());

        return accountRepository.save(account);

    }

    @Transactional
    public AccountBase update(String token, String accountId, UpdateAccountRequestDTO dto) {

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

//    @Transactional
//    public void transfer(
//            String token,
//            AccountBase from,
//            AccountBase to,
//            BigDecimal amount,
//            LocalDate issueDate,
//            LocalDate dueDate,
//            LocalDate paymentDate,
//            String observations
//    )
//    {
//
//        JwtPayload payload = JwtUtil.extractPayload(token);
//
//        User createdBy = userRepository.findById(payload.id()).orElseThrow(
//                () -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado")
//        );
//
//        boolean isChecking = from.getType().equals(AccountType.CHECKING);
//
//        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
//            throw new BusinessException(HttpStatus.BAD_REQUEST, "O valor da transferência deve ser positivo.");
//        }
//
//        if (!isChecking && from.getBalance().compareTo(amount) < 0) {
//            throw new BusinessException(HttpStatus.BAD_REQUEST, "Saldo insuficiente na conta de origem.");
//        }
//
//        transactionRepository.save(new Transaction(createdBy, from, TransactionType.TRANSFER_OUT,
//                amount.negate(), paymentDate,
//                issueDate, dueDate, (observations != null ? observations + "\n" : "") +
//                "Transferência para " + to.getName()));
//        transactionRepository.save(new Transaction(createdBy, to, TransactionType.TRANSFER_IN, amount,
//                issueDate, dueDate, paymentDate,
//                (observations != null ? observations + "\n" : "") + "Transferência recebida de " + from.getName()));
//    }

}
