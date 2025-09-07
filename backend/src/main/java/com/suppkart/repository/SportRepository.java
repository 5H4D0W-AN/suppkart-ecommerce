package com.suppkart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.Sport;

@Repository
public interface SportRepository extends JpaRepository<Sport, Long> {
    
    /**
     * Find sport by slug
     */
    Optional<Sport> findBySlug(String slug);
    
    /**
     * Find all active sports
     */
    @Query("SELECT s FROM Sport s WHERE s.isActive = true ORDER BY s.displayOrder ASC, s.name ASC")
    List<Sport> findActiveSports();
    
    /**
     * Find sports by name (partial match)
     */
    @Query("SELECT s FROM Sport s WHERE s.isActive = true AND LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Sport> findByNameContaining(@Param("name") String name);
    
    /**
     * Check if slug exists
     */
    boolean existsBySlug(String slug);
    
    /**
     * Check if name exists
     */
    boolean existsByName(String name);
    
    /**
     * Count active sports
     */
    @Query("SELECT COUNT(s) FROM Sport s WHERE s.isActive = true")
    long countActiveSports();
    
    /**
     * Find sports with product count
     */
    @Query("SELECT s, COUNT(DISTINCT ps.product) as productCount FROM Sport s " +
           "LEFT JOIN s.productSports ps ON ps.product.isActive = true " +
           "WHERE s.isActive = true " +
           "GROUP BY s.sportId " +
           "ORDER BY s.displayOrder ASC")
    List<Object[]> findSportsWithProductCount();
    
    /**
     * Find top sports by product count
     */
    @Query("SELECT s FROM Sport s " +
           "LEFT JOIN s.productSports ps ON ps.product.isActive = true " +
           "WHERE s.isActive = true " +
           "GROUP BY s.sportId " +
           "ORDER BY COUNT(DISTINCT ps.product) DESC")
    List<Sport> findTopSportsByProductCount();
    
    /**
     * Find active sports ordered by display order and name
     */
    @Query("SELECT s FROM Sport s WHERE s.isActive = true ORDER BY s.displayOrder ASC, s.name ASC")
    List<Sport> findByIsActiveTrueOrderByDisplayOrderAscNameAsc();
}
