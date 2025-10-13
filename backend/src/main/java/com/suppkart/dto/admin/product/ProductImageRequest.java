package com.suppkart.dto.admin.product;

import org.springframework.web.multipart.MultipartFile;

public class ProductImageRequest {
    
    private String imageUrl;
    private String altText;
    private Integer sortOrder;
    private Boolean isPrimary = false;
    private String mediaType = "IMAGE";
    
    // For file uploads
    private MultipartFile file;
    
    // Constructors
    public ProductImageRequest() {}
    
    public ProductImageRequest(String imageUrl, String altText, Integer sortOrder, Boolean isPrimary) {
        this.imageUrl = imageUrl;
        this.altText = altText;
        this.sortOrder = sortOrder;
        this.isPrimary = isPrimary;
    }
    
    // Getters and setters
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
    
    public Boolean getIsPrimary() {
        return isPrimary;
    }
    
    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
    
    public String getMediaType() {
        return mediaType;
    }
    
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
    
    public MultipartFile getFile() {
        return file;
    }
    
    public void setFile(MultipartFile file) {
        this.file = file;
    }
}