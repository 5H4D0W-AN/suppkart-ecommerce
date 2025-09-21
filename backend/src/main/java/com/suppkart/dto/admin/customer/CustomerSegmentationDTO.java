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
 * DTO for customer segmentation analytics in admin interface
 * Provides comprehensive customer segmentation data for business intelligence
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSegmentationDTO {
    
    /**
     * Total number of customers
     */
    private Integer totalCustomers;
    
    /**
     * Number of new customers (registered in last 30 days)
     */
    private Integer newCustomers;
    
    /**
     * Number of returning customers (made more than one order)
     */
    private Integer returningCustomers;
    
    /**
     * Number of loyal customers (made 5+ orders or spent $500+)
     */
    private Integer loyalCustomers;
    
    /**
     * Number of inactive customers (no activity in last 90 days)
     */
    private Integer inactiveCustomers;
    
    /**
     * Number of at-risk customers (showing declining engagement)
     */
    private Integer atRiskCustomers;
    
    /**
     * Number of high-value customers (top 20% by spending)
     */
    private Integer highValueCustomers;
    
    /**
     * Number of VIP customers (top 5% by spending)
     */
    private Integer vipCustomers;
    
    /**
     * Customer segmentation by spending tiers
     * Key: spending tier (e.g., "0-100", "100-500", "500-1000", "1000+")
     * Value: number of customers in that tier
     */
    private Map<String, Integer> bySpendingTiers;
    
    /**
     * Customer segmentation by order frequency
     * Key: frequency tier (e.g., "1", "2-5", "6-10", "10+")
     * Value: number of customers in that tier
     */
    private Map<String, Integer> byOrderFrequency;
    
    /**
     * Customer segmentation by registration period
     * Key: time period (e.g., "Last 7 days", "Last 30 days", "Last 90 days", "Older")
     * Value: number of customers registered in that period
     */
    private Map<String, Integer> byRegistrationPeriod;
    
    /**
     * Customer segmentation by geographic location
     * Key: location (city, state, or country)
     * Value: number of customers in that location
     */
    private Map<String, Integer> byLocation;
    
    /**
     * Customer segmentation by age group
     * Key: age range (e.g., "18-25", "26-35", "36-45", "46-55", "55+")
     * Value: number of customers in that age group
     */
    private Map<String, Integer> byAgeGroup;
    
    /**
     * Customer segmentation by gender
     * Key: gender (Male, Female, Other, Not Specified)
     * Value: number of customers
     */
    private Map<String, Integer> byGender;
    
    /**
     * Customer segmentation by customer tier
     * Key: tier (Bronze, Silver, Gold, Platinum, Diamond)
     * Value: number of customers in that tier
     */
    private Map<String, Integer> byCustomerTier;
    
    /**
     * Customer segmentation by engagement level
     * Key: engagement level (High, Medium, Low, Inactive)
     * Value: number of customers
     */
    private Map<String, Integer> byEngagementLevel;
    
    /**
     * Customer segmentation by acquisition channel
     * Key: channel (Organic, Social Media, Email, Referral, Paid Ads, etc.)
     * Value: number of customers acquired through that channel
     */
    private Map<String, Integer> byAcquisitionChannel;
    
    /**
     * Customer segmentation by device preference
     * Key: device type (Mobile, Desktop, Tablet)
     * Value: number of customers primarily using that device
     */
    private Map<String, Integer> byDevicePreference;
    
    /**
     * Customer segmentation by payment method preference
     * Key: payment method (Credit Card, Debit Card, UPI, COD, etc.)
     * Value: number of customers preferring that method
     */
    private Map<String, Integer> byPaymentMethod;
    
    /**
     * Customer segmentation by product category preference
     * Key: category name
     * Value: number of customers who primarily purchase from that category
     */
    private Map<String, Integer> byCategoryPreference;
    
    /**
     * Customer segmentation by brand preference
     * Key: brand name
     * Value: number of customers who primarily purchase that brand
     */
    private Map<String, Integer> byBrandPreference;
    
    /**
     * Customer segmentation by subscription status
     * Key: subscription status (Active, Expired, Never Subscribed)
     * Value: number of customers
     */
    private Map<String, Integer> bySubscriptionStatus;
    
    /**
     * Customer segmentation by loyalty program participation
     * Key: participation status (Active Member, Inactive Member, Not Enrolled)
     * Value: number of customers
     */
    private Map<String, Integer> byLoyaltyProgram;
    
    /**
     * Customer segmentation by review activity
     * Key: review activity level (Active Reviewer, Occasional Reviewer, Non-Reviewer)
     * Value: number of customers
     */
    private Map<String, Integer> byReviewActivity;
    
    /**
     * Customer segmentation by support interaction
     * Key: support interaction level (High, Medium, Low, None)
     * Value: number of customers
     */
    private Map<String, Integer> bySupportInteraction;
    
    /**
     * Customer segmentation by risk level
     * Key: risk level (High Risk, Medium Risk, Low Risk)
     * Value: number of customers
     */
    private Map<String, Integer> byRiskLevel;
    
    /**
     * Customer lifetime value distribution
     * Key: CLV range (e.g., "0-100", "100-500", "500-1000", "1000+")
     * Value: number of customers in that CLV range
     */
    private Map<String, Integer> byLifetimeValue;
    
    /**
     * Customer segmentation by seasonal activity
     * Key: season (Spring, Summer, Fall, Winter)
     * Value: number of customers most active in that season
     */
    private Map<String, Integer> bySeasonalActivity;
    
    /**
     * Customer churn risk analysis
     * Key: churn risk level (High, Medium, Low)
     * Value: number of customers at that risk level
     */
    private Map<String, Integer> byChurnRisk;
    
    /**
     * Growth metrics
     */
    private GrowthMetrics growthMetrics;
    
    /**
     * Retention metrics
     */
    private RetentionMetrics retentionMetrics;
    
    /**
     * Engagement metrics
     */
    private EngagementMetrics engagementMetrics;
    
    /**
     * Revenue metrics by segment
     */
    private RevenueMetrics revenueMetrics;
    
    /**
     * Timestamp when this segmentation data was generated
     */
    private LocalDateTime generatedAt;
    
    /**
     * Period for which this segmentation data is calculated
     */
    private String reportPeriod;
    
    // Nested DTOs for detailed metrics
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrowthMetrics {
        private Integer newCustomersThisMonth;
        private Integer newCustomersLastMonth;
        private Double growthRate;
        private Integer newCustomersThisWeek;
        private Integer newCustomersLastWeek;
        private Double weeklyGrowthRate;
        private List<MonthlyGrowth> monthlyGrowthTrend;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetentionMetrics {
        private Double overallRetentionRate;
        private Double monthlyRetentionRate;
        private Double quarterlyRetentionRate;
        private Double yearlyRetentionRate;
        private Map<String, Double> retentionBySegment;
        private List<CohortRetention> cohortAnalysis;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EngagementMetrics {
        private Double averageSessionDuration;
        private Double averagePageViews;
        private Double bounceRate;
        private Double conversionRate;
        private Map<String, Double> engagementBySegment;
        private List<EngagementTrend> engagementTrends;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueMetrics {
        private BigDecimal totalRevenue;
        private BigDecimal averageOrderValue;
        private BigDecimal customerLifetimeValue;
        private Map<String, BigDecimal> revenueBySegment;
        private Map<String, BigDecimal> aovBySegment;
        private Map<String, BigDecimal> clvBySegment;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyGrowth {
        private String month;
        private Integer newCustomers;
        private Double growthRate;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CohortRetention {
        private String cohort;
        private LocalDateTime cohortDate;
        private Integer initialSize;
        private Map<String, Double> retentionRates; // Key: period (1 month, 3 months, etc.)
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EngagementTrend {
        private String period;
        private Double engagementScore;
        private Integer activeUsers;
        private Double conversionRate;
    }
    
    // Helper methods
    
    /**
     * Get percentage of new customers
     */
    public Double getNewCustomerPercentage() {
        if (totalCustomers == null || totalCustomers == 0) return 0.0;
        return (newCustomers != null ? newCustomers : 0) * 100.0 / totalCustomers;
    }
    
    /**
     * Get percentage of returning customers
     */
    public Double getReturningCustomerPercentage() {
        if (totalCustomers == null || totalCustomers == 0) return 0.0;
        return (returningCustomers != null ? returningCustomers : 0) * 100.0 / totalCustomers;
    }
    
    /**
     * Get percentage of loyal customers
     */
    public Double getLoyalCustomerPercentage() {
        if (totalCustomers == null || totalCustomers == 0) return 0.0;
        return (loyalCustomers != null ? loyalCustomers : 0) * 100.0 / totalCustomers;
    }
    
    /**
     * Get percentage of inactive customers
     */
    public Double getInactiveCustomerPercentage() {
        if (totalCustomers == null || totalCustomers == 0) return 0.0;
        return (inactiveCustomers != null ? inactiveCustomers : 0) * 100.0 / totalCustomers;
    }
    
    /**
     * Get percentage of high-value customers
     */
    public Double getHighValueCustomerPercentage() {
        if (totalCustomers == null || totalCustomers == 0) return 0.0;
        return (highValueCustomers != null ? highValueCustomers : 0) * 100.0 / totalCustomers;
    }
    
    /**
     * Get customer health score (0-100)
     */
    public Double getCustomerHealthScore() {
        if (totalCustomers == null || totalCustomers == 0) return 0.0;
        
        double loyaltyScore = getLoyalCustomerPercentage() * 0.3;
        double retentionScore = (100.0 - getInactiveCustomerPercentage()) * 0.3;
        double valueScore = getHighValueCustomerPercentage() * 0.2;
        double growthScore = getNewCustomerPercentage() * 0.2;
        
        return Math.min(100.0, loyaltyScore + retentionScore + valueScore + growthScore);
    }
    
    /**
     * Get the dominant spending tier
     */
    public String getDominantSpendingTier() {
        if (bySpendingTiers == null || bySpendingTiers.isEmpty()) return "Unknown";
        
        return bySpendingTiers.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }
    
    /**
     * Get the dominant acquisition channel
     */
    public String getDominantAcquisitionChannel() {
        if (byAcquisitionChannel == null || byAcquisitionChannel.isEmpty()) return "Unknown";
        
        return byAcquisitionChannel.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }
    
    /**
     * Get customer distribution summary
     */
    public String getDistributionSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (totalCustomers != null && totalCustomers > 0) {
            summary.append(String.format("Total: %d customers", totalCustomers));
            
            if (newCustomers != null && newCustomers > 0) {
                summary.append(String.format(", New: %d (%.1f%%)", 
                    newCustomers, getNewCustomerPercentage()));
            }
            
            if (loyalCustomers != null && loyalCustomers > 0) {
                summary.append(String.format(", Loyal: %d (%.1f%%)", 
                    loyalCustomers, getLoyalCustomerPercentage()));
            }
            
            if (inactiveCustomers != null && inactiveCustomers > 0) {
                summary.append(String.format(", Inactive: %d (%.1f%%)", 
                    inactiveCustomers, getInactiveCustomerPercentage()));
            }
        }
        
        return summary.toString();
    }
    
    /**
     * Check if segmentation data is recent (generated within last 24 hours)
     */
    public Boolean getIsDataRecent() {
        if (generatedAt == null) return false;
        return generatedAt.isAfter(LocalDateTime.now().minusHours(24));
    }
    
    /**
     * Get data freshness indicator
     */
    public String getDataFreshness() {
        if (generatedAt == null) return "Unknown";
        
        LocalDateTime now = LocalDateTime.now();
        long hours = java.time.Duration.between(generatedAt, now).toHours();
        
        if (hours < 1) return "Fresh (< 1 hour)";
        if (hours < 24) return String.format("Recent (%d hours ago)", hours);
        
        long days = hours / 24;
        if (days < 7) return String.format("Stale (%d days ago)", days);
        
        return "Very Stale (> 1 week)";
    }
}
