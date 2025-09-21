package com.suppkart.dto.admin.order;

import java.math.BigDecimal;

/**
 * DTO for order item information in admin order details
 */
public class OrderItemDTO {
    
    private Long id;
    private Long productId;
    private String productName;
    private String variantName;
    private String sku;
    private int quantity;
    private BigDecimal price;
    private BigDecimal total;
    private String imageUrl;

    // Constructors
    public OrderItemDTO() {}

    public OrderItemDTO(Long id, Long productId, String productName, String variantName,
                       String sku, int quantity, BigDecimal price, BigDecimal total, String imageUrl) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.variantName = variantName;
        this.sku = sku;
        this.quantity = quantity;
        this.price = price;
        this.total = total;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
