package com.suppkart.dto.admin.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentRequest {
    
    @NotBlank(message = "Courier company is required")
    private String courierCompany;
    
    @NotBlank(message = "Tracking number is required")
    private String trackingNumber;
    
    @NotNull(message = "Package weight is required")
    @Positive(message = "Package weight must be positive")
    private Double packageWeight;
    
    private LocalDate estimatedDeliveryDate;
}
