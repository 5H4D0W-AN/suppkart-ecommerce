package com.suppkart.repository;

import com.suppkart.model.entity.SeoMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeoMetadataRepository extends JpaRepository<SeoMetadata, Long> {

    /**
     * Find SEO metadata by page type and entity ID
     */
    Optional<SeoMetadata> findByPageTypeAndEntityId(String pageType, Long entityId);

    /**
     * Find all SEO metadata by page type
     */
    List<SeoMetadata> findByPageType(String pageType);

    /**
     * Find SEO metadata by page type ordered by creation date
     */
    List<SeoMetadata> findByPageTypeOrderByCreatedAtDesc(String pageType);

    /**
     * Check if SEO metadata exists for page type and entity ID
     */
    boolean existsByPageTypeAndEntityId(String pageType, Long entityId);

    /**
     * Find SEO metadata by canonical URL
     */
    Optional<SeoMetadata> findByCanonicalUrl(String canonicalUrl);

    /**
     * Find SEO metadata with noIndex set to true
     */
    List<SeoMetadata> findByNoIndexTrue();

    /**
     * Find SEO metadata with noIndex set to false
     */
    List<SeoMetadata> findByNoIndexFalse();

    /**
     * Find SEO metadata by title containing (case insensitive)
     */
    List<SeoMetadata> findByTitleContainingIgnoreCase(String title);

    /**
     * Find SEO metadata by description containing (case insensitive)
     */
    List<SeoMetadata> findByDescriptionContainingIgnoreCase(String description);

    /**
     * Find SEO metadata by keywords containing (case insensitive)
     */
    List<SeoMetadata> findByKeywordsContainingIgnoreCase(String keywords);

    /**
     * Find SEO metadata by Open Graph title containing (case insensitive)
     */
    List<SeoMetadata> findByOgTitleContainingIgnoreCase(String ogTitle);

    /**
     * Find SEO metadata by Open Graph description containing (case insensitive)
     */
    List<SeoMetadata> findByOgDescriptionContainingIgnoreCase(String ogDescription);

    /**
     * Find SEO metadata with Open Graph image set
     */
    @Query("SELECT s FROM SeoMetadata s WHERE s.ogImage IS NOT NULL AND s.ogImage != ''")
    List<SeoMetadata> findWithOgImage();

    /**
     * Find SEO metadata without Open Graph image
     */
    @Query("SELECT s FROM SeoMetadata s WHERE s.ogImage IS NULL OR s.ogImage = ''")
    List<SeoMetadata> findWithoutOgImage();

    /**
     * Find SEO metadata with canonical URL set
     */
    @Query("SELECT s FROM SeoMetadata s WHERE s.canonicalUrl IS NOT NULL AND s.canonicalUrl != ''")
    List<SeoMetadata> findWithCanonicalUrl();

    /**
     * Find SEO metadata without canonical URL
     */
    @Query("SELECT s FROM SeoMetadata s WHERE s.canonicalUrl IS NULL OR s.canonicalUrl = ''")
    List<SeoMetadata> findWithoutCanonicalUrl();

    /**
     * Count SEO metadata by page type
     */
    long countByPageType(String pageType);

    /**
     * Count SEO metadata with noIndex set to true
     */
    long countByNoIndexTrue();

    /**
     * Count SEO metadata with noIndex set to false
     */
    long countByNoIndexFalse();

    /**
     * Find duplicate canonical URLs
     */
    @Query("SELECT s.canonicalUrl FROM SeoMetadata s WHERE s.canonicalUrl IS NOT NULL " +
           "GROUP BY s.canonicalUrl HAVING COUNT(s.canonicalUrl) > 1")
    List<String> findDuplicateCanonicalUrls();

    /**
     * Find SEO metadata by multiple page types
     */
    List<SeoMetadata> findByPageTypeIn(List<String> pageTypes);

    /**
     * Find SEO metadata by entity IDs
     */
    List<SeoMetadata> findByEntityIdIn(List<Long> entityIds);

    /**
     * Find SEO metadata by page type and entity IDs
     */
    List<SeoMetadata> findByPageTypeAndEntityIdIn(String pageType, List<Long> entityIds);

    /**
     * Delete SEO metadata by page type and entity ID
     */
    void deleteByPageTypeAndEntityId(String pageType, Long entityId);

    /**
     * Delete SEO metadata by page type
     */
    void deleteByPageType(String pageType);

    /**
     * Find SEO metadata with missing required fields
     */
    @Query("SELECT s FROM SeoMetadata s WHERE s.title IS NULL OR s.title = '' OR " +
           "s.description IS NULL OR s.description = ''")
    List<SeoMetadata> findWithMissingRequiredFields();

    /**
     * Find SEO metadata with complete Open Graph data
     */
    @Query("SELECT s FROM SeoMetadata s WHERE s.ogTitle IS NOT NULL AND s.ogTitle != '' AND " +
           "s.ogDescription IS NOT NULL AND s.ogDescription != '' AND " +
           "s.ogImage IS NOT NULL AND s.ogImage != ''")
    List<SeoMetadata> findWithCompleteOgData();

    /**
     * Find SEO metadata with incomplete Open Graph data
     */
    @Query("SELECT s FROM SeoMetadata s WHERE s.ogTitle IS NULL OR s.ogTitle = '' OR " +
           "s.ogDescription IS NULL OR s.ogDescription = '' OR " +
           "s.ogImage IS NULL OR s.ogImage = ''")
    List<SeoMetadata> findWithIncompleteOgData();
}
