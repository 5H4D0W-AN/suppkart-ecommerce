package com.suppkart.model.entity;

import com.suppkart.model.enums.ContentType;
import com.suppkart.model.enums.PageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing dynamic SEO metadata and content for pages
 */
@Entity
@Table(name = "seo_metadata", indexes = {
    @Index(name = "idx_page_type", columnList = "page_type"),
    @Index(name = "idx_page_type_element_key", columnList = "page_type, element_key")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeoMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "page_type", nullable = false, length = 50)
    private PageType pageType;

    @Column(name = "element_key", nullable = false, length = 100)
    private String elementKey; // e.g., "H2Header", "LongFormContent", "PopularSearches"

    @Column(name = "element_label", length = 200)
    private String elementLabel; // Human readable label for admin UI

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private ContentType contentType;

    @Column(name = "content_value", columnDefinition = "TEXT")
    private String contentValue; // For text/html/json content

    @Column(name = "media_url", length = 500)
    private String mediaUrl; // For media content

    @Column(name = "alt_text", length = 255)
    private String altText; // For media content accessibility

    @Column(name = "display_order")
    private Integer displayOrder; // For ordering elements

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // SEO specific fields
    @Column(name = "meta_title", length = 255)
    private String metaTitle;

    @Column(name = "meta_description", columnDefinition = "TEXT")
    private String metaDescription;

    @Column(name = "meta_keywords", columnDefinition = "TEXT")
    private String metaKeywords;

    @Column(name = "og_title", length = 255)
    private String ogTitle;

    @Column(name = "og_description", columnDefinition = "TEXT")
    private String ogDescription;

    @Column(name = "og_image", length = 500)
    private String ogImage;

    @Column(name = "canonical_url", length = 500)
    private String canonicalUrl;

    @Column(name = "no_index", nullable = false)
    @Builder.Default
    private Boolean noIndex = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
