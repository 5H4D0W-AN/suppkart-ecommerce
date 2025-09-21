package com.suppkart.dto.admin.consultation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private Integer totalPending;
    private Integer totalConfirmed;
    private Integer totalCompleted;
    private Integer todayConsultations;
    private Integer weekConsultations;
}
