package com.suppkart.dto.admin.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.suppkart.dto.admin.order.AddressDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for detailed customer information in admin interface
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailDTO {

    // Core customer information
    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime registrationDate;
    private String status;

    // Address information
    private List<AddressDTO> addresses;

    // Order statistics
    private Integer orderCount;
    private BigDecimal totalSpent;
    private LocalDateTime lastOrderDate;

    // Additional customer details
    private String referralSource;
    private String notes;

    // Enhanced customer information
    private String profileImage;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String gender;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private Boolean isVerified;
    private Boolean isActive;
    private String preferredLanguage;
    private String timezone;

    // Customer analytics
    private BigDecimal averageOrderValue;
    private Integer daysAsCustomer;
    private String customerTier;
    private Integer loyaltyPoints;
    private LocalDateTime lastLoginDate;
    private String riskLevel;
    private Boolean hasActiveSubscription;

    // Engagement metrics
    private Integer wishlistItemCount;
    private Integer reviewCount;
    private Double averageRating;
    private Integer consultationCount;
    private Integer supportTicketCount;

    // Communication preferences
    private Boolean emailNotifications;
    private Boolean smsNotifications;
    private Boolean marketingEmails;
    private Boolean orderUpdates;

    // Account security
    private Boolean twoFactorEnabled;
    private LocalDateTime passwordLastChanged;
    private Integer loginAttempts;
    private LocalDateTime lastFailedLogin;

    // Social information
    private String socialProvider;
    private String socialId;
    private Integer referralCount;
    private BigDecimal referralEarnings;

    // Computed fields
    public BigDecimal getAverageOrderValue() {
        if (orderCount == null || orderCount == 0 || totalSpent == null) {
            return BigDecimal.ZERO;
        }
        return totalSpent.divide(BigDecimal.valueOf(orderCount), 2, BigDecimal.ROUND_HALF_UP);
    }

    public Integer getDaysAsCustomer() {
        if (registrationDate == null) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(registrationDate.toLocalDate(), LocalDateTime.now().toLocalDate());
    }

    public String getRiskLevel() {
        if (totalSpent == null) {
            return "UNKNOWN";
        }

        if (totalSpent.compareTo(BigDecimal.valueOf(10000)) > 0) {
            return "LOW";
        } else if (totalSpent.compareTo(BigDecimal.valueOf(1000)) > 0) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }
    }

    public String getCustomerTier() {
        if (totalSpent == null) {
            return "BRONZE";
        }

        if (totalSpent.compareTo(BigDecimal.valueOf(50000)) > 0) {
            return "PLATINUM";
        } else if (totalSpent.compareTo(BigDecimal.valueOf(20000)) > 0) {
            return "GOLD";
        } else if (totalSpent.compareTo(BigDecimal.valueOf(5000)) > 0) {
            return "SILVER";
        } else {
            return "BRONZE";
        }
    }

    public Boolean getIsHighValue() {
        return totalSpent != null && totalSpent.compareTo(BigDecimal.valueOf(10000)) > 0;
    }

    public Boolean getIsAtRisk() {
        if (lastOrderDate == null) {
            return true;
        }

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return lastOrderDate.isBefore(thirtyDaysAgo);
    }

    public String getEngagementLevel() {
        int score = 0;

        if (orderCount != null && orderCount > 5) {
            score += 2;
        }
        if (reviewCount != null && reviewCount > 3) {
            score += 1;
        }
        if (wishlistItemCount != null && wishlistItemCount > 5) {
            score += 1;
        }
        if (consultationCount != null && consultationCount > 0) {
            score += 1;
        }

        if (score >= 4) {
            return "HIGH";
        }
        if (score >= 2) {
            return "MEDIUM";
        }
        return "LOW";
    }
}
