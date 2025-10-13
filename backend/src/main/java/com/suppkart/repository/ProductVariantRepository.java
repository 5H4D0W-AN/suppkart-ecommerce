package com.suppkart.repository;

import com.suppkart.model.entity.Product;
import com.suppkart.model.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    
    // Existing methods
    boolean existsBySku(String sku);
    List<ProductVariant> findByProduct(Product product);
    
    // New methods for enhanced functionality
    @Modifying
    @Query("UPDATE ProductVariant v SET v.isDefault = false WHERE v.product.productId = :productId AND v.variantId != :excludeVariantId")
    void unsetDefaultForProduct(@Param("productId") Long productId, @Param("excludeVariantId") Long excludeVariantId);
    
    @Query("SELECT COUNT(v) FROM ProductVariant v WHERE v.product.productId = :productId")
    long countByProductId(@Param("productId") Long productId);
    
    Optional<ProductVariant> findByProductAndIsDefaultTrue(Product product);
    
    @Query("SELECT v FROM ProductVariant v WHERE v.product.productId = :productId AND v.isActive = true")
    List<ProductVariant> findActiveVariantsByProductId(@Param("productId") Long productId);
    
    @Query("SELECT v FROM ProductVariant v WHERE v.product.productId = :productId AND v.isDefault = true")
    Optional<ProductVariant> findDefaultVariantByProductId(@Param("productId") Long productId);
    
    // Methods needed by ProductService
    List<ProductVariant> findByProduct_ProductIdAndIsActiveTrue(Long productId);
    Optional<ProductVariant> findByVariantIdAndIsActiveTrue(Long variantId);
}