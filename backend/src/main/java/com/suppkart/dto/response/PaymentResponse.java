package com.suppkart.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import com.suppkart.model.enums.PaymentMethod;
import com.suppkart.model.enums.PaymentStatus;

public class PaymentResponse {
    
    private boolean success;
    private String message;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    
    // Transaction details
    private String transactionId;
    private String orderId;
    private String paymentId; // Gateway specific payment ID
    private String gatewayOrderId; // Gateway specific order ID
    
    // Amount details
    private BigDecimal amount;
    private String currency;
    private BigDecimal fee; // Gateway fee
    private BigDecimal tax; // Tax amount
    
    // Payment gateway response
    private String gatewayResponse;
    private String errorCode;
    private String errorDescription;
    
    // URLs for redirect/callback
    private String redirectUrl;
    private String callbackUrl;
    
    // Payment method specific data
    private String upiTransactionId;
    private String bankReferenceNumber;
    private String cardLast4; // Last 4 digits of card
    private String cardType; // VISA, MASTERCARD, etc.
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private LocalDateTime expiresAt;
    
    // Additional metadata
    private Map<String, Object> metadata;
    
    // Constructors
    public PaymentResponse() {}
    
    public PaymentResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public PaymentResponse(boolean success, String message, PaymentStatus status) {
        this.success = success;
        this.message = message;
        this.status = status;
    }
    
    // Static factory methods
    public static PaymentResponse success(String message) {
        return new PaymentResponse(true, message, PaymentStatus.COMPLETED);
    }
    
    public static PaymentResponse failure(String message) {
        return new PaymentResponse(false, message, PaymentStatus.FAILED);
    }
    
    public static PaymentResponse pending(String message) {
        return new PaymentResponse(true, message, PaymentStatus.PENDING);
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    
    public String getGatewayOrderId() {
        return gatewayOrderId;
    }
    
    public void setGatewayOrderId(String gatewayOrderId) {
        this.gatewayOrderId = gatewayOrderId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public BigDecimal getFee() {
        return fee;
    }
    
    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }
    
    public BigDecimal getTax() {
        return tax;
    }
    
    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }
    
    public String getGatewayResponse() {
        return gatewayResponse;
    }
    
    public void setGatewayResponse(String gatewayResponse) {
        this.gatewayResponse = gatewayResponse;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorDescription() {
        return errorDescription;
    }
    
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
    
    public String getRedirectUrl() {
        return redirectUrl;
    }
    
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
    
    public String getCallbackUrl() {
        return callbackUrl;
    }
    
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
    
    public String getUpiTransactionId() {
        return upiTransactionId;
    }
    
    public void setUpiTransactionId(String upiTransactionId) {
        this.upiTransactionId = upiTransactionId;
    }
    
    public String getBankReferenceNumber() {
        return bankReferenceNumber;
    }
    
    public void setBankReferenceNumber(String bankReferenceNumber) {
        this.bankReferenceNumber = bankReferenceNumber;
    }
    
    public String getCardLast4() {
        return cardLast4;
    }
    
    public void setCardLast4(String cardLast4) {
        this.cardLast4 = cardLast4;
    }
    
    public String getCardType() {
        return cardType;
    }
    
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    @Override
    public String toString() {
        return "PaymentResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", status=" + status +
                ", transactionId='" + transactionId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                '}';
    }
}
