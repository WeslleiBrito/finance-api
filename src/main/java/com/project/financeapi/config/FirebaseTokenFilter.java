package com.project.financeapi.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.project.financeapi.entity.User;
import com.project.financeapi.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_SYNC_ENDPOINT = "/api/users/sync";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader(AUTH_HEADER);

        // 🔹 Se não tem token → deixa o Spring decidir (vai bloquear depois)
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String uid = decodedToken.getUid();

            Optional<User> userOptional = userRepository.findById(uid);

            // 🔴 Usuário não existe no banco
            if (userOptional.isEmpty()) {

                // Permite apenas endpoint de cadastro
                if (request.getRequestURI().equals(USER_SYNC_ENDPOINT)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuário não cadastrado");
                return;
            }

            User user = userOptional.get();

            // 🔴 Usuário inativo
            if (!user.getUserStatus().name().equals("ACTIVATED")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuário inativo");
                return;
            }

            // ✅ Usuário válido → autentica
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            Collections.emptyList()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido ou expirado");
            return;
        }

        filterChain.doFilter(request, response);
    }
}