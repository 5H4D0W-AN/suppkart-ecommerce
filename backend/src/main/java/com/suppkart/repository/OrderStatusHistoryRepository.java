package com.suppkart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.OrderStatusHistory;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    
    /**
     * Find all status history for an order, ordered by creation time (newest first)
     */
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.order.orderId = :orderId ORDER BY osh.createdAt DESC")
    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtDesc(@Param("orderId") Long orderId);
    
    /**
     * Find all status history for an order, ordered by creation time (oldest first)
     */
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.order.orderId = :orderId ORDER BY osh.createdAt ASC")
    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtAsc(@Param("orderId") Long orderId);
    
    /**
     * Find the latest status history entry for an order
     */
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.order.orderId = :orderId ORDER BY osh.createdAt DESC LIMIT 1")
    OrderStatusHistory findLatestByOrderId(@Param("orderId") Long orderId);
}