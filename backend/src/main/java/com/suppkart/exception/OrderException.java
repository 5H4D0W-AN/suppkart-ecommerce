package com.suppkart.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for order-related business logic errors
 */
public class OrderException extends BusinessException {
    
    public OrderException(String message) {
        super("ORDER_ERROR", message);
    }
    
    public OrderException(String message, Throwable cause) {
        super("ORDER_ERROR", message, cause);
    }
    
    public OrderException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public OrderException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    
    public OrderException(String errorCode, String message, HttpStatus httpStatus) {
        super(errorCode, message, httpStatus);
    }
    
    public OrderException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(errorCode, message, httpStatus, cause);
    }
}
