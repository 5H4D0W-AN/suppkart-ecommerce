package com.suppkart.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suppkart.exception.BusinessException;
import com.suppkart.model.entity.Category;
import com.suppkart.model.entity.Product;
import com.suppkart.model.entity.ProductVariant;
import com.suppkart.model.enums.Brand;
import com.suppkart.repository.CategoryRepository;
import com.suppkart.repository.ProductRepository;
import com.suppkart.repository.ProductVariantRepository;

@Service
@Transactional
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Get all products with pagination and filtering
     */
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        logger.info("Fetching all products with pagination: page={}, size={}", 
                   pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            Page<Product> products = productRepository.findByIsActiveTrue(pageable);
            logger.info("Successfully fetched {} products", products.getTotalElements());
            return products;
        } catch (Exception e) {
            logger.error("Error fetching products: {}", e.getMessage(), e);
            throw new BusinessException("PRODUCT_FETCH_ERROR", "Failed to fetch products");
        }
    }

    /**
     * Get products by category with pagination
     */
    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        logger.info("Fetching products by category: categoryId={}, page={}, size={}", 
                   categoryId, pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            if (!categoryRepository.existsById(categoryId)) {
                throw new BusinessException("CATEGORY_NOT_FOUND", "Category not found with id: " + categoryId);
            }
            
            Page<Product> products = productRepository.findByCategoryCategoryIdAndActiveTrue(categoryId, pageable);
            logger.info("Successfully fetched {} products for category {}", products.getTotalElements(), categoryId);
            return products;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching products by category: {}", e.getMessage(), e);
            throw new BusinessException("PRODUCT_FETCH_ERROR", "Failed to fetch products by category");
        }
    }

    /**
     * Get products by brand with pagination
     */
    @Transactional(readOnly = true)
    public Page<Product> getProductsByBrand(Brand brand, Pageable pageable) {
        logger.info("Fetching products by brand: brand={}, page={}, size={}", 
                   brand, pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            Page<Product> products = productRepository.findByBrandAndIsActiveTrue(brand, pageable);
            logger.info("Successfully fetched {} products for brand {}", products.getTotalElements(), brand);
            return products;
        } catch (Exception e) {
            logger.error("Error fetching products by brand: {}", e.getMessage(), e);
            throw new BusinessException("PRODUCT_FETCH_ERROR", "Failed to fetch products by brand");
        }
    }

    /**
     * Search products by name or description
     */
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String searchTerm, Pageable pageable) {
        logger.info("Searching products with term: '{}', page={}, size={}", 
                   searchTerm, pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return getAllProducts(pageable);
            }
            
            String trimmedSearchTerm = searchTerm.trim();
            Page<Product> products = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndActiveTrue(
                    trimmedSearchTerm, trimmedSearchTerm, pageable);
            
            logger.info("Search found {} products for term '{}'", products.getTotalElements(), trimmedSearchTerm);
            return products;
        } catch (Exception e) {
            logger.error("Error searching products: {}", e.getMessage(), e);
            throw new BusinessException("PRODUCT_SEARCH_ERROR", "Failed to search products");
        }
    }

    /**
     * Get product by ID with all details including variants
     */
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long productId) {
        logger.info("Fetching product by ID: {}", productId);
        
        try {
            Optional<Product> product = productRepository.findByProductIdAndIsActiveTrue(productId);
            
            if (product.isPresent()) {
                logger.info("Successfully found product: {}", product.get().getName());
            } else {
                logger.warn("Product not found with ID: {}", productId);
            }
            
            return product;
        } catch (Exception e) {
            logger.error("Error fetching product by ID: {}", e.getMessage(), e);
            throw new BusinessException("PRODUCT_FETCH_ERROR", "Failed to fetch product details");
        }
    }

    /**
     * Get all variants for a product
     */
    @Transactional(readOnly = true)
    public List<ProductVariant> getProductVariants(Long productId) {
        logger.info("Fetching variants for product ID: {}", productId);
        
        try {
            if (!productRepository.existsByProductIdAndIsActiveTrue(productId)) {
                throw new BusinessException("PRODUCT_NOT_FOUND", "Product not found with id: " + productId);
            }
            
            List<ProductVariant> variants = productVariantRepository.findByProduct_ProductIdAndIsActiveTrue(productId);
            logger.info("Found {} variants for product {}", variants.size(), productId);
            return variants;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching product variants: {}", e.getMessage(), e);
            throw new BusinessException("VARIANT_FETCH_ERROR", "Failed to fetch product variants");
        }
    }

    /**
     * Get specific product variant
     */
    @Transactional(readOnly = true)
    public Optional<ProductVariant> getProductVariant(Long variantId) {
        logger.info("Fetching product variant by ID: {}", variantId);
        
        try {
            Optional<ProductVariant> variant = productVariantRepository.findByVariantIdAndIsActiveTrue(variantId);
            
            if (variant.isPresent()) {
                logger.info("Successfully found variant: {} - {}", 
                           variant.get().getProduct().getName(), variant.get().getSize());
            } else {
                logger.warn("Product variant not found with ID: {}", variantId);
            }
            
            return variant;
        } catch (Exception e) {
            logger.error("Error fetching product variant: {}", e.getMessage(), e);
            throw new BusinessException("VARIANT_FETCH_ERROR", "Failed to fetch product variant");
        }
    }

    /**
     * Get featured products
     */
    @Transactional(readOnly = true)
    public List<Product> getFeaturedProducts() {
        logger.info("Fetching featured products");
        
        try {
            List<Product> products = productRepository.findByFeaturedTrueAndActiveTrueOrderByCreatedAtDesc();
            logger.info("Found {} featured products", products.size());
            return products;
        } catch (Exception e) {
            logger.error("Error fetching featured products: {}", e.getMessage(), e);
            throw new BusinessException("PRODUCT_FETCH_ERROR", "Failed to fetch featured products");
        }
    }

    /**
     * Get related products based on categories and sports
     */
    @Transactional(readOnly = true)
    public List<Product> getRelatedProducts(Long productId, int limit) {
        logger.info("Fetching related products for product ID: {}, limit: {}", productId, limit);
        
        try {
            Optional<Product> productOpt = getProductById(productId);
            if (!productOpt.isPresent()) {
                throw new BusinessException("PRODUCT_NOT_FOUND", "Product not found with id: " + productId);
            }
            
            Product product = productOpt.get();
            // For now, use a simple related products query - will be improved once entity relationships are confirmed
            List<Product> relatedProducts = productRepository.findRelatedProducts(
                    productId, 
                    null, // Will be updated once Product-Category relationship is confirmed
                    limit
            );
            
            logger.info("Found {} related products for product {}", relatedProducts.size(), productId);
            return relatedProducts;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching related products: {}", e.getMessage(), e);
            throw new BusinessException("PRODUCT_FETCH_ERROR", "Failed to fetch related products");
        }
    }

    /**
     * Get all categories
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        logger.info("Fetching all categories");
        
        try {
            List<Category> categories = categoryRepository.findByIsActiveTrueOrderByNameAsc();
            logger.info("Found {} categories", categories.size());
            return categories;
        } catch (Exception e) {
            logger.error("Error fetching categories: {}", e.getMessage(), e);
            throw new BusinessException("CATEGORY_FETCH_ERROR", "Failed to fetch categories");
        }
    }

    /**
     * Check product availability
     */
    @Transactional(readOnly = true)
    public boolean isProductAvailable(Long productId) {
        logger.info("Checking availability for product ID: {}", productId);
        
        try {
            Optional<Product> product = getProductById(productId);
            boolean available = product.isPresent() && product.get().getIsActive();
            
            logger.info("Product {} availability: {}", productId, available);
            return available;
        } catch (Exception e) {
            logger.error("Error checking product availability: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check variant availability and stock
     */
    @Transactional(readOnly = true)
    public boolean isVariantAvailable(Long variantId, int requestedQuantity) {
        logger.info("Checking availability for variant ID: {}, quantity: {}", variantId, requestedQuantity);
        
        try {
            Optional<ProductVariant> variant = getProductVariant(variantId);
            
            if (!variant.isPresent()) {
                logger.warn("Variant not found: {}", variantId);
                return false;
            }
            
            ProductVariant productVariant = variant.get();
            boolean available = productVariant.getIsActive() && 
                               productVariant.getStockQuantity() >= requestedQuantity;
            
            logger.info("Variant {} availability for quantity {}: {}", 
                       variantId, requestedQuantity, available);
            return available;
        } catch (Exception e) {
            logger.error("Error checking variant availability: {}", e.getMessage(), e);
            return false;
        }
    }
}
