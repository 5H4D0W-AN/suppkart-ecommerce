package com.suppkart.repository;

import com.suppkart.model.entity.InventoryHistory;
import com.suppkart.model.enums.ChangeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Long> {

    /**
     * Find inventory history by product ID ordered by updated date descending
     */
    @Query("SELECT ih FROM InventoryHistory ih WHERE ih.product.productId = :productId ORDER BY ih.updatedAt DESC")
    List<InventoryHistory> findByProductIdOrderByUpdatedAtDesc(@Param("productId") Long productId);

    /**
     * Find inventory history by product ID and variant ID ordered by updated date descending
     */
    @Query("SELECT ih FROM InventoryHistory ih WHERE ih.product.productId = :productId AND ih.variant.variantId = :variantId ORDER BY ih.updatedAt DESC")
    List<InventoryHistory> findByProductIdAndVariantIdOrderByUpdatedAtDesc(@Param("productId") Long productId, @Param("variantId") Long variantId);

    /**
     * Find inventory history by product ID where variant is null
     */
    @Query("SELECT ih FROM InventoryHistory ih WHERE ih.product.productId = :productId AND ih.variant IS NULL ORDER BY ih.updatedAt DESC")
    List<InventoryHistory> findByProductIdAndVariantIdIsNullOrderByUpdatedAtDesc(@Param("productId") Long productId);

    /**
     * Find inventory history between date range
     */
    List<InventoryHistory> findByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find inventory history by change type
     */
    List<InventoryHistory> findByChangeTypeOrderByUpdatedAtDesc(ChangeType changeType);

    /**
     * Find inventory history by updated by user
     */
    @Query("SELECT ih FROM InventoryHistory ih WHERE ih.updatedBy.userId = :userId ORDER BY ih.updatedAt DESC")
    List<InventoryHistory> findByUpdatedByIdOrderByUpdatedAtDesc(@Param("userId") Long userId);

    /**
     * Find recent inventory history (last N records)
     */
    @Query("SELECT ih FROM InventoryHistory ih ORDER BY ih.updatedAt DESC")
    List<InventoryHistory> findRecentHistory();

    /**
     * Find inventory history by product and change type
     */
    @Query("SELECT ih FROM InventoryHistory ih WHERE ih.product.productId = :productId AND ih.changeType = :changeType ORDER BY ih.updatedAt DESC")
    List<InventoryHistory> findByProductIdAndChangeTypeOrderByUpdatedAtDesc(@Param("productId") Long productId, @Param("changeType") ChangeType changeType);

    /**
     * Find inventory history by variant and change type
     */
    @Query("SELECT ih FROM InventoryHistory ih WHERE ih.variant.variantId = :variantId AND ih.changeType = :changeType ORDER BY ih.updatedAt DESC")
    List<InventoryHistory> findByVariantIdAndChangeTypeOrderByUpdatedAtDesc(@Param("variantId") Long variantId, @Param("changeType") ChangeType changeType);

    /**
     * Find inventory history with quantity increases
     */
    @Query("SELECT ih FROM InventoryHistory ih WHERE ih.newQuantity > ih.previousQuantity ORDER BY ih.updatedAt DESC")
    List<InventoryHistory> findQuantityIncreases();

    /**
     * Find inventory history with quantity decreases
     */
    @Query("SELECT ih FROM InventoryHistory ih WHERE ih.newQuantity < ih.previousQuantity ORDER BY ih.updatedAt DESC")
    List<InventoryHistory> findQuantityDecreases();

    /**
     * Count history records by product
     */
    @Query("SELECT COUNT(ih) FROM InventoryHistory ih WHERE ih.product.productId = :productId")
    Long countByProductId(@Param("productId") Long productId);

    /**
     * Count history records by change type
     */
    Long countByChangeType(ChangeType changeType);

    /**
     * Find history records by date range and change type
     */
    List<InventoryHistory> findByUpdatedAtBetweenAndChangeType(
            LocalDateTime start, LocalDateTime end, ChangeType changeType);

    /**
     * Find history records by product name (case insensitive)
     */
    @Query("SELECT ih FROM InventoryHistory ih WHERE LOWER(ih.product.name) LIKE LOWER(CONCAT('%', :productName, '%')) ORDER BY ih.updatedAt DESC")
    List<InventoryHistory> findByProductNameContainingIgnoreCase(@Param("productName") String productName);

    /**
     * Find history records with large quantity changes (absolute difference >= threshold)
     */
    @Query("SELECT ih FROM InventoryHistory ih WHERE ABS(ih.newQuantity - ih.previousQuantity) >= :threshold ORDER BY ih.updatedAt DESC")
    List<InventoryHistory> findLargeQuantityChanges(@Param("threshold") Integer threshold);
}
