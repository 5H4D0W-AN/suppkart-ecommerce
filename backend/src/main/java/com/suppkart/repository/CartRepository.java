package com.suppkart.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    /**
     * Find cart by user ID
     */
    Optional<Cart> findByUser_UserId(Long userId);
    
    /**
     * Find cart by user
     */
    Optional<Cart> findByUser(com.suppkart.model.entity.User user);
    
    /**
     * Find cart by session ID (for guest users)
     */
    Optional<Cart> findBySessionId(String sessionId);
    
    /**
     * Find expired guest carts
     */
    @Query("SELECT c FROM Cart c WHERE c.sessionId IS NOT NULL AND c.expiresAt < :currentTime")
    List<Cart> findExpiredGuestCarts(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find carts created in the last N days
     */
    @Query("SELECT c FROM Cart c WHERE c.createdAt >= :fromDate")
    List<Cart> findCartsCreatedSince(@Param("fromDate") LocalDateTime fromDate);
    
    /**
     * Count active carts (not expired)
     */
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.user IS NOT NULL OR " +
           "(c.sessionId IS NOT NULL AND (c.expiresAt IS NULL OR c.expiresAt >= CURRENT_TIMESTAMP))")
    long countActiveCarts();
    
    /**
     * Count guest carts
     */
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.sessionId IS NOT NULL")
    long countGuestCarts();
    
    /**
     * Count user carts
     */
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.user IS NOT NULL")
    long countUserCarts();
    
    /**
     * Find guest carts created before a certain time (for expiration)
     */
    List<Cart> findByUserIsNullAndCreatedAtBefore(LocalDateTime createdBefore);
    
    /**
     * Delete expired guest carts
     */
    @Query("DELETE FROM Cart c WHERE c.sessionId IS NOT NULL AND c.expiresAt < :currentTime")
    void deleteExpiredGuestCarts(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find expired carts (both user and guest carts older than cutoff time)
     */
    @Query("SELECT c FROM Cart c WHERE c.updatedAt < :cutoffTime")
    List<Cart> findExpiredCarts(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Find old guest carts (guest carts older than cutoff time)
     */
    @Query("SELECT c FROM Cart c WHERE c.user IS NULL AND c.sessionId IS NOT NULL AND c.createdAt < :cutoffTime")
    List<Cart> findOldGuestCarts(@Param("cutoffTime") LocalDateTime cutoffTime);
}
