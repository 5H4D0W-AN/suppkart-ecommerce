package com.suppkart.dto.response;

import java.time.LocalDateTime;

import com.suppkart.model.enums.ReferralStatus;

public class ReferralDto {
    
    private Long referralId;
    private String referralCode;
    private Long referrerUserId;
    private String referrerUserName;
    private Long referredUserId;
    private String referredUserName;
    private ReferralStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime usageDate;
    private Long firstOrderId;
    private LocalDateTime firstOrderCompletionDate;
    
    // Constructors
    public ReferralDto() {}
    
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
    
    public Long getReferrerUserId() {
        return referrerUserId;
    }
    
    public void setReferrerUserId(Long referrerUserId) {
        this.referrerUserId = referrerUserId;
    }
    
    public String getReferrerUserName() {
        return referrerUserName;
    }
    
    public void setReferrerUserName(String referrerUserName) {
        this.referrerUserName = referrerUserName;
    }
    
    public Long getReferredUserId() {
        return referredUserId;
    }
    
    public void setReferredUserId(Long referredUserId) {
        this.referredUserId = referredUserId;
    }
    
    public String getReferredUserName() {
        return referredUserName;
    }
    
    public void setReferredUserName(String referredUserName) {
        this.referredUserName = referredUserName;
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
    
    public Long getFirstOrderId() {
        return firstOrderId;
    }
    
    public void setFirstOrderId(Long firstOrderId) {
        this.firstOrderId = firstOrderId;
    }
    
    public LocalDateTime getFirstOrderCompletionDate() {
        return firstOrderCompletionDate;
    }
    
    public void setFirstOrderCompletionDate(LocalDateTime firstOrderCompletionDate) {
        this.firstOrderCompletionDate = firstOrderCompletionDate;
    }
    
    @Override
    public String toString() {
        return "ReferralDto{" +
                "referralId=" + referralId +
                ", referralCode='" + referralCode + '\'' +
                ", referrerUserId=" + referrerUserId +
                ", referredUserId=" + referredUserId +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", usageDate=" + usageDate +
                '}';
    }
}
