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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "product_sports")
@IdClass(ProductSport.ProductSportId.class)
public class ProductSport {
    
    @Id
    @Column(name = "product_id")
    private Long productId;
    
    @Id
    @Column(name = "sport_id")
    private Long sportId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", insertable = false, updatable = false)
    private Sport sport;
    
    @NotNull
    @Min(1)
    @Max(10)
    @Column(name = "relevance", nullable = false)
    private Integer relevance = 5;
    
    // Constructors
    public ProductSport() {}
    
    public ProductSport(Long productId, Long sportId, Integer relevance) {
        this.productId = productId;
        this.sportId = sportId;
        this.relevance = relevance;
    }
    
    public ProductSport(Product product, Sport sport, Integer relevance) {
        this.product = product;
        this.sport = sport;
        this.productId = product.getProductId();
        this.sportId = sport.getSportId();
        this.relevance = relevance;
    }
    
    // Getters and Setters
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Long getSportId() {
        return sportId;
    }
    
    public void setSportId(Long sportId) {
        this.sportId = sportId;
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
    
    public Sport getSport() {
        return sport;
    }
    
    public void setSport(Sport sport) {
        this.sport = sport;
        if (sport != null) {
            this.sportId = sport.getSportId();
        }
    }
    
    public Integer getRelevance() {
        return relevance;
    }
    
    public void setRelevance(Integer relevance) {
        this.relevance = relevance;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductSport that = (ProductSport) o;
        return Objects.equals(productId, that.productId) && 
               Objects.equals(sportId, that.sportId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(productId, sportId);
    }
    
    @Override
    public String toString() {
        return "ProductSport{" +
                "productId=" + productId +
                ", sportId=" + sportId +
                ", relevance=" + relevance +
                '}';
    }
    
    // Composite Key Class
    public static class ProductSportId implements Serializable {
        private Long productId;
        private Long sportId;
        
        public ProductSportId() {}
        
        public ProductSportId(Long productId, Long sportId) {
            this.productId = productId;
            this.sportId = sportId;
        }
        
        public Long getProductId() {
            return productId;
        }
        
        public void setProductId(Long productId) {
            this.productId = productId;
        }
        
        public Long getSportId() {
            return sportId;
        }
        
        public void setSportId(Long sportId) {
            this.sportId = sportId;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProductSportId that = (ProductSportId) o;
            return Objects.equals(productId, that.productId) && 
                   Objects.equals(sportId, that.sportId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(productId, sportId);
        }
    }
}
