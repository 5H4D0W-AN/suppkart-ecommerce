package com.suppkart.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suppkart.model.entity.RefreshToken;
import com.suppkart.model.entity.Role;
import com.suppkart.model.entity.User;
import com.suppkart.model.entity.UserProfile;
import com.suppkart.model.enums.AuthProvider;
import com.suppkart.model.enums.RoleType;
import com.suppkart.model.enums.UserStatus;
import com.suppkart.repository.RefreshTokenRepository;
import com.suppkart.repository.RoleRepository;
import com.suppkart.repository.UserRepository;
import com.suppkart.security.JwtTokenProvider;

@Service
@Transactional
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    /**
     * Register a new user
     */
    public User registerUser(String email, String password, String firstName, String lastName, String phone) {
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        // Get default role
        Role userRole = roleRepository.findByName(RoleType.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        // Create user
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setAuthProvider(AuthProvider.EMAIL);
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(userRole));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Create user profile
        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setPhone(phone);
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        userService.saveUserProfile(profile);

        // Create empty cart for the user
        cartService.createCartForUser(savedUser);

        return savedUser;
    }

    /**
     * Authenticate user with email and password
     */
    public Authentication authenticateUser(String email, String password) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
    }

    /**
     * Generate JWT token for user
     */
    public String generateAccessToken(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return jwtTokenProvider.generateTokenFromUser(user);
    }

    /**
     * Generate refresh token for user
     */
    public RefreshToken generateRefreshToken(User user) {
        // Delete existing refresh token
        refreshTokenRepository.deleteByUser(user);

        // Create new refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(jwtTokenProvider.generateRefreshToken());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7)); // 7 days expiry
        refreshToken.setCreatedAt(LocalDateTime.now());

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Refresh access token using refresh token
     */
    public String refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        return jwtTokenProvider.generateTokenFromUser(refreshToken.getUser());
    }

    /**
     * Logout user by deleting refresh token
     */
    public void logout(String refreshTokenValue) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(refreshTokenValue);
        refreshToken.ifPresent(refreshTokenRepository::delete);
    }

    /**
     * Logout user by user ID
     */
    public void logoutUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        refreshTokenRepository.deleteByUser(user);
    }

    /**
     * Change user password
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Logout user from all devices
        refreshTokenRepository.deleteByUser(user);
    }

    /**
     * Reset password (without current password verification)
     */
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Logout user from all devices
        refreshTokenRepository.deleteByUser(user);
    }

    /**
     * Verify if refresh token is valid
     */
    public boolean isRefreshTokenValid(String tokenValue) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(tokenValue);
        return refreshToken.isPresent() && 
               refreshToken.get().getExpiresAt().isAfter(LocalDateTime.now());
    }

    /**
     * Get user by refresh token
     */
    public User getUserByRefreshToken(String tokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        return refreshToken.getUser();
    }

    /**
     * Clean up expired refresh tokens
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}
