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
public class BlogPostFilterRequest {
    private String title;
    private BlogPostStatus status;
    private Set<Long> categoryIds;
    private Set<String> tags;
    private Long authorId;
    private LocalDateTime publishDateFrom;
    private LocalDateTime publishDateTo;
    private LocalDateTime createdDateFrom;
    private LocalDateTime createdDateTo;
    
    // Additional fields for search and sorting
    private String searchQuery;
    private String sortBy;
    private String sortDirection;
    private String categorySlug;
    private Long categoryId;
}
