package com.project.financeapi.service;

import com.project.financeapi.dto.investment.response.FixedIncomeDashboardDTO;
import com.project.financeapi.dto.investment.response.LotDetailDTO;
import com.project.financeapi.dto.investment.response.TransactionLedgerDTO;
import com.project.financeapi.entity.FixedIncome;
import com.project.financeapi.entity.FixedIncomeLot;
import com.project.financeapi.entity.InvestmentTransaction;
import com.project.financeapi.enumSystem.FixedIncomeStatus;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.FixedIncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvestmentLedgerService {

    private final FixedIncomeRepository fixedIncomeRepository;
    private final UserContextService userContextService;

    // Injeção do Motor Reativo que consolida os eventos
    private final DailyYieldEngineService dailyYieldEngineService;

    /**
     * Traz o dashboard consolidado de todos os investimentos da conta.
     */
    @Transactional
    public List<FixedIncomeDashboardDTO> getAllActiveDashboardsByAccount(UUID accountId) {
        String loggedUserId = userContextService.getAuthenticatedUser().getId();

        // Gatilho do Motor Reativo (Lazy Evaluation)
        dailyYieldEngineService.processPendingYieldsForAccount(accountId);

        // Busca Otimizada (Evita N+1 usando o JOIN FETCH que criamos no repositório)
        List<FixedIncome> activeIncomes = fixedIncomeRepository.findAllActiveByAccountIdWithLots(accountId);

        // Validação de segurança e isolamento de tenant
        if (!activeIncomes.isEmpty() && !activeIncomes.getFirst().getAccount().getAccountHolder().getId().equals(loggedUserId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Acesso negado aos investimentos desta conta.");
        }

        LocalDate today = LocalDate.now();

        // O Mapeamento. A matemática tributária é lida da projeção de estado
        return activeIncomes.stream()
                .map(income -> mapToDashboardDTO(income, today))
                .filter(dto -> dto.status() == FixedIncomeStatus.ACTIVE) // Adicionado! Oculta os contratos esgotados (saldo 0)
                .toList();
    }

    /**
     * Traz o dashboard detalhado de um único papel (CDB específico, por exemplo).
     */
    @Transactional
    public FixedIncomeDashboardDTO getDashboard(UUID fixedIncomeId) {
        String loggedUserId = userContextService.getAuthenticatedUser().getId();

        dailyYieldEngineService.processPendingYieldsForFixedIncome(fixedIncomeId);

        FixedIncome income = fixedIncomeRepository.findByIdWithLots(fixedIncomeId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Investimento não encontrado."));

        if (!income.getAccount().getAccountHolder().getId().equals(loggedUserId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Acesso negado a este investimento.");
        }

        return mapToDashboardDTO(income, LocalDate.now());
    }

    // ========================================================================
    // MÉTODOS DE MAPEAMENTO E AGREGAÇÃO (Event Sourcing Projection)
    // ========================================================================

    private FixedIncomeDashboardDTO mapToDashboardDTO(FixedIncome income, LocalDate referenceDate) {
        BigDecimal totalPrincipal = BigDecimal.ZERO;
        BigDecimal totalProjectedGrossBalance = BigDecimal.ZERO;
        BigDecimal totalProjectedTaxes = BigDecimal.ZERO;
        BigDecimal totalProjectedNetBalance = BigDecimal.ZERO;

        List<LotDetailDTO> activeLotsDto = new ArrayList<>();

        for (FixedIncomeLot lot : income.getLots()) {
            FixedIncomeLot.LotState state = lot.projectState();

            if (state.grossBalance().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal irTax = lot.getProjectedIrTax(referenceDate, state);
                BigDecimal iofTax = lot.getProjectedIofTax(referenceDate, state);
                BigDecimal netBalance = lot.getProjectedNetBalance(referenceDate, state);

                totalPrincipal = totalPrincipal.add(state.remainingPrincipal());
                totalProjectedGrossBalance = totalProjectedGrossBalance.add(state.grossBalance());
                totalProjectedTaxes = totalProjectedTaxes.add(irTax).add(iofTax);
                totalProjectedNetBalance = totalProjectedNetBalance.add(netBalance);

                // Mapeia o extrato isolado DESTE lote específico (Ordenado do mais novo para o mais velho)
                List<TransactionLedgerDTO> lotTransactions = lot.getTransactions().stream()
                        .sorted(Comparator.comparing(InvestmentTransaction::getReferenceDate).reversed())
                        .map(t -> new TransactionLedgerDTO(
                                t.getId(),
                                t.getType(),
                                t.getReferenceDate(),
                                t.getGrossAmount(),
                                t.getAmount(),
                                t.getIrTax(),
                                t.getIofTax(),
                                t.getAppliedMarketRate(),
                                t.getDescription()
                        ))
                        .toList();

                activeLotsDto.add(new LotDetailDTO(
                        lot.getId(),
                        lot.getPurchaseDate(),
                        lot.getAgeInDays(referenceDate),
                        state.remainingPrincipal(),
                        state.grossBalance(),
                        irTax,
                        iofTax,
                        netBalance,
                        lotTransactions // Transações injetadas diretamente no Lote
                ));
            }
        }

        activeLotsDto.sort(Comparator.comparing(LotDetailDTO::purchaseDate));

        FixedIncomeStatus status = totalProjectedGrossBalance.compareTo(BigDecimal.ZERO) > 0
                ? FixedIncomeStatus.ACTIVE
                : FixedIncomeStatus.CLOSED;

        return new FixedIncomeDashboardDTO(
                income.getId(),
                income.getName(),
                income.getType(),
                income.getIndexer(),
                income.getContractedRate(),
                income.getMaturityDate(),
                status,
                totalPrincipal,
                totalProjectedGrossBalance,
                totalProjectedTaxes,
                totalProjectedNetBalance,
                activeLotsDto,
                new ArrayList<>() // Pode remover a propriedade recentTransactions do FixedIncomeDashboardDTO depois se quiser
        );
    }
    private List<TransactionLedgerDTO> mapRecentTransactions(List<FixedIncomeLot> lots) {
        return lots.stream()
                .flatMap(lot -> lot.getTransactions().stream())
                .sorted(Comparator.comparing(InvestmentTransaction::getReferenceDate).reversed()) // Mais recentes primeiro
                .limit(10) // Evita payload massivo
                .map(t -> new TransactionLedgerDTO(
                        t.getId(),
                        t.getType(),
                        t.getReferenceDate(),
                        t.getGrossAmount(),
                        t.getAmount(),
                        t.getIrTax(),
                        t.getIofTax(),
                        t.getAppliedMarketRate(),
                        t.getDescription()
                ))
                .toList();
    }
}