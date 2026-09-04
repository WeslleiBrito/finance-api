package com.project.financeapi.entity;

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
@Table(name = "fixed_income_lots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FixedIncomeLot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fixed_income_id", nullable = false)
    private FixedIncome fixedIncome;

    @Column(name = "initial_principal", precision = 15, scale = 2, nullable = false)
    private BigDecimal initialPrincipal;

    @Column(name = "remaining_principal", precision = 15, scale = 2, nullable = false)
    private BigDecimal remainingPrincipal;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    // O relacionamento com o histórico financeiro (Extrato) deste lote específico
    @OneToMany(mappedBy = "lot", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("referenceDate ASC")
    private List<InvestmentTransaction> transactions = new ArrayList<>();
}