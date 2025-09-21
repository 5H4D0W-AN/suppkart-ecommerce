package com.suppkart.dto.content;

import com.suppkart.model.enums.TargetDevice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for updating a banner
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerUpdateRequest {
    
    private String title;
    private String imageUrl;
    private String linkUrl;
    private String altText;
    private Integer displayOrder;
    private Boolean active;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private TargetDevice targetDevice;
    private String location;
}
