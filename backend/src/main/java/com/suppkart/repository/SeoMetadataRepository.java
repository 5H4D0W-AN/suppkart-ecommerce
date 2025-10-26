package com.suppkart.repository;

import com.suppkart.model.entity.SeoMetadata;
import com.suppkart.model.enums.PageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeoMetadataRepository extends JpaRepository<SeoMetadata, Long> {
    
    /**
     * Find all elements for a page type ordered by display order and creation date
     */
    List<SeoMetadata> findByPageTypeOrderByDisplayOrderAscCreatedAtAsc(PageType pageType);
    
    /**
     * Find active elements for a page type ordered by display order and creation date
     */
    List<SeoMetadata> findByPageTypeAndIsActiveTrueOrderByDisplayOrderAscCreatedAtAsc(PageType pageType);
    
    /**
     * Check if element key exists for a page type
     */
    boolean existsByPageTypeAndElementKey(PageType pageType, String elementKey);
    
    /**
     * Find element by page type and element key
     */
    SeoMetadata findByPageTypeAndElementKey(PageType pageType, String elementKey);
    
    /**
     * Find all elements for multiple page types
     */
    List<SeoMetadata> findByPageTypeInAndIsActiveTrueOrderByPageTypeAscDisplayOrderAscCreatedAtAsc(List<PageType> pageTypes);
}