package com.suppkart.dto.content;

import com.suppkart.model.enums.BlogPostStatus;
import com.suppkart.model.enums.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPostCreateRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    private String slug;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    // Content type: HTML, MARKDOWN, or PLAIN_TEXT
    @Builder.Default
    private ContentType contentType = ContentType.HTML;
    
    // Short excerpt/summary for listing pages
    @Size(max = 500, message = "Excerpt must not exceed 500 characters")
    private String excerpt;
    
    private String featuredImage;
    
    // Optional for updates (ignored when updating)
    private Long authorId;
    
    @Builder.Default
    private BlogPostStatus status = BlogPostStatus.DRAFT;
    
    private LocalDateTime publishDate;
    
    private Set<Long> categoryIds;
    
    private Set<String> tags;
    
    // Product IDs to suggest at the end of the blog post
    private Set<Long> suggestedProductIds;
    
    @Size(max = 255, message = "Meta title must not exceed 255 characters")
    private String metaTitle;
    
    @Size(max = 500, message = "Meta description must not exceed 500 characters")
    private String metaDescription;
    
    @Size(max = 500, message = "Meta keywords must not exceed 500 characters")
    private String metaKeywords;
}
