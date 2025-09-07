package com.suppkart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.Order;
import com.suppkart.model.entity.OrderItem;
import com.suppkart.model.entity.Product;
import com.suppkart.model.entity.ProductVariant;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * Find all order items by order
     * @param order the order
     * @return List<OrderItem>
     */
    List<OrderItem> findByOrderOrderByCreatedAtAsc(Order order);
    
    /**
     * Find order items by product
     * @param product the product
     * @return List<OrderItem>
     */
    List<OrderItem> findByProduct(Product product);
    
    /**
     * Find order items by product variant
     * @param productVariant the product variant
     * @return List<OrderItem>
     */
    List<OrderItem> findByProductVariant(ProductVariant productVariant);
    
    /**
     * Count order items by order
     * @param order the order
     * @return long count
     */
    long countByOrder(Order order);
    
    /**
     * Get total quantity of items in an order
     * @param order the order
     * @return Integer total quantity
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.order = :order")
    Integer getTotalQuantityByOrder(@Param("order") Order order);
    
    /**
     * Find order items by product SKU
     * @param productSku the product SKU
     * @return List<OrderItem>
     */
    List<OrderItem> findByProductSku(String productSku);
    
    /**
     * Find order items by product name (for search)
     * @param productName the product name
     * @return List<OrderItem>
     */
    @Query("SELECT oi FROM OrderItem oi WHERE LOWER(oi.productName) LIKE LOWER(CONCAT('%', :productName, '%'))")
    List<OrderItem> findByProductNameContainingIgnoreCase(@Param("productName") String productName);
    
    /**
     * Get most purchased products (aggregated by product)
     * @param limit maximum number of results
     * @return List of Object arrays containing product and total quantity
     */
    @Query("SELECT oi.product, SUM(oi.quantity) as totalQuantity FROM OrderItem oi " +
           "GROUP BY oi.product ORDER BY totalQuantity DESC")
    List<Object[]> findMostPurchasedProducts(@Param("limit") int limit);
    
    /**
     * Get most purchased variants (aggregated by variant)
     * @param limit maximum number of results
     * @return List of Object arrays containing variant and total quantity
     */
    @Query("SELECT oi.productVariant, SUM(oi.quantity) as totalQuantity FROM OrderItem oi " +
           "WHERE oi.productVariant IS NOT NULL " +
           "GROUP BY oi.productVariant ORDER BY totalQuantity DESC")
    List<Object[]> findMostPurchasedVariants(@Param("limit") int limit);
    
    /**
     * Calculate total revenue from order items
     * @return Object array with total revenue
     */
    @Query("SELECT COALESCE(SUM(oi.totalPrice), 0) FROM OrderItem oi")
    Double getTotalRevenue();
    
    /**
     * Calculate total revenue by product
     * @param product the product
     * @return Double total revenue
     */
    @Query("SELECT COALESCE(SUM(oi.totalPrice), 0) FROM OrderItem oi WHERE oi.product = :product")
    Double getTotalRevenueByProduct(@Param("product") Product product);
    
    /**
     * Find order items that contain specific product or variant
     * @param product the product
     * @param productVariant the product variant (can be null)
     * @return List<OrderItem>
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.product = :product " +
           "AND (:productVariant IS NULL OR oi.productVariant = :productVariant)")
    List<OrderItem> findByProductAndVariant(@Param("product") Product product, 
                                          @Param("productVariant") ProductVariant productVariant);
    
    /**
     * Check if a product has been ordered before
     * @param product the product
     * @return boolean
     */
    boolean existsByProduct(Product product);
    
    /**
     * Check if a product variant has been ordered before
     * @param productVariant the product variant
     * @return boolean
     */
    boolean existsByProductVariant(ProductVariant productVariant);
    
    /**
     * Delete all order items by order
     * @param order the order
     */
    void deleteByOrder(Order order);
}
