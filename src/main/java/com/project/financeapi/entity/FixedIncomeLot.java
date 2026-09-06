package com.project.financeapi.entity;

import com.project.financeapi.enumSystem.InvestmentTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "fixed_income_lots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedIncomeLot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixed_income_id", nullable = false)
    private FixedIncome fixedIncome;

    @Column(nullable = false, updatable = false)
    private LocalDate purchaseDate;

    @Column(nullable = false, updatable = false)
    private LocalDate ledgerStartDate;

    @Builder.Default
    @OneToMany(mappedBy = "lot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvestmentTransaction> transactions = new ArrayList<>();

    // ========================================================================
    // EVENT SOURCING: PROJEÇÃO DE ESTADO NO TEMPO
    // ========================================================================

    public record LotState(BigDecimal remainingPrincipal, BigDecimal grossBalance) {}

    @Transient
    public LotState projectState() {
        return projectStateUpTo(LocalDate.now().plusDays(1)); // Uma data futura para englobar todos os eventos
    }

    @Transient
    public LotState projectStateUpTo(LocalDate targetDate) {
        BigDecimal principal = BigDecimal.ZERO;
        BigDecimal balance = BigDecimal.ZERO;

        if (transactions == null || transactions.isEmpty()) {
            return new LotState(principal, balance);
        }

        // Reconstrói a história ignorando eventos que ocorreram DEPOIS da data alvo
        List<InvestmentTransaction> history = transactions.stream()
                .filter(t -> !t.getReferenceDate().isAfter(targetDate))
                .sorted(Comparator.comparing(InvestmentTransaction::getReferenceDate))
                .toList();

        for (InvestmentTransaction tx : history) {
            if (tx.getType() == InvestmentTransactionType.APPORT) {
                principal = principal.add(tx.getGrossAmount());
                balance = balance.add(tx.getGrossAmount());
            } else if (tx.getType() == InvestmentTransactionType.DAILY_YIELD) {
                balance = balance.add(tx.getGrossAmount());
            } else if (tx.getType() == InvestmentTransactionType.RESCUE) {
                if (balance.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal ratio = tx.getGrossAmount().divide(balance, 8, RoundingMode.HALF_UP);
                    BigDecimal principalDeduction = principal.multiply(ratio).setScale(4, RoundingMode.HALF_UP);
                    principal = principal.subtract(principalDeduction);
                }
                balance = balance.subtract(tx.getGrossAmount());
            }
        }
        return new LotState(principal.max(BigDecimal.ZERO), balance.max(BigDecimal.ZERO));
    }

    @Transient
    public Optional<LocalDate> getLastYieldDate() {
        if (transactions == null) return Optional.empty();
        return transactions.stream()
                .filter(t -> t.getType() == InvestmentTransactionType.DAILY_YIELD)
                .map(InvestmentTransaction::getReferenceDate)
                .max(LocalDate::compareTo);
    }

    // ========================================================================
    // INTELIGÊNCIA TRIBUTÁRIA
    // ========================================================================

    @Transient
    public long getAgeInDays(LocalDate referenceDate) {
        if (this.purchaseDate == null || referenceDate == null) return 0;
        long days = ChronoUnit.DAYS.between(this.purchaseDate, referenceDate);
        return Math.max(days, 0);
    }

    @Transient
    public BigDecimal getProjectedIofTax(LocalDate currentDate, LotState state) {
        if (fixedIncome.getIsTaxExempt()) return BigDecimal.ZERO;
        BigDecimal profit = state.grossBalance().subtract(state.remainingPrincipal()).max(BigDecimal.ZERO);
        if (profit.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        long days = getAgeInDays(currentDate);
        if (days >= 30) return BigDecimal.ZERO;

        BigDecimal iofRate = BigDecimal.valueOf(100 - (days * 3.33)).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return profit.multiply(iofRate).setScale(2, RoundingMode.HALF_UP).max(BigDecimal.ZERO);
    }

    @Transient
    public BigDecimal getProjectedIrTax(LocalDate currentDate, LotState state) {
        if (fixedIncome.getIsTaxExempt()) return BigDecimal.ZERO;
        BigDecimal profit = state.grossBalance().subtract(state.remainingPrincipal()).max(BigDecimal.ZERO);
        BigDecimal iof = getProjectedIofTax(currentDate, state);
        BigDecimal taxableBase = profit.subtract(iof).max(BigDecimal.ZERO);

        long days = getAgeInDays(currentDate);
        BigDecimal irRate;
        if (days <= 180) irRate = BigDecimal.valueOf(0.225);
        else if (days <= 360) irRate = BigDecimal.valueOf(0.20);
        else if (days <= 720) irRate = BigDecimal.valueOf(0.175);
        else irRate = BigDecimal.valueOf(0.15);

        return taxableBase.multiply(irRate).setScale(2, RoundingMode.HALF_UP);
    }

    @Transient
    public BigDecimal getProjectedNetBalance(LocalDate currentDate, LotState state) {
        return state.grossBalance()
                .subtract(getProjectedIofTax(currentDate, state))
                .subtract(getProjectedIrTax(currentDate, state));
    }
}