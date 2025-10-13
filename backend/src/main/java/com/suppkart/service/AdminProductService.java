package com.suppkart.service;

import com.suppkart.dto.admin.product.*;
import com.suppkart.exception.BusinessException;
import com.suppkart.model.entity.*;
import com.suppkart.model.enums.Brand;
import com.suppkart.repository.*;
import com.suppkart.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final ProductImageRepository productImageRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final FileUploadService fileUploadService;
    private final SportRepository sportRepository;
    private final GoalRepository goalRepository;

    public AdminProductService(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              ProductVariantRepository productVariantRepository,
                              ProductImageRepository productImageRepository,
                              ProductCategoryRepository productCategoryRepository,
                              FileUploadService fileUploadService,
                              SportRepository sportRepository,
                              GoalRepository goalRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productVariantRepository = productVariantRepository;
        this.productImageRepository = productImageRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.fileUploadService = fileUploadService;
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
        product.setCodEligible(request.getCodEligible() != null ? request.getCodEligible() : true);
        product.setAutoGenerateSeo(request.getAutoGenerateSeo() != null ? request.getAutoGenerateSeo() : true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        // Validate that at least one variant is provided
        if (request.getVariants() == null || request.getVariants().isEmpty()) {
            throw new BusinessException("VARIANTS_REQUIRED", "Product must have at least one variant");
        }
        
        // Create variants (at least one is guaranteed)
        boolean hasDefault = false;
        for (int i = 0; i < request.getVariants().size(); i++) {
            ProductVariantRequest variantRequest = request.getVariants().get(i);
            
            // Set first variant as default if no default specified
            if (i == 0 && !hasDefault) {
                variantRequest.setDefault(true);
            }
            
            if (variantRequest.isDefault()) {
                if (hasDefault) {
                    throw new BusinessException("MULTIPLE_DEFAULTS", "Only one variant can be default");
                }
                hasDefault = true;
            }
            
            createProductVariant(savedProduct.getProductId(), variantRequest);
        }

        // Handle categories through ProductCategory relationship
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            for (Long categoryId : request.getCategoryIds()) {
                Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "Category not found with ID: " + categoryId));
                
                ProductCategory productCategory = new ProductCategory();
                productCategory.setProduct(savedProduct);
                productCategory.setCategory(category);
                productCategoryRepository.save(productCategory);
            }
        }

        logger.info("Product created successfully with ID: {}", savedProduct.getProductId());
        return convertToProductDetailDTO(savedProduct);
    }

    /**
     * Update existing product
     */
    public ProductDetailDTO updateProduct(Long id, ProductCreateRequest request) {
        logger.info("Updating product with ID: {}", id);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found with ID: " + id));

        // Check if SKU is being changed and if it already exists
        if (!request.getSku().equals(existingProduct.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new BusinessException("PRODUCT_SKU_EXISTS", "Product with SKU " + request.getSku() + " already exists");
        }

        // Update all product fields
        existingProduct.setName(request.getName());
        existingProduct.setSku(request.getSku());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setShortDescription(request.getShortDescription());
        existingProduct.setIsActive("ACTIVE".equals(request.getStatus()));
        existingProduct.setMetaTitle(request.getMetaTitle());
        existingProduct.setMetaDescription(request.getMetaDescription());
        existingProduct.setCodEligible(request.getCodEligible() != null ? request.getCodEligible() : true);
        existingProduct.setAutoGenerateSeo(request.getAutoGenerateSeo() != null ? request.getAutoGenerateSeo() : true);
        existingProduct.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(existingProduct);

        // Update categories - remove existing and add new ones
        productCategoryRepository.deleteByProductId(id);
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            for (Long categoryId : request.getCategoryIds()) {
                Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "Category not found with ID: " + categoryId));
                
                ProductCategory productCategory = new ProductCategory();
                productCategory.setProduct(savedProduct);
                productCategory.setCategory(category);
                productCategoryRepository.save(productCategory);
            }
        }

        // Update variants - this will be handled separately through variant endpoints
        // since variants have their own lifecycle management

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

        ProductVariant existingVariant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new BusinessException("VARIANT_NOT_FOUND", "Variant not found with ID: " + variantId));

        // Check if SKU is being changed and if it already exists
        if (!request.getSku().equals(existingVariant.getSku()) && productVariantRepository.existsBySku(request.getSku())) {
            throw new BusinessException("VARIANT_SKU_EXISTS", "Variant with SKU " + request.getSku() + " already exists");
        }

        // Validate discount percentage
        validateDiscountPercentage(request.getDiscountPercentage());

        // Update all variant fields
        existingVariant.setName(request.getName());
        existingVariant.setSku(request.getSku());
        existingVariant.setPrice(request.getPrice());
        existingVariant.setSalePrice(request.getSalePrice());
        existingVariant.setStockQuantity(request.getStock());
        existingVariant.setSize(request.getSize());
        existingVariant.setWeight(request.getWeight());
        existingVariant.setFlavor(request.getFlavor());
        existingVariant.setImageUrl(request.getImageUrl());
        existingVariant.setBarcode(request.getBarcode());
        existingVariant.setDiscountPercentage(request.getDiscountPercentage());
        existingVariant.setDiscountStartDate(request.getDiscountStartDate());
        existingVariant.setDiscountEndDate(request.getDiscountEndDate());
        existingVariant.setDiscountReason(request.getDiscountReason());
        existingVariant.setCodEligible(request.getCodEligible());
        existingVariant.setMetaTitle(request.getMetaTitle());
        existingVariant.setMetaDescription(request.getMetaDescription());
        existingVariant.setMetaKeywords(request.getMetaKeywords());
        existingVariant.setIsActive(request.isActive());
        existingVariant.setIsDefault(request.isDefault());
        existingVariant.setUpdatedAt(LocalDateTime.now());

        ProductVariant savedVariant = productVariantRepository.save(existingVariant);
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

        // Check if this is the last variant
        long variantCount = productVariantRepository.countByProductId(variant.getProduct().getProductId());
        if (variantCount <= 1) {
            throw new BusinessException("LAST_VARIANT", "Cannot delete the last variant. Product must have at least one variant.");
        }
        
        // If deleting default variant, set another as default
        if (variant.getIsDefault()) {
            List<ProductVariant> otherVariants = productVariantRepository.findByProduct(variant.getProduct());
            otherVariants.stream()
                .filter(v -> !v.getVariantId().equals(variantId))
                .findFirst()
                .ifPresent(v -> {
                    v.setIsDefault(true);
                    productVariantRepository.save(v);
                });
        }
        
        // Delete variant images from storage
        List<ProductImage> variantImages = productImageRepository.findByVariant_VariantIdOrderBySortOrder(variantId);
        for (ProductImage image : variantImages) {
            fileUploadService.deleteFile(image.getImageUrl());
        }
        
        // Delete variant (images will be deleted by cascade)
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
    
    /**
     * Upload images for a variant
     */
    public List<ProductImageDTO> uploadVariantImages(Long variantId, List<MultipartFile> files, List<String> altTexts) {
        logger.info("Uploading {} images for variant ID: {}", files.size(), variantId);
        
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new BusinessException("VARIANT_NOT_FOUND", "Variant not found with ID: " + variantId));
        
        // Check current image count
        long currentImageCount = productImageRepository.countByVariantId(variantId);
        if (currentImageCount + files.size() > 10) {
            throw new BusinessException("TOO_MANY_IMAGES", "Maximum 10 images allowed per variant");
        }
        
        List<ProductImageDTO> uploadedImages = new ArrayList<>();
        
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String altText = (altTexts != null && i < altTexts.size()) ? altTexts.get(i) : null;
            
            // Upload file
            String imageUrl = fileUploadService.uploadFile(file, "variants/" + variantId);
            String mediaType = fileUploadService.getMediaType(file.getContentType());
            
            // Create ProductImage entity
            ProductImage image = new ProductImage();
            image.setVariant(variant);
            image.setImageUrl(imageUrl);
            image.setAltText(altText);
            image.setMediaType(mediaType);
            image.setSortOrder((int) (currentImageCount + i));
            image.setIsPrimary(currentImageCount == 0 && i == 0); // First image is primary if no images exist
            image.setCreatedAt(LocalDateTime.now());
            
            ProductImage savedImage = productImageRepository.save(image);
            uploadedImages.add(convertToProductImageDTO(savedImage));
        }
        
        logger.info("Successfully uploaded {} images for variant ID: {}", files.size(), variantId);
        return uploadedImages;
    }
    
    /**
     * Reorder variant images
     */
    public void reorderVariantImages(Long variantId, List<ImageOrderRequest> imageOrder) {
        logger.info("Reordering images for variant ID: {}", variantId);
        
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new BusinessException("VARIANT_NOT_FOUND", "Variant not found with ID: " + variantId));
        
        // Reset all primary flags first
        List<ProductImage> variantImages = productImageRepository.findByVariant_VariantIdOrderBySortOrder(variantId);
        variantImages.forEach(img -> img.setIsPrimary(false));
        
        // Update order and primary flag
        for (ImageOrderRequest orderRequest : imageOrder) {
            ProductImage image = productImageRepository.findById(orderRequest.getImageId())
                    .orElseThrow(() -> new BusinessException("IMAGE_NOT_FOUND", "Image not found with ID: " + orderRequest.getImageId()));
            
            if (!image.getVariant().getVariantId().equals(variantId)) {
                throw new BusinessException("IMAGE_VARIANT_MISMATCH", "Image does not belong to the specified variant");
            }
            
            image.setSortOrder(orderRequest.getSortOrder());
            if (orderRequest.getIsPrimary() != null && orderRequest.getIsPrimary()) {
                image.setIsPrimary(true);
            }
            
            productImageRepository.save(image);
        }
        
        logger.info("Successfully reordered images for variant ID: {}", variantId);
    }
    
    /**
     * Delete an image
     */
    public void deleteImage(Long imageId) {
        logger.info("Deleting image with ID: {}", imageId);
        
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException("IMAGE_NOT_FOUND", "Image not found with ID: " + imageId));
        
        // Delete file from storage
        fileUploadService.deleteFile(image.getImageUrl());
        
        // If this was the primary image, set another as primary
        if (image.getIsPrimary() && image.getVariant() != null) {
            List<ProductImage> otherImages = productImageRepository.findByVariant_VariantIdOrderBySortOrder(image.getVariant().getVariantId());
            otherImages.stream()
                    .filter(img -> !img.getImageId().equals(imageId))
                    .findFirst()
                    .ifPresent(img -> {
                        img.setIsPrimary(true);
                        productImageRepository.save(img);
                    });
        }
        
        productImageRepository.delete(image);
        logger.info("Successfully deleted image with ID: {}", imageId);
    }

    // Helper methods
    
    private void createVariantImages(ProductVariant variant, List<ProductImageRequest> imageRequests) {
        for (int i = 0; i < imageRequests.size(); i++) {
            ProductImageRequest imageRequest = imageRequests.get(i);
            
            ProductImage image = new ProductImage();
            image.setVariant(variant);
            image.setImageUrl(imageRequest.getImageUrl());
            image.setAltText(imageRequest.getAltText());
            image.setSortOrder(imageRequest.getSortOrder() != null ? imageRequest.getSortOrder() : i);
            image.setIsPrimary(imageRequest.getIsPrimary() != null ? imageRequest.getIsPrimary() : (i == 0));
            image.setMediaType(imageRequest.getMediaType());
            image.setCreatedAt(LocalDateTime.now());
            
            productImageRepository.save(image);
        }
    }
    
    private void validateDiscountPercentage(BigDecimal discountPercentage) {
        if (discountPercentage != null && discountPercentage.compareTo(new BigDecimal("40.0")) > 0) {
            throw new BusinessException("DISCOUNT_LIMIT_EXCEEDED", "Discount cannot exceed 40%");
        }
    }

    private ProductVariant createProductVariant(Long productId, ProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found with ID: " + productId));

        // Check if variant SKU already exists
        if (productVariantRepository.existsBySku(request.getSku())) {
            throw new BusinessException("VARIANT_SKU_EXISTS", "Variant with SKU " + request.getSku() + " already exists");
        }

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
        variant.setImageUrl(request.getImageUrl()); // Keep for backward compatibility
        variant.setBarcode(request.getBarcode());
        variant.setDiscountPercentage(request.getDiscountPercentage());
        variant.setDiscountStartDate(request.getDiscountStartDate());
        variant.setDiscountEndDate(request.getDiscountEndDate());
        variant.setDiscountReason(request.getDiscountReason());
        variant.setCodEligible(request.getCodEligible());
        variant.setMetaTitle(request.getMetaTitle());
        variant.setMetaDescription(request.getMetaDescription());
        variant.setMetaKeywords(request.getMetaKeywords());
        variant.setIsActive(request.isActive());
        variant.setIsDefault(request.isDefault());
        variant.setCreatedAt(LocalDateTime.now());
        variant.setUpdatedAt(LocalDateTime.now());

        // Validate discount percentage
        validateDiscountPercentage(request.getDiscountPercentage());

        ProductVariant savedVariant = productVariantRepository.save(variant);
        
        // Handle variant images
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            createVariantImages(savedVariant, request.getImages());
        }

        return savedVariant;
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

        // Price is now at variant level only

        // Convert variants - stock is now at variant level only
        List<ProductVariant> variants = product.getVariants();
        if (variants != null && !variants.isEmpty()) {
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

        // Price and stock are now at variant level only
        List<ProductVariant> variants = product.getVariants();
        if (variants != null && !variants.isEmpty()) {
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
        dto.setImageUrl(variant.getImageUrl()); // Keep for backward compatibility
        dto.setBarcode(variant.getBarcode());
        dto.setDiscountPercentage(variant.getDiscountPercentage());
        dto.setDiscountStartDate(variant.getDiscountStartDate());
        dto.setDiscountEndDate(variant.getDiscountEndDate());
        dto.setDiscountReason(variant.getDiscountReason());
        dto.setCodEligible(variant.getCodEligible());
        dto.setMetaTitle(variant.getMetaTitle());
        dto.setMetaDescription(variant.getMetaDescription());
        dto.setMetaKeywords(variant.getMetaKeywords());
        dto.setActive(variant.getIsActive() != null ? variant.getIsActive() : true);
        dto.setIsDefault(variant.getIsDefault());
        
        // Convert variant images
        List<ProductImage> variantImages = productImageRepository.findByVariantOrderBySortOrder(variant);
        if (!variantImages.isEmpty()) {
            List<ProductImageDTO> imageDTOs = variantImages.stream()
                    .map(this::convertToProductImageDTO)
                    .collect(Collectors.toList());
            dto.setImages(imageDTOs);
        }
        
        return dto;
    }
    
    private ProductImageDTO convertToProductImageDTO(ProductImage image) {
        ProductImageDTO dto = new ProductImageDTO();
        dto.setId(image.getImageId());
        dto.setUrl(image.getImageUrl());
        dto.setAltText(image.getAltText());
        dto.setSortOrder(image.getSortOrder());
        dto.setIsDefault(image.getIsPrimary());
        dto.setMediaType(image.getMediaType());
        dto.setVariantId(image.getVariant() != null ? image.getVariant().getVariantId() : null);
        dto.setProductId(image.getProduct() != null ? image.getProduct().getProductId() : null);
        return dto;
    }
}
