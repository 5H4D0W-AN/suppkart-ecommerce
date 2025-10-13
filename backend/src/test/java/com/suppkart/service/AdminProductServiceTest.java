package com.suppkart.service;

import com.suppkart.dto.admin.product.*;
import com.suppkart.exception.BusinessException;
import com.suppkart.model.entity.*;
import com.suppkart.model.enums.Brand;
import com.suppkart.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private ProductVariantRepository productVariantRepository;
    
    @Mock
    private ProductCategoryRepository productCategoryRepository;
    
    @Mock
    private ProductImageRepository productImageRepository;
    
    @Mock
    private FileUploadService fileUploadService;
    
    @Mock
    private SportRepository sportRepository;
    
    @Mock
    private GoalRepository goalRepository;

    @InjectMocks
    private AdminProductService adminProductService;

    private ProductCreateRequest createRequest;
    private Product product;
    private ProductVariant variant;
    private Category category;

    @BeforeEach
    void setUp() {
        // Setup test data
        createRequest = new ProductCreateRequest();
        createRequest.setName("Test Product");
        createRequest.setSku("TEST-001");
        createRequest.setDescription("Test Description");
        createRequest.setShortDescription("Short Description");
        createRequest.setStatus("ACTIVE");
        createRequest.setCategoryIds(Arrays.asList(1L));
        createRequest.setCodEligible(true);
        createRequest.setAutoGenerateSeo(true);
        
        // Setup variant
        ProductVariantRequest variantRequest = new ProductVariantRequest();
        variantRequest.setName("Test Variant");
        variantRequest.setSku("TEST-001-VAR");
        variantRequest.setPrice(new BigDecimal("99.99"));
        variantRequest.setStock(100);
        variantRequest.setDefault(true);
        variantRequest.setActive(true);
        createRequest.setVariants(Arrays.asList(variantRequest));
        
        // Setup entities
        product = new Product();
        product.setProductId(1L);
        product.setName("Test Product");
        product.setSku("TEST-001");
        product.setBrand(Brand.SUPPKART);
        product.setIsActive(true);
        product.setVariants(new ArrayList<>());
        product.setProductCategories(new ArrayList<>());
        
        variant = new ProductVariant();
        variant.setVariantId(1L);
        variant.setProduct(product);
        variant.setName("Test Variant");
        variant.setSku("TEST-001-VAR");
        variant.setPrice(new BigDecimal("99.99"));
        variant.setStockQuantity(100);
        variant.setIsDefault(true);
        variant.setIsActive(true);
        
        category = new Category();
        category.setCategoryId(1L);
        category.setName("Test Category");
        category.setSlug("test-category");
    }

    // ========== CREATE PRODUCT TESTS ==========
    
    @Test
    void createProduct_Success() {
        // Given
        lenient().when(productRepository.existsBySku("TEST-001")).thenReturn(false);
        lenient().when(productRepository.save(any(Product.class))).thenReturn(product);
        lenient().when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        lenient().when(productVariantRepository.existsBySku("TEST-001-VAR")).thenReturn(false);
        lenient().when(productVariantRepository.save(any(ProductVariant.class))).thenReturn(variant);
        lenient().when(productCategoryRepository.save(any(ProductCategory.class))).thenReturn(new ProductCategory());
        lenient().when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        
        // When
        ProductDetailDTO result = adminProductService.createProduct(createRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals("TEST-001", result.getSku());
        verify(productRepository).save(any(Product.class));
        verify(productVariantRepository).save(any(ProductVariant.class));
        verify(productCategoryRepository).save(any(ProductCategory.class));
    }
    
    @Test
    void createProduct_DuplicateSKU_ThrowsException() {
        // Given
        when(productRepository.existsBySku("TEST-001")).thenReturn(true);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminProductService.createProduct(createRequest));
        assertEquals("PRODUCT_SKU_EXISTS", exception.getErrorCode());
        assertEquals("Product with SKU TEST-001 already exists", exception.getMessage());
    }
    
    @Test
    void createProduct_NoVariants_ThrowsException() {
        // Given
        createRequest.setVariants(null);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminProductService.createProduct(createRequest));
        assertEquals("VARIANTS_REQUIRED", exception.getErrorCode());
        assertEquals("Product must have at least one variant", exception.getMessage());
    }
    
    @Test
    void createProduct_EmptyVariants_ThrowsException() {
        // Given
        createRequest.setVariants(new ArrayList<>());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminProductService.createProduct(createRequest));
        assertEquals("VARIANTS_REQUIRED", exception.getErrorCode());
    }
    
    @Test
    void createProduct_MultipleVariants_FirstSetAsDefault() {
        // Given
        ProductVariantRequest variant1 = new ProductVariantRequest();
        variant1.setName("Variant 1");
        variant1.setSku("TEST-001-VAR1");
        variant1.setPrice(new BigDecimal("99.99"));
        variant1.setStock(100);
        variant1.setActive(true);
        // Not setting default explicitly
        
        ProductVariantRequest variant2 = new ProductVariantRequest();
        variant2.setName("Variant 2");
        variant2.setSku("TEST-001-VAR2");
        variant2.setPrice(new BigDecimal("149.99"));
        variant2.setStock(50);
        variant2.setActive(true);
        
        createRequest.setVariants(Arrays.asList(variant1, variant2));
        
        lenient().when(productRepository.existsBySku("TEST-001")).thenReturn(false);
        lenient().when(productRepository.save(any(Product.class))).thenReturn(product);
        lenient().when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        lenient().when(productVariantRepository.existsBySku(anyString())).thenReturn(false);
        lenient().when(productVariantRepository.save(any(ProductVariant.class))).thenReturn(variant);
        lenient().when(productCategoryRepository.save(any(ProductCategory.class))).thenReturn(new ProductCategory());
        lenient().when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        
        // When
        ProductDetailDTO result = adminProductService.createProduct(createRequest);
        
        // Then
        assertNotNull(result);
        verify(productVariantRepository, times(2)).save(any(ProductVariant.class));
        // First variant should be set as default
        assertTrue(variant1.isDefault());
    }
    
    @Test
    void createProduct_MultipleDefaults_ThrowsException() {
        // Given
        ProductVariantRequest variant1 = new ProductVariantRequest();
        variant1.setName("Variant 1");
        variant1.setSku("TEST-001-VAR1");
        variant1.setPrice(new BigDecimal("99.99"));
        variant1.setStock(100);
        variant1.setDefault(true);
        variant1.setActive(true);
        
        ProductVariantRequest variant2 = new ProductVariantRequest();
        variant2.setName("Variant 2");
        variant2.setSku("TEST-001-VAR2");
        variant2.setPrice(new BigDecimal("149.99"));
        variant2.setStock(50);
        variant2.setDefault(true); // Second default - should fail
        variant2.setActive(true);
        
        createRequest.setVariants(Arrays.asList(variant1, variant2));
        
        lenient().when(productRepository.existsBySku("TEST-001")).thenReturn(false);
        lenient().when(productRepository.save(any(Product.class))).thenReturn(product);
        lenient().when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        lenient().when(productVariantRepository.existsBySku(anyString())).thenReturn(false);
        lenient().when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminProductService.createProduct(createRequest));
        assertEquals("MULTIPLE_DEFAULTS", exception.getErrorCode());
        assertEquals("Only one variant can be default", exception.getMessage());
    }
    
    @Test
    void createProduct_InvalidCategory_ThrowsException() {
        // Given
        lenient().when(productRepository.existsBySku("TEST-001")).thenReturn(false);
        lenient().when(productRepository.save(any(Product.class))).thenReturn(product);
        lenient().when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        lenient().when(productVariantRepository.existsBySku("TEST-001-VAR")).thenReturn(false);
        lenient().when(productVariantRepository.save(any(ProductVariant.class))).thenReturn(variant);
        lenient().when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminProductService.createProduct(createRequest));
        assertEquals("CATEGORY_NOT_FOUND", exception.getErrorCode());
        assertEquals("Category not found with ID: 1", exception.getMessage());
    }
    
    @Test
    void createProduct_DuplicateVariantSKU_ThrowsException() {
        // Given
        lenient().when(productRepository.existsBySku("TEST-001")).thenReturn(false);
        lenient().when(productRepository.save(any(Product.class))).thenReturn(product);
        lenient().when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        lenient().when(productVariantRepository.existsBySku("TEST-001-VAR")).thenReturn(true);
        lenient().when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminProductService.createProduct(createRequest));
        assertEquals("VARIANT_SKU_EXISTS", exception.getErrorCode());
        assertEquals("Variant with SKU TEST-001-VAR already exists", exception.getMessage());
    }

    // ========== UPDATE PRODUCT TESTS ==========
    
    @Test
    void updateProduct_Success() {
        // Given
        Long productId = 1L;
        createRequest.setSku("TEST-001-UPDATED");
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySku("TEST-001-UPDATED")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productCategoryRepository.save(any(ProductCategory.class))).thenReturn(new ProductCategory());
        
        // When
        ProductDetailDTO result = adminProductService.updateProduct(productId, createRequest);
        
        // Then
        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
        verify(productCategoryRepository).deleteByProductId(productId);
        verify(productCategoryRepository).save(any(ProductCategory.class));
    }
    
    @Test
    void updateProduct_NotFound_ThrowsException() {
        // Given
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminProductService.updateProduct(productId, createRequest));
        assertEquals("PRODUCT_NOT_FOUND", exception.getErrorCode());
        assertEquals("Product not found with ID: 999", exception.getMessage());
    }
    
    @Test
    void updateProduct_DuplicateSKU_ThrowsException() {
        // Given
        Long productId = 1L;
        createRequest.setSku("EXISTING-SKU");
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySku("EXISTING-SKU")).thenReturn(true);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminProductService.updateProduct(productId, createRequest));
        assertEquals("PRODUCT_SKU_EXISTS", exception.getErrorCode());
        assertEquals("Product with SKU EXISTING-SKU already exists", exception.getMessage());
    }

    // ========== VARIANT MANAGEMENT TESTS ==========
    
    @Test
    void addProductVariant_Success() {
        // Given
        Long productId = 1L;
        ProductVariantRequest variantRequest = new ProductVariantRequest();
        variantRequest.setName("New Variant");
        variantRequest.setSku("TEST-001-NEW");
        variantRequest.setPrice(new BigDecimal("199.99"));
        variantRequest.setStock(50);
        variantRequest.setActive(true);
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productVariantRepository.existsBySku("TEST-001-NEW")).thenReturn(false);
        when(productVariantRepository.save(any(ProductVariant.class))).thenReturn(variant);
        
        // When
        ProductVariantDTO result = adminProductService.addProductVariant(productId, variantRequest);
        
        // Then
        assertNotNull(result);
        verify(productVariantRepository).save(any(ProductVariant.class));
    }
    
    @Test
    void updateProductVariant_Success() {
        // Given
        Long variantId = 1L;
        ProductVariantRequest updateRequest = new ProductVariantRequest();
        updateRequest.setName("Updated Variant");
        updateRequest.setSku("TEST-001-UPDATED");
        updateRequest.setPrice(new BigDecimal("299.99"));
        updateRequest.setStock(75);
        updateRequest.setActive(true);
        
        when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));
        when(productVariantRepository.existsBySku("TEST-001-UPDATED")).thenReturn(false);
        when(productVariantRepository.save(any(ProductVariant.class))).thenReturn(variant);
        
        // When
        ProductVariantDTO result = adminProductService.updateProductVariant(variantId, updateRequest);
        
        // Then
        assertNotNull(result);
        verify(productVariantRepository).save(any(ProductVariant.class));
    }
    
    @Test
    void updateProductVariant_ExcessiveDiscount_ThrowsException() {
        // Given
        Long variantId = 1L;
        ProductVariantRequest updateRequest = new ProductVariantRequest();
        updateRequest.setName("Updated Variant");
        updateRequest.setSku("TEST-001-UPDATED");
        updateRequest.setPrice(new BigDecimal("299.99"));
        updateRequest.setStock(75);
        updateRequest.setDiscountPercentage(new BigDecimal("50.0")); // Over 40% limit
        updateRequest.setActive(true);
        
        when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));
        when(productVariantRepository.existsBySku("TEST-001-UPDATED")).thenReturn(false);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminProductService.updateProductVariant(variantId, updateRequest));
        assertEquals("DISCOUNT_LIMIT_EXCEEDED", exception.getErrorCode());
        assertEquals("Discount cannot exceed 40%", exception.getMessage());
    }
    
    @Test
    void deleteProductVariant_Success() {
        // Given
        Long variantId = 1L;
        ProductVariant anotherVariant = new ProductVariant();
        anotherVariant.setVariantId(2L);
        anotherVariant.setProduct(product);
        
        when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));
        when(productVariantRepository.countByProductId(product.getProductId())).thenReturn(2L);
        when(productVariantRepository.findByProduct(product)).thenReturn(Arrays.asList(variant, anotherVariant));
        when(productImageRepository.findByVariant_VariantIdOrderBySortOrder(variantId)).thenReturn(new ArrayList<>());
        
        // When
        adminProductService.deleteProductVariant(variantId);
        
        // Then
        verify(productVariantRepository).delete(variant);
    }
    
    @Test
    void deleteProductVariant_LastVariant_ThrowsException() {
        // Given
        Long variantId = 1L;
        when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));
        when(productVariantRepository.countByProductId(product.getProductId())).thenReturn(1L);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminProductService.deleteProductVariant(variantId));
        assertEquals("LAST_VARIANT", exception.getErrorCode());
        assertEquals("Cannot delete the last variant. Product must have at least one variant.", exception.getMessage());
    }
    
    @Test
    void deleteProductVariant_DefaultVariant_SetsNewDefault() {
        // Given
        Long variantId = 1L;
        variant.setIsDefault(true);
        
        ProductVariant anotherVariant = new ProductVariant();
        anotherVariant.setVariantId(2L);
        anotherVariant.setProduct(product);
        anotherVariant.setIsDefault(false);
        
        when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));
        when(productVariantRepository.countByProductId(product.getProductId())).thenReturn(2L);
        when(productVariantRepository.findByProduct(product)).thenReturn(Arrays.asList(variant, anotherVariant));
        when(productImageRepository.findByVariant_VariantIdOrderBySortOrder(variantId)).thenReturn(new ArrayList<>());
        
        // When
        adminProductService.deleteProductVariant(variantId);
        
        // Then
        verify(productVariantRepository).save(anotherVariant);
        assertTrue(anotherVariant.getIsDefault());
        verify(productVariantRepository).delete(variant);
    }

    // ========== IMAGE MANAGEMENT TESTS ==========
    
    @Test
    void uploadVariantImages_Success() {
        // Given
        Long variantId = 1L;
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        List<MultipartFile> files = Arrays.asList(file1, file2);
        List<String> altTexts = Arrays.asList("Alt 1", "Alt 2");
        
        lenient().when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));
        lenient().when(productImageRepository.countByVariantId(variantId)).thenReturn(0L);
        lenient().when(fileUploadService.uploadFile(any(MultipartFile.class), anyString())).thenReturn("http://example.com/image.jpg");
        lenient().when(fileUploadService.getMediaType(any())).thenReturn("IMAGE");
        lenient().when(productImageRepository.save(any(ProductImage.class))).thenReturn(new ProductImage());
        
        // When
        List<ProductImageDTO> result = adminProductService.uploadVariantImages(variantId, files, altTexts);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(fileUploadService, times(2)).uploadFile(any(MultipartFile.class), eq("variants/" + variantId));
        verify(productImageRepository, times(2)).save(any(ProductImage.class));
    }
    
    @Test
    void uploadVariantImages_TooManyImages_ThrowsException() {
        // Given
        Long variantId = 1L;
        List<MultipartFile> files = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            files.add(mock(MultipartFile.class));
        }
        
        when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));
        when(productImageRepository.countByVariantId(variantId)).thenReturn(8L); // Already 8 images
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminProductService.uploadVariantImages(variantId, files, null));
        assertEquals("TOO_MANY_IMAGES", exception.getErrorCode());
        assertEquals("Maximum 10 images allowed per variant", exception.getMessage());
    }

    // ========== SEARCH AND FILTERING TESTS ==========
    
    @Test
    void getAllProducts_WithFilters_Success() {
        // Given
        ProductFilterRequest filter = new ProductFilterRequest();
        filter.setSearch("test");
        filter.setStatus("ACTIVE");
        filter.setBrand("SUPPKART");
        
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        
        // When
        Page<ProductListItemDTO> result = adminProductService.getAllProducts(filter, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }
    
    @Test
    void getProductById_Success() {
        // Given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        
        // When
        ProductDetailDTO result = adminProductService.getProductById(productId);
        
        // Then
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals("TEST-001", result.getSku());
    }
    
    @Test
    void getProductById_NotFound_ThrowsException() {
        // Given
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminProductService.getProductById(productId));
        assertEquals("PRODUCT_NOT_FOUND", exception.getErrorCode());
        assertEquals("Product not found with ID: 999", exception.getMessage());
    }

    // ========== STATUS MANAGEMENT TESTS ==========
    
    @Test
    void changeProductStatus_Success() {
        // Given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        
        // When
        adminProductService.changeProductStatus(productId, "INACTIVE");
        
        // Then
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    void deleteProduct_SoftDelete_Success() {
        // Given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        
        // When
        adminProductService.deleteProduct(productId);
        
        // Then
        verify(productRepository).save(any(Product.class));
        assertFalse(product.getIsActive());
    }
    
    @Test
    void updateVariantStock_Success() {
        // Given
        Long variantId = 1L;
        int newQuantity = 200;
        
        when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));
        when(productVariantRepository.save(any(ProductVariant.class))).thenReturn(variant);
        
        // When
        adminProductService.updateVariantStock(variantId, newQuantity);
        
        // Then
        verify(productVariantRepository).save(variant);
        assertEquals(newQuantity, variant.getStockQuantity());
    }
}