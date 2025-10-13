package com.suppkart.repository;

import com.suppkart.model.entity.Product;
import com.suppkart.model.entity.ProductImage;
import com.suppkart.model.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    // Existing methods for product images
    List<ProductImage> findByProductOrderBySortOrder(Product product);
    Optional<ProductImage> findByProductAndIsPrimaryTrue(Product product);
    
    // New methods for variant images
    List<ProductImage> findByVariantOrderBySortOrder(ProductVariant variant);
    List<ProductImage> findByVariant_VariantIdOrderBySortOrder(Long variantId);
    Optional<ProductImage> findByVariantAndIsPrimaryTrue(ProductVariant variant);
    void deleteByVariant_VariantId(Long variantId);
    
    // Combined queries
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.productId = :productId AND pi.variant IS NULL ORDER BY pi.sortOrder")
    List<ProductImage> findProductLevelImages(@Param("productId") Long productId);
    
    @Query("SELECT pi FROM ProductImage pi WHERE pi.variant.variantId = :variantId ORDER BY pi.sortOrder")
    List<ProductImage> findVariantLevelImages(@Param("variantId") Long variantId);
    
    // Count images for a variant
    @Query("SELECT COUNT(pi) FROM ProductImage pi WHERE pi.variant.variantId = :variantId")
    long countByVariantId(@Param("variantId") Long variantId);
    
    // Find primary image for variant
    @Query("SELECT pi FROM ProductImage pi WHERE pi.variant.variantId = :variantId AND pi.isPrimary = true")
    Optional<ProductImage> findPrimaryImageByVariantId(@Param("variantId") Long variantId);
}