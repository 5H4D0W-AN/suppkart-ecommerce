package com.suppkart.model.entity;

import com.suppkart.model.enums.AlertType;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_alerts")
@EntityListeners(AuditingEntityListener.class)
public class StockAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;

    @Column(name = "threshold", nullable = false)
    private Integer threshold;

    @Column(name = "current_stock", nullable = false)
    private Integer currentStock;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved = false;

    @Column(name = "notification_sent", nullable = false)
    private Boolean notificationSent = false;

    // Constructors
    public StockAlert() {}

    public StockAlert(Product product, ProductVariant variant, AlertType alertType, 
                     Integer threshold, Integer currentStock) {
        this.product = product;
        this.variant = variant;
        this.alertType = alertType;
        this.threshold = threshold;
        this.currentStock = currentStock;
        this.isResolved = false;
        this.notificationSent = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Boolean getIsResolved() {
        return isResolved;
    }

    public void setIsResolved(Boolean isResolved) {
        this.isResolved = isResolved;
        if (isResolved && this.resolvedAt == null) {
            this.resolvedAt = LocalDateTime.now();
        }
    }

    public Boolean getNotificationSent() {
        return notificationSent;
    }

    public void setNotificationSent(Boolean notificationSent) {
        this.notificationSent = notificationSent;
    }

    // Utility methods
    public void resolve() {
        this.isResolved = true;
        this.resolvedAt = LocalDateTime.now();
    }

    public void markNotificationSent() {
        this.notificationSent = true;
    }

    public boolean isActive() {
        return !isResolved;
    }

    public boolean isLowStockAlert() {
        return alertType == AlertType.LOW_STOCK;
    }

    public boolean isOutOfStockAlert() {
        return alertType == AlertType.OUT_OF_STOCK;
    }

    public String getDisplayName() {
        if (variant != null) {
            return product.getName() + " - " + variant.getSku();
        }
        return product.getName();
    }

    public String getAlertDescription() {
        String productName = getDisplayName();
        switch (alertType) {
            case LOW_STOCK:
                return "Low stock alert for " + productName + ". Current stock: " + currentStock + ", Threshold: " + threshold;
            case OUT_OF_STOCK:
                return "Out of stock alert for " + productName + ". Current stock: " + currentStock;
            default:
                return "Stock alert for " + productName + ". Current stock: " + currentStock;
        }
    }

    @Override
    public String toString() {
        return "StockAlert{" +
                "id=" + id +
                ", product=" + (product != null ? product.getName() : null) +
                ", variant=" + (variant != null ? variant.getSku() : null) +
                ", alertType=" + alertType +
                ", threshold=" + threshold +
                ", currentStock=" + currentStock +
                ", isResolved=" + isResolved +
                ", createdAt=" + createdAt +
                '}';
    }
}
