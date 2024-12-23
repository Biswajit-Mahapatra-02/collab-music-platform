package com.G15.musicplatform.collab_music_platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for development
                .csrf(csrf -> csrf.disable())
                // Allow H2 console in frames
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(auth -> auth
                        // Permit access to H2 console
                        .requestMatchers("/h2-console/**").permitAll()
                        // Permit WebSocket handshake paths ("/ws/**")
                        .requestMatchers("/ws/**").permitAll()
                        // Everything else is permitted as well
                        .anyRequest().permitAll());

        return http.build();
    }
}
