package com.suppkart.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "reviews")
@EntityListeners(AuditingEntityListener.class)
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @NotNull
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    @Column(name = "rating", nullable = false)
    private Integer rating;
    
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    @Column(name = "title", length = 100)
    private String title;
    
    @Size(max = 2000, message = "Content cannot exceed 2000 characters")
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    // Display name for the review - configurable by admin for admin-submitted reviews
    @Size(max = 50, message = "Display name cannot exceed 50 characters")
    @Column(name = "display_name", length = 50)
    private String displayName;
    
    @Column(name = "verified", nullable = false)
    private Boolean verified = false; // Verified purchase
    
    @Column(name = "approved", nullable = false)
    private Boolean approved = false; // Moderation status
    
    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = true;
    
    @Column(name = "helpful_votes", nullable = false)
    private Integer helpfulVotes = 0;
    
    // Track if review was created by admin (for internal purposes only)
    @Column(name = "created_by_admin", nullable = false)
    private Boolean createdByAdmin = false;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public Review() {}
    
    public Review(Product product, User user, Integer rating) {
        this.product = product;
        this.user = user;
        this.rating = rating;
    }
    
    public Review(Product product, Integer rating, String displayName) {
        this.product = product;
        this.rating = rating;
        this.displayName = displayName;
        this.createdByAdmin = true;
        this.approved = true; // Admin reviews are auto-approved
        this.verified = true; // Admin reviews are marked as verified
    }
    
    // Getters and Setters
    public Long getReviewId() {
        return reviewId;
    }
    
    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
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
    
    public Boolean getVerified() {
        return verified;
    }
    
    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
    
    public Boolean getApproved() {
        return approved;
    }
    
    public void setApproved(Boolean approved) {
        this.approved = approved;
    }
    
    public Boolean getIsVisible() {
        return isVisible;
    }
    
    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
    }
    
    public Integer getHelpfulVotes() {
        return helpfulVotes;
    }
    
    public void setHelpfulVotes(Integer helpfulVotes) {
        this.helpfulVotes = helpfulVotes;
    }
    
    public Boolean getCreatedByAdmin() {
        return createdByAdmin;
    }
    
    public void setCreatedByAdmin(Boolean createdByAdmin) {
        this.createdByAdmin = createdByAdmin;
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
    
    // Utility methods
    public String getUserDisplayName() {
        // If admin set a custom display name, use it
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName;
        }
        
        // For regular user reviews, generate from user info
        if (user != null) {
            String lastName = user.getLastName();
            if (lastName != null && !lastName.isEmpty()) {
                return user.getFirstName() + " " + lastName.charAt(0) + ".";
            }
            return user.getFirstName();
        }
        
        return "Anonymous";
    }
    
    public boolean canBeDisplayed() {
        return approved && isVisible;
    }
    
    public boolean isAdminReview() {
        return createdByAdmin != null && createdByAdmin;
    }
    
    public boolean hasContent() {
        return content != null && !content.trim().isEmpty();
    }
    
    public boolean hasTitle() {
        return title != null && !title.trim().isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return Objects.equals(reviewId, review.reviewId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(reviewId);
    }
    
    @Override
    public String toString() {
        return "Review{" +
                "reviewId=" + reviewId +
                ", rating=" + rating +
                ", title='" + title + '\'' +
                ", verified=" + verified +
                ", approved=" + approved +
                ", createdByAdmin=" + createdByAdmin +
                '}';
    }
}
