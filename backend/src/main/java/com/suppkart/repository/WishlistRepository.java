package com.suppkart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.Wishlist;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    
    /**
     * Find wishlist by user ID
     */
    Optional<Wishlist> findByUserUserId(Long userId);
    
    /**
     * Find public wishlists
     */
    List<Wishlist> findByIsPublicTrue();
    
    /**
     * Find wishlist by user ID with items (fetch join)
     */
    @Query("SELECT w FROM Wishlist w LEFT JOIN FETCH w.items WHERE w.user.userId = :userId")
    Optional<Wishlist> findByUserUserIdWithItems(@Param("userId") Long userId);
    
    /**
     * Check if wishlist exists for user
     */
    boolean existsByUserUserId(Long userId);
    
    /**
     * Count total wishlists
     */
    long countByIsPublicTrue();
    
    /**
     * Find public wishlists with pagination
     */
    @Query("SELECT w FROM Wishlist w WHERE w.isPublic = true ORDER BY w.updatedAt DESC")
    List<Wishlist> findPublicWishlistsOrderByUpdatedAtDesc();
}
