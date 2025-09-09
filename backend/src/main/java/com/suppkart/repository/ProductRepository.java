package com.suppkart.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.Product;
import com.suppkart.model.enums.Brand;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    
    /**
     * Find product by slug
     */
    Optional<Product> findBySlug(String slug);
    
    /**
     * Find active products
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    List<Product> findActiveProducts();
    
    /**
     * Find active products with pagination
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true")
    Page<Product> findActiveProducts(Pageable pageable);
    
    /**
     * Find highlighted/featured products
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.isHighlighted = true ORDER BY p.createdAt DESC")
    List<Product> findHighlightedProducts();
    
    /**
     * Find products by brand
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.brand = :brand ORDER BY p.createdAt DESC")
    List<Product> findByBrand(@Param("brand") Brand brand);
    
    /**
     * Find products by brand with pagination
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.brand = :brand")
    Page<Product> findByBrand(@Param("brand") Brand brand, Pageable pageable);
    
    /**
     * Find products with price range
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "JOIN p.variants v " +
           "WHERE p.isActive = true AND v.isActive = true " +
           "AND (v.salePrice IS NOT NULL AND v.salePrice BETWEEN :minPrice AND :maxPrice " +
           "OR v.salePrice IS NULL AND v.price BETWEEN :minPrice AND :maxPrice)")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    /**
     * Search products by name or description
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);
    
    /**
     * Search products with pagination
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Find products by category
     */
    @Query("SELECT p FROM Product p " +
           "JOIN p.productCategories pc " +
           "WHERE p.isActive = true AND pc.category.categoryId = :categoryId " +
           "ORDER BY pc.displayOrder ASC, p.createdAt DESC")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);
    
    /**
     * Find products by category with pagination
     */
    @Query("SELECT p FROM Product p " +
           "JOIN p.productCategories pc " +
           "WHERE p.isActive = true AND pc.category.categoryId = :categoryId " +
           "ORDER BY pc.displayOrder ASC, p.createdAt DESC")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
    
    /**
     * Find products by sport
     */
    @Query("SELECT p FROM Product p " +
           "JOIN p.productSports ps " +
           "WHERE p.isActive = true AND ps.sport.sportId = :sportId " +
           "ORDER BY ps.relevance DESC, p.createdAt DESC")
    List<Product> findBySportId(@Param("sportId") Long sportId);
    
    /**
     * Find products by goal
     */
    @Query("SELECT p FROM Product p " +
           "JOIN p.productGoals pg " +
           "WHERE p.isActive = true AND pg.goal.goalId = :goalId " +
           "ORDER BY pg.effectiveness DESC, p.createdAt DESC")
    List<Product> findByGoalId(@Param("goalId") Long goalId);
    
    /**
     * Find products with low stock
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "JOIN p.variants v " +
           "WHERE p.isActive = true AND v.isActive = true " +
           "AND v.stockQuantity <= p.lowStockThreshold")
    List<Product> findLowStockProducts();
    
    /**
     * Count products by brand
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true AND p.brand = :brand")
    long countByBrand(@Param("brand") Brand brand);
    
    /**
     * Count products by category
     */
    @Query("SELECT COUNT(DISTINCT p) FROM Product p " +
           "JOIN p.productCategories pc " +
           "WHERE p.isActive = true AND pc.category.categoryId = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);
    
    /**
     * Find products by SKU
     */
    Optional<Product> findBySku(String sku);
    
    /**
     * Check if slug exists
     */
    boolean existsBySlug(String slug);
    
    /**
     * Check if SKU exists
     */
    boolean existsBySku(String sku);
    
    /**
     * Find top rated products
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.avgRating >= :minRating ORDER BY p.avgRating DESC")
    List<Product> findTopRatedProducts(@Param("minRating") BigDecimal minRating);
    
    /**
     * Find recently added products
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    List<Product> findRecentlyAddedProducts(Pageable pageable);
    
    /**
     * Find active products with pagination
     */
    Page<Product> findByIsActiveTrue(Pageable pageable);
    
    /**
     * Find product by ID and active status
     */
    Optional<Product> findByProductIdAndIsActiveTrue(Long productId);
    
    /**
     * Check if product exists by ID and active status
     */
    boolean existsByProductIdAndIsActiveTrue(Long productId);
    
    /**
     * Find products by brand and active status with pagination
     */
    Page<Product> findByBrandAndIsActiveTrue(Brand brand, Pageable pageable);
    
    /**
     * Find products by category and active status with pagination
     */
    @Query("SELECT p FROM Product p " +
           "JOIN p.productCategories pc " +
           "WHERE p.isActive = true AND pc.category.categoryId = :categoryId")
    Page<Product> findByCategoryCategoryIdAndActiveTrue(@Param("categoryId") Long categoryId, Pageable pageable);
    
    /**
     * Search products by name or description with active status and pagination
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%')))")
    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndActiveTrue(
            @Param("name") String name, @Param("description") String description, Pageable pageable);
    
    /**
     * Find featured products that are active
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.isHighlighted = true ORDER BY p.createdAt DESC")
    List<Product> findByFeaturedTrueAndActiveTrueOrderByCreatedAtDesc();
    
    /**
     * Find related products based on category
     */
    @Query("SELECT p FROM Product p " +
           "JOIN p.productCategories pc " +
           "WHERE p.isActive = true AND p.productId != :productId " +
           "AND pc.category.categoryId = :categoryId " +
           "ORDER BY p.createdAt DESC")
    List<Product> findRelatedProducts(@Param("productId") Long productId,
                                      @Param("categoryId") Long categoryId,
                                      Pageable pageable);
    
    /**
     * Find related products with limit
     */
    default List<Product> findRelatedProducts(Long productId, Long categoryId, int limit) {
        return findRelatedProducts(productId, categoryId, 
                org.springframework.data.domain.PageRequest.of(0, limit));
    }
    
    /**
     * Find highlighted/featured products that are active with pagination
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.isHighlighted = true ORDER BY p.createdAt DESC")
    List<Product> findByIsHighlightedTrueAndIsActiveTrue(Pageable pageable);
    
    /**
     * Find active products ordered by rating and review count
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.avgRating DESC, p.reviewCount DESC")
    List<Product> findByIsActiveTrueOrderByAvgRatingDescReviewCountDesc(Pageable pageable);
}
