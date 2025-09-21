package com.suppkart.dto.content;

import com.suppkart.model.enums.TargetDevice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating a banner
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerCreateRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
    
    private String linkUrl;
    
    private String altText;
    
    private Integer displayOrder;
    
    @NotNull(message = "Active status is required")
    private Boolean active;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    @NotNull(message = "Target device is required")
    private TargetDevice targetDevice;
    
    @NotBlank(message = "Location is required")
    private String location;
}
