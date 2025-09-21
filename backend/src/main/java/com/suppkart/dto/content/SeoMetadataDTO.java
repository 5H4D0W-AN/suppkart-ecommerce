package com.suppkart.dto.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for SEO metadata response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeoMetadataDTO {

    private Long id;
    private String pageType;
    private Long entityId;
    private String title;
    private String description;
    private String keywords;
    private String ogTitle;
    private String ogDescription;
    private String ogImage;
    private String canonicalUrl;
    private Boolean noIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
