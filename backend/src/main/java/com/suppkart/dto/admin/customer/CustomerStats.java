package com.suppkart.dto.admin.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for customer statistics in admin interface
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStats {
    
    // Order Statistics
    private Integer totalOrders;
    private BigDecimal totalSpent;
    private BigDecimal averageOrderValue;
    private LocalDateTime firstOrderDate;
    private LocalDateTime lastOrderDate;
    
    // Purchase Behavior
    private List<String> favoriteCategories;
    private List<String> favoriteBrands;
    private Double purchaseFrequency; // orders per month
    private Integer daysSinceLastOrder;
    private Integer daysSinceFirstOrder;
    
    // Product Preferences
    private String mostPurchasedProduct;
    private Integer mostPurchasedProductCount;
    private BigDecimal averageProductPrice;
    private String preferredPaymentMethod;
    private String preferredShippingMethod;
    
    // Engagement Metrics
    private Integer wishlistItemCount;
    private Integer reviewCount;
    private Double averageRating;
    private Integer consultationCount;
    private Integer supportTicketCount;
    
    // Financial Metrics
    private BigDecimal lifetimeValue;
    private BigDecimal monthlySpendingAverage;
    private BigDecimal yearlySpendingAverage;
    private Integer refundCount;
    private BigDecimal refundAmount;
    private BigDecimal outstandingBalance;
    
    // Loyalty Metrics
    private Integer loyaltyPoints;
    private String customerTier;
    private Integer referralCount;
    private BigDecimal referralEarnings;
    private Boolean hasActiveSubscription;
    
    // Activity Metrics
    private LocalDateTime lastLoginDate;
    private Integer loginCount;
    private Integer pageViews;
    private Integer cartAbandonmentCount;
    private Double conversionRate;
    
    // Geographic Data
    private String primaryShippingCity;
    private String primaryShippingState;
    private String primaryShippingCountry;
    private Integer addressCount;
    
    // Risk Assessment
    private String riskLevel; // LOW, MEDIUM, HIGH
    private Integer chargebackCount;
    private Integer disputeCount;
    private Boolean hasPaymentIssues;
    private Integer failedPaymentAttempts;
    
    // Seasonal Patterns
    private Map<String, BigDecimal> monthlySpending; // Month -> Amount
    private Map<String, Integer> categoryPreferences; // Category -> Order Count
    private Map<String, Integer> brandPreferences; // Brand -> Order Count
    
    // Communication Preferences
    private Boolean emailNotifications;
    private Boolean smsNotifications;
    private Boolean marketingEmails;
    private Boolean orderUpdates;
    private String preferredLanguage;
    private String timezone;
    
    // Social Metrics
    private String socialProvider;
    private Boolean isInfluencer;
    private Integer socialFollowers;
    private Integer socialShares;
    
    // Helper methods for computed values
    public BigDecimal getAverageOrderValue() {
        if (totalOrders != null && totalOrders > 0 && totalSpent != null) {
            return totalSpent.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    public Integer getDaysSinceLastOrder() {
        if (lastOrderDate != null) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(lastOrderDate, LocalDateTime.now());
        }
        return null;
    }
    
    public Integer getDaysSinceFirstOrder() {
        if (firstOrderDate != null) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(firstOrderDate, LocalDateTime.now());
        }
        return null;
    }
    
    public String getRiskLevel() {
        if (chargebackCount != null && chargebackCount > 2) return "HIGH";
        if (disputeCount != null && disputeCount > 1) return "HIGH";
        if (failedPaymentAttempts != null && failedPaymentAttempts > 5) return "MEDIUM";
        if (refundCount != null && refundCount > 3) return "MEDIUM";
        return "LOW";
    }
    
    public String getCustomerTier() {
        if (totalSpent == null) return "BRONZE";
        
        BigDecimal spent = totalSpent;
        if (spent.compareTo(BigDecimal.valueOf(10000)) >= 0) return "PLATINUM";
        if (spent.compareTo(BigDecimal.valueOf(5000)) >= 0) return "GOLD";
        if (spent.compareTo(BigDecimal.valueOf(1000)) >= 0) return "SILVER";
        return "BRONZE";
    }
    
    public Boolean getIsHighValue() {
        return totalSpent != null && totalSpent.compareTo(BigDecimal.valueOf(5000)) >= 0;
    }
    
    public Boolean getIsAtRisk() {
        return "HIGH".equals(getRiskLevel()) || 
               (daysSinceLastOrder != null && daysSinceLastOrder > 90) ||
               (cartAbandonmentCount != null && cartAbandonmentCount > 5);
    }
    
    public String getEngagementLevel() {
        int score = 0;
        if (reviewCount != null && reviewCount > 5) score += 2;
        if (wishlistItemCount != null && wishlistItemCount > 10) score += 1;
        if (consultationCount != null && consultationCount > 0) score += 2;
        if (loginCount != null && loginCount > 20) score += 1;
        if (referralCount != null && referralCount > 0) score += 2;
        
        if (score >= 6) return "HIGH";
        if (score >= 3) return "MEDIUM";
        return "LOW";
    }
    
    public Double getPurchaseFrequency() {
        if (totalOrders != null && daysSinceFirstOrder != null && daysSinceFirstOrder > 0) {
            return (totalOrders.doubleValue() / daysSinceFirstOrder) * 30; // orders per month
        }
        return 0.0;
    }
    
    public Double getConversionRate() {
        if (pageViews != null && pageViews > 0 && totalOrders != null) {
            return (totalOrders.doubleValue() / pageViews) * 100;
        }
        return 0.0;
    }
}
