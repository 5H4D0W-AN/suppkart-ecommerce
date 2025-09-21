package com.suppkart.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentOrderDTO {
    private Long id;
    private String orderNumber;
    private String customerName;
    private LocalDateTime date;
    private BigDecimal total;
    private String status;
    private String paymentStatus;
}
