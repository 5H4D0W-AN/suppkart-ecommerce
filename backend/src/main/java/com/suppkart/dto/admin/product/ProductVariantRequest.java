package com.suppkart.dto.admin.product;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Map;

public class ProductVariantRequest {
    
    @NotBlank(message = "Variant name is required")
    @Size(max = 255, message = "Variant name must not exceed 255 characters")
    private String name;
    
    @NotBlank(message = "Variant SKU is required")
    @Size(max = 100, message = "Variant SKU must not exceed 100 characters")
    private String sku;
    
    @NotNull(message = "Variant price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Variant price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Variant price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;
    
    @DecimalMin(value = "0.0", message = "Sale price must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Sale price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal salePrice;
    
    @Min(value = 0, message = "Stock must be non-negative")
    private int stock;
    
    @Size(max = 100, message = "Size must not exceed 100 characters")
    private String size;
    
    @Size(max = 100, message = "Weight must not exceed 100 characters")
    private String weight;
    
    @Size(max = 100, message = "Flavor must not exceed 100 characters")
    private String flavor;
    
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;
    
    @DecimalMin(value = "0.0", message = "Discount percentage must be non-negative")
    @DecimalMax(value = "100.0", message = "Discount percentage must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Discount percentage must have at most 3 integer digits and 2 decimal places")
    private BigDecimal discountPercentage;
    
    private boolean isActive = true;
    private boolean isDefault = false;
    
    private Map<String, String> attributes;
    
    // Constructors
    public ProductVariantRequest() {}
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getSalePrice() {
        return salePrice;
    }
    
    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }
    
    public int getStock() {
        return stock;
    }
    
    public void setStock(int stock) {
        this.stock = stock;
    }
    
    public String getSize() {
        return size;
    }
    
    public void setSize(String size) {
        this.size = size;
    }
    
    public String getWeight() {
        return weight;
    }
    
    public void setWeight(String weight) {
        this.weight = weight;
    }
    
    public String getFlavor() {
        return flavor;
    }
    
    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
    
    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
    
    public Map<String, String> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
