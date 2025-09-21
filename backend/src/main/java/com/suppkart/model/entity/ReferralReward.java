package com.suppkart.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.suppkart.model.enums.RewardStatus;
import com.suppkart.model.enums.RewardType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "referral_rewards")
public class ReferralReward {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reward_id")
    private Long rewardId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referral_id", nullable = false)
    private Referral referral;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false)
    private RewardType rewardType;
    
    @Column(name = "reward_amount", precision = 10, scale = 2)
    private BigDecimal rewardAmount;
    
    @Column(name = "reward_percentage")
    private Integer rewardPercentage;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RewardStatus status = RewardStatus.PENDING;
    
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;
    
    @Column(name = "usage_date")
    private LocalDateTime usageDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applied_order_id")
    private Order appliedOrder;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_referrer_reward", nullable = false)
    private Boolean isReferrerReward = false;
    
    // Constructors
    public ReferralReward() {}
    
    public ReferralReward(User user, Referral referral, RewardType rewardType, 
                         BigDecimal rewardAmount, Boolean isReferrerReward) {
        this.user = user;
        this.referral = referral;
        this.rewardType = rewardType;
        this.rewardAmount = rewardAmount;
        this.isReferrerReward = isReferrerReward;
        this.status = isReferrerReward ? RewardStatus.PENDING : RewardStatus.ACTIVE;
        
        // Set expiration date (30 days from creation)
        this.expirationDate = LocalDateTime.now().plusDays(30);
    }
    
    public ReferralReward(User user, Referral referral, RewardType rewardType, 
                         Integer rewardPercentage, Boolean isReferrerReward) {
        this.user = user;
        this.referral = referral;
        this.rewardType = rewardType;
        this.rewardPercentage = rewardPercentage;
        this.isReferrerReward = isReferrerReward;
        this.status = isReferrerReward ? RewardStatus.PENDING : RewardStatus.ACTIVE;
        
        // Set expiration date (30 days from creation)
        this.expirationDate = LocalDateTime.now().plusDays(30);
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public void activate() {
        this.status = RewardStatus.ACTIVE;
    }
    
    public void applyToOrder(Order order) {
        this.appliedOrder = order;
        this.status = RewardStatus.APPLIED;
        this.usageDate = LocalDateTime.now();
    }
    
    public void expire() {
        this.status = RewardStatus.EXPIRED;
    }
    
    public boolean isActive() {
        return status == RewardStatus.ACTIVE && 
               (expirationDate == null || expirationDate.isAfter(LocalDateTime.now()));
    }
    
    public boolean isExpired() {
        return status == RewardStatus.EXPIRED || 
               (expirationDate != null && expirationDate.isBefore(LocalDateTime.now()));
    }
    
    public BigDecimal getDiscountAmount(BigDecimal orderTotal) {
        if (rewardType == RewardType.CREDIT) {
            return rewardAmount;
        } else if (rewardType == RewardType.DISCOUNT && rewardPercentage != null) {
            return orderTotal.multiply(BigDecimal.valueOf(rewardPercentage))
                           .divide(BigDecimal.valueOf(100));
        } else if (rewardType == RewardType.DISCOUNT && rewardAmount != null) {
            return rewardAmount;
        }
        return BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public Long getRewardId() {
        return rewardId;
    }
    
    public void setRewardId(Long rewardId) {
        this.rewardId = rewardId;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Referral getReferral() {
        return referral;
    }
    
    public void setReferral(Referral referral) {
        this.referral = referral;
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
    
    public Order getAppliedOrder() {
        return appliedOrder;
    }
    
    public void setAppliedOrder(Order appliedOrder) {
        this.appliedOrder = appliedOrder;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Boolean getIsReferrerReward() {
        return isReferrerReward;
    }
    
    public void setIsReferrerReward(Boolean isReferrerReward) {
        this.isReferrerReward = isReferrerReward;
    }
    
    @Override
    public String toString() {
        return "ReferralReward{" +
                "rewardId=" + rewardId +
                ", rewardType=" + rewardType +
                ", rewardAmount=" + rewardAmount +
                ", rewardPercentage=" + rewardPercentage +
                ", status=" + status +
                ", isReferrerReward=" + isReferrerReward +
                ", createdAt=" + createdAt +
                ", expirationDate=" + expirationDate +
                '}';
    }
}
