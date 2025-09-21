package com.suppkart.dto.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for SEO metadata create/update requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeoMetadataRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 500, message = "Keywords must not exceed 500 characters")
    private String keywords;

    @Size(max = 255, message = "OG title must not exceed 255 characters")
    private String ogTitle;

    @Size(max = 500, message = "OG description must not exceed 500 characters")
    private String ogDescription;

    @Size(max = 500, message = "OG image URL must not exceed 500 characters")
    private String ogImage;

    @Size(max = 500, message = "Canonical URL must not exceed 500 characters")
    private String canonicalUrl;

    private Boolean noIndex = false;
}
