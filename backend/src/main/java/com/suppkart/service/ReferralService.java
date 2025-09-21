package com.suppkart.service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suppkart.dto.response.ReferralDto;
import com.suppkart.dto.response.ReferralStatsDto;
import com.suppkart.exception.ReferralException;
import com.suppkart.model.entity.Order;
import com.suppkart.model.entity.Referral;
import com.suppkart.model.entity.ReferralReward;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.OrderStatus;
import com.suppkart.model.enums.ReferralStatus;
import com.suppkart.model.enums.RewardStatus;
import com.suppkart.model.enums.RewardType;
import com.suppkart.repository.ReferralRepository;
import com.suppkart.repository.ReferralRewardRepository;
import com.suppkart.repository.UserRepository;

@Service
@Transactional
public class ReferralService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReferralService.class);
    private static final String REFERRAL_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int REFERRAL_CODE_LENGTH = 8;
    private static final BigDecimal REFEREE_REWARD_AMOUNT = new BigDecimal("100.00");
    private static final BigDecimal REFERRER_REWARD_AMOUNT = new BigDecimal("50.00");
    
    @Autowired
    private ReferralRepository referralRepository;
    
    @Autowired
    private ReferralRewardRepository referralRewardRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ReferralRewardService referralRewardService;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Generate unique referral code for a user
     */
    public String generateReferralCode(User user) {
        try {
            List<Referral> existingReferrals = referralRepository.findByReferrerUserOrderByCreatedAtDesc(user);
            if (!existingReferrals.isEmpty()) {
                return existingReferrals.get(0).getReferralCode();
            }
            
            String referralCode;
            int attempts = 0;
            int maxAttempts = 10;
            
            do {
                referralCode = generateUniqueCode();
                attempts++;
                
                if (attempts > maxAttempts) {
                    throw new ReferralException("Unable to generate unique referral code after " + maxAttempts + " attempts");
                }
            } while (referralRepository.findByReferralCode(referralCode).isPresent());
            
            Referral referral = new Referral(referralCode, user);
            referralRepository.save(referral);
            
            logger.info("Generated referral code {} for user {}", referralCode, user.getUserId());
            return referralCode;
            
        } catch (Exception e) {
            logger.error("Error generating referral code for user {}: {}", user.getUserId(), e.getMessage());
            throw new ReferralException("Failed to generate referral code: " + e.getMessage());
        }
    }
    
    /**
     * Process new user registration with referral code
     */
    public void processNewUserRegistration(User newUser, String referralCode) {
        if (referralCode == null || referralCode.trim().isEmpty()) {
            return;
        }
        
        try {
            Optional<Referral> referralOpt = referralRepository.findByReferralCode(referralCode.trim().toUpperCase());
            
            if (referralOpt.isEmpty()) {
                logger.warn("Invalid referral code {} used during registration", referralCode);
                return;
            }
            
            Referral referral = referralOpt.get();
            
            if (referral.isUsed()) {
                logger.warn("Referral code {} is already used", referralCode);
                return;
            }
            
            if (referral.getReferrerUser().getUserId().equals(newUser.getUserId())) {
                logger.warn("User {} tried to use their own referral code", newUser.getUserId());
                return;
            }
            
            referral.markAsUsed(newUser);
            referralRepository.save(referral);
            
            ReferralReward refereeReward = new ReferralReward(
                newUser, 
                referral, 
                RewardType.CREDIT, 
                REFEREE_REWARD_AMOUNT, 
                false
            );
            referralRewardService.createReward(refereeReward);
            
            ReferralReward referrerReward = new ReferralReward(
                referral.getReferrerUser(), 
                referral, 
                RewardType.CREDIT, 
                REFERRER_REWARD_AMOUNT, 
                true
            );
            referrerReward.setStatus(RewardStatus.PENDING);
            referralRewardService.createReward(referrerReward);
            
            logger.info("Processed referral code {} for new user {}. Referee reward: ₹{}, Referrer reward pending", 
                       referralCode, newUser.getUserId(), REFEREE_REWARD_AMOUNT);
            
        } catch (Exception e) {
            logger.error("Error processing referral code {} for user {}: {}", referralCode, newUser.getUserId(), e.getMessage());
        }
    }
    
    /**
     * Track first purchase by referred users
     */
    public void trackFirstPurchase(User user, Order order) {
        try {
            List<Referral> referrals = referralRepository.findByReferredUserAndFirstOrderIsNull(user);
            
            for (Referral referral : referrals) {
                referral.markFirstOrderCompleted(order);
                referralRepository.save(referral);
                
                activateReferrerRewards(referral);
                
                logger.info("Recorded first purchase for referral {} by user {}", referral.getReferralId(), user.getUserId());
            }
            
        } catch (Exception e) {
            logger.error("Error tracking first purchase for user {}: {}", user.getUserId(), e.getMessage());
        }
    }
    
    /**
     * Get user's referral code
     */
    @Transactional(readOnly = true)
    public String getUserReferralCode(User user) {
        List<Referral> referrals = referralRepository.findByReferrerUserOrderByCreatedAtDesc(user);
        
        if (referrals.isEmpty()) {
            return generateReferralCode(user);
        }
        
        return referrals.get(0).getReferralCode();
    }
    
    /**
     * Get referral statistics for a user
     */
    @Transactional(readOnly = true)
    public ReferralStatsDto getUserReferralStats(User user) {
        try {
            Long totalReferrals = referralRepository.countByReferrerUser(user);
            Long successfulReferrals = referralRepository.countByReferrerUserAndStatus(user, ReferralStatus.REWARDED);
            
            Double totalRewardValue = referralRewardRepository.getTotalRewardValueByUser(user);
            BigDecimal totalRewards = totalRewardValue != null ? BigDecimal.valueOf(totalRewardValue) : BigDecimal.ZERO;
            
            Double availableCredit = referralRewardRepository.getAvailableCreditByUser(user, LocalDateTime.now());
            BigDecimal availableCredits = availableCredit != null ? BigDecimal.valueOf(availableCredit) : BigDecimal.ZERO;
            
            ReferralStatsDto result = new ReferralStatsDto();
            result.setTotalReferrals(totalReferrals);
            result.setSuccessfulReferrals(successfulReferrals);
            result.setRewardedReferrals(successfulReferrals);
            result.setTotalRewardValueEarned(totalRewards);
            result.setAvailableCreditAmount(availableCredits);
            result.setReferralCode(getUserReferralCode(user));
            return result;
            
        } catch (Exception e) {
            logger.error("Error getting referral stats for user {}: {}", user.getUserId(), e.getMessage());
            throw new ReferralException("Failed to retrieve referral statistics: " + e.getMessage());
        }
    }
    
    /**
     * Get user's referrals with pagination
     */
    @Transactional(readOnly = true)
    public Page<ReferralDto> getUserReferrals(User user, Pageable pageable) {
        try {
            Page<Referral> referrals = referralRepository.findByReferrerUserOrderByCreatedAtDesc(user, pageable);
            return referrals.map(this::convertToDto);
            
        } catch (Exception e) {
            logger.error("Error getting referrals for user {}: {}", user.getUserId(), e.getMessage());
            throw new ReferralException("Failed to retrieve user referrals: " + e.getMessage());
        }
    }
    
    /**
     * Process order completion events to track first purchases
     */
    public void processOrderCompletionEvent(Order order) {
        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            trackFirstPurchase(order.getUser(), order);
        }
    }
    
    /**
     * Validate referral code
     */
    @Transactional(readOnly = true)
    public boolean isValidReferralCode(String referralCode) {
        if (referralCode == null || referralCode.trim().isEmpty()) {
            return false;
        }
        
        Optional<Referral> referral = referralRepository.findByReferralCode(referralCode.trim().toUpperCase());
        return referral.isPresent() && !referral.get().isUsed();
    }
    
    /**
     * Generate sharing URLs for different platforms
     */
    public String generateSharingUrl(String referralCode, String platform) {
        String baseUrl = "https://suppkart.com/register?ref=" + referralCode;
        String message = "Join SuppKart using my referral code and get amazing discounts! Use code: " + referralCode;
        
        switch (platform.toLowerCase()) {
            case "whatsapp":
                return "https://wa.me/?text=" + encodeUrl(message + " " + baseUrl);
            case "facebook":
                return "https://www.facebook.com/sharer/sharer.php?u=" + encodeUrl(baseUrl);
            case "twitter":
                return "https://twitter.com/intent/tweet?text=" + encodeUrl(message) + "&url=" + encodeUrl(baseUrl);
            case "email":
                return "mailto:?subject=" + encodeUrl("Join SuppKart!") + "&body=" + encodeUrl(message + " " + baseUrl);
            default:
                return baseUrl;
        }
    }
    
    // Admin methods
    
    /**
     * Get all referrals for admin with filters and pagination
     */
    @Transactional(readOnly = true)
    public Page<ReferralDto> getAllReferralsAdmin(ReferralStatus status, String referrerName, String referralCode, Pageable pageable) {
        try {
            Page<Referral> referrals = referralRepository.findAllByOrderByCreatedAtDesc(pageable);
            return referrals.map(this::convertToDto);
        } catch (Exception e) {
            logger.error("Error getting all referrals for admin: {}", e.getMessage());
            throw new ReferralException("Failed to retrieve referrals: " + e.getMessage());
        }
    }
    
    /**
     * Get total referrals count
     */
    @Transactional(readOnly = true)
    public Long getTotalReferralsCount() {
        try {
            return referralRepository.count();
        } catch (Exception e) {
            logger.error("Error getting total referrals count: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * Get successful referrals count
     */
    @Transactional(readOnly = true)
    public Long getSuccessfulReferralsCount() {
        try {
            return referralRepository.countByStatus(ReferralStatus.REWARDED);
        } catch (Exception e) {
            logger.error("Error getting successful referrals count: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * Get pending referrals count
     */
    @Transactional(readOnly = true)
    public Long getPendingReferralsCount() {
        try {
            return referralRepository.countByStatus(ReferralStatus.USED);
        } catch (Exception e) {
            logger.error("Error getting pending referrals count: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * Get monthly referral statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMonthlyReferralStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            
            Long monthlyReferrals = referralRepository.countByCreatedAtBetween(startOfMonth, now);
            Long monthlySuccessful = referralRepository.countByStatusAndCreatedAtBetween(ReferralStatus.REWARDED, startOfMonth, now);
            
            stats.put("monthlyReferrals", monthlyReferrals);
            stats.put("monthlySuccessful", monthlySuccessful);
            stats.put("period", startOfMonth.getMonth().name() + " " + startOfMonth.getYear());
            
            return stats;
        } catch (Exception e) {
            logger.error("Error getting monthly referral stats: {}", e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * Update referral program settings
     */
    @Transactional
    public void updateReferralSettings(Map<String, Object> settings) {
        try {
            logger.info("Referral settings updated: {}", settings);
        } catch (Exception e) {
            logger.error("Error updating referral settings: {}", e.getMessage());
            throw new ReferralException("Failed to update referral settings: " + e.getMessage());
        }
    }
    
    /**
     * Get top referrers
     */
    @Transactional(readOnly = true)
    public Page<ReferralDto> getTopReferrers(Pageable pageable) {
        try {
            Page<Referral> topReferrers = referralRepository.findAllByOrderByCreatedAtDesc(pageable);
            return topReferrers.map(this::convertToDto);
        } catch (Exception e) {
            logger.error("Error getting top referrers: {}", e.getMessage());
            throw new ReferralException("Failed to retrieve top referrers: " + e.getMessage());
        }
    }
    
    /**
     * Get referral analytics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getReferralAnalytics(String period) {
        try {
            Map<String, Object> analytics = new HashMap<>();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate;
            
            switch (period != null ? period.toLowerCase() : "month") {
                case "week":
                    startDate = now.minusWeeks(1);
                    break;
                case "year":
                    startDate = now.minusYears(1);
                    break;
                default:
                    startDate = now.minusMonths(1);
            }
            
            Long totalReferrals = referralRepository.countByCreatedAtBetween(startDate, now);
            Long successfulReferrals = referralRepository.countByStatusAndCreatedAtBetween(ReferralStatus.REWARDED, startDate, now);
            
            analytics.put("period", period);
            analytics.put("totalReferrals", totalReferrals);
            analytics.put("successfulReferrals", successfulReferrals);
            analytics.put("conversionRate", totalReferrals > 0 ? (double) successfulReferrals / totalReferrals * 100 : 0.0);
            
            return analytics;
        } catch (Exception e) {
            logger.error("Error getting referral analytics: {}", e.getMessage());
            return new HashMap<>();
        }
    }
    
    private void activateReferrerRewards(Referral referral) {
        try {
            List<ReferralReward> pendingRewards = referralRewardRepository.findReferrerRewardsByReferralId(referral.getReferralId())
                .stream()
                .filter(reward -> reward.getStatus() == RewardStatus.PENDING)
                .collect(Collectors.toList());
            
            for (ReferralReward reward : pendingRewards) {
                reward.activate();
                referralRewardService.updateReward(reward);
                
                logger.info("Activated referrer reward {} for user {}", reward.getRewardId(), reward.getUser().getUserId());
            }
            
        } catch (Exception e) {
            logger.error("Error activating referrer rewards for referral {}: {}", referral.getReferralId(), e.getMessage());
        }
    }
    
    private String generateUniqueCode() {
        StringBuilder code = new StringBuilder(REFERRAL_CODE_LENGTH);
        for (int i = 0; i < REFERRAL_CODE_LENGTH; i++) {
            int index = secureRandom.nextInt(REFERRAL_CODE_CHARS.length());
            code.append(REFERRAL_CODE_CHARS.charAt(index));
        }
        return code.toString();
    }
    
    private String encodeUrl(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return value;
        }
    }
    
    private ReferralDto convertToDto(Referral referral) {
        ReferralDto dto = new ReferralDto();
        dto.setReferralId(referral.getReferralId());
        dto.setReferralCode(referral.getReferralCode());
        dto.setReferrerUserId(referral.getReferrerUser().getUserId());
        dto.setReferrerUserName(referral.getReferrerUser().getFullName());
        dto.setReferredUserId(referral.getReferredUser() != null ? referral.getReferredUser().getUserId() : null);
        dto.setReferredUserName(referral.getReferredUser() != null ? referral.getReferredUser().getFullName() : null);
        dto.setStatus(referral.getStatus());
        dto.setCreatedAt(referral.getCreatedAt());
        dto.setUsageDate(referral.getUsageDate());
        dto.setFirstOrderId(referral.getFirstOrder() != null ? referral.getFirstOrder().getOrderId() : null);
        dto.setFirstOrderCompletionDate(referral.getFirstOrderCompletionDate());
        return dto;
    }
}
