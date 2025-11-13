package com.project.financeapi.service;

import com.project.financeapi.dto.transaction.TransactionDTO;
import com.project.financeapi.dto.transaction.TransactionRequestDTO;
import com.project.financeapi.dto.transaction.TransactionResponseDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.*;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.*;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;


@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final InstallmentRepository installmentRepository;
    private final JwtUtil jwtUtil;


    // =======================
    // PRINCIPAL MÉTODO
    // =======================
    @Transactional
    public List<TransactionResponseDTO> create(String token, TransactionRequestDTO dto) {
        User user = getAuthenticatedUser(token);

        List<Transaction> transactions = transactionRepository.saveAll(dto.itens().stream()
                .map(item -> buildTransaction(user, item))
                .toList());


        return transactions.stream()
                .map(this::responseDTO)
                .toList();
    }


    // =======================
    // MÉTODOS AUXILIARES
    // =======================

    private Transaction buildTransaction(User user, TransactionDTO item) {

        Installment installment = getAndValidateInstallment(item.installmentId(), user);

        AccountBase account = getAndValidateAccount(item.accountId(), user);


        BigDecimal remaining = installment.getRemainingBalance();
        if (item.amount().compareTo(remaining) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Valor informado (" + item.amount() + ") ultrapassa o saldo da parcela (" + remaining + ").");
        }

        return new Transaction(
                user,
                account,
                installment,
                item.amount(),
                item.paymentDate(),
                item.isReversed(),
                item.observations()
        );

    }

    public TransactionResponseDTO responseDTO(Transaction transaction) {

        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getInstallment().getId(),
                transaction.getAccount().getId(),
                transaction.getAmount(),
                transaction.getInstallment().getMovementType(),
                transaction.getIsReversed(),
                transaction.getPaymentDate(),
                transaction.getCreatedAt(),
                transaction.getObservations()
        );
    }

    private User getAuthenticatedUser(String token) {
        JwtPayload payload = jwtUtil.extractPayload(token);
        return userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }


    private AccountBase getAndValidateAccount(UUID accountId, User user) {
        AccountBase account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada"));

        if (!account.getAccountHolder().getId().equals(user.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Você não tem acesso a esta conta.");
        }

        return account;
    }

    private Installment getAndValidateInstallment(UUID installmentId, User user) {

        if (installmentId == null) return null;

        return installmentRepository.findCreditCardByCreatedByAndId(user.getId(), installmentId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Instrumento não encontrado."));
    }


}
