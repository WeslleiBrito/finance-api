package com.project.financeapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_operation_group")
@Getter
@Setter
public class UserOperationGroup {

    @EmbeddedId
    private UserOperationGroupId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("operationGroupId")
    @JoinColumn(name = "operation_group_id")
    private OperationGroup operationGroup;

    @Column(nullable = false)
    private boolean enabled = true;

    protected UserOperationGroup() {}

    public UserOperationGroup(User user, OperationGroup group) {
        this.user = user;
        this.operationGroup = group;
        this.id = new UserOperationGroupId(user.getId(), group.getId());
    }
}
