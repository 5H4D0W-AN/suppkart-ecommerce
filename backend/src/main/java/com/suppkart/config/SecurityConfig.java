package com.suppkart.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.suppkart.security.jwt.JwtAuthenticationFilter;
import com.suppkart.security.oauth.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    @Qualifier("customUserDetailsService")
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Authentication manager bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * DAO Authentication Provider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * CORS Configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // Allow all origins in development
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Security Filter Chain Configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // H2 Console (development only)
                .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                
                // Public endpoints
                .requestMatchers(AntPathRequestMatcher.antMatcher("/auth/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/auth/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/cart/**")).permitAll() // Allow guest cart access
                .requestMatchers(AntPathRequestMatcher.antMatcher("/products/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/categories/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/sports/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/goals/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/home/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/oauth2/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/login/oauth2/**")).permitAll()
                
                // Public content management endpoints (for frontend usage)
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/blog/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/pages/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/banners/**")).permitAll()
                
                // Health check and documentation
                .requestMatchers(AntPathRequestMatcher.antMatcher("/actuator/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/swagger-ui/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/swagger-ui.html")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/v3/api-docs/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api-docs/**")).permitAll()
                
                // Admin authentication endpoints (public)
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/admin/auth/login")).permitAll()
                
                // Admin endpoints (require ADMIN or SUPER_ADMIN role)
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/admin/auth/**")).hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/admin/dashboard/**")).hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/admin/products/**")).hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/admin/orders/**")).hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/admin/users/**")).hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/admin/categories/**")).hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/admin/reviews/**")).hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/admin/consultations/**")).hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/admin/contacts/**")).hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/admin/referrals/**")).hasAnyRole("ADMIN", "SUPER_ADMIN")
                
                // Admin content management endpoints (require ADMIN or CONTENT_MANAGER role)
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/admin/blog/**")).hasAnyRole("ADMIN", "CONTENT_MANAGER")
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/admin/pages/**")).hasAnyRole("ADMIN", "CONTENT_MANAGER")
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/admin/banners/**")).hasAnyRole("ADMIN", "CONTENT_MANAGER")
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/admin/seo/**")).hasAnyRole("ADMIN", "CONTENT_MANAGER")
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/admin/uploads/**")).hasAnyRole("ADMIN", "CONTENT_MANAGER")
                
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/admin/**")).hasAnyRole("ADMIN", "SUPER_ADMIN")
                
                // Legacy admin endpoints (backward compatibility)
                .requestMatchers(AntPathRequestMatcher.antMatcher("/admin/**")).hasRole("ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher("/users/stats")).hasRole("ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher("/users/{userId}")).hasRole("ADMIN")
                
                // Authenticated endpoints
                .requestMatchers(AntPathRequestMatcher.antMatcher("/users/me")).authenticated()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/orders/**")).authenticated()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/addresses/**")).authenticated()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/consultations/**")).authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    // Custom authentication entry point to handle admin vs regular endpoints
                    String requestURI = request.getRequestURI();
                    if (requestURI.startsWith("/api/admin/")) {
                        // For admin endpoints, return 401 Unauthorized instead of redirecting to OAuth2
                        response.setStatus(401);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Admin authentication required\"}");
                    } else {
                        // For regular endpoints, redirect to OAuth2 login
                        response.sendRedirect("/oauth2/authorization/google");
                    }
                })
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler((request, response, authentication) -> {
                    // Custom success handler
                    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                    String email = oauth2User.getAttribute("email");
                    
                    // Redirect to frontend with success
                    response.sendRedirect("http://localhost:3000/auth/oauth2/redirect?success=true&email=" + email);
                })
                .failureHandler((request, response, exception) -> {
                    // Custom failure handler
                    response.sendRedirect("http://localhost:3000/auth/oauth2/redirect?success=false&error=" + exception.getMessage());
                })
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Allow H2 console frames (development only)
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }
}
