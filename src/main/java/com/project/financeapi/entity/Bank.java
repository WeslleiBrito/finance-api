package com.project.financeapi.entity;

import com.project.financeapi.enums.BankStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bank", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"created_by", "name"})
})
@Getter
@Setter
public class Bank {

    @Id
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 10)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BankStatus status = BankStatus.ACTIVE;

    @Column(name = "is_global", nullable = false)
    private Boolean isGlobal = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Bank() {
    }

    public Bank(String name, String code, User createdBy) {
        this.name = name;
        this.code = code;
        this.createdBy = createdBy;
    }

    public Bank(String name, User createdBy) {
        this.name = name;
        this.createdBy = createdBy;
    }
}
