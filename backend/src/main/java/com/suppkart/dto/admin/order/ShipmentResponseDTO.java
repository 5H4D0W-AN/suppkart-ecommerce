package com.suppkart.dto.admin.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponseDTO {
    
    private Long id;
    private String courierCompany;
    private String trackingNumber;
    private String trackingUrl;
    private String labelUrl;
    private Double packageWeight;
    private LocalDate estimatedDeliveryDate;
    private LocalDateTime shipmentDate;
}
