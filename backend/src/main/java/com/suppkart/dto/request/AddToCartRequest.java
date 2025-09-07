package com.suppkart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AddToCartRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    private Long variantId; // Optional for products without variants
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    // Constructors
    public AddToCartRequest() {}
    
    public AddToCartRequest(Long productId, Long variantId, Integer quantity) {
        this.productId = productId;
        this.variantId = variantId;
        this.quantity = quantity;
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
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
