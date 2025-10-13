package com.suppkart.service;

import com.suppkart.dto.admin.product.ProductVariantRequest;
import com.suppkart.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ProductVariantValidator {
    
    public void validateVariantList(List<ProductVariantRequest> variants) {
        if (variants == null || variants.isEmpty()) {
            throw new BusinessException("VARIANTS_REQUIRED", "Product must have at least one variant");
        }
        
        long defaultCount = variants.stream()
            .mapToLong(v -> v.isDefault() ? 1 : 0)
            .sum();
            
        if (defaultCount > 1) {
            throw new BusinessException("MULTIPLE_DEFAULTS", "Only one variant can be default");
        }
        
        if (defaultCount == 0) {
            variants.get(0).setDefault(true); // Set first as default
        }
        
        // Validate discount percentages
        variants.forEach(variant -> {
            if (variant.getDiscountPercentage() != null && 
                variant.getDiscountPercentage().compareTo(new BigDecimal("40.0")) > 0) {
                throw new BusinessException("DISCOUNT_LIMIT_EXCEEDED", 
                    "Discount cannot exceed 40% for variant: " + variant.getName());
            }
        });
        
        // Validate SKU uniqueness within the variant list
        long uniqueSkuCount = variants.stream()
            .map(ProductVariantRequest::getSku)
            .distinct()
            .count();
            
        if (uniqueSkuCount != variants.size()) {
            throw new BusinessException("DUPLICATE_VARIANT_SKU", "Variant SKUs must be unique within a product");
        }
    }
}