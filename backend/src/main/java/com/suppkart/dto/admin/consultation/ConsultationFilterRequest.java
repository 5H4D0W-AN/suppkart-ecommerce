package com.suppkart.dto.admin.consultation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationFilterRequest {
    private String search;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String topic;
}
