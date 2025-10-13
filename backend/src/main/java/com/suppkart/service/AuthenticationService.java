package com.suppkart.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

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

    @Autowired
    private ReferralService referralService;

    /**
     * Register a new user
     */
    public User registerUser(String email, String password, String firstName, String lastName, String phone) {
        return registerUser(email, password, firstName, lastName, phone, null);
    }

    /**
     * Register a new user with referral code
     */
    public User registerUser(String email, String password, String firstName, String lastName, String phone, String referralCode) {
        logger.info("User registration attempt for email: {}", email);

        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            logger.warn("Registration failed - email already exists: {}", email);
            throw new RuntimeException("Email already registered");
        }

        // Get default role
        Role userRole = roleRepository.findByName(RoleType.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        // Create user
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
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

        // Process referral code if provided
        if (referralCode != null && !referralCode.trim().isEmpty()) {
            try {
                referralService.processNewUserRegistration(savedUser, referralCode.trim());
                logger.info("Referral code processed successfully for user: {}", email);
            } catch (Exception e) {
                // Log error but don't fail registration
                // The user is already created, so we shouldn't rollback
                logger.error("Failed to process referral code during registration for user: {}", email, e);
            }
        }

        logger.info("User registration successful for email: {}", email);
        return savedUser;
    }

    /**
     * Authenticate user with email and password
     */
    public Authentication authenticateUser(String email, String password) {
        logger.info("Authentication attempt for user: {}", email);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            logger.info("Authentication successful for user: {}", email);
            return authentication;
        } catch (AuthenticationException e) {
            logger.warn("Authentication failed for user: {} - {}", email, e.getMessage());
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    /**
     * Generate JWT token for user
     */
    public String generateAccessToken(Authentication authentication) {
        String email = authentication.getName();
        logger.debug("Generating access token for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found when generating token: {}", email);
                    return new RuntimeException("User not found");
                });

        String token = jwtTokenProvider.generateTokenFromUser(user);
        logger.debug("Access token generated successfully for user: {}", email);
        return token;
    }

    /**
     * Generate refresh token for user
     */
    public RefreshToken generateRefreshToken(User user) {
        logger.debug("Generating refresh token for user: {}", user.getEmail());

        // Delete existing refresh token
        refreshTokenRepository.deleteByUser(user);

        // Create new refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(jwtTokenProvider.generateRefreshToken());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7)); // 7 days expiry
        refreshToken.setCreatedAt(LocalDateTime.now());

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        logger.debug("Refresh token generated successfully for user: {}", user.getEmail());
        return savedToken;
    }

    /**
     * Refresh access token using refresh token
     */
    public String refreshAccessToken(String refreshTokenValue) {
        logger.debug("Attempting to refresh access token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> {
                    logger.warn("Invalid refresh token provided");
                    return new RuntimeException("Invalid refresh token");
                });

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            logger.warn("Expired refresh token used for user: {}", refreshToken.getUser().getEmail());
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        String newAccessToken = jwtTokenProvider.generateTokenFromUser(refreshToken.getUser());
        logger.debug("Access token refreshed successfully for user: {}", refreshToken.getUser().getEmail());
        return newAccessToken;
    }

    /**
     * Logout user by deleting refresh token
     */
    public void logout(String refreshTokenValue) {
        logger.info("User logout attempt");

        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(refreshTokenValue);
        if (refreshToken.isPresent()) {
            String userEmail = refreshToken.get().getUser().getEmail();
            refreshTokenRepository.delete(refreshToken.get());
            logger.info("User logged out successfully: {}", userEmail);
        } else {
            logger.warn("Logout attempted with invalid refresh token");
        }
    }

    /**
     * Logout user by user ID
     */
    public void logoutUser(Long userId) {
        logger.info("Logout user by ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found for logout: {}", userId);
                    return new RuntimeException("User not found");
                });

        refreshTokenRepository.deleteByUser(user);
        logger.info("User logged out from all devices: {}", user.getEmail());
    }

    /**
     * Change user password
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        logger.info("Password change attempt for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found for password change: {}", userId);
                    return new RuntimeException("User not found");
                });

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            logger.warn("Incorrect current password provided for user: {}", user.getEmail());
            throw new RuntimeException("Current password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Logout user from all devices
        refreshTokenRepository.deleteByUser(user);

        logger.info("Password changed successfully for user: {}", user.getEmail());
    }

    /**
     * Reset password (without current password verification)
     */
    public void resetPassword(String email, String newPassword) {
        logger.info("Password reset attempt for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found for password reset: {}", email);
                    return new RuntimeException("User not found");
                });

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Logout user from all devices
        refreshTokenRepository.deleteByUser(user);

        logger.info("Password reset successful for user: {}", email);
    }

    /**
     * Verify if refresh token is valid
     */
    public boolean isRefreshTokenValid(String tokenValue) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(tokenValue);
        return refreshToken.isPresent()
                && refreshToken.get().getExpiresAt().isAfter(LocalDateTime.now());
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
        logger.debug("Starting cleanup of expired refresh tokens");
        refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
        logger.info("Cleaned up expired refresh tokens");
    }
}
