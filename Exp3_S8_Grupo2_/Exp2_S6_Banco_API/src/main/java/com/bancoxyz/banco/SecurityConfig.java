
package com.bancoxyz.banco;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String JWK_SET_URI =
            "http://banco-xyz-keycloak:8080/realms/banco-xyz/protocol/openid-connect/certs";

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(JWK_SET_URI).build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/banco/health").permitAll()
                        .requestMatchers("/api/banco/config").authenticated()
                        .requestMatchers("/api/banco/seguro").authenticated()
                        .requestMatchers("/api/banco/resiliencia").authenticated()
                        .requestMatchers("/api/banco/transferencias").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                );

        return http.build();
    }
}

