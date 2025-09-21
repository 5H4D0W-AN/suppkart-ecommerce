package com.suppkart.dto.admin.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for review settings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSettingsDTO {
    private Boolean autoApprove;
    private Boolean requireVerifiedPurchase;
    private Integer minimumReviewLength;
    private Boolean allowImages;
    private Boolean allowReplies;
    private List<String> moderationEmails;
}
