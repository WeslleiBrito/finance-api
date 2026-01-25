package com.project.financeapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_operation_type")
@Getter
@Setter
public class UserOperationType {

    @EmbeddedId
    private UserOperationTypeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("operationTypeId")
    @JoinColumn(name = "operation_type_id", nullable = false)
    private OperationType operationType;

    @Column(nullable = false)
    private boolean enabled = true;

    protected UserOperationType() {
    }

    public UserOperationType(User user, OperationType operationType) {
        this.user = user;
        this.operationType = operationType;
        this.id = new UserOperationTypeId(
                user.getId(),
                operationType.getId()
        );

    }
}
