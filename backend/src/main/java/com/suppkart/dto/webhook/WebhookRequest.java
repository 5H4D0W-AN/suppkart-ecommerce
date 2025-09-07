package com.suppkart.dto.webhook;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Base webhook request DTO for handling payment webhook events
 */
public class WebhookRequest {
    
    @NotBlank
    private String event;
    
    @NotNull
    @JsonProperty("account_id")
    private String accountId;
    
    @NotNull
    private Map<String, Object> payload;
    
    @JsonProperty("created_at")
    private Long createdAt;
    
    private String signature;
    
    // Constructors
    public WebhookRequest() {}
    
    public WebhookRequest(String event, String accountId, Map<String, Object> payload) {
        this.event = event;
        this.accountId = accountId;
        this.payload = payload;
    }
    
    // Getters and Setters
    public String getEvent() {
        return event;
    }
    
    public void setEvent(String event) {
        this.event = event;
    }
    
    public String getAccountId() {
        return accountId;
    }
    
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    
    public Map<String, Object> getPayload() {
        return payload;
    }
    
    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
    
    public Long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }
    
    @Override
    public String toString() {
        return "WebhookRequest{" +
                "event='" + event + '\'' +
                ", accountId='" + accountId + '\'' +
                ", createdAt=" + createdAt +
                ", signature='" + signature + '\'' +
                '}';
    }
}
