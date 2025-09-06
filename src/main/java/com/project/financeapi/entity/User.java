package com.project.financeapi.entity;

import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter @ToString
public class User {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.PRIVATE)
    private String id;

    @NotBlank(message = "O nome é obrigatório")
    @Column(name = "nome", nullable = false)
    private String name;

    @Email(message = "O email informado é inválido")
    @Column(name="email", unique = true, nullable = false)
    @Setter(AccessLevel.PRIVATE)
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 4, message = "A senha deve ter pelo menos 4 caracteres")
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "token_version")
    private Integer tokenVersion = 0;

    @Column(name = "user_status")
    private UserStatus userStatus = UserStatus.ACTIVATED;

    @Setter(AccessLevel.PRIVATE)
    @OneToMany(mappedBy = "accountHolder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountBase> accounts = new ArrayList<>();


    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
