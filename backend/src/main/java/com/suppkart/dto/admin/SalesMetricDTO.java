package com.suppkart.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for sales metrics data
 */
public class SalesMetricDTO {
    
    private LocalDate date;
    private BigDecimal revenue;
    private int orderCount;
    
    public SalesMetricDTO() {}
    
    public SalesMetricDTO(LocalDate date, BigDecimal revenue, int orderCount) {
        this.date = date;
        this.revenue = revenue;
        this.orderCount = orderCount;
    }
    
    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public BigDecimal getRevenue() {
        return revenue;
    }
    
    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }
    
    public int getOrderCount() {
        return orderCount;
    }
    
    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }
}
