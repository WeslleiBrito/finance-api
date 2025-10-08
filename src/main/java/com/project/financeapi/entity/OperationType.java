package com.project.financeapi.entity;

import com.project.financeapi.enums.MovementType;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id")
    private OperationGroup group;

}
