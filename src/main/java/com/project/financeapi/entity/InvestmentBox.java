package com.project.financeapi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "investment_boxes")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class InvestmentBox {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private InvestmentProduct product;

    private String name;

    @Builder.Default
    @OneToMany(mappedBy = "box", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private List<FixedIncomeLot> lots = new ArrayList<>();

    @Transient
    public boolean hasBalance() {
        if (this.lots == null || this.lots.isEmpty()) {
            return false;
        }
        return this.lots.stream().anyMatch(lot -> lot.projectState().grossBalance().compareTo(BigDecimal.ZERO) > 0);
    }
}