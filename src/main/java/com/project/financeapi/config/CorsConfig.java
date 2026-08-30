package com.project.financeapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 🌟 Permitir credenciais é obrigatório se o frontend enviar headers Authorization
        config.setAllowCredentials(true);

        // 🌟 Lista de domínios permitidos (Não use '*' se 'allowCredentials' for true)
        config.setAllowedOrigins(List.of(
                "http://localhost:8081",
                "http://localhost:8080",
                "http://localhost:5173",
                "http://localhost:3000",
                "http://192.168.56.1:8081",
                "http://192.168.0.5:8081",
                "http://172.30.144.1:8081",
                "http://172.21.32.1:8081"
        ));

        // 🌟 Permitir todos os métodos (inclusive OPTIONS) e cabeçalhos
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 🌟 Expõe os cabeçalhos para o frontend, permitindo que ele leia a resposta real
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));

        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}