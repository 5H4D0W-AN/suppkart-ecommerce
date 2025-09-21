package com.suppkart.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingProductDTO {
    private Long id;
    private String name;
    private String sku;
    private int unitsSold;
    private BigDecimal revenue;
    private String thumbnail;
}
