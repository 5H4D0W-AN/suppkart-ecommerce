package com.suppkart.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.annotation.CreatedDate;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "product_images")
@EntityListeners(AuditingEntityListener.class)
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    // Make product optional since image can belong to variant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // Add variant relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @NotBlank
    @Size(max = 500)
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Size(max = 255)
    @Column(name = "alt_text")
    private String altText;

    @NotNull
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @NotNull
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    // Add media type support
    @Size(max = 20)
    @Column(name = "media_type", length = 20)
    private String mediaType = "IMAGE"; // IMAGE, VIDEO

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public ProductImage() {
    }

    // Constructor for product-level images
    public ProductImage(Product product, String imageUrl, String altText) {
        this.product = product;
        this.imageUrl = imageUrl;
        this.altText = altText;
    }

    // Constructor for variant-level images
    public ProductImage(ProductVariant variant, String imageUrl, String altText) {
        this.variant = variant;
        this.imageUrl = imageUrl;
        this.altText = altText;
    }

    public ProductImage(Product product, String imageUrl, String altText, Integer sortOrder, Boolean isPrimary) {
        this.product = product;
        this.imageUrl = imageUrl;
        this.altText = altText;
        this.sortOrder = sortOrder;
        this.isPrimary = isPrimary;
    }

    // Getters and Setters
    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Add getter and setter for variant
    public ProductVariant getVariant() {
        return variant;
    }

    public void setVariant(ProductVariant variant) {
        this.variant = variant;
    }

    // Add getter and setter for mediaType
    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    // Utility methods
    public boolean isVariantImage() {
        return variant != null;
    }

    public boolean isProductImage() {
        return product != null && variant == null;
    }

    public String getFileName() {
        if (imageUrl != null && imageUrl.contains("/")) {
            return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        }
        return imageUrl;
    }

    public String getFileExtension() {
        String fileName = getFileName();
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    public boolean isImageFile() {
        String extension = getFileExtension();
        return extension.equals("jpg") || extension.equals("jpeg")
                || extension.equals("png") || extension.equals("gif")
                || extension.equals("webp") || extension.equals("svg");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductImage that = (ProductImage) o;
        return Objects.equals(imageId, that.imageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageId);
    }

    @Override
    public String toString() {
        return "ProductImage{"
                + "imageId=" + imageId
                + ", imageUrl='" + imageUrl + '\''
                + ", altText='" + altText + '\''
                + ", sortOrder=" + sortOrder
                + ", isPrimary=" + isPrimary
                + '}';
    }
}
