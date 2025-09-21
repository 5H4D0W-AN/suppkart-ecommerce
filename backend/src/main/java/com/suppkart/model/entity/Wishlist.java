package com.suppkart.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "wishlists")
@EntityListeners(AuditingEntityListener.class)
public class Wishlist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private Long wishlistId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name = "My Wishlist";
    
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;
    
    @Column(name = "total_items", nullable = false)
    private Integer totalItems = 0;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WishlistItem> items = new ArrayList<>();
    
    // Constructors
    public Wishlist() {}
    
    public Wishlist(User user) {
        this.user = user;
        this.name = "My Wishlist";
        this.isPublic = false;
    }
    
    public Wishlist(User user, String name, Boolean isPublic) {
        this.user = user;
        this.name = name;
        this.isPublic = isPublic;
    }
    
    // Getters and Setters
    public Long getWishlistId() {
        return wishlistId;
    }
    
    public void setWishlistId(Long wishlistId) {
        this.wishlistId = wishlistId;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public Integer getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
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
    
    public List<WishlistItem> getItems() {
        return items;
    }
    
    public void setItems(List<WishlistItem> items) {
        this.items = items;
    }
    
    // Utility methods
    public void addItem(WishlistItem item) {
        items.add(item);
        item.setWishlist(this);
        recalculateTotalItems();
    }
    
    public void removeItem(WishlistItem item) {
        items.remove(item);
        item.setWishlist(null);
        recalculateTotalItems();
    }
    
    public void clearItems() {
        items.clear();
        recalculateTotalItems();
    }
    
    public void recalculateTotalItems() {
        this.totalItems = items.size();
    }
    
    public boolean containsProduct(Long productId, Long variantId) {
        return items.stream()
            .anyMatch(item -> {
                boolean productMatches = item.getProduct().getProductId().equals(productId);
                if (variantId != null && item.getVariant() != null) {
                    return productMatches && item.getVariant().getVariantId().equals(variantId);
                } else if (variantId == null && item.getVariant() == null) {
                    return productMatches;
                }
                return false;
            });
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wishlist wishlist = (Wishlist) o;
        return Objects.equals(wishlistId, wishlist.wishlistId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(wishlistId);
    }
    
    @Override
    public String toString() {
        return "Wishlist{" +
                "wishlistId=" + wishlistId +
                ", name='" + name + '\'' +
                ", isPublic=" + isPublic +
                ", totalItems=" + totalItems +
                '}';
    }
}
