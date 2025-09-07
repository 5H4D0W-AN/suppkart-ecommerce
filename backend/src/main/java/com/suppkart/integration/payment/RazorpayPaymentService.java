package com.suppkart.integration.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suppkart.dto.request.PaymentRequest;
import com.suppkart.dto.response.PaymentResponse;
import com.suppkart.exception.BusinessException;
import com.suppkart.model.entity.Order;
import com.suppkart.model.enums.PaymentMethod;
import com.suppkart.model.enums.PaymentStatus;

@Service
public class RazorpayPaymentService implements PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(RazorpayPaymentService.class);
    
    @Value("${payment.razorpay.key-id}")
    private String keyId;
    
    @Value("${payment.razorpay.key-secret}")
    private String keySecret;
    
    @Value("${payment.razorpay.webhook-secret}")
    private String webhookSecret;
    
    @Value("${payment.razorpay.api-url:https://api.razorpay.com/v1}")
    private String apiUrl;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public RazorpayPaymentService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public PaymentResponse initializePayment(Order order, PaymentRequest paymentRequest) {
        logger.info("Initializing Razorpay payment for order: {}", order.getOrderNumber());
        
        try {
            // Create Razorpay order
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("amount", paymentRequest.getAmount().multiply(BigDecimal.valueOf(100)).intValue()); // Convert to paise
            orderData.put("currency", paymentRequest.getCurrency());
            orderData.put("receipt", order.getOrderNumber());
            orderData.put("payment_capture", 1); // Auto capture
            
            // Add customer details
            Map<String, Object> notes = new HashMap<>();
            notes.put("order_id", order.getOrderNumber());
            notes.put("customer_email", paymentRequest.getCustomerEmail());
            notes.put("customer_phone", paymentRequest.getCustomerPhone());
            orderData.put("notes", notes);
            
            // Make API call to create Razorpay order
            String response = callRazorpayAPI("/orders", "POST", orderData);
            JsonNode responseNode = objectMapper.readTree(response);
            
            // Build payment response
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setSuccess(true);
            paymentResponse.setMessage("Payment initialized successfully");
            paymentResponse.setStatus(PaymentStatus.PENDING);
            paymentResponse.setPaymentMethod(PaymentMethod.RAZORPAY);
            paymentResponse.setOrderId(order.getOrderNumber());
            paymentResponse.setGatewayOrderId(responseNode.get("id").asText());
            paymentResponse.setAmount(paymentRequest.getAmount());
            paymentResponse.setCurrency(paymentRequest.getCurrency());
            paymentResponse.setCreatedAt(LocalDateTime.now());
            
            // Add metadata for frontend
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("razorpay_order_id", responseNode.get("id").asText());
            metadata.put("razorpay_key_id", keyId);
            metadata.put("amount", responseNode.get("amount").asInt());
            metadata.put("currency", responseNode.get("currency").asText());
            metadata.put("receipt", responseNode.get("receipt").asText());
            paymentResponse.setMetadata(metadata);
            
            return paymentResponse;
            
        } catch (Exception e) {
            logger.error("Error initializing Razorpay payment for order {}: {}", order.getOrderNumber(), e.getMessage(), e);
            return PaymentResponse.failure("Failed to initialize payment: " + e.getMessage());
        }
    }
    
    @Override
    public PaymentResponse processPaymentCallback(String callbackData) {
        logger.info("Processing Razorpay payment callback");
        
        try {
            JsonNode callbackNode = objectMapper.readTree(callbackData);
            
            // Verify webhook signature (implement webhook signature verification)
            // This is crucial for security
            
            String event = callbackNode.get("event").asText();
            JsonNode paymentNode = callbackNode.get("payload").get("payment").get("entity");
            
            PaymentResponse response = new PaymentResponse();
            response.setPaymentMethod(PaymentMethod.RAZORPAY);
            response.setTransactionId(paymentNode.get("id").asText());
            response.setOrderId(paymentNode.get("notes").get("order_id").asText());
            response.setAmount(BigDecimal.valueOf(paymentNode.get("amount").asLong()).divide(BigDecimal.valueOf(100)));
            response.setCurrency(paymentNode.get("currency").asText());
            response.setProcessedAt(LocalDateTime.now());
            
            switch (event) {
                case "payment.captured":
                    response.setSuccess(true);
                    response.setStatus(PaymentStatus.COMPLETED);
                    response.setMessage("Payment completed successfully");
                    break;
                case "payment.failed":
                    response.setSuccess(false);
                    response.setStatus(PaymentStatus.FAILED);
                    response.setMessage("Payment failed");
                    response.setErrorCode(paymentNode.get("error_code").asText());
                    response.setErrorDescription(paymentNode.get("error_description").asText());
                    break;
                default:
                    response.setSuccess(true);
                    response.setStatus(PaymentStatus.PENDING);
                    response.setMessage("Payment is being processed");
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error processing Razorpay callback: {}", e.getMessage(), e);
            return PaymentResponse.failure("Failed to process payment callback: " + e.getMessage());
        }
    }
    
    @Override
    public PaymentResponse verifyPayment(String transactionId, String orderId) {
        logger.info("Verifying Razorpay payment - Transaction ID: {}, Order ID: {}", transactionId, orderId);
        
        try {
            // Fetch payment details from Razorpay
            String response = callRazorpayAPI("/payments/" + transactionId, "GET", null);
            JsonNode paymentNode = objectMapper.readTree(response);
            
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setPaymentMethod(PaymentMethod.RAZORPAY);
            paymentResponse.setTransactionId(transactionId);
            paymentResponse.setOrderId(orderId);
            paymentResponse.setAmount(BigDecimal.valueOf(paymentNode.get("amount").asLong()).divide(BigDecimal.valueOf(100)));
            paymentResponse.setCurrency(paymentNode.get("currency").asText());
            
            String status = paymentNode.get("status").asText();
            switch (status) {
                case "captured":
                    paymentResponse.setSuccess(true);
                    paymentResponse.setStatus(PaymentStatus.COMPLETED);
                    paymentResponse.setMessage("Payment verified successfully");
                    break;
                case "failed":
                    paymentResponse.setSuccess(false);
                    paymentResponse.setStatus(PaymentStatus.FAILED);
                    paymentResponse.setMessage("Payment failed");
                    break;
                default:
                    paymentResponse.setSuccess(true);
                    paymentResponse.setStatus(PaymentStatus.PENDING);
                    paymentResponse.setMessage("Payment is pending");
            }
            
            return paymentResponse;
            
        } catch (Exception e) {
            logger.error("Error verifying Razorpay payment {}: {}", transactionId, e.getMessage(), e);
            return PaymentResponse.failure("Failed to verify payment: " + e.getMessage());
        }
    }
    
    @Override
    public PaymentResponse refundPayment(String transactionId, BigDecimal amount, String reason) {
        logger.info("Processing Razorpay refund - Transaction ID: {}, Amount: {}", transactionId, amount);
        
        try {
            Map<String, Object> refundData = new HashMap<>();
            refundData.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue()); // Convert to paise
            if (reason != null) {
                Map<String, Object> notes = new HashMap<>();
                notes.put("reason", reason);
                refundData.put("notes", notes);
            }
            
            String response = callRazorpayAPI("/payments/" + transactionId + "/refund", "POST", refundData);
            JsonNode refundNode = objectMapper.readTree(response);
            
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setSuccess(true);
            paymentResponse.setMessage("Refund processed successfully");
            paymentResponse.setStatus(PaymentStatus.REFUNDED);
            paymentResponse.setPaymentMethod(PaymentMethod.RAZORPAY);
            paymentResponse.setTransactionId(refundNode.get("id").asText());
            paymentResponse.setAmount(amount);
            paymentResponse.setProcessedAt(LocalDateTime.now());
            
            return paymentResponse;
            
        } catch (Exception e) {
            logger.error("Error processing Razorpay refund for transaction {}: {}", transactionId, e.getMessage(), e);
            return PaymentResponse.failure("Failed to process refund: " + e.getMessage());
        }
    }
    
    @Override
    public PaymentMethod getSupportedPaymentMethod() {
        return PaymentMethod.RAZORPAY;
    }
    
    @Override
    public boolean supports(PaymentMethod paymentMethod) {
        return PaymentMethod.RAZORPAY.equals(paymentMethod);
    }
    
    /**
     * Make API call to Razorpay
     */
    private String callRazorpayAPI(String endpoint, String method, Object requestBody) {
        try {
            // This is a simplified implementation
            // In a real application, you would use proper HTTP client with authentication
            // and error handling
            
            // For now, return a mock response to avoid compilation errors
            // TODO: Implement actual Razorpay API integration
            throw new BusinessException("RAZORPAY_API_NOT_IMPLEMENTED", 
                "Razorpay API integration not yet implemented. Please configure Razorpay SDK.");
            
        } catch (Exception e) {
            logger.error("Error calling Razorpay API endpoint {}: {}", endpoint, e.getMessage(), e);
            throw new BusinessException("RAZORPAY_API_ERROR", "Razorpay API call failed: " + e.getMessage());
        }
    }
}
