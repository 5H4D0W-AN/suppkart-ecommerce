package com.suppkart.dto.admin;

import java.math.BigDecimal;

/**
 * DTO for admin dashboard summary metrics
 */
public class DashboardSummaryDTO {
    
    private BigDecimal totalRevenue;
    private int orderCount;
    private int customerCount;
    private BigDecimal averageOrderValue;
    private int lowStockCount;
    private int pendingOrdersCount;
    
    public DashboardSummaryDTO() {}
    
    public DashboardSummaryDTO(BigDecimal totalRevenue, int orderCount, int customerCount, 
                              BigDecimal averageOrderValue, int lowStockCount, int pendingOrdersCount) {
        this.totalRevenue = totalRevenue;
        this.orderCount = orderCount;
        this.customerCount = customerCount;
        this.averageOrderValue = averageOrderValue;
        this.lowStockCount = lowStockCount;
        this.pendingOrdersCount = pendingOrdersCount;
    }
    
    // Getters and Setters
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public int getOrderCount() {
        return orderCount;
    }
    
    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }
    
    public int getCustomerCount() {
        return customerCount;
    }
    
    public void setCustomerCount(int customerCount) {
        this.customerCount = customerCount;
    }
    
    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }
    
    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }
    
    public int getLowStockCount() {
        return lowStockCount;
    }
    
    public void setLowStockCount(int lowStockCount) {
        this.lowStockCount = lowStockCount;
    }
    
    public int getPendingOrdersCount() {
        return pendingOrdersCount;
    }
    
    public void setPendingOrdersCount(int pendingOrdersCount) {
        this.pendingOrdersCount = pendingOrdersCount;
    }
}
