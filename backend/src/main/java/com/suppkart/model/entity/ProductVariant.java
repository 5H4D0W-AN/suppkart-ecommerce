package com.suppkart.model.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_variants")
@EntityListeners(AuditingEntityListener.class)
public class ProductVariant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long variantId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;
    
    @Size(max = 100)
    @Column(name = "flavor", length = 100)
    private String flavor;
    
    @Size(max = 100)
    @Column(name = "size", length = 100)
    private String size;
    
    @Size(max = 100)
    @Column(name = "weight", length = 100)
    private String weight;
    
    @NotNull
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "sale_price", precision = 10, scale = 2)
    private BigDecimal salePrice;
    
    @DecimalMax(value = "40.0", message = "Discount cannot exceed 40%")
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;
    
    @Column(name = "discount_start_date")
    private LocalDateTime discountStartDate;
    
    @Column(name = "discount_end_date")
    private LocalDateTime discountEndDate;
    
    @Size(max = 50)
    @Column(name = "discount_reason", length = 50)
    private String discountReason;
    
    @Column(name = "cod_eligible", nullable = false)
    private Boolean codEligible = true;
    
    // SEO fields for variant-level SEO
    @Size(max = 255)
    @Column(name = "meta_title")
    private String metaTitle;
    
    @Size(max = 500)
    @Column(name = "meta_description", length = 500)
    private String metaDescription;
    
    @Size(max = 255)
    @Column(name = "meta_keywords")
    private String metaKeywords;
    
    @NotNull
    @PositiveOrZero
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;
    
    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @NotNull
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
    @Size(max = 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Size(max = 100)
    @Column(name = "barcode", length = 100)
    private String barcode;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Add relationship to images
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductImage> variantImages = new ArrayList<>();
    
    // Constructors
    public ProductVariant() {}
    
    public ProductVariant(Product product, String sku, String name, BigDecimal price) {
        this.product = product;
        this.sku = sku;
        this.name = name;
        this.price = price;
    }
    
    // Getters and Setters
    public Long getVariantId() {
        return variantId;
    }
    
    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getFlavor() {
        return flavor;
    }
    
    public void setFlavor(String flavor) {
        this.flavor = flavor;
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
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
        calculateDiscountPercentage();
    }
    
    public BigDecimal getSalePrice() {
        return salePrice;
    }
    
    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
        calculateDiscountPercentage();
    }
    
    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
    
    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
    
    public Integer getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Boolean getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
    
    public List<ProductImage> getVariantImages() {
        return variantImages;
    }
    
    public void setVariantImages(List<ProductImage> variantImages) {
        this.variantImages = variantImages;
    }
    
    public String getBarcode() {
        return barcode;
    }
    
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
    
    // Utility methods
    public BigDecimal getEffectivePrice() {
        return salePrice != null ? salePrice : price;
    }
    
    public boolean isOnSale() {
        return salePrice != null && salePrice.compareTo(price) < 0;
    }
    
    public boolean isInStock() {
        return stockQuantity > 0;
    }
    
    public boolean isLowStock() {
        return stockQuantity <= (product != null ? product.getLowStockThreshold() : 2) && stockQuantity > 0;
    }
    
    private void calculateDiscountPercentage() {
        if (price != null && salePrice != null && price.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = price.subtract(salePrice);
            this.discountPercentage = discount.divide(price, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        } else {
            this.discountPercentage = BigDecimal.ZERO;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductVariant that = (ProductVariant) o;
        return Objects.equals(variantId, that.variantId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(variantId);
    }
    
    @Override
    public String toString() {
        return "ProductVariant{" +
                "variantId=" + variantId +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", flavor='" + flavor + '\'' +
                ", size='" + size + '\'' +
                ", weight='" + weight + '\'' +
                ", price=" + price +
                ", salePrice=" + salePrice +
                ", stockQuantity=" + stockQuantity +
                ", isActive=" + isActive +
                ", isDefault=" + isDefault +
                '}';
    }
}
