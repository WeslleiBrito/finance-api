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

            // 4. Validação da Data (Adicionado isEqual indiretamente usando apenas isBefore)
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
            // 6. Instancia a Transação
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

        // 7. Validação de Sobrepaga da Parcela (Apenas valor Principal)
        for (Map.Entry<UUID, BigDecimal> entry : principalAmortizationTotals.entrySet()) {
            Installment installment = installmentRepository.findByIdForUpdate(entry.getKey()).orElseThrow();

            // Aqui validamos apenas o montante principal contra o saldo restante principal
            BigDecimal totalAmortizedAfterPayment = installment.getTotalPaid().add(entry.getValue());

            if (totalAmortizedAfterPayment.compareTo(installment.getAmount()) > 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "O pagamento (sem juros/multas) excede o valor original da parcela.");
            }
        }

        // 8. Validação do Saldo da Conta
        for (Map.Entry<UUID, BigDecimal> entry : accountTotals.entrySet()) {
            AccountBase account = accountRepository.findByIdForUpdate(entry.getKey()).orElseThrow();
            BigDecimal availableBalance = account.getBalance();

            if (account.getType() == AccountType.CHECKING) {
                availableBalance = availableBalance.add(((CheckingAccount) account).getOverdraftLimit());
            }

            if (availableBalance.compareTo(entry.getValue()) < 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Saldo insuficiente para realizar as transações.");
            }
        }

        // 9. Persistência
        return transactionRepository.saveAll(transactions).stream()
                .map(Transaction::toResponse)
                .toList();
    }

    public List<TransactionResponseDTO> findAllByUser() {
        User user = userContextService.getAuthenticatedUser();

        return transactionRepository.findAllByUserIdOrderByPaymentDateDesc(user.getId())
                .stream()
                .map(Transaction::toResponse)
                .toList();
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

        // 🌟 LÓGICA DE ESTORNO DUPLO PARA TRANSFERÊNCIAS (Verificando o Vínculo)
        if (original.getMovementType() == MovementType.TRANSFER && original.getLinkedTransaction() != null) {
            Transaction sibling = original.getLinkedTransaction();

            if (transactionRepository.existsByReversalOfId(sibling.getId()) || sibling.isReversed()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "A contraparte desta transferência já foi estornada.");
            }

            // 1. Cria o Estorno da Original (usando o construtor completo para garantir o reversalOf)
            Transaction originalReversal = new Transaction(
                    original.getAmount(), original.getInterest(), original.getFine(), original.getDiscount(),
                    original.getMovementDirection().toggle(), MovementType.REVERSAL, LocalDate.now(),
                    user, original.getAccount(), null, original, null, finalReason
            );

            // 2. Cria o Estorno da Irmã
            Transaction siblingReversal = new Transaction(
                    sibling.getAmount(), sibling.getInterest(), sibling.getFine(), sibling.getDiscount(),
                    sibling.getMovementDirection().toggle(), MovementType.REVERSAL, LocalDate.now(),
                    user, sibling.getAccount(), null, sibling, null, finalReason + " (Estorno Vinculado)"
            );

            // 3. Cruza os vínculos dos novos estornos
            originalReversal.setLinkedTransaction(siblingReversal);
            siblingReversal.setLinkedTransaction(originalReversal);

            // 4. Marca as transações originais como estornadas (reversed = true)
            original.setReversed(true);
            sibling.setReversed(true);

            // 5. Salva as originais atualizadas e depois os estornos
            transactionRepository.saveAll(List.of(original, sibling));
            transactionRepository.saveAll(List.of(originalReversal, siblingReversal));

            return originalReversal.toResponse();
        }

        // ==============================================================
        // Fluxo padrão para transações COMUNS (Pagamentos, Recebimentos, Ajustes)
        // ==============================================================
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

        // 🌟 Marca a transação original como estornada e salva
        original.setReversed(true);
        transactionRepository.save(original);

        // Salva e retorna a nova transação de estorno
        Transaction savedReversal = transactionRepository.save(reversal);

        return savedReversal.toResponse();
    }

    @Transactional
    public List<TransactionResponseDTO> createManualAdjustments(CreateManualAdjustmentTransactionRequestDTO request) {
        User user = userContextService.getAuthenticatedUser();

        // 📊 Acumulador de impacto financeiro por conta para validação de saldo
        Map<UUID, BigDecimal> accountBalanceImpact = new HashMap<>();
        List<Transaction> transactions = new ArrayList<>();

        for (CreateManualAdjustmentTransactionDTO dto : request.dto()) {

            // 1. Validar a Conta com LOCK PESSIMISTA
            AccountBase account = accountRepository.findByIdForUpdate(dto.accountId())
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
                throw new BusinessException(HttpStatus.BAD_REQUEST, "A data do ajuste não pode ser futura à data atual.");
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
                    MovementType.MANUAL_ADJUSTMENT, // Uso correto do Enum para Ajuste Manual
                    dto.paymentDate(),
                    user,
                    account,
                    null, // Installment é NULL
                    null, // ReversalOf é NULL
                    instrument,
                    dto.reason() // Salvamos o motivo no campo observations
            );

            transactions.add(transaction);
        }

        // 6. Validar limite de saque para contas correntes (Check de Saldo)
        for (Map.Entry<UUID, BigDecimal> entry : accountBalanceImpact.entrySet()) {
            if (entry.getValue().signum() < 0) { // Se o saldo final do lote for negativo (saída)
                AccountBase account = accountRepository.findByIdForUpdate(entry.getKey()).orElseThrow();
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

        BigDecimal availableBalance = sourceAccount.getBalance();
        if (sourceAccount.getType() == AccountType.CHECKING) {
            availableBalance = availableBalance.add(((CheckingAccount) sourceAccount).getOverdraftLimit());
        }

        if (availableBalance.compareTo(request.amount()) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Saldo insuficiente na conta de origem para realizar a transferência.");
        }

        String baseObs = request.observations() != null && !request.observations().trim().isEmpty()
                ? request.observations()
                : "Transferência";

        // 1. Cria e SALVA a Saída primeiro (para gerar o ID no banco)
        Transaction outflow = new Transaction(
                request.amount(), MovementDirection.OUTFLOW, MovementType.TRANSFER,
                request.transferDate(), user, sourceAccount,
                baseObs + " (Para: " + destAccount.getName() + ")", null
        );
        outflow = transactionRepository.save(outflow);

        // 2. Cria e SALVA a Entrada já apontando para a Saída
        Transaction inflow = new Transaction(
                request.amount(), MovementDirection.INFLOW, MovementType.TRANSFER,
                request.transferDate(), user, destAccount,
                baseObs + " (De: " + sourceAccount.getName() + ")", outflow
        );
        inflow = transactionRepository.save(inflow);

        // 3. Atualiza a Saída apontando para a Entrada
        outflow.setLinkedTransaction(inflow);
        transactionRepository.save(outflow);

        return List.of(outflow.toResponse(), inflow.toResponse());
    }

    private void validateAccountAndInstrumentCompatibility(
            AccountBase account,
            PaymentInstrumentBase instrument
    ) {
        // 🌟 CORREÇÃO: Previne o erro se a transação não tiver instrumento de pagamento!
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
}