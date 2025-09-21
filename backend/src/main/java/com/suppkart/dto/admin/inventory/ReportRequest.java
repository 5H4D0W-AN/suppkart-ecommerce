package com.suppkart.dto.admin.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Inventory Report generation requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    
    @NotBlank(message = "Report format is required")
    @Pattern(regexp = "^(PDF|EXCEL|CSV)$", message = "Format must be PDF, EXCEL, or CSV")
    private String format;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;
    
    private Boolean includeHistory;
    
    private Boolean includeLowStock;
    
    private Boolean includeOutOfStock;
    
    private Boolean includeAlerts;
    
    private Boolean includeValuation;
    
    private Boolean includeMovement;
    
    private Boolean includeAging;
    
    private List<Long> categoryIds;
    
    private List<String> brandNames;
    
    private List<Long> productIds;
    
    private List<Long> variantIds;
    
    private String reportType;
    
    private String groupBy;
    
    private String sortBy;
    
    private String sortDirection;
    
    // Additional filters
    private Integer minQuantity;
    
    private Integer maxQuantity;
    
    private String status;
    
    private Boolean activeOnly;
    
    private String warehouseLocation;
    
    private String supplierName;
    
    // Report customization
    private String title;
    
    private String description;
    
    private Boolean includeCharts;
    
    private Boolean includeSummary;
    
    private String language;
    
    private String currency;
    
    private String timezone;
    
    // Helper methods
    public String getFormat() {
        return format != null ? format.toUpperCase() : "PDF";
    }
    
    public Boolean getIncludeHistory() {
        return includeHistory != null ? includeHistory : false;
    }
    
    public Boolean getIncludeLowStock() {
        return includeLowStock != null ? includeLowStock : true;
    }
    
    public Boolean getIncludeOutOfStock() {
        return includeOutOfStock != null ? includeOutOfStock : true;
    }
    
    public Boolean getIncludeAlerts() {
        return includeAlerts != null ? includeAlerts : false;
    }
    
    public Boolean getIncludeValuation() {
        return includeValuation != null ? includeValuation : false;
    }
    
    public Boolean getIncludeMovement() {
        return includeMovement != null ? includeMovement : false;
    }
    
    public Boolean getIncludeAging() {
        return includeAging != null ? includeAging : false;
    }
    
    public String getReportType() {
        return reportType != null ? reportType : "INVENTORY_SUMMARY";
    }
    
    public String getGroupBy() {
        return groupBy != null ? groupBy : "CATEGORY";
    }
    
    public String getSortBy() {
        return sortBy != null ? sortBy : "productName";
    }
    
    public String getSortDirection() {
        return sortDirection != null ? sortDirection.toUpperCase() : "ASC";
    }
    
    public Boolean getActiveOnly() {
        return activeOnly != null ? activeOnly : true;
    }
    
    public Boolean getIncludeCharts() {
        return includeCharts != null ? includeCharts : true;
    }
    
    public Boolean getIncludeSummary() {
        return includeSummary != null ? includeSummary : true;
    }
    
    public String getLanguage() {
        return language != null ? language : "en";
    }
    
    public String getCurrency() {
        return currency != null ? currency : "INR";
    }
    
    public String getTimezone() {
        return timezone != null ? timezone : "Asia/Kolkata";
    }
    
    public String getTitle() {
        if (title != null && !title.trim().isEmpty()) {
            return title;
        }
        return generateDefaultTitle();
    }
    
    private String generateDefaultTitle() {
        String type = getReportType();
        return switch (type) {
            case "INVENTORY_SUMMARY" -> "Inventory Summary Report";
            case "LOW_STOCK" -> "Low Stock Report";
            case "OUT_OF_STOCK" -> "Out of Stock Report";
            case "STOCK_MOVEMENT" -> "Stock Movement Report";
            case "INVENTORY_VALUATION" -> "Inventory Valuation Report";
            case "STOCK_AGING" -> "Stock Aging Report";
            case "ALERT_SUMMARY" -> "Stock Alert Summary Report";
            default -> "Inventory Report";
        };
    }
    
    public String getDescription() {
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        return generateDefaultDescription();
    }
    
    private String generateDefaultDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("Generated on ").append(LocalDateTime.now().toString());
        
        if (startDate != null && endDate != null) {
            desc.append(" for period from ").append(startDate.toLocalDate())
                .append(" to ").append(endDate.toLocalDate());
        }
        
        if (categoryIds != null && !categoryIds.isEmpty()) {
            desc.append(" (").append(categoryIds.size()).append(" categories)");
        }
        
        if (brandNames != null && !brandNames.isEmpty()) {
            desc.append(" (").append(brandNames.size()).append(" brands)");
        }
        
        return desc.toString();
    }
    
    public boolean hasDateRange() {
        return startDate != null && endDate != null;
    }
    
    public boolean hasCategoryFilter() {
        return categoryIds != null && !categoryIds.isEmpty();
    }
    
    public boolean hasBrandFilter() {
        return brandNames != null && !brandNames.isEmpty();
    }
    
    public boolean hasProductFilter() {
        return productIds != null && !productIds.isEmpty();
    }
    
    public boolean hasVariantFilter() {
        return variantIds != null && !variantIds.isEmpty();
    }
    
    public boolean hasQuantityFilter() {
        return minQuantity != null || maxQuantity != null;
    }
    
    public boolean isDetailedReport() {
        return Boolean.TRUE.equals(getIncludeHistory()) || 
               Boolean.TRUE.equals(getIncludeMovement()) || 
               Boolean.TRUE.equals(getIncludeAging());
    }
    
    public String getFileExtension() {
        return switch (getFormat()) {
            case "PDF" -> ".pdf";
            case "EXCEL" -> ".xlsx";
            case "CSV" -> ".csv";
            default -> ".pdf";
        };
    }
    
    public String getContentType() {
        return switch (getFormat()) {
            case "PDF" -> "application/pdf";
            case "EXCEL" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "CSV" -> "text/csv";
            default -> "application/pdf";
        };
    }
}
