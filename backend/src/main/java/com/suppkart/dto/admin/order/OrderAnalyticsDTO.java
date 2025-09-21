package com.suppkart.dto.admin.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAnalyticsDTO {
    
    private LocalDate date;
    private Long orderCount;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private String groupBy; // DAY, WEEK, MONTH
}
