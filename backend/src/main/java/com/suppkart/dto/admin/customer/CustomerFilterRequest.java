package com.suppkart.dto.admin.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for filtering customers in admin interface
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFilterRequest {
    
    // Basic search filters
    private String search; // Search by name, email, or phone
    private String status; // Customer status filter
    
    // Date range filters
    private LocalDateTime startDate; // Registration start date
    private LocalDateTime endDate; // Registration end date
    
    // Order-based filters
    private Integer minOrders; // Minimum number of orders
    private Integer maxOrders; // Maximum number of orders
    private BigDecimal minSpent; // Minimum total spent
    private BigDecimal maxSpent; // Maximum total spent
    
    // Customer tier filters
    private String customerTier; // BRONZE, SILVER, GOLD, PLATINUM
    private String riskLevel; // LOW, MEDIUM, HIGH
    private String engagementLevel; // LOW, MEDIUM, HIGH
    
    // Location filters
    private String city;
    private String state;
    private String country;
    
    // Account status filters
    private Boolean isVerified;
    private Boolean isActive;
    private Boolean hasActiveSubscription;
    
    // Activity filters
    private LocalDateTime lastLoginAfter; // Last login after date
    private LocalDateTime lastLoginBefore; // Last login before date
    private LocalDateTime lastOrderAfter; // Last order after date
    private LocalDateTime lastOrderBefore; // Last order before date
    private Boolean hasActiveOrders; // Has active orders
    private Boolean hasWishlistItems; // Has wishlist items
    private Boolean hasReviews; // Has reviews
    
    // Referral filters
    private String referralSource;
    private Boolean hasReferrals; // Has made referrals
    private Integer minReferrals; // Minimum referrals made
    
    // Communication preferences
    private Boolean emailNotifications;
    private Boolean smsNotifications;
    private Boolean marketingEmails;
    
    // Social filters
    private String socialProvider; // Google, Facebook, etc.
    
    // Sorting options
    private String sortBy; // Field to sort by
    private String sortDirection; // ASC or DESC
    
    // Additional filters
    private Boolean isHighValue; // High-value customers
    private Boolean isAtRisk; // At-risk customers (no recent orders)
    private Integer minLoyaltyPoints;
    private Integer maxLoyaltyPoints;
    private Integer minWishlistItems;
    private Integer minReviews;
    private Double minAverageRating;
    
    // Age-based filters (if date of birth is available)
    private Integer minAge;
    private Integer maxAge;
    
    // Gender filter
    private String gender;
    
    // Security filters
    private Boolean twoFactorEnabled;
    private Integer maxLoginAttempts; // Filter customers with failed login attempts
    
    // Helper methods for default values
    public String getSortBy() {
        return sortBy != null ? sortBy : "registrationDate";
    }
    
    public String getSortDirection() {
        return sortDirection != null ? sortDirection : "DESC";
    }
    
    // Helper method to check if any filter is applied
    public boolean hasFilters() {
        return search != null || status != null || startDate != null || endDate != null ||
               minOrders != null || maxOrders != null || minSpent != null || maxSpent != null ||
               customerTier != null || riskLevel != null || engagementLevel != null ||
               city != null || state != null || country != null ||
               isVerified != null || isActive != null || hasActiveSubscription != null ||
               lastLoginAfter != null || lastLoginBefore != null ||
               lastOrderAfter != null || lastOrderBefore != null ||
               referralSource != null || hasReferrals != null || minReferrals != null ||
               emailNotifications != null || smsNotifications != null || marketingEmails != null ||
               socialProvider != null || isHighValue != null || isAtRisk != null ||
               minLoyaltyPoints != null || maxLoyaltyPoints != null ||
               minWishlistItems != null || minReviews != null || minAverageRating != null ||
               minAge != null || maxAge != null || gender != null ||
               twoFactorEnabled != null || maxLoginAttempts != null;
    }
    
    // Helper method to get search terms for database query
    public String getSearchPattern() {
        return search != null ? "%" + search.toLowerCase() + "%" : null;
    }
}
