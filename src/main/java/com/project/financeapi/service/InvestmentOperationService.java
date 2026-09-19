package com.project.financeapi.service;

import com.project.financeapi.dto.investment.request.CreateBoxDTO;
import com.project.financeapi.dto.investment.request.CreateProductDTO;
import com.project.financeapi.dto.investment.request.InvestmentApportDTO;
import com.project.financeapi.dto.investment.request.InvestmentRescueDTO;
import com.project.financeapi.dto.investment.response.ProductDashboardDTO;
import com.project.financeapi.entity.*;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enumSystem.InvestmentTransactionType;
import com.project.financeapi.enumSystem.MovementDirection;
import com.project.financeapi.enumSystem.YieldConvention;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvestmentOperationService {

    private final InvestmentProductRepository productRepo;
    private final InvestmentTierRepository tierRepo;
    private final InvestmentBoxRepository boxRepo;
    private final FixedIncomeLotRepository lotRepo;
    private final InvestmentTransactionRepository txRepo;
    private final AccountRepository accountRepo;
    private final TransactionService transactionService;
    private final InvestmentLedgerService ledgerService;

    // --- ENDPOINT 1: CRIAR PRODUTO ---
    @Transactional
    public ProductDashboardDTO createProduct(CreateProductDTO dto) {
        AccountBase account = accountRepo.findById(dto.accountId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada."));

        InvestmentProduct product = new InvestmentProduct();
        product.setAccount(account);
        product.setName(dto.name());
        product.setIndexer(dto.indexer());
        product.setType(dto.type());
        // NOVO: se o front não enviar, cai no padrão CDI exponencial (comportamento anterior preservado)
        product.setConvention(dto.convention() != null ? dto.convention() : YieldConvention.CDI_EXPONENTIAL_252);

        final InvestmentProduct savedProduct = productRepo.save(product);

        if (dto.tiers() != null && !dto.tiers().isEmpty()) {
            List<InvestmentTier> productTiers = dto.tiers().stream().map(tierDto -> {
                InvestmentTier tier = new InvestmentTier();
                tier.setProduct(savedProduct);
                tier.setMinBalance(tierDto.minBalance() != null ? tierDto.minBalance() : BigDecimal.ZERO);
                tier.setMaxBalance(tierDto.maxBalance());
                tier.setRateMultiplier(tierDto.rateMultiplier());
                // NOVO: regra de elegibilidade opcional (ex: 115% só com R$1.000/mês movimentado)
                tier.setRequiredMonthlyMovement(tierDto.requiredMonthlyMovement());
                return tier;
            }).toList();

            tierRepo.saveAll(productTiers);
            savedProduct.setTiers(productTiers);
        }

        return ledgerService.getDashboardsByAccount(account.getId()).stream()
                .filter(p -> p.id().equals(savedProduct.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao gerar dashboard do produto criado."));
    }

    // --- ENDPOINT 2: CRIAR CAIXINHA (OBJETIVO) ---
    @Transactional
    public ProductDashboardDTO createBox(CreateBoxDTO dto) {
        InvestmentProduct product = productRepo.findById(dto.productId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Produto de investimento não encontrado."));

        InvestmentBox box = new InvestmentBox();
        box.setProduct(product);
        box.setName(dto.name());
        boxRepo.save(box);

        return ledgerService.getDashboardsByAccount(product.getAccount().getId()).stream()
                .filter(p -> p.id().equals(product.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao atualizar dashboard do produto."));
    }

    // --- ENDPOINT 3: REALIZAR APORTE (CRIAR LOTE E TRANSAÇÃO) ---
    @Transactional
    public void executeApport(InvestmentApportDTO dto) {
        InvestmentBox box = boxRepo.findById(dto.boxId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Caixinha (Objetivo) não encontrada."));

        AccountBase debitAccount = accountRepo.findByIdForUpdate(dto.accountId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta de origem não encontrada."));

        transactionService.createInvestmentTransaction(
                debitAccount.getId(), dto.amount(), MovementDirection.OUTFLOW,
                dto.purchaseDate() != null ? dto.purchaseDate() : LocalDate.now(),
                "Aporte na caixinha: " + box.getName()
        );

        FixedIncomeLot lot = FixedIncomeLot.builder()
                .box(box)
                .purchaseDate(dto.purchaseDate() != null ? dto.purchaseDate() : LocalDate.now())
                .ledgerStartDate(LocalDate.now())
                .build();
        lot = lotRepo.save(lot);

        InvestmentTransaction apportTransaction = InvestmentTransaction.builder()
                .lot(lot)
                .type(InvestmentTransactionType.APPORT)
                .referenceDate(dto.purchaseDate() != null ? dto.purchaseDate() : LocalDate.now())
                .grossAmount(dto.amount())
                .amount(dto.amount())
                .irTax(BigDecimal.ZERO)
                .iofTax(BigDecimal.ZERO)
                .description("Aporte de Capital")
                .build();

        txRepo.save(apportTransaction);
    }

    // --- ENDPOINT 4: RESGATE (INALTERADO) ---
    @Transactional
    public void executeRescue(InvestmentRescueDTO dto) {
        InvestmentBox box = boxRepo.findByIdWithDetails(dto.boxId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Caixinha não encontrada."));

        BigDecimal remainingNetRequested = dto.requestedAmount();
        List<InvestmentTransaction> transactionsToSave = new ArrayList<>();
        BigDecimal totalNetAmountToCredit = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();

        List<FixedIncomeLot> activeLots = lotRepo.findActiveLotsForRescueByBox(box.getId());

        for (FixedIncomeLot lot : activeLots) {
            if (remainingNetRequested.compareTo(BigDecimal.ZERO) <= 0) break;

            FixedIncomeLot.LotState state = lot.projectState();

            BigDecimal irTaxProvision = lot.getProjectedIrTax(today, state);
            BigDecimal iofTaxProvision = lot.getProjectedIofTax(today, state);
            BigDecimal lotNetBalance = lot.getProjectedNetBalance(today, state);

            if (lotNetBalance.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal netAmountToTake = remainingNetRequested.min(lotNetBalance);
            BigDecimal ratio = netAmountToTake.divide(lotNetBalance, 8, RoundingMode.HALF_UP);

            BigDecimal irTaxMaterialized = irTaxProvision.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
            BigDecimal iofTaxMaterialized = iofTaxProvision.multiply(ratio).setScale(2, RoundingMode.HALF_UP);

            BigDecimal grossAmountToTake = netAmountToTake.add(irTaxMaterialized).add(iofTaxMaterialized);

            InvestmentTransaction rescueTransaction = InvestmentTransaction.builder()
                    .lot(lot)
                    .type(InvestmentTransactionType.RESCUE)
                    .referenceDate(today)
                    .grossAmount(grossAmountToTake)
                    .amount(netAmountToTake)
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

        txRepo.saveAll(transactionsToSave);

        transactionService.createInvestmentTransaction(
                box.getProduct().getAccount().getId(),
                totalNetAmountToCredit,
                MovementDirection.INFLOW,
                today,
                "Resgate da caixinha: " + box.getName()
        );
    }
}
