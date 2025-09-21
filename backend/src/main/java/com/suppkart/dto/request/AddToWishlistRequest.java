package com.suppkart.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AddToWishlistRequest {
    
    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be positive")
    private Long productId;
    
    private Long variantId; // Optional for product variants
    
    // Constructors
    public AddToWishlistRequest() {}
    
    public AddToWishlistRequest(Long productId, Long variantId) {
        this.productId = productId;
        this.variantId = variantId;
    }
    
    // Getters and Setters
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Long getVariantId() {
        return variantId;
    }
    
    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }
    
    @Override
    public String toString() {
        return "AddToWishlistRequest{" +
                "productId=" + productId +
                ", variantId=" + variantId +
                '}';
    }
}
