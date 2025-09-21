package com.suppkart.controller;

import com.suppkart.dto.admin.*;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.service.AdminDashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Admin Dashboard operations
 * Provides endpoints for dashboard metrics and analytics
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminDashboardController {

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardController.class);
    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    /**
     * Get dashboard summary metrics
     * 
     * @return Dashboard summary with key metrics
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getDashboardSummary() {
        log.info("Admin requesting dashboard summary");
        
        DashboardSummaryDTO summary = adminDashboardService.getDashboardSummary();
        
        return ResponseEntity.ok(ApiResponse.success(
            "Dashboard summary retrieved successfully", 
            summary
        ));
    }

    /**
     * Get sales metrics with optional date range filter
     * 
     * @param days Number of days to look back (default: 30)
     * @return Sales metrics for the specified period
     */
    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<List<SalesMetricDTO>>> getSalesMetrics(
            @RequestParam(defaultValue = "30") int days) {
        log.info("Admin requesting sales metrics for last {} days", days);
        
        if (days <= 0 || days > 365) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Days parameter must be between 1 and 365", null)
            );
        }
        
        List<SalesMetricDTO> salesMetrics = adminDashboardService.getSalesTrend(days);
        
        return ResponseEntity.ok(ApiResponse.success(
            "Sales metrics retrieved successfully", 
            salesMetrics
        ));
    }

    /**
     * Get order status distribution
     * 
     * @return Count of orders by status
     */
    @GetMapping("/orders/status")
    public ResponseEntity<ApiResponse<List<OrderStatusCountDTO>>> getOrderStatusDistribution() {
        log.info("Admin requesting order status distribution");
        
        List<OrderStatusCountDTO> statusDistribution = adminDashboardService.getOrderStatusDistribution();
        
        return ResponseEntity.ok(ApiResponse.success(
            "Order status distribution retrieved successfully", 
            statusDistribution
        ));
    }

    /**
     * Get top selling products
     * 
     * @param limit Number of products to return (default: 10, max: 50)
     * @return Top selling products with sales data
     */
    @GetMapping("/products/top-selling")
    public ResponseEntity<ApiResponse<List<TopSellingProductDTO>>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Admin requesting top {} selling products", limit);
        
        if (limit <= 0 || limit > 50) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Limit parameter must be between 1 and 50", null)
            );
        }
        
        List<TopSellingProductDTO> topProducts = adminDashboardService.getTopSellingProducts(limit);
        
        return ResponseEntity.ok(ApiResponse.success(
            "Top selling products retrieved successfully", 
            topProducts
        ));
    }

    /**
     * Get products with low stock
     * 
     * @param limit Number of products to return (default: 10, max: 50)
     * @return Products with low stock levels
     */
    @GetMapping("/products/low-stock")
    public ResponseEntity<ApiResponse<List<LowStockProductDTO>>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Admin requesting {} products with low stock", limit);
        
        if (limit <= 0 || limit > 50) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Limit parameter must be between 1 and 50", null)
            );
        }
        
        List<LowStockProductDTO> lowStockProducts = adminDashboardService.getLowStockProducts(limit);
        
        return ResponseEntity.ok(ApiResponse.success(
            "Low stock products retrieved successfully", 
            lowStockProducts
        ));
    }

    /**
     * Get recent orders
     * 
     * @param limit Number of orders to return (default: 10, max: 50)
     * @return Most recent orders
     */
    @GetMapping("/orders/recent")
    public ResponseEntity<ApiResponse<List<RecentOrderDTO>>> getRecentOrders(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Admin requesting {} recent orders", limit);
        
        if (limit <= 0 || limit > 50) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Limit parameter must be between 1 and 50", null)
            );
        }
        
        List<RecentOrderDTO> recentOrders = adminDashboardService.getRecentOrders(limit);
        
        return ResponseEntity.ok(ApiResponse.success(
            "Recent orders retrieved successfully", 
            recentOrders
        ));
    }

    /**
     * Get customer metrics
     * 
     * @return Customer acquisition and retention metrics
     */
    @GetMapping("/customers/metrics")
    public ResponseEntity<ApiResponse<CustomerMetricsDTO>> getCustomerMetrics() {
        log.info("Admin requesting customer metrics");
        
        CustomerMetricsDTO customerMetrics = adminDashboardService.getCustomerMetrics();
        
        return ResponseEntity.ok(ApiResponse.success(
            "Customer metrics retrieved successfully", 
            customerMetrics
        ));
    }
}
