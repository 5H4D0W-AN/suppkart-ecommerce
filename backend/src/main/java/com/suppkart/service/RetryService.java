package com.suppkart.service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.suppkart.exception.PaymentException;

/**
 * Service for handling retry mechanisms for failed operations
 */
@Service
public class RetryService {
    
    private static final Logger logger = LoggerFactory.getLogger(RetryService.class);
    
    @Value("${retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${retry.initial-interval:1000}")
    private long initialRetryInterval;
    
    @Value("${retry.max-interval:10000}")
    private long maxRetryInterval;
    
    @Value("${retry.multiplier:2.0}")
    private double retryMultiplier;
    
    /**
     * Execute operation with retry mechanism
     */
    public <T> T executeWithRetry(Supplier<T> operation, String operationName) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                if (attempt > 1) {
                    logger.info("Retrying operation: {} - Attempt: {}/{}", 
                               operationName, attempt, maxRetryAttempts);
                }
                
                return operation.get();
                
            } catch (Exception e) {
                lastException = e;
                logger.warn("Operation failed: {} - Attempt: {}/{} - Error: {}", 
                           operationName, attempt, maxRetryAttempts, e.getMessage());
                
                if (attempt < maxRetryAttempts) {
                    try {
                        long waitTime = calculateBackoffTime(attempt);
                        logger.debug("Waiting {} ms before retry", waitTime);
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new PaymentException(PaymentException.ErrorCodes.PAYMENT_GATEWAY_ERROR, 
                                                 "Operation interrupted: " + ie.getMessage());
                    }
                }
            }
        }
        
        throw new PaymentException(PaymentException.ErrorCodes.PAYMENT_GATEWAY_ERROR, 
                                 "Operation failed after " + maxRetryAttempts + " attempts: " + lastException.getMessage());
    }
    
    /**
     * Execute operation asynchronously with retry mechanism
     */
    @Async
    public <T> CompletableFuture<T> executeWithRetryAsync(Supplier<T> operation, String operationName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeWithRetry(operation, operationName);
            } catch (Exception e) {
                logger.error("Async operation failed after all retries: {}", operationName, e);
                throw new RuntimeException("Operation failed after retries: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Execute payment operation with specific retry logic
     */
    public <T> T executePaymentWithRetry(Supplier<T> paymentOperation, String paymentId) {
        return executeWithRetry(() -> {
            try {
                T result = paymentOperation.get();
                logger.info("Payment operation successful for payment ID: {}", paymentId);
                return result;
                
            } catch (PaymentException e) {
                // Don't retry for certain payment errors
                if (isNonRetryablePaymentError(e)) {
                    logger.warn("Non-retryable payment error for payment ID: {} - {}", paymentId, e.getMessage());
                    throw e;
                }
                
                logger.warn("Retryable payment error for payment ID: {} - {}", paymentId, e.getMessage());
                throw e;
                
            } catch (Exception e) {
                logger.warn("General error in payment operation for payment ID: {} - {}", paymentId, e.getMessage());
                throw new PaymentException(PaymentException.ErrorCodes.PAYMENT_GATEWAY_ERROR, 
                                         "Payment operation failed: " + e.getMessage());
            }
        }, "Payment-" + paymentId);
    }
    
    /**
     * Execute webhook processing with retry
     */
    public void executeWebhookWithRetry(Runnable webhookOperation, String webhookEvent) {
        executeWithRetry(() -> {
            try {
                webhookOperation.run();
                logger.info("Webhook processing successful for event: {}", webhookEvent);
                return null;
                
            } catch (Exception e) {
                logger.warn("Webhook processing error for event: {} - {}", webhookEvent, e.getMessage());
                throw e;
            }
        }, "Webhook-" + webhookEvent);
    }
    
    /**
     * Execute order update with retry
     */
    public void executeOrderUpdateWithRetry(Runnable orderUpdateOperation, String orderId) {
        executeWithRetry(() -> {
            try {
                orderUpdateOperation.run();
                logger.info("Order update successful for order ID: {}", orderId);
                return null;
                
            } catch (Exception e) {
                logger.warn("Order update error for order ID: {} - {}", orderId, e.getMessage());
                throw e;
            }
        }, "OrderUpdate-" + orderId);
    }
    
    /**
     * Schedule retry for failed operation
     */
    @Async
    public void scheduleRetry(Runnable operation, String operationName, long delayMinutes) {
        CompletableFuture.delayedExecutor(delayMinutes, TimeUnit.MINUTES)
            .execute(() -> {
                try {
                    logger.info("Executing scheduled retry for operation: {}", operationName);
                    operation.run();
                    logger.info("Scheduled retry successful for operation: {}", operationName);
                    
                } catch (Exception e) {
                    logger.error("Scheduled retry failed for operation: {}", operationName, e);
                }
            });
    }
    
    /**
     * Calculate backoff time for retry attempts
     */
    private long calculateBackoffTime(int attempt) {
        long backoffTime = (long) (initialRetryInterval * Math.pow(retryMultiplier, attempt - 1));
        return Math.min(backoffTime, maxRetryInterval);
    }
    
    /**
     * Check if payment error is retryable
     */
    private boolean isNonRetryablePaymentError(PaymentException e) {
        String errorCode = e.getErrorCode();
        
        // Don't retry for these specific errors
        return PaymentException.ErrorCodes.PAYMENT_CANCELLED.equals(errorCode) ||
               PaymentException.ErrorCodes.INVALID_PAYMENT_METHOD.equals(errorCode) ||
               PaymentException.ErrorCodes.INSUFFICIENT_BALANCE.equals(errorCode) ||
               PaymentException.ErrorCodes.INVALID_PAYMENT_SIGNATURE.equals(errorCode) ||
               PaymentException.ErrorCodes.PAYMENT_VERIFICATION_FAILED.equals(errorCode) ||
               PaymentException.ErrorCodes.INVALID_TRANSACTION_ID.equals(errorCode) ||
               PaymentException.ErrorCodes.PAYMENT_AMOUNT_MISMATCH.equals(errorCode);
    }
    
    /**
     * Get retry statistics for monitoring
     */
    public RetryStats getRetryStats() {
        return new RetryStats(
            maxRetryAttempts,
            initialRetryInterval,
            maxRetryInterval,
            retryMultiplier,
            LocalDateTime.now()
        );
    }
    
    /**
     * Retry statistics class
     */
    public static class RetryStats {
        private final int maxAttempts;
        private final long initialInterval;
        private final long maxInterval;
        private final double multiplier;
        private final LocalDateTime timestamp;
        
        public RetryStats(int maxAttempts, long initialInterval, long maxInterval, 
                         double multiplier, LocalDateTime timestamp) {
            this.maxAttempts = maxAttempts;
            this.initialInterval = initialInterval;
            this.maxInterval = maxInterval;
            this.multiplier = multiplier;
            this.timestamp = timestamp;
        }
        
        // Getters
        public int getMaxAttempts() { return maxAttempts; }
        public long getInitialInterval() { return initialInterval; }
        public long getMaxInterval() { return maxInterval; }
        public double getMultiplier() { return multiplier; }
        public LocalDateTime getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return "RetryStats{" +
                    "maxAttempts=" + maxAttempts +
                    ", initialInterval=" + initialInterval +
                    ", maxInterval=" + maxInterval +
                    ", multiplier=" + multiplier +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}
