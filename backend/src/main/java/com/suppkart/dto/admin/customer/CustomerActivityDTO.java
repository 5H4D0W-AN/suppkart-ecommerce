package com.suppkart.dto.admin.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for customer activity tracking in admin interface
 * Represents various customer activities like login, order, review, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerActivityDTO {
    
    private Long id;
    
    private Long customerId;
    
    private String customerName;
    
    private String customerEmail;
    
    /**
     * Type of activity performed by customer
     * Examples: LOGIN, LOGOUT, ORDER_PLACED, ORDER_CANCELLED, REVIEW_SUBMITTED, 
     * PRODUCT_VIEWED, CART_UPDATED, WISHLIST_UPDATED, PROFILE_UPDATED, 
     * PASSWORD_CHANGED, ADDRESS_ADDED, CONSULTATION_BOOKED, SUPPORT_TICKET_CREATED
     */
    private String activityType;
    
    /**
     * Detailed description of the activity
     * Examples: "Logged in from mobile app", "Placed order #12345", 
     * "Added product to wishlist", "Updated shipping address"
     */
    private String detail;
    
    /**
     * Additional context or metadata about the activity
     * Examples: "Order value: $150.00", "Product: Nike Air Max", "Rating: 5 stars"
     */
    private String context;
    
    /**
     * Timestamp when the activity occurred
     */
    private LocalDateTime timestamp;
    
    /**
     * IP address from which the activity was performed
     */
    private String ipAddress;
    
    /**
     * Device information (browser, mobile app, etc.)
     * Examples: "Chrome 91.0", "iOS App 2.1.0", "Android App 1.8.5"
     */
    private String device;
    
    /**
     * User agent string for web activities
     */
    private String userAgent;
    
    /**
     * Geographic location (city, country) if available
     */
    private String location;
    
    /**
     * Session ID for grouping related activities
     */
    private String sessionId;
    
    /**
     * Reference ID for related entities (order ID, product ID, etc.)
     */
    private String referenceId;
    
    /**
     * Reference type (ORDER, PRODUCT, REVIEW, etc.)
     */
    private String referenceType;
    
    /**
     * Activity status (SUCCESS, FAILED, PENDING)
     */
    private String status;
    
    /**
     * Error message if activity failed
     */
    private String errorMessage;
    
    /**
     * Duration of the activity in milliseconds (for timed activities)
     */
    private Long duration;
    
    /**
     * Source of the activity (WEB, MOBILE_APP, API, ADMIN)
     */
    private String source;
    
    /**
     * Priority level of the activity (LOW, MEDIUM, HIGH, CRITICAL)
     */
    private String priority;
    
    /**
     * Tags for categorizing activities
     */
    private String tags;
    
    /**
     * Additional metadata as JSON string
     */
    private String metadata;
    
    // Helper methods
    
    /**
     * Check if activity is security-related
     */
    public Boolean getIsSecurityActivity() {
        if (activityType == null) return false;
        return activityType.contains("LOGIN") || 
               activityType.contains("PASSWORD") || 
               activityType.contains("SECURITY") ||
               activityType.contains("AUTH");
    }
    
    /**
     * Check if activity is order-related
     */
    public Boolean getIsOrderActivity() {
        if (activityType == null) return false;
        return activityType.contains("ORDER") || 
               activityType.contains("CHECKOUT") || 
               activityType.contains("PAYMENT");
    }
    
    /**
     * Check if activity is product-related
     */
    public Boolean getIsProductActivity() {
        if (activityType == null) return false;
        return activityType.contains("PRODUCT") || 
               activityType.contains("CART") || 
               activityType.contains("WISHLIST");
    }
    
    /**
     * Check if activity failed
     */
    public Boolean getIsFailed() {
        return "FAILED".equals(status);
    }
    
    /**
     * Check if activity is high priority
     */
    public Boolean getIsHighPriority() {
        return "HIGH".equals(priority) || "CRITICAL".equals(priority);
    }
    
    /**
     * Get formatted activity description
     */
    public String getFormattedDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(detail != null ? detail : activityType);
        
        if (context != null && !context.isEmpty()) {
            sb.append(" - ").append(context);
        }
        
        return sb.toString();
    }
    
    /**
     * Get activity category based on type
     */
    public String getActivityCategory() {
        if (activityType == null) return "OTHER";
        
        if (getIsSecurityActivity()) return "SECURITY";
        if (getIsOrderActivity()) return "ORDER";
        if (getIsProductActivity()) return "PRODUCT";
        if (activityType.contains("PROFILE") || activityType.contains("ACCOUNT")) return "ACCOUNT";
        if (activityType.contains("SUPPORT") || activityType.contains("CONSULTATION")) return "SUPPORT";
        if (activityType.contains("REVIEW") || activityType.contains("RATING")) return "REVIEW";
        
        return "OTHER";
    }
    
    /**
     * Get display name for activity type
     */
    public String getActivityTypeDisplay() {
        if (activityType == null) return "Unknown";
        
        String result = activityType.replace("_", " ").toLowerCase();
        // Capitalize first letter of each word
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : result.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                sb.append(c);
            } else if (capitalizeNext) {
                sb.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    /**
     * Get time ago string for display
     */
    public String getTimeAgo() {
        if (timestamp == null) return "Unknown";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(timestamp, now).toMinutes();
        
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " minutes ago";
        
        long hours = minutes / 60;
        if (hours < 24) return hours + " hours ago";
        
        long days = hours / 24;
        if (days < 30) return days + " days ago";
        
        long months = days / 30;
        if (months < 12) return months + " months ago";
        
        long years = months / 12;
        return years + " years ago";
    }
    
    /**
     * Get risk level based on activity
     */
    public String getRiskLevel() {
        if (getIsFailed() && getIsSecurityActivity()) return "HIGH";
        if (getIsSecurityActivity()) return "MEDIUM";
        if (getIsFailed()) return "MEDIUM";
        return "LOW";
    }
}
