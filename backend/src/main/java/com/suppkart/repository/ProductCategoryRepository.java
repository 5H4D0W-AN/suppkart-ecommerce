package com.suppkart.repository;

import com.suppkart.model.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    
    List<ProductCategory> findByProduct_ProductId(Long productId);
    
    @Modifying
    @Query("DELETE FROM ProductCategory pc WHERE pc.product.productId = :productId")
    void deleteByProductId(@Param("productId") Long productId);
    
    boolean existsByProduct_ProductIdAndCategory_CategoryId(Long productId, Long categoryId);
}