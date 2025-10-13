package com.suppkart.dto.admin.product;

public class ImageOrderRequest {
    
    private Long imageId;
    private Integer sortOrder;
    private Boolean isPrimary;
    
    // Constructors
    public ImageOrderRequest() {}
    
    public ImageOrderRequest(Long imageId, Integer sortOrder, Boolean isPrimary) {
        this.imageId = imageId;
        this.sortOrder = sortOrder;
        this.isPrimary = isPrimary;
    }
    
    // Getters and setters
    public Long getImageId() {
        return imageId;
    }
    
    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public Boolean getIsPrimary() {
        return isPrimary;
    }
    
    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
}