package com.suppkart.exception;

public class OrderException extends BusinessException {
    
    public OrderException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public OrderException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    
    public static class ErrorCodes {
        public static final String ORDER_NOT_FOUND = "ORDER_NOT_FOUND";
        public static final String ORDER_ALREADY_CANCELLED = "ORDER_ALREADY_CANCELLED";
        public static final String ORDER_CANNOT_BE_CANCELLED = "ORDER_CANNOT_BE_CANCELLED";
        public static final String ORDER_ALREADY_DELIVERED = "ORDER_ALREADY_DELIVERED";
        public static final String INSUFFICIENT_STOCK = "INSUFFICIENT_STOCK";
        public static final String INVALID_ORDER_STATUS = "INVALID_ORDER_STATUS";
        public static final String ORDER_ITEMS_EMPTY = "ORDER_ITEMS_EMPTY";
        public static final String ORDER_CREATION_FAILED = "ORDER_CREATION_FAILED";
        public static final String ORDER_UPDATE_FAILED = "ORDER_UPDATE_FAILED";
        public static final String UNAUTHORIZED_ORDER_ACCESS = "UNAUTHORIZED_ORDER_ACCESS";
        public static final String ORDER_TOTAL_MISMATCH = "ORDER_TOTAL_MISMATCH";
        public static final String ORDER_PAYMENT_PENDING = "ORDER_PAYMENT_PENDING";
        public static final String ORDER_PAYMENT_FAILED = "ORDER_PAYMENT_FAILED";
    }
}
