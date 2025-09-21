package com.suppkart.dto.response;

import java.math.BigDecimal;

public class ReferralStatsDto {
    
    private Long totalReferrals;
    private Long successfulReferrals;
    private Long rewardedReferrals;
    private Long activeRewards;
    private Long expiredRewards;
    private BigDecimal totalRewardValueEarned;
    private BigDecimal availableCreditAmount;
    private String referralCode;
    
    // Constructors
    public ReferralStatsDto() {}
    
    public ReferralStatsDto(Long totalReferrals, Long successfulReferrals, Long rewardedReferrals) {
        this.totalReferrals = totalReferrals;
        this.successfulReferrals = successfulReferrals;
        this.rewardedReferrals = rewardedReferrals;
    }
    
    // Getters and Setters
    public Long getTotalReferrals() {
        return totalReferrals;
    }
    
    public void setTotalReferrals(Long totalReferrals) {
        this.totalReferrals = totalReferrals;
    }
    
    public Long getSuccessfulReferrals() {
        return successfulReferrals;
    }
    
    public void setSuccessfulReferrals(Long successfulReferrals) {
        this.successfulReferrals = successfulReferrals;
    }
    
    public Long getRewardedReferrals() {
        return rewardedReferrals;
    }
    
    public void setRewardedReferrals(Long rewardedReferrals) {
        this.rewardedReferrals = rewardedReferrals;
    }
    
    public Long getActiveRewards() {
        return activeRewards;
    }
    
    public void setActiveRewards(Long activeRewards) {
        this.activeRewards = activeRewards;
    }
    
    public Long getExpiredRewards() {
        return expiredRewards;
    }
    
    public void setExpiredRewards(Long expiredRewards) {
        this.expiredRewards = expiredRewards;
    }
    
    public BigDecimal getTotalRewardValueEarned() {
        return totalRewardValueEarned;
    }
    
    public void setTotalRewardValueEarned(BigDecimal totalRewardValueEarned) {
        this.totalRewardValueEarned = totalRewardValueEarned;
    }
    
    public BigDecimal getAvailableCreditAmount() {
        return availableCreditAmount;
    }
    
    public void setAvailableCreditAmount(BigDecimal availableCreditAmount) {
        this.availableCreditAmount = availableCreditAmount;
    }
    
    public String getReferralCode() {
        return referralCode;
    }
    
    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }
    
    @Override
    public String toString() {
        return "ReferralStatsDto{" +
                "totalReferrals=" + totalReferrals +
                ", successfulReferrals=" + successfulReferrals +
                ", rewardedReferrals=" + rewardedReferrals +
                ", activeRewards=" + activeRewards +
                ", totalRewardValueEarned=" + totalRewardValueEarned +
                ", availableCreditAmount=" + availableCreditAmount +
                ", referralCode='" + referralCode + '\'' +
                '}';
    }
}
