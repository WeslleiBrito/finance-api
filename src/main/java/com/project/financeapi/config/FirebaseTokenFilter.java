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
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return request.getMethod().equalsIgnoreCase("OPTIONS") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        System.out.println("\n>>> 1. REQUISIÇÃO RECEBIDA NA ROTA: " + request.getRequestURI());

        String header = request.getHeader(AUTH_HEADER);

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            System.out.println(">>> 2. BLOQUEIO: Cabeçalho 'Authorization' ausente ou não começa com 'Bearer '.");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String uid = decodedToken.getUid();
            System.out.println(">>> 2. SUCESSO: Token válido no Firebase! UID: " + uid);

            Optional<User> userOptional = userRepository.findById(uid);

            if (userOptional.isEmpty()) {
                if (request.getRequestURI().equals(USER_SYNC_ENDPOINT)) {
                    System.out.println(">>> 3. INFO: Usuário novo detectado. Liberando rota de sincronização...");
                    filterChain.doFilter(request, response);
                    return;
                }

                System.out.println(">>> 3. BLOQUEIO: Usuário não existe no banco de dados local (PostgreSQL).");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuário não cadastrado");
                return;
            }

            User user = userOptional.get();

            if (!user.getUserStatus().name().equals("ACTIVATED")) {
                System.out.println(">>> 3. BLOQUEIO: Usuário está inativo no banco de dados local.");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuário inativo");
                return;
            }

            System.out.println(">>> 3. SUCESSO: Usuário encontrado e ativado! Liberando acesso ao Controller...");
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            System.out.println(">>> 2. ERRO FATAL DO FIREBASE: Falha ao validar o token. Motivo: " + e.getMessage());
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido ou expirado");
            return;
        }

        filterChain.doFilter(request, response);
    }

}