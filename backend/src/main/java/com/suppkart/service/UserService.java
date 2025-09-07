package com.suppkart.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suppkart.model.entity.User;
import com.suppkart.model.entity.UserProfile;
import com.suppkart.model.enums.AuthProvider;
import com.suppkart.model.enums.UserStatus;
import com.suppkart.repository.UserProfileRepository;
import com.suppkart.repository.UserRepository;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Get user by ID
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Create new user with email/password
     */
    public User createUser(String email, String password, String firstName, String lastName, String phone) {
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User with email " + email + " already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setAuthProvider(AuthProvider.EMAIL);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Create user profile
        UserProfile profile = new UserProfile();
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setPhone(phone);
        profile.setUser(user);
        user.setUserProfile(profile);

        return userRepository.save(user);
    }

    /**
     * Create user with social login
     */
    public User createSocialUser(String email, String firstName, String lastName, AuthProvider provider, String providerId) {
        User user = new User();
        user.setEmail(email);
        user.setAuthProvider(provider);
        user.setAuthProviderId(providerId);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Create user profile
        UserProfile profile = new UserProfile();
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setUser(user);
        user.setUserProfile(profile);

        return userRepository.save(user);
    }

    /**
     * Update user profile
     */
    public User updateUserProfile(Long userId, String firstName, String lastName, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
            user.setUserProfile(profile);
        }

        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setPhone(phone);
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Update user password
     */
    public void updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Save user profile
     */
    public UserProfile saveUserProfile(UserProfile userProfile) {
        userProfile.setUpdatedAt(LocalDateTime.now());
        return userProfileRepository.save(userProfile);
    }

    /**
     * Deactivate user
     */
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(UserStatus.INACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Activate user
     */
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Get all users with pagination
     */
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Get active users with pagination
     */
    @Transactional(readOnly = true)
    public Page<User> getActiveUsers(Pageable pageable) {
        return userRepository.findByStatusPaged(UserStatus.ACTIVE, pageable);
    }

    /**
     * Search users by name or email
     */
    @Transactional(readOnly = true)
    public List<User> searchUsers(String searchTerm) {
        return userRepository.findByEmailContainingOrUserProfile_FirstNameContainingOrUserProfile_LastNameContaining(
                searchTerm, searchTerm, searchTerm);
    }

    /**
     * Check if email exists
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Get user statistics
     */
    @Transactional(readOnly = true)
    public UserStats getUserStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long newUsersThisMonth = userRepository.countUsersCreatedAfter(LocalDateTime.now().minusMonths(1));

        return new UserStats(totalUsers, activeUsers, newUsersThisMonth);
    }

    /**
     * User statistics DTO
     */
    public static class UserStats {
        private long totalUsers;
        private long activeUsers;
        private long newUsersThisMonth;

        public UserStats(long totalUsers, long activeUsers, long newUsersThisMonth) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.newUsersThisMonth = newUsersThisMonth;
        }

        // Getters
        public long getTotalUsers() { return totalUsers; }
        public long getActiveUsers() { return activeUsers; }
        public long getNewUsersThisMonth() { return newUsersThisMonth; }
    }
}
