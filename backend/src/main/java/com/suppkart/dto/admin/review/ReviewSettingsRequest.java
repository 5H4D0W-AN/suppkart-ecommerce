package com.suppkart.dto.admin.review;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSettingsRequest {
    
    @NotNull(message = "Auto approve setting is required")
    private Boolean autoApprove;
    
    @NotNull(message = "Require verified purchase setting is required")
    private Boolean requireVerifiedPurchase;
    
    @Min(value = 0, message = "Minimum review length must be non-negative")
    private Integer minimumReviewLength;
    
    private List<String> moderationEmails;
}
