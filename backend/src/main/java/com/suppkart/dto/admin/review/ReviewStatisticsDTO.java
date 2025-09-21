package com.suppkart.dto.admin.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for review statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatisticsDTO {
    private Integer totalReviews;
    private Double averageRating;
    private Map<Integer, Integer> ratingDistribution;
    private Integer pendingReviews;
    private Double verifiedReviewsPercentage;
    private Integer reviewsLastMonth;
}
