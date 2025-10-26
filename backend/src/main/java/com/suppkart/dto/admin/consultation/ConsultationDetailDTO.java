package com.suppkart.dto.admin.consultation;

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
public class ConsultationDetailDTO {
    private Long id;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Long customerId;
    private LocalDate date;
    private String topic;
    private String consultationType;
    private String notes;
    private String adminNotes;
    private String status;
    private LocalDateTime createdAt;
}
