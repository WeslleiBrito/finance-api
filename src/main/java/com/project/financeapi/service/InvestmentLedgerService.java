package com.project.financeapi.service;

import com.project.financeapi.dto.investment.response.*;
import com.project.financeapi.entity.*;
import com.project.financeapi.enumSystem.FixedIncomeStatus;
import com.project.financeapi.repository.InvestmentProductRepository;
import com.project.financeapi.service.yield.DailyYieldEngineService;
import lombok.RequiredArgsConstructor;
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

    private final InvestmentProductRepository productRepository;
    private final DailyYieldEngineService yieldEngine;

    @Transactional
    public List<ProductDashboardDTO> getDashboardsByAccount(UUID accountId) {
        yieldEngine.processPendingYieldsForAccount(accountId);
        List<InvestmentProduct> products = productRepository.findAllActiveByAccountIdWithDetails(accountId);
        LocalDate today = LocalDate.now();

        return products.stream()
                .map(prod -> mapProduct(prod, today))
                .toList();
    }

    private ProductDashboardDTO mapProduct(InvestmentProduct product, LocalDate date) {
        BigDecimal totalProductBalance = BigDecimal.ZERO;
        List<BoxDetailDTO> boxDtos = new ArrayList<>();

        for (InvestmentBox box : product.getBoxes()) {
            BigDecimal boxPrincipal = BigDecimal.ZERO, boxGross = BigDecimal.ZERO, boxTaxes = BigDecimal.ZERO, boxNet = BigDecimal.ZERO;
            List<LotDetailDTO> activeLotsDto = new ArrayList<>();

            for (FixedIncomeLot lot : box.getLots()) {
                FixedIncomeLot.LotState state = lot.projectState();
                if (state.grossBalance().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal irTax = lot.getProjectedIrTax(date, state);
                    BigDecimal iofTax = lot.getProjectedIofTax(date, state);
                    BigDecimal net = lot.getProjectedNetBalance(date, state);

                    boxPrincipal = boxPrincipal.add(state.remainingPrincipal());
                    boxGross = boxGross.add(state.grossBalance());
                    boxTaxes = boxTaxes.add(irTax).add(iofTax);
                    boxNet = boxNet.add(net);

                    List<TransactionLedgerDTO> txs = lot.getTransactions().stream()
                            .sorted(Comparator.comparing(InvestmentTransaction::getReferenceDate).reversed())
                            .map(t -> new TransactionLedgerDTO(t.getId(), t.getType().name(), t.getReferenceDate(), t.getGrossAmount(), t.getAmount(), t.getIrTax(), t.getIofTax(), t.getAppliedMarketRate(), t.getDescription()))
                            .toList();

                    activeLotsDto.add(new LotDetailDTO(lot.getId(), lot.getPurchaseDate(), lot.getAgeInDays(date), state.remainingPrincipal(), state.grossBalance(), irTax, iofTax, net, txs));
                }
            }

            // Removemos o 'if (boxGross.compareTo(BigDecimal.ZERO) > 0)' para permitir caixas zeradas
            totalProductBalance = totalProductBalance.add(boxNet);
            boxDtos.add(new BoxDetailDTO(box.getId(), box.getName(), boxPrincipal, boxGross, boxTaxes, boxNet, activeLotsDto));
        }

        BigDecimal displayRate = product.getTiers().isEmpty() ? BigDecimal.ZERO : product.getTiers().iterator().next().getRateMultiplier();
        FixedIncomeStatus status = totalProductBalance.compareTo(BigDecimal.ZERO) > 0 ? FixedIncomeStatus.ACTIVE : FixedIncomeStatus.CLOSED;
        List<FixedIncomeTierDTO> tiers = product.getTiers().stream().map(t -> new FixedIncomeTierDTO(t.getId(), t.getMinBalance(), t.getMaxBalance(), t.getRateMultiplier())).toList();

        return new ProductDashboardDTO(product.getId(), product.getName(), product.getType(), product.getIndexer(), displayRate, status, totalProductBalance, tiers, boxDtos);
    }
}