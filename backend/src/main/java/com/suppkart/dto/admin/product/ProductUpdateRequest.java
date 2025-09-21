package com.suppkart.dto.admin.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ProductUpdateRequest {
    
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;
    
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;
    
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
    
    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;
    
    @Min(value = 0, message = "Stock must be non-negative")
    private Integer stock;
    
    private List<Long> categoryIds;
    
    @Pattern(regexp = "ACTIVE|INACTIVE|OUT_OF_STOCK", message = "Status must be ACTIVE, INACTIVE, or OUT_OF_STOCK")
    private String status;
    
    private Boolean hasVariants;
    
    @Valid
    private List<ProductVariantRequest> variants;
    
    private Map<String, String> specifications;
    
    @Size(max = 255, message = "Meta title must not exceed 255 characters")
    private String metaTitle;
    
    @Size(max = 500, message = "Meta description must not exceed 500 characters")
    private String metaDescription;
    
    @Size(max = 500, message = "Meta keywords must not exceed 500 characters")
    private String metaKeywords;
    
    @Size(max = 50, message = "Brand must not exceed 50 characters")
    private String brand;
    
    @Size(max = 100, message = "Serving size must not exceed 100 characters")
    private String servingSize;
    
    @Min(value = 0, message = "Servings per container must be non-negative")
    private Integer servingsPerContainer;
    
    @Size(max = 100, message = "Protein content must not exceed 100 characters")
    private String proteinContent;
    
    @Size(max = 5000, message = "Ingredients must not exceed 5000 characters")
    private String ingredients;
    
    @Size(max = 5000, message = "Directions must not exceed 5000 characters")
    private String directions;
    
    @Size(max = 5000, message = "Warnings must not exceed 5000 characters")
    private String warnings;
    
    @DecimalMin(value = "0.0", message = "Weight must be non-negative")
    @Digits(integer = 6, fraction = 2, message = "Weight must have at most 6 integer digits and 2 decimal places")
    private BigDecimal weight;
    
    @Size(max = 50, message = "Dimensions must not exceed 50 characters")
    private String dimensions;
    
    @Size(max = 100, message = "Barcode must not exceed 100 characters")
    private String barcode;
    
    @Min(value = 1, message = "Low stock threshold must be at least 1")
    private Integer lowStockThreshold;
    
    private Boolean isHighlighted;
    
    private List<Long> sportIds;
    private List<Long> goalIds;
    
    // Constructors
    public ProductUpdateRequest() {}
    
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getShortDescription() {
        return shortDescription;
    }
    
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public List<Long> getCategoryIds() {
        return categoryIds;
    }
    
    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Boolean getHasVariants() {
        return hasVariants;
    }
    
    public void setHasVariants(Boolean hasVariants) {
        this.hasVariants = hasVariants;
    }
    
    public List<ProductVariantRequest> getVariants() {
        return variants;
    }
    
    public void setVariants(List<ProductVariantRequest> variants) {
        this.variants = variants;
    }
    
    public Map<String, String> getSpecifications() {
        return specifications;
    }
    
    public void setSpecifications(Map<String, String> specifications) {
        this.specifications = specifications;
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
    
    public String getBrand() {
        return brand;
    }
    
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public String getServingSize() {
        return servingSize;
    }
    
    public void setServingSize(String servingSize) {
        this.servingSize = servingSize;
    }
    
    public Integer getServingsPerContainer() {
        return servingsPerContainer;
    }
    
    public void setServingsPerContainer(Integer servingsPerContainer) {
        this.servingsPerContainer = servingsPerContainer;
    }
    
    public String getProteinContent() {
        return proteinContent;
    }
    
    public void setProteinContent(String proteinContent) {
        this.proteinContent = proteinContent;
    }
    
    public String getIngredients() {
        return ingredients;
    }
    
    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }
    
    public String getDirections() {
        return directions;
    }
    
    public void setDirections(String directions) {
        this.directions = directions;
    }
    
    public String getWarnings() {
        return warnings;
    }
    
    public void setWarnings(String warnings) {
        this.warnings = warnings;
    }
    
    public BigDecimal getWeight() {
        return weight;
    }
    
    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
    
    public String getDimensions() {
        return dimensions;
    }
    
    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }
    
    public String getBarcode() {
        return barcode;
    }
    
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
    
    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }
    
    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }
    
    public Boolean getIsHighlighted() {
        return isHighlighted;
    }
    
    public void setIsHighlighted(Boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
    }
    
    public List<Long> getSportIds() {
        return sportIds;
    }
    
    public void setSportIds(List<Long> sportIds) {
        this.sportIds = sportIds;
    }
    
    public List<Long> getGoalIds() {
        return goalIds;
    }
    
    public void setGoalIds(List<Long> goalIds) {
        this.goalIds = goalIds;
    }
}
