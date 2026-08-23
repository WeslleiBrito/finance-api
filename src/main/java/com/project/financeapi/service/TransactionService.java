package com.project.financeapi.service;

import com.project.financeapi.dto.transaction.*;
import com.project.financeapi.entity.*;
import com.project.financeapi.entity.account.CheckingAccount;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.enumSystem.AccountType;
import com.project.financeapi.enumSystem.MovementDirection;
import com.project.financeapi.enumSystem.MovementType;
import com.project.financeapi.enumSystem.PaymentType;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final InstallmentRepository installmentRepository;
    private final PaymentInstrumentRepository paymentInstrumentRepository;
    private final TransactionRepository transactionRepository;
    private final UserContextService userContextService;

    @Transactional
    public List<TransactionResponseDTO> createCommonTransactions(
            CreateTransactionRequestDTO request
    ) {

        User user = userContextService.getAuthenticatedUser();

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

            if(LocalDate.now().isBefore(dto.paymentDate())){
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "A data informada não pode ser mair que a data atual."
                );
            }
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

    // ==========================================
    // MÉTODOS NOVOS: BUSCA E ESTORNO
    // ==========================================

    public List<TransactionResponseDTO> findAllByUser() {
        User user = userContextService.getAuthenticatedUser();

        return transactionRepository.findAllByUserIdOrderByPaymentDateDesc(user.getId())
                .stream()
                .map(Transaction::toResponse)
                .toList();
    }

    @Transactional
    public TransactionResponseDTO reverseTransaction(UUID transactionId) {
        User user = userContextService.getAuthenticatedUser();

        // 1. Busca a transação original
        Transaction original = transactionRepository.findByIdAndUserId(transactionId, user.getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Transação não encontrada."));

        // (Opcional) Regra de negócio para não estornar duas vezes
        if (original.getMovementType() == MovementType.REVERSAL) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Esta transação já é um estorno.");
        }

        // 2. Cria a transação reversa (Estorno)
        Transaction reversal = new Transaction(
                original.getAmount(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                original.getMovementDirection() == MovementDirection.INFLOW ? MovementDirection.OUTFLOW : MovementDirection.INFLOW,
                MovementType.REVERSAL, // 🌟 É AQUI QUE A MÁGICA ACONTECE!
                LocalDate.now(),
                user,
                original.getAccount(),
                original.getInstallment(),
                null,
                original.getPaymentInstrument(),
                "Estorno da transação " + original.getId()
        );

        // 3. Salva a nova transação
        // Como AccountBase e Installment são calculados dinamicamente,
        // apenas salvar o estorno já regulariza o saldo da conta e o status da parcela!
        Transaction savedReversal = transactionRepository.save(reversal);

        return savedReversal.toResponse();
    }
    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    @Transactional
    public List<TransactionResponseDTO> createManualAdjustments(CreateManualAdjustmentTransactionRequestDTO request) {
        User user = userContextService.getAuthenticatedUser();

        // 📊 Acumulador de impacto financeiro por conta para validação de saldo
        Map<UUID, BigDecimal> accountBalanceImpact = new HashMap<>();
        List<Transaction> transactions = new ArrayList<>();

        for (CreateManualAdjustmentTransactionDTO dto : request.dto()) {

            // 1. Validar a Conta
            AccountBase account = accountRepository.findById(dto.accountId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada."));

            if (!account.getAccountHolder().getId().equals(user.getId())) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "A conta não pertence ao usuário.");
            }

            // 2. Validar o Instrumento de Pagamento (se houver)
            PaymentInstrumentBase instrument = null;
            if (dto.paymentInstrumentId() != null) {
                instrument = paymentInstrumentRepository.findByIdAndUser(dto.paymentInstrumentId(), user.getId())
                        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Instrumento não encontrado."));
            }

            validateAccountAndInstrumentCompatibility(account, instrument);

            // 3. Validação de Data
            if(LocalDate.now().isBefore(dto.paymentDate())){
                throw new BusinessException(HttpStatus.BAD_REQUEST, "A data do ajuste não pode ser maior que a data atual.");
            }

            // 4. Calcular o impacto da transação no saldo (- se saída, + se entrada)
            BigDecimal impact = dto.direction() == MovementDirection.OUTFLOW
                    ? dto.amount().negate()
                    : dto.amount();

            accountBalanceImpact.merge(account.getId(), impact, BigDecimal::add);

            // 5. Instanciar a Transação Avulsa (Ajuste)
            Transaction transaction = new Transaction(
                    dto.amount(),
                    BigDecimal.ZERO, // Sem juros
                    BigDecimal.ZERO, // Sem multas
                    BigDecimal.ZERO, // Sem descontos
                    dto.direction(),

                    // 🌟 DICA DE DOMÍNIO: Se você tiver "ADJUSTMENT" no enum MovementType, use-o.
                    // Caso contrário, deduzimos pela direção:
                    dto.direction() == MovementDirection.OUTFLOW ? MovementType.PAYMENT : MovementType.RECEIPT,

                    dto.paymentDate(),
                    user,
                    account,
                    null, // 🌟 Aqui está o pulo do gato: Installment é NULL!
                    null, // ReversalOf é NULL
                    instrument,
                    dto.reason() // Salvamos o motivo no campo observations
            );

            transactions.add(transaction);
        }

        // 6. Validar limite de saque para contas correntes (Check de Saldo)
        for (Map.Entry<UUID, BigDecimal> entry : accountBalanceImpact.entrySet()) {
            if (entry.getValue().signum() < 0) { // Se o saldo final do lote for negativo (saída)
                AccountBase account = accountRepository.findById(entry.getKey()).orElseThrow();
                BigDecimal availableBalance = account.getBalance();

                if (account.getType() == AccountType.CHECKING) {
                    availableBalance = availableBalance.add(((CheckingAccount) account).getOverdraftLimit());
                }

                // Subtrai (soma o impacto que já está negativo) e verifica se "estourou" o limite
                if (availableBalance.add(entry.getValue()).compareTo(BigDecimal.ZERO) < 0) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "Saldo insuficiente para realizar o ajuste de saída na conta.");
                }
            }
        }

        // 7. Persistir os ajustes
        return transactionRepository.saveAll(transactions).stream()
                .map(Transaction::toResponse)
                .toList();
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
