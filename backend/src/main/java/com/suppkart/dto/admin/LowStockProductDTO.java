package com.suppkart.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LowStockProductDTO {
    private Long id;
    private String name;
    private String sku;
    private int currentStock;
    private String thumbnail;
}
