package com.project.financeapi.entity;

import com.project.financeapi.dto.card.cardBrand.CardBrandResponseDTO;
import com.project.financeapi.dto.card.creditCard.CardResponseDTO;
import com.project.financeapi.enums.CardBrandStatus;
import com.project.financeapi.enums.CardStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="card_brand")
@Getter
@Setter
public class CardBrand {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardBrandStatus status = CardBrandStatus.ACTIVE;

    @Column(name = "is_global", nullable = false)
    private boolean isGlobal = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt = LocalDate.now();

    public CardBrand() {}

    public CardBrand(String name, User createdBy) {
        this.name = name;
        this.createdBy = createdBy;
    }

    public CardBrandResponseDTO toResponse() {

        return new CardBrandResponseDTO(
                this.getId(),
                this.getName(),
                this.getStatus(),
                this.isGlobal,
                this.getCreatedAt()
        );
    }
}
