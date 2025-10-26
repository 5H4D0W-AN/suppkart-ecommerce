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
public class ConsultationDTO {
    private Long id;
    private String customerName;
    private String customerEmail;
    private LocalDate date;
    private String topic;
    private String status;
}
