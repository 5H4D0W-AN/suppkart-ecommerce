package com.suppkart.exception;

public class ReferralException extends RuntimeException {
    
    public ReferralException(String message) {
        super(message);
    }
    
    public ReferralException(String message, Throwable cause) {
        super(message, cause);
    }
}
