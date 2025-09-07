package com.suppkart.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom exception for token-related errors
 */
public class TokenException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    
    public TokenException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.UNAUTHORIZED;
    }
    
    public TokenException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public TokenException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.UNAUTHORIZED;
    }
    
    public TokenException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
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
    
    // Common token error codes
    public static class ErrorCodes {
        public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
        public static final String TOKEN_INVALID = "TOKEN_INVALID";
        public static final String TOKEN_REVOKED = "TOKEN_REVOKED";
        public static final String TOKEN_NOT_FOUND = "TOKEN_NOT_FOUND";
        public static final String REFRESH_TOKEN_EXPIRED = "REFRESH_TOKEN_EXPIRED";
        public static final String REFRESH_TOKEN_INVALID = "REFRESH_TOKEN_INVALID";
        public static final String TOKEN_GENERATION_FAILED = "TOKEN_GENERATION_FAILED";
    }
}
