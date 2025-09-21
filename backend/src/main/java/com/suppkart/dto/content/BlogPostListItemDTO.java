package com.suppkart.dto.content;

import com.suppkart.model.enums.BlogPostStatus;
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
public class BlogPostListItemDTO {
    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String featuredImage;
    private BlogPostStatus status;
    private LocalDateTime publishDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<BlogCategoryDTO> categories;
    private Set<String> tags;
    private Integer views;
    private AuthorDTO author;
    
    // Additional fields for simplified access
    private String authorName;
    private Set<String> categoryNames;
}
