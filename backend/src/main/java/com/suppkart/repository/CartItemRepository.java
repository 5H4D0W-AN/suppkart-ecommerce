package com.suppkart.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find all cart items for a cart
     */
    List<CartItem> findByCart_CartIdOrderByAddedAtDesc(Long cartId);

    /**
     * Find all cart items for a cart (by Cart entity)
     */
    List<CartItem> findByCart(com.suppkart.model.entity.Cart cart);

    /**
     * Delete all cart items for a cart (by Cart entity)
     */
    void deleteByCart(com.suppkart.model.entity.Cart cart);

    /**
     * Find cart item by cart and product (no variant)
     */
    Optional<CartItem> findByCart_CartIdAndProduct_ProductIdAndVariantIsNull(Long cartId, Long productId);

    /**
     * Find cart item by cart, product and variant
     */
    Optional<CartItem> findByCart_CartIdAndProduct_ProductIdAndVariant_VariantId(Long cartId, Long productId, Long variantId);

    /**
     * Count items in a cart
     */
    long countByCart_CartId(Long cartId);

    /**
     * Calculate total quantity in a cart
     */
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    Integer getTotalQuantityByCartId(@Param("cartId") Long cartId);

    /**
     * Calculate total price for a cart
     */
    @Query("SELECT COALESCE(SUM(ci.quantity * ci.priceAtAddition), 0) FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    Double getTotalPriceByCartId(@Param("cartId") Long cartId);

    /**
     * Check if product exists in cart (any variant)
     */
    boolean existsByCart_CartIdAndProduct_ProductId(Long cartId, Long productId);

    /**
     * Check if specific product variant exists in cart
     */
    @Query("SELECT COUNT(ci) > 0 FROM CartItem ci WHERE ci.cart.cartId = :cartId AND ci.product.productId = :productId AND " +
           "(:variantId IS NULL AND ci.variant IS NULL OR ci.variant.variantId = :variantId)")
    boolean existsByCartProductAndVariant(@Param("cartId") Long cartId, @Param("productId") Long productId, @Param("variantId") Long variantId);

    /**
     * Delete all cart items for a cart
     */
    void deleteByCart_CartId(Long cartId);

    /**
     * Delete cart items for a specific product
     */
    void deleteByCart_CartIdAndProduct_ProductId(Long cartId, Long productId);

    /**
     * Find cart items by product ID (for inventory management)
     */
    List<CartItem> findByProduct_ProductId(Long productId);

    /**
     * Find cart items by variant ID (for inventory management)
     */
    List<CartItem> findByVariant_VariantId(Long variantId);

    /**
     * Find cart items for expired guest carts
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.expiresAt IS NOT NULL AND ci.cart.expiresAt < :now")
    List<CartItem> findItemsInExpiredCarts(@Param("now") LocalDateTime now);

    /**
     * Delete cart items for expired guest carts
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.expiresAt IS NOT NULL AND ci.cart.expiresAt < :now")
    void deleteItemsInExpiredCarts(@Param("now") LocalDateTime now);

    /**
     * Find cart items with out of stock products
     */
    @Query("SELECT ci FROM CartItem ci WHERE " +
           "(ci.variant IS NULL AND ci.product.stockQuantity < ci.quantity) OR " +
           "(ci.variant IS NOT NULL AND ci.variant.stockQuantity < ci.quantity)")
    List<CartItem> findItemsWithInsufficientStock();

    /**
     * Update price for all cart items of a product
     */
    @Modifying
    @Query("UPDATE CartItem ci SET ci.priceAtAddition = :newPrice WHERE ci.product.productId = :productId AND ci.variant IS NULL")
    void updatePriceForProduct(@Param("productId") Long productId, @Param("newPrice") Double newPrice);

    /**
     * Update price for all cart items of a product variant
     */
    @Modifying
    @Query("UPDATE CartItem ci SET ci.priceAtAddition = :newPrice WHERE ci.variant.variantId = :variantId")
    void updatePriceForVariant(@Param("variantId") Long variantId, @Param("newPrice") Double newPrice);

    /**
     * Find cart items added in a date range
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.addedAt BETWEEN :startDate AND :endDate")
    List<CartItem> findItemsAddedBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
