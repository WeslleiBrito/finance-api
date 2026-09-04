package com.project.financeapi.service;

import com.project.financeapi.dto.transaction.*;
import com.project.financeapi.entity.*;
import com.project.financeapi.entity.account.CheckingAccount;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.enumSystem.*;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    public List<TransactionResponseDTO> createCommonTransactions(CreateTransactionRequestDTO request) {

        User user = userContextService.getAuthenticatedUser();

        // 📊 Acumuladores de impacto para validação em lote
        Map<UUID, BigDecimal> principalAmortizationTotals = new HashMap<>(); // Apenas o valor principal amortiza a parcela
        Map<UUID, BigDecimal> accountTotals = new HashMap<>(); // Impacto total (efetivo) na conta

        List<Transaction> transactions = new ArrayList<>();

        for (CreateTransactionDTO dto : request.transactions()) {

            // 1. Busca da Parcela com LOCK PESSIMISTA
            Installment installment = installmentRepository.findByIdForUpdate(dto.installmentId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Parcela não encontrada."));

            if (!installment.getInvoice().getCreatedBy().getId().equals(user.getId())) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "Parcela não pertence ao usuário.");
            }

            // 2. Busca da Conta com LOCK PESSIMISTA
            AccountBase account = accountRepository.findByIdForUpdate(dto.accountId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada."));

            if (!account.getAccountHolder().getId().equals(user.getId())) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "Conta não pertence ao usuário.");
            }

            // 3. Busca do Instrumento
            PaymentInstrumentBase instrument = null;
            if (dto.paymentInstrumentId() != null) {
                instrument = paymentInstrumentRepository
                        .findByIdAndUser(dto.paymentInstrumentId(), user.getId())
                        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Instrumento de pagamento não encontrado."));
            }

            validateAccountAndInstrumentCompatibility(account, instrument);

            // 4. Validação da Data
            if (LocalDate.now().isBefore(dto.paymentDate())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "A data informada não pode ser futura à data atual.");
            }

            // 5. Cálculos Financeiros
            BigDecimal interest = defaultZero(dto.interest());
            BigDecimal fine = defaultZero(dto.fine());
            BigDecimal discount = defaultZero(dto.discount());
            BigDecimal principalAmount = dto.amount(); // O valor nominal que amortiza a dívida

            // O valor que sai da conta (Efetivo) = Principal + Juros + Multas - Descontos
            BigDecimal effectiveAmount = principalAmount.add(interest).add(fine).subtract(discount);

            if (effectiveAmount.signum() <= 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Valor efetivo da transação deve ser maior que zero.");
            }

            // Acumula APENAS o valor principal amortizado para bater com o total da parcela
            principalAmortizationTotals.merge(installment.getId(), principalAmount, BigDecimal::add);

            // Acumula o valor EFETIVO para deduzir do saldo da conta
            if(installment.getMovementDirection() == MovementDirection.OUTFLOW) {
                accountTotals.merge(account.getId(), effectiveAmount, BigDecimal::add);
            }

            // 🌟 6. RESTITUIÇÃO DE LIMITE DO CARTÃO DE CRÉDITO
            // Se a parcela que estamos pagando foi feita no Cartão, o pagamento libera o limite
            if (installment.getPaymentInstrument() instanceof CreditCard card) {
                card.freeUpLimit(principalAmount); // Apenas o valor principal devolve o limite
                paymentInstrumentRepository.save(card);
            }

            // 7. Instancia a Transação
            Transaction transaction = new Transaction(
                    principalAmount,
                    interest,
                    fine,
                    discount,
                    installment.getMovementDirection(),
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

        // 8. Validação de Sobrepaga da Parcela (Apenas valor Principal)
        for (Map.Entry<UUID, BigDecimal> entry : principalAmortizationTotals.entrySet()) {
            Installment installment = installmentRepository.findByIdForUpdate(entry.getKey()).orElseThrow();

            BigDecimal totalAmortizedAfterPayment = installment.getTotalPaid().add(entry.getValue());

            if (totalAmortizedAfterPayment.compareTo(installment.getAmount()) > 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "O pagamento (sem juros/multas) excede o valor original da parcela.");
            }
        }

        // 9. Validação do Saldo da Conta
        for (Map.Entry<UUID, BigDecimal> entry : accountTotals.entrySet()) {
            AccountBase account = accountRepository.findByIdForUpdate(entry.getKey()).orElseThrow();

            validateSufficientFunds(
                    account,
                    entry.getValue(),
                    "Saldo insuficiente na conta '" + account.getName() + "' para realizar as transações."
            );
        }

        // 10. Persistência
        return transactionRepository.saveAll(transactions).stream()
                .map(Transaction::toResponse)
                .toList();
    }

    public Page<TransactionResponseDTO> findAllByUser(Pageable pageable) {
        User user = userContextService.getAuthenticatedUser();

        // Passa o pageable direto pro Repositório
        Page<Transaction> transactionsPage = transactionRepository.findAllByUserId(user.getId(), pageable);

        // O Spring mapeia cada elemento da página mantendo os dados de paginação (total, página atual, etc)
        return transactionsPage.map(Transaction::toResponse);
    }

    @Transactional
    public TransactionResponseDTO reverseTransaction(UUID transactionId, ReversalRequestDTO dto) {
        User user = userContextService.getAuthenticatedUser();

        // 1. Busca a transação original
        Transaction original = transactionRepository.findByIdAndUserId(transactionId, user.getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Transação não encontrada."));

        // Regra de negócio para não estornar duas vezes ou estornar um estorno
        if (original.getMovementType() == MovementType.REVERSAL) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Esta transação já é um estorno.");
        }

        if (transactionRepository.existsByReversalOfId(original.getId()) || original.isReversed()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Esta transação já foi estornada anteriormente.");
        }

        String finalReason = (dto != null && dto.reason() != null && !dto.reason().trim().isEmpty())
                ? dto.reason()
                : "Estorno da transação " + original.getId();

        // LÓGICA DE ESTORNO DUPLO PARA TRANSFERÊNCIAS
        if (original.getMovementType() == MovementType.TRANSFER && original.getLinkedTransaction() != null) {
            Transaction sibling = original.getLinkedTransaction();

            if (transactionRepository.existsByReversalOfId(sibling.getId()) || sibling.isReversed()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "A contraparte desta transferência já foi estornada.");
            }

            if (original.getMovementDirection() == MovementDirection.INFLOW) {
                validateSufficientFunds(
                        original.getAccount(),
                        original.getAmount(),
                        "Saldo insuficiente para estornar este recebimento. A conta ficaria negativa além do limite."
                );
            }

            Transaction originalReversal = new Transaction(
                    original.getAmount(), original.getInterest(), original.getFine(), original.getDiscount(),
                    original.getMovementDirection().toggle(), MovementType.REVERSAL, LocalDate.now(),
                    user, original.getAccount(), null, original, null, finalReason
            );

            Transaction siblingReversal = new Transaction(
                    sibling.getAmount(), sibling.getInterest(), sibling.getFine(), sibling.getDiscount(),
                    sibling.getMovementDirection().toggle(), MovementType.REVERSAL, LocalDate.now(),
                    user, sibling.getAccount(), null, sibling, null, finalReason + " (Estorno Vinculado)"
            );

            originalReversal.setLinkedTransaction(siblingReversal);
            siblingReversal.setLinkedTransaction(originalReversal);

            original.setReversed(true);
            sibling.setReversed(true);

            transactionRepository.saveAll(List.of(original, sibling));
            transactionRepository.saveAll(List.of(originalReversal, siblingReversal));

            return originalReversal.toResponse();
        }

        // ==============================================================
        // Fluxo padrão para transações COMUNS (Pagamentos, Recebimentos, Ajustes)
        // ==============================================================

        if (original.getMovementDirection() == MovementDirection.INFLOW) {
            validateSufficientFunds(
                    original.getAccount(),
                    original.getAmount(),
                    "Saldo insuficiente para estornar este recebimento. A conta ficaria negativa além do limite."
            );
        }

        Transaction reversal = new Transaction(
                original.getAmount(),
                original.getInterest(),
                original.getFine(),
                original.getDiscount(),
                original.getMovementDirection().toggle(),
                MovementType.REVERSAL,
                LocalDate.now(),
                user,
                original.getAccount(),
                original.getInstallment(),
                original,
                original.getPaymentInstrument(),
                finalReason
        );

        // Marca a transação original como estornada e salva
        original.setReversed(true);
        transactionRepository.save(original);

        // 🌟 CONSUMO DE LIMITE DE CARTÃO NO ESTORNO
        // Se a transação original era o pagamento de um cartão, o estorno significa que a dívida voltou!
        if (original.getInstallment() != null && original.getInstallment().getPaymentInstrument() instanceof CreditCard card) {
            card.consumeLimit(original.getAmount()); // Consome novamente o limite
            paymentInstrumentRepository.save(card);
        }

        // Salva e retorna a nova transação de estorno
        Transaction savedReversal = transactionRepository.save(reversal);

        return savedReversal.toResponse();
    }

    @Transactional
    public List<TransactionResponseDTO> createManualAdjustments(CreateManualAdjustmentTransactionRequestDTO request) {
        User user = userContextService.getAuthenticatedUser();

        Map<UUID, BigDecimal> accountBalanceImpact = new HashMap<>();
        List<Transaction> transactions = new ArrayList<>();

        for (CreateManualAdjustmentTransactionDTO dto : request.dto()) {

            AccountBase account = accountRepository.findByIdForUpdate(dto.accountId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada."));

            if (!account.getAccountHolder().getId().equals(user.getId())) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "A conta não pertence ao usuário.");
            }

            PaymentInstrumentBase instrument = null;
            if (dto.paymentInstrumentId() != null) {
                instrument = paymentInstrumentRepository.findByIdAndUser(dto.paymentInstrumentId(), user.getId())
                        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Instrumento não encontrado."));
            }

            validateAccountAndInstrumentCompatibility(account, instrument);

            if(LocalDate.now().isBefore(dto.paymentDate())){
                throw new BusinessException(HttpStatus.BAD_REQUEST, "A data do ajuste não pode ser futura à data atual.");
            }

            BigDecimal impact = dto.direction() == MovementDirection.OUTFLOW
                    ? dto.amount().negate()
                    : dto.amount();

            accountBalanceImpact.merge(account.getId(), impact, BigDecimal::add);

            Transaction transaction = new Transaction(
                    dto.amount(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    dto.direction(),
                    MovementType.MANUAL_ADJUSTMENT,
                    dto.paymentDate(),
                    user,
                    account,
                    null,
                    null,
                    instrument,
                    dto.reason()
            );

            transactions.add(transaction);
        }

        for (Map.Entry<UUID, BigDecimal> entry : accountBalanceImpact.entrySet()) {
            if (entry.getValue().signum() < 0) {
                AccountBase impactedAccount = accountRepository.findByIdForUpdate(entry.getKey()).orElseThrow();

                validateSufficientFunds(
                        impactedAccount,
                        entry.getValue().abs(),
                        "Saldo insuficiente para realizar o ajuste de saída na conta."
                );
            }
        }

        return transactionRepository.saveAll(transactions).stream()
                .map(Transaction::toResponse)
                .toList();
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    @Transactional
    public List<TransactionResponseDTO> transfer(TransferRequestDTO request) {
        User user = userContextService.getAuthenticatedUser();

        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "A conta de origem e destino não podem ser a mesma.");
        }

        if (LocalDate.now().isBefore(request.transferDate())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "A data da transferência não pode ser futura.");
        }

        AccountBase sourceAccount = accountRepository.findByIdForUpdate(request.sourceAccountId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta de origem não encontrada."));

        if (!sourceAccount.getAccountHolder().getId().equals(user.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "A conta de origem não pertence ao usuário.");
        }

        AccountBase destAccount = accountRepository.findByIdForUpdate(request.destinationAccountId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta de destino não encontrada."));

        if (!destAccount.getAccountHolder().getId().equals(user.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "A conta de destino não pertence ao usuário.");
        }

        validateSufficientFunds(
                sourceAccount,
                request.amount(),
                "Saldo insuficiente na conta de origem para realizar a transferência."
        );

        String baseObs = request.observations() != null && !request.observations().trim().isEmpty()
                ? request.observations()
                : "Transferência";

        Transaction outflow = new Transaction(
                request.amount(), MovementDirection.OUTFLOW, MovementType.TRANSFER,
                request.transferDate(), user, sourceAccount,
                baseObs + " (Para: " + destAccount.getName() + ")", null
        );
        outflow = transactionRepository.save(outflow);

        Transaction inflow = new Transaction(
                request.amount(), MovementDirection.INFLOW, MovementType.TRANSFER,
                request.transferDate(), user, destAccount,
                baseObs + " (De: " + sourceAccount.getName() + ")", outflow
        );
        inflow = transactionRepository.save(inflow);

        outflow.setLinkedTransaction(inflow);
        transactionRepository.save(outflow);

        return List.of(outflow.toResponse(), inflow.toResponse());
    }

    private void validateAccountAndInstrumentCompatibility(
            AccountBase account,
            PaymentInstrumentBase instrument
    ) {
        if (instrument == null) {
            return;
        }

        if(instrument.getInstrumentNature() == InstrumentNature.PURCHASE){
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "A natureza de Pagamento [PURCHASE] não pode ser usado para efetivar uma transação."
            );
        };

        if (account.getType() == AccountType.WALLET) {
            if (instrument.getPaymentType() != PaymentType.CASH) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "Contas do tipo WALLET só podem ser movimentadas com instrumento CASH."
                );
            }
        } else {
            if (instrument.getPaymentType() == PaymentType.CASH) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "Instrumento CASH só pode ser usado com contas do tipo WALLET."
                );
            }
        }
    }

    private void validateSufficientFunds(AccountBase account, BigDecimal amountToRemove, String customErrorMessage) {
        BigDecimal availableBalance = account.getBalance();

        if (account.getType() == AccountType.CHECKING) {
            availableBalance = availableBalance.add(((CheckingAccount) account).getOverdraftLimit());
        }

        if (availableBalance.compareTo(amountToRemove) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, customErrorMessage);
        }
    }

    public Page<TransactionResponseDTO> searchTransactions(
            MovementDirection direction,
            String searchName,
            UUID accountId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        // A camada de serviço cuida da regra de isolamento de usuário
        String userId = userContextService.getAuthenticatedUser().getId();

        return transactionRepository.searchTransactions(
                userId, direction, searchName, accountId, startDate, endDate, pageable
        ).map(Transaction::toResponse); // Converte para DTO aqui
    }
}