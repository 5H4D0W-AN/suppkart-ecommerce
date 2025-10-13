package com.suppkart.dto.admin.product;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    
    @Size(max = 100, message = "Barcode must not exceed 100 characters")
    private String barcode;
    
    @DecimalMin(value = "0.0", message = "Discount percentage must be non-negative")
    @DecimalMax(value = "100.0", message = "Discount percentage must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Discount percentage must have at most 3 integer digits and 2 decimal places")
    private BigDecimal discountPercentage;
    
    private boolean isActive = true;
    private boolean isDefault = false;
    
    private Map<String, String> attributes;
    
    // New fields for enhanced functionality
    private LocalDateTime discountStartDate;
    private LocalDateTime discountEndDate;
    private String discountReason;
    private Boolean codEligible = true;
    
    // SEO fields
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    
    // Media handling - replace single imageUrl with list
    private List<ProductImageRequest> images;
    
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
    
    // Getters and setters for new fields
    public LocalDateTime getDiscountStartDate() {
        return discountStartDate;
    }
    
    public void setDiscountStartDate(LocalDateTime discountStartDate) {
        this.discountStartDate = discountStartDate;
    }
    
    public LocalDateTime getDiscountEndDate() {
        return discountEndDate;
    }
    
    public void setDiscountEndDate(LocalDateTime discountEndDate) {
        this.discountEndDate = discountEndDate;
    }
    
    public String getDiscountReason() {
        return discountReason;
    }
    
    public void setDiscountReason(String discountReason) {
        this.discountReason = discountReason;
    }
    
    public Boolean getCodEligible() {
        return codEligible;
    }
    
    public void setCodEligible(Boolean codEligible) {
        this.codEligible = codEligible;
    }
    
    public String getMetaTitle() {
        return metaTitle;
    }
    
    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }
    
    public String getMetaDescription() {
        return metaDescription;
    }
    
    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }
    
    public String getMetaKeywords() {
        return metaKeywords;
    }
    
    public void setMetaKeywords(String metaKeywords) {
        this.metaKeywords = metaKeywords;
    }
    
    public List<ProductImageRequest> getImages() {
        return images;
    }
    
    public void setImages(List<ProductImageRequest> images) {
        this.images = images;
    }
    
    public String getBarcode() {
        return barcode;
    }
    
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}
