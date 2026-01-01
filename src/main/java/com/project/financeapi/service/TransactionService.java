package com.project.financeapi.service;

import com.project.financeapi.dto.transaction.CreateTransactionDTO;
import com.project.financeapi.dto.transaction.CreateTransactionRequestDTO;
import com.project.financeapi.dto.transaction.TransactionResponseDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.*;
import com.project.financeapi.entity.account.CheckingAccount;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.enums.AccountType;
import com.project.financeapi.enums.MovementDirection;
import com.project.financeapi.enums.MovementType;
import com.project.financeapi.enums.PaymentType;
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

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final InstallmentRepository installmentRepository;
    private final PaymentInstrumentRepository paymentInstrumentRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public List<TransactionResponseDTO> createCommonTransactions(
            String token,
            CreateTransactionRequestDTO request
    ) {

        // 🔐 Usuário
        JwtPayload payload = jwtUtil.extractPayload(token);
        User user = userRepository.findById(payload.id())
                .orElseThrow(() ->
                        new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado.")
                );

        // 📊 Acumuladores
        Map<UUID, BigDecimal> installmentTotals = new HashMap<>();
        Map<UUID, BigDecimal> accountTotals = new HashMap<>();

        List<Transaction> transactions = new ArrayList<>();

        for (CreateTransactionDTO dto : request.transactions()) {

            // 📦 Parcela
            Installment installment = installmentRepository.findById(dto.installmentId())
                    .orElseThrow(() ->
                            new BusinessException(HttpStatus.NOT_FOUND, "Parcela não encontrada.")
                    );

            if (!installment.getInvoice().getCreatedBy().getId().equals(user.getId())) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "Parcela não pertence ao usuário.");
            }

            // 🏦 Conta
            AccountBase account = accountRepository.findById(dto.accountId())
                    .orElseThrow(() ->
                            new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada.")
                    );

            if (!account.getAccountHolder().getId().equals(user.getId())) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "Conta não pertence ao usuário.");
            }

            // 💳 Instrumento (opcional)
            PaymentInstrumentBase instrument = null;
            if (dto.paymentInstrumentId() != null) {
                instrument = paymentInstrumentRepository
                        .findByIdAndUser(dto.paymentInstrumentId(), user.getId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        HttpStatus.NOT_FOUND,
                                        "Instrumento de pagamento não encontrado."
                                )
                        );
            }

            validateAccountAndInstrumentCompatibility(account, instrument);

            // 💰 Valores
            BigDecimal interest = defaultZero(dto.interest());
            BigDecimal fine = defaultZero(dto.fine());
            BigDecimal discount =  defaultZero(dto.discount());

            BigDecimal effectiveAmount = dto.amount()
                    .add(interest)
                    .add(fine)
                    .subtract(discount);

            if (effectiveAmount.signum() <= 0) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "Valor efetivo da transação deve ser maior que zero."
                );
            }

            // 📊 Acúmulo
            installmentTotals.merge(
                    installment.getId(),
                    effectiveAmount,
                    BigDecimal::add
            );

            accountTotals.merge(
                    account.getId(),
                    effectiveAmount,
                    BigDecimal::add
            );

            // 🧾 Criação
            Transaction transaction = new Transaction(
                    dto.amount(),
                    interest,
                    fine,
                    discount,
                    installment.getMovementType() == MovementType.PAYMENT
                            ? MovementDirection.OUTFLOW
                            : MovementDirection.INFLOW,
                    installment.getMovementType(),
                    dto.paymentDate(),
                    user,
                    account,
                    installment,
                    null,
                    instrument,
                    dto.observations()
            );

            transactions.add(transaction);
        }

        // 🔎 Validação das parcelas
        for (Map.Entry<UUID, BigDecimal> entry : installmentTotals.entrySet()) {
            Installment installment = installmentRepository.findById(entry.getKey()).orElseThrow();

            BigDecimal totalAfterPayment = installment.getTotalPaid().add(entry.getValue());

            if (totalAfterPayment.compareTo(installment.getAmount()) > 0) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "Pagamento excede o valor da parcela."
                );
            }
        }

        // 🔎 Validação das contas
        for (Map.Entry<UUID, BigDecimal> entry : accountTotals.entrySet()) {
            AccountBase account = accountRepository.findById(entry.getKey()).orElseThrow();

            BigDecimal availableBalance = account.getBalance();

            if (account.getType() == AccountType.CHECKING) {
                availableBalance = availableBalance.add(
                        ((CheckingAccount) account).getOverdraftLimit()
                );
            }

            if (availableBalance.compareTo(entry.getValue()) < 0) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "Saldo insuficiente para realizar as transações."
                );
            }
        }

        // 💾 Persistência
        return transactionRepository.saveAll(transactions).stream().map(
                Transaction::toResponse
        ).toList();
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private void validateAccountAndInstrumentCompatibility(
            AccountBase account,
            PaymentInstrumentBase instrument
    ) {
        if (account.getType() == AccountType.WALLET) {

            if (instrument == null || instrument.getPaymentType() != PaymentType.CASH) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "Contas do tipo WALLET só podem ser movimentadas com instrumento CASH."
                );
            }

        } else {
            // Conta NÃO é WALLET
            if (instrument != null && instrument.getPaymentType() == PaymentType.CASH) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "Instrumento CASH só pode ser usado com contas do tipo WALLET."
                );
            }
        }
    }

}
