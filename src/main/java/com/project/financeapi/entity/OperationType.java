package com.project.financeapi.entity;

import com.project.financeapi.dto.OperationType.OperationTypeResponseDTO;
import com.project.financeapi.enums.MovementType;
import com.project.financeapi.enums.OperationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "operation_type")
@Setter
@Getter
public class OperationType {
    @Id
    @Setter(AccessLevel.PRIVATE)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
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
    @JoinColumn(name = "operation_group_id")
    private OperationGroup group;

    public OperationType(String name, MovementType movementType, User createdBy, OperationGroup group) {
        this.name = name;
        this.movementType = movementType;
        this.createdBy = createdBy;
        this.group = group;
    }

    public OperationType() {
    }

    public OperationTypeResponseDTO toResponse() {
        return new OperationTypeResponseDTO(
                this.getId(),
                this.getName(),
                this.getMovementType(),
                this.getOperationStatus(),
                this.getIsGlobal(),
                this.getGroup().toResponse()
        );
    }
}
