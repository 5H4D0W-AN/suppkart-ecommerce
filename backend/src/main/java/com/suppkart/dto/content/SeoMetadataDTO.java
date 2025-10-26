package com.suppkart.dto.content;

import com.suppkart.model.enums.ContentType;
import com.suppkart.model.enums.PageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeoMetadataDTO {
    private Long id;
    private PageType pageType;
    private String elementKey;
    private String elementLabel;
    private ContentType contentType;
    private String contentValue;
    private String mediaUrl;
    private String altText;
    private Integer displayOrder;
    private Boolean isActive;
    
    // SEO fields
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private String ogTitle;
    private String ogDescription;
    private String ogImage;
    private String canonicalUrl;
    private Boolean noIndex;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}