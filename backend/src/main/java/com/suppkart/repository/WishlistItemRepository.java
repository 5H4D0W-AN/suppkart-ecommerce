package com.suppkart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.WishlistItem;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    
    /**
     * Find items by wishlist ID
     */
    List<WishlistItem> findByWishlistWishlistId(Long wishlistId);
    
    /**
     * Find items by wishlist ID with product and variant details
     */
    @Query("SELECT wi FROM WishlistItem wi " +
           "LEFT JOIN FETCH wi.product p " +
           "LEFT JOIN FETCH wi.variant v " +
           "WHERE wi.wishlist.wishlistId = :wishlistId " +
           "ORDER BY wi.addedAt DESC")
    List<WishlistItem> findByWishlistIdWithProductDetails(@Param("wishlistId") Long wishlistId);
    
    /**
     * Check if product exists in user's wishlist
     */
    @Query("SELECT wi FROM WishlistItem wi " +
           "WHERE wi.wishlist.user.userId = :userId " +
           "AND wi.product.productId = :productId " +
           "AND (:variantId IS NULL AND wi.variant IS NULL OR wi.variant.variantId = :variantId)")
    Optional<WishlistItem> findByUserIdAndProductIdAndVariantId(
        @Param("userId") Long userId, 
        @Param("productId") Long productId, 
        @Param("variantId") Long variantId
    );
    
    /**
     * Check if product exists in wishlist (boolean check)
     */
    @Query("SELECT CASE WHEN COUNT(wi) > 0 THEN true ELSE false END FROM WishlistItem wi " +
           "WHERE wi.wishlist.user.userId = :userId " +
           "AND wi.product.productId = :productId " +
           "AND (:variantId IS NULL AND wi.variant IS NULL OR wi.variant.variantId = :variantId)")
    boolean existsByUserIdAndProductIdAndVariantId(
        @Param("userId") Long userId, 
        @Param("productId") Long productId, 
        @Param("variantId") Long variantId
    );
    
    /**
     * Count items per wishlist
     */
    long countByWishlistWishlistId(Long wishlistId);
    
    /**
     * Delete by user ID and product ID and variant ID
     */
    @Query("DELETE FROM WishlistItem wi " +
           "WHERE wi.wishlist.user.userId = :userId " +
           "AND wi.product.productId = :productId " +
           "AND (:variantId IS NULL AND wi.variant IS NULL OR wi.variant.variantId = :variantId)")
    void deleteByUserIdAndProductIdAndVariantId(
        @Param("userId") Long userId, 
        @Param("productId") Long productId, 
        @Param("variantId") Long variantId
    );
    
    /**
     * Find all items for a user across all wishlists
     */
    @Query("SELECT wi FROM WishlistItem wi " +
           "LEFT JOIN FETCH wi.product p " +
           "LEFT JOIN FETCH wi.variant v " +
           "WHERE wi.wishlist.user.userId = :userId " +
           "ORDER BY wi.addedAt DESC")
    List<WishlistItem> findAllByUserIdWithProductDetails(@Param("userId") Long userId);
    
    /**
     * Find items by product ID (for stock checking)
     */
    @Query("SELECT wi FROM WishlistItem wi " +
           "LEFT JOIN FETCH wi.wishlist w " +
           "LEFT JOIN FETCH w.user u " +
           "WHERE wi.product.productId = :productId")
    List<WishlistItem> findByProductIdWithWishlistAndUser(@Param("productId") Long productId);
}
