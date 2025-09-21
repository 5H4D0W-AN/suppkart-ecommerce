package com.suppkart.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.ReferralReward;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.RewardStatus;

@Repository
public interface ReferralRewardRepository extends JpaRepository<ReferralReward, Long> {
    
    /**
     * Find active rewards by user ID
     */
    @Query("SELECT r FROM ReferralReward r WHERE r.user = :user AND r.status = 'ACTIVE' " +
           "AND (r.expirationDate IS NULL OR r.expirationDate > :currentTime)")
    List<ReferralReward> findActiveRewardsByUser(@Param("user") User user, 
                                                @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find rewards by user and status
     */
    List<ReferralReward> findByUserAndStatusOrderByCreatedAtDesc(User user, RewardStatus status);
    
    /**
     * Find all rewards by user
     */
    List<ReferralReward> findByUserOrderByCreatedAtDesc(User user);
    
    Page<ReferralReward> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    /**
     * Find rewards by status
     */
    List<ReferralReward> findByStatusOrderByCreatedAtDesc(RewardStatus status);
    
    Page<ReferralReward> findByStatusOrderByCreatedAtDesc(RewardStatus status, Pageable pageable);
    
    /**
     * Find expired but unused rewards
     */
    @Query("SELECT r FROM ReferralReward r WHERE r.status = 'ACTIVE' " +
           "AND r.expirationDate IS NOT NULL AND r.expirationDate < :currentTime")
    List<ReferralReward> findExpiredActiveRewards(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find pending referrer rewards (waiting for first purchase)
     */
    @Query("SELECT r FROM ReferralReward r WHERE r.status = 'PENDING' AND r.isReferrerReward = true")
    List<ReferralReward> findPendingReferrerRewards();
    
    /**
     * Find all rewards for admin with pagination
     */
    Page<ReferralReward> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Find rewards by referral ID
     */
    @Query("SELECT r FROM ReferralReward r WHERE r.referral.referralId = :referralId")
    List<ReferralReward> findByReferralId(@Param("referralId") Long referralId);
    
    /**
     * Find rewards by date range
     */
    @Query("SELECT r FROM ReferralReward r WHERE r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.createdAt DESC")
    List<ReferralReward> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count rewards by user and status
     */
    Long countByUserAndStatus(User user, RewardStatus status);
    
    /**
     * Get total reward value by user
     */
    @Query("SELECT SUM(r.rewardAmount) FROM ReferralReward r WHERE r.user = :user AND r.status = 'APPLIED'")
    Double getTotalRewardValueByUser(@Param("user") User user);
    
    /**
     * Get available credit amount for user
     */
    @Query("SELECT SUM(r.rewardAmount) FROM ReferralReward r WHERE r.user = :user " +
           "AND r.status = 'ACTIVE' AND r.rewardType = 'CREDIT' " +
           "AND (r.expirationDate IS NULL OR r.expirationDate > :currentTime)")
    Double getAvailableCreditByUser(@Param("user") User user, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find referrer rewards by referral
     */
    @Query("SELECT r FROM ReferralReward r WHERE r.referral.referralId = :referralId AND r.isReferrerReward = true")
    List<ReferralReward> findReferrerRewardsByReferralId(@Param("referralId") Long referralId);
    
    /**
     * Find referee rewards by referral
     */
    @Query("SELECT r FROM ReferralReward r WHERE r.referral.referralId = :referralId AND r.isReferrerReward = false")
    List<ReferralReward> findRefereeRewardsByReferralId(@Param("referralId") Long referralId);
    
    /**
     * Get reward statistics for platform
     */
    @Query("SELECT " +
           "COUNT(r) as totalRewards, " +
           "SUM(CASE WHEN r.status = 'ACTIVE' THEN 1 ELSE 0 END) as activeRewards, " +
           "SUM(CASE WHEN r.status = 'APPLIED' THEN 1 ELSE 0 END) as appliedRewards, " +
           "SUM(CASE WHEN r.status = 'EXPIRED' THEN 1 ELSE 0 END) as expiredRewards, " +
           "SUM(CASE WHEN r.status = 'APPLIED' THEN r.rewardAmount ELSE 0 END) as totalValueApplied " +
           "FROM ReferralReward r")
    Object[] getPlatformRewardStats();
    
    /**
     * Find rewards that need to be activated (referrer rewards when first purchase is complete)
     */
    @Query("SELECT r FROM ReferralReward r " +
           "WHERE r.status = 'PENDING' AND r.isReferrerReward = true " +
           "AND r.referral.status = 'REWARDED'")
    List<ReferralReward> findRewardsToActivate();
    
    /**
     * Count rewards by status
     */
    Long countByStatus(RewardStatus status);
    
    /**
     * Get total reward value across all users
     */
    @Query("SELECT SUM(r.rewardAmount) FROM ReferralReward r WHERE r.status = 'APPLIED'")
    Double getTotalRewardValueForAllUsers();
}
