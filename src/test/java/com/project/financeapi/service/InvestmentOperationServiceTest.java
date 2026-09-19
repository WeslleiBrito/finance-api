package com.project.financeapi.service;

import com.project.financeapi.dto.investment.request.CreateBoxDTO;
import com.project.financeapi.dto.investment.request.CreateProductDTO;
import com.project.financeapi.dto.investment.request.InvestmentApportDTO;
import com.project.financeapi.dto.investment.request.InvestmentRescueDTO;
import com.project.financeapi.dto.investment.request.TierRequestDTO;
import com.project.financeapi.dto.investment.response.ProductDashboardDTO;
import com.project.financeapi.entity.*;
import com.project.financeapi.entity.account.CheckingAccount;
import com.project.financeapi.enumSystem.FixedIncomeType;
import com.project.financeapi.enumSystem.IndexerType;
import com.project.financeapi.enumSystem.InvestmentTransactionType;
import com.project.financeapi.enumSystem.MovementDirection;
import com.project.financeapi.enumSystem.YieldConvention;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestmentOperationServiceTest {

    @Mock private InvestmentProductRepository productRepo;
    @Mock private InvestmentTierRepository tierRepo;
    @Mock private InvestmentBoxRepository boxRepo;
    @Mock private FixedIncomeLotRepository lotRepo;
    @Mock private InvestmentTransactionRepository txRepo;
    @Mock private AccountRepository accountRepo;
    @Mock private TransactionService transactionService;
    @Mock private InvestmentLedgerService ledgerService;

    @InjectMocks
    private InvestmentOperationService operationService;

    @Captor
    private ArgumentCaptor<List<InvestmentTransaction>> transactionsCaptor;

    @Captor
    private ArgumentCaptor<InvestmentProduct> productCaptor;

    @Captor
    private ArgumentCaptor<List<InvestmentTier>> tiersCaptor;

    private CheckingAccount account;
    private InvestmentProduct product;
    private InvestmentBox box;
    private FixedIncomeLot activeLot;
    private final UUID accountId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID boxId = UUID.randomUUID();
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        account = new CheckingAccount();
        account.setId(accountId);

        product = InvestmentProduct.builder()
                .id(productId)
                .account(account)
                .name("CDB Banco Poupi")
                .type(FixedIncomeType.LCI)
                .indexer(IndexerType.CDI)
                .convention(YieldConvention.CDI_EXPONENTIAL_252)
                .build();

        box = InvestmentBox.builder()
                .id(boxId)
                .product(product)
                .name("Reserva de Emergência")
                .build();

        activeLot = FixedIncomeLot.builder()
                .id(UUID.randomUUID())
                .box(box)
                .purchaseDate(today.minusDays(90))
                .ledgerStartDate(today.minusDays(90))
                .transactions(new ArrayList<>())
                .build();

        InvestmentTransaction apport = InvestmentTransaction.builder()
                .lot(activeLot)
                .type(InvestmentTransactionType.APPORT)
                .grossAmount(new BigDecimal("1000.00"))
                .amount(new BigDecimal("1000.00"))
                .irTax(BigDecimal.ZERO)
                .iofTax(BigDecimal.ZERO)
                .referenceDate(today.minusDays(90))
                .build();

        activeLot.getTransactions().add(apport);
    }

    // --- TESTES DE CRIAÇÃO DE PRODUTO ---

    @Test
    @DisplayName("Deve criar Produto com convenção explícita e mapear requiredMonthlyMovement nos tiers")
    void shouldCreateProductWithConventionAndEligibilityRule() {
        when(accountRepo.findById(accountId)).thenReturn(Optional.of(account));
        when(productRepo.save(any())).thenReturn(product);

        ProductDashboardDTO mockDashboard = mock(ProductDashboardDTO.class);
        when(mockDashboard.id()).thenReturn(productId);
        when(ledgerService.getDashboardsByAccount(accountId)).thenReturn(List.of(mockDashboard));

        List<TierRequestDTO> tiers = List.of(
                new TierRequestDTO(BigDecimal.ZERO, new BigDecimal("4999.99"), new BigDecimal("115.00"), new BigDecimal("1000.00")),
                new TierRequestDTO(new BigDecimal("4999.99"), null, new BigDecimal("100.00"), null)
        );
        CreateProductDTO dto = new CreateProductDTO(
                accountId, "Mercado Pago", IndexerType.CDI, FixedIncomeType.CDB,
                YieldConvention.CDI_EXPONENTIAL_252, tiers
        );

        operationService.createProduct(dto);

        verify(productRepo).save(productCaptor.capture());
        assertEquals(YieldConvention.CDI_EXPONENTIAL_252, productCaptor.getValue().getConvention());

        verify(tierRepo).saveAll(tiersCaptor.capture());
        List<InvestmentTier> savedTiers = tiersCaptor.getValue();

        assertEquals(2, savedTiers.size());
        assertEquals(new BigDecimal("1000.00"), savedTiers.get(0).getRequiredMonthlyMovement());
        assertNull(savedTiers.get(1).getRequiredMonthlyMovement());
    }

    @Test
    @DisplayName("Deve aplicar CDI_EXPONENTIAL_252 como fallback quando convention não for informada")
    void shouldFallbackToDefaultConventionWhenNull() {
        when(accountRepo.findById(accountId)).thenReturn(Optional.of(account));
        when(productRepo.save(any())).thenReturn(product);

        ProductDashboardDTO mockDashboard = mock(ProductDashboardDTO.class);
        when(mockDashboard.id()).thenReturn(productId);
        when(ledgerService.getDashboardsByAccount(accountId)).thenReturn(List.of(mockDashboard));

        CreateProductDTO dto = new CreateProductDTO(
                accountId, "CDB Sem Convenção", IndexerType.CDI, FixedIncomeType.CDB,
                null, List.of()
        );

        operationService.createProduct(dto);

        verify(productRepo).save(productCaptor.capture());
        assertEquals(YieldConvention.CDI_EXPONENTIAL_252, productCaptor.getValue().getConvention());
        verify(tierRepo, never()).saveAll(any());
    }

    @Test
    @DisplayName("Deve lançar erro ao criar produto com conta inexistente")
    void shouldThrowWhenAccountNotFoundOnCreateProduct() {
        when(accountRepo.findById(accountId)).thenReturn(Optional.empty());

        CreateProductDTO dto = new CreateProductDTO(
                accountId, "CDB Fantasma", IndexerType.CDI, FixedIncomeType.CDB,
                YieldConvention.CDI_EXPONENTIAL_252, List.of()
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> operationService.createProduct(dto));
        assertTrue(exception.getMessage().contains("Conta não encontrada"));

        verify(productRepo, never()).save(any());
    }

    // --- TESTES DE CRIAÇÃO DE CAIXINHA ---

    @Test
    @DisplayName("Deve criar Caixinha vinculada a um Produto existente")
    void shouldCreateBox() {
        when(productRepo.findById(productId)).thenReturn(Optional.of(product));
        when(boxRepo.save(any())).thenReturn(box);

        ProductDashboardDTO mockDashboard = mock(ProductDashboardDTO.class);
        when(mockDashboard.id()).thenReturn(productId);
        when(ledgerService.getDashboardsByAccount(account.getId())).thenReturn(List.of(mockDashboard));

        CreateBoxDTO dto = new CreateBoxDTO(productId, "Nova Meta");

        operationService.createBox(dto);

        verify(boxRepo, times(1)).save(any(InvestmentBox.class));
    }

    @Test
    @DisplayName("Deve lançar erro ao criar Caixinha para Produto inexistente")
    void shouldThrowWhenProductNotFoundOnCreateBox() {
        when(productRepo.findById(productId)).thenReturn(Optional.empty());

        CreateBoxDTO dto = new CreateBoxDTO(productId, "Nova Meta");

        BusinessException exception = assertThrows(BusinessException.class, () -> operationService.createBox(dto));
        assertTrue(exception.getMessage().contains("Produto de investimento não encontrado"));

        verify(boxRepo, never()).save(any());
    }

    // --- TESTES DE APORTE ---

    @Test
    @DisplayName("Deve realizar Aporte isolado criando Lote e Transação com data explícita")
    void shouldExecuteApportWithExplicitDate() {
        when(boxRepo.findById(boxId)).thenReturn(Optional.of(box));
        when(accountRepo.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));
        when(lotRepo.save(any())).thenReturn(activeLot);

        InvestmentApportDTO dto = new InvestmentApportDTO(accountId, boxId, new BigDecimal("500.00"), today);

        operationService.executeApport(dto);

        verify(transactionService, times(1)).createInvestmentTransaction(
                eq(accountId), eq(new BigDecimal("500.00")), eq(MovementDirection.OUTFLOW), eq(today), anyString());
        verify(lotRepo, times(1)).save(any(FixedIncomeLot.class));
        verify(txRepo, times(1)).save(any(InvestmentTransaction.class));
    }

    @Test
    @DisplayName("Deve usar LocalDate.now() como purchaseDate quando não informado no aporte")
    void shouldUseTodayAsPurchaseDateWhenNullInApport() {
        when(boxRepo.findById(boxId)).thenReturn(Optional.of(box));
        when(accountRepo.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));
        when(lotRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        InvestmentApportDTO dto = new InvestmentApportDTO(accountId, boxId, new BigDecimal("300.00"), null);

        operationService.executeApport(dto);

        ArgumentCaptor<FixedIncomeLot> lotCaptor = ArgumentCaptor.forClass(FixedIncomeLot.class);
        verify(lotRepo).save(lotCaptor.capture());
        assertEquals(today, lotCaptor.getValue().getPurchaseDate());

        ArgumentCaptor<InvestmentTransaction> txCaptor = ArgumentCaptor.forClass(InvestmentTransaction.class);
        verify(txRepo).save(txCaptor.capture());
        assertEquals(today, txCaptor.getValue().getReferenceDate());
        assertEquals(InvestmentTransactionType.APPORT, txCaptor.getValue().getType());
    }

    @Test
    @DisplayName("Deve lançar erro ao aportar em Caixinha inexistente")
    void shouldThrowWhenBoxNotFoundOnApport() {
        when(boxRepo.findById(boxId)).thenReturn(Optional.empty());

        InvestmentApportDTO dto = new InvestmentApportDTO(accountId, boxId, new BigDecimal("500.00"), today);

        BusinessException exception = assertThrows(BusinessException.class, () -> operationService.executeApport(dto));
        assertTrue(exception.getMessage().contains("Caixinha (Objetivo) não encontrada"));

        verify(lotRepo, never()).save(any());
        verify(txRepo, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro ao aportar com conta de origem inexistente")
    void shouldThrowWhenDebitAccountNotFoundOnApport() {
        when(boxRepo.findById(boxId)).thenReturn(Optional.of(box));
        when(accountRepo.findByIdForUpdate(accountId)).thenReturn(Optional.empty());

        InvestmentApportDTO dto = new InvestmentApportDTO(accountId, boxId, new BigDecimal("500.00"), today);

        BusinessException exception = assertThrows(BusinessException.class, () -> operationService.executeApport(dto));
        assertTrue(exception.getMessage().contains("Conta de origem não encontrada"));

        verify(lotRepo, never()).save(any());
    }

    // --- TESTES DE RESGATE ---

    @Test
    @DisplayName("Deve gerar o evento de RESGATE TOTAL mirando diretamente na Caixinha")
    void shouldExecuteFullRescueAndGenerateTransaction() {
        InvestmentRescueDTO request = new InvestmentRescueDTO(boxId, new BigDecimal("1000.00"));

        when(boxRepo.findByIdWithDetails(boxId)).thenReturn(Optional.of(box));
        when(lotRepo.findActiveLotsForRescueByBox(boxId)).thenReturn(List.of(activeLot));

        operationService.executeRescue(request);

        verify(txRepo, times(1)).saveAll(transactionsCaptor.capture());
        List<InvestmentTransaction> savedTransactions = transactionsCaptor.getValue();

        assertEquals(1, savedTransactions.size());
        InvestmentTransaction rescueTx = savedTransactions.get(0);

        assertEquals(InvestmentTransactionType.RESCUE, rescueTx.getType());
        assertEquals(new BigDecimal("1000.00"), rescueTx.getGrossAmount());
        assertEquals(new BigDecimal("1000.00"), rescueTx.getAmount());

        verify(transactionService, times(1)).createInvestmentTransaction(
                eq(accountId), eq(new BigDecimal("1000.00")), eq(MovementDirection.INFLOW), eq(today), anyString()
        );
    }

    @Test
    @DisplayName("Deve gerar o evento de RESGATE PARCIAL consumindo apenas parte do Lote")
    void shouldExecutePartialRescueAndGenerateTransaction() {
        InvestmentRescueDTO request = new InvestmentRescueDTO(boxId, new BigDecimal("400.00"));

        when(boxRepo.findByIdWithDetails(boxId)).thenReturn(Optional.of(box));
        when(lotRepo.findActiveLotsForRescueByBox(boxId)).thenReturn(List.of(activeLot));

        operationService.executeRescue(request);

        verify(txRepo, times(1)).saveAll(transactionsCaptor.capture());
        List<InvestmentTransaction> savedTransactions = transactionsCaptor.getValue();

        assertEquals(1, savedTransactions.size());
        assertEquals(new BigDecimal("400.00"), savedTransactions.getFirst().getAmount());
    }

    @Test
    @DisplayName("Deve abortar a transação e lançar erro se o resgate pedido for maior que o saldo líquido da Caixinha")
    void shouldThrowExceptionWhenRequestedAmountExceedsBoxBalance() {
        InvestmentRescueDTO request = new InvestmentRescueDTO(boxId, new BigDecimal("1500.00"));

        when(boxRepo.findByIdWithDetails(boxId)).thenReturn(Optional.of(box));
        when(lotRepo.findActiveLotsForRescueByBox(boxId)).thenReturn(List.of(activeLot));

        BusinessException exception = assertThrows(BusinessException.class, () -> operationService.executeRescue(request));
        assertTrue(exception.getMessage().contains("Saldo líquido insuficiente"));

        verify(txRepo, never()).saveAll(any());
        verify(transactionService, never()).createInvestmentTransaction(any(), any(), any(), any(), anyString());
    }

    @Test
    @DisplayName("Deve lançar erro ao resgatar Caixinha inexistente")
    void shouldThrowWhenBoxNotFoundOnRescue() {
        when(boxRepo.findByIdWithDetails(boxId)).thenReturn(Optional.empty());

        InvestmentRescueDTO request = new InvestmentRescueDTO(boxId, new BigDecimal("100.00"));

        BusinessException exception = assertThrows(BusinessException.class, () -> operationService.executeRescue(request));
        assertTrue(exception.getMessage().contains("Caixinha não encontrada"));

        verify(txRepo, never()).saveAll(any());
    }

    @Test
    @DisplayName("Deve lançar erro ao resgatar Caixinha sem lotes ativos com saldo")
    void shouldThrowWhenNoActiveLotsAvailableForRescue() {
        InvestmentRescueDTO request = new InvestmentRescueDTO(boxId, new BigDecimal("100.00"));

        when(boxRepo.findByIdWithDetails(boxId)).thenReturn(Optional.of(box));
        when(lotRepo.findActiveLotsForRescueByBox(boxId)).thenReturn(List.of());

        BusinessException exception = assertThrows(BusinessException.class, () -> operationService.executeRescue(request));
        assertTrue(exception.getMessage().contains("Saldo líquido insuficiente"));
    }
}
