package com.suppkart.exception;

public class PaymentException extends BusinessException {
    
    public PaymentException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public PaymentException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    
    public static class ErrorCodes {
        public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
        public static final String PAYMENT_TIMEOUT = "PAYMENT_TIMEOUT";
        public static final String PAYMENT_CANCELLED = "PAYMENT_CANCELLED";
        public static final String PAYMENT_NOT_FOUND = "PAYMENT_NOT_FOUND";
        public static final String PAYMENT_ALREADY_PROCESSED = "PAYMENT_ALREADY_PROCESSED";
        public static final String INVALID_PAYMENT_METHOD = "INVALID_PAYMENT_METHOD";
        public static final String INSUFFICIENT_BALANCE = "INSUFFICIENT_BALANCE";
        public static final String PAYMENT_GATEWAY_ERROR = "PAYMENT_GATEWAY_ERROR";
        public static final String INVALID_PAYMENT_SIGNATURE = "INVALID_PAYMENT_SIGNATURE";
        public static final String PAYMENT_VERIFICATION_FAILED = "PAYMENT_VERIFICATION_FAILED";
        public static final String RAZORPAY_ERROR = "RAZORPAY_ERROR";
        public static final String UPI_ERROR = "UPI_ERROR";
        public static final String INVALID_TRANSACTION_ID = "INVALID_TRANSACTION_ID";
        public static final String PAYMENT_AMOUNT_MISMATCH = "PAYMENT_AMOUNT_MISMATCH";
        public static final String PAYMENT_CALLBACK_ERROR = "PAYMENT_CALLBACK_ERROR";
    }
}
