package com.project.financeapi.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "operation_group")
@Getter
@Setter
public class OperationGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.PRIVATE)
    private String id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    public OperationGroup() {}

    public OperationGroup(String name, User createdBy) {
        this.name = name;
        this.createdBy = createdBy;
    }



    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OperationType> operationTypes = new ArrayList<>();

}
