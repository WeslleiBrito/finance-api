package com.project.financeapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder @ToString
public class User {

    @Id
    @Column(name = "id")
    private String id;

    @NotBlank(message = "O nome é obrigatório")
    @Column(name = "nome", nullable = false)
    private String name;

    @Email(message = "O email informado é inválido")
    @Column(name="email", unique = true, nullable = false)
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 4, message = "A senha deve ter pelo menos 4 caracteres")
    @Column(name = "password", nullable = false)
    private String password; // store hashed

    @Column(name = "token_version")
    private Integer tokenVersion = 0;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts;

}
