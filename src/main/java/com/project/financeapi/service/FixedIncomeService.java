package com.project.financeapi.service;

import com.project.financeapi.entity.*;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enumSystem.InvestmentTransactionType;
import com.project.financeapi.enumSystem.MovementDirection;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.AccountRepository;
import com.project.financeapi.repository.FixedIncomeLotRepository;
import com.project.financeapi.repository.FixedIncomeRepository;
import com.project.financeapi.repository.InvestmentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.project.financeapi.dto.investment.request.InvestmentRescueDTO;
import com.project.financeapi.dto.investment.request.InvestmentApportDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FixedIncomeService {

    private final FixedIncomeRepository fixedIncomeRepository;
    private final FixedIncomeLotRepository lotRepository;
    private final InvestmentTransactionRepository investmentTransactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;

    @Transactional
    public UUID createApport(InvestmentApportDTO dto) { // <-- Alterado de void para UUID
        AccountBase account = accountRepository.findByIdForUpdate(dto.accountId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada."));

        transactionService.createInvestmentTransaction(
                account.getId(), dto.amount(), MovementDirection.OUTFLOW,
                LocalDate.now(), "Aporte em Investimento: " + dto.name()
        );

        FixedIncome fixedIncome;
        if (dto.fixedIncomeId() == null) {
            fixedIncome = FixedIncome.builder()
                    .account(account)
                    .name(dto.name())
                    .indexer(dto.indexer())
                    .contractedRate(dto.contractedRate())
                    .type(dto.type())
                    .maturityDate(dto.maturityDate())
                    .build();
            fixedIncome = fixedIncomeRepository.save(fixedIncome);
        } else {
            fixedIncome = fixedIncomeRepository.findById(dto.fixedIncomeId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Investimento não encontrado."));
        }

        FixedIncomeLot lot = FixedIncomeLot.builder()
                .fixedIncome(fixedIncome)
                .purchaseDate(dto.purchaseDate() != null ? dto.purchaseDate() : LocalDate.now())
                .ledgerStartDate(LocalDate.now())
                .build();
        lot = lotRepository.save(lot);

        InvestmentTransaction apportTransaction = InvestmentTransaction.builder()
                .lot(lot)
                .type(InvestmentTransactionType.APPORT)
                .referenceDate(LocalDate.now())
                .grossAmount(dto.amount())
                .amount(dto.amount())
                .irTax(BigDecimal.ZERO)
                .iofTax(BigDecimal.ZERO)
                .description("Aporte de Capital")
                .build();

        investmentTransactionRepository.save(apportTransaction);

        return fixedIncome.getId(); // <-- Retorna o ID gerado ou atualizado
    }

    @Transactional
    public void executeRescue(InvestmentRescueDTO dto) {
        FixedIncome fixedIncome = fixedIncomeRepository.findById(dto.fixedIncomeId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Investimento não encontrado."));

        // O valor solicitado pelo usuário é o LÍQUIDO que ele quer ver pingar na conta corrente
        BigDecimal remainingNetRequested = dto.requestedAmount();

        List<InvestmentTransaction> transactionsToSave = new ArrayList<>();
        BigDecimal totalNetAmountToCredit = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();

        List<FixedIncomeLot> activeLots = lotRepository.findActiveLotsForRescueOderByOldest(fixedIncome.getId());

        for (FixedIncomeLot lot : activeLots) {
            if (remainingNetRequested.compareTo(BigDecimal.ZERO) <= 0) break;

            FixedIncomeLot.LotState state = lot.projectState();

            // O lote provisiona os impostos exatos para o dia de hoje
            BigDecimal irTaxProvision = lot.getProjectedIrTax(today, state);
            BigDecimal iofTaxProvision = lot.getProjectedIofTax(today, state);
            BigDecimal lotNetBalance = lot.getProjectedNetBalance(today, state);

            if (lotNetBalance.compareTo(BigDecimal.ZERO) <= 0) continue;

            // A mordida no lote é baseada no saldo LÍQUIDO
            BigDecimal netAmountToTake = remainingNetRequested.min(lotNetBalance);
            BigDecimal ratio = netAmountToTake.divide(lotNetBalance, 8, RoundingMode.HALF_UP);

            // Materializamos os impostos proporcionais à fatia que estamos sacando
            BigDecimal irTaxMaterialized = irTaxProvision.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
            BigDecimal iofTaxMaterialized = iofTaxProvision.multiply(ratio).setScale(2, RoundingMode.HALF_UP);

            // CÁLCULO POR DENTRO: O valor bruto consumido do lote é a soma do líquido desejado + impostos retidos
            BigDecimal grossAmountToTake = netAmountToTake.add(irTaxMaterialized).add(iofTaxMaterialized);

            // GERA O EVENTO DE SAÍDA MATERIALIZANDO OS IMPOSTOS (O fato gerador)
            InvestmentTransaction rescueTransaction = InvestmentTransaction.builder()
                    .lot(lot)
                    .type(InvestmentTransactionType.RESCUE)
                    .referenceDate(today)
                    .grossAmount(grossAmountToTake) // O valor que esvazia a base bruta do lote
                    .amount(netAmountToTake)        // O valor exato que o usuário pediu
                    .irTax(irTaxMaterialized)
                    .iofTax(iofTaxMaterialized)
                    .description("Resgate de Capital (Fatia PEPS)")
                    .build();

            transactionsToSave.add(rescueTransaction);
            totalNetAmountToCredit = totalNetAmountToCredit.add(netAmountToTake);
            remainingNetRequested = remainingNetRequested.subtract(netAmountToTake);
        }

        if (remainingNetRequested.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Saldo líquido insuficiente para realizar o resgate solicitado.");
        }

        investmentTransactionRepository.saveAll(transactionsToSave);

        // A transação entra na conta corrente livre de bitributação
        transactionService.createInvestmentTransaction(
                fixedIncome.getAccount().getId(),
                totalNetAmountToCredit,
                MovementDirection.INFLOW,
                today,
                "Resgate de Investimento: " + fixedIncome.getName()
        );
    }
}