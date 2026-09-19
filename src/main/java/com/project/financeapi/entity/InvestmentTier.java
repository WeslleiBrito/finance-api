package com.project.financeapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "investment_tiers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class InvestmentTier {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private InvestmentProduct product;

    @Column(precision = 15, scale = 4)
    private BigDecimal minBalance;

    @Column(precision = 15, scale = 4)
    private BigDecimal maxBalance;

    @Column(precision = 10, scale = 4)
    private BigDecimal rateMultiplier;

    // NOVO: se não-nulo, exige movimentação mínima (conta + caixinha) no mês
    // para este tier ser aplicável. Ex: Mercado Pago exige R$1.000/mês p/ 115%.
    @Column(precision = 15, scale = 4)
    private BigDecimal requiredMonthlyMovement;
}
