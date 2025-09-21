package com.suppkart.service;

import java.security.MessageDigest;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suppkart.dto.webhook.WebhookRequest;
import com.suppkart.exception.PaymentException;
import com.suppkart.model.entity.Order;
import com.suppkart.model.enums.OrderStatus;
import com.suppkart.model.enums.PaymentStatus;
import com.suppkart.model.enums.WebhookEventType;
import com.suppkart.repository.OrderRepository;

/**
 * Service for handling payment webhook events
 */
@Service
@Transactional
public class WebhookService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);
    
    @Value("${payment.webhook.secret:}")
    private String webhookSecret;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private EmailNotificationService emailNotificationService;
    
    /**
     * Process incoming webhook request
     */
    public void processWebhook(WebhookRequest webhookRequest) {
        try {
            logger.info("Processing webhook event: {} for account: {}", 
                       webhookRequest.getEvent(), webhookRequest.getAccountId());
            
            // Verify webhook signature if secret is configured
            if (!webhookSecret.isEmpty() && !verifyWebhookSignature(webhookRequest)) {
                throw new PaymentException(PaymentException.ErrorCodes.INVALID_PAYMENT_SIGNATURE, "Invalid webhook signature");
            }
            
            WebhookEventType eventType = WebhookEventType.fromValue(webhookRequest.getEvent());
            Map<String, Object> payload = webhookRequest.getPayload();
            
            switch (eventType) {
                case PAYMENT_AUTHORIZED:
                    handlePaymentAuthorized(payload);
                    break;
                case PAYMENT_CAPTURED:
                    handlePaymentCaptured(payload);
                    break;
                case PAYMENT_FAILED:
                    handlePaymentFailed(payload);
                    break;
                case PAYMENT_CANCELLED:
                    handlePaymentCancelled(payload);
                    break;
                case ORDER_PAID:
                    handleOrderPaid(payload);
                    break;
                case REFUND_PROCESSED:
                    handleRefundProcessed(payload);
                    break;
                case REFUND_FAILED:
                    handleRefundFailed(payload);
                    break;
                default:
                    logger.warn("Unhandled webhook event type: {}", eventType);
                    break;
            }
            
            logger.info("Successfully processed webhook event: {}", webhookRequest.getEvent());
            
        } catch (Exception e) {
            logger.error("Error processing webhook event: {}", webhookRequest.getEvent(), e);
            throw new PaymentException(PaymentException.ErrorCodes.PAYMENT_CALLBACK_ERROR, "Failed to process webhook: " + e.getMessage());
        }
    }
    
    /**
     * Handle payment authorized event
     */
    private void handlePaymentAuthorized(Map<String, Object> payload) {
        String orderId = extractOrderId(payload);
        if (orderId != null) {
            updateOrderPaymentStatus(orderId, PaymentStatus.PROCESSING, OrderStatus.CONFIRMED);
        }
    }
    
    /**
     * Handle payment captured event
     */
    private void handlePaymentCaptured(Map<String, Object> payload) {
        String orderId = extractOrderId(payload);
        if (orderId != null) {
            updateOrderPaymentStatus(orderId, PaymentStatus.COMPLETED, OrderStatus.CONFIRMED);
            
            // Send order confirmation email
            try {
                Order order = orderRepository.findById(Long.parseLong(orderId)).orElse(null);
                if (order != null) {
                    // emailNotificationService.sendOrderConfirmation(order);
                    logger.info("Order confirmation email should be sent for order: {}", orderId);
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid order ID format for email notification: {}", orderId);
            }
        }
    }
    
    /**
     * Handle payment failed event
     */
    private void handlePaymentFailed(Map<String, Object> payload) {
        String orderId = extractOrderId(payload);
        if (orderId != null) {
            updateOrderPaymentStatus(orderId, PaymentStatus.FAILED, OrderStatus.CANCELLED);
            
            // Send payment failure notification
            try {
                Order order = orderRepository.findById(Long.parseLong(orderId)).orElse(null);
                if (order != null) {
                    // emailNotificationService.sendPaymentFailureNotification(order);
                    logger.info("Payment failure notification should be sent for order: {}", orderId);
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid order ID format for payment failure notification: {}", orderId);
            }
        }
    }
    
    /**
     * Handle payment cancelled event
     */
    private void handlePaymentCancelled(Map<String, Object> payload) {
        String orderId = extractOrderId(payload);
        if (orderId != null) {
            updateOrderPaymentStatus(orderId, PaymentStatus.CANCELLED, OrderStatus.CANCELLED);
        }
    }
    
    /**
     * Handle order paid event
     */
    private void handleOrderPaid(Map<String, Object> payload) {
        String orderId = extractOrderId(payload);
        if (orderId != null) {
            updateOrderPaymentStatus(orderId, PaymentStatus.COMPLETED, OrderStatus.CONFIRMED);
        }
    }
    
    /**
     * Handle refund processed event
     */
    private void handleRefundProcessed(Map<String, Object> payload) {
        String orderId = extractOrderId(payload);
        if (orderId != null) {
            updateOrderPaymentStatus(orderId, PaymentStatus.REFUNDED, OrderStatus.REFUNDED);
            
            // Send refund confirmation email
            try {
                Order order = orderRepository.findById(Long.parseLong(orderId)).orElse(null);
                if (order != null) {
                    // emailNotificationService.sendRefundConfirmation(order);
                    logger.info("Refund confirmation email should be sent for order: {}", orderId);
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid order ID format for refund confirmation: {}", orderId);
            }
        }
    }
    
    /**
     * Handle refund failed event
     */
    private void handleRefundFailed(Map<String, Object> payload) {
        String orderId = extractOrderId(payload);
        logger.warn("Refund failed for order: {}", orderId);
        // Additional handling for refund failures if needed
    }
    
    /**
     * Update order payment status
     */
    private void updateOrderPaymentStatus(String orderId, PaymentStatus paymentStatus, OrderStatus orderStatus) {
        try {
            Order order = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new PaymentException(PaymentException.ErrorCodes.PAYMENT_NOT_FOUND, "Order not found: " + orderId));
            
            order.setPaymentStatus(paymentStatus);
            order.setOrderStatus(orderStatus);
            
            orderRepository.save(order);
            
            logger.info("Updated order {} - Payment Status: {}, Order Status: {}", 
                       orderId, paymentStatus, orderStatus);
            
        } catch (NumberFormatException e) {
            logger.error("Invalid order ID format: {}", orderId, e);
            throw new PaymentException(PaymentException.ErrorCodes.PAYMENT_CALLBACK_ERROR, "Invalid order ID format: " + orderId);
        } catch (Exception e) {
            logger.error("Failed to update order status for order: {}", orderId, e);
            throw new PaymentException(PaymentException.ErrorCodes.PAYMENT_CALLBACK_ERROR, "Failed to update order status: " + e.getMessage());
        }
    }
    
    /**
     * Extract order ID from webhook payload
     */
    private String extractOrderId(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }
        
        // Try common field names for order ID
        Object orderId = payload.get("order_id");
        if (orderId == null) {
            orderId = payload.get("orderId");
        }
        if (orderId == null) {
            orderId = payload.get("receipt");
        }
        if (orderId == null && payload.containsKey("order")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> orderData = (Map<String, Object>) payload.get("order");
            orderId = orderData.get("receipt");
        }
        
        return orderId != null ? orderId.toString() : null;
    }
    
    /**
     * Verify webhook signature for security
     */
    private boolean verifyWebhookSignature(WebhookRequest webhookRequest) {
        if (webhookRequest.getSignature() == null || webhookSecret.isEmpty()) {
            return false;
        }
        
        try {
            // Create the payload string for verification
            String payload = createPayloadString(webhookRequest);
            
            // Generate expected signature using HMAC-SHA256
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(webhookSecret.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            
            byte[] hash = mac.doFinal(payload.getBytes());
            String expectedSignature = bytesToHex(hash);
            
            // Compare signatures
            return MessageDigest.isEqual(
                expectedSignature.getBytes(),
                webhookRequest.getSignature().getBytes()
            );
            
        } catch (Exception e) {
            logger.error("Error verifying webhook signature", e);
            return false;
        }
    }
    
    /**
     * Create payload string for signature verification
     */
    private String createPayloadString(WebhookRequest webhookRequest) {
        return webhookRequest.getEvent() + "|" + 
               webhookRequest.getAccountId() + "|" + 
               webhookRequest.getCreatedAt();
    }
    
    /**
     * Convert bytes to hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
