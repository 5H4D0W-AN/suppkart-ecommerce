package com.suppkart.dto.admin.review;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating review status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatusUpdateRequest {
    @NotBlank(message = "Status is required")
    private String status;
    
    private String reason;
    
    @Builder.Default
    private Boolean notifyCustomer = false;
}
