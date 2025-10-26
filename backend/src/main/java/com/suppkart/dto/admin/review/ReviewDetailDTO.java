package com.suppkart.dto.admin.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for detailed review information in admin interface
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetailDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private Long customerId;
    private String customerName;
    private Boolean verified;
    private Integer rating;
    private String title;
    private String content;
    private String status;
    private Integer helpfulVotes;
    private LocalDateTime createdAt;
    private Boolean isFake;
}
