package com.suppkart.controller;

import com.suppkart.dto.admin.order.*;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.service.AdminOrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Admin Order Management
 * Provides endpoints for order management, status updates, shipment creation, and analytics
 */
@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminOrderController {

    private static final Logger logger = LoggerFactory.getLogger(AdminOrderController.class);

    @Autowired
    private AdminOrderService adminOrderService;

    /**
     * Get all orders with filtering and pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderListItemDTO>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String minAmount,
            @RequestParam(required = false) String maxAmount) {

        try {
            logger.info("Fetching orders with filters - page: {}, size: {}, search: {}, status: {}", 
                       page, size, search, status);

            // Create filter request
            OrderFilterRequest filter = new OrderFilterRequest();
            filter.setSearch(search);
            filter.setStatus(status);
            filter.setPaymentStatus(paymentStatus);
            filter.setStartDate(startDate);
            filter.setEndDate(endDate);
            if (minAmount != null) {
                filter.setMinAmount(new java.math.BigDecimal(minAmount));
            }
            if (maxAmount != null) {
                filter.setMaxAmount(new java.math.BigDecimal(maxAmount));
            }

            // Create pageable
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                       Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<OrderListItemDTO> orders = adminOrderService.getAllOrders(filter, pageable);

            return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));

        } catch (Exception e) {
            logger.error("Error fetching orders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch orders: " + e.getMessage()));
        }
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> getOrderById(@PathVariable Long id) {
        try {
            logger.info("Fetching order details for ID: {}", id);

            OrderDetailDTO order = adminOrderService.getOrderById(id);
            return ResponseEntity.ok(ApiResponse.success("Order details retrieved successfully", order));

        } catch (Exception e) {
            logger.error("Error fetching order {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch order: " + e.getMessage()));
        }
    }

    /**
     * Update order status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String comment) {

        try {
            logger.info("Updating order {} status to: {}", id, status);

            OrderDetailDTO updatedOrder = adminOrderService.updateOrderStatus(id, status, comment);
            return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", updatedOrder));

        } catch (Exception e) {
            logger.error("Error updating order {} status: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update order status: " + e.getMessage()));
        }
    }

    /**
     * Create shipment for order
     */
    @PostMapping("/{id}/shipments")
    public ResponseEntity<ApiResponse<ShipmentResponseDTO>> createShipment(
            @PathVariable Long id,
            @Valid @RequestBody ShipmentRequest request) {

        try {
            logger.info("Creating shipment for order: {}", id);

            ShipmentResponseDTO shipment = adminOrderService.createShipment(id, request);
            return ResponseEntity.ok(ApiResponse.success("Shipment created successfully", shipment));

        } catch (Exception e) {
            logger.error("Error creating shipment for order {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create shipment: " + e.getMessage()));
        }
    }

    /**
     * Update order notes
     */
    @PatchMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> updateOrderNotes(
            @PathVariable Long id,
            @RequestParam String notes) {

        try {
            logger.info("Updating notes for order: {}", id);

            OrderDetailDTO updatedOrder = adminOrderService.updateOrderNotes(id, notes);
            return ResponseEntity.ok(ApiResponse.success("Order notes updated successfully", updatedOrder));

        } catch (Exception e) {
            logger.error("Error updating order {} notes: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update order notes: " + e.getMessage()));
        }
    }

    /**
     * Process refund for order
     */
    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> processRefund(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request) {

        try {
            logger.info("Processing refund for order: {}", id);

            OrderDetailDTO updatedOrder = adminOrderService.processRefund(id, request);
            return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", updatedOrder));

        } catch (Exception e) {
            logger.error("Error processing refund for order {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to process refund: " + e.getMessage()));
        }
    }

    /**
     * Generate and download invoice
     */
    @GetMapping("/{id}/invoice")
    public ResponseEntity<byte[]> generateInvoice(@PathVariable Long id) {
        try {
            logger.info("Generating invoice for order: {}", id);

            byte[] invoicePdf = adminOrderService.generateInvoice(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "invoice-" + id + ".pdf");
            headers.setContentLength(invoicePdf.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(invoicePdf);

        } catch (Exception e) {
            logger.error("Error generating invoice for order {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get order status history
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<OrderStatusHistoryDTO>>> getOrderStatusHistory(@PathVariable Long id) {
        try {
            logger.info("Fetching status history for order: {}", id);

            List<OrderStatusHistoryDTO> history = adminOrderService.getOrderStatusHistory(id);
            return ResponseEntity.ok(ApiResponse.success("Order status history retrieved successfully", history));

        } catch (Exception e) {
            logger.error("Error fetching order {} history: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch order history: " + e.getMessage()));
        }
    }

    /**
     * Get order analytics
     */
    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<List<OrderAnalyticsDTO>>> getOrderAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "day") String groupBy) {

        try {
            logger.info("Fetching order analytics from {} to {} grouped by {}", startDate, endDate, groupBy);

            List<OrderAnalyticsDTO> analytics = adminOrderService.getOrderAnalytics(startDate, endDate, groupBy);
            return ResponseEntity.ok(ApiResponse.success("Order analytics retrieved successfully", analytics));

        } catch (Exception e) {
            logger.error("Error fetching order analytics: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch order analytics: " + e.getMessage()));
        }
    }
}
