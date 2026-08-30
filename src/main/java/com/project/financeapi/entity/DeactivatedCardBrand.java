package com.project.financeapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "deactivated_card_brands")
@Getter
@NoArgsConstructor
public class DeactivatedCardBrand {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_brand_id", nullable = false)
    private CardBrand cardBrand;

    public DeactivatedCardBrand(User user, CardBrand cardBrand) {
        this.user = user;
        this.cardBrand = cardBrand;
    }
}