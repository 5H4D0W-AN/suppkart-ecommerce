package com.suppkart.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class WishlistItemResponse {
    
    private Long wishlistItemId;
    private Long productId;
    private String productName;
    private String productSku;
    private String productDescription;
    private BigDecimal productPrice;
    private String productImageUrl;
    private Long variantId;
    private String variantName;
    private String variantSku;
    private BigDecimal variantPrice;
    private Boolean inStock;
    private Boolean onSale;
    private BigDecimal salePrice;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime addedAt;
    
    // Constructors
    public WishlistItemResponse() {}
    
    public WishlistItemResponse(Long wishlistItemId, Long productId, String productName, 
                              String productSku, BigDecimal productPrice, LocalDateTime addedAt) {
        this.wishlistItemId = wishlistItemId;
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.productPrice = productPrice;
        this.addedAt = addedAt;
    }
    
    // Getters and Setters
    public Long getWishlistItemId() {
        return wishlistItemId;
    }
    
    public void setWishlistItemId(Long wishlistItemId) {
        this.wishlistItemId = wishlistItemId;
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
    
    public String getProductSku() {
        return productSku;
    }
    
    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }
    
    public String getProductDescription() {
        return productDescription;
    }
    
    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }
    
    public BigDecimal getProductPrice() {
        return productPrice;
    }
    
    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }
    
    public String getProductImageUrl() {
        return productImageUrl;
    }
    
    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
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
    
    public String getVariantSku() {
        return variantSku;
    }
    
    public void setVariantSku(String variantSku) {
        this.variantSku = variantSku;
    }
    
    public BigDecimal getVariantPrice() {
        return variantPrice;
    }
    
    public void setVariantPrice(BigDecimal variantPrice) {
        this.variantPrice = variantPrice;
    }
    
    public Boolean getInStock() {
        return inStock;
    }
    
    public void setInStock(Boolean inStock) {
        this.inStock = inStock;
    }
    
    public Boolean getOnSale() {
        return onSale;
    }
    
    public void setOnSale(Boolean onSale) {
        this.onSale = onSale;
    }
    
    public BigDecimal getSalePrice() {
        return salePrice;
    }
    
    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }
    
    public LocalDateTime getAddedAt() {
        return addedAt;
    }
    
    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
    
    // Utility methods
    public BigDecimal getCurrentPrice() {
        if (variantPrice != null) {
            return onSale != null && onSale && salePrice != null ? salePrice : variantPrice;
        }
        return onSale != null && onSale && salePrice != null ? salePrice : productPrice;
    }
    
    public String getDisplayName() {
        if (variantName != null && !variantName.isEmpty()) {
            return productName + " - " + variantName;
        }
        return productName;
    }
    
    public String getCurrentSku() {
        return variantSku != null ? variantSku : productSku;
    }
    
    @Override
    public String toString() {
        return "WishlistItemResponse{" +
                "wishlistItemId=" + wishlistItemId +
                ", productName='" + productName + '\'' +
                ", variantName='" + variantName + '\'' +
                ", addedAt=" + addedAt +
                '}';
    }
}
