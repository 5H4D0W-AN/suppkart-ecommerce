package com.suppkart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.Product;
import com.suppkart.model.entity.Review;
import com.suppkart.model.entity.User;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    /**
     * Find all approved and visible reviews for a product
     */
    @Query("SELECT r FROM Review r WHERE r.product.productId = :productId " +
           "AND r.approved = true AND r.isVisible = true " +
           "ORDER BY r.createdAt DESC")
    Page<Review> findByProductIdAndApprovedTrueAndIsVisibleTrueOrderByCreatedAtDesc(
        @Param("productId") Long productId, Pageable pageable);
    
    /**
     * Count approved and visible reviews for a product
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.productId = :productId " +
           "AND r.approved = true AND r.isVisible = true")
    Long countByProductIdAndApprovedTrueAndIsVisible(@Param("productId") Long productId);
    
    /**
     * Find review by user and product (to prevent duplicate reviews)
     */
    Optional<Review> findByUserAndProduct(User user, Product product);
    
    /**
     * Check if user has already reviewed a product
     */
    boolean existsByUserAndProduct(User user, Product product);
    
    /**
     * Find pending reviews for admin moderation
     */
    @Query("SELECT r FROM Review r WHERE r.approved = false " +
           "ORDER BY r.createdAt ASC")
    Page<Review> findPendingReviewsOrderByCreatedAtAsc(Pageable pageable);
    
    /**
     * Find all reviews for a product (admin view - includes non-approved)
     */
    @Query("SELECT r FROM Review r WHERE r.product.productId = :productId " +
           "ORDER BY r.createdAt DESC")
    Page<Review> findAllByProductIdOrderByCreatedAtDesc(
        @Param("productId") Long productId, Pageable pageable);
    
    /**
     * Find reviews by user
     */
    Page<Review> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    /**
     * Calculate average rating for a product (approved reviews only)
     */
    @Query("SELECT AVG(CAST(r.rating AS double)) FROM Review r " +
           "WHERE r.product.productId = :productId AND r.approved = true AND r.isVisible = true")
    Double calculateAverageRatingByProductId(@Param("productId") Long productId);
    
    /**
     * Get rating distribution for a product
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r " +
           "WHERE r.product.productId = :productId AND r.approved = true AND r.isVisible = true " +
           "GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistributionByProductId(@Param("productId") Long productId);
    
    /**
     * Find reviews by rating range for a product
     */
    @Query("SELECT r FROM Review r WHERE r.product.productId = :productId " +
           "AND r.approved = true AND r.isVisible = true " +
           "AND r.rating BETWEEN :minRating AND :maxRating " +
           "ORDER BY r.createdAt DESC")
    Page<Review> findByProductIdAndRatingBetweenOrderByCreatedAtDesc(
        @Param("productId") Long productId, 
        @Param("minRating") Integer minRating, 
        @Param("maxRating") Integer maxRating, 
        Pageable pageable);
    
    /**
     * Find verified purchase reviews for a product
     */
    @Query("SELECT r FROM Review r WHERE r.product.productId = :productId " +
           "AND r.approved = true AND r.isVisible = true AND r.verified = true " +
           "ORDER BY r.createdAt DESC")
    Page<Review> findVerifiedReviewsByProductIdOrderByCreatedAtDesc(
        @Param("productId") Long productId, Pageable pageable);
    
    /**
     * Find most helpful reviews for a product
     */
    @Query("SELECT r FROM Review r WHERE r.product.productId = :productId " +
           "AND r.approved = true AND r.isVisible = true " +
           "ORDER BY r.helpfulVotes DESC, r.createdAt DESC")
    Page<Review> findMostHelpfulByProductIdOrderByHelpfulVotesDesc(
        @Param("productId") Long productId, Pageable pageable);
    
    /**
     * Find reviews that need admin attention (flagged, reported, etc.)
     */
    @Query("SELECT r FROM Review r WHERE r.approved = false OR r.isVisible = false " +
           "ORDER BY r.updatedAt DESC")
    Page<Review> findReviewsNeedingAttention(Pageable pageable);
    
    /**
     * Get review statistics for admin dashboard
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN r.approved = true THEN 1 END) as approvedCount, " +
           "COUNT(CASE WHEN r.approved = false THEN 1 END) as pendingCount, " +
           "COUNT(CASE WHEN r.createdByAdmin = true THEN 1 END) as adminCreatedCount, " +
           "COUNT(CASE WHEN r.verified = true THEN 1 END) as verifiedCount, " +
           "AVG(CAST(r.rating AS double)) as averageRating " +
           "FROM Review r")
    Object[] getReviewStatistics();
    
    /**
     * Find admin-created reviews for a product (admin function)
     */
    @Query("SELECT r FROM Review r WHERE r.product.productId = :productId " +
           "AND r.createdByAdmin = true ORDER BY r.createdAt DESC")
    List<Review> findAdminCreatedReviewsByProductId(@Param("productId") Long productId);
    
    /**
     * Delete all reviews for a product (admin function)
     */
    void deleteByProduct(Product product);
    
    /**
     * Delete all reviews by a user
     */
    void deleteByUser(User user);
}
