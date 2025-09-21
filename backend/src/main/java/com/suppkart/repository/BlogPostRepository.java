package com.suppkart.repository;

import com.suppkart.model.entity.BlogCategory;
import com.suppkart.model.entity.BlogPost;
import com.suppkart.model.enums.BlogPostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BlogPost entity
 */
@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    /**
     * Find blog post by slug
     */
    Optional<BlogPost> findBySlug(String slug);

    /**
     * Find blog posts by status
     */
    List<BlogPost> findByStatus(BlogPostStatus status);

    /**
     * Find published blog posts that are ready to be displayed
     */
    List<BlogPost> findByStatusAndPublishDateLessThanEqual(BlogPostStatus status, LocalDateTime date);

    /**
     * Find blog posts by category
     */
    @Query("SELECT bp FROM BlogPost bp JOIN bp.categories c WHERE c = :category")
    List<BlogPost> findByCategoriesContaining(@Param("category") BlogCategory category);

    /**
     * Find blog posts by status with pagination, ordered by publish date descending
     */
    Page<BlogPost> findByStatusOrderByPublishDateDesc(BlogPostStatus status, Pageable pageable);

    /**
     * Find blog posts by status with pagination, ordered by created date descending
     */
    Page<BlogPost> findByStatusOrderByCreatedAtDesc(BlogPostStatus status, Pageable pageable);

    /**
     * Find published blog posts with publish date filter, ordered by publish date descending
     */
    Page<BlogPost> findByStatusAndPublishDateLessThanEqualOrderByPublishDateDesc(BlogPostStatus status, LocalDateTime date, Pageable pageable);

    /**
     * Find all blog posts ordered by created date descending
     */
    Page<BlogPost> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find blog posts by category with pagination
     */
    Page<BlogPost> findByCategoriesContaining(BlogCategory category, Pageable pageable);

    /**
     * Find blog post by slug and status
     */
    Optional<BlogPost> findBySlugAndStatus(String slug, BlogPostStatus status);

    /**
     * Find blog posts by title containing text (case insensitive) with pagination
     */
    Page<BlogPost> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * Find blog posts by status and title containing text with pagination
     */
    Page<BlogPost> findByStatusAndTitleContainingIgnoreCase(BlogPostStatus status, String title, Pageable pageable);

    /**
     * Find blog posts by author ID
     */
    List<BlogPost> findByAuthorUserId(Long authorId);

    /**
     * Find blog posts by author ID with pagination
     */
    Page<BlogPost> findByAuthorUserId(Long authorId, Pageable pageable);

    /**
     * Count blog posts by status
     */
    long countByStatus(BlogPostStatus status);

    /**
     * Find recent blog posts by status with limit
     */
    @Query("SELECT bp FROM BlogPost bp WHERE bp.status = :status ORDER BY bp.createdAt DESC")
    List<BlogPost> findRecentByStatus(@Param("status") BlogPostStatus status, Pageable pageable);

    /**
     * Find blog posts by tags containing
     */
    @Query("SELECT bp FROM BlogPost bp JOIN bp.tags t WHERE t IN :tags")
    List<BlogPost> findByTagsIn(@Param("tags") List<String> tags);

    /**
     * Check if slug exists
     */
    boolean existsBySlug(String slug);

    /**
     * Check if slug exists excluding current post
     */
    boolean existsBySlugAndIdNot(String slug, Long id);
}
