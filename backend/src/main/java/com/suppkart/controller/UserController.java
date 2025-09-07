package com.suppkart.controller;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suppkart.dto.request.UpdateProfileRequest;
import com.suppkart.dto.response.UserResponse;
import com.suppkart.model.entity.User;
import com.suppkart.model.entity.UserProfile;
import com.suppkart.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Get current user profile
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            UserResponse userResponse = mapToUserResponse(user);
            return ResponseEntity.ok(userResponse);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: Failed to get user profile - " + e.getMessage());
        }
    }

    /**
     * Update current user profile
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateCurrentUser(@Valid @RequestBody UpdateProfileRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            User currentUser = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            User updatedUser = userService.updateUserProfile(
                    currentUser.getUserId(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPhone()
            );
            
            UserResponse userResponse = mapToUserResponse(updatedUser);
            return ResponseEntity.ok(userResponse);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: Failed to update profile - " + e.getMessage());
        }
    }

    /**
     * Get user profile by ID (Admin only)
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            UserResponse userResponse = mapToUserResponse(user);
            return ResponseEntity.ok(userResponse);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: User not found - " + e.getMessage());
        }
    }

    /**
     * Update user profile by ID (Admin only)
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserById(@PathVariable Long userId, 
                                          @Valid @RequestBody UpdateProfileRequest request) {
        try {
            User updatedUser = userService.updateUserProfile(
                    userId,
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPhone()
            );
            
            UserResponse userResponse = mapToUserResponse(updatedUser);
            return ResponseEntity.ok(userResponse);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: Failed to update user - " + e.getMessage());
        }
    }

    /**
     * Delete user account (soft delete by setting status to DELETED)
     */
    @DeleteMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            User currentUser = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Instead of actual deletion, we'll deactivate the user
            userService.deactivateUser(currentUser.getUserId());
            
            return ResponseEntity.ok("Account deleted successfully");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: Failed to delete account - " + e.getMessage());
        }
    }

    /**
     * Delete user by ID (Admin only)
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUserById(@PathVariable Long userId) {
        try {
            userService.deactivateUser(userId);
            return ResponseEntity.ok("User deleted successfully");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: Failed to delete user - " + e.getMessage());
        }
    }

    /**
     * Get user statistics (Admin only)
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserStats() {
        try {
            UserService.UserStats stats = userService.getUserStats();
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: Failed to get user statistics - " + e.getMessage());
        }
    }

    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setUserId(user.getUserId());
        userResponse.setEmail(user.getEmail());
        
        UserProfile profile = user.getUserProfile();
        if (profile != null) {
            userResponse.setFirstName(profile.getFirstName());
            userResponse.setLastName(profile.getLastName());
            userResponse.setPhone(profile.getPhone());
        } else {
            userResponse.setFirstName(user.getFirstName());
            userResponse.setLastName(user.getLastName());
            userResponse.setPhone(user.getPhone());
        }
        
        userResponse.setAuthProvider(user.getAuthProvider());
        userResponse.setStatus(user.getStatus());
        userResponse.setEmailVerified(user.getEmailVerified());
        userResponse.setLastLoginAt(user.getLastLoginAt());
        userResponse.setCreatedAt(user.getCreatedAt());
        
        // Map roles
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        userResponse.setRoles(roles);
        
        return userResponse;
    }
}
