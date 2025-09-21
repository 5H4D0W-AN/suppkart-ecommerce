package com.suppkart.exception;

/**
 * Custom exception for inventory-related operations
 */
public class InventoryException extends RuntimeException {
    
    public InventoryException(String message) {
        super(message);
    }
    
    public InventoryException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // Specific inventory exception types
    public static class InsufficientStockException extends InventoryException {
        public InsufficientStockException(String message) {
            super(message);
        }
    }
    
    public static class InventoryNotFoundException extends InventoryException {
        public InventoryNotFoundException(String message) {
            super(message);
        }
    }
    
    public static class InvalidStockAdjustmentException extends InventoryException {
        public InvalidStockAdjustmentException(String message) {
            super(message);
        }
    }
    
    public static class StockAlertNotFoundException extends InventoryException {
        public StockAlertNotFoundException(String message) {
            super(message);
        }
    }
    
    public static class DuplicateInventoryException extends InventoryException {
        public DuplicateInventoryException(String message) {
            super(message);
        }
    }
}
