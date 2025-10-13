package com.suppkart.dto.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for suggested products in blog posts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestedProductDTO {

    private Long productId;
    private String name;
    private String shortDescription;
    private BigDecimal price;
    private String imageUrl;
    private String slug;
    private Boolean inStock;
    private Double rating;
    private Integer reviewCount;
}
