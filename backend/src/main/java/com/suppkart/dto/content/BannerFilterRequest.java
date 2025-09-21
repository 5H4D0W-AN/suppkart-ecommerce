package com.suppkart.dto.content;

import com.suppkart.model.enums.TargetDevice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for filtering banners
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerFilterRequest {

    private Boolean active;
    private String location;
    private TargetDevice targetDevice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String title;
    private String sortBy = "displayOrder"; // Default sort by display order
    private String sortDirection = "ASC"; // Default ascending order
}
