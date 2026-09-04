package com.project.financeapi.service;

import com.project.financeapi.dto.investment.InvestmentApportDTO;
import com.project.financeapi.dto.investment.InvestmentRescueDTO;
import com.project.financeapi.entity.*;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enumSystem.FixedIncomeStatus;
import com.project.financeapi.enumSystem.InvestmentTransactionType;
import com.project.financeapi.enumSystem.MovementDirection;
import com.project.financeapi.enumSystem.MovementType;
import com.project.financeapi.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FixedIncomeService {

    private final FixedIncomeRepository fixedIncomeRepository;
    private final FixedIncomeLotRepository lotRepository;
    private final InvestmentTransactionRepository investmentTransactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository; // Repositório da conta-corrente
    private final UserContextService userContextService;

    /**
     * 1. FLUXO DE APORTE (Criação de Sacola ou Novo Lote)
     */
    @Transactional
    public void createApport(InvestmentApportDTO dto) {
        User user = userContextService.getAuthenticatedUser();
        AccountBase account = accountRepository.findById(dto.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada."));

        // 1. Pega o saldo real da conta
        BigDecimal availableBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;

        // 3. Valida o saldo e exibe no Console os valores exatos em caso de bloqueio
        if (availableBalance.compareTo(dto.amount()) < 0) {
            throw new IllegalStateException(String.format(
                    "Saldo insuficiente. Disponível no DB: R$ %s | Tentativa de Aporte: R$ %s",
                    availableBalance, dto.amount()
            ));
        }

        FixedIncome fixedIncome;

        if (dto.fixedIncomeId() != null) {
            fixedIncome = fixedIncomeRepository.findById(dto.fixedIncomeId())
                    .orElseThrow(() -> new IllegalArgumentException("Sacola de investimento não encontrada."));
        } else {
            fixedIncome = new FixedIncome();
            fixedIncome.setName(dto.name());
            fixedIncome.setType(dto.type());
            fixedIncome.setIndexer(dto.indexer());
            fixedIncome.setContractedRate(dto.contractedRate());
            fixedIncome.setMaturityDate(dto.maturityDate());
            fixedIncome.setAccount(account);
            fixedIncome.setCreatedBy(user);
            fixedIncome.setStatus(FixedIncomeStatus.ACTIVE);
            fixedIncome = fixedIncomeRepository.save(fixedIncome);
        }

        // 1. Cria o Lote físico
        FixedIncomeLot lot = new FixedIncomeLot();
        lot.setFixedIncome(fixedIncome);
        lot.setInitialPrincipal(dto.amount());
        lot.setRemainingPrincipal(dto.amount());
        lot.setPurchaseDate(dto.purchaseDate());
        lot = lotRepository.save(lot);

        // 2. Grava a transação inicial no Livro-Razão da sacola (Custódia)
        InvestmentTransaction invTx = new InvestmentTransaction();
        invTx.setLot(lot);
        invTx.setType(InvestmentTransactionType.APPORT);
        invTx.setAmount(dto.amount());
        invTx.setReferenceDate(dto.purchaseDate());
        invTx.setDescription("Aporte: " + fixedIncome.getName());
        investmentTransactionRepository.save(invTx);

        // 3. Debita a Conta Corrente gerando uma Transaction padrão usando o construtor simplificado
        Transaction accountTx = new Transaction(
                dto.amount(),
                MovementDirection.OUTFLOW,
                MovementType.INVESTMENT_APPORT, // Utilizando o novo Enum
                dto.purchaseDate(),
                user,
                account,
                "Aporte em Investimento: " + fixedIncome.getName(),
                null
        );
        transactionRepository.save(accountTx);
    }

    /**
     * 2. FLUXO DE RESGATE COM REGRA PEPS (Primeiro que Entra, Primeiro que Sai)
     */
    @Transactional
    public void executeRescue(InvestmentRescueDTO dto) {
        User user = userContextService.getAuthenticatedUser();
        FixedIncome fixedIncome = fixedIncomeRepository.findById(dto.fixedIncomeId())
                .orElseThrow(() -> new IllegalArgumentException("Sacola não encontrada."));

        BigDecimal amountToRescue = dto.rescueAmount();
        BigDecimal totalNetBalance = investmentTransactionRepository.calculateNetBalanceByFixedIncome(fixedIncome.getId());

        if (totalNetBalance.compareTo(amountToRescue) < 0) {
            throw new IllegalStateException("Saldo investido insuficiente para o resgate solicitado.");
        }

        List<FixedIncomeLot> activeLots = lotRepository.findActiveLotsByFixedIncomeOrderByDateAsc(fixedIncome.getId());

        BigDecimal remainingRescue = amountToRescue;
        BigDecimal totalPrincipalRescued = BigDecimal.ZERO;
        BigDecimal totalProfitRescued = BigDecimal.ZERO;

        for (FixedIncomeLot lot : activeLots) {
            if (remainingRescue.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal lotNetBalance = investmentTransactionRepository.calculateNetBalanceByLot(lot.getId());
            if (lotNetBalance.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal amountTakenFromLot = remainingRescue.min(lotNetBalance);
            BigDecimal proportion = amountTakenFromLot.divide(lotNetBalance, 8, RoundingMode.HALF_UP);

            BigDecimal principalToDeduct = lot.getRemainingPrincipal().multiply(proportion).setScale(2, RoundingMode.HALF_UP);
            BigDecimal profitRealized = amountTakenFromLot.subtract(principalToDeduct);

            lot.setRemainingPrincipal(lot.getRemainingPrincipal().subtract(principalToDeduct));
            lotRepository.save(lot);

            InvestmentTransaction rescueTx = new InvestmentTransaction();
            rescueTx.setLot(lot);
            rescueTx.setType(InvestmentTransactionType.RESCUE);
            rescueTx.setAmount(amountTakenFromLot.negate());
            rescueTx.setReferenceDate(dto.rescueDate());
            rescueTx.setDescription("Resgate Parcial");
            investmentTransactionRepository.save(rescueTx);

            totalPrincipalRescued = totalPrincipalRescued.add(principalToDeduct);
            totalProfitRescued = totalProfitRescued.add(profitRealized);
            remainingRescue = remainingRescue.subtract(amountTakenFromLot);
        }

        BigDecimal finalNetBalance = investmentTransactionRepository.calculateNetBalanceByFixedIncome(fixedIncome.getId());
        if (finalNetBalance.compareTo(BigDecimal.ZERO) <= 0) {
            fixedIncome.setStatus(FixedIncomeStatus.CLOSED);
            fixedIncomeRepository.save(fixedIncome);
        }

        AccountBase account = fixedIncome.getAccount();

        // 3. Credita o principal devolvido na Conta Corrente
        if (totalPrincipalRescued.compareTo(BigDecimal.ZERO) > 0) {
            Transaction principalTx = new Transaction(
                    totalPrincipalRescued,
                    MovementDirection.INFLOW,
                    MovementType.INVESTMENT_REDEEM_PRINCIPAL, // Utilizando o novo Enum
                    dto.rescueDate(),
                    user,
                    account,
                    "Resgate de Investimento (Principal): " + fixedIncome.getName(),
                    null
            );
            transactionRepository.save(principalTx);
        }

        // 4. Credita o lucro realizado na Conta Corrente (se houver lucro)
        if (totalProfitRescued.compareTo(BigDecimal.ZERO) > 0) {
            Transaction profitTx = new Transaction(
                    totalProfitRescued,
                    MovementDirection.INFLOW,
                    MovementType.INVESTMENT_REDEEM_PROFIT, // Utilizando o novo Enum
                    dto.rescueDate(),
                    user,
                    account,
                    "Rendimento Resgatado Líquido: " + fixedIncome.getName(),
                    null
            );
            transactionRepository.save(profitTx);
        }
    }
}