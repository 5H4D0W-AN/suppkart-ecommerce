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
@Table(name = "product_goals")
@IdClass(ProductGoal.ProductGoalId.class)
public class ProductGoal {
    
    @Id
    @Column(name = "product_id")
    private Long productId;
    
    @Id
    @Column(name = "goal_id")
    private Long goalId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", insertable = false, updatable = false)
    private Goal goal;
    
    @NotNull
    @Min(1)
    @Max(10)
    @Column(name = "effectiveness", nullable = false)
    private Integer effectiveness = 5;
    
    // Constructors
    public ProductGoal() {}
    
    public ProductGoal(Long productId, Long goalId, Integer effectiveness) {
        this.productId = productId;
        this.goalId = goalId;
        this.effectiveness = effectiveness;
    }
    
    public ProductGoal(Product product, Goal goal, Integer effectiveness) {
        this.product = product;
        this.goal = goal;
        this.productId = product.getProductId();
        this.goalId = goal.getGoalId();
        this.effectiveness = effectiveness;
    }
    
    // Getters and Setters
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Long getGoalId() {
        return goalId;
    }
    
    public void setGoalId(Long goalId) {
        this.goalId = goalId;
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
    
    public Goal getGoal() {
        return goal;
    }
    
    public void setGoal(Goal goal) {
        this.goal = goal;
        if (goal != null) {
            this.goalId = goal.getGoalId();
        }
    }
    
    public Integer getEffectiveness() {
        return effectiveness;
    }
    
    public void setEffectiveness(Integer effectiveness) {
        this.effectiveness = effectiveness;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductGoal that = (ProductGoal) o;
        return Objects.equals(productId, that.productId) && 
               Objects.equals(goalId, that.goalId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(productId, goalId);
    }
    
    @Override
    public String toString() {
        return "ProductGoal{" +
                "productId=" + productId +
                ", goalId=" + goalId +
                ", effectiveness=" + effectiveness +
                '}';
    }
    
    // Composite Key Class
    public static class ProductGoalId implements Serializable {
        private Long productId;
        private Long goalId;
        
        public ProductGoalId() {}
        
        public ProductGoalId(Long productId, Long goalId) {
            this.productId = productId;
            this.goalId = goalId;
        }
        
        public Long getProductId() {
            return productId;
        }
        
        public void setProductId(Long productId) {
            this.productId = productId;
        }
        
        public Long getGoalId() {
            return goalId;
        }
        
        public void setGoalId(Long goalId) {
            this.goalId = goalId;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProductGoalId that = (ProductGoalId) o;
            return Objects.equals(productId, that.productId) && 
                   Objects.equals(goalId, that.goalId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(productId, goalId);
        }
    }
}
