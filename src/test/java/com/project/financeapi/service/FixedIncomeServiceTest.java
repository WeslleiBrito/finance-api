package com.project.financeapi.service;

import com.project.financeapi.dto.investment.request.InvestmentRescueDTO;
import com.project.financeapi.entity.FixedIncome;
import com.project.financeapi.entity.FixedIncomeLot;
import com.project.financeapi.entity.InvestmentTransaction;
import com.project.financeapi.entity.account.CheckingAccount;
import com.project.financeapi.enumSystem.FixedIncomeType;
import com.project.financeapi.enumSystem.InvestmentTransactionType;
import com.project.financeapi.enumSystem.MovementDirection;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.FixedIncomeLotRepository;
import com.project.financeapi.repository.FixedIncomeRepository;
import com.project.financeapi.repository.InvestmentTransactionRepository;
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
class FixedIncomeServiceTest {

    @Mock
    private FixedIncomeRepository fixedIncomeRepository;
    @Mock
    private FixedIncomeLotRepository lotRepository;
    @Mock
    private InvestmentTransactionRepository investmentTransactionRepository;
    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private FixedIncomeService fixedIncomeService;

    @Captor
    private ArgumentCaptor<List<InvestmentTransaction>> transactionsCaptor;

    private FixedIncome fixedIncome;
    private FixedIncomeLot activeLot;
    private final UUID fixedIncomeId = UUID.randomUUID();
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        CheckingAccount account = new CheckingAccount();
        account.setId(UUID.randomUUID());

        fixedIncome = FixedIncome.builder()
                .id(fixedIncomeId)
                .account(account)
                .type(FixedIncomeType.LCI) // Isento de IR/IOF
                .build();

        activeLot = FixedIncomeLot.builder()
                .id(UUID.randomUUID())
                .fixedIncome(fixedIncome)
                .purchaseDate(today.minusDays(90))
                .ledgerStartDate(today.minusDays(90))
                .transactions(new ArrayList<>())
                .build();

        // Injeta R$ 1.000,00 orgânicos para o projectState() ler
        InvestmentTransaction apport = InvestmentTransaction.builder()
                .lot(activeLot)
                .type(InvestmentTransactionType.APPORT)
                .grossAmount(new BigDecimal("1000.00"))
                .amount(new BigDecimal("1000.00"))
                .irTax(BigDecimal.ZERO)
                .iofTax(BigDecimal.ZERO)
                .build();

        activeLot.getTransactions().add(apport);
    }

    @Test
    @DisplayName("Deve gerar o evento de RESGATE TOTAL corretamente sem mutar o Lote")
    void shouldExecuteFullRescueAndGenerateTransaction() {
        // Arrange
        InvestmentRescueDTO request = new InvestmentRescueDTO(fixedIncomeId, new BigDecimal("1000.00"));

        when(fixedIncomeRepository.findById(fixedIncomeId)).thenReturn(Optional.of(fixedIncome));
        when(lotRepository.findActiveLotsForRescueOderByOldest(fixedIncomeId)).thenReturn(List.of(activeLot));

        // Act
        fixedIncomeService.executeRescue(request);

        // Assert 1: Interceptamos as transações que iriam para o banco
        verify(investmentTransactionRepository, times(1)).saveAll(transactionsCaptor.capture());
        List<InvestmentTransaction> savedTransactions = transactionsCaptor.getValue();

        assertEquals(1, savedTransactions.size(), "Deve gerar exatamente um evento de resgate.");
        InvestmentTransaction rescueTx = savedTransactions.get(0);

        assertEquals(InvestmentTransactionType.RESCUE, rescueTx.getType());
        assertEquals(new BigDecimal("1000.00"), rescueTx.getGrossAmount(), "O evento deve atestar a saída integral.");

        // Assert 2: Verifica o crédito na conta corrente
        verify(transactionService, times(1)).createInvestmentTransaction(
                any(), eq(new BigDecimal("1000.00")), eq(MovementDirection.INFLOW), eq(today), anyString()
        );
    }

    @Test
    @DisplayName("Deve gerar o evento de RESGATE PARCIAL proporcional")
    void shouldExecutePartialRescueAndGenerateTransaction() {
        // Arrange
        InvestmentRescueDTO request = new InvestmentRescueDTO(fixedIncomeId, new BigDecimal("400.00"));

        when(fixedIncomeRepository.findById(fixedIncomeId)).thenReturn(Optional.of(fixedIncome));
        when(lotRepository.findActiveLotsForRescueOderByOldest(fixedIncomeId)).thenReturn(List.of(activeLot));

        // Act
        fixedIncomeService.executeRescue(request);

        // Assert 1: Intercepta e atesta a saída parcial
        verify(investmentTransactionRepository, times(1)).saveAll(transactionsCaptor.capture());
        List<InvestmentTransaction> savedTransactions = transactionsCaptor.getValue();

        assertEquals(1, savedTransactions.size());
        assertEquals(new BigDecimal("400.00"), savedTransactions.get(0).getGrossAmount());
    }

    @Test
    @DisplayName("Deve abortar a transação e lançar erro se tentar sacar mais do que o projetado")
    void shouldThrowExceptionWhenRequestedAmountExceedsBalance() {
        // Arrange
        InvestmentRescueDTO request = new InvestmentRescueDTO(fixedIncomeId, new BigDecimal("1500.00"));

        when(fixedIncomeRepository.findById(fixedIncomeId)).thenReturn(Optional.of(fixedIncome));
        when(lotRepository.findActiveLotsForRescueOderByOldest(fixedIncomeId)).thenReturn(List.of(activeLot));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> fixedIncomeService.executeRescue(request));
        assertTrue(exception.getMessage().contains("Saldo bruto insuficiente"));

        // Garante que nenhum evento foi gerado/salvo e nenhum dinheiro entrou na conta
        verify(investmentTransactionRepository, never()).saveAll(any());
        verify(transactionService, never()).createInvestmentTransaction(any(), any(), any(), any(), anyString());
    }
}