package com.suppkart.dto.admin.review;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for admin response to review
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseRequest {
    @NotBlank(message = "Response is required")
    private String response;
    
    @Builder.Default
    private Boolean notifyCustomer = false;
}
