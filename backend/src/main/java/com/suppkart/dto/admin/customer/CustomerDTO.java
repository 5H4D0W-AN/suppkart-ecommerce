package com.suppkart.dto.admin.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for customer list view in admin panel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    
    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime registrationDate;
    private String status;
    private Integer orderCount;
    private BigDecimal totalSpent;
    private LocalDateTime lastOrderDate;
    
    // Additional fields for enhanced customer management
    private String profileImage;
    private String city;
    private String state;
    private String country;
    private Boolean isVerified;
    private String referralSource;
    private Integer loyaltyPoints;
    private String customerTier; // BRONZE, SILVER, GOLD, PLATINUM
    private LocalDateTime lastLoginDate;
    private Boolean isActive;
    private String preferredLanguage;
    private String timezone;
    
    // Computed fields
    private BigDecimal averageOrderValue;
    private Integer daysAsCustomer;
    private String riskLevel; // LOW, MEDIUM, HIGH
    private Boolean hasActiveSubscription;
    private Integer wishlistItemCount;
    private Integer reviewCount;
    private Double averageRating;
}
