package com.suppkart.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suppkart.dto.response.ApiResponse;
import com.suppkart.dto.webhook.WebhookRequest;
import com.suppkart.exception.PaymentException;
import com.suppkart.service.WebhookService;

import jakarta.validation.Valid;

/**
 * Controller for handling payment webhook events
 */
@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    
    @Autowired
    private WebhookService webhookService;
    
    /**
     * Handle Razorpay webhook events
     */
    @PostMapping("/razorpay")
    public ResponseEntity<ApiResponse<String>> handleRazorpayWebhook(
            @Valid @RequestBody WebhookRequest webhookRequest,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        
        try {
            logger.info("Received Razorpay webhook: {}", webhookRequest.getEvent());
            
            // Set signature for verification
            if (signature != null) {
                webhookRequest.setSignature(signature);
            }
            
            webhookService.processWebhook(webhookRequest);
            
            return ResponseEntity.ok(
                ApiResponse.success("Webhook processed successfully", "OK")
            );
            
        } catch (PaymentException e) {
            logger.error("Payment exception processing Razorpay webhook", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), "WEBHOOK_ERROR"));
                
        } catch (Exception e) {
            logger.error("Error processing Razorpay webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error", "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Handle PhonePe/UPI webhook events
     */
    @PostMapping("/phonepe")
    public ResponseEntity<ApiResponse<String>> handlePhonePeWebhook(
            @Valid @RequestBody WebhookRequest webhookRequest,
            @RequestHeader(value = "X-PhonePe-Signature", required = false) String signature) {
        
        try {
            logger.info("Received PhonePe webhook: {}", webhookRequest.getEvent());
            
            // Set signature for verification
            if (signature != null) {
                webhookRequest.setSignature(signature);
            }
            
            webhookService.processWebhook(webhookRequest);
            
            return ResponseEntity.ok(
                ApiResponse.success("Webhook processed successfully", "OK")
            );
            
        } catch (PaymentException e) {
            logger.error("Payment exception processing PhonePe webhook", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), "WEBHOOK_ERROR"));
                
        } catch (Exception e) {
            logger.error("Error processing PhonePe webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error", "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Generic webhook handler for other payment gateways
     */
    @PostMapping("/generic")
    public ResponseEntity<ApiResponse<String>> handleGenericWebhook(
            @Valid @RequestBody WebhookRequest webhookRequest,
            @RequestHeader(value = "X-Webhook-Signature", required = false) String signature) {
        
        try {
            logger.info("Received generic webhook: {}", webhookRequest.getEvent());
            
            // Set signature for verification
            if (signature != null) {
                webhookRequest.setSignature(signature);
            }
            
            webhookService.processWebhook(webhookRequest);
            
            return ResponseEntity.ok(
                ApiResponse.success("Webhook processed successfully", "OK")
            );
            
        } catch (PaymentException e) {
            logger.error("Payment exception processing generic webhook", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), "WEBHOOK_ERROR"));
                
        } catch (Exception e) {
            logger.error("Error processing generic webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error", "INTERNAL_ERROR"));
        }
    }
}
