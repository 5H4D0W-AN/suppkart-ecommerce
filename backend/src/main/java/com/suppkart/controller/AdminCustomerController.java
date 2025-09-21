package com.suppkart.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suppkart.dto.admin.customer.CustomerActivityDTO;
import com.suppkart.dto.admin.customer.CustomerDTO;
import com.suppkart.dto.admin.customer.CustomerDetailDTO;
import com.suppkart.dto.admin.customer.CustomerFilterRequest;
import com.suppkart.dto.admin.customer.CustomerSegmentationDTO;
import com.suppkart.dto.admin.customer.CustomerStats;
import com.suppkart.dto.admin.customer.CustomerStatusRequest;
import com.suppkart.dto.admin.order.OrderSummaryDTO;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.service.AdminCustomerService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * Admin Customer Controller
 * Handles all customer management operations for admin users
 * 
 * @author SuppKart Team
 */
@RestController
@RequestMapping("/api/admin/customers")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
@Slf4j
public class AdminCustomerController {

    @Autowired
    private AdminCustomerService adminCustomerService;

    /**
     * Get all customers with filtering and pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CustomerDTO>>> getAllCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) Integer minOrders,
            @RequestParam(required = false) java.math.BigDecimal minSpent,
            @RequestParam(required = false) String customerTier,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Boolean isVerified,
            @RequestParam(required = false) String referralSource,
            @RequestParam(required = false) Boolean hasActiveOrders,
            @RequestParam(required = false) Boolean hasWishlistItems,
            @RequestParam(required = false) Boolean hasReviews,
            @RequestParam(required = false) LocalDateTime lastLoginBefore,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Getting all customers with filters - search: {}, status: {}, page: {}", 
                search, status, pageable.getPageNumber());
        
        CustomerFilterRequest filter = CustomerFilterRequest.builder()
                .search(search)
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .minOrders(minOrders)
                .minSpent(minSpent)
                .customerTier(customerTier)
                .riskLevel(riskLevel)
                .city(city)
                .state(state)
                .country(country)
                .isVerified(isVerified)
                .referralSource(referralSource)
                .hasActiveOrders(hasActiveOrders)
                .hasWishlistItems(hasWishlistItems)
                .hasReviews(hasReviews)
                .lastLoginBefore(lastLoginBefore)
                .build();
        
        Page<CustomerDTO> customers = adminCustomerService.getAllCustomers(filter, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Customers retrieved successfully", customers));
    }

    /**
     * Get customer details by ID
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<CustomerDetailDTO>> getCustomerById(@PathVariable Long customerId) {
        log.info("Getting customer details for ID: {}", customerId);
        
        CustomerDetailDTO customer = adminCustomerService.getCustomerById(customerId);
        
        return ResponseEntity.ok(ApiResponse.success("Customer details retrieved successfully", customer));
    }

    /**
     * Get customer orders
     */
    @GetMapping("/{customerId}/orders")
    public ResponseEntity<ApiResponse<List<OrderSummaryDTO>>> getCustomerOrders(@PathVariable Long customerId) {
        log.info("Getting orders for customer ID: {}", customerId);
        
        List<OrderSummaryDTO> orders = adminCustomerService.getCustomerOrders(customerId);
        
        return ResponseEntity.ok(ApiResponse.success("Customer orders retrieved successfully", orders));
    }

    /**
     * Get customer statistics
     */
    @GetMapping("/{customerId}/stats")
    public ResponseEntity<ApiResponse<CustomerStats>> getCustomerStats(@PathVariable Long customerId) {
        log.info("Getting statistics for customer ID: {}", customerId);
        
        CustomerStats stats = adminCustomerService.getCustomerStats(customerId);
        
        return ResponseEntity.ok(ApiResponse.success("Customer statistics retrieved successfully", stats));
    }

    /**
     * Update customer status
     */
    @PatchMapping("/{customerId}/status")
    public ResponseEntity<ApiResponse<CustomerDetailDTO>> updateCustomerStatus(
            @PathVariable Long customerId,
            @Valid @RequestBody CustomerStatusRequest request) {
        
        log.info("Updating status for customer ID: {} to status: {}", customerId, request.getStatus());
        
        CustomerDetailDTO updatedCustomer = adminCustomerService.updateCustomerStatus(customerId, request);
        
        return ResponseEntity.ok(ApiResponse.success("Customer status updated successfully", updatedCustomer));
    }

    /**
     * Delete customer (GDPR compliance)
     */
    @DeleteMapping("/{customerId}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long customerId) {
        log.info("Deleting customer ID: {}", customerId);
        
        adminCustomerService.deleteCustomer(customerId);
        
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully", null));
    }

