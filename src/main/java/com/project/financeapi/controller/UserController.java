package com.project.financeapi.controller;

import com.project.financeapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuários", description = "Endpoints para sincronização de utilizadores com o Firebase")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/sync")
    @Operation(summary = "Sincroniza um novo utilizador do Firebase",
            description = "Deve ser chamado pelo App logo após o registo no Firebase. Requer o Token do Firebase no header Authorization.")
    public ResponseEntity<Void> syncUser(
            @RequestHeader("Authorization") String authHeader) {

        // Extrai o token removendo o prefixo "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);

        // Chama o serviço para validar o token no Google e criar o utilizador no banco local
        userService.syncUser(token);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}