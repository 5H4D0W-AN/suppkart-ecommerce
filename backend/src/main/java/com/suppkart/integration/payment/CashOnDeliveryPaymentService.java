package com.suppkart.integration.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.suppkart.dto.request.PaymentRequest;
import com.suppkart.dto.response.PaymentResponse;
import com.suppkart.model.entity.Order;
import com.suppkart.model.enums.PaymentMethod;
import com.suppkart.model.enums.PaymentStatus;

@Service
public class CashOnDeliveryPaymentService implements PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(CashOnDeliveryPaymentService.class);
    
    @Override
    public PaymentResponse initializePayment(Order order, PaymentRequest paymentRequest) {
        logger.info("Initializing Cash on Delivery payment for order: {}", order.getOrderNumber());
        
        try {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setSuccess(true);
            paymentResponse.setMessage("Cash on Delivery payment initialized successfully");
            paymentResponse.setStatus(PaymentStatus.PENDING);
            paymentResponse.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
            paymentResponse.setOrderId(order.getOrderNumber());
            paymentResponse.setAmount(paymentRequest.getAmount());
            paymentResponse.setCurrency(paymentRequest.getCurrency());
            paymentResponse.setCreatedAt(LocalDateTime.now());
            
            // Generate COD transaction ID for tracking
            String transactionId = "COD_" + order.getOrderNumber() + "_" + System.currentTimeMillis();
            paymentResponse.setTransactionId(transactionId);
            
            // Add metadata for COD
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("payment_type", "Cash on Delivery");
            metadata.put("collection_method", "At delivery location");
            metadata.put("transaction_id", transactionId);
            metadata.put("cod_charges", calculateCodCharges(paymentRequest.getAmount()));
            
            paymentResponse.setMetadata(metadata);
            
            return paymentResponse;
            
        } catch (Exception e) {
            logger.error("Error initializing COD payment for order {}: {}", order.getOrderNumber(), e.getMessage(), e);
            return PaymentResponse.failure("Failed to initialize Cash on Delivery payment: " + e.getMessage());
        }
    }
    
    @Override
    public PaymentResponse processPaymentCallback(String callbackData) {
        logger.info("Cash on Delivery does not require callback processing");
        
        // COD payments are handled at delivery time, no callback needed
        PaymentResponse response = new PaymentResponse();
        response.setSuccess(true);
        response.setStatus(PaymentStatus.PENDING);
        response.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        response.setMessage("Cash on Delivery - Payment will be collected at delivery");
        response.setProcessedAt(LocalDateTime.now());
        
        return response;
    }
    
    @Override
    public PaymentResponse verifyPayment(String transactionId, String orderId) {
        logger.info("Verifying Cash on Delivery payment - Transaction ID: {}, Order ID: {}", transactionId, orderId);
        
        // For COD, verification happens when delivery is confirmed and payment is collected
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        paymentResponse.setTransactionId(transactionId);
        paymentResponse.setOrderId(orderId);
        paymentResponse.setSuccess(true);
        paymentResponse.setStatus(PaymentStatus.PENDING);
        paymentResponse.setMessage("Cash on Delivery - Payment pending collection at delivery");
        
        return paymentResponse;
    }
    
    @Override
    public PaymentResponse refundPayment(String transactionId, BigDecimal amount, String reason) {
        logger.info("Processing Cash on Delivery refund - Transaction ID: {}, Amount: {}", transactionId, amount);
        
        // COD refunds are typically handled manually or through bank transfer
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setSuccess(true);
        paymentResponse.setMessage("COD refund will be processed manually. Customer will be contacted for refund details.");
        paymentResponse.setStatus(PaymentStatus.PROCESSING);
        paymentResponse.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        paymentResponse.setTransactionId("REFUND_" + transactionId);
        paymentResponse.setAmount(amount);
        paymentResponse.setProcessedAt(LocalDateTime.now());
        
        return paymentResponse;
    }
    
    @Override
    public PaymentMethod getSupportedPaymentMethod() {
        return PaymentMethod.CASH_ON_DELIVERY;
    }
    
    @Override
    public boolean supports(PaymentMethod paymentMethod) {
        return PaymentMethod.CASH_ON_DELIVERY.equals(paymentMethod);
    }
    
    /**
     * Calculate COD charges based on order amount
     * @param orderAmount the order amount
     * @return COD charges
     */
    private BigDecimal calculateCodCharges(BigDecimal orderAmount) {
        // Simple COD charges calculation
        // Typically a flat fee or percentage of order value
        BigDecimal codCharges = BigDecimal.ZERO;
        
        if (orderAmount.compareTo(BigDecimal.valueOf(500)) < 0) {
            // Flat fee of ₹30 for orders below ₹500
            codCharges = BigDecimal.valueOf(30);
        } else if (orderAmount.compareTo(BigDecimal.valueOf(1000)) < 0) {
            // Flat fee of ₹20 for orders between ₹500-1000
            codCharges = BigDecimal.valueOf(20);
        }
        // No COD charges for orders above ₹1000
        
        return codCharges;
    }
    
    /**
     * Mark COD payment as collected
     * @param transactionId the transaction ID
     * @param collectedAmount the amount collected
     * @return PaymentResponse
     */
    public PaymentResponse markPaymentCollected(String transactionId, BigDecimal collectedAmount) {
        logger.info("Marking COD payment as collected - Transaction ID: {}, Amount: {}", transactionId, collectedAmount);
        
        try {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setSuccess(true);
            paymentResponse.setMessage("Cash on Delivery payment collected successfully");
            paymentResponse.setStatus(PaymentStatus.COMPLETED);
            paymentResponse.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
            paymentResponse.setTransactionId(transactionId);
            paymentResponse.setAmount(collectedAmount);
            paymentResponse.setProcessedAt(LocalDateTime.now());
            
            return paymentResponse;
            
        } catch (Exception e) {
            logger.error("Error marking COD payment as collected for transaction {}: {}", transactionId, e.getMessage(), e);
            return PaymentResponse.failure("Failed to mark COD payment as collected: " + e.getMessage());
        }
    }
}
