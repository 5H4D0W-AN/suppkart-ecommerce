package com.suppkart.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suppkart.dto.request.PaymentRequest;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.dto.response.PaymentResponse;
import com.suppkart.exception.BusinessException;
import com.suppkart.integration.payment.PaymentService;
import com.suppkart.model.entity.Order;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.PaymentMethod;
import com.suppkart.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private List<PaymentService> paymentServices;
    
    @Autowired
    private OrderService orderService;
    
    /**
     * Initialize payment for an order
     */
    @PostMapping("/initialize")
    public ResponseEntity<ApiResponse<PaymentResponse>> initializePayment(
            @Valid @RequestBody PaymentRequest paymentRequest,
            @AuthenticationPrincipal User user) {
        
        logger.info("Initializing payment for order: {} with method: {}", 
            paymentRequest.getOrderId(), paymentRequest.getPaymentMethod());
        
        try {
            // Get order
            Order order = orderService.getOrderByNumber(paymentRequest.getOrderId(), user)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", 
                    "Order not found: " + paymentRequest.getOrderId()));
            
            // Find payment service for the requested method
            PaymentService paymentService = getPaymentService(paymentRequest.getPaymentMethod());
            
            // Initialize payment
            PaymentResponse paymentResponse = paymentService.initializePayment(order, paymentRequest);
            
            logger.info("Payment initialization result for order {}: {}", 
                paymentRequest.getOrderId(), paymentResponse.isSuccess());
            
            return ResponseEntity.ok(ApiResponse.success("Payment initialized successfully", paymentResponse));
            
        } catch (BusinessException e) {
            logger.error("Business error initializing payment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error initializing payment: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to initialize payment"));
        }
    }
    
    /**
     * Handle payment callback/webhook
     */
    @PostMapping("/callback/{paymentMethod}")
    public ResponseEntity<ApiResponse<PaymentResponse>> handlePaymentCallback(
            @PathVariable PaymentMethod paymentMethod,
            @RequestBody String callbackData) {
        
        logger.info("Processing payment callback for method: {}", paymentMethod);
        
        try {
            // Find payment service for the method
            PaymentService paymentService = getPaymentService(paymentMethod);
            
            // Process callback
            PaymentResponse paymentResponse = paymentService.processPaymentCallback(callbackData);
            
            logger.info("Payment callback processing result: {}", paymentResponse.isSuccess());
            
            return ResponseEntity.ok(ApiResponse.success("Payment callback processed", paymentResponse));
            
        } catch (BusinessException e) {
            logger.error("Business error processing payment callback: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing payment callback: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to process payment callback"));
        }
    }
    
    /**
     * Verify payment status
     */
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @RequestParam String transactionId,
            @RequestParam String orderId,
            @RequestParam PaymentMethod paymentMethod,
            @AuthenticationPrincipal User user) {
        
        logger.info("Verifying payment - Transaction: {}, Order: {}, Method: {}", 
            transactionId, orderId, paymentMethod);
        
        try {
            // Verify user owns the order
            orderService.getOrderByNumber(orderId, user)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", 
                    "Order not found: " + orderId));
            
            // Find payment service for the method
            PaymentService paymentService = getPaymentService(paymentMethod);
            
            // Verify payment
            PaymentResponse paymentResponse = paymentService.verifyPayment(transactionId, orderId);
            
            logger.info("Payment verification result for transaction {}: {}", 
                transactionId, paymentResponse.isSuccess());
            
            return ResponseEntity.ok(ApiResponse.success("Payment verification completed", paymentResponse));
            
        } catch (BusinessException e) {
            logger.error("Business error verifying payment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error verifying payment: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to verify payment"));
        }
    }
    
    /**
     * Process refund
     */
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> processRefund(
            @RequestParam String transactionId,
            @RequestParam String orderId,
            @RequestParam PaymentMethod paymentMethod,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal User user) {
        
        logger.info("Processing refund - Transaction: {}, Order: {}, Method: {}", 
            transactionId, orderId, paymentMethod);
        
        try {
            // Get order and verify user ownership
            Order order = orderService.getOrderByNumber(orderId, user)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", 
                    "Order not found: " + orderId));
            
            // Find payment service for the method
            PaymentService paymentService = getPaymentService(paymentMethod);
            
            // Process refund
            PaymentResponse paymentResponse = paymentService.refundPayment(
                transactionId, order.getTotalAmount(), reason);
            
            logger.info("Refund processing result for transaction {}: {}", 
                transactionId, paymentResponse.isSuccess());
            
            return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", paymentResponse));
            
        } catch (BusinessException e) {
            logger.error("Business error processing refund: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing refund: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to process refund"));
        }
    }
    
    /**
     * Get supported payment methods
     */
    @GetMapping("/methods")
    public ResponseEntity<ApiResponse<List<PaymentMethod>>> getSupportedPaymentMethods() {
        try {
            List<PaymentMethod> supportedMethods = paymentServices.stream()
                .map(PaymentService::getSupportedPaymentMethod)
                .toList();
            
            return ResponseEntity.ok(ApiResponse.success("Supported payment methods retrieved", supportedMethods));
            
        } catch (Exception e) {
            logger.error("Error getting supported payment methods: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to get supported payment methods"));
        }
    }
    
    /**
     * Find payment service for the given payment method
     */
    private PaymentService getPaymentService(PaymentMethod paymentMethod) {
        return paymentServices.stream()
            .filter(service -> service.supports(paymentMethod))
            .findFirst()
            .orElseThrow(() -> new BusinessException("PAYMENT_METHOD_NOT_SUPPORTED", 
                "Payment method not supported: " + paymentMethod));
    }
}
