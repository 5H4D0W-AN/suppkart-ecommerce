package com.suppkart.dto.admin.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for Product Variant information in admin interface
 */
public class ProductVariantDTO {
    private Long id;
    private String name;
    private String sku;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer stockQuantity;
    private String size;
    private Double weight;
    private String flavor;
    private String imageUrl;
    private BigDecimal discountPercentage;
    private Boolean active;
    private Boolean isDefault;
    private Map<String, String> attributes;
    
    // New fields for enhanced functionality
    private LocalDateTime discountStartDate;
    private LocalDateTime discountEndDate;
    private String discountReason;
    private Boolean codEligible;
    
    // SEO fields
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    
    // Images
    private List<ProductImageDTO> images;
    
    // Barcode for variant
    private String barcode;

    // Default constructor
    public ProductVariantDTO() {}

    // Constructor with essential fields
    public ProductVariantDTO(Long id, String name, String sku, BigDecimal price, Integer stockQuantity) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
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
    
    public List<ProductImageDTO> getImages() {
        return images;
    }
    
    public void setImages(List<ProductImageDTO> images) {
        this.images = images;
    }
    
    public String getBarcode() {
        return barcode;
    }
    
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}
