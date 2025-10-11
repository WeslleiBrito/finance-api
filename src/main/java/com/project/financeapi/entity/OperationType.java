package com.project.financeapi.entity;

import com.project.financeapi.enums.MovementType;
import com.project.financeapi.enums.OperationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Setter;

@Entity
@Table(name = "operation_type")
public class OperationType {
    @Id
    @Setter(AccessLevel.PRIVATE)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType movementType;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type_status", nullable = false)
    private OperationStatus operationStatus = OperationStatus.ACTIVE;

    @Column(nullable = false, name="is_global")
    private Boolean isGlobal = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id")
    private OperationGroup group;

}
