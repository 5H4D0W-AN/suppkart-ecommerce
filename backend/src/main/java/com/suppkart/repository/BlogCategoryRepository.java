package com.suppkart.repository;

import com.suppkart.model.entity.BlogCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BlogCategory entity
 */
@Repository
public interface BlogCategoryRepository extends JpaRepository<BlogCategory, Long> {

    /**
     * Find blog category by slug
     */
    Optional<BlogCategory> findBySlug(String slug);

    /**
     * Check if category name exists
     */
    boolean existsByName(String name);

    /**
     * Check if category slug exists
     */
    boolean existsBySlug(String slug);

    /**
     * Check if name exists excluding current category
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * Check if slug exists excluding current category
     */
    boolean existsBySlugAndIdNot(String slug, Long id);

    /**
     * Find all blog categories ordered by name ascending
     */
    List<BlogCategory> findAllByOrderByNameAsc();
}
