package com.suppkart.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

import com.suppkart.security.JwtTokenProvider;

/**
 * Unified test configuration that supports different security scenarios
 */
@TestConfiguration
@EnableWebSecurity
public class UnifiedTestConfig {

    /**
     * Security configuration for admin tests that require authentication and authorization
     */
    @Bean
    @Profile("!permissive-security")
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "CONTENT_MANAGER")
                .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {
                });
        return http.build();
    }

    /**
     * Permissive security configuration for tests that don't need authentication
     */
    @Bean
    @Profile("permissive-security")
    public SecurityFilterChain permissiveSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public FileUploadConfig fileUploadConfig() {
        FileUploadConfig config = new FileUploadConfig();
        config.setUploadDir("test-uploads");
        config.setBaseUrl("http://localhost:8080");
        config.setMaxFileSize(5 * 1024 * 1024); // 5MB
        config.setMaxRequestSize(50 * 1024 * 1024); // 50MB
        return config;
    }

    @Bean
    @Primary
    public JwtTokenProvider jwtTokenProvider() {
        return Mockito.mock(JwtTokenProvider.class);
    }

    @Bean("customUserDetailsService")
    @Primary
    public UserDetailsService customUserDetailsService() {
        return Mockito.mock(UserDetailsService.class);
    }
}
