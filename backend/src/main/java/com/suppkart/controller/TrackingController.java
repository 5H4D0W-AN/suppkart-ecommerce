package com.suppkart.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suppkart.dto.response.ApiResponse;
import com.suppkart.dto.response.TrackingResponse;
import com.suppkart.exception.BusinessException;
import com.suppkart.model.entity.User;
import com.suppkart.security.CustomUserDetailsService.UserPrincipal;
import com.suppkart.service.TrackingService;
import com.suppkart.service.UserService;

import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/tracking")
@Validated
public class TrackingController {
    
    private static final Logger logger = LoggerFactory.getLogger(TrackingController.class);
    
    @Autowired
    private TrackingService trackingService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Get tracking information for an order by order ID
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<TrackingResponse>> getOrderTracking(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("Getting tracking for order {} by user: {}", orderId, userPrincipal.getUsername());
        
        try {
            User user = userService.getUserById(userPrincipal.getId());
            TrackingResponse trackingResponse = trackingService.getOrderTracking(orderId, user);
            
            return ResponseEntity.ok(
                ApiResponse.success("Tracking information retrieved successfully", trackingResponse)
            );
            
        } catch (BusinessException e) {
            logger.error("Business error getting tracking for order {} by user {}: {}", 
                        orderId, userPrincipal.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            logger.error("Error getting tracking for order {} by user {}: {}", 
                        orderId, userPrincipal.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to retrieve tracking information")
            );
        }
    }
    
    /**
     * Get tracking information by tracking number
     */
    @GetMapping("/number/{trackingNumber}")
    public ResponseEntity<ApiResponse<TrackingResponse>> getTrackingByNumber(
            @PathVariable @NotBlank String trackingNumber,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("Getting tracking for tracking number {} by user: {}", trackingNumber, userPrincipal.getUsername());
        
        try {
            User user = userService.getUserById(userPrincipal.getId());
            TrackingResponse trackingResponse = trackingService.getTrackingByNumber(trackingNumber, user);
            
            return ResponseEntity.ok(
                ApiResponse.success("Tracking information retrieved successfully", trackingResponse)
            );
            
        } catch (BusinessException e) {
            logger.error("Business error getting tracking for tracking number {} by user {}: {}", 
                        trackingNumber, userPrincipal.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            logger.error("Error getting tracking for tracking number {} by user {}: {}", 
                        trackingNumber, userPrincipal.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to retrieve tracking information")
            );
        }
    }
    
    /**
     * Shiprocket webhook endpoint for tracking updates
     * TODO: This endpoint will receive webhook notifications from Shiprocket
     * and update order tracking status automatically
     */
    @PostMapping("/webhook/shiprocket")
    public ResponseEntity<ApiResponse<String>> handleShiprocketWebhook(
            @RequestBody String webhookPayload,
            @RequestParam(required = false) String signature) {
        
        logger.info("Received Shiprocket webhook payload");
        
        try {
            // TODO: Implement proper webhook validation and processing
            // 1. Validate webhook signature from Shiprocket
            // 2. Parse the webhook payload to extract tracking information
            // 3. Update order status based on tracking updates
            // 4. Send email notifications for status changes if needed
            
            // Mock implementation for now
            logger.info("Processing Shiprocket webhook - TODO: Implement actual webhook processing");
            
            // Example of how the actual implementation would work:
            // ShiprocketWebhookPayload payload = parseWebhookPayload(webhookPayload);
            // boolean updated = trackingService.updateTrackingFromWebhook(
            //     payload.getTrackingNumber(),
            //     payload.getStatus(),
            //     payload.getLocation(),
            //     payload.getRemarks()
            // );
            
            return ResponseEntity.ok(
                ApiResponse.success("Webhook processed successfully", "OK")
            );
            
        } catch (Exception e) {
            logger.error("Error processing Shiprocket webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to process webhook")
            );
        }
    }
    
    /**
     * Public tracking endpoint (no authentication required)
     * This allows customers to track orders without logging in using tracking number
     */
    @GetMapping("/public/{trackingNumber}")
    public ResponseEntity<ApiResponse<TrackingResponse>> getPublicTracking(
            @PathVariable @NotBlank String trackingNumber) {
        
        logger.info("Getting public tracking for tracking number: {}", trackingNumber);
        
        try {
            // TODO: Implement public tracking that doesn't require user authentication
            // This would return limited tracking information without sensitive user data
            
            // For now, return a message indicating this feature is not yet implemented
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                ApiResponse.error("Public tracking feature not yet implemented. Please login to track your order.")
            );
            
        } catch (Exception e) {
            logger.error("Error getting public tracking for tracking number {}: {}", 
                        trackingNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to retrieve tracking information")
            );
        }
    }
}
