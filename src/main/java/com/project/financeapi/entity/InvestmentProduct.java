package com.project.financeapi.entity;

import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enumSystem.FixedIncomeType;
import com.project.financeapi.enumSystem.IndexerType;
import com.project.financeapi.enumSystem.YieldConvention;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "investment_products")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class InvestmentProduct {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountBase account;

    private String name;

    @Enumerated(EnumType.STRING)
    private IndexerType indexer;

    @Enumerated(EnumType.STRING)
    private FixedIncomeType type;

    // NOVO: define COMO a taxa do indexador se transforma em rendimento diário/mensal.
    // Sem isso o engine não sabe se é exponencial 252, mensal-aniversário, etc.
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private YieldConvention convention = YieldConvention.CDI_EXPONENTIAL_252;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @org.hibernate.annotations.BatchSize(size = 50)
    private List<InvestmentTier> tiers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @org.hibernate.annotations.BatchSize(size = 50)
    private List<InvestmentBox> boxes = new ArrayList<>();

    /**
     * Convenções que precisam avaliar cada lote isoladamente (ex: aniversário
     * de aporte na poupança). Diferente de CDI, que pode ser calculado
     * agregando todos os lotes num "superBalance" único.
     */
    @Transient
    public boolean requiresPerLotCalculation() {
        return convention == YieldConvention.POUPANCA_MONTHLY_ANIVERSARIO;
    }

    @Transient
    public boolean isActive() {
        if (this.boxes == null || this.boxes.isEmpty()) return false;
        return this.boxes.stream().anyMatch(InvestmentBox::hasBalance);
    }
}
