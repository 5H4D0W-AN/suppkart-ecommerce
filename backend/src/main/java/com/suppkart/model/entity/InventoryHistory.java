package com.suppkart.model.entity;

import com.suppkart.model.enums.ChangeType;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_history")
@EntityListeners(AuditingEntityListener.class)
public class InventoryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Column(name = "previous_quantity", nullable = false)
    private Integer previousQuantity;

    @Column(name = "new_quantity", nullable = false)
    private Integer newQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private ChangeType changeType;

    @Column(name = "reason", length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = false)
    private User updatedBy;

    @CreatedDate
    @Column(name = "updated_at", nullable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "supplier_name", length = 255)
    private String supplierName;

    @Column(name = "customer_name", length = 255)
    private String customerName;

    @Column(name = "unit_cost")
    private Double unitCost;

    @Column(name = "unit_price")
    private Double unitPrice;

    // Constructors
    public InventoryHistory() {}

    public InventoryHistory(Product product, ProductVariant variant, Integer previousQuantity, 
                           Integer newQuantity, ChangeType changeType, String reason, User updatedBy) {
        this.product = product;
        this.variant = variant;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.changeType = changeType;
        this.reason = reason;
        this.updatedBy = updatedBy;
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

    public Integer getPreviousQuantity() {
        return previousQuantity;
    }

    public void setPreviousQuantity(Integer previousQuantity) {
        this.previousQuantity = previousQuantity;
    }

    public Integer getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(Integer newQuantity) {
        this.newQuantity = newQuantity;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(Double unitCost) {
        this.unitCost = unitCost;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    // Utility methods
    public Integer getQuantityChange() {
        return newQuantity - previousQuantity;
    }

    public boolean isIncrease() {
        return newQuantity > previousQuantity;
    }

    public boolean isDecrease() {
        return newQuantity < previousQuantity;
    }

    @Override
    public String toString() {
        return "InventoryHistory{" +
                "id=" + id +
                ", product=" + (product != null ? product.getName() : null) +
                ", variant=" + (variant != null ? variant.getSku() : null) +
                ", previousQuantity=" + previousQuantity +
                ", newQuantity=" + newQuantity +
                ", changeType=" + changeType +
                ", reason='" + reason + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
