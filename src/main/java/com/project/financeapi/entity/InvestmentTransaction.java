package com.project.financeapi.entity;

import com.project.financeapi.enumSystem.InvestmentTransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "investment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lot_id", nullable = false)
    private FixedIncomeLot lot;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private InvestmentTransactionType type;

    // O valor líquido que de fato impacta o saldo do lote (positivo para APPORT/YIELD, negativo para RESCUE)
    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    // O lucro sujo antes dos impostos (Útil para gráficos e declaração de IR)
    @Column(name = "gross_amount", precision = 15, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "ir_tax", precision = 15, scale = 2)
    private BigDecimal irTax;

    @Column(name = "iof_tax", precision = 15, scale = 2)
    private BigDecimal iofTax;

    @Column(name = "b3_custody_fee", precision = 15, scale = 2)
    private BigDecimal b3CustodyFee;

    @Column(name = "reference_date", nullable = false)
    private LocalDate referenceDate;

    // A nossa "fotografia" de auditoria: salva a taxa exata usada no dia para este cálculo
    @Column(name = "applied_market_rate", precision = 15, scale = 8)
    private BigDecimal appliedMarketRate;

    @Column(name = "description", length = 150)
    private String description;
}