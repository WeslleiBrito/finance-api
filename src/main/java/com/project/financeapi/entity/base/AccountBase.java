package com.project.financeapi.entity.base;

import com.project.financeapi.entity.AccountType;
import com.project.financeapi.entity.User;
import jakarta.persistence.*;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class AccountBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Setter
    @Column(nullable = false, length = 100)
    private String name;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime creationDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;


    public void deposit(BigDecimal value) {
        this.balance = this.balance.add(value);
    }

    public void withdraw(BigDecimal value) {
        this.balance = this.balance.subtract(value);
    }

}
