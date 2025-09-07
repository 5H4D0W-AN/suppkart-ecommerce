package com.suppkart.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CartResponse {
    private Long cartId;
    private Long userId;
    private String sessionId;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    
    // Constructors
    public CartResponse() {}
    
    public CartResponse(Long cartId, Long userId, String sessionId) {
        this.cartId = cartId;
        this.userId = userId;
        this.sessionId = sessionId;
    }
    
    // Getters and Setters
    public Long getCartId() {
        return cartId;
    }
    
    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public List<CartItemResponse> getItems() {
        return items;
    }
    
    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Integer getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    // Helper class for cart item response
    public static class CartItemResponse {
        private Long itemId;
        private Long productId;
        private String productName;
        private Long variantId;
        private String variantName;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;
        private String imageUrl;
        
        // Constructors
        public CartItemResponse() {}
        
        public CartItemResponse(Long itemId, Long productId, String productName, Integer quantity, BigDecimal price) {
            this.itemId = itemId;
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
        }
        
        // Getters and Setters
        public Long getItemId() {
            return itemId;
        }
        
        public void setItemId(Long itemId) {
            this.itemId = itemId;
        }
        
        public Long getProductId() {
            return productId;
        }
        
        public void setProductId(Long productId) {
            this.productId = productId;
        }
        
        public String getProductName() {
            return productName;
        }
        
        public void setProductName(String productName) {
            this.productName = productName;
        }
        
        public Long getVariantId() {
            return variantId;
        }
        
        public void setVariantId(Long variantId) {
            this.variantId = variantId;
        }
        
        public String getVariantName() {
            return variantName;
        }
        
        public void setVariantName(String variantName) {
            this.variantName = variantName;
        }
        
        public BigDecimal getPrice() {
            return price;
        }
        
        public void setPrice(BigDecimal price) {
            this.price = price;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
        
        public BigDecimal getSubtotal() {
            return subtotal;
        }
        
        public void setSubtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
        }
        
        public String getImageUrl() {
            return imageUrl;
        }
        
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}
