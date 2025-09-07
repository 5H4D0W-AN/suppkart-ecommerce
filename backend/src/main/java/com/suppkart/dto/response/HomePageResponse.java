package com.suppkart.dto.response;

import java.util.List;

import com.suppkart.model.entity.Category;
import com.suppkart.model.entity.Goal;
import com.suppkart.model.entity.Sport;

public class HomePageResponse {
    
    private List<BannerResponse> heroBanners;
    private List<ProductResponse> featuredProducts;
    private List<ProductResponse> bestSellers;
    private List<ProductResponse> topProducts;
    private List<CategoryResponse> categories;
    private List<GoalResponse> goals;
    private List<SportResponse> sports;
    private WeeklyDealResponse weeklyDeal;
    private List<TestimonialResponse> testimonials;
    private List<ReviewResponse> customerReviews;
    
    // Constructors
    public HomePageResponse() {}
    
    // Getters and Setters
    public List<BannerResponse> getHeroBanners() {
        return heroBanners;
    }
    
    public void setHeroBanners(List<BannerResponse> heroBanners) {
        this.heroBanners = heroBanners;
    }
    
    public List<ProductResponse> getFeaturedProducts() {
        return featuredProducts;
    }
    
    public void setFeaturedProducts(List<ProductResponse> featuredProducts) {
        this.featuredProducts = featuredProducts;
    }
    
    public List<ProductResponse> getBestSellers() {
        return bestSellers;
    }
    
    public void setBestSellers(List<ProductResponse> bestSellers) {
        this.bestSellers = bestSellers;
    }
    
    public List<ProductResponse> getTopProducts() {
        return topProducts;
    }
    
    public void setTopProducts(List<ProductResponse> topProducts) {
        this.topProducts = topProducts;
    }
    
    public List<CategoryResponse> getCategories() {
        return categories;
    }
    
    public void setCategories(List<CategoryResponse> categories) {
        this.categories = categories;
    }
    
    public List<GoalResponse> getGoals() {
        return goals;
    }
    
    public void setGoals(List<GoalResponse> goals) {
        this.goals = goals;
    }
    
    public List<SportResponse> getSports() {
        return sports;
    }
    
    public void setSports(List<SportResponse> sports) {
        this.sports = sports;
    }
    
    public WeeklyDealResponse getWeeklyDeal() {
        return weeklyDeal;
    }
    
    public void setWeeklyDeal(WeeklyDealResponse weeklyDeal) {
        this.weeklyDeal = weeklyDeal;
    }
    
    public List<TestimonialResponse> getTestimonials() {
        return testimonials;
    }
    
    public void setTestimonials(List<TestimonialResponse> testimonials) {
        this.testimonials = testimonials;
    }
    
    public List<ReviewResponse> getCustomerReviews() {
        return customerReviews;
    }
    
    public void setCustomerReviews(List<ReviewResponse> customerReviews) {
        this.customerReviews = customerReviews;
    }
    
    // Nested response classes
    public static class BannerResponse {
        private Long id;
        private String title;
        private String description;
        private String imageUrl;
        private String linkUrl;
        private boolean active;
        
        // Constructors
        public BannerResponse() {}
        
        public BannerResponse(Long id, String title, String description, String imageUrl, String linkUrl, boolean active) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.imageUrl = imageUrl;
            this.linkUrl = linkUrl;
            this.active = active;
        }
        
