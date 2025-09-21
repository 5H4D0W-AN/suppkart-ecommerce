package com.suppkart.dto.content;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for reordering banners
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerOrderRequest {

    @NotNull(message = "Banner ID is required")
    private Long bannerId;

    @NotNull(message = "Display order is required")
    private Integer displayOrder;
}
