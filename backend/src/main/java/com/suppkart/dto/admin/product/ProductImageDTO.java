package com.suppkart.dto.admin.product;

/**
 * DTO for Product Image information in admin interface
 */
public class ProductImageDTO {
    private Long id;
    private String url;
    private String altText;
    private Integer sortOrder;
    private Boolean isDefault;
    private String mediaType; // Add this field
    private Long variantId; // Add this to identify variant images
    private Long productId; // Add this to identify product images

    // Default constructor
    public ProductImageDTO() {}

    // Constructor with all fields
    public ProductImageDTO(Long id, String url, String altText, Integer sortOrder, Boolean isDefault) {
        this.id = id;
        this.url = url;
        this.altText = altText;
        this.sortOrder = sortOrder;
        this.isDefault = isDefault;
    }
    
    // Constructor with new fields
    public ProductImageDTO(Long id, String url, String altText, Integer sortOrder, Boolean isDefault, 
                          String mediaType, Long variantId, Long productId) {
        this.id = id;
        this.url = url;
        this.altText = altText;
        this.sortOrder = sortOrder;
        this.isDefault = isDefault;
        this.mediaType = mediaType;
        this.variantId = variantId;
        this.productId = productId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public String getMediaType() {
        return mediaType;
    }
    
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
    
    public Long getVariantId() {
        return variantId;
    }
    
    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
