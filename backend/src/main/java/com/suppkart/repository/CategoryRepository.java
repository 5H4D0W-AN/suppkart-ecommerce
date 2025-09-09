package com.suppkart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Find category by slug
     */
    Optional<Category> findBySlug(String slug);
    
    /**
     * Find all active categories
     */
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> findActiveCategories();
    
    /**
     * Find categories by name (partial match)
     */
    @Query("SELECT c FROM Category c WHERE c.isActive = true AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Category> findByNameContaining(@Param("name") String name);
    
    /**
     * Check if slug exists
     */
    boolean existsBySlug(String slug);
    
    /**
     * Check if name exists
     */
    boolean existsByName(String name);
    
    /**
     * Count active categories
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.isActive = true")
    long countActiveCategories();
    
    /**
     * Find categories with product count
     */
    @Query("SELECT c, COUNT(DISTINCT pc.product) as productCount FROM Category c " +
           "LEFT JOIN ProductCategory pc ON pc.categoryId = c.categoryId " +
           "LEFT JOIN Product p ON pc.productId = p.productId AND p.isActive = true " +
           "WHERE c.isActive = true " +
           "GROUP BY c.categoryId " +
           "ORDER BY c.displayOrder ASC")
    List<Object[]> findCategoriesWithProductCount();
    
    /**
     * Find top categories by product count
     */
    @Query("SELECT c FROM Category c " +
           "LEFT JOIN ProductCategory pc ON pc.categoryId = c.categoryId " +
           "LEFT JOIN Product p ON pc.productId = p.productId AND p.isActive = true " +
           "WHERE c.isActive = true " +
           "GROUP BY c.categoryId " +
           "ORDER BY COUNT(DISTINCT p.productId) DESC")
    List<Category> findTopCategoriesByProductCount();
    
    /**
     * Find active categories ordered by name
     */
    List<Category> findByIsActiveTrueOrderByNameAsc();
    
    /**
     * Find active categories ordered by display order and name
     */
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> findByIsActiveTrueOrderByDisplayOrderAscNameAsc();
}
