package com.suppkart.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suppkart.dto.request.CheckoutRequest;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.dto.response.OrderResponse;
import com.suppkart.exception.OrderException;
import com.suppkart.model.entity.Order;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.OrderStatus;
import com.suppkart.security.CustomUserDetailsService.UserPrincipal;
import com.suppkart.service.OrderService;
import com.suppkart.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Create order from checkout
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CheckoutRequest checkoutRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("Creating order for user: {}", userPrincipal.getUsername());
        
        try {
            User user = userService.getUserById(userPrincipal.getId());
            Order order = orderService.processCheckout(checkoutRequest, user);
            
            OrderResponse orderResponse = convertToOrderResponse(order);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Order created successfully", orderResponse)
            );
            
        } catch (OrderException e) {
            logger.error("Order error creating order for user {}: {}", 
                        userPrincipal.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            logger.error("Error creating order for user {}: {}", 
                        userPrincipal.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to create order")
            );
        }
    }
    
    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("Getting order {} for user: {}", orderId, userPrincipal.getUsername());
        
        try {
            User user = userService.getUserById(userPrincipal.getId());
            Optional<Order> orderOpt = orderService.getOrderById(orderId, user);
            
            if (orderOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Order not found")
                );
            }
            
            Order order = orderOpt.get();
            OrderResponse orderResponse = convertToOrderResponse(order);
            
            return ResponseEntity.ok(
                ApiResponse.success("Order retrieved successfully", orderResponse)
            );
            
        } catch (OrderException e) {
            logger.error("Order error getting order {} for user {}: {}", 
                        orderId, userPrincipal.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            logger.error("Error getting order {} for user {}: {}", 
                        orderId, userPrincipal.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to retrieve order")
            );
        }
    }
    
    /**
     * Get user's order history with pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getUserOrders(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) OrderStatus status,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("Getting orders for user: {}, page: {}, size: {}", 
                   userPrincipal.getUsername(), page, size);
        
        try {
            User user = userService.getUserById(userPrincipal.getId());
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                       Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Order> orders = orderService.getUserOrders(user, pageable);
            Page<OrderResponse> orderResponses = orders.map(this::convertToOrderResponse);
            
            return ResponseEntity.ok(
                ApiResponse.success("Orders retrieved successfully", orderResponses)
            );
            
        } catch (OrderException e) {
            logger.error("Order error getting orders for user {}: {}", 
                        userPrincipal.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            logger.error("Error getting orders for user {}: {}", 
                        userPrincipal.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to retrieve orders")
            );
        }
    }
    
    /**
     * Cancel order
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("Cancelling order {} for user: {}", orderId, userPrincipal.getUsername());
        
        try {
            User user = userService.getUserById(userPrincipal.getId());
            Order order = orderService.cancelOrder(orderId, user);
            
            OrderResponse orderResponse = convertToOrderResponse(order);
            
            return ResponseEntity.ok(
                ApiResponse.success("Order cancelled successfully", orderResponse)
            );
            
        } catch (OrderException e) {
            logger.error("Order error cancelling order {} for user {}: {}", 
                        orderId, userPrincipal.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            logger.error("Error cancelling order {} for user {}: {}", 
                        orderId, userPrincipal.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to cancel order")
            );
        }
    }
    
    // Helper methods
    
    private OrderResponse convertToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderNumber(order.getOrderNumber());
        response.setOrderStatus(order.getOrderStatus());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setPaymentTransactionId(order.getPaymentTransactionId());
        
        // Shipping address (denormalized fields in Order entity)
        response.setShippingFirstName(order.getShippingFirstName());
        response.setShippingLastName(order.getShippingLastName());
        response.setShippingAddressLine1(order.getShippingAddressLine1());
        response.setShippingAddressLine2(order.getShippingAddressLine2());
        response.setShippingCity(order.getShippingCity());
        response.setShippingState(order.getShippingState());
        response.setShippingPostalCode(order.getShippingPostalCode());
        response.setShippingCountry(order.getShippingCountry());
        response.setShippingPhone(order.getShippingPhone());
        
        // Financial details
        response.setSubtotal(order.getSubtotal());
        response.setTaxAmount(order.getTaxAmount());
        response.setShippingCost(order.getShippingCost());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setTotalAmount(order.getTotalAmount());
        
        // Additional information
        response.setDeliveryInstructions(order.getDeliveryInstructions());
        response.setCouponCode(order.getCouponCode());
        
        // Tracking information
        response.setTrackingNumber(order.getTrackingNumber());
        response.setCarrierName(order.getCarrierName());
        response.setEstimatedDeliveryDate(order.getEstimatedDeliveryDate());
        response.setDeliveredAt(order.getDeliveredAt());
        response.setCancelledAt(order.getCancelledAt());
        response.setCancellationReason(order.getCancellationReason());
        
        // Order items
        if (order.getOrderItems() != null) {
            List<OrderResponse.OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
                    .map(item -> {
                        OrderResponse.OrderItemResponse itemResponse = new OrderResponse.OrderItemResponse();
                        itemResponse.setOrderItemId(item.getOrderItemId());
                        itemResponse.setProductName(item.getProductVariant().getProduct().getName());
                        itemResponse.setVariantName(item.getProductVariant().getName());
                        // For now, skip image URL - will implement after fixing Product entity
                        itemResponse.setQuantity(item.getQuantity());
                        itemResponse.setUnitPrice(item.getUnitPrice());
                        itemResponse.setTotalPrice(item.getTotalPrice());
                        return itemResponse;
                    })
                    .collect(Collectors.toList());
            response.setOrderItems(orderItemResponses);
        }
        
        // Audit fields
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        
        return response;
    }
}
