package com.suppkart.model.enums;

/**
 * Enum for different types of webhook events
 */
public enum WebhookEventType {
    // Payment events
    PAYMENT_AUTHORIZED("payment.authorized"),
    PAYMENT_CAPTURED("payment.captured"),
    PAYMENT_FAILED("payment.failed"),
    PAYMENT_CANCELLED("payment.cancelled"),
    
    // Refund events
    REFUND_PROCESSED("refund.processed"),
    REFUND_FAILED("refund.failed"),
    
    // Order events
    ORDER_PAID("order.paid"),
    ORDER_CANCELLED("order.cancelled"),
    
    // Subscription events (future use)
    SUBSCRIPTION_ACTIVATED("subscription.activated"),
    SUBSCRIPTION_CANCELLED("subscription.cancelled"),
    
    // Settlement events
    SETTLEMENT_PROCESSED("settlement.processed"),
    
    // Dispute events
    DISPUTE_CREATED("dispute.created"),
    DISPUTE_RESOLVED("dispute.resolved"),
    
    // Generic webhook event
    UNKNOWN("unknown");
    
    private final String value;
    
    WebhookEventType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Get WebhookEventType from string value
     */
    public static WebhookEventType fromValue(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        
        for (WebhookEventType type : WebhookEventType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        
        return UNKNOWN;
    }
    
    /**
     * Check if the event is payment related
     */
    public boolean isPaymentEvent() {
        return this == PAYMENT_AUTHORIZED || this == PAYMENT_CAPTURED || 
               this == PAYMENT_FAILED || this == PAYMENT_CANCELLED || this == ORDER_PAID;
    }
    
    /**
     * Check if the event is refund related
     */
    public boolean isRefundEvent() {
        return this == REFUND_PROCESSED || this == REFUND_FAILED;
    }
    
    /**
     * Check if the event requires order status update
     */
    public boolean requiresOrderUpdate() {
        return isPaymentEvent() || this == ORDER_CANCELLED || isRefundEvent();
    }
}
