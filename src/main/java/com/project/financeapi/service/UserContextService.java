package com.project.financeapi.service;

import com.google.firebase.auth.FirebaseToken;
import com.project.financeapi.entity.User;
import com.project.financeapi.exception.AccessBlockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserContextService {

    /**
     * Retorna a entidade relacional do usuário para vincular às tabelas do banco.
     */
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new AccessBlockedException("Utilizador não autenticado no contexto.");
        }

        return (User) authentication.getPrincipal();
    }

    /**
     * Busca o nome do usuário em tempo real diretamente do Token Firebase.
     */
    public String getUserName() {
        FirebaseToken token = getFirebaseToken();
        return (token != null && token.getName() != null) ? token.getName() : "Usuário";
    }

    /**
     * Busca o e-mail do usuário em tempo real diretamente do Token Firebase.
     */
    public String getUserEmail() {
        FirebaseToken token = getFirebaseToken();
        return (token != null && token.getEmail() != null) ? token.getEmail() : "";
    }

    private FirebaseToken getFirebaseToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Lembre-se que configuramos o FirebaseTokenFilter para salvar o token aqui!
        if (authentication != null && authentication.getCredentials() instanceof FirebaseToken) {
            return (FirebaseToken) authentication.getCredentials();
        }
        return null;
    }
}