package com.suppkart.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.suppkart.model.entity.Product;
import com.suppkart.model.entity.ProductImage;
import com.suppkart.model.entity.ProductVariant;
import com.suppkart.model.enums.Brand;

public class ProductResponse {
    
    private Long productId;
    private String name;
    private String description;
    private String shortDescription;
    private Brand brand;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String discountPercentage;
    private boolean inStock;
    private Double averageRating;
    private Integer reviewCount;
    private String mainImageUrl;
    private List<String> imageUrls;
    private List<ProductVariantResponse> variants;
    private List<String> categories;
    private List<String> sports;
    private List<String> goals;
    private boolean isFeatured;
    private boolean isBestSeller;
    private boolean isTopProduct;
    
    // Constructors
    public ProductResponse() {}
    
    public ProductResponse(Product product) {
        this.productId = product.getProductId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.shortDescription = product.getShortDescription();
        this.brand = product.getBrand();
        this.price = product.getMinPrice(); // Use min price from variants
        this.originalPrice = product.getMaxPrice(); // Use max price as original
        this.averageRating = product.getAvgRating() != null ? product.getAvgRating().doubleValue() : null;
        this.reviewCount = product.getReviewCount();
        
        // Calculate discount percentage
        if (this.originalPrice != null && this.price != null && 
            this.originalPrice.compareTo(this.price) > 0) {
            BigDecimal discount = this.originalPrice.subtract(this.price);
            BigDecimal percentage = discount.divide(this.originalPrice, 4, BigDecimal.ROUND_HALF_UP)
                                           .multiply(new BigDecimal(100));
            this.discountPercentage = percentage.setScale(0, BigDecimal.ROUND_HALF_UP) + "%";
        } else {
            this.discountPercentage = null;
        }
        
        // Set stock status
        this.inStock = product.isInStock();
        
        // Set main image URL
        ProductImage primaryImage = product.getPrimaryImage();
        if (primaryImage != null) {
            this.mainImageUrl = primaryImage.getImageUrl();
        }
        
        // Set all image URLs
        this.imageUrls = product.getImages().stream()
                               .map(ProductImage::getImageUrl)
                               .collect(Collectors.toList());
        
        // Set variants
        this.variants = product.getVariants().stream()
                              .map(ProductVariantResponse::new)
                              .collect(Collectors.toList());
        
        // TODO: Set categories when ProductCategory relationship is implemented
        this.categories = new ArrayList<>();
        
        // TODO: Set sports when ProductSport relationship is implemented
        this.sports = new ArrayList<>();
        
        // TODO: Set goals when ProductGoal relationship is implemented
        this.goals = new ArrayList<>();
        
        // Set flags
        this.isFeatured = product.getIsHighlighted(); // Use highlighted as featured
        this.isBestSeller = false; // TODO: Implement based on sales data
        this.isTopProduct = false; // TODO: Implement based on rating/popularity
    }
    
    // Getters and Setters
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getShortDescription() {
        return shortDescription;
    }
    
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
    
    public Brand getBrand() {
        return brand;
    }
    
    public void setBrand(Brand brand) {
        this.brand = brand;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }
    
    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }
    
    public String getDiscountPercentage() {
        return discountPercentage;
    }
    
    public void setDiscountPercentage(String discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
    
    public boolean isInStock() {
        return inStock;
    }
    
    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }
    
    public Double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
    
    public Integer getReviewCount() {
        return reviewCount;
    }
    
    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    public String getMainImageUrl() {
        return mainImageUrl;
    }
    
    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }
    
    public List<String> getImageUrls() {
        return imageUrls;
    }
    
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
    
    public List<ProductVariantResponse> getVariants() {
        return variants;
    }
    
    public void setVariants(List<ProductVariantResponse> variants) {
        this.variants = variants;
    }
    
    public List<String> getCategories() {
        return categories;
    }
    
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
    
    public List<String> getSports() {
        return sports;
    }
    
    public void setSports(List<String> sports) {
        this.sports = sports;
    }
    
    public List<String> getGoals() {
        return goals;
    }
    
    public void setGoals(List<String> goals) {
        this.goals = goals;
    }
    
    public boolean isFeatured() {
        return isFeatured;
    }
    
    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }
    
    public boolean isBestSeller() {
        return isBestSeller;
    }
    
    public void setBestSeller(boolean bestSeller) {
        isBestSeller = bestSeller;
    }
    
    public boolean isTopProduct() {
        return isTopProduct;
    }
    
    public void setTopProduct(boolean topProduct) {
        isTopProduct = topProduct;
    }
    
    // Nested ProductVariantResponse class
    public static class ProductVariantResponse {
        private Long variantId;
        private String size;
        private String flavor;
        private String weight;
        private BigDecimal price;
        private Integer stockQuantity;
        private boolean available;
        
        // Constructors
        public ProductVariantResponse() {}
        
        public ProductVariantResponse(ProductVariant variant) {
            this.variantId = variant.getVariantId();
            this.size = variant.getSize();
            this.flavor = variant.getFlavor();
            this.weight = null; // TODO: Add weight field to ProductVariant entity if needed
            this.price = variant.getPrice();
            this.stockQuantity = variant.getStockQuantity();
            this.available = variant.getStockQuantity() > 0;
        }
        
        // Getters and Setters
        public Long getVariantId() {
            return variantId;
        }
        
        public void setVariantId(Long variantId) {
            this.variantId = variantId;
        }
        
        public String getSize() {
            return size;
        }
        
        public void setSize(String size) {
            this.size = size;
        }
        
        public String getFlavor() {
            return flavor;
        }
        
        public void setFlavor(String flavor) {
            this.flavor = flavor;
        }
        
        public String getWeight() {
            return weight;
        }
        
        public void setWeight(String weight) {
            this.weight = weight;
        }
        
        public BigDecimal getPrice() {
            return price;
        }
        
        public void setPrice(BigDecimal price) {
            this.price = price;
        }
        
        public Integer getStockQuantity() {
            return stockQuantity;
        }
        
        public void setStockQuantity(Integer stockQuantity) {
            this.stockQuantity = stockQuantity;
        }
        
        public boolean isAvailable() {
            return available;
        }
        
        public void setAvailable(boolean available) {
            this.available = available;
        }
    }
}
