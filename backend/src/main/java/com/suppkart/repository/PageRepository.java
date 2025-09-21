package com.suppkart.repository;

import com.suppkart.model.entity.Page;
import com.suppkart.model.enums.PageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Page entity
 */
@Repository
public interface PageRepository extends JpaRepository<Page, Long> {

    /**
     * Find page by slug
     */
    Optional<Page> findBySlug(String slug);

    /**
     * Find pages by status
     */
    List<Page> findByStatus(PageStatus status);

    /**
     * Check if slug exists
     */
    boolean existsBySlug(String slug);

    /**
     * Check if slug exists excluding current page
     */
    boolean existsBySlugAndIdNot(String slug, Long id);

    /**
     * Count pages by status
     */
    long countByStatus(PageStatus status);

    /**
     * Find all pages ordered by updated date descending
     */
    List<Page> findAllByOrderByUpdatedAtDesc();

    /**
     * Find pages by status ordered by updated date descending
     */
    List<Page> findByStatusOrderByUpdatedAtDesc(PageStatus status);

    /**
     * Find page by slug and status
     */
    Optional<Page> findBySlugAndStatus(String slug, PageStatus status);
}
