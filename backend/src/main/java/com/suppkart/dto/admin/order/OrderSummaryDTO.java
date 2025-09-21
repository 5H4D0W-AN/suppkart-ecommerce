package com.suppkart.dto.admin.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for order summary information in admin interface
 * Used for displaying basic order information in customer management
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDTO {
    
    /**
     * Order ID
     */
    private Long id;
    
    /**
     * Order number
     */
    private String orderNumber;
    
    /**
     * Order status
     */
    private String status;
    
    /**
     * Total order amount
     */
    private BigDecimal totalAmount;
    
    /**
     * Order creation date
     */
    private LocalDateTime createdAt;
    
    /**
     * Number of items in the order
     */
    private Integer itemCount;
    
    /**
     * Customer ID
     */
    private Long customerId;
    
    /**
     * Customer name
     */
    private String customerName;
    
    /**
     * Customer email
     */
    private String customerEmail;
    
    /**
     * Payment method used
     */
    private String paymentMethod;
    
    /**
     * Payment status
     */
    private String paymentStatus;
    
    /**
     * Shipping address city
     */
    private String shippingCity;
    
    /**
     * Shipping address state
     */
    private String shippingState;
    
    /**
     * Order last updated date
     */
    private LocalDateTime updatedAt;
    
    /**
     * Discount amount applied
     */
    private BigDecimal discountAmount;
    
    /**
     * Tax amount
     */
    private BigDecimal taxAmount;
    
    /**
     * Shipping amount
     */
    private BigDecimal shippingAmount;
    
    /**
     * Whether order is urgent/priority
     */
    private Boolean isUrgent;
    
    /**
     * Order notes
     */
    private String notes;
}
