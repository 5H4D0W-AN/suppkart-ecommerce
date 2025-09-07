package com.suppkart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.User;
import com.suppkart.model.enums.AuthProvider;
import com.suppkart.model.enums.UserStatus;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by email address
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by email and auth provider
     */
    Optional<User> findByEmailAndAuthProvider(String email, AuthProvider authProvider);
    
    /**
     * Find user by auth provider ID (for social login)
     */
    Optional<User> findByAuthProviderIdAndAuthProvider(String authProviderId, AuthProvider authProvider);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find users by status
     */
    @Query("SELECT u FROM User u WHERE u.status = :status")
    java.util.List<User> findByStatus(@Param("status") UserStatus status);
    
    /**
     * Find active users
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE'")
    java.util.List<User> findActiveUsers();
    
    /**
     * Count users by auth provider
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.authProvider = :provider")
    long countByAuthProvider(@Param("provider") AuthProvider provider);
    
    /**
     * Find users created in the last N days
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= CURRENT_DATE - :days")
    java.util.List<User> findUsersCreatedInLastDays(@Param("days") int days);
    
    /**
     * Update user last login timestamp
     */
    @Query("UPDATE User u SET u.lastLoginAt = CURRENT_TIMESTAMP WHERE u.userId = :userId")
    void updateLastLoginAt(@Param("userId") Long userId);
    
    /**
     * Find users by status with pagination
     */
    @Query("SELECT u FROM User u WHERE u.status = :status")
    org.springframework.data.domain.Page<User> findByStatusPaged(@Param("status") UserStatus status, org.springframework.data.domain.Pageable pageable);
    
    /**
     * Count users by status
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") UserStatus status);
    
    /**
     * Count users created after a specific date
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt > :dateTime")
    long countUsersCreatedAfter(@Param("dateTime") java.time.LocalDateTime dateTime);
    
    /**
     * Search users by email or name
     */
    @Query("SELECT u FROM User u JOIN u.userProfile up WHERE " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(up.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(up.lastName) LIKE LOWER(CONCAT('%', :search, '%'))")
    java.util.List<User> findByEmailContainingOrUserProfile_FirstNameContainingOrUserProfile_LastNameContaining(
        @Param("search") String search, 
        @Param("search") String search2, 
        @Param("search") String search3);
}
