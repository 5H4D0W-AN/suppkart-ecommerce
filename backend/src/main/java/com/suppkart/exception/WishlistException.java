package com.suppkart.exception;

import org.springframework.http.HttpStatus;

public class WishlistException extends BusinessException {
    
    private static final long serialVersionUID = 1L;
    
    public WishlistException(String message) {
        super("WISHLIST_ERROR", message, HttpStatus.BAD_REQUEST);
    }
    
    public WishlistException(String message, HttpStatus status) {
        super("WISHLIST_ERROR", message, status);
    }
    
    public WishlistException(String message, Throwable cause) {
        super("WISHLIST_ERROR", message, cause);
    }
    
    public WishlistException(String message, Throwable cause, HttpStatus status) {
        super("WISHLIST_ERROR", message, status, cause);
    }
    
    // Static factory methods for common wishlist exceptions
    public static WishlistException wishlistNotFound() {
        return new WishlistException("Wishlist not found", HttpStatus.NOT_FOUND);
    }
    
    public static WishlistException wishlistNotFound(Long wishlistId) {
        return new WishlistException("Wishlist not found with ID: " + wishlistId, HttpStatus.NOT_FOUND);
    }
    
    public static WishlistException wishlistItemNotFound() {
        return new WishlistException("Wishlist item not found", HttpStatus.NOT_FOUND);
    }
    
    public static WishlistException wishlistItemNotFound(Long itemId) {
        return new WishlistException("Wishlist item not found with ID: " + itemId, HttpStatus.NOT_FOUND);
    }
    
    public static WishlistException productAlreadyInWishlist() {
        return new WishlistException("Product is already in the wishlist");
    }
    
    public static WishlistException productNotInWishlist() {
        return new WishlistException("Product is not in the wishlist");
    }
    
    public static WishlistException wishlistAccessDenied() {
        return new WishlistException("Access denied to this wishlist", HttpStatus.FORBIDDEN);
    }
    
    public static WishlistException wishlistIsPrivate() {
        return new WishlistException("This wishlist is private and cannot be accessed", HttpStatus.FORBIDDEN);
    }
    
    public static WishlistException invalidWishlistOperation(String operation) {
        return new WishlistException("Invalid wishlist operation: " + operation);
    }
    
    public static WishlistException wishlistLimitExceeded() {
        return new WishlistException("Wishlist item limit exceeded");
    }
    
    public static WishlistException userNotAuthenticated() {
        return new WishlistException("User must be authenticated to access wishlist", HttpStatus.UNAUTHORIZED);
    }
}