    /**
     * Export customer data (GDPR compliance)
     */
    @GetMapping("/{customerId}/export")
    public ResponseEntity<ApiResponse<String>> exportCustomerData(@PathVariable Long customerId) {
        log.info("Exporting data for customer ID: {}", customerId);
        
        adminCustomerService.exportCustomerData(customerId);
        
        return ResponseEntity.ok(ApiResponse.success("Customer data export initiated. You will receive an email with the data.", "Export initiated"));
    }

    /**
     * Get customer activity log
     */
    @GetMapping("/{customerId}/activity")
    public ResponseEntity<ApiResponse<List<CustomerActivityDTO>>> getCustomerActivity(@PathVariable Long customerId) {
        log.info("Getting activity log for customer ID: {}", customerId);
        
        List<CustomerActivityDTO> activities = adminCustomerService.getCustomerActivity(customerId);
        
        return ResponseEntity.ok(ApiResponse.success("Customer activity retrieved successfully", activities));
    }

    /**
     * Get new customers (recently registered)
     */
    @GetMapping("/new")
    public ResponseEntity<ApiResponse<Page<CustomerDTO>>> getNewCustomers(
            @RequestParam(defaultValue = "30") int days,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Getting new customers from last {} days", days);
        
        Page<CustomerDTO> newCustomers = adminCustomerService.getNewCustomers(days, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("New customers retrieved successfully", newCustomers));
    }

    /**
     * Get customer segmentation data
     */
    @GetMapping("/segmentation")
    public ResponseEntity<ApiResponse<CustomerSegmentationDTO>> getCustomerSegmentation() {
        log.info("Getting customer segmentation data");
        
        CustomerSegmentationDTO segmentation = adminCustomerService.getCustomerSegmentation();
        
        return ResponseEntity.ok(ApiResponse.success("Customer segmentation data retrieved successfully", segmentation));
    }

    /**
     * Get customers by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<CustomerDTO>>> getCustomersByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Getting customers with status: {}", status);
        
        CustomerFilterRequest filter = CustomerFilterRequest.builder()
                .status(status)
                .build();
        
        Page<CustomerDTO> customers = adminCustomerService.getAllCustomers(filter, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Customers with status " + status + " retrieved successfully", customers));
    }

    /**
     * Get high-value customers
     */
    @GetMapping("/high-value")
    public ResponseEntity<ApiResponse<Page<CustomerDTO>>> getHighValueCustomers(
            @RequestParam(defaultValue = "10000") java.math.BigDecimal minSpent,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Getting high-value customers with minimum spent: {}", minSpent);
        
        CustomerFilterRequest filter = CustomerFilterRequest.builder()
                .minSpent(minSpent)
                .build();
        
        Page<CustomerDTO> customers = adminCustomerService.getAllCustomers(filter, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("High-value customers retrieved successfully", customers));
    }

    /**
     * Get at-risk customers (inactive for specified days)
     */
    @GetMapping("/at-risk")
    public ResponseEntity<ApiResponse<Page<CustomerDTO>>> getAtRiskCustomers(
            @RequestParam(defaultValue = "90") int inactiveDays,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Getting at-risk customers inactive for {} days", inactiveDays);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(inactiveDays);
        CustomerFilterRequest filter = CustomerFilterRequest.builder()
                .lastLoginBefore(cutoffDate)
                .build();
        
        Page<CustomerDTO> customers = adminCustomerService.getAllCustomers(filter, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("At-risk customers retrieved successfully", customers));
    }

    /**
     * Get customers by location
     */
    @GetMapping("/location")
    public ResponseEntity<ApiResponse<Page<CustomerDTO>>> getCustomersByLocation(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String country,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Getting customers by location - city: {}, state: {}, country: {}", city, state, country);
        
        CustomerFilterRequest filter = CustomerFilterRequest.builder()
                .city(city)
                .state(state)
                .country(country)
                .build();
        
        Page<CustomerDTO> customers = adminCustomerService.getAllCustomers(filter, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Customers by location retrieved successfully", customers));
    }

    /**
     * Search customers by email or phone
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<CustomerDTO>>> searchCustomers(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Searching customers with query: {}", query);
        
        CustomerFilterRequest filter = CustomerFilterRequest.builder()
                .search(query)
                .build();
        
        Page<CustomerDTO> customers = adminCustomerService.getAllCustomers(filter, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Customer search results retrieved successfully", customers));
    }
}
