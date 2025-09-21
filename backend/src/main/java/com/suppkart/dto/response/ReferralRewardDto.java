package com.suppkart.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.suppkart.model.enums.RewardStatus;
import com.suppkart.model.enums.RewardType;

public class ReferralRewardDto {
    
    private Long rewardId;
    private Long userId;
    private String userName;
    private Long referralId;
    private String referralCode;
    private RewardType rewardType;
    private BigDecimal rewardAmount;
    private Integer rewardPercentage;
    private RewardStatus status;
    private LocalDateTime expirationDate;
    private LocalDateTime usageDate;
    private Long appliedOrderId;
    private Boolean isReferrerReward;
    private LocalDateTime createdAt;
    
    // Constructors
    public ReferralRewardDto() {}
    
    // Getters and Setters
    public Long getRewardId() {
        return rewardId;
    }
    
    public void setRewardId(Long rewardId) {
        this.rewardId = rewardId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public Long getReferralId() {
        return referralId;
    }
    
    public void setReferralId(Long referralId) {
        this.referralId = referralId;
    }
    
    public String getReferralCode() {
        return referralCode;
    }
    
    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }
    
    public RewardType getRewardType() {
        return rewardType;
    }
    
    public void setRewardType(RewardType rewardType) {
        this.rewardType = rewardType;
    }
    
    public BigDecimal getRewardAmount() {
        return rewardAmount;
    }
    
    public void setRewardAmount(BigDecimal rewardAmount) {
        this.rewardAmount = rewardAmount;
    }
    
    public Integer getRewardPercentage() {
        return rewardPercentage;
    }
    
    public void setRewardPercentage(Integer rewardPercentage) {
        this.rewardPercentage = rewardPercentage;
    }
    
    public RewardStatus getStatus() {
        return status;
    }
    
    public void setStatus(RewardStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }
    
    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }
    
    public LocalDateTime getUsageDate() {
        return usageDate;
    }
    
    public void setUsageDate(LocalDateTime usageDate) {
        this.usageDate = usageDate;
    }
    
    public Long getAppliedOrderId() {
        return appliedOrderId;
    }
    
    public void setAppliedOrderId(Long appliedOrderId) {
        this.appliedOrderId = appliedOrderId;
    }
    
    public Boolean getIsReferrerReward() {
        return isReferrerReward;
    }
    
    public void setIsReferrerReward(Boolean isReferrerReward) {
        this.isReferrerReward = isReferrerReward;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "ReferralRewardDto{" +
                "rewardId=" + rewardId +
                ", userId=" + userId +
                ", rewardType=" + rewardType +
                ", rewardAmount=" + rewardAmount +
                ", status=" + status +
                ", isReferrerReward=" + isReferrerReward +
                ", createdAt=" + createdAt +
                '}';
    }
}
