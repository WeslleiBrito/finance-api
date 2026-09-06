package com.project.financeapi.entity;

import com.project.financeapi.dto.operationGroup.OperationGroupResponseDTO;
import com.project.financeapi.enumSystem.StatusEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "operation_group",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_operation_group_system",
                        columnNames = {"name", "is_system"}
                ),
                @UniqueConstraint(
                        name = "uq_operation_group_user",
                        columnNames = {"created_by", "name"}
                )
        }
)
@Getter
@Setter
public class OperationGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "is_system", nullable = false)
    private boolean isSystem = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OperationType> operationTypes = new ArrayList<>();

    protected OperationGroup() {}

    public OperationGroup(String name, User createdBy) {
        this.name = name;
        this.createdBy = createdBy;
    }

    public OperationGroupResponseDTO toResponse(StatusEntity status) {
        return new OperationGroupResponseDTO(
                this.getId(),
                this.getName(),
                this.isSystem,
                status
        );
    }
}
