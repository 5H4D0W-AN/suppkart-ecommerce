package com.suppkart.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerMetricsDTO {
    private int newCustomers;
    private int returningCustomers;
    private double conversionRate;
}
