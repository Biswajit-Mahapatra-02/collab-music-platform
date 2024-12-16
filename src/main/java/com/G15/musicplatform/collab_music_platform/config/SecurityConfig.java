package com.G15.musicplatform.collab_music_platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())    // Disable CSRF for development
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)) // Allow H2 console in frames
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/h2-console/**").permitAll() // Allow H2 console access
                                .anyRequest().permitAll()  // Allow all requests
                );
        return http.build();
    }
}
