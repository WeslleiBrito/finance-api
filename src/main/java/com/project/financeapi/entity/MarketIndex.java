package com.project.financeapi.entity;

import com.project.financeapi.enumSystem.IndexType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "market_indices", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"reference_date", "index_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MarketIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_date", nullable = false)
    private LocalDate referenceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "index_type", nullable = false)
    private IndexType indexType;

    @Column(precision = 10, scale = 7, nullable = false)
    private BigDecimal rate;
}