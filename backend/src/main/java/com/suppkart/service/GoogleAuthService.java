package com.suppkart.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suppkart.model.entity.RefreshToken;
import com.suppkart.model.entity.Role;
import com.suppkart.model.entity.User;
import com.suppkart.model.entity.UserProfile;
import com.suppkart.model.enums.AuthProvider;
import com.suppkart.model.enums.RoleType;
import com.suppkart.model.enums.UserStatus;
import com.suppkart.repository.RoleRepository;
import com.suppkart.repository.UserRepository;
import com.suppkart.security.JwtTokenProvider;

@Service
@Transactional
public class GoogleAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    @Lazy
    private AuthenticationService authenticationService;

    @Autowired
    private CartService cartService;

    /**
     * Process Google OAuth2 user
     */
    public User processOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        String picture = oAuth2User.getAttribute("picture");

        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update user if it's a Google user
            if (user.getAuthProvider() == AuthProvider.GOOGLE) {
                updateExistingGoogleUser(user, oAuth2User);
            }
            return user;
        }

        // Create new user
        return createNewGoogleUser(email, firstName, lastName, googleId, picture);
    }

    /**
     * Create new Google user
     */
    private User createNewGoogleUser(String email, String firstName, String lastName, String googleId, String picture) {
        // Get default role
        Role userRole = roleRepository.findByName(RoleType.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        // Create user
        User user = new User();
        user.setEmail(email);
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setAuthProviderId(googleId);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true); // Google emails are verified
        user.setRoles(Set.of(userRole));
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Create user profile
        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        profile.setFirstName(firstName != null ? firstName : "");
        profile.setLastName(lastName != null ? lastName : "");
        profile.setProfileImageUrl(picture);
        profile.setCreatedAt(java.time.LocalDateTime.now());
        profile.setUpdatedAt(java.time.LocalDateTime.now());

        savedUser.setUserProfile(profile);
        userRepository.save(savedUser);

        // Create empty cart for the user
        cartService.createCartForUser(savedUser);

        return savedUser;
    }

    /**
     * Update existing Google user
     */
    private void updateExistingGoogleUser(User user, OAuth2User oAuth2User) {
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        String picture = oAuth2User.getAttribute("picture");

        UserProfile profile = user.getUserProfile();
        if (profile != null) {
            if (firstName != null) profile.setFirstName(firstName);
            if (lastName != null) profile.setLastName(lastName);
            if (picture != null) profile.setProfileImageUrl(picture);
            profile.setUpdatedAt(java.time.LocalDateTime.now());
        }

        user.setLastLoginAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Generate JWT token for OAuth2 user
     */
    public String generateTokenForOAuth2User(User user) {
        return jwtTokenProvider.generateTokenFromUser(user);
    }

    /**
     * Generate refresh token for OAuth2 user
     */
    public RefreshToken generateRefreshToken(User user) {
        return authenticationService.generateRefreshToken(user);
    }

    /**
     * Find or create user by Google ID
     */
    public Optional<User> findByGoogleId(String googleId) {
        return userRepository.findByAuthProviderIdAndAuthProvider(googleId, AuthProvider.GOOGLE);
    }

    /**
     * Link Google account to existing user
     */
    public User linkGoogleAccount(String userEmail, String googleId) {
        Optional<User> existingUser = userRepository.findByEmail(userEmail);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setAuthProvider(AuthProvider.GOOGLE);
            user.setAuthProviderId(googleId);
            user.setEmailVerified(true);
            user.setUpdatedAt(java.time.LocalDateTime.now());
            return userRepository.save(user);
        }
        
        throw new RuntimeException("User not found with email: " + userEmail);
    }

    /**
     * Unlink Google account from user
     */
    public User unlinkGoogleAccount(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getAuthProvider() == AuthProvider.GOOGLE) {
                user.setAuthProvider(AuthProvider.EMAIL);
                user.setAuthProviderId(null);
                user.setUpdatedAt(java.time.LocalDateTime.now());
                return userRepository.save(user);
            }
        }
        
        throw new RuntimeException("User not found or not linked to Google");
    }
}
