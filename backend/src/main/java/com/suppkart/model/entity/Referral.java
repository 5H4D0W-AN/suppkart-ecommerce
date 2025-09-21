package com.suppkart.model.entity;

import java.time.LocalDateTime;

import com.suppkart.model.enums.ReferralStatus;

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
@Table(name = "referrals")
public class Referral {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "referral_id")
    private Long referralId;
    
    @Column(name = "referral_code", unique = true, nullable = false, length = 20)
    private String referralCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_user_id", nullable = false)
    private User referrerUser;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referred_user_id")
    private User referredUser;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReferralStatus status = ReferralStatus.UNUSED;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "usage_date")
    private LocalDateTime usageDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_order_id")
    private Order firstOrder;
    
    @Column(name = "first_order_completion_date")
    private LocalDateTime firstOrderCompletionDate;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Referral() {}
    
    public Referral(String referralCode, User referrerUser) {
        this.referralCode = referralCode;
        this.referrerUser = referrerUser;
        this.status = ReferralStatus.UNUSED;
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
    public void markAsUsed(User referredUser) {
        this.referredUser = referredUser;
        this.status = ReferralStatus.USED;
        this.usageDate = LocalDateTime.now();
    }
    
    public void markFirstOrderCompleted(Order order) {
        this.firstOrder = order;
        this.firstOrderCompletionDate = LocalDateTime.now();
        this.status = ReferralStatus.REWARDED;
    }
    
    public boolean isUsed() {
        return status == ReferralStatus.USED || status == ReferralStatus.REWARDED;
    }
    
    public boolean isRewarded() {
        return status == ReferralStatus.REWARDED;
    }
    
    // Getters and Setters
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
    
    public User getReferrerUser() {
        return referrerUser;
    }
    
    public void setReferrerUser(User referrerUser) {
        this.referrerUser = referrerUser;
    }
    
    public User getReferredUser() {
        return referredUser;
    }
    
    public void setReferredUser(User referredUser) {
        this.referredUser = referredUser;
    }
    
    public ReferralStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReferralStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUsageDate() {
        return usageDate;
    }
    
    public void setUsageDate(LocalDateTime usageDate) {
        this.usageDate = usageDate;
    }
    
    public Order getFirstOrder() {
        return firstOrder;
    }
    
    public void setFirstOrder(Order firstOrder) {
        this.firstOrder = firstOrder;
    }
    
    public LocalDateTime getFirstOrderCompletionDate() {
        return firstOrderCompletionDate;
    }
    
    public void setFirstOrderCompletionDate(LocalDateTime firstOrderCompletionDate) {
        this.firstOrderCompletionDate = firstOrderCompletionDate;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "Referral{" +
                "referralId=" + referralId +
                ", referralCode='" + referralCode + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", usageDate=" + usageDate +
                ", firstOrderCompletionDate=" + firstOrderCompletionDate +
                '}';
    }
}
