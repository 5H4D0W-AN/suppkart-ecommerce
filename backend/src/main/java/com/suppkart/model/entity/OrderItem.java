package com.suppkart.model.entity;

import java.math.BigDecimal;
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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "order_items")
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;
    
    // Product details at time of order (for historical accuracy)
    @NotNull
    @Size(max = 255)
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Size(max = 255)
    @Column(name = "variant_name")
    private String variantName;
    
    @Size(max = 50)
    @Column(name = "product_sku", length = 50)
    private String productSku;
    
    @NotNull
    @Min(1)
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @DecimalMin(value = "0.0")
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0")
    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    // Product attributes at time of order
    @Column(name = "product_image_url", length = 500)
    private String productImageUrl;
    
    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;
    
    @Column(name = "product_weight", precision = 8, scale = 3)
    private BigDecimal productWeight;
    
    @Size(max = 50)
    @Column(name = "product_brand", length = 50)
    private String productBrand;
    
    // Audit Fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public OrderItem() {}
    
    public OrderItem(Order order, Product product, Integer quantity, BigDecimal unitPrice) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.productName = product.getName();
        this.productSku = product.getSku();
        this.productBrand = product.getBrand() != null ? product.getBrand().toString() : null;
        this.productDescription = product.getDescription();
        // Set product image URL from first image if available
        if (!product.getImages().isEmpty()) {
            this.productImageUrl = product.getImages().get(0).getImageUrl();
        }
    }
    
    public OrderItem(Order order, ProductVariant variant, Integer quantity, BigDecimal unitPrice) {
        this(order, variant.getProduct(), quantity, unitPrice);
        this.productVariant = variant;
        this.variantName = variant.getName();
        // Override SKU with variant SKU if available
        if (variant.getSku() != null) {
            this.productSku = variant.getSku();
        }
    }
    
    // Getters and Setters
    public Long getOrderItemId() {
        return orderItemId;
    }
    
    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public ProductVariant getProductVariant() {
        return productVariant;
    }
    
    public void setProductVariant(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getVariantName() {
        return variantName;
    }
    
    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }
    
    public String getProductSku() {
        return productSku;
    }
    
    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        // Recalculate total price when quantity changes
        if (this.unitPrice != null) {
            this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        // Recalculate total price when unit price changes
        if (this.quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        }
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }
    
    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }
    
    public String getProductImageUrl() {
        return productImageUrl;
    }
    
    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }
    
    public String getProductDescription() {
        return productDescription;
    }
    
    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }
    
    public BigDecimal getProductWeight() {
        return productWeight;
    }
    
    public void setProductWeight(BigDecimal productWeight) {
        this.productWeight = productWeight;
    }
    
    public String getProductBrand() {
        return productBrand;
    }
    
    public void setProductBrand(String productBrand) {
        this.productBrand = productBrand;
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
    
    // Utility methods
    public String getDisplayName() {
        if (variantName != null && !variantName.trim().isEmpty()) {
            return productName + " - " + variantName;
        }
        return productName;
    }
    
    public BigDecimal getFinalPrice() {
        BigDecimal finalPrice = totalPrice;
        if (discountAmount != null) {
            finalPrice = finalPrice.subtract(discountAmount);
        }
        if (taxAmount != null) {
            finalPrice = finalPrice.add(taxAmount);
        }
        return finalPrice;
    }
    
    public void recalculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(orderItemId, orderItem.orderItemId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(orderItemId);
    }
    
    @Override
    public String toString() {
        return "OrderItem{" +
                "orderItemId=" + orderItemId +
                ", productName='" + productName + '\'' +
                ", variantName='" + variantName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
