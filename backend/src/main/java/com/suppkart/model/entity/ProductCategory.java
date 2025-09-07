package com.suppkart.model.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "product_categories")
@IdClass(ProductCategory.ProductCategoryId.class)
public class ProductCategory {
    
    @Id
    @Column(name = "product_id")
    private Long productId;
    
    @Id
    @Column(name = "category_id")
    private Long categoryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;
    
    @NotNull
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
    
    // Constructors
    public ProductCategory() {}
    
    public ProductCategory(Long productId, Long categoryId, Integer displayOrder) {
        this.productId = productId;
        this.categoryId = categoryId;
        this.displayOrder = displayOrder;
    }
    
    public ProductCategory(Product product, Category category, Integer displayOrder) {
        this.product = product;
        this.category = category;
        this.productId = product.getProductId();
        this.categoryId = category.getCategoryId();
        this.displayOrder = displayOrder;
    }
    
    // Getters and Setters
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            this.productId = product.getProductId();
        }
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
        if (category != null) {
            this.categoryId = category.getCategoryId();
        }
    }
    
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductCategory that = (ProductCategory) o;
        return Objects.equals(productId, that.productId) && 
               Objects.equals(categoryId, that.categoryId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(productId, categoryId);
    }
    
    @Override
    public String toString() {
        return "ProductCategory{" +
                "productId=" + productId +
                ", categoryId=" + categoryId +
                ", displayOrder=" + displayOrder +
                '}';
    }
    
    // Composite Key Class
    public static class ProductCategoryId implements Serializable {
        private Long productId;
        private Long categoryId;
        
        public ProductCategoryId() {}
        
        public ProductCategoryId(Long productId, Long categoryId) {
            this.productId = productId;
            this.categoryId = categoryId;
        }
        
        public Long getProductId() {
            return productId;
        }
        
        public void setProductId(Long productId) {
            this.productId = productId;
        }
        
        public Long getCategoryId() {
            return categoryId;
        }
        
        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProductCategoryId that = (ProductCategoryId) o;
            return Objects.equals(productId, that.productId) && 
                   Objects.equals(categoryId, that.categoryId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(productId, categoryId);
        }
    }
}
