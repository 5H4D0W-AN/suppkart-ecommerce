package com.suppkart.integration.payment;

import java.math.BigDecimal;

import com.suppkart.dto.request.PaymentRequest;
import com.suppkart.dto.response.PaymentResponse;
import com.suppkart.model.entity.Order;
import com.suppkart.model.enums.PaymentMethod;

/**
 * Payment service interface for handling different payment methods
 */
public interface PaymentService {
    
    /**
     * Initialize payment for an order
     * @param order the order
     * @param paymentRequest the payment request
     * @return PaymentResponse
     */
    PaymentResponse initializePayment(Order order, PaymentRequest paymentRequest);
    
    /**
     * Process payment callback
     * @param callbackData the callback data from payment gateway
     * @return PaymentResponse
     */
    PaymentResponse processPaymentCallback(String callbackData);
    
    /**
     * Verify payment status
     * @param transactionId the transaction ID
     * @param orderId the order ID
     * @return PaymentResponse
     */
    PaymentResponse verifyPayment(String transactionId, String orderId);
    
    /**
     * Refund payment
     * @param transactionId the transaction ID
     * @param amount the refund amount
     * @param reason the refund reason
     * @return PaymentResponse
     */
    PaymentResponse refundPayment(String transactionId, BigDecimal amount, String reason);
    
    /**
     * Get supported payment method
     * @return PaymentMethod
     */
    PaymentMethod getSupportedPaymentMethod();
    
    /**
     * Check if payment method is supported
     * @param paymentMethod the payment method
     * @return boolean
     */
    boolean supports(PaymentMethod paymentMethod);
}
