package com.suppkart.dto.admin.product;

import java.math.BigDecimal;

/**
 * DTO for Product List Item in admin interface
 * Used for displaying products in paginated lists
 */
public class ProductListItemDTO {
    private Long id;
    private String name;
    private String sku;
    private BigDecimal price;
    private Integer stockQuantity;
    private String mainCategory;
    private String status;
    private String thumbnailUrl;
    private Boolean hasVariants;
    private Integer variantCount;

    // Default constructor
    public ProductListItemDTO() {}

    // Constructor with essential fields
    public ProductListItemDTO(Long id, String name, String sku, BigDecimal price, Integer stockQuantity, String status) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.status = status;
    }

    // Constructor with all fields
    public ProductListItemDTO(Long id, String name, String sku, BigDecimal price, Integer stockQuantity, 
                             String mainCategory, String status, String thumbnailUrl, Boolean hasVariants, Integer variantCount) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.mainCategory = mainCategory;
        this.status = status;
        this.thumbnailUrl = thumbnailUrl;
        this.hasVariants = hasVariants;
        this.variantCount = variantCount;
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

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(String mainCategory) {
        this.mainCategory = mainCategory;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Boolean getHasVariants() {
        return hasVariants;
    }

    public void setHasVariants(Boolean hasVariants) {
        this.hasVariants = hasVariants;
    }

    public Integer getVariantCount() {
        return variantCount;
    }

    public void setVariantCount(Integer variantCount) {
        this.variantCount = variantCount;
    }
}
