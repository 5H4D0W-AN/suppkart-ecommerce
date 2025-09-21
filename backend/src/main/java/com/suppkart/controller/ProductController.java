package com.suppkart.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suppkart.dto.response.ApiResponse;
import com.suppkart.exception.BusinessException;
import com.suppkart.model.entity.Category;
import com.suppkart.model.entity.Product;
import com.suppkart.model.entity.ProductVariant;
import com.suppkart.model.enums.Brand;
import com.suppkart.service.ProductService;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/products")
@Validated
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    /**
     * Get all products with pagination
     * GET /api/products?page=0&size=10&sort=name,asc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Product>>> getAllProducts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        logger.info("GET /api/products - page: {}, size: {}, sortBy: {}, sortDirection: {}", 
                   page, size, sortBy, sortDirection);
        
        try {
            Sort.Direction direction = Sort.Direction.fromString(sortDirection);
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<Product> products = productService.getAllProducts(pageable);
            
            logger.info("Successfully retrieved {} products, total pages: {}", 
                       products.getNumberOfElements(), products.getTotalPages());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Products retrieved successfully", products)
            );
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid sort direction: {}", sortDirection, e);
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, "Invalid sort direction: " + sortDirection, null)
            );
        } catch (BusinessException e) {
            logger.error("Business error retrieving products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, e.getMessage(), null)
            );
        } catch (Exception e) {
            logger.error("Unexpected error retrieving products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Failed to retrieve products", null)
            );
        }
    }

    /**
     * Get product by ID
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(
            @PathVariable @NotNull @Min(1) Long id) {
        
        logger.info("GET /api/products/{} - Retrieving product by ID", id);
        
        try {
            Optional<Product> product = productService.getProductById(id);
            
            if (product.isPresent()) {
                logger.info("Successfully retrieved product: {}", product.get().getName());
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "Product retrieved successfully", product.get())
                );
            } else {
                logger.warn("Product not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(false, "Product not found with id: " + id, null)
                );
            }
            
        } catch (BusinessException e) {
            logger.error("Business error retrieving product {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, e.getMessage(), null)
            );
        } catch (Exception e) {
            logger.error("Unexpected error retrieving product {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Failed to retrieve product", null)
            );
        }
    }

    /**
     * Get products by category
     * GET /api/products/category/{categoryId}?page=0&size=10
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<Product>>> getProductsByCategory(
            @PathVariable @NotNull @Min(1) Long categoryId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        logger.info("GET /api/products/category/{} - page: {}, size: {}", 
                   categoryId, page, size);
        
        try {
            Sort.Direction direction = Sort.Direction.fromString(sortDirection);
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<Product> products = productService.getProductsByCategory(categoryId, pageable);
            
            logger.info("Successfully retrieved {} products for category {}", 
                       products.getNumberOfElements(), categoryId);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Products retrieved successfully", products)
            );
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid sort direction: {}", sortDirection, e);
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, "Invalid sort direction: " + sortDirection, null)
            );
        } catch (BusinessException e) {
            logger.error("Business error retrieving products for category {}: {}", 
                        categoryId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, e.getMessage(), null)
            );
        } catch (Exception e) {
            logger.error("Unexpected error retrieving products for category {}: {}", 
                        categoryId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Failed to retrieve products", null)
            );
        }
    }

    /**
     * Get products by brand
     * GET /api/products/brand/{brand}?page=0&size=10
     */
    @GetMapping("/brand/{brand}")
    public ResponseEntity<ApiResponse<Page<Product>>> getProductsByBrand(
            @PathVariable @NotNull Brand brand,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        logger.info("GET /api/products/brand/{} - page: {}, size: {}", 
                   brand, page, size);
        
        try {
            Sort.Direction direction = Sort.Direction.fromString(sortDirection);
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<Product> products = productService.getProductsByBrand(brand, pageable);
            
            logger.info("Successfully retrieved {} products for brand {}", 
                       products.getNumberOfElements(), brand);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Products retrieved successfully", products)
            );
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid sort direction: {}", sortDirection, e);
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, "Invalid sort direction: " + sortDirection, null)
            );
        } catch (BusinessException e) {
            logger.error("Business error retrieving products for brand {}: {}", 
                        brand, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, e.getMessage(), null)
            );
        } catch (Exception e) {
            logger.error("Unexpected error retrieving products for brand {}: {}", 
                        brand, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Failed to retrieve products", null)
            );
        }
    }

    /**
     * Search products
     * GET /api/products/search?q=protein&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<Product>>> searchProducts(
            @RequestParam("q") String searchTerm,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        logger.info("GET /api/products/search - query: '{}', page: {}, size: {}", 
                   searchTerm, page, size);
        
        try {
            Sort.Direction direction = Sort.Direction.fromString(sortDirection);
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<Product> products = productService.searchProducts(searchTerm, pageable);
            
            logger.info("Search found {} products for term '{}'", 
                       products.getNumberOfElements(), searchTerm);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Search completed successfully", products)
            );
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid sort direction: {}", sortDirection, e);
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, "Invalid sort direction: " + sortDirection, null)
            );
        } catch (BusinessException e) {
            logger.error("Business error searching products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, e.getMessage(), null)
            );
        } catch (Exception e) {
            logger.error("Unexpected error searching products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Search failed", null)
            );
        }
    }

    /**
     * Get product variants
     * GET /api/products/{id}/variants
     */
    @GetMapping("/{id}/variants")
    public ResponseEntity<ApiResponse<List<ProductVariant>>> getProductVariants(
            @PathVariable @NotNull @Min(1) Long id) {
        
        logger.info("GET /api/products/{}/variants - Retrieving variants", id);
        
        try {
            List<ProductVariant> variants = productService.getProductVariants(id);
            
            logger.info("Successfully retrieved {} variants for product {}", 
                       variants.size(), id);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Product variants retrieved successfully", variants)
            );
            
        } catch (BusinessException e) {
            logger.error("Business error retrieving variants for product {}: {}", 
                        id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, e.getMessage(), null)
            );
        } catch (Exception e) {
            logger.error("Unexpected error retrieving variants for product {}: {}", 
                        id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Failed to retrieve product variants", null)
            );
        }
    }

    /**
     * Get specific product variant
     * GET /api/products/variants/{variantId}
     */
    @GetMapping("/variants/{variantId}")
    public ResponseEntity<ApiResponse<ProductVariant>> getProductVariant(
            @PathVariable @NotNull @Min(1) Long variantId) {
        
        logger.info("GET /api/products/variants/{} - Retrieving variant", variantId);
        
        try {
            Optional<ProductVariant> variant = productService.getProductVariant(variantId);
            
            if (variant.isPresent()) {
                logger.info("Successfully retrieved variant: {}", variantId);
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "Product variant retrieved successfully", variant.get())
                );
            } else {
                logger.warn("Product variant not found with ID: {}", variantId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(false, "Product variant not found with id: " + variantId, null)
                );
            }
            
        } catch (BusinessException e) {
            logger.error("Business error retrieving variant {}: {}", variantId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, e.getMessage(), null)
            );
        } catch (Exception e) {
            logger.error("Unexpected error retrieving variant {}: {}", variantId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Failed to retrieve product variant", null)
            );
        }
    }

    /**
     * Get featured products
     * GET /api/products/featured
     */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<Product>>> getFeaturedProducts() {
        
        logger.info("GET /api/products/featured - Retrieving featured products");
        
        try {
            List<Product> products = productService.getFeaturedProducts();
            
            logger.info("Successfully retrieved {} featured products", products.size());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Featured products retrieved successfully", products)
            );
            
        } catch (BusinessException e) {
            logger.error("Business error retrieving featured products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, e.getMessage(), null)
            );
        } catch (Exception e) {
            logger.error("Unexpected error retrieving featured products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Failed to retrieve featured products", null)
            );
        }
    }

    /**
     * Get related products
     * GET /api/products/{id}/related?limit=5
     */
    @GetMapping("/{id}/related")
    public ResponseEntity<ApiResponse<List<Product>>> getRelatedProducts(
            @PathVariable @NotNull @Min(1) Long id,
            @RequestParam(defaultValue = "5") @Min(1) int limit) {
        
        logger.info("GET /api/products/{}/related - limit: {}", id, limit);
        
        try {
            List<Product> relatedProducts = productService.getRelatedProducts(id, limit);
            
            logger.info("Successfully retrieved {} related products for product {}", 
                       relatedProducts.size(), id);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Related products retrieved successfully", relatedProducts)
            );
            
        } catch (BusinessException e) {
            logger.error("Business error retrieving related products for {}: {}", 
                        id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, e.getMessage(), null)
            );
        } catch (Exception e) {
            logger.error("Unexpected error retrieving related products for {}: {}", 
                        id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Failed to retrieve related products", null)
            );
        }
    }

    /**
     * Get all categories
     * GET /api/products/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        
        logger.info("GET /api/products/categories - Retrieving all categories");
        
        try {
            List<Category> categories = productService.getAllCategories();
            
            logger.info("Successfully retrieved {} categories", categories.size());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Categories retrieved successfully", categories)
            );
            
        } catch (BusinessException e) {
            logger.error("Business error retrieving categories: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, e.getMessage(), null)
            );
        } catch (Exception e) {
            logger.error("Unexpected error retrieving categories: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Failed to retrieve categories", null)
            );
        }
    }

    /**
     * Check product availability
     * GET /api/products/{id}/availability
     */
    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Boolean>> checkProductAvailability(
            @PathVariable @NotNull @Min(1) Long id) {
        
        logger.info("GET /api/products/{}/availability - Checking availability", id);
        
        try {
            boolean available = productService.isProductAvailable(id);
            
            logger.info("Product {} availability: {}", id, available);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Product availability checked", available)
            );
            
        } catch (Exception e) {
            logger.error("Error checking product {} availability: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Failed to check product availability", false)
            );
        }
    }

    /**
     * Check variant availability
     * GET /api/products/variants/{variantId}/availability?quantity=1
     */
    @GetMapping("/variants/{variantId}/availability")
    public ResponseEntity<ApiResponse<Boolean>> checkVariantAvailability(
            @PathVariable @NotNull @Min(1) Long variantId,
            @RequestParam(defaultValue = "1") @Min(1) int quantity) {
        
        logger.info("GET /api/products/variants/{}/availability - quantity: {}", 
                   variantId, quantity);
        
        try {
            boolean available = productService.isVariantAvailable(variantId, quantity);
            
            logger.info("Variant {} availability for quantity {}: {}", 
                       variantId, quantity, available);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Variant availability checked", available)
            );
            
        } catch (Exception e) {
            logger.error("Error checking variant {} availability: {}", variantId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Failed to check variant availability", false)
            );
        }
    }
}
