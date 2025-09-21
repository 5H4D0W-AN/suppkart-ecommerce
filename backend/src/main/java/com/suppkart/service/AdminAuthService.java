package com.suppkart.service;

import com.suppkart.dto.admin.AdminLoginRequest;
import com.suppkart.dto.admin.AdminUserDTO;
import com.suppkart.dto.admin.ChangePasswordRequest;
import com.suppkart.dto.admin.JwtAuthResponse;
import com.suppkart.exception.BusinessException;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.RoleType;
import com.suppkart.model.enums.UserStatus;
import com.suppkart.repository.UserRepository;
import com.suppkart.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminAuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminAuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    /**
     * Authenticate admin and generate JWT token
     */
    public JwtAuthResponse authenticate(AdminLoginRequest loginRequest) {
        logger.info("Admin authentication attempt for email: {}", loginRequest.getEmail());
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );
            
            // Get user details
            User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
            
            // Check if user has admin role
            if (!hasAdminRole(user)) {
                logger.warn("Non-admin user attempted admin login: {}", loginRequest.getEmail());
                throw new BadCredentialsException("Access denied. Admin privileges required.");
            }
            
            // Check if user is active
            if (user.getStatus() != UserStatus.ACTIVE) {
                logger.warn("Inactive admin user attempted login: {}", loginRequest.getEmail());
                throw new BadCredentialsException("Account is not active");
            }
            
            // Generate JWT token
            String token = jwtTokenProvider.generateToken(user);
            long expirationTime = jwtTokenProvider.getExpirationTime();
            
            // Update last login time
            updateAdminLastLogin(user.getEmail());
            
            logger.info("Admin authentication successful for email: {}", loginRequest.getEmail());
            
            return new JwtAuthResponse(token, expirationTime);
            
        } catch (Exception e) {
            logger.error("Admin authentication failed for email: {}", loginRequest.getEmail(), e);
            throw new BadCredentialsException("Invalid credentials");
        }
    }
    
    /**
     * Get current admin profile details
     */
    public AdminUserDTO getAdminProfile() {
        User currentAdmin = getCurrentAdmin();
        return convertToAdminUserDTO(currentAdmin);
    }
    
    /**
     * Update admin password
     */
    public void changePassword(ChangePasswordRequest request) {
        User currentAdmin = getCurrentAdmin();
        
        // Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentAdmin.getPasswordHash())) {
            throw new BusinessException("INVALID_CURRENT_PASSWORD", "Current password is incorrect");
        }
        
        // Validate new password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("PASSWORD_MISMATCH", "New password and confirm password do not match");
        }
        
        // Update password
        currentAdmin.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentAdmin);
        
        logger.info("Password changed successfully for admin: {}", currentAdmin.getEmail());
    }
    
    /**
     * Get currently authenticated admin
     */
    public User getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("ADMIN_NOT_AUTHENTICATED", "No authenticated admin found");
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("ADMIN_USER_NOT_FOUND", "Admin user not found"));
        
        if (!hasAdminRole(user)) {
            throw new BusinessException("INSUFFICIENT_PRIVILEGES", "Current user does not have admin privileges");
        }
        
        return user;
    }
    
    /**
     * Update admin's last login time
     */
    public void updateAdminLastLogin(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("ADMIN_USER_NOT_FOUND", "Admin user not found"));
        
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        logger.debug("Updated last login time for admin: {}", email);
    }
    
    /**
     * Check if user has admin role
     */
    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
            .anyMatch(role -> role.getName() == RoleType.ADMIN || role.getName() == RoleType.SUPER_ADMIN);
    }
    
    /**
     * Convert User entity to AdminUserDTO
     */
    private AdminUserDTO convertToAdminUserDTO(User user) {
        Set<String> roleNames = user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(Collectors.toSet());
        
        return new AdminUserDTO(
            user.getUserId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            roleNames,
            user.getStatus() == UserStatus.ACTIVE,
            user.getLastLoginAt(),
            user.getCreatedAt()
        );
    }
}
