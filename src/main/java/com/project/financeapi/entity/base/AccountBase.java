package com.project.financeapi.entity.base;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.project.financeapi.enums.AccountStatus;
import com.project.financeapi.enums.AccountType;
import com.project.financeapi.entity.Transaction;
import com.project.financeapi.entity.User;
import com.project.financeapi.enums.MovementType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AccountBase {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Setter
    @Column(nullable = false, length = 100)
    private String name;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus status = AccountStatus.ACTIVATED;

    @Column(name = "initial_value", nullable = false)
    private BigDecimal initialValue;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime creationDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User accountHolder;

    @JsonManagedReference
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<Transaction>();

    public AccountBase(AccountType type, User accountHolder, BigDecimal initialValue) {
        this.type = type;
        this.accountHolder = accountHolder;
        this.initialValue = initialValue != null ? initialValue : BigDecimal.ZERO;
    }

    public AccountBase() {
    }

    public BigDecimal getBalance() {

        if(transactions == null || transactions.isEmpty()){
            return initialValue;
        }

        return initialValue.add(transactions.stream()
                .map(t -> {
                    if(t.getMovementType() == MovementType.CREDIT){
                        return t.getAmount();
                    }else{
                        return t.getAmount().negate();
                    }
                }).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

}
