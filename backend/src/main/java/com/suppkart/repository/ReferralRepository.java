package com.suppkart.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.Referral;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.ReferralStatus;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {
    
    /**
     * Find referrals by referrer user ID
     */
    List<Referral> findByReferrerUserOrderByCreatedAtDesc(User referrerUser);
    
    Page<Referral> findByReferrerUserOrderByCreatedAtDesc(User referrerUser, Pageable pageable);
    
    /**
     * Find referral by unique referral code
     */
    Optional<Referral> findByReferralCode(String referralCode);
    
    /**
     * Find referrals by referred user ID
     */
    List<Referral> findByReferredUser(User referredUser);
    
    /**
     * Count successful referrals by user ID
     */
    @Query("SELECT COUNT(r) FROM Referral r WHERE r.referrerUser = :user AND r.status = :status")
    Long countByReferrerUserAndStatus(@Param("user") User user, @Param("status") ReferralStatus status);
    
    /**
     * Find referrals with completed first orders
     */
    @Query("SELECT r FROM Referral r WHERE r.firstOrder IS NOT NULL AND r.firstOrderCompletionDate IS NOT NULL")
    List<Referral> findReferralsWithCompletedFirstOrders();
    
    /**
     * Find referrals by status
     */
    List<Referral> findByStatusOrderByCreatedAtDesc(ReferralStatus status);
    
    Page<Referral> findByStatusOrderByCreatedAtDesc(ReferralStatus status, Pageable pageable);
    
    /**
     * Find all referrals for admin with pagination
     */
    Page<Referral> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Count total referrals by referrer user
     */
    Long countByReferrerUser(User referrerUser);
    
    /**
     * Count successful referrals (USED or REWARDED) by referrer user
     */
    @Query("SELECT COUNT(r) FROM Referral r WHERE r.referrerUser = :user AND r.status IN ('USED', 'REWARDED')")
    Long countSuccessfulReferralsByReferrerUser(@Param("user") User user);
    
    /**
     * Find referrals by date range
     */
    @Query("SELECT r FROM Referral r WHERE r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.createdAt DESC")
    List<Referral> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find referrals that are used but not yet rewarded (for processing pending referrer rewards)
     */
    @Query("SELECT r FROM Referral r WHERE r.status = 'USED' AND r.firstOrder IS NOT NULL")
    List<Referral> findUsedReferralsWithFirstOrders();
    
    /**
     * Get referral statistics for a user
     */
    @Query("SELECT " +
           "COUNT(r) as totalReferrals, " +
           "SUM(CASE WHEN r.status = 'USED' OR r.status = 'REWARDED' THEN 1 ELSE 0 END) as successfulReferrals, " +
           "SUM(CASE WHEN r.status = 'REWARDED' THEN 1 ELSE 0 END) as rewardedReferrals " +
           "FROM Referral r WHERE r.referrerUser = :user")
    Object[] getReferralStatsByUser(@Param("user") User user);
    
    /**
     * Get platform-wide referral statistics
     */
    @Query("SELECT " +
           "COUNT(r) as totalReferrals, " +
           "SUM(CASE WHEN r.status = 'USED' OR r.status = 'REWARDED' THEN 1 ELSE 0 END) as successfulReferrals, " +
           "SUM(CASE WHEN r.status = 'REWARDED' THEN 1 ELSE 0 END) as rewardedReferrals, " +
           "COUNT(DISTINCT r.referrerUser) as activeReferrers " +
           "FROM Referral r")
    Object[] getPlatformReferralStats();
    
    /**
     * Find top referrers by successful referrals
     */
    @Query("SELECT r.referrerUser, COUNT(r) as successfulCount " +
           "FROM Referral r WHERE r.status IN ('USED', 'REWARDED') " +
           "GROUP BY r.referrerUser ORDER BY successfulCount DESC")
    List<Object[]> findTopReferrers(Pageable pageable);
    
    /**
     * Count referrals by status
     */
    Long countByStatus(ReferralStatus status);
    
    /**
     * Count referrals created between dates
     */
    Long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Count referrals by status and created between dates
     */
    Long countByStatusAndCreatedAtBetween(ReferralStatus status, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find referrals by referred user where first order is null
     */
    List<Referral> findByReferredUserAndFirstOrderIsNull(User referredUser);
}
