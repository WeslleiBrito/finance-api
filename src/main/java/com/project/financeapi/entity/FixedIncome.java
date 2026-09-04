package com.project.financeapi.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enumSystem.FixedIncomeStatus;
import com.project.financeapi.enumSystem.FixedIncomeType;
import com.project.financeapi.enumSystem.IndexerType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
public class FixedIncome {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;

    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "fixed_income_type", nullable = false)
    private FixedIncomeType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "indexer_type", nullable = false)
    private IndexerType indexer;

    // Ex: 110.00 (para 110% do CDI) ou 10.5 (para 10.5% Pré-fixado)
    @Column(name = "contracted_rate", precision = 10, scale = 4, nullable = false)
    private BigDecimal contractedRate;

    @Column(name = "maturity_date", nullable = true) // Ou simplesmente remova o nullable = false
    private LocalDate maturityDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FixedIncomeStatus status = FixedIncomeStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountBase account;

    // O relacionamento com os lotes (Aportes)
    @OneToMany(mappedBy = "fixedIncome", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("purchaseDate ASC")
    private List<FixedIncomeLot> lots = new ArrayList<>();
}