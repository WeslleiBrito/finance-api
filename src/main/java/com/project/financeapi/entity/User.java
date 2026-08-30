package com.project.financeapi.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enumSystem.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "O nome é obrigatório")
    @Column(name = "nome", nullable = false)
    private String name;

    @Email(message = "O email informado é inválido")
    @Column(name="email", unique = true, nullable = false)
    @JsonIgnore
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    private UserStatus userStatus = UserStatus.ACTIVATED;

    @JsonBackReference
    @Setter(AccessLevel.PRIVATE)
    @OneToMany(mappedBy = "accountHolder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountBase> accounts = new ArrayList<>();


    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public User() {
    }

}
