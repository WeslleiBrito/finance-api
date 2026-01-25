package com.project.financeapi.entity;

import com.project.financeapi.dto.OperationType.OperationTypeResponseDTO;
import com.project.financeapi.enumSystem.MovementType;
import com.project.financeapi.enumSystem.StatusEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "operation_type",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_operation_type_system",
                        columnNames = {"name", "movement_type", "is_system"}
                ),
                @UniqueConstraint(
                        name = "uq_operation_type_user",
                        columnNames = {"created_by", "name", "movement_type"}
                )
        }
)
@Setter
@Getter
public class OperationType {
    @Id
    @Setter(AccessLevel.PRIVATE)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operation_group_id")
    private OperationGroup group;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_status")
    private StatusEntity status = StatusEntity.ACTIVE;

    @Column(name = "is_system")
    private boolean isSystem = false;

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
                this.getStatus(),
                this.getGroup().toResponse()
        );
    }
}
