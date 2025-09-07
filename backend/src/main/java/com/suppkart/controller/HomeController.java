package com.suppkart.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suppkart.dto.response.ApiResponse;
import com.suppkart.dto.response.HomePageResponse;
import com.suppkart.service.HomeService;

@RestController
@RequestMapping("/api/home")
public class HomeController {
    
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    
    @Autowired
    private HomeService homeService;
    
    /**
     * Get homepage data
     */
    @GetMapping
    public ResponseEntity<ApiResponse<HomePageResponse>> getHomePageData() {
        logger.info("Received request to get homepage data");
        
        try {
            HomePageResponse homePageData = homeService.getHomePageData();
            
            return ResponseEntity.ok(ApiResponse.success(
                "Homepage data retrieved successfully",
                homePageData
            ));
            
        } catch (Exception e) {
            logger.error("Error retrieving homepage data", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve homepage data: " + e.getMessage()));
        }
    }
    
    /**
     * Get featured products only
     */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<?>> getFeaturedProducts() {
        logger.info("Received request to get featured products");
        
        try {
            var featuredProducts = homeService.getFeaturedProducts();
            
            return ResponseEntity.ok(ApiResponse.success(
                "Featured products retrieved successfully",
                featuredProducts
            ));
            
        } catch (Exception e) {
            logger.error("Error retrieving featured products", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve featured products: " + e.getMessage()));
        }
    }
    
    /**
     * Get best seller products only
     */
    @GetMapping("/bestsellers")
    public ResponseEntity<ApiResponse<?>> getBestSellers() {
        logger.info("Received request to get best seller products");
        
        try {
            var bestSellers = homeService.getBestSellers();
            
            return ResponseEntity.ok(ApiResponse.success(
                "Best sellers retrieved successfully",
                bestSellers
            ));
            
        } catch (Exception e) {
            logger.error("Error retrieving best sellers", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve best sellers: " + e.getMessage()));
        }
    }
    
    /**
     * Get top products only
     */
    @GetMapping("/top")
    public ResponseEntity<ApiResponse<?>> getTopProducts() {
        logger.info("Received request to get top products");
        
        try {
            var topProducts = homeService.getTopProducts();
            
            return ResponseEntity.ok(ApiResponse.success(
                "Top products retrieved successfully",
                topProducts
            ));
            
        } catch (Exception e) {
            logger.error("Error retrieving top products", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve top products: " + e.getMessage()));
        }
    }
    
    /**
     * Get categories for homepage
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<?>> getCategories() {
        logger.info("Received request to get homepage categories");
        
        try {
            var categories = homeService.getCategories();
            
            return ResponseEntity.ok(ApiResponse.success(
                "Categories retrieved successfully",
                categories
            ));
            
        } catch (Exception e) {
            logger.error("Error retrieving categories", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve categories: " + e.getMessage()));
        }
    }
    
    /**
     * Get sports for homepage
     */
    @GetMapping("/sports")
    public ResponseEntity<ApiResponse<?>> getSports() {
        logger.info("Received request to get homepage sports");
        
        try {
            var sports = homeService.getSports();
            
            return ResponseEntity.ok(ApiResponse.success(
                "Sports retrieved successfully",
                sports
            ));
            
        } catch (Exception e) {
            logger.error("Error retrieving sports", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve sports: " + e.getMessage()));
        }
    }
    
    /**
     * Get goals for homepage
     */
    @GetMapping("/goals")
    public ResponseEntity<ApiResponse<?>> getGoals() {
        logger.info("Received request to get homepage goals");
        
        try {
            var goals = homeService.getGoals();
            
            return ResponseEntity.ok(ApiResponse.success(
                "Goals retrieved successfully",
                goals
            ));
            
        } catch (Exception e) {
            logger.error("Error retrieving goals", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve goals: " + e.getMessage()));
        }
    }
}
