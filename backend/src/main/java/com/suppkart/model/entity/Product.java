package com.suppkart.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.suppkart.model.enums.Brand;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Size(max = 500)
    @Column(name = "short_description", length = 500)
    private String shortDescription;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "brand", nullable = false, length = 50)
    private Brand brand;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;
    
    @Size(max = 100)
    @Column(name = "barcode", length = 100)
    private String barcode;
    
    @Column(name = "weight", precision = 8, scale = 2)
    private BigDecimal weight; // in grams
    
    @Size(max = 50)
    @Column(name = "dimensions", length = 50)
    private String dimensions; // LxWxH
    
    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @NotNull
    @Column(name = "is_highlighted", nullable = false)
    private Boolean isHighlighted = false;
    
    @Size(max = 255)
    @Column(name = "slug", unique = true)
    private String slug;
    
    @Size(max = 255)
    @Column(name = "meta_title")
    private String metaTitle;
    
    @Size(max = 500)
    @Column(name = "meta_description", length = 500)
    private String metaDescription;
    
    @Column(name = "avg_rating", precision = 3, scale = 2)
    private BigDecimal avgRating = BigDecimal.ZERO;
    
    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;
    
    @Size(max = 100)
    @Column(name = "serving_size", length = 100)
    private String servingSize;
    
    @Column(name = "servings_per_container")
    private Integer servingsPerContainer;
    
    @Size(max = 100)
    @Column(name = "protein_content", length = 100)
    private String proteinContent;
    
    @Column(name = "ingredients", columnDefinition = "TEXT")
    private String ingredients;
    
    @Column(name = "directions", columnDefinition = "TEXT")
    private String directions;
    
    @Column(name = "warnings", columnDefinition = "TEXT")
    private String warnings;
    
    @Column(name = "low_stock_threshold", nullable = false)
    private Integer lowStockThreshold = 2;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductVariant> variants = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductImage> images = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductCategory> productCategories = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductSport> productSports = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductGoal> productGoals = new ArrayList<>();
    
    // Constructors
    public Product() {}
    
    public Product(String name, Brand brand, String sku) {
        this.name = name;
        this.brand = brand;
        this.sku = sku;
    }
    
    // Getters and Setters
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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
    
    public Brand getBrand() {
        return brand;
    }
    
    public void setBrand(Brand brand) {
        this.brand = brand;
    }
    
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public String getBarcode() {
        return barcode;
    }
    
    public void setBarcode(String barcode) {
        this.barcode = barcode;
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
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Boolean getIsHighlighted() {
        return isHighlighted;
    }
    
    public void setIsHighlighted(Boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
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
    
    public BigDecimal getAvgRating() {
        return avgRating;
    }
    
    public void setAvgRating(BigDecimal avgRating) {
        this.avgRating = avgRating;
    }
    
    public Integer getReviewCount() {
        return reviewCount;
    }
    
    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
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
    
    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }
    
    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
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
    
    public List<ProductVariant> getVariants() {
        return variants;
    }
    
    public void setVariants(List<ProductVariant> variants) {
        this.variants = variants;
    }
    
    public List<ProductImage> getImages() {
        return images;
    }
    
    public void setImages(List<ProductImage> images) {
        this.images = images;
    }
    
    public List<ProductCategory> getProductCategories() {
        return productCategories;
    }
    
    public void setProductCategories(List<ProductCategory> productCategories) {
        this.productCategories = productCategories;
    }
    
    public List<ProductSport> getProductSports() {
        return productSports;
    }
    
    public void setProductSports(List<ProductSport> productSports) {
        this.productSports = productSports;
    }
    
    public List<ProductGoal> getProductGoals() {
        return productGoals;
    }
    
    public void setProductGoals(List<ProductGoal> productGoals) {
        this.productGoals = productGoals;
    }
    
    // Utility methods
    public ProductVariant getDefaultVariant() {
        return variants.stream()
            .filter(ProductVariant::getIsDefault)
            .findFirst()
            .orElse(variants.isEmpty() ? null : variants.get(0));
    }
    
    public ProductImage getPrimaryImage() {
        return images.stream()
            .filter(ProductImage::getIsPrimary)
            .findFirst()
            .orElse(images.isEmpty() ? null : images.get(0));
    }
    
    public boolean hasVariants() {
        return !variants.isEmpty();
    }
    
    public boolean isInStock() {
        if (hasVariants()) {
            return variants.stream().anyMatch(v -> v.getStockQuantity() > 0);
        }
        return true; // If no variants, assume in stock
    }
    
    public boolean isLowStock() {
        if (hasVariants()) {
            return variants.stream().anyMatch(v -> v.getStockQuantity() <= lowStockThreshold && v.getStockQuantity() > 0);
        }
        return false;
    }
    
    public BigDecimal getMinPrice() {
        return variants.stream()
            .map(v -> v.getSalePrice() != null ? v.getSalePrice() : v.getPrice())
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
    }
    
    public BigDecimal getMaxPrice() {
        return variants.stream()
            .map(v -> v.getSalePrice() != null ? v.getSalePrice() : v.getPrice())
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(productId, product.productId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", brand=" + brand +
                ", sku='" + sku + '\'' +
                ", isActive=" + isActive +
                ", avgRating=" + avgRating +
                ", reviewCount=" + reviewCount +
                '}';
    }
}