        // Getters and Setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getImageUrl() {
            return imageUrl;
        }
        
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
        
        public String getLinkUrl() {
            return linkUrl;
        }
        
        public void setLinkUrl(String linkUrl) {
            this.linkUrl = linkUrl;
        }
        
        public boolean isActive() {
            return active;
        }
        
        public void setActive(boolean active) {
            this.active = active;
        }
    }
    
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String description;
        private String imageUrl;
        private String iconUrl;
        private int productCount;
        
        // Constructors
        public CategoryResponse() {}
        
        public CategoryResponse(Category category, int productCount) {
            this.id = category.getCategoryId();
            this.name = category.getName();
            this.description = category.getDescription();
            // TODO: Add imageUrl and iconUrl fields to Category entity
            this.imageUrl = null;
            this.iconUrl = null;
            this.productCount = productCount;
        }
        
        // Getters and Setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
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
        
        public String getImageUrl() {
            return imageUrl;
        }
        
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
        
        public String getIconUrl() {
            return iconUrl;
        }
        
        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }
        
        public int getProductCount() {
            return productCount;
        }
        
        public void setProductCount(int productCount) {
            this.productCount = productCount;
        }
    }
    
    public static class GoalResponse {
        private Long id;
        private String name;
        private String description;
        private String imageUrl;
        private String iconUrl;
        private int productCount;
        
        // Constructors
        public GoalResponse() {}
        
        public GoalResponse(Goal goal, int productCount) {
            this.id = goal.getGoalId();
            this.name = goal.getName();
            this.description = goal.getDescription();
            // TODO: Add imageUrl and iconUrl fields to Goal entity
            this.imageUrl = null;
            this.iconUrl = null;
            this.productCount = productCount;
        }
        
        // Getters and Setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
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
        
        public String getImageUrl() {
            return imageUrl;
        }
        
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
        
        public String getIconUrl() {
            return iconUrl;
        }
        
        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }
        
        public int getProductCount() {
            return productCount;
        }
        
        public void setProductCount(int productCount) {
            this.productCount = productCount;
        }
    }
    
    public static class SportResponse {
        private Long id;
        private String name;
        private String description;
        private String imageUrl;
        private String iconUrl;
        private int productCount;
        
        // Constructors
        public SportResponse() {}
        
        public SportResponse(Sport sport, int productCount) {
            this.id = sport.getSportId();
            this.name = sport.getName();
            this.description = sport.getDescription();
            // TODO: Add imageUrl and iconUrl fields to Sport entity
            this.imageUrl = null;
            this.iconUrl = null;
            this.productCount = productCount;
        }
        
        // Getters and Setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
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
        
        public String getImageUrl() {
            return imageUrl;
        }
        
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
        
        public String getIconUrl() {
            return iconUrl;
        }
        
        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }
        
        public int getProductCount() {
            return productCount;
        }
        
        public void setProductCount(int productCount) {
            this.productCount = productCount;
        }
    }
    
    public static class WeeklyDealResponse {
        private Long productId;
        private String productName;
        private String productImage;
        private String originalPrice;
        private String discountedPrice;
        private String discountPercentage;
        private String dealEndTime;
        private String dealDescription;
        
        // Constructors
        public WeeklyDealResponse() {}
        
        // Getters and Setters
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
        
        public String getProductImage() {
            return productImage;
        }
        
        public void setProductImage(String productImage) {
            this.productImage = productImage;
        }
        
        public String getOriginalPrice() {
            return originalPrice;
        }
        
        public void setOriginalPrice(String originalPrice) {
            this.originalPrice = originalPrice;
        }
        
        public String getDiscountedPrice() {
            return discountedPrice;
        }
        
        public void setDiscountedPrice(String discountedPrice) {
            this.discountedPrice = discountedPrice;
        }
        
        public String getDiscountPercentage() {
            return discountPercentage;
        }
        
        public void setDiscountPercentage(String discountPercentage) {
            this.discountPercentage = discountPercentage;
        }
        
        public String getDealEndTime() {
            return dealEndTime;
        }
        
        public void setDealEndTime(String dealEndTime) {
            this.dealEndTime = dealEndTime;
        }
        
        public String getDealDescription() {
            return dealDescription;
        }
        
        public void setDealDescription(String dealDescription) {
            this.dealDescription = dealDescription;
        }
    }
    
    public static class TestimonialResponse {
        private Long id;
        private String athleteName;
        private String athleteImage;
        private String sport;
        private String testimonialText;
        private String designation;
        
        // Constructors
        public TestimonialResponse() {}
        
        // Getters and Setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getAthleteName() {
            return athleteName;
        }
        
        public void setAthleteName(String athleteName) {
            this.athleteName = athleteName;
        }
        
        public String getAthleteImage() {
            return athleteImage;
        }
        
        public void setAthleteImage(String athleteImage) {
            this.athleteImage = athleteImage;
        }
        
        public String getSport() {
            return sport;
        }
        
        public void setSport(String sport) {
            this.sport = sport;
        }
        
        public String getTestimonialText() {
            return testimonialText;
        }
        
        public void setTestimonialText(String testimonialText) {
            this.testimonialText = testimonialText;
        }
        
        public String getDesignation() {
            return designation;
        }
        
        public void setDesignation(String designation) {
            this.designation = designation;
        }
    }
    
    public static class ReviewResponse {
        private Long id;
        private String customerName;
        private String customerImage;
        private Integer rating;
        private String reviewText;
        private String productName;
        private String reviewDate;
        
        // Constructors
        public ReviewResponse() {}
        
        // Getters and Setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getCustomerName() {
            return customerName;
        }
        
        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }
        
        public String getCustomerImage() {
            return customerImage;
        }
        
        public void setCustomerImage(String customerImage) {
            this.customerImage = customerImage;
        }
        
        public Integer getRating() {
            return rating;
        }
        
        public void setRating(Integer rating) {
            this.rating = rating;
        }
        
        public String getReviewText() {
            return reviewText;
        }
        
        public void setReviewText(String reviewText) {
            this.reviewText = reviewText;
        }
        
        public String getProductName() {
            return productName;
        }
        
        public void setProductName(String productName) {
            this.productName = productName;
        }
        
        public String getReviewDate() {
            return reviewDate;
        }
        
        public void setReviewDate(String reviewDate) {
            this.reviewDate = reviewDate;
        }
    }
}
