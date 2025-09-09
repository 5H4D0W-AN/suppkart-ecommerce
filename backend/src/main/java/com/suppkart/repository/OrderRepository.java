package com.suppkart.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.Order;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.OrderStatus;
import com.suppkart.model.enums.PaymentStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Find order by order number
     * @param orderNumber the order number
     * @return Optional<Order>
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * Find all orders by user
     * @param user the user
     * @param pageable pagination information
     * @return Page<Order>
     */
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    /**
     * Find orders by user and status
     * @param user the user
     * @param orderStatus the order status
     * @param pageable pagination information
     * @return Page<Order>
     */
    Page<Order> findByUserAndOrderStatusOrderByCreatedAtDesc(User user, OrderStatus orderStatus, Pageable pageable);
    
    /**
     * Find orders by user and multiple statuses
     * @param user the user
     * @param statuses list of order statuses
     * @param pageable pagination information
     * @return Page<Order>
     */
    Page<Order> findByUserAndOrderStatusInOrderByCreatedAtDesc(User user, List<OrderStatus> statuses, Pageable pageable);
    
    /**
     * Find orders by user within date range
     * @param user the user
     * @param startDate start date
     * @param endDate end date
     * @param pageable pagination information
     * @return Page<Order>
     */
    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    Page<Order> findByUserAndDateRange(@Param("user") User user,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       Pageable pageable);
    
    /**
     * Find orders by status
     * @param orderStatus the order status
     * @param pageable pagination information
     * @return Page<Order>
     */
    Page<Order> findByOrderStatusOrderByCreatedAtDesc(OrderStatus orderStatus, Pageable pageable);
    
    /**
     * Find orders by payment status
     * @param paymentStatus the payment status
     * @param pageable pagination information
     * @return Page<Order>
     */
    Page<Order> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus paymentStatus, Pageable pageable);
    
    /**
     * Find orders that need status update (pending orders older than specified time)
     * @param createdBefore the cutoff date
     * @return List<Order>
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus = :status AND o.createdAt < :createdBefore")
    List<Order> findOrdersNeedingStatusUpdate(@Param("status") OrderStatus status,
                                              @Param("createdBefore") LocalDateTime createdBefore);
    
    /**
     * Find orders by tracking number
     * @param trackingNumber the tracking number
     * @return Optional<Order>
     */
    Optional<Order> findByTrackingNumber(String trackingNumber);
    
    /**
     * Find orders with tracking numbers that need update
     * @param statuses list of order statuses
     * @return List<Order>
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus IN :statuses AND o.trackingNumber IS NOT NULL AND o.deliveredAt IS NULL")
    List<Order> findOrdersForTrackingUpdate(@Param("statuses") List<OrderStatus> statuses);
    
    /**
     * Count orders by user and status
     * @param user the user
     * @param orderStatus the order status
     * @return long count
     */
    long countByUserAndOrderStatus(User user, OrderStatus orderStatus);
    
    /**
     * Count total orders by user
     * @param user the user
     * @return long count
     */
    long countByUser(User user);
    
    /**
     * Find user's recent orders (last N orders)
     * @param user the user
     * @param pageable pagination information
     * @return Page<Order>
     */
    @Query("SELECT o FROM Order o WHERE o.user = :user ORDER BY o.createdAt DESC")
    Page<Order> findRecentOrdersByUser(@Param("user") User user, Pageable pageable);
    
    /**
     * Find orders by user and payment transaction ID
     * @param user the user
     * @param paymentTransactionId the payment transaction ID
     * @return Optional<Order>
     */
    Optional<Order> findByUserAndPaymentTransactionId(User user, String paymentTransactionId);
    
    /**
     * Check if order exists by order number and user
     * @param orderNumber the order number
     * @param user the user
     * @return boolean
     */
    boolean existsByOrderNumberAndUser(String orderNumber, User user);
    
    /**
     * Find orders that can be cancelled (pending, confirmed, processing)
     * @param user the user
     * @param cancellableStatuses list of cancellable statuses
     * @param pageable pagination information
     * @return Page<Order>
     */
    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.orderStatus IN :statuses ORDER BY o.createdAt DESC")
    Page<Order> findCancellableOrdersByUser(@Param("user") User user,
                                            @Param("statuses") List<OrderStatus> statuses,
                                            Pageable pageable);
    
    /**
     * Find orders by coupon code usage
     * @param couponCode the coupon code
     * @param pageable pagination information
     * @return Page<Order>
     */
    Page<Order> findByCouponCodeOrderByCreatedAtDesc(String couponCode, Pageable pageable);
    
    /**
     * Get order statistics for a user
     * @param user the user
     * @return List of Object arrays containing status and count
     */
    @Query("SELECT o.orderStatus, COUNT(o) FROM Order o WHERE o.user = :user GROUP BY o.orderStatus")
    List<Object[]> getOrderStatisticsByUser(@Param("user") User user);
    
    /**
     * Find orders requiring notification (delivered but not acknowledged)
     * @param status the order status
     * @param deliveredBefore cutoff date for delivery
     * @return List<Order>
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus = :status AND o.deliveredAt < :deliveredBefore")
    List<Order> findOrdersRequiringNotification(@Param("status") OrderStatus status,
                                                @Param("deliveredBefore") LocalDateTime deliveredBefore);
    
    /**
     * Find orders by estimated delivery date range
     * @param startDate start date
     * @param endDate end date
     * @return List<Order>
     */
    @Query("SELECT o FROM Order o WHERE o.estimatedDeliveryDate BETWEEN :startDate AND :endDate ORDER BY o.estimatedDeliveryDate ASC")
    List<Order> findByEstimatedDeliveryDateBetween(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
}
