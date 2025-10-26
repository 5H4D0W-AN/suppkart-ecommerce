package com.suppkart.dto.content;

import com.suppkart.model.enums.ContentType;
import com.suppkart.model.enums.PageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeoMetadataRequest {
    
    @NotNull(message = "Page type is required")
    private PageType pageType;
    
    @NotBlank(message = "Element key is required")
    @Size(max = 100, message = "Element key must not exceed 100 characters")
    private String elementKey;
    
    @Size(max = 200, message = "Element label must not exceed 200 characters")
    private String elementLabel;
    
    @NotNull(message = "Content type is required")
    private ContentType contentType;
    
    private String contentValue;
    
    @Size(max = 500, message = "Media URL must not exceed 500 characters")
    private String mediaUrl;
    
    @Size(max = 255, message = "Alt text must not exceed 255 characters")
    private String altText;
    
    private Integer displayOrder;
    
    @Builder.Default
    private Boolean isActive = true;
    
    // SEO fields
    @Size(max = 255, message = "Meta title must not exceed 255 characters")
    private String metaTitle;
    
    private String metaDescription;
    
    private String metaKeywords;
    
    @Size(max = 255, message = "OG title must not exceed 255 characters")
    private String ogTitle;
    
    private String ogDescription;
    
    @Size(max = 500, message = "OG image must not exceed 500 characters")
    private String ogImage;
    
    @Size(max = 500, message = "Canonical URL must not exceed 500 characters")
    private String canonicalUrl;
    
    @Builder.Default
    private Boolean noIndex = false;
}