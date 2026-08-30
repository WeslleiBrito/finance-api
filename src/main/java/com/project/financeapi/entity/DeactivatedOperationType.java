package com.project.financeapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "deactivated_operation_types")
@Getter
@NoArgsConstructor
public class DeactivatedOperationType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_type_id", nullable = false)
    private OperationType operationType;

    public DeactivatedOperationType(User user, OperationType operationType) {
        this.user = user;
        this.operationType = operationType;
    }
}