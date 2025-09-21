package com.suppkart.service;

import com.suppkart.dto.admin.product.*;
import com.suppkart.exception.BusinessException;
import com.suppkart.model.entity.*;
import com.suppkart.model.enums.Brand;
import com.suppkart.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for Admin Product Management operations
 */
@Service
@Transactional
public class AdminProductService {

    private static final Logger logger = LoggerFactory.getLogger(AdminProductService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final SportRepository sportRepository;
    private final GoalRepository goalRepository;

    public AdminProductService(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              ProductVariantRepository productVariantRepository,
                              SportRepository sportRepository,
                              GoalRepository goalRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productVariantRepository = productVariantRepository;
        this.sportRepository = sportRepository;
        this.goalRepository = goalRepository;
    }

    /**
     * Create new product
     */
    public ProductDetailDTO createProduct(ProductCreateRequest request) {
        logger.info("Creating new product with name: {}", request.getName());

        // Check if SKU already exists
        if (productRepository.existsBySku(request.getSku())) {
            throw new BusinessException("PRODUCT_SKU_EXISTS", "Product with SKU " + request.getSku() + " already exists");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setShortDescription(request.getShortDescription());
        
        // Set brand - default to SUPPKART if not provided
        product.setBrand(Brand.SUPPKART);
        
        // Set active status based on request status
        product.setIsActive("ACTIVE".equals(request.getStatus()));
        
        product.setMetaTitle(request.getMetaTitle());
        product.setMetaDescription(request.getMetaDescription());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        // Create variants if provided
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (ProductVariantRequest variantRequest : request.getVariants()) {
                createProductVariant(savedProduct.getProductId(), variantRequest);
            }
        }

        // Handle categories through ProductCategory relationship
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            for (Long categoryId : request.getCategoryIds()) {
                Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "Category not found with ID: " + categoryId));
                
                ProductCategory productCategory = new ProductCategory();
                productCategory.setProduct(savedProduct);
                productCategory.setCategory(category);
                // Note: You'll need to save this through ProductCategoryRepository if it exists
            }
        }

        logger.info("Product created successfully with ID: {}", savedProduct.getProductId());
        return convertToProductDetailDTO(savedProduct);
    }

    /**
     * Update existing product
     */
    public ProductDetailDTO updateProduct(Long id, ProductUpdateRequest request) {
        logger.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found with ID: " + id));

        // Update fields if provided
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getSku() != null && !request.getSku().equals(product.getSku())) {
            if (productRepository.existsBySku(request.getSku())) {
                throw new BusinessException("PRODUCT_SKU_EXISTS", "Product with SKU " + request.getSku() + " already exists");
            }
            product.setSku(request.getSku());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getShortDescription() != null) {
            product.setShortDescription(request.getShortDescription());
        }
        if (request.getStatus() != null) {
            product.setIsActive("ACTIVE".equals(request.getStatus()));
        }
        if (request.getMetaTitle() != null) {
            product.setMetaTitle(request.getMetaTitle());
        }
        if (request.getMetaDescription() != null) {
            product.setMetaDescription(request.getMetaDescription());
        }

        product.setUpdatedAt(LocalDateTime.now());
        Product savedProduct = productRepository.save(product);

        logger.info("Product updated successfully with ID: {}", savedProduct.getProductId());
        return convertToProductDetailDTO(savedProduct);
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductDetailDTO getProductById(Long id) {
        logger.info("Fetching product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found with ID: " + id));

        return convertToProductDetailDTO(product);
    }

    /**
     * Get all products with filtering and pagination
     */
    @Transactional(readOnly = true)
    public Page<ProductListItemDTO> getAllProducts(ProductFilterRequest filter, Pageable pageable) {
        logger.info("Fetching products with filter: {}", filter.getSearch());

        Specification<Product> spec = createProductSpecification(filter);
        Page<Product> products = productRepository.findAll(spec, pageable);

        List<ProductListItemDTO> productDTOs = products.getContent().stream()
                .map(this::convertToProductListItemDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(productDTOs, pageable, products.getTotalElements());
    }

    /**
     * Delete product (soft delete)
     */
    public void deleteProduct(Long id) {
        logger.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found with ID: " + id));

        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        logger.info("Product deleted successfully with ID: {}", id);
    }

    /**
     * Change product status
     */
    public void changeProductStatus(Long id, String status) {
        logger.info("Changing product status for ID: {} to: {}", id, status);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found with ID: " + id));

        product.setIsActive("ACTIVE".equals(status));
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        logger.info("Product status changed successfully for ID: {}", id);
    }

    /**
     * Add product variant
     */
    public ProductVariantDTO addProductVariant(Long productId, ProductVariantRequest request) {
        logger.info("Adding variant to product ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found with ID: " + productId));

        // Check if variant SKU already exists
        if (productVariantRepository.existsBySku(request.getSku())) {
            throw new BusinessException("VARIANT_SKU_EXISTS", "Variant with SKU " + request.getSku() + " already exists");
        }

        ProductVariant variant = createProductVariant(productId, request);
        return convertToProductVariantDTO(variant);
    }

    /**
     * Update product variant
     */
    public ProductVariantDTO updateProductVariant(Long variantId, ProductVariantRequest request) {
        logger.info("Updating variant with ID: {}", variantId);

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new BusinessException("VARIANT_NOT_FOUND", "Variant not found with ID: " + variantId));

        // Update fields if provided
        if (request.getName() != null) {
            variant.setName(request.getName());
        }
        if (request.getSku() != null && !request.getSku().equals(variant.getSku())) {
            if (productVariantRepository.existsBySku(request.getSku())) {
                throw new BusinessException("VARIANT_SKU_EXISTS", "Variant with SKU " + request.getSku() + " already exists");
            }
            variant.setSku(request.getSku());
        }
        if (request.getPrice() != null) {
            variant.setPrice(request.getPrice());
        }
        if (request.getSalePrice() != null) {
            variant.setSalePrice(request.getSalePrice());
        }
        if (request.getStock() > 0) {
            variant.setStockQuantity(request.getStock());
        }
        if (request.getSize() != null) {
            variant.setSize(request.getSize());
        }
        if (request.getWeight() != null) {
            variant.setWeight(request.getWeight());
        }
        if (request.getFlavor() != null) {
            variant.setFlavor(request.getFlavor());
        }
        if (request.getImageUrl() != null) {
            variant.setImageUrl(request.getImageUrl());
        }
        if (request.getDiscountPercentage() != null) {
            variant.setDiscountPercentage(request.getDiscountPercentage());
        }
        variant.setIsActive(request.isActive());
        variant.setIsDefault(request.isDefault());

        ProductVariant savedVariant = productVariantRepository.save(variant);
        logger.info("Variant updated successfully with ID: {}", savedVariant.getVariantId());
        return convertToProductVariantDTO(savedVariant);
    }

    /**
     * Delete product variant
     */
    public void deleteProductVariant(Long variantId) {
        logger.info("Deleting variant with ID: {}", variantId);

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new BusinessException("VARIANT_NOT_FOUND", "Variant not found with ID: " + variantId));

        productVariantRepository.delete(variant);
        logger.info("Variant deleted successfully with ID: {}", variantId);
    }

    /**
     * Update variant stock (Product doesn't have stock at product level, only variants do)
     */
    public void updateVariantStock(Long variantId, int quantity) {
        logger.info("Updating stock for variant ID: {} to quantity: {}", variantId, quantity);

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new BusinessException("VARIANT_NOT_FOUND", "Variant not found with ID: " + variantId));

        variant.setStockQuantity(quantity);
        productVariantRepository.save(variant);

        logger.info("Variant stock updated successfully for ID: {}", variantId);
    }

    // Helper methods

    private ProductVariant createProductVariant(Long productId, ProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found with ID: " + productId));

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setName(request.getName());
        variant.setSku(request.getSku());
        variant.setPrice(request.getPrice());
        variant.setSalePrice(request.getSalePrice());
        variant.setStockQuantity(request.getStock() > 0 ? request.getStock() : 0);
        variant.setSize(request.getSize());
        variant.setWeight(request.getWeight());
        variant.setFlavor(request.getFlavor());
        variant.setImageUrl(request.getImageUrl());
        variant.setDiscountPercentage(request.getDiscountPercentage());
        variant.setIsActive(request.isActive());
        variant.setIsDefault(request.isDefault());
        variant.setCreatedAt(LocalDateTime.now());
        variant.setUpdatedAt(LocalDateTime.now());

        return productVariantRepository.save(variant);
    }

    private Specification<Product> createProductSpecification(ProductFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search by name or SKU
            if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
                String searchTerm = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchTerm);
                Predicate skuPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), searchTerm);
                predicates.add(criteriaBuilder.or(namePredicate, skuPredicate));
            }

            // Filter by active status
            if (filter.getStatus() != null && !filter.getStatus().trim().isEmpty()) {
                boolean isActive = "ACTIVE".equals(filter.getStatus());
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }

            // Filter by brand
            if (filter.getBrand() != null && !filter.getBrand().trim().isEmpty()) {
                try {
                    Brand brand = Brand.valueOf(filter.getBrand().toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("brand"), brand));
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid brand filter: {}", filter.getBrand());
                }
            }

            // Only show active products by default
            predicates.add(criteriaBuilder.equal(root.get("isActive"), true));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private ProductDetailDTO convertToProductDetailDTO(Product product) {
        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setId(product.getProductId());
        dto.setName(product.getName());
        dto.setSku(product.getSku());
        dto.setDescription(product.getDescription());
        dto.setShortDescription(product.getShortDescription());
        dto.setStatus(product.getIsActive() ? "ACTIVE" : "INACTIVE");
        dto.setMetaTitle(product.getMetaTitle());
        dto.setMetaDescription(product.getMetaDescription());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        // Get price range from variants
        if (product.hasVariants()) {
            BigDecimal minPrice = product.getMinPrice();
            BigDecimal maxPrice = product.getMaxPrice();
            dto.setPrice(minPrice);
        }

        // Get total stock from variants
        List<ProductVariant> variants = product.getVariants();
        if (variants != null && !variants.isEmpty()) {
            int totalStock = variants.stream()
                .mapToInt(ProductVariant::getStockQuantity)
                .sum();
            dto.setStock(totalStock);

            // Convert variants
            List<ProductVariantDTO> variantDTOs = variants.stream()
                    .map(this::convertToProductVariantDTO)
                    .collect(Collectors.toList());
            dto.setVariants(variantDTOs);
        }

        // Convert categories (through ProductCategory relationship)
        if (product.getProductCategories() != null) {
            List<ProductDetailDTO.CategoryDTO> categories = product.getProductCategories().stream()
                    .map(pc -> new ProductDetailDTO.CategoryDTO(pc.getCategory().getCategoryId(), pc.getCategory().getName(), pc.getCategory().getSlug()))
                    .collect(Collectors.toList());
            dto.setCategories(categories);
        }

        // Convert images
        if (product.getImages() != null) {
            List<ProductImageDTO> images = product.getImages().stream()
                    .map(img -> {
                        ProductImageDTO imageDTO = new ProductImageDTO();
                        imageDTO.setId(img.getImageId());
                        imageDTO.setUrl(img.getImageUrl());
                        imageDTO.setAltText(img.getAltText());
                        imageDTO.setSortOrder(img.getSortOrder());
                        imageDTO.setIsDefault(img.getIsPrimary() != null ? img.getIsPrimary() : false);
                        return imageDTO;
                    })
                    .collect(Collectors.toList());
            dto.setImages(images);
        }

        return dto;
    }

    private ProductListItemDTO convertToProductListItemDTO(Product product) {
        ProductListItemDTO dto = new ProductListItemDTO();
        dto.setId(product.getProductId());
        dto.setName(product.getName());
        dto.setSku(product.getSku());
        dto.setStatus(product.getIsActive() ? "ACTIVE" : "INACTIVE");
        dto.setHasVariants(product.hasVariants());

        // Get price from variants
        if (product.hasVariants()) {
            dto.setPrice(product.getMinPrice());
        }

        // Get total stock from variants
        List<ProductVariant> variants = product.getVariants();
        if (variants != null && !variants.isEmpty()) {
            int totalStock = variants.stream()
                .mapToInt(ProductVariant::getStockQuantity)
                .sum();
            // Remove setStock call as ProductListItemDTO doesn't have this method
            dto.setVariantCount(variants.size());
        }

        // Get main category
        if (product.getProductCategories() != null && !product.getProductCategories().isEmpty()) {
            dto.setMainCategory(product.getProductCategories().iterator().next().getCategory().getName());
        }

        // Get thumbnail URL
        ProductImage primaryImage = product.getPrimaryImage();
        if (primaryImage != null) {
            dto.setThumbnailUrl(primaryImage.getImageUrl());
        }

        return dto;
    }

    private ProductVariantDTO convertToProductVariantDTO(ProductVariant variant) {
        ProductVariantDTO dto = new ProductVariantDTO();
        dto.setId(variant.getVariantId());
        dto.setName(variant.getName());
        dto.setSku(variant.getSku());
        dto.setPrice(variant.getPrice());
        dto.setSalePrice(variant.getSalePrice());
        dto.setStockQuantity(variant.getStockQuantity());
        dto.setSize(variant.getSize());
        dto.setWeight(variant.getWeight() != null ? Double.parseDouble(variant.getWeight()) : null);
        dto.setFlavor(variant.getFlavor());
        dto.setImageUrl(variant.getImageUrl());
        dto.setDiscountPercentage(variant.getDiscountPercentage());
        dto.setActive(variant.getIsActive() != null ? variant.getIsActive() : true);
        dto.setIsDefault(variant.getIsDefault());
        return dto;
    }
}
