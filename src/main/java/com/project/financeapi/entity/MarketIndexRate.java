package com.project.financeapi.entity;

import com.project.financeapi.enumSystem.IndexerType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "market_index_rates",
        uniqueConstraints = {
                // Garante que nunca teremos duas taxas de CDI cadastradas para o mesmo dia
                @UniqueConstraint(columnNames = {"indexer_type", "reference_date"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketIndexRate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "indexer_type", nullable = false, updatable = false)
    private IndexerType indexerType; // CDI, SELIC, IPCA, IGPM

    @Column(name = "reference_date", nullable = false, updatable = false)
    private LocalDate referenceDate; // O dia (ou mês, no caso do IPCA) a que a taxa pertence

    @Column(nullable = false, updatable = false, precision = 10, scale = 8)
    private BigDecimal rateValue; // O valor percentual daquele dia/mês (ex: 0.040168)
}