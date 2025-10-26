package com.suppkart.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suppkart.dto.response.ReferralRewardDto;
import com.suppkart.exception.ReferralException;
import com.suppkart.model.entity.Order;
import com.suppkart.model.entity.ReferralReward;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.RewardStatus;
import com.suppkart.model.enums.RewardType;
import com.suppkart.repository.ReferralRewardRepository;

@Service
@Transactional
public class ReferralRewardService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReferralRewardService.class);
    
    @Autowired
    private ReferralRewardRepository referralRewardRepository;
    
    @Autowired
    private EmailNotificationService emailNotificationService;
    
    /**
     * Create a new reward
     */
    public ReferralReward createReward(ReferralReward reward) {
        try {
            return referralRewardRepository.save(reward);
        } catch (Exception e) {
            logger.error("Error creating reward: {}", e.getMessage());
            throw new ReferralException("Failed to create reward: " + e.getMessage());
        }
    }
    
    /**
     * Update an existing reward
     */
    public ReferralReward updateReward(ReferralReward reward) {
        try {
            return referralRewardRepository.save(reward);
        } catch (Exception e) {
            logger.error("Error updating reward {}: {}", reward.getRewardId(), e.getMessage());
            throw new ReferralException("Failed to update reward: " + e.getMessage());
        }
    }
    
    /**
     * Get active rewards for a user
     */
    @Transactional(readOnly = true)
    public List<ReferralRewardDto> getActiveRewardsByUser(User user) {
        try {
            List<ReferralReward> rewards = referralRewardRepository.findActiveRewardsByUser(user, LocalDateTime.now());
            return rewards.stream().map(this::convertToDto).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting active rewards for user {}: {}", user.getUserId(), e.getMessage());
            throw new ReferralException("Failed to retrieve active rewards: " + e.getMessage());
        }
    }
    
    /**
     * Get all rewards for a user with pagination
     */
    @Transactional(readOnly = true)
    public Page<ReferralRewardDto> getUserRewards(User user, Pageable pageable) {
        try {
            Page<ReferralReward> rewards = referralRewardRepository.findByUserOrderByCreatedAtDesc(user, pageable);
            return rewards.map(this::convertToDto);
        } catch (Exception e) {
            logger.error("Error getting rewards for user {}: {}", user.getUserId(), e.getMessage());
            throw new ReferralException("Failed to retrieve user rewards: " + e.getMessage());
        }
    }
    
    /**
     * Get all rewards with pagination (Admin)
     */
    @Transactional(readOnly = true)
    public Page<ReferralRewardDto> getAllRewards(Pageable pageable) {
        try {
            Page<ReferralReward> rewards = referralRewardRepository.findAllByOrderByCreatedAtDesc(pageable);
            return rewards.map(this::convertToDto);
        } catch (Exception e) {
            logger.error("Error getting all rewards: {}", e.getMessage());
            throw new ReferralException("Failed to retrieve rewards: " + e.getMessage());
        }
    }
    
    /**
     * Get rewards by status with pagination (Admin)
     */
    @Transactional(readOnly = true)
    public Page<ReferralRewardDto> getRewardsByStatus(RewardStatus status, Pageable pageable) {
        try {
            Page<ReferralReward> rewards = referralRewardRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
            return rewards.map(this::convertToDto);
        } catch (Exception e) {
            logger.error("Error getting rewards by status {}: {}", status, e.getMessage());
            throw new ReferralException("Failed to retrieve rewards by status: " + e.getMessage());
        }
    }
    
    /**
     * Get all rewards with filters (Admin)
     */
    @Transactional(readOnly = true)
    public Page<ReferralRewardDto> getAllRewardsWithFilters(RewardStatus status, String userName, Boolean isReferrerReward, Pageable pageable) {
        try {
            Page<ReferralReward> rewards;
            
            if (status != null || userName != null || isReferrerReward != null) {
                rewards = referralRewardRepository.findRewardsWithFilters(status, userName, isReferrerReward, pageable);
            } else {
                rewards = referralRewardRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
            
            return rewards.map(this::convertToDto);
        } catch (Exception e) {
            logger.error("Error getting rewards with filters: {}", e.getMessage());
            throw new ReferralException("Failed to retrieve rewards with filters: " + e.getMessage());
        }
    }
    
    /**
     * Apply rewards during checkout
     */
    public BigDecimal applyRewardsToOrder(User user, BigDecimal orderTotal, Order order) {
        try {
            List<ReferralReward> activeRewards = referralRewardRepository.findActiveRewardsByUser(user, LocalDateTime.now());
            BigDecimal totalDiscount = BigDecimal.ZERO;
            
            for (ReferralReward reward : activeRewards) {
                if (reward.isActive()) {
                    BigDecimal discountAmount = reward.getDiscountAmount(orderTotal);
                    
                    // Apply the reward to the order
                    reward.applyToOrder(order);
                    referralRewardRepository.save(reward);
                    
                    totalDiscount = totalDiscount.add(discountAmount);
                    
                    logger.info("Applied reward {} to order {}. Discount: ₹{}", 
                               reward.getRewardId(), order.getOrderId(), discountAmount);
                    
                    // For credit rewards, we typically apply one at a time
                    if (reward.getRewardType() == RewardType.CREDIT) {
                        break;
                    }
                }
            }
            
            return totalDiscount;
            
        } catch (Exception e) {
            logger.error("Error applying rewards for user {} to order {}: {}", 
                        user.getUserId(), order.getOrderId(), e.getMessage());
            throw new ReferralException("Failed to apply rewards: " + e.getMessage());
        }
    }
    
    /**
     * Get available credit amount for a user
     */
    @Transactional(readOnly = true)
    public BigDecimal getAvailableCreditForUser(User user) {
        try {
            Double availableCredit = referralRewardRepository.getAvailableCreditByUser(user, LocalDateTime.now());
            return availableCredit != null ? BigDecimal.valueOf(availableCredit) : BigDecimal.ZERO;
        } catch (Exception e) {
            logger.error("Error getting available credit for user {}: {}", user.getUserId(), e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Expire old rewards
     */
    @Transactional
    public void expireOldRewards() {
        try {
            List<ReferralReward> expiredRewards = referralRewardRepository.findExpiredActiveRewards(LocalDateTime.now());
            
            for (ReferralReward reward : expiredRewards) {
                reward.expire();
                referralRewardRepository.save(reward);
                
                logger.info("Expired reward {} for user {}", reward.getRewardId(), reward.getUser().getUserId());
            }
            
            if (!expiredRewards.isEmpty()) {
                logger.info("Expired {} old rewards", expiredRewards.size());
            }
            
        } catch (Exception e) {
            logger.error("Error expiring old rewards: {}", e.getMessage());
        }
    }
    
    /**
     * Expire rewards older than 15 days with user notification
     */
    @Transactional
    public void expireRewardsOlderThan15Days() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(15);
            List<ReferralReward> rewardsToExpire = referralRewardRepository.findActiveRewardsOlderThan(cutoffDate);
            
            for (ReferralReward reward : rewardsToExpire) {
                // Expire the reward
                reward.expire();
                referralRewardRepository.save(reward);
                
                // Send notification to user
                try {
                    sendRewardExpirationNotification(reward);
                } catch (Exception emailException) {
                    logger.error("Failed to send expiration notification for reward {} to user {}: {}", 
                               reward.getRewardId(), reward.getUser().getUserId(), emailException.getMessage());
                }
                
                logger.info("Expired reward {} for user {} (older than 15 days)", 
                           reward.getRewardId(), reward.getUser().getUserId());
            }
            
            if (!rewardsToExpire.isEmpty()) {
                logger.info("Expired {} rewards older than 15 days with user notifications", rewardsToExpire.size());
            }
            
        } catch (Exception e) {
            logger.error("Error expiring rewards older than 15 days: {}", e.getMessage());
        }
    }
    
    /**
     * Send reward expiration notification to user
     */
    private void sendRewardExpirationNotification(ReferralReward reward) {
        try {
            String subject = "Referral Reward Expired - SuppKart";
            
            StringBuilder content = new StringBuilder();
            content.append("Dear ").append(reward.getUser().getFirstName()).append(",\n\n");
            content.append("We wanted to inform you that one of your referral rewards has expired.\n\n");
            content.append("Reward Details:\n");
            content.append("Reward Amount: ₹").append(reward.getRewardAmount()).append("\n");
            content.append("Referral Code: ").append(reward.getReferral().getReferralCode()).append("\n");
            content.append("Expiration Date: ").append(reward.getExpirationDate()).append("\n\n");
            content.append("This reward was valid for 15 days from the date it was activated. ");
            content.append("Unfortunately, it has now expired and can no longer be used.\n\n");
            content.append("Don't worry! You can continue earning new rewards by referring more friends to SuppKart. ");
            content.append("Each successful referral earns you fresh rewards.\n\n");
            content.append("Keep sharing your referral code and enjoy the benefits!\n\n");
            content.append("Best regards,\n");
            content.append("SuppKart Team");
            
            emailNotificationService.sendEmail(reward.getUser().getEmail(), subject, content.toString());
            
        } catch (Exception e) {
            logger.error("Failed to send reward expiration notification to user {}: {}", 
                       reward.getUser().getUserId(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Activate pending referrer rewards
     */
    @Transactional
    public void activatePendingReferrerRewards() {
        try {
            List<ReferralReward> rewardsToActivate = referralRewardRepository.findRewardsToActivate();
            
            for (ReferralReward reward : rewardsToActivate) {
                reward.activate();
                referralRewardRepository.save(reward);
                
                logger.info("Activated pending referrer reward {} for user {}", 
                           reward.getRewardId(), reward.getUser().getUserId());
            }
            
            if (!rewardsToActivate.isEmpty()) {
                logger.info("Activated {} pending referrer rewards", rewardsToActivate.size());
            }
            
        } catch (Exception e) {
            logger.error("Error activating pending referrer rewards: {}", e.getMessage());
        }
    }
    
    /**
     * Get reward statistics by user
     */
    @Transactional(readOnly = true)
    public RewardStatsDto getRewardStatsByUser(User user) {
        try {
            Long totalRewards = referralRewardRepository.countByUserAndStatus(user, null);
            Long activeRewards = referralRewardRepository.countByUserAndStatus(user, RewardStatus.ACTIVE);
            Long appliedRewards = referralRewardRepository.countByUserAndStatus(user, RewardStatus.APPLIED);
            Long expiredRewards = referralRewardRepository.countByUserAndStatus(user, RewardStatus.EXPIRED);
            
            Double totalValueApplied = referralRewardRepository.getTotalRewardValueByUser(user);
            BigDecimal totalValue = totalValueApplied != null ? BigDecimal.valueOf(totalValueApplied) : BigDecimal.ZERO;
            
            BigDecimal availableCredit = getAvailableCreditForUser(user);
            
            return RewardStatsDto.builder()
                .totalRewards(totalRewards)
                .activeRewards(activeRewards)
                .appliedRewards(appliedRewards)
                .expiredRewards(expiredRewards)
                .totalValueApplied(totalValue)
                .availableCredit(availableCredit)
                .build();
            
        } catch (Exception e) {
            logger.error("Error getting reward stats for user {}: {}", user.getUserId(), e.getMessage());
            throw new ReferralException("Failed to retrieve reward statistics: " + e.getMessage());
        }
    }
    
    /**
     * Check if user has available rewards for checkout
     */
    @Transactional(readOnly = true)
    public boolean hasAvailableRewards(User user) {
        try {
            List<ReferralReward> activeRewards = referralRewardRepository.findActiveRewardsByUser(user, LocalDateTime.now());
            return !activeRewards.isEmpty();
        } catch (Exception e) {
            logger.error("Error checking available rewards for user {}: {}", user.getUserId(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Get best available reward for order
     */
    @Transactional(readOnly = true)
    public ReferralReward getBestRewardForOrder(User user, BigDecimal orderTotal) {
        try {
            List<ReferralReward> activeRewards = referralRewardRepository.findActiveRewardsByUser(user, LocalDateTime.now());
            
            ReferralReward bestReward = null;
            BigDecimal bestDiscount = BigDecimal.ZERO;
            
            for (ReferralReward reward : activeRewards) {
                if (reward.isActive()) {
                    BigDecimal discount = reward.getDiscountAmount(orderTotal);
                    if (discount.compareTo(bestDiscount) > 0) {
                        bestDiscount = discount;
                        bestReward = reward;
                    }
                }
            }
            
            return bestReward;
            
        } catch (Exception e) {
            logger.error("Error finding best reward for user {} and order total {}: {}", 
                        user.getUserId(), orderTotal, e.getMessage());
            return null;
        }
    }
    
    private ReferralRewardDto convertToDto(ReferralReward reward) {
        ReferralRewardDto dto = new ReferralRewardDto();
        dto.setRewardId(reward.getRewardId());
        dto.setUserId(reward.getUser().getUserId());
        dto.setUserName(reward.getUser().getFullName());
        dto.setReferralId(reward.getReferral().getReferralId());
        dto.setReferralCode(reward.getReferral().getReferralCode());
        dto.setRewardType(reward.getRewardType());
        dto.setRewardAmount(reward.getRewardAmount());
        dto.setRewardPercentage(reward.getRewardPercentage());
        dto.setStatus(reward.getStatus());
        dto.setExpirationDate(reward.getExpirationDate());
        dto.setUsageDate(reward.getUsageDate());
        dto.setAppliedOrderId(reward.getAppliedOrder() != null ? reward.getAppliedOrder().getOrderId() : null);
        dto.setIsReferrerReward(reward.getIsReferrerReward());
        dto.setCreatedAt(reward.getCreatedAt());
        return dto;
    }
    
    // Inner class for reward statistics
    public static class RewardStatsDto {
        private Long totalRewards;
        private Long activeRewards;
        private Long appliedRewards;
        private Long expiredRewards;
        private BigDecimal totalValueApplied;
        private BigDecimal availableCredit;
        
        private RewardStatsDto(Builder builder) {
            this.totalRewards = builder.totalRewards;
            this.activeRewards = builder.activeRewards;
            this.appliedRewards = builder.appliedRewards;
            this.expiredRewards = builder.expiredRewards;
            this.totalValueApplied = builder.totalValueApplied;
            this.availableCredit = builder.availableCredit;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private Long totalRewards;
            private Long activeRewards;
            private Long appliedRewards;
            private Long expiredRewards;
            private BigDecimal totalValueApplied;
            private BigDecimal availableCredit;
            
            public Builder totalRewards(Long totalRewards) {
                this.totalRewards = totalRewards;
                return this;
            }
            
            public Builder activeRewards(Long activeRewards) {
                this.activeRewards = activeRewards;
                return this;
            }
            
            public Builder appliedRewards(Long appliedRewards) {
                this.appliedRewards = appliedRewards;
                return this;
            }
            
            public Builder expiredRewards(Long expiredRewards) {
                this.expiredRewards = expiredRewards;
                return this;
            }
            
            public Builder totalValueApplied(BigDecimal totalValueApplied) {
                this.totalValueApplied = totalValueApplied;
                return this;
            }
            
            public Builder availableCredit(BigDecimal availableCredit) {
                this.availableCredit = availableCredit;
                return this;
            }
            
            public RewardStatsDto build() {
                return new RewardStatsDto(this);
            }
        }
        
        // Getters
        public Long getTotalRewards() { return totalRewards; }
        public Long getActiveRewards() { return activeRewards; }
        public Long getAppliedRewards() { return appliedRewards; }
        public Long getExpiredRewards() { return expiredRewards; }
        public BigDecimal getTotalValueApplied() { return totalValueApplied; }
        public BigDecimal getAvailableCredit() { return availableCredit; }
    }
    
    // Admin statistical methods
    
    /**
     * Get total rewards count
     */
    @Transactional(readOnly = true)
    public Long getTotalRewardsCount() {
        try {
            return referralRewardRepository.count();
        } catch (Exception e) {
            logger.error("Error getting total rewards count: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * Get total reward value across all users
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalRewardValue() {
        try {
            Double totalValue = referralRewardRepository.getTotalRewardValueForAllUsers();
            return totalValue != null ? BigDecimal.valueOf(totalValue) : BigDecimal.ZERO;
        } catch (Exception e) {
            logger.error("Error getting total reward value: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Get active rewards count
     */
    @Transactional(readOnly = true)
    public Long getActiveRewardsCount() {
        try {
            return referralRewardRepository.countByStatus(RewardStatus.ACTIVE);
        } catch (Exception e) {
            logger.error("Error getting active rewards count: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * Get applied rewards count
     */
    @Transactional(readOnly = true)
    public Long getAppliedRewardsCount() {
        try {
            return referralRewardRepository.countByStatus(RewardStatus.APPLIED);
        } catch (Exception e) {
            logger.error("Error getting applied rewards count: {}", e.getMessage());
            return 0L;
        }
    }
}
