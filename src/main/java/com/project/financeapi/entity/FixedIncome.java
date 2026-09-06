package com.project.financeapi.entity;

import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enumSystem.FixedIncomeType;
import com.project.financeapi.enumSystem.IndexerType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "fixed_incomes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedIncome {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountBase account;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IndexerType indexer;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal contractedRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FixedIncomeType type;

    // Vencimento agora é opcional na base de dados para aceitar CDB de liquidez diária
    @Column(nullable = true)
    private LocalDate maturityDate;

    @Builder.Default
    @OneToMany(mappedBy = "fixedIncome", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FixedIncomeLot> lots = new ArrayList<>();

    @Transient
    public Boolean getIsTaxExempt() {
        return this.type.isTaxExempt();
    }
}