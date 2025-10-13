package com.suppkart.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suppkart.dto.response.AuthResponse;
import com.suppkart.dto.response.UserResponse;
import com.suppkart.model.entity.RefreshToken;
import com.suppkart.model.entity.User;
import com.suppkart.model.entity.UserProfile;
import com.suppkart.service.GoogleAuthService;


// ToDo: Not in use right now, redundant, with redundant functions-- to be removed
@RestController
@RequestMapping("/api/auth/oauth2")
public class GoogleAuthController {

    @Autowired
    private GoogleAuthService googleAuthService;

    /**
     * Google OAuth2 login success callback
     */
    @GetMapping("/login/success")
    public ResponseEntity<?> loginSuccess(@AuthenticationPrincipal OAuth2User principal) {
        try {
            if (principal == null) {
                return ResponseEntity.badRequest().body("OAuth2 authentication failed");
            }

            // Process OAuth2 user
            User user = googleAuthService.processOAuth2User(principal);

            // Generate tokens
            String accessToken = googleAuthService.generateTokenForOAuth2User(user);
            RefreshToken refreshToken = googleAuthService.generateRefreshToken(user);

            // Create response
            UserResponse userResponse = mapToUserResponse(user);
            AuthResponse authResponse = new AuthResponse(accessToken, refreshToken.getToken(), userResponse);

            return ResponseEntity.ok(authResponse);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: OAuth2 login failed - " + e.getMessage());
        }
    }

    /**
     * Google OAuth2 login failure callback
     */
    @GetMapping("/login/failure")
    public ResponseEntity<?> loginFailure() {
        Map<String, String> response = new HashMap<>();
        response.put("error", "OAuth2 authentication failed");
        response.put("message", "Unable to authenticate with Google");
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Get current authenticated OAuth2 user
     */
    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        try {
            if (principal == null) {
                return ResponseEntity.badRequest().body("No authenticated user found");
            }

            // Find user by email
            String email = principal.getAttribute("email");
            User user = googleAuthService.processOAuth2User(principal);
            
            UserResponse userResponse = mapToUserResponse(user);
            return ResponseEntity.ok(userResponse);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: Failed to get user info - " + e.getMessage());
        }
    }

    /**
     * Link Google account to existing user
     */
    @PostMapping("/link")
    public ResponseEntity<?> linkGoogleAccount(@AuthenticationPrincipal OAuth2User principal) {
        try {
            if (principal == null) {
                return ResponseEntity.badRequest().body("OAuth2 authentication required");
            }

            String email = principal.getAttribute("email");
            String googleId = principal.getAttribute("sub");

            User user = googleAuthService.linkGoogleAccount(email, googleId);
            UserResponse userResponse = mapToUserResponse(user);

            return ResponseEntity.ok(userResponse);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: Failed to link Google account - " + e.getMessage());
        }
    }

    /**
     * Unlink Google account
     */
    @PostMapping("/unlink")
    public ResponseEntity<?> unlinkGoogleAccount(@AuthenticationPrincipal OAuth2User principal) {
        try {
            if (principal == null) {
                return ResponseEntity.badRequest().body("OAuth2 authentication required");
            }

            String email = principal.getAttribute("email");
            String googleId = principal.getAttribute("sub");
            
            // Find user and unlink
            User currentUser = googleAuthService.processOAuth2User(principal);
            User user = googleAuthService.unlinkGoogleAccount(currentUser.getUserId());
            
            UserResponse userResponse = mapToUserResponse(user);
            return ResponseEntity.ok(userResponse);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: Failed to unlink Google account - " + e.getMessage());
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
