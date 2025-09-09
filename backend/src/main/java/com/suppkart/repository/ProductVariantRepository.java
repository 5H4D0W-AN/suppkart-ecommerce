package com.suppkart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    /**
     * Find all variants for a product
     */
    List<ProductVariant> findByProduct_ProductIdOrderByIsDefaultDescCreatedAtAsc(Long productId);

    /**
     * Find active variants for a product
     */
    List<ProductVariant> findByProduct_ProductIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtAsc(Long productId);

    /**
     * Find variants with stock for a product
     */
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.productId = :productId AND pv.stockQuantity > 0 ORDER BY pv.isDefault DESC, pv.createdAt ASC")
    List<ProductVariant> findVariantsWithStockByProductId(@Param("productId") Long productId);

    /**
     * Find default variant for a product
     */
    Optional<ProductVariant> findByProduct_ProductIdAndIsDefaultTrue(Long productId);

    /**
     * Find variant by SKU
     */
    Optional<ProductVariant> findBySku(String sku);

    /**
     * Find variants by flavor
     */
    List<ProductVariant> findByFlavorIgnoreCaseOrderByCreatedAtAsc(String flavor);

    /**
     * Find variants by size
     */
    List<ProductVariant> findBySizeIgnoreCaseOrderByCreatedAtAsc(String size);

    /**
     * Find variants by flavor and size for a product
     */
    Optional<ProductVariant> findByProduct_ProductIdAndFlavorIgnoreCaseAndSizeIgnoreCase(Long productId, String flavor, String size);

    /**
     * Check if variant exists with same flavor and size for product
     */
    boolean existsByProduct_ProductIdAndFlavorIgnoreCaseAndSizeIgnoreCaseAndVariantIdNot(Long productId, String flavor, String size, Long variantId);

    /**
     * Check if SKU exists
     */
    boolean existsBySku(String sku);

    /**
     * Check if SKU exists for different variant
     */
    boolean existsBySkuAndVariantIdNot(String sku, Long variantId);

    /**
     * Count variants for a product
     */
    long countByProduct_ProductId(Long productId);

    /**
     * Count active variants for a product
     */
    long countByProduct_ProductIdAndIsActiveTrue(Long productId);

    /**
     * Remove default status from all variants of a product
     */
    @Modifying
    @Query("UPDATE ProductVariant pv SET pv.isDefault = false WHERE pv.product.productId = :productId")
    void removeDefaultStatusForProduct(@Param("productId") Long productId);

    /**
     * Find variants with low stock
     */
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.stockQuantity <= :threshold AND pv.isActive = true")
    List<ProductVariant> findVariantsWithLowStock(@Param("threshold") Integer threshold);

    /**
     * Find out of stock variants
     */
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.stockQuantity = 0 AND pv.isActive = true")
    List<ProductVariant> findOutOfStockVariants();

    /**
     * Update stock quantity for a variant
     */
    @Modifying
    @Query("UPDATE ProductVariant pv SET pv.stockQuantity = :stockQuantity, pv.updatedAt = CURRENT_TIMESTAMP WHERE pv.variantId = :variantId")
    void updateStock(@Param("variantId") Long variantId, @Param("stockQuantity") Integer stockQuantity);

    /**
     * Reduce stock quantity for a variant
     */
    @Modifying
    @Query("UPDATE ProductVariant pv SET pv.stockQuantity = pv.stockQuantity - :quantity, pv.updatedAt = CURRENT_TIMESTAMP WHERE pv.variantId = :variantId AND pv.stockQuantity >= :quantity")
    int reduceStock(@Param("variantId") Long variantId, @Param("quantity") Integer quantity);

    /**
     * Increase stock quantity for a variant
     */
    @Modifying
    @Query("UPDATE ProductVariant pv SET pv.stockQuantity = pv.stockQuantity + :quantity, pv.updatedAt = CURRENT_TIMESTAMP WHERE pv.variantId = :variantId")
    void increaseStock(@Param("variantId") Long variantId, @Param("quantity") Integer quantity);

    /**
     * Find variants by price range
     */
    @Query("SELECT pv FROM ProductVariant pv WHERE " +
           "(:minPrice IS NULL OR COALESCE(pv.salePrice, pv.price) >= :minPrice) AND " +
           "(:maxPrice IS NULL OR COALESCE(pv.salePrice, pv.price) <= :maxPrice) AND " +
           "pv.isActive = true")
    List<ProductVariant> findVariantsByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    /**
     * Calculate total stock for a product across all variants
     */
    @Query("SELECT COALESCE(SUM(pv.stockQuantity), 0) FROM ProductVariant pv WHERE pv.product.productId = :productId AND pv.isActive = true")
    Integer getTotalStockForProduct(@Param("productId") Long productId);

    /**
     * Find variants on sale
     */
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.salePrice IS NOT NULL AND pv.salePrice < pv.price AND pv.isActive = true")
    List<ProductVariant> findVariantsOnSale();

    /**
     * Delete all variants for a product
     */
    void deleteByProduct_ProductId(Long productId);
    
    /**
     * Find variant by ID and active status
     */
    Optional<ProductVariant> findByVariantIdAndIsActiveTrue(Long variantId);
    
    /**
     * Find active variants for a product by product ID
     */
    List<ProductVariant> findByProduct_ProductIdAndIsActiveTrue(Long productId);
}
