package com.suppkart.controller;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suppkart.dto.request.LoginRequest;
import com.suppkart.dto.request.RefreshTokenRequest;
import com.suppkart.dto.request.RegisterRequest;
import com.suppkart.dto.response.AuthResponse;
import com.suppkart.dto.response.UserResponse;
import com.suppkart.model.entity.RefreshToken;
import com.suppkart.model.entity.User;
import com.suppkart.model.entity.UserProfile;
import com.suppkart.service.AuthenticationService;
import com.suppkart.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Check if user already exists
            if (userService.emailExists(registerRequest.getEmail())) {
                return ResponseEntity.badRequest()
                        .body("Error: Email is already taken!");
            }

            // Create new user
            User user = authenticationService.registerUser(
                    registerRequest.getEmail(),
                    registerRequest.getPassword(),
                    registerRequest.getFirstName(),
                    registerRequest.getLastName(),
                    registerRequest.getPhone(),
                    registerRequest.getReferralCode()
            );

            // Generate tokens
            Authentication authentication = authenticationService.authenticateUser(
                    registerRequest.getEmail(),
                    registerRequest.getPassword()
            );
            String accessToken = authenticationService.generateAccessToken(authentication);
            RefreshToken refreshToken = authenticationService.generateRefreshToken(user);

            // Create response
            UserResponse userResponse = mapToUserResponse(user);
            AuthResponse authResponse = new AuthResponse(accessToken, refreshToken.getToken(), userResponse);

            return ResponseEntity.ok(authResponse);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: Registration failed - " + e.getMessage());
        }
    }

    /**
     * Authenticate user
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user
            Authentication authentication = authenticationService.authenticateUser(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );

            // Get user
            User user = userService.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate tokens
            String accessToken = authenticationService.generateAccessToken(authentication);
            RefreshToken refreshToken = authenticationService.generateRefreshToken(user);

            // Update last login time without triggering merge
            userService.updateLastLoginTime(user.getUserId());

            // Create response
            UserResponse userResponse = mapToUserResponse(user);
            AuthResponse authResponse = new AuthResponse(accessToken, refreshToken.getToken(), userResponse);

            return ResponseEntity.ok(authResponse);

        } catch (Exception e) {
            e.printStackTrace(); // Add this for debugging
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Error: Invalid credentials - " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * Refresh access token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String refreshTokenValue = request.getRefreshToken();

            // Validate refresh token
            if (!authenticationService.isRefreshTokenValid(refreshTokenValue)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Error: Invalid or expired refresh token");
            }

            // Generate new access token
            String newAccessToken = authenticationService.refreshAccessToken(refreshTokenValue);

            // Get user for response
            User user = authenticationService.getUserByRefreshToken(refreshTokenValue);
            UserResponse userResponse = mapToUserResponse(user);

            AuthResponse authResponse = new AuthResponse(newAccessToken, refreshTokenValue, userResponse);

            return ResponseEntity.ok(authResponse);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Error: Token refresh failed - " + e.getMessage());
        }
    }

    /**
     * Logout user
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            authenticationService.logout(request.getRefreshToken());
            return ResponseEntity.ok("User logged out successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: Logout failed - " + e.getMessage());
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
        Set<String> roles = user.getRoles() != null
                ? user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet())
                : Set.of();
        userResponse.setRoles(roles);

        return userResponse;
    }
}
