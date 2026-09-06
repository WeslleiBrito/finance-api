package com.project.financeapi.entity;

import com.project.financeapi.enumSystem.InvestmentTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "investment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false, updatable = false)
    private FixedIncomeLot lot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private InvestmentTransactionType type; // APPORT, DAILY_YIELD, RESCUE, MATURITY_LIQUIDATION

    @Column(nullable = false, updatable = false)
    private LocalDate referenceDate; // D+1 para os rendimentos

    @Column(nullable = false, updatable = false, precision = 19, scale = 4)
    private BigDecimal grossAmount; // Crescimento puro (DAILY_YIELD) ou Valor Total Movimentado

    @Column(nullable = false, updatable = false, precision = 19, scale = 4)
    private BigDecimal amount; // Valor líquido final (só é menor que o grossAmount em saídas)

    // Regra 7: Impostos dinâmicos vs materializados
    // Em DAILY_YIELD, estes campos serão ZERO. Em RESCUE, guardam a mordida real da Receita.
    @Column(nullable = false, updatable = false, precision = 19, scale = 4)
    private BigDecimal irTax;

    @Column(nullable = false, updatable = false, precision = 19, scale = 4)
    private BigDecimal iofTax;

    @Column(updatable = false, precision = 10, scale = 8)
    private BigDecimal appliedMarketRate; // A taxa oficial do Bacen do dia, para garantir auditoria

    @Column(updatable = false)
    private String description;
}