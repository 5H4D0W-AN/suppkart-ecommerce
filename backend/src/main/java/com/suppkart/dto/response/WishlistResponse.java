package com.suppkart.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class WishlistResponse {
    
    private Long wishlistId;
    private String name;
    private Boolean isPublic;
    private Integer totalItems;
    private Long userId;
    private String userName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private List<WishlistItemResponse> items;
    
    // Constructors
    public WishlistResponse() {}
    
    public WishlistResponse(Long wishlistId, String name, Boolean isPublic, Integer totalItems,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.wishlistId = wishlistId;
        this.name = name;
        this.isPublic = isPublic;
        this.totalItems = totalItems;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public Long getWishlistId() {
        return wishlistId;
    }
    
    public void setWishlistId(Long wishlistId) {
        this.wishlistId = wishlistId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public Integer getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
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
    
    public List<WishlistItemResponse> getItems() {
        return items;
    }
    
    public void setItems(List<WishlistItemResponse> items) {
        this.items = items;
    }
    
    @Override
    public String toString() {
        return "WishlistResponse{" +
                "wishlistId=" + wishlistId +
                ", name='" + name + '\'' +
                ", isPublic=" + isPublic +
                ", totalItems=" + totalItems +
                ", createdAt=" + createdAt +
                '}';
    }
}
