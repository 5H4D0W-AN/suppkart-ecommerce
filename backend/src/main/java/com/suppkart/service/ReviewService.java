package com.suppkart.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suppkart.dto.request.FakeReviewRequest;
import com.suppkart.dto.request.ReviewSubmitRequest;
import com.suppkart.dto.response.ReviewDto;
import com.suppkart.exception.BusinessException;
import com.suppkart.exception.ReviewException;
import com.suppkart.model.entity.Product;
import com.suppkart.model.entity.Review;
import com.suppkart.model.entity.User;
import com.suppkart.repository.OrderItemRepository;
import com.suppkart.repository.ProductRepository;
import com.suppkart.repository.ReviewRepository;
import com.suppkart.repository.UserRepository;

@Service
@Transactional
public class ReviewService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);
    
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    
    public ReviewService(ReviewRepository reviewRepository, 
                        ProductRepository productRepository,
                        UserRepository userRepository,
                        OrderItemRepository orderItemRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
    }
    
    /**
     * Submit a new review for a product
     */
    public ReviewDto submitReview(Long productId, Long userId, ReviewSubmitRequest request) {
        logger.info("Submitting review for product {} by user {}", productId, userId);
        
        // Validate product exists
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found with id: " + productId));
        
        // Validate user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found with id: " + userId));
        
        // Check if user already reviewed this product
        if (reviewRepository.existsByUserAndProduct(user, product)) {
            throw new ReviewException("You have already reviewed this product");
        }
        
        // Check if this is a verified purchase
        boolean isVerified = hasUserPurchasedProduct(userId, productId);
        
        // Create review
        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setContent(request.getContent());
        review.setVerified(isVerified);
        review.setApproved(false); // Needs moderation
        review.setIsVisible(true);
        review.setHelpfulVotes(0);
        
        Review savedReview = reviewRepository.save(review);
        
        logger.info("Review submitted successfully with id: {}", savedReview.getReviewId());
        return mapToDto(savedReview);
    }
    
    /**
     * Get reviews for a product (customer-facing)
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto> getProductReviews(Long productId, Integer page, Integer size) {
        logger.debug("Fetching reviews for product: {}", productId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findByProductIdAndApprovedTrueAndIsVisibleTrueOrderByCreatedAtDesc(
            productId, pageable);
        
        return reviews.map(this::mapToDto);
    }
    
    /**
     * Get reviews by rating filter
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto> getProductReviewsByRating(Long productId, Integer rating, Integer page, Integer size) {
        logger.debug("Fetching reviews for product: {} with rating: {}", productId, rating);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findByProductIdAndRatingBetweenOrderByCreatedAtDesc(
            productId, rating, rating, pageable);
        
        return reviews.map(this::mapToDto);
    }
    
    /**
     * Get verified reviews only
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto> getVerifiedProductReviews(Long productId, Integer page, Integer size) {
        logger.debug("Fetching verified reviews for product: {}", productId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findVerifiedReviewsByProductIdOrderByCreatedAtDesc(
            productId, pageable);
        
        return reviews.map(this::mapToDto);
    }
    
    /**
     * Get most helpful reviews
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto> getMostHelpfulReviews(Long productId, Integer page, Integer size) {
        logger.debug("Fetching most helpful reviews for product: {}", productId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewRepository.findMostHelpfulByProductIdOrderByHelpfulVotesDesc(
            productId, pageable);
        
        return reviews.map(this::mapToDto);
    }
    
    /**
     * Approve a review (admin function)
     */
    public ReviewDto approveReview(Long reviewId) {
        logger.info("Approving review with id: {}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new BusinessException("REVIEW_NOT_FOUND", "Review not found with id: " + reviewId));
        
        review.setApproved(true);
        review.setIsVisible(true);
        
        Review savedReview = reviewRepository.save(review);
        
        // Update product rating stats
        updateProductRatingStats(savedReview.getProduct());
        
        logger.info("Review approved successfully: {}", reviewId);
        return mapToDto(savedReview);
    }
    
    /**
     * Reject a review (admin function)
     */
    public void rejectReview(Long reviewId) {
        logger.info("Rejecting review with id: {}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new BusinessException("REVIEW_NOT_FOUND", "Review not found with id: " + reviewId));
        
        review.setApproved(false);
        review.setIsVisible(false);
        
        reviewRepository.save(review);
        
        // Update product rating stats
        updateProductRatingStats(review.getProduct());
        
        logger.info("Review rejected successfully: {}", reviewId);
    }
    
    /**
     * Delete a review (admin function)
     */
    public void deleteReview(Long reviewId) {
        logger.info("Deleting review with id: {}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new BusinessException("REVIEW_NOT_FOUND", "Review not found with id: " + reviewId));
        
        Product product = review.getProduct();
        reviewRepository.delete(review);
        
        // Update product rating stats
        updateProductRatingStats(product);
        
        logger.info("Review deleted successfully: {}", reviewId);
    }
    
    /**
     * Toggle review visibility (admin function)
     */
    public ReviewDto toggleReviewVisibility(Long reviewId) {
        logger.info("Toggling visibility for review with id: {}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new BusinessException("REVIEW_NOT_FOUND", "Review not found with id: " + reviewId));
        
        review.setIsVisible(!review.getIsVisible());
        Review savedReview = reviewRepository.save(review);
        
        // Update product rating stats
        updateProductRatingStats(savedReview.getProduct());
        
        logger.info("Review visibility toggled successfully: {} -> {}", reviewId, savedReview.getIsVisible());
        return mapToDto(savedReview);
    }
    
    /**
     * Get pending reviews for moderation (admin function)
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto> getPendingReviews(Integer page, Integer size) {
        logger.debug("Fetching pending reviews for moderation");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<Review> reviews = reviewRepository.findPendingReviewsOrderByCreatedAtAsc(pageable);
        
        return reviews.map(this::mapToDto);
    }
    
    /**
     * Get all reviews for a product (admin function)
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto> getAllProductReviews(Long productId, Integer page, Integer size) {
        logger.debug("Fetching all reviews for product: {} (admin view)", productId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findAllByProductIdOrderByCreatedAtDesc(productId, pageable);
        
        return reviews.map(this::mapToDto);
    }
    
    /**
     * Create fake reviews for a product (admin function)
     */
    public List<ReviewDto> createFakeReviews(Long productId, List<FakeReviewRequest> fakeReviewRequests) {
        logger.info("Creating {} fake reviews for product: {}", fakeReviewRequests.size(), productId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found with id: " + productId));
        
        List<Review> fakeReviews = new ArrayList<>();
        Random random = new Random();
        
        for (FakeReviewRequest fakeReviewRequest : fakeReviewRequests) {
            Review review = new Review();
            review.setProduct(product);
            review.setUser(null); // No user for fake reviews
            review.setRating(fakeReviewRequest.getRating());
            review.setTitle(fakeReviewRequest.getTitle());
            review.setContent(fakeReviewRequest.getContent());
            review.setDisplayName(fakeReviewRequest.getDisplayName()); // Set custom display name
            review.setVerified(true); // Fake reviews are "verified"
            review.setApproved(true);
            review.setIsVisible(true);
            review.setCreatedByAdmin(true);
            review.setHelpfulVotes(random.nextInt(10)); // Random helpful votes 0-9
            
            // Set creation date
            if (fakeReviewRequest.getCreatedAt() != null) {
                review.setCreatedAt(fakeReviewRequest.getCreatedAt());
            } else {
                // Random date within last 30 days
                review.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30)));
            }
            
            fakeReviews.add(review);
        }
        
        List<Review> savedReviews = reviewRepository.saveAll(fakeReviews);
        
        // Update product rating stats
        updateProductRatingStats(product);
        
        logger.info("Created {} fake reviews successfully for product: {}", savedReviews.size(), productId);
        return savedReviews.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    /**
     * Get review statistics for admin dashboard
     */
    @Transactional(readOnly = true)
    public Object[] getReviewStatistics() {
        return reviewRepository.getReviewStatistics();
    }
    
    /**
     * Update product rating statistics
     */
    public void updateProductRatingStats(Product product) {
        logger.debug("Updating rating stats for product: {}", product.getProductId());
        
        // Calculate average rating from approved and visible reviews
        Double averageRating = reviewRepository.calculateAverageRatingByProductId(product.getProductId());
        Long reviewCount = reviewRepository.countByProductIdAndApprovedTrueAndIsVisible(product.getProductId());
        
        if (averageRating != null && reviewCount != null) {
            product.setAvgRating(BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP));
            product.setReviewCount(reviewCount.intValue());
        } else {
            product.setAvgRating(BigDecimal.ZERO);
            product.setReviewCount(0);
        }
        
        productRepository.save(product);
        
        logger.debug("Updated product {} rating stats: avgRating={}, reviewCount={}", 
            product.getProductId(), product.getAvgRating(), product.getReviewCount());
    }
    
    /**
     * Check if user has purchased the product (verified purchase)
     */
    private boolean hasUserPurchasedProduct(Long userId, Long productId) {
        try {
            // Check if user has any completed orders containing this product
            return orderItemRepository.hasUserPurchasedProduct(userId, productId);
        } catch (Exception e) {
            logger.warn("Error checking purchase verification for user {} and product {}: {}", 
                userId, productId, e.getMessage());
            return false; // Default to unverified if we can't determine
        }
    }
    
    /**
     * Map Review entity to ReviewDto
     */
    private ReviewDto mapToDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setReviewId(review.getReviewId());
        dto.setProductId(review.getProduct().getProductId());
        
        if (review.getUser() != null) {
            dto.setUserId(review.getUser().getUserId());
            dto.setUserName(review.getUserDisplayName());
        } else {
            dto.setUserId(null);
            dto.setUserName("Anonymous User");
        }
        
        dto.setRating(review.getRating());
        dto.setTitle(review.getTitle());
        dto.setContent(review.getContent());
        dto.setVerified(review.getVerified());
        dto.setApproved(review.getApproved());
        dto.setIsVisible(review.getIsVisible());
        dto.setHelpfulVotes(review.getHelpfulVotes());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        
        return dto;
    }
}
