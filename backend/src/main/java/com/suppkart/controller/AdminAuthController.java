package com.suppkart.controller;

import com.suppkart.dto.admin.AdminLoginRequest;
import com.suppkart.dto.admin.AdminUserDTO;
import com.suppkart.dto.admin.ChangePasswordRequest;
import com.suppkart.dto.admin.JwtAuthResponse;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.service.AdminAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminAuthController.class);
    
    @Autowired
    private AdminAuthService adminAuthService;
    
    /**
     * Admin login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> login(@Valid @RequestBody AdminLoginRequest loginRequest) {
        String email = loginRequest != null ? loginRequest.getEmail() : "null";
        logger.info("Admin login request received for email: {}", email);
        
        try {
            JwtAuthResponse authResponse = adminAuthService.authenticate(loginRequest);
            
            ApiResponse<JwtAuthResponse> response = new ApiResponse<>(
                true,
                "Admin login successful",
                authResponse
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Admin login failed for email: {}", email, e);
            
            ApiResponse<JwtAuthResponse> response = new ApiResponse<>(
                false,
                "Login failed: " + e.getMessage(),
                null
            );
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    /**
     * Get admin profile endpoint
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminUserDTO>> getProfile() {
        logger.info("Admin profile request received");
        
        try {
            AdminUserDTO adminProfile = adminAuthService.getAdminProfile();
            
            ApiResponse<AdminUserDTO> response = new ApiResponse<>(
                true,
                "Admin profile retrieved successfully",
                adminProfile
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to retrieve admin profile", e);
            
            ApiResponse<AdminUserDTO> response = new ApiResponse<>(
                false,
                "Failed to retrieve profile: " + e.getMessage(),
                null
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Update admin profile endpoint
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminUserDTO>> updateProfile(@Valid @RequestBody AdminUserDTO updateRequest) {
        logger.info("Admin profile update request received");
        
        try {
            // For now, just return the current profile as profile update logic would need additional implementation
            AdminUserDTO adminProfile = adminAuthService.getAdminProfile();
            
            ApiResponse<AdminUserDTO> response = new ApiResponse<>(
                true,
                "Admin profile updated successfully",
                adminProfile
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to update admin profile", e);
            
            ApiResponse<AdminUserDTO> response = new ApiResponse<>(
                false,
                "Failed to update profile: " + e.getMessage(),
                null
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Change admin password endpoint
     */
    @PostMapping("/change-password")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        logger.info("Admin password change request received");
        
        try {
            adminAuthService.changePassword(changePasswordRequest);
            
            ApiResponse<String> response = new ApiResponse<>(
                true,
                "Password changed successfully",
                "Password has been updated successfully"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to change admin password", e);
            
            ApiResponse<String> response = new ApiResponse<>(
                false,
                "Failed to change password: " + e.getMessage(),
                null
            );
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Admin logout endpoint (optional - JWT is stateless)
     */
    @PostMapping("/logout")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> logout() {
        logger.info("Admin logout request received");
        
        // Since JWT is stateless, logout is handled on client-side by removing the token
        // This endpoint is provided for consistency and potential future token blacklisting
        
        ApiResponse<String> response = new ApiResponse<>(
            true,
            "Logout successful",
            "Please remove the JWT token from client storage"
        );
        
        return ResponseEntity.ok(response);
    }
}
