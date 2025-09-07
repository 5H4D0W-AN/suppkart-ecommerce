package com.suppkart.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.suppkart.dto.response.HomePageResponse;
import com.suppkart.dto.response.ProductResponse;
import com.suppkart.model.entity.Category;
import com.suppkart.model.entity.Goal;
import com.suppkart.model.entity.Product;
import com.suppkart.model.entity.Sport;
import com.suppkart.repository.CategoryRepository;
import com.suppkart.repository.GoalRepository;
import com.suppkart.repository.ProductRepository;
import com.suppkart.repository.SportRepository;

@Service
public class HomeService {
    
    private static final Logger logger = LoggerFactory.getLogger(HomeService.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private SportRepository sportRepository;
    
    @Autowired
    private GoalRepository goalRepository;
    
    /**
     * Get complete homepage data
     */
    public HomePageResponse getHomePageData() {
        logger.info("Fetching homepage data");
        
        HomePageResponse response = new HomePageResponse();
        
        try {
            // Set hero banners (mock data for now)
            response.setHeroBanners(getMockHeroBanners());
            
            // Set featured products (highlighted products)
            response.setFeaturedProducts(getFeaturedProducts());
            
            // Set best sellers (mock implementation - top rated products)
            response.setBestSellers(getBestSellers());
            
            // Set top products (featured products with high ratings)
            response.setTopProducts(getTopProducts());
            
            // Set categories
            response.setCategories(getCategories());
            
            // Set goals (shop by goals)
            response.setGoals(getGoals());
            
            // Set sports (shop by sports)
            response.setSports(getSports());
            
            // Set weekly deal (mock data)
            response.setWeeklyDeal(getMockWeeklyDeal());
            
            // Set testimonials (mock data)
            response.setTestimonials(getMockTestimonials());
            
            // Set customer reviews (mock data)
            response.setCustomerReviews(getMockCustomerReviews());
            
        } catch (Exception e) {
            logger.error("Error fetching homepage data", e);
            // Return partial data or empty response on error
            return new HomePageResponse();
        }
        
        return response;
    }
    
    /**
     * Get featured products (highlighted products)
     */
    public List<ProductResponse> getFeaturedProducts() {
        List<Product> products = productRepository.findHighlightedProducts();
        
        return products.stream()
                      .limit(5) // Limit to 5 products
                      .map(ProductResponse::new)
                      .collect(Collectors.toList());
    }
    
    /**
     * Get best seller products (mock implementation - top rated)
     */
    public List<ProductResponse> getBestSellers() {
        Pageable pageable = PageRequest.of(0, 5); // Limit to 5 products
        List<Product> products = productRepository.findRecentlyAddedProducts(pageable);
        
        return products.stream()
                      .map(product -> {
                          ProductResponse response = new ProductResponse(product);
                          response.setBestSeller(true);
                          return response;
                      })
                      .collect(Collectors.toList());
    }
    
    /**
     * Get top products (high-rated featured products)
     */
    public List<ProductResponse> getTopProducts() {
        Pageable pageable = PageRequest.of(0, 5); // Limit to 5 products  
        List<Product> products = productRepository.findRecentlyAddedProducts(pageable);
        
        return products.stream()
                      .map(product -> {
                          ProductResponse response = new ProductResponse(product);
                          response.setTopProduct(true);
                          return response;
                      })
                      .collect(Collectors.toList());
    }
    
    /**
     * Get categories for homepage
     */
    public List<HomePageResponse.CategoryResponse> getCategories() {
        List<Category> categories = categoryRepository.findAll();
        
        return categories.stream()
                        .limit(4) // Limit to 4 main categories for homepage
                        .map(category -> {
                            // TODO: Get actual product count per category
                            int productCount = 0; // productRepository.countByCategoryAndIsActiveTrue(category);
                            return new HomePageResponse.CategoryResponse(category, productCount);
                        })
                        .collect(Collectors.toList());
    }
    
    /**
     * Get goals for "Shop by Goals" section
     */
    public List<HomePageResponse.GoalResponse> getGoals() {
        List<Goal> goals = goalRepository.findAll();
        
        return goals.stream()
                   .limit(4) // Limit to 4 main goals for homepage
                   .map(goal -> {
                       // TODO: Get actual product count per goal
                       int productCount = 0; // productRepository.countByGoalAndIsActiveTrue(goal);
                       return new HomePageResponse.GoalResponse(goal, productCount);
                   })
                   .collect(Collectors.toList());
    }
    
    /**
     * Get sports for "Shop by Sports" section
     */
    public List<HomePageResponse.SportResponse> getSports() {
        List<Sport> sports = sportRepository.findAll();
        
        return sports.stream()
                    .limit(4) // Limit to 4 main sports for homepage
                    .map(sport -> {
                        // TODO: Get actual product count per sport
                        int productCount = 0; // productRepository.countBySportAndIsActiveTrue(sport);
                        return new HomePageResponse.SportResponse(sport, productCount);
                    })
                    .collect(Collectors.toList());
    }
    
    /**
     * Mock hero banners data
     */
    private List<HomePageResponse.BannerResponse> getMockHeroBanners() {
        List<HomePageResponse.BannerResponse> banners = new ArrayList<>();
        
        banners.add(new HomePageResponse.BannerResponse(
            1L, 
            "Premium Supplements for Professional Athletes", 
            "Discover high-quality imported supplements trusted by professionals",
            "/assets/banners/hero-banner-1.jpg",
            "/products/featured",
            true
        ));
        
        banners.add(new HomePageResponse.BannerResponse(
            2L,
            "TREC Nutrition - Official Partner",
            "Explore the complete range of TREC supplements",
            "/assets/banners/hero-banner-2.jpg", 
            "/products/brand/trec",
            true
        ));
        
        return banners;
    }
    
    /**
     * Mock weekly deal data
     */
    private HomePageResponse.WeeklyDealResponse getMockWeeklyDeal() {
        HomePageResponse.WeeklyDealResponse deal = new HomePageResponse.WeeklyDealResponse();
        deal.setProductId(101L);
        deal.setProductName("BCAA 5000 Powder");
        deal.setProductImage("/assets/products/bcaa-5000.jpg");
        deal.setOriginalPrice("₹2,499");
        deal.setDiscountedPrice("₹1,999");
        deal.setDiscountPercentage("20%");
        deal.setDealEndTime("2025-01-15T23:59:59Z"); // End of week
        deal.setDealDescription("Weekly Special: Premium BCAA supplement at unbeatable price!");
        
        return deal;
    }
    
    /**
     * Mock testimonials data
     */
    private List<HomePageResponse.TestimonialResponse> getMockTestimonials() {
        List<HomePageResponse.TestimonialResponse> testimonials = new ArrayList<>();
        
        HomePageResponse.TestimonialResponse testimonial1 = new HomePageResponse.TestimonialResponse();
        testimonial1.setId(1L);
        testimonial1.setAthleteName("Virat Kohli");
        testimonial1.setAthleteImage("/assets/athletes/virat-kohli.jpg");
        testimonial1.setSport("Cricket");
        testimonial1.setTestimonialText("SuppKart provides the highest quality supplements that help me maintain peak performance throughout the season.");
        testimonial1.setDesignation("Indian Cricket Team Captain");
        testimonials.add(testimonial1);
        
        HomePageResponse.TestimonialResponse testimonial2 = new HomePageResponse.TestimonialResponse();
        testimonial2.setId(2L);
        testimonial2.setAthleteName("Mary Kom");
        testimonial2.setAthleteImage("/assets/athletes/mary-kom.jpg");
        testimonial2.setSport("Boxing");
        testimonial2.setTestimonialText("The imported supplements from SuppKart have been instrumental in my training and recovery process.");
        testimonial2.setDesignation("Olympic Bronze Medalist");
        testimonials.add(testimonial2);
        
        HomePageResponse.TestimonialResponse testimonial3 = new HomePageResponse.TestimonialResponse();
        testimonial3.setId(3L);
        testimonial3.setAthleteName("Sania Nehwal");
        testimonial3.setAthleteImage("/assets/athletes/sania-nehwal.jpg");
        testimonial3.setSport("Tennis");
        testimonial3.setTestimonialText("Trust SuppKart for authentic, high-quality supplements that deliver results when it matters most.");
        testimonial3.setDesignation("Former World No. 1 Badminton Player");
        testimonials.add(testimonial3);
        
        return testimonials;
    }
    
    /**
     * Mock customer reviews data
     */
    private List<HomePageResponse.ReviewResponse> getMockCustomerReviews() {
        List<HomePageResponse.ReviewResponse> reviews = new ArrayList<>();
        
        HomePageResponse.ReviewResponse review1 = new HomePageResponse.ReviewResponse();
        review1.setId(1L);
        review1.setCustomerName("Rajesh Kumar");
        review1.setCustomerImage("/assets/customers/default-avatar.jpg");
        review1.setRating(5);
        review1.setReviewText("Excellent quality supplements. Noticed significant improvement in my performance within weeks!");
        review1.setProductName("Whey Protein Isolate");
        review1.setReviewDate("2025-01-05");
        reviews.add(review1);
        
        HomePageResponse.ReviewResponse review2 = new HomePageResponse.ReviewResponse();
        review2.setId(2L);
        review2.setCustomerName("Priya Sharma");
        review2.setCustomerImage("/assets/customers/default-avatar.jpg");
        review2.setRating(5);
        review2.setReviewText("Fast delivery and authentic products. SuppKart is my go-to store for all supplement needs.");
        review2.setProductName("Multivitamin Complex");
        review2.setReviewDate("2025-01-03");
        reviews.add(review2);
        
        HomePageResponse.ReviewResponse review3 = new HomePageResponse.ReviewResponse();
        review3.setId(3L);
        review3.setCustomerName("Amit Singh");
        review3.setCustomerImage("/assets/customers/default-avatar.jpg");
        review3.setRating(4);
        review3.setReviewText("Great product range and competitive prices. The consultation service is very helpful.");
        review3.setProductName("Pre-Workout Formula");
        review3.setReviewDate("2025-01-01");
        reviews.add(review3);
        
        return reviews;
    }
}
