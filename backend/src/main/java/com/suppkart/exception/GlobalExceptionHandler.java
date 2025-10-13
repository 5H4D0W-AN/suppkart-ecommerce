package com.suppkart.exception;

import com.suppkart.dto.response.ApiResponse;
import com.suppkart.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle BusinessException
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        logger.warn("Business exception occurred: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        ErrorResponse response = new ErrorResponse();
        response.setSuccess(false);
        response.setMessage(ex.getMessage());
        
        ErrorResponse.ErrorDetail error = new ErrorResponse.ErrorDetail();
        error.setCode(ex.getErrorCode());
        error.setMessage(ex.getMessage());
        response.setError(error);
        
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    /**
     * Handle ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        
        ApiResponse<Object> response = new ApiResponse<>(false, ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Illegal argument: {}", ex.getMessage());
        
        ApiResponse<Object> response = new ApiResponse<>(false, ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle missing request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingRequestParameter(MissingServletRequestParameterException ex) {
        logger.warn("Missing request parameter: {}", ex.getMessage());
        
        ApiResponse<Object> response = new ApiResponse<>(false, "Missing required parameter: " + ex.getParameterName());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle missing multipart request parts (file uploads)
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingRequestPart(MissingServletRequestPartException ex) {
        logger.warn("Missing request part: {}", ex.getMessage());
        
        ApiResponse<Object> response = new ApiResponse<>(false, "Missing required file: " + ex.getRequestPartName());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle unsupported media type
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        logger.warn("Unsupported media type: {}", ex.getMessage());
        
        ApiResponse<Object> response = new ApiResponse<>(false, "Unsupported media type. Expected: " + ex.getSupportedMediaTypes());
        
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }

    /**
     * Handle malformed JSON and other message not readable exceptions
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        logger.warn("Malformed JSON or unreadable message: {}", ex.getMessage());
        
        ApiResponse<Object> response = new ApiResponse<>(false, "Malformed JSON or invalid request format");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        logger.warn("Validation exception occurred: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Object> response = new ApiResponse<>(false, "Validation failed");
        response.setData(Map.of("errors", errors));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        logger.error("Unexpected exception occurred", ex);
        
        ApiResponse<Object> response = new ApiResponse<>(false, "An unexpected error occurred");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}