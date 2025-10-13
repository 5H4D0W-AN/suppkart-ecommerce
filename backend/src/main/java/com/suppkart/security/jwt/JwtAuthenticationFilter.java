package com.suppkart.security.jwt;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.suppkart.security.JwtTokenProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    @Qualifier("customUserDetailsService")
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                String userEmail = jwtTokenProvider.extractUsername(jwt);
                
                if (userEmail != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                    
                    if (jwtTokenProvider.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.debug("JWT authentication successful for user: " + userEmail + " accessing: " + requestURI);
                    } else {
                        logger.warn("Invalid JWT token for user: " + userEmail + " accessing: " + requestURI);
                    }
                } else {
                    logger.warn("Unable to extract username from JWT token for request: " + requestURI);
                }
            } else if (requiresAuthentication(requestURI)) {
                logger.debug("No JWT token found for protected endpoint: " + requestURI);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context for request: " + requestURI, ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Check if the request URI requires authentication
     */
    private boolean requiresAuthentication(String requestURI) {
        return !requestURI.startsWith("/auth/") && 
               !requestURI.startsWith("/api/auth/") &&
               !requestURI.startsWith("/api/admin/auth/login") &&
               !requestURI.startsWith("/products/") && 
               !requestURI.startsWith("/categories/") &&
               !requestURI.startsWith("/h2-console/") &&
               !requestURI.startsWith("/swagger-ui/") &&
               !requestURI.startsWith("/v3/api-docs/");
    }

    /**
     * Extract JWT token from request header
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
