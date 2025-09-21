package com.suppkart.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.User;
import com.suppkart.model.enums.AuthProvider;
import com.suppkart.model.enums.UserStatus;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    
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
    List<User> findByStatus(@Param("status") UserStatus status);
    
    /**
     * Find active users
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE'")
    List<User> findActiveUsers();
    
    /**
     * Count users by auth provider
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.authProvider = :provider")
    long countByAuthProvider(@Param("provider") AuthProvider provider);
    
    /**
     * Find users created in the last N days
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= DATEADD(DAY, -:days, CURRENT_TIMESTAMP)")
    List<User> findUsersCreatedInLastDays(@Param("days") int days);
    
    /**
     * Update user last login timestamp
     */
    @Query("UPDATE User u SET u.lastLoginAt = CURRENT_TIMESTAMP WHERE u.userId = :userId")
    void updateLastLoginAt(@Param("userId") Long userId);
    
    /**
     * Find users by status with pagination
     */
    @Query("SELECT u FROM User u WHERE u.status = :status")
    Page<User> findByStatusPaged(@Param("status") UserStatus status, Pageable pageable);
    
    /**
     * Count users by status
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") UserStatus status);
    
    /**
     * Count users created after a specific date
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt > :dateTime")
    long countUsersCreatedAfter(@Param("dateTime") LocalDateTime dateTime);
    
    /**
     * Search users by email or name
     */
    @Query("SELECT u FROM User u JOIN u.userProfile up WHERE " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(up.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(up.lastName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> findByEmailContainingOrUserProfile_FirstNameContainingOrUserProfile_LastNameContaining(
        @Param("search") String search, 
        @Param("search") String search2, 
        @Param("search") String search3);
    
    /**
     * Find users by role name
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRolesContaining(@Param("roleName") com.suppkart.model.enums.RoleType roleName);
    
    /**
     * Find user by email (alias for findByEmail for admin auth)
     */
    default Optional<User> findByUsername(String username) {
        return findByEmail(username);
    }
    
    // Additional methods needed by AdminCustomerService
    
    /**
     * Find users by user ID and order by created date
     */
    @Query("SELECT u FROM User u WHERE u.userId = :userId ORDER BY u.createdAt DESC")
    List<User> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    /**
     * Find users by user ID and status in list
     */
    @Query("SELECT u FROM User u WHERE u.userId = :userId AND u.status IN :statuses")
    List<User> findByUserIdAndStatusIn(@Param("userId") Long userId, @Param("statuses") List<UserStatus> statuses);
    
    /**
     * Count users created after specific date
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt > :dateTime")
    long countByCreatedAtAfter(@Param("dateTime") LocalDateTime dateTime);
    
    /**
     * Count users with last login before date or null
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLoginAt < :dateTime OR u.lastLoginAt IS NULL")
    long countByLastLoginBeforeOrLastLoginIsNull(@Param("dateTime") LocalDateTime dateTime);
    
    /**
     * Count returning customers (users with more than one order)
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN Order o ON u.userId = o.user.userId GROUP BY u.userId HAVING COUNT(o) > 1")
    long countReturningCustomers();
    
    /**
     * Count loyal customers (users with more than 5 orders)
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN Order o ON u.userId = o.user.userId GROUP BY u.userId HAVING COUNT(o) > 5")
    long countLoyalCustomers();
    
    /**
     * Find users created after date ordered by created date
     */
    @Query("SELECT u FROM User u WHERE u.createdAt > :dateTime ORDER BY u.createdAt DESC")
    Page<User> findByCreatedAtAfterOrderByCreatedAtDesc(@Param("dateTime") LocalDateTime dateTime, Pageable pageable);
}
