package com.suppkart.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FakeReviewRequest {
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;
    
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;
    
    @Size(max = 2000, message = "Content cannot exceed 2000 characters")
    private String content;
    
    @Size(max = 50, message = "Display name cannot exceed 50 characters")
    private String displayName; // Custom display name for the review
    
    private LocalDateTime createdAt; // Optional - will use current time if not provided
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    private String customerName; // Name to display as the reviewer
    
    private Boolean verified = false; // Whether this is a verified purchase review
    
    // Constructors
    public FakeReviewRequest() {}
    
    public FakeReviewRequest(Integer rating, String displayName) {
        this.rating = rating;
        this.displayName = displayName;
    }
    
    public FakeReviewRequest(Integer rating, String title, String content, String displayName) {
        this.rating = rating;
        this.title = title;
        this.content = content;
        this.displayName = displayName;
    }
    
    public FakeReviewRequest(Integer rating, String title, String content, String displayName, LocalDateTime createdAt) {
        this.rating = rating;
        this.title = title;
        this.content = content;
        this.displayName = displayName;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Integer getRating() {
        return rating;
    }
    
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public Boolean getVerified() {
        return verified;
    }
    
    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
    
    @Override
    public String toString() {
        return "FakeReviewRequest{" +
                "rating=" + rating +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", displayName='" + displayName + '\'' +
                ", createdAt=" + createdAt +
                ", productId=" + productId +
                ", customerName='" + customerName + '\'' +
                ", verified=" + verified +
                '}';
    }
}
