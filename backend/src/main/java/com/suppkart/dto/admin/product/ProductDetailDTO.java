package com.suppkart.dto.admin.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ProductDetailDTO {
    
    private Long id;
    private String name;
    private String sku;
    private String description;
    private String shortDescription;
    private BigDecimal price;
    private int stock;
    private List<CategoryDTO> categories;
    private String status;
    private List<ProductImageDTO> images;
    private List<ProductVariantDTO> variants;
    private Map<String, String> specifications;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private String brand;
    private String servingSize;
    private Integer servingsPerContainer;
    private String proteinContent;
    private String ingredients;
    private String directions;
    private String warnings;
    private BigDecimal weight;
    private String dimensions;
    private String barcode;
    private int lowStockThreshold;
    private boolean isHighlighted;
    private BigDecimal avgRating;
    private int reviewCount;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SportDTO> sports;
    private List<GoalDTO> goals;
    
    // Constructors
    public ProductDetailDTO() {}
    
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
    
    public int getStock() {
        return stock;
    }
    
    public void setStock(int stock) {
        this.stock = stock;
    }
    
    public List<CategoryDTO> getCategories() {
        return categories;
    }
    
    public void setCategories(List<CategoryDTO> categories) {
        this.categories = categories;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public List<ProductImageDTO> getImages() {
        return images;
    }
    
    public void setImages(List<ProductImageDTO> images) {
        this.images = images;
    }
    
    public List<ProductVariantDTO> getVariants() {
        return variants;
    }
    
    public void setVariants(List<ProductVariantDTO> variants) {
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
    
    public int getLowStockThreshold() {
        return lowStockThreshold;
    }
    
    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }
    
    public boolean isHighlighted() {
        return isHighlighted;
    }
    
    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }
    
    public BigDecimal getAvgRating() {
        return avgRating;
    }
    
    public void setAvgRating(BigDecimal avgRating) {
        this.avgRating = avgRating;
    }
    
    public int getReviewCount() {
        return reviewCount;
    }
    
    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
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
    
    public List<SportDTO> getSports() {
        return sports;
    }
    
    public void setSports(List<SportDTO> sports) {
        this.sports = sports;
    }
    
    public List<GoalDTO> getGoals() {
        return goals;
    }
    
    public void setGoals(List<GoalDTO> goals) {
        this.goals = goals;
    }
    
    // Helper DTOs
    public static class CategoryDTO {
        private Long id;
        private String name;
        private String slug;
        
        public CategoryDTO() {}
        
        public CategoryDTO(Long id, String name, String slug) {
            this.id = id;
            this.name = name;
            this.slug = slug;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
    }
    
    public static class SportDTO {
        private Long id;
        private String name;
        private String slug;
        private int relevance;
        
        public SportDTO() {}
        
        public SportDTO(Long id, String name, String slug, int relevance) {
            this.id = id;
            this.name = name;
            this.slug = slug;
            this.relevance = relevance;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public int getRelevance() { return relevance; }
        public void setRelevance(int relevance) { this.relevance = relevance; }
    }
    
    public static class GoalDTO {
        private Long id;
        private String name;
        private String slug;
        private int effectiveness;
        
        public GoalDTO() {}
        
        public GoalDTO(Long id, String name, String slug, int effectiveness) {
            this.id = id;
            this.name = name;
            this.slug = slug;
            this.effectiveness = effectiveness;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public int getEffectiveness() { return effectiveness; }
        public void setEffectiveness(int effectiveness) { this.effectiveness = effectiveness; }
    }
}
