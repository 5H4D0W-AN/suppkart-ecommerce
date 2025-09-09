package com.suppkart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.Goal;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    
    /**
     * Find goal by slug
     */
    Optional<Goal> findBySlug(String slug);
    
    /**
     * Find all active goals
     */
    @Query("SELECT g FROM Goal g WHERE g.isActive = true ORDER BY g.displayOrder ASC, g.name ASC")
    List<Goal> findActiveGoals();
    
    /**
     * Find goals by name (partial match)
     */
    @Query("SELECT g FROM Goal g WHERE g.isActive = true AND LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Goal> findByNameContaining(@Param("name") String name);
    
    /**
     * Check if slug exists
     */
    boolean existsBySlug(String slug);
    
    /**
     * Check if name exists
     */
    boolean existsByName(String name);
    
    /**
     * Count active goals
     */
    @Query("SELECT COUNT(g) FROM Goal g WHERE g.isActive = true")
    long countActiveGoals();
    
    /**
     * Find goals with product count
     */
    @Query("SELECT g, COUNT(DISTINCT pg.product) as productCount FROM Goal g " +
           "LEFT JOIN ProductGoal pg ON pg.goalId = g.goalId " +
           "LEFT JOIN Product p ON p.productId = pg.productId AND p.isActive = true " +
           "WHERE g.isActive = true " +
           "GROUP BY g.goalId " +
           "ORDER BY g.displayOrder ASC")
    List<Object[]> findGoalsWithProductCount();
    
    /**
     * Find top goals by product count
     */
    @Query("SELECT g FROM Goal g " +
           "LEFT JOIN ProductGoal pg ON pg.goalId = g.goalId " +
           "LEFT JOIN Product p ON p.productId = pg.productId AND p.isActive = true " +
           "WHERE g.isActive = true " +
           "GROUP BY g.goalId " +
           "ORDER BY COUNT(DISTINCT p) DESC")
    List<Goal> findTopGoalsByProductCount();
    
    /**
     * Find active goals ordered by display order and name
     */
    @Query("SELECT g FROM Goal g WHERE g.isActive = true ORDER BY g.displayOrder ASC, g.name ASC")
    List<Goal> findByIsActiveTrueOrderByDisplayOrderAscNameAsc();
}
