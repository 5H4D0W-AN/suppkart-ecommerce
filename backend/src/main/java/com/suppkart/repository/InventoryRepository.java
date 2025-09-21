package com.suppkart.repository;

import com.suppkart.model.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Find inventory by product ID and variant ID
     */
    @Query("SELECT i FROM Inventory i WHERE i.product.productId = :productId AND i.variant.variantId = :variantId")
    Optional<Inventory> findByProductIdAndVariantId(@Param("productId") Long productId, @Param("variantId") Long variantId);

    /**
     * Find inventory by product ID where variant is null (for products without variants)
     */
    @Query("SELECT i FROM Inventory i WHERE i.product.productId = :productId AND i.variant IS NULL")
    Optional<Inventory> findByProductIdAndVariantIdIsNull(@Param("productId") Long productId);

    /**
     * Find all inventory items where quantity is less than or equal to the low stock threshold
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.lowStockThreshold")
    List<Inventory> findLowStockItems();

    /**
     * Find all inventory items where quantity equals zero (out of stock)
     */
    List<Inventory> findByQuantityEquals(Integer quantity);

    /**
     * Find all out of stock items
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantity = 0")
    List<Inventory> findOutOfStockItems();

    /**
     * Find inventory by product ID
     */
    @Query("SELECT i FROM Inventory i WHERE i.product.productId = :productId")
    List<Inventory> findByProductId(@Param("productId") Long productId);

    /**
     * Find inventory items with quantity less than specified threshold
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantity < :threshold")
    List<Inventory> findByQuantityLessThan(@Param("threshold") Integer threshold);

    /**
     * Find inventory items with quantity between min and max values
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantity BETWEEN :min AND :max")
    List<Inventory> findByQuantityBetween(@Param("min") Integer min, @Param("max") Integer max);

    /**
     * Find inventory items that need restocking (quantity <= threshold)
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= :threshold")
    List<Inventory> findItemsNeedingRestock(@Param("threshold") Integer threshold);

    /**
     * Count total inventory items
     */
    @Query("SELECT COUNT(i) FROM Inventory i")
    Long countTotalItems();

    /**
     * Count low stock items
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.quantity <= i.lowStockThreshold")
    Long countLowStockItems();

    /**
     * Count out of stock items
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.quantity = 0")
    Long countOutOfStockItems();

    /**
     * Find inventory by product name (case insensitive search)
     */
    @Query("SELECT i FROM Inventory i WHERE LOWER(i.product.name) LIKE LOWER(CONCAT('%', :productName, '%'))")
    List<Inventory> findByProductNameContainingIgnoreCase(@Param("productName") String productName);

    /**
     * Find inventory by variant SKU
     */
    @Query("SELECT i FROM Inventory i WHERE i.variant.sku = :sku")
    Optional<Inventory> findByVariantSku(@Param("sku") String sku);

    /**
     * Find inventory items by category
     */
    @Query("SELECT i FROM Inventory i JOIN i.product.productCategories pc WHERE pc.category.categoryId = :categoryId")
    List<Inventory> findByProductCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Find all inventory items with filters
     */
    @Query("SELECT i FROM Inventory i " +
           "LEFT JOIN i.product p " +
           "LEFT JOIN i.variant v " +
           "LEFT JOIN p.productCategories pc " +
           "LEFT JOIN pc.category c " +
           "WHERE (:search IS NULL OR " +
           "       LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "       LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "       (v IS NOT NULL AND LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%'))) OR " +
           "       (v IS NOT NULL AND LOWER(v.sku) LIKE LOWER(CONCAT('%', :search, '%')))) " +
           "AND (:categoryId IS NULL OR c.categoryId = :categoryId) " +
           "AND (:brandName IS NULL OR LOWER(p.brand) = LOWER(:brandName)) " +
           "AND (:lowStock IS NULL OR :lowStock = false OR i.quantity <= i.lowStockThreshold) " +
           "AND (:outOfStock IS NULL OR :outOfStock = false OR i.quantity = 0) " +
           "AND (:inStock IS NULL OR :inStock = false OR i.quantity > 0) " +
           "AND (:status IS NULL OR p.isActive = true) " +
           "AND (:minQuantity IS NULL OR i.quantity >= :minQuantity) " +
           "AND (:maxQuantity IS NULL OR i.quantity <= :maxQuantity)")
    Page<Inventory> findAllWithFilters(
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("brandName") String brandName,
            @Param("lowStock") Boolean lowStock,
            @Param("outOfStock") Boolean outOfStock,
            @Param("inStock") Boolean inStock,
            @Param("status") String status,
            @Param("minQuantity") Integer minQuantity,
            @Param("maxQuantity") Integer maxQuantity,
            Pageable pageable);
}
