package com.suppkart.model.entity;

import com.suppkart.model.enums.BlogPostStatus;
import com.suppkart.model.enums.ContentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a blog post
 */
@Entity
@Table(name = "blog_posts")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, unique = true, length = 500)
    private String slug;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    @Builder.Default
    private ContentType contentType = ContentType.HTML;

    @Column(columnDefinition = "TEXT")
    private String excerpt;

    @Column(name = "featured_image", length = 500)
    private String featuredImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BlogPostStatus status = BlogPostStatus.DRAFT;

    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    @Column(name = "meta_title", length = 255)
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Column(name = "meta_keywords", length = 500)
    private String metaKeywords;

    @Column(nullable = false)
    @Builder.Default
    private Integer views = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "blog_post_categories",
        joinColumns = @JoinColumn(name = "blog_post_id"),
        inverseJoinColumns = @JoinColumn(name = "blog_category_id")
    )
    @Builder.Default
    private Set<BlogCategory> categories = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "blog_post_tags", joinColumns = @JoinColumn(name = "blog_post_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "blog_post_suggested_products",
        joinColumns = @JoinColumn(name = "blog_post_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @Builder.Default
    private Set<Product> suggestedProducts = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
