package com.project.financeapi.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enumSystem.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter @ToString(exclude = "accounts")
public class User {

    @Id
    @Column(name = "id", length = 128)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    private UserStatus userStatus = UserStatus.ACTIVATED;

    @JsonBackReference
    @Setter(AccessLevel.PRIVATE)
    @OneToMany(mappedBy = "accountHolder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountBase> accounts = new ArrayList<>();

    public User() {
    }
}