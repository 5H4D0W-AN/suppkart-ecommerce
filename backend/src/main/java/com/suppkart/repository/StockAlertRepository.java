package com.suppkart.repository;

import com.suppkart.model.entity.StockAlert;
import com.suppkart.model.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {

    /**
     * Find all unresolved alerts
     */
    List<StockAlert> findByIsResolvedFalse();

    /**
     * Find unresolved alerts by alert type
     */
    List<StockAlert> findByAlertTypeAndIsResolvedFalse(AlertType alertType);

    /**
     * Find unresolved alerts for a specific product and variant
     */
    @Query("SELECT sa FROM StockAlert sa WHERE sa.product.productId = :productId AND sa.variant.variantId = :variantId AND sa.isResolved = false")
    List<StockAlert> findByProductIdAndVariantIdAndIsResolvedFalse(@Param("productId") Long productId, @Param("variantId") Long variantId);

    /**
     * Find unresolved alerts for a specific product (no variant)
     */
    @Query("SELECT sa FROM StockAlert sa WHERE sa.product.productId = :productId AND sa.variant IS NULL AND sa.isResolved = false")
    List<StockAlert> findByProductIdAndVariantIdIsNullAndIsResolvedFalse(@Param("productId") Long productId);

    /**
     * Find all alerts for a specific product
     */
    @Query("SELECT sa FROM StockAlert sa WHERE sa.product.productId = :productId ORDER BY sa.createdAt DESC")
    List<StockAlert> findByProductIdOrderByCreatedAtDesc(@Param("productId") Long productId);

    /**
     * Find all alerts for a specific product and variant
     */
    @Query("SELECT sa FROM StockAlert sa WHERE sa.product.productId = :productId AND sa.variant.variantId = :variantId ORDER BY sa.createdAt DESC")
    List<StockAlert> findByProductIdAndVariantIdOrderByCreatedAtDesc(@Param("productId") Long productId, @Param("variantId") Long variantId);

    /**
     * Find alerts by alert type
     */
    List<StockAlert> findByAlertTypeOrderByCreatedAtDesc(AlertType alertType);

    /**
     * Find alerts created within a date range
     */
    List<StockAlert> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find alerts that haven't been notified yet
     */
    List<StockAlert> findByNotificationSentFalse();

    /**
     * Find unresolved alerts that haven't been notified
     */
    List<StockAlert> findByIsResolvedFalseAndNotificationSentFalse();

    /**
     * Find recent alerts (last N records)
     */
    @Query("SELECT sa FROM StockAlert sa ORDER BY sa.createdAt DESC")
    List<StockAlert> findRecentAlerts();

    /**
     * Count unresolved alerts
     */
    Long countByIsResolvedFalse();

    /**
     * Count unresolved alerts by type
     */
    Long countByAlertTypeAndIsResolvedFalse(AlertType alertType);

    /**
     * Count alerts for a specific product
     */
    @Query("SELECT COUNT(sa) FROM StockAlert sa WHERE sa.product.productId = :productId")
    Long countByProductId(@Param("productId") Long productId);

    /**
     * Find alerts by current stock level (less than or equal to threshold)
     */
    @Query("SELECT sa FROM StockAlert sa WHERE sa.currentStock <= :threshold AND sa.isResolved = false")
    List<StockAlert> findByCurrentStockLessThanEqual(@Param("threshold") Integer threshold);

    /**
     * Find alerts by product name (case insensitive)
     */
    @Query("SELECT sa FROM StockAlert sa WHERE LOWER(sa.product.name) LIKE LOWER(CONCAT('%', :productName, '%')) ORDER BY sa.createdAt DESC")
    List<StockAlert> findByProductNameContainingIgnoreCase(@Param("productName") String productName);

    /**
     * Find alerts by variant SKU
     */
    @Query("SELECT sa FROM StockAlert sa WHERE sa.variant.sku = :sku ORDER BY sa.createdAt DESC")
    List<StockAlert> findByVariantSku(@Param("sku") String sku);

    /**
     * Find existing unresolved alert for product and variant
     */
    @Query("SELECT sa FROM StockAlert sa WHERE sa.product.productId = :productId AND sa.variant.variantId = :variantId AND sa.alertType = :alertType AND sa.isResolved = false")
    Optional<StockAlert> findByProductIdAndVariantIdAndAlertTypeAndIsResolvedFalse(
            @Param("productId") Long productId, @Param("variantId") Long variantId, @Param("alertType") AlertType alertType);

    /**
     * Find existing unresolved alert for product (no variant)
     */
    @Query("SELECT sa FROM StockAlert sa WHERE sa.product.productId = :productId AND sa.variant IS NULL AND sa.alertType = :alertType AND sa.isResolved = false")
    Optional<StockAlert> findByProductIdAndVariantIdIsNullAndAlertTypeAndIsResolvedFalse(
            @Param("productId") Long productId, @Param("alertType") AlertType alertType);

    /**
     * Find alerts resolved within a date range
     */
    List<StockAlert> findByIsResolvedTrueAndResolvedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find old unresolved alerts (created before specified date)
     */
    @Query("SELECT sa FROM StockAlert sa WHERE sa.isResolved = false AND sa.createdAt < :date ORDER BY sa.createdAt ASC")
    List<StockAlert> findOldUnresolvedAlerts(@Param("date") LocalDateTime date);

    /**
     * Find alerts by category
     */
    @Query("SELECT sa FROM StockAlert sa JOIN sa.product.productCategories pc WHERE pc.category.categoryId = :categoryId ORDER BY sa.createdAt DESC")
    List<StockAlert> findByProductCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Find critical alerts (out of stock)
     */
    @Query("SELECT sa FROM StockAlert sa WHERE sa.alertType = 'OUT_OF_STOCK' AND sa.isResolved = false ORDER BY sa.createdAt DESC")
    List<StockAlert> findCriticalAlerts();

    /**
     * Find low stock alerts that are not critical
     */
    @Query("SELECT sa FROM StockAlert sa WHERE sa.alertType = 'LOW_STOCK' AND sa.isResolved = false ORDER BY sa.createdAt DESC")
    List<StockAlert> findLowStockAlerts();

    /**
     * Check if unresolved alert exists for product and variant
     */
    @Query("SELECT CASE WHEN COUNT(sa) > 0 THEN true ELSE false END FROM StockAlert sa WHERE sa.product.productId = :productId AND sa.variant.variantId = :variantId AND sa.isResolved = false")
    boolean existsByProductIdAndVariantIdAndIsResolvedFalse(@Param("productId") Long productId, @Param("variantId") Long variantId);

    /**
     * Check if unresolved alert exists for product (no variant)
     */
    @Query("SELECT CASE WHEN COUNT(sa) > 0 THEN true ELSE false END FROM StockAlert sa WHERE sa.product.productId = :productId AND sa.variant IS NULL AND sa.isResolved = false")
    boolean existsByProductIdAndVariantIdIsNullAndIsResolvedFalse(@Param("productId") Long productId);
}
