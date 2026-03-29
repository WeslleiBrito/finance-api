package com.project.financeapi.service;

import com.project.financeapi.entity.User;
import com.project.financeapi.exception.AccessBlockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserContextService {

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new AccessBlockedException("Utilizador não autenticado no contexto.");
        }

        return (User) authentication.getPrincipal();
    }

}
