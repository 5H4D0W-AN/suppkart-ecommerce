package com.suppkart.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suppkart.dto.admin.order.OrderStatusHistoryDTO;
import com.suppkart.service.OrderTrackingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for customer order tracking functionality
 */
@Slf4j
@RestController
@RequestMapping("/api/orders/tracking")
@RequiredArgsConstructor
public class OrderTrackingController {

    private final OrderTrackingService orderTrackingService;

    /**
     * Get order tracking history by order ID
     */
    @GetMapping("/{orderId}/history")
    public ResponseEntity<List<OrderStatusHistoryDTO>> getOrderTrackingHistory(
            @PathVariable Long orderId) {
        
        log.info("Getting tracking history for order: {}", orderId);
        List<OrderStatusHistoryDTO> history = orderTrackingService.getOrderTrackingHistory(orderId);
        return ResponseEntity.ok(history);
    }

    /**
     * Get order tracking history by order number
     */
    @GetMapping("/by-order-number")
    public ResponseEntity<List<OrderStatusHistoryDTO>> getOrderTrackingHistoryByOrderNumber(
            @RequestParam String orderNumber) {
        
        log.info("Getting tracking history for order number: {}", orderNumber);
        List<OrderStatusHistoryDTO> history = orderTrackingService.getOrderTrackingHistoryByOrderNumber(orderNumber);
        return ResponseEntity.ok(history);
    }

    /**
     * Get current order status by order ID
     */
    @GetMapping("/{orderId}/current-status")
    public ResponseEntity<OrderStatusHistoryDTO> getCurrentOrderStatus(
            @PathVariable Long orderId) {
        
        log.info("Getting current status for order: {}", orderId);
        OrderStatusHistoryDTO currentStatus = orderTrackingService.getCurrentOrderStatus(orderId);
        return ResponseEntity.ok(currentStatus);
    }

    /**
     * Get current order status by order number
     */
    @GetMapping("/current-status")
    public ResponseEntity<OrderStatusHistoryDTO> getCurrentOrderStatusByOrderNumber(
            @RequestParam String orderNumber) {
        
        log.info("Getting current status for order number: {}", orderNumber);
        OrderStatusHistoryDTO currentStatus = orderTrackingService.getCurrentOrderStatusByOrderNumber(orderNumber);
        return ResponseEntity.ok(currentStatus);
    }
}