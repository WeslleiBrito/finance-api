package com.project.financeapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "deactivated_operation_groups")
@Getter
@NoArgsConstructor
public class DeactivatedOperationGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_group_id", nullable = false)
    private OperationGroup operationGroup;

    public DeactivatedOperationGroup(User user, OperationGroup operationGroup) {
        this.user = user;
        this.operationGroup = operationGroup;
    }
}