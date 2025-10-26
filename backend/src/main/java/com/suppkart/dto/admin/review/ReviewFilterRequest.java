package com.suppkart.dto.admin.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for review filtering parameters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewFilterRequest {
    private String search;
    private Long productId;
    private Long customerId;
    private Integer rating;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean verified;
    private Boolean isFake;
    private Boolean isVisible;
    private Boolean approved;
}
