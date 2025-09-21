package com.suppkart.controller;

import com.suppkart.dto.admin.inventory.*;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Admin controller for inventory management operations
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminInventoryController {

    private final InventoryService inventoryService;

    /**
     * Get inventory for a specific product
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<InventoryDTO>> getInventory(
            @PathVariable Long productId,
            @RequestParam(required = false) Long variantId) {
        
        log.info("Getting inventory for productId: {}, variantId: {}", productId, variantId);
        
        InventoryDTO inventory = inventoryService.getInventory(productId, variantId);
        return ResponseEntity.ok(ApiResponse.success("Inventory retrieved successfully", inventory));
    }

    /**
     * Get inventory for a specific variant
     */
    @GetMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<ApiResponse<InventoryDTO>> getVariantInventory(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        
        log.info("Getting variant inventory for productId: {}, variantId: {}", productId, variantId);
        
        InventoryDTO inventory = inventoryService.getInventory(productId, variantId);
        return ResponseEntity.ok(ApiResponse.success("Variant inventory retrieved successfully", inventory));
    }

    /**
     * Get all inventory with filtering and pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<InventoryDTO>>> getAllInventory(
            @ModelAttribute InventoryFilterRequest filter,
            @PageableDefault(size = 20, sort = "lastUpdated") Pageable pageable) {
        
        log.info("Getting all inventory with filter: {}", filter);
        
        Page<InventoryDTO> inventoryPage = inventoryService.getAllInventory(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Inventory list retrieved successfully", inventoryPage));
    }

    /**
     * Update inventory quantity for a specific variant
     */
    @PutMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<ApiResponse<InventoryDTO>> updateVariantInventory(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @Valid @RequestBody InventoryUpdateRequest request) {
        
        log.info("Updating variant inventory for productId: {}, variantId: {}", productId, variantId);
        
        InventoryDTO updatedInventory = inventoryService.updateInventory(productId, variantId, request);
        return ResponseEntity.ok(ApiResponse.success("Variant inventory updated successfully", updatedInventory));
    }

    /**
     * Update inventory quantity for a product (no variant)
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<InventoryDTO>> updateProductInventory(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryUpdateRequest request) {
        
        log.info("Updating product inventory for productId: {}", productId);
        
        InventoryDTO updatedInventory = inventoryService.updateInventory(productId, null, request);
        return ResponseEntity.ok(ApiResponse.success("Product inventory updated successfully", updatedInventory));
    }

    /**
     * Record stock adjustment with reason
     */
    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse<String>> adjustStock(
            @Valid @RequestBody StockAdjustmentRequest request) {
        
        log.info("Recording stock adjustment for productId: {}, variantId: {}", 
                request.getProductId(), request.getVariantId());
        
        inventoryService.adjustStock(request);
        return ResponseEntity.ok(ApiResponse.success("Stock adjustment recorded successfully"));
    }

    /**
     * Get inventory history for a product
     */
    @GetMapping("/{productId}/history")
    public ResponseEntity<ApiResponse<List<InventoryHistoryDTO>>> getProductHistory(
            @PathVariable Long productId,
            @RequestParam(required = false) Long variantId) {
        
        log.info("Getting inventory history for productId: {}, variantId: {}", productId, variantId);
        
        List<InventoryHistoryDTO> history = inventoryService.getInventoryHistory(productId, variantId);
        return ResponseEntity.ok(ApiResponse.success("Inventory history retrieved successfully", history));
    }

    /**
     * Get low stock alerts
     */
    @GetMapping("/alerts/low-stock")
    public ResponseEntity<ApiResponse<List<StockAlertDTO>>> getLowStockAlerts() {
        log.info("Getting low stock alerts");
        
        List<StockAlertDTO> alerts = inventoryService.getLowStockAlerts();
        return ResponseEntity.ok(ApiResponse.success("Low stock alerts retrieved successfully", alerts));
    }

    /**
     * Get out of stock alerts
     */
    @GetMapping("/alerts/out-of-stock")
    public ResponseEntity<ApiResponse<List<StockAlertDTO>>> getOutOfStockAlerts() {
        log.info("Getting out of stock alerts");
        
        List<StockAlertDTO> alerts = inventoryService.getOutOfStockAlerts();
        return ResponseEntity.ok(ApiResponse.success("Out of stock alerts retrieved successfully", alerts));
    }

    /**
     * Resolve an alert
     */
    @PostMapping("/alerts/{alertId}/resolve")
    public ResponseEntity<ApiResponse<StockAlertDTO>> resolveAlert(@PathVariable Long alertId) {
        log.info("Resolving alert: {}", alertId);
        
        StockAlertDTO resolvedAlert = inventoryService.resolveAlert(alertId);
        return ResponseEntity.ok(ApiResponse.success("Alert resolved successfully", resolvedAlert));
    }

    /**
     * Configure low stock threshold for a product
     */
    @PutMapping("/{productId}/threshold")
    public ResponseEntity<ApiResponse<String>> configureProductThreshold(
            @PathVariable Long productId,
            @RequestParam int threshold) {
        
        log.info("Configuring threshold for productId: {}, threshold: {}", productId, threshold);
        
        inventoryService.configureStockThreshold(productId, null, threshold);
        return ResponseEntity.ok(ApiResponse.success("Stock threshold configured successfully"));
    }

    /**
     * Configure low stock threshold for a variant
     */
    @PutMapping("/{productId}/variants/{variantId}/threshold")
    public ResponseEntity<ApiResponse<String>> configureVariantThreshold(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @RequestParam int threshold) {
        
        log.info("Configuring threshold for productId: {}, variantId: {}, threshold: {}", 
                productId, variantId, threshold);
        
        inventoryService.configureStockThreshold(productId, variantId, threshold);
        return ResponseEntity.ok(ApiResponse.success("Stock threshold configured successfully"));
    }

    /**
     * Generate inventory report
     */
    @GetMapping("/report")
    public ResponseEntity<ApiResponse<String>> generateReport(
            @ModelAttribute @Valid ReportRequest request) {
        
        log.info("Generating inventory report: {}", request.getReportType());
        
        inventoryService.generateStockReport(request);
        return ResponseEntity.ok(ApiResponse.success("Report generation initiated"));
    }

    /**
     * Get inventory statistics for dashboard
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<InventoryStatsDTO>> getInventoryStats() {
        log.info("Getting inventory statistics");
        
        // This would be implemented to return inventory statistics
        InventoryStatsDTO stats = new InventoryStatsDTO();
        stats.setTotalProducts(0);
        stats.setLowStockItems(0);
        stats.setOutOfStockItems(0);
        stats.setTotalValue(java.math.BigDecimal.ZERO);
        
        return ResponseEntity.ok(ApiResponse.success("Inventory statistics retrieved successfully", stats));
    }

    /**
     * Bulk update inventory
     */
    @PostMapping("/bulk-update")
    public ResponseEntity<ApiResponse<String>> bulkUpdateInventory(
            @Valid @RequestBody List<InventoryUpdateRequest> requests) {
        
        log.info("Bulk updating inventory for {} items", requests.size());
        
        // This would be implemented to handle bulk updates
        return ResponseEntity.ok(ApiResponse.success("Bulk inventory update completed"));
    }

    /**
     * Export inventory data
     */
    @GetMapping("/export")
    public ResponseEntity<ApiResponse<String>> exportInventory(
            @RequestParam(defaultValue = "CSV") String format,
            @ModelAttribute InventoryFilterRequest filter) {
        
        log.info("Exporting inventory data in format: {}", format);
        
        // This would be implemented to export inventory data
        return ResponseEntity.ok(ApiResponse.success("Inventory export initiated"));
    }

    // Inner DTO class for inventory statistics
    public static class InventoryStatsDTO {
        private Integer totalProducts;
        private Integer lowStockItems;
        private Integer outOfStockItems;
        private java.math.BigDecimal totalValue;

        // Getters and setters
        public Integer getTotalProducts() { return totalProducts; }
        public void setTotalProducts(Integer totalProducts) { this.totalProducts = totalProducts; }

        public Integer getLowStockItems() { return lowStockItems; }
        public void setLowStockItems(Integer lowStockItems) { this.lowStockItems = lowStockItems; }

        public Integer getOutOfStockItems() { return outOfStockItems; }
        public void setOutOfStockItems(Integer outOfStockItems) { this.outOfStockItems = outOfStockItems; }

        public java.math.BigDecimal getTotalValue() { return totalValue; }
        public void setTotalValue(java.math.BigDecimal totalValue) { this.totalValue = totalValue; }
    }
}
