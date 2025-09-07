package com.suppkart.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom exception for cart-related errors
 */
public class CartException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    
    public CartException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
    
    public CartException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public CartException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
    
    public CartException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    
    // Common cart error codes
    public static class ErrorCodes {
        public static final String CART_NOT_FOUND = "CART_NOT_FOUND";
        public static final String CART_ITEM_NOT_FOUND = "CART_ITEM_NOT_FOUND";
        public static final String INVALID_QUANTITY = "INVALID_QUANTITY";
        public static final String PRODUCT_NOT_AVAILABLE = "PRODUCT_NOT_AVAILABLE";
        public static final String CART_EXPIRED = "CART_EXPIRED";
        public static final String CART_EMPTY = "CART_EMPTY";
        public static final String MAX_QUANTITY_EXCEEDED = "MAX_QUANTITY_EXCEEDED";
        public static final String INSUFFICIENT_STOCK = "INSUFFICIENT_STOCK";
    }
}
