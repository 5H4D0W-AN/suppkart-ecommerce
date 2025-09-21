package com.suppkart.controller;

import com.suppkart.dto.admin.product.*;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.service.AdminProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Admin Product Management operations
 */
@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminProductController {

    private static final Logger logger = LoggerFactory.getLogger(AdminProductController.class);

    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    /**
     * Create new product
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductDetailDTO>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        
        logger.info("Admin creating new product: {}", request.getName());
        
        ProductDetailDTO product = adminProductService.createProduct(request);
        
        ApiResponse<ProductDetailDTO> response = ApiResponse.success("Product created successfully", product);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update existing product
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        
        logger.info("Admin updating product with ID: {}", id);
        
        ProductDetailDTO product = adminProductService.updateProduct(id, request);
        
        ApiResponse<ProductDetailDTO> response = ApiResponse.success("Product updated successfully", product);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> getProductById(
            @PathVariable Long id) {
        
        logger.info("Admin fetching product with ID: {}", id);
        
        ProductDetailDTO product = adminProductService.getProductById(id);
        
        ApiResponse<ProductDetailDTO> response = ApiResponse.success("Product retrieved successfully", product);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all products with filtering and pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductListItemDTO>>> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        logger.info("Admin fetching products with search: {}, status: {}", search, status);
        
        // Create filter request
        ProductFilterRequest filter = new ProductFilterRequest();
        filter.setSearch(search);
        filter.setStatus(status);
        filter.setBrand(brand);
        filter.setCategoryId(categoryId);
        filter.setInStock(inStock);
        
        // Create pageable
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductListItemDTO> products = adminProductService.getAllProducts(filter, pageable);
        
        ApiResponse<Page<ProductListItemDTO>> response = ApiResponse.success("Products retrieved successfully", products);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete product (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id) {
        
        logger.info("Admin deleting product with ID: {}", id);
        
        adminProductService.deleteProduct(id);
        
        ApiResponse<Void> response = ApiResponse.success("Product deleted successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update product status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateProductStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusRequest) {
        
        String status = statusRequest.get("status");
        logger.info("Admin updating product status for ID: {} to: {}", id, status);
        
        adminProductService.changeProductStatus(id, status);
        
        ApiResponse<Void> response = ApiResponse.success("Product status updated successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Add product variant
     */
    @PostMapping("/{id}/variants")
    public ResponseEntity<ApiResponse<ProductVariantDTO>> addProductVariant(
            @PathVariable Long id,
            @Valid @RequestBody ProductVariantRequest request) {
        
        logger.info("Admin adding variant to product ID: {}", id);
        
        ProductVariantDTO variant = adminProductService.addProductVariant(id, request);
        
        ApiResponse<ProductVariantDTO> response = ApiResponse.success("Variant added successfully", variant);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update product variant
     */
    @PutMapping("/variants/{variantId}")
    public ResponseEntity<ApiResponse<ProductVariantDTO>> updateProductVariant(
            @PathVariable Long variantId,
            @Valid @RequestBody ProductVariantRequest request) {
        
        logger.info("Admin updating variant with ID: {}", variantId);
        
        ProductVariantDTO variant = adminProductService.updateProductVariant(variantId, request);
        
        ApiResponse<ProductVariantDTO> response = ApiResponse.success("Variant updated successfully", variant);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete product variant
     */
    @DeleteMapping("/variants/{variantId}")
    public ResponseEntity<ApiResponse<Void>> deleteProductVariant(
            @PathVariable Long variantId) {
        
        logger.info("Admin deleting variant with ID: {}", variantId);
        
        adminProductService.deleteProductVariant(variantId);
        
        ApiResponse<Void> response = ApiResponse.success("Variant deleted successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update variant stock
     */
    @PatchMapping("/variants/{variantId}/stock")
    public ResponseEntity<ApiResponse<Void>> updateVariantStock(
            @PathVariable Long variantId,
            @RequestBody Map<String, Integer> stockRequest) {
        
        Integer quantity = stockRequest.get("quantity");
        logger.info("Admin updating stock for variant ID: {} to quantity: {}", variantId, quantity);
        
        adminProductService.updateVariantStock(variantId, quantity);
        
        ApiResponse<Void> response = ApiResponse.success("Stock updated successfully");
        
        return ResponseEntity.ok(response);
    }
}
