package com.project.financeapi.entity;

import com.project.financeapi.dto.bank.*;
import com.project.financeapi.enumSystem.StatusEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bank")
@Getter
@Setter
public class Bank {

    @Id
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 🌟 Adicionamos unique = true diretamente na coluna
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(length = 10)
    private String code;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'ACTIVE'")
    @Enumerated(EnumType.STRING)
    private StatusEntity status = StatusEntity.ACTIVE;

    public Bank() {
    }

    public Bank(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public BankResponseDTO toResponse() {
        return new BankResponseDTO(
                this.getId(),
                this.getName(),
                this.getCode(),
                this.getStatus()
        );
    }
}