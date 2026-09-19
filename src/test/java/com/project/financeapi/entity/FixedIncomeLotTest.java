package com.project.financeapi.entity;

import com.project.financeapi.enumSystem.FixedIncomeType;
import com.project.financeapi.enumSystem.InvestmentTransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class FixedIncomeLotTest {

    @Mock
    private InvestmentProduct mockProduct;
    @Mock
    private InvestmentBox mockBox;

    private FixedIncomeLot lot;
    private final LocalDate purchaseDate = LocalDate.of(2026, 1, 1);

    @BeforeEach
    void setUp() {
        // Mocka a cadeia: Lote -> Caixinha -> Produto (Para acessar o Tipo do Investimento)
        lenient().when(mockBox.getProduct()).thenReturn(mockProduct);
        lenient().when(mockProduct.getType()).thenReturn(FixedIncomeType.CDB); // Padrão: Não isento

        lot = FixedIncomeLot.builder()
                .box(mockBox)
                .purchaseDate(purchaseDate)
                .ledgerStartDate(purchaseDate)
                .transactions(new ArrayList<>())
                .build();

        InvestmentTransaction apport = InvestmentTransaction.builder()
                .lot(lot)
                .type(InvestmentTransactionType.APPORT)
                .referenceDate(purchaseDate)
                .grossAmount(new BigDecimal("1000.00"))
                .amount(new BigDecimal("1000.00"))
                .irTax(BigDecimal.ZERO)
                .iofTax(BigDecimal.ZERO)
                .build();

        InvestmentTransaction yield = InvestmentTransaction.builder()
                .lot(lot)
                .type(InvestmentTransactionType.DAILY_YIELD)
                .referenceDate(purchaseDate.plusDays(10))
                .grossAmount(new BigDecimal("100.00")) // R$ 100 de lucro
                .amount(new BigDecimal("100.00"))
                .irTax(BigDecimal.ZERO)
                .iofTax(BigDecimal.ZERO)
                .build();

        lot.getTransactions().add(apport);
        lot.getTransactions().add(yield);
    }

    @Test
    @DisplayName("Deve calcular a idade do lote corretamente (getAgeInDays)")
    void shouldCalculateAgeCorrectly() {
        LocalDate referenceDate = purchaseDate.plusDays(45);
        long age = lot.getAgeInDays(referenceDate);
        assertEquals(45, age);
    }

    @Test
    @DisplayName("Não deve retornar idade negativa se a data de referência for anterior à compra")
    void shouldNotReturnNegativeAge() {
        LocalDate referenceDate = purchaseDate.minusDays(10);
        long age = lot.getAgeInDays(referenceDate);
        assertEquals(0, age, "A idade do dinheiro não pode ser negativa");
    }

    @Test
    @DisplayName("Deve zerar IR e IOF para papéis isentos (ex: LCI) independentemente do lucro")
    void shouldExemptTaxesForLci() {
        lenient().when(mockProduct.getType()).thenReturn(FixedIncomeType.LCI);
        LocalDate referenceDate = purchaseDate.plusDays(15);

        FixedIncomeLot.LotState state = lot.projectState();
        BigDecimal irTax = lot.getProjectedIrTax(referenceDate, state);
        BigDecimal iofTax = lot.getProjectedIofTax(referenceDate, state);

        assertEquals(0, irTax.compareTo(BigDecimal.ZERO), "IR deve ser zero para LCI");
        assertEquals(0, iofTax.compareTo(BigDecimal.ZERO), "IOF deve ser zero para LCI");
    }

    @Test
    @DisplayName("Deve cobrar IOF para CDB (Não isento) com menos de 30 dias")
    void shouldApplyIofForCdbUnder30Days() {
        LocalDate referenceDate = purchaseDate.plusDays(15);
        FixedIncomeLot.LotState state = lot.projectState();
        BigDecimal iofTax = lot.getProjectedIofTax(referenceDate, state);

        assertTrue(iofTax.compareTo(BigDecimal.ZERO) > 0, "Deve haver cobrança de IOF antes de 30 dias");
    }

    @Test
    @DisplayName("Deve zerar IOF para CDB (Não isento) após 30 dias de investimento")
    void shouldZeroIofForCdbAfter30Days() {
        LocalDate referenceDate = purchaseDate.plusDays(30);
        FixedIncomeLot.LotState state = lot.projectState();
        BigDecimal iofTax = lot.getProjectedIofTax(referenceDate, state);

        assertEquals(0, iofTax.compareTo(BigDecimal.ZERO), "IOF deve ser zero a partir do 30º dia");
    }

    @Test
    @DisplayName("Deve garantir que o Saldo Líquido é igual ao Saldo Bruto menos Impostos")
    void shouldCalculateNetBalanceCorrectly() {
        LocalDate referenceDate = purchaseDate.plusDays(15);
        FixedIncomeLot.LotState state = lot.projectState();

        BigDecimal gross = state.grossBalance();
        BigDecimal irTax = lot.getProjectedIrTax(referenceDate, state);
        BigDecimal iofTax = lot.getProjectedIofTax(referenceDate, state);
        BigDecimal net = lot.getProjectedNetBalance(referenceDate, state);

        BigDecimal expectedNet = gross.subtract(irTax).subtract(iofTax);
        assertEquals(0, net.compareTo(expectedNet), "Saldo líquido não bate com a dedução de impostos");
    }

    @Test
    @DisplayName("Deve cobrar 22,5% de IR para resgates até 180 dias")
    void shouldApply22_5PercentIrTax() {
        LocalDate referenceDate = purchaseDate.plusDays(180);
        FixedIncomeLot.LotState state = lot.projectState();
        BigDecimal irTax = lot.getProjectedIrTax(referenceDate, state);
        assertEquals(new BigDecimal("22.50"), irTax);
    }

    @Test
    @DisplayName("Deve cobrar 20,0% de IR para resgates entre 181 e 360 dias")
    void shouldApply20_0PercentIrTax() {
        LocalDate referenceDate = purchaseDate.plusDays(181);
        FixedIncomeLot.LotState state = lot.projectState();
        BigDecimal irTax = lot.getProjectedIrTax(referenceDate, state);
        assertEquals(new BigDecimal("20.00"), irTax);
    }

    @Test
    @DisplayName("Deve cobrar 17,5% de IR para resgates entre 361 e 720 dias")
    void shouldApply17_5PercentIrTax() {
        LocalDate referenceDate = purchaseDate.plusDays(361);
        FixedIncomeLot.LotState state = lot.projectState();
        BigDecimal irTax = lot.getProjectedIrTax(referenceDate, state);
        assertEquals(new BigDecimal("17.50"), irTax);
    }

    @Test
    @DisplayName("Deve cobrar 15,0% de IR para resgates após 720 dias")
    void shouldApply15_0PercentIrTax() {
        LocalDate referenceDate = purchaseDate.plusDays(721);
        FixedIncomeLot.LotState state = lot.projectState();
        BigDecimal irTax = lot.getProjectedIrTax(referenceDate, state);
        assertEquals(new BigDecimal("15.00"), irTax);
    }
}