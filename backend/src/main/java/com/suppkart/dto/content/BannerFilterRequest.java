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
    private String sortBy; // Sort field (defaults to displayOrder in service if null)
    private String sortDirection; // Sort direction (defaults to ASC in service if null)
}
