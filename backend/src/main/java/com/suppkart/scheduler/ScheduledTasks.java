package com.suppkart.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.suppkart.model.entity.Cart;
import com.suppkart.model.entity.RefreshToken;
import com.suppkart.repository.CartRepository;
import com.suppkart.repository.RefreshTokenRepository;
import com.suppkart.service.ReferralRewardService;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private ReferralRewardService referralRewardService;

    /**
     * Clean up expired carts - runs every hour
     * Cron expression: second, minute, hour, day of month, month, day of week
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    @Transactional
    public void cleanupExpiredCarts() {
        try {
            logger.info("Starting cleanup of expired carts...");
            
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(7); // 7 days ago
            List<Cart> expiredCarts = cartRepository.findExpiredCarts(cutoffTime);
            
            if (!expiredCarts.isEmpty()) {
                logger.info("Found {} expired carts to cleanup", expiredCarts.size());
                
                // Delete expired carts (this will cascade to cart items)
                cartRepository.deleteAll(expiredCarts);
                
                logger.info("Successfully cleaned up {} expired carts", expiredCarts.size());
            } else {
                logger.debug("No expired carts found for cleanup");
            }
            
        } catch (Exception e) {
            logger.error("Error during cart cleanup", e);
        }
    }

    /**
     * Clean up expired refresh tokens - runs every 6 hours
     */
    @Scheduled(cron = "0 0 */6 * * *") // Every 6 hours
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        try {
            logger.info("Starting cleanup of expired refresh tokens...");
            
            LocalDateTime now = LocalDateTime.now();
            List<RefreshToken> expiredTokens = refreshTokenRepository.findByExpiresAtBefore(now);
            
            if (!expiredTokens.isEmpty()) {
                logger.info("Found {} expired refresh tokens to cleanup", expiredTokens.size());
                
                // Delete expired refresh tokens
                refreshTokenRepository.deleteAll(expiredTokens);
                
                logger.info("Successfully cleaned up {} expired refresh tokens", expiredTokens.size());
            } else {
                logger.debug("No expired refresh tokens found for cleanup");
            }
            
        } catch (Exception e) {
            logger.error("Error during refresh token cleanup", e);
        }
    }

    /**
     * Clean up guest carts older than 30 days - runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    @Transactional
    public void cleanupOldGuestCarts() {
        try {
            logger.info("Starting cleanup of old guest carts...");
            
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(30); // 30 days ago
            List<Cart> oldGuestCarts = cartRepository.findOldGuestCarts(cutoffTime);
            
            if (!oldGuestCarts.isEmpty()) {
                logger.info("Found {} old guest carts to cleanup", oldGuestCarts.size());
                
                // Delete old guest carts
                cartRepository.deleteAll(oldGuestCarts);
                
                logger.info("Successfully cleaned up {} old guest carts", oldGuestCarts.size());
            } else {
                logger.debug("No old guest carts found for cleanup");
            }
            
        } catch (Exception e) {
            logger.error("Error during old guest cart cleanup", e);
        }
    }

    /**
     * Log system health metrics - runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes (300,000 milliseconds)
    public void logSystemHealth() {
        try {
            // Get cart statistics
            long totalCarts = cartRepository.count();
            long activeCarts = cartRepository.countActiveCarts();
            long guestCarts = cartRepository.countGuestCarts();
            
            // Get refresh token statistics
            long totalRefreshTokens = refreshTokenRepository.count();
            long expiredTokens = refreshTokenRepository.countExpiredTokens(LocalDateTime.now());
            
            logger.info("System Health - Carts: {} total, {} active, {} guest | Refresh Tokens: {} total, {} expired", 
                       totalCarts, activeCarts, guestCarts, totalRefreshTokens, expiredTokens);
            
        } catch (Exception e) {
            logger.error("Error logging system health metrics", e);
        }
    }
    
    /**
     * Expire old referral rewards (15+ days old) - runs daily at 3 AM
     */
    @Scheduled(cron = "0 0 3 * * *") // Daily at 3 AM
    @Transactional
    public void expireOldReferralRewards() {
        try {
            logger.info("Starting expiration of referral rewards older than 15 days...");
            referralRewardService.expireRewardsOlderThan15Days();
            logger.info("Completed expiration of old referral rewards");
        } catch (Exception e) {
            logger.error("Error during referral reward expiration", e);
        }
    }
    
    /**
     * Activate pending referrer rewards - runs every 30 minutes
     */
    @Scheduled(cron = "0 */30 * * * *") // Every 30 minutes
    @Transactional
    public void activatePendingReferrerRewards() {
        try {
            logger.debug("Checking for pending referrer rewards to activate...");
            referralRewardService.activatePendingReferrerRewards();
        } catch (Exception e) {
            logger.error("Error during pending referrer reward activation", e);
        }
    }
}
