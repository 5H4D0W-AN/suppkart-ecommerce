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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "wishlist_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"wishlist_id", "product_id", "variant_id"})
})
@EntityListeners(AuditingEntityListener.class)
public class WishlistItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_item_id")
    private Long wishlistItemId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlist_id", nullable = false)
    private Wishlist wishlist;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;
    
    @CreatedDate
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;
    
    // Constructors
    public WishlistItem() {}
    
    public WishlistItem(Wishlist wishlist, Product product, ProductVariant variant) {
        this.wishlist = wishlist;
        this.product = product;
        this.variant = variant;
    }
    
    // Getters and Setters
    public Long getWishlistItemId() {
        return wishlistItemId;
    }
    
    public void setWishlistItemId(Long wishlistItemId) {
        this.wishlistItemId = wishlistItemId;
    }
    
    public Wishlist getWishlist() {
        return wishlist;
    }
    
    public void setWishlist(Wishlist wishlist) {
        this.wishlist = wishlist;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public ProductVariant getVariant() {
        return variant;
    }
    
    public void setVariant(ProductVariant variant) {
        this.variant = variant;
    }
    
    public LocalDateTime getAddedAt() {
        return addedAt;
    }
    
    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
    
    // Utility methods
    public String getProductName() {
        if (variant != null) {
            return product.getName() + " - " + variant.getName();
        }
        return product.getName();
    }
    
    public String getProductSku() {
        if (variant != null) {
            return variant.getSku();
        }
        return product.getSku();
    }
    
    public boolean isSameProductAndVariant(Long productId, Long variantId) {
        boolean productMatches = this.product.getProductId().equals(productId);
        if (variantId != null && this.variant != null) {
            return productMatches && this.variant.getVariantId().equals(variantId);
        } else if (variantId == null && this.variant == null) {
            return productMatches;
        }
        return false;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WishlistItem that = (WishlistItem) o;
        return Objects.equals(wishlistItemId, that.wishlistItemId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(wishlistItemId);
    }
    
    @Override
    public String toString() {
        return "WishlistItem{" +
                "wishlistItemId=" + wishlistItemId +
                ", productName='" + getProductName() + '\'' +
                ", addedAt=" + addedAt +
                '}';
    }
}
