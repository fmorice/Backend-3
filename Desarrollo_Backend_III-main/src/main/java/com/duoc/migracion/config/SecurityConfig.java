package com.duoc.migracion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/bff/web/**").hasRole("WEB")
                        .requestMatchers("/api/bff/mobile/**").hasRole("MOBILE")
                        .requestMatchers("/api/bff/cajero/**").hasRole("CAJERO")
                        .requestMatchers("/api/internal/**").authenticated()
                        .anyRequest().permitAll()
                )
                .httpBasic(withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService users() {
        UserDetails web = User.withUsername("webUser").password("webPass").roles("WEB").build();
        UserDetails mobile = User.withUsername("mobileUser").password("mobilePass").roles("MOBILE").build();
        UserDetails cajero = User.withUsername("cajeroUser").password("cajeroPass").roles("CAJERO").build();
        return new InMemoryUserDetailsManager(web, mobile, cajero);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
