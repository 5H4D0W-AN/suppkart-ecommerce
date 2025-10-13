package com.suppkart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suppkart.dto.admin.product.*;
import com.suppkart.exception.BusinessException;
import com.suppkart.service.AdminProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(properties = {
    "spring.servlet.multipart.enabled=true",
    "spring.servlet.multipart.max-file-size=10MB",
    "spring.servlet.multipart.max-request-size=10MB"
})
@AutoConfigureMockMvc
class AdminProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminProductService adminProductService;



    @Autowired
    private ObjectMapper objectMapper;

    private ProductCreateRequest createRequest;
    private ProductDetailDTO productDetailDTO;
    private ProductListItemDTO productListItemDTO;
    private ProductVariantDTO variantDTO;
    private ProductImageDTO imageDTO;

    @BeforeEach
    void setUp() {
        // Setup create request
        createRequest = new ProductCreateRequest();
        createRequest.setName("Test Product");
        createRequest.setSku("TEST-001");
        createRequest.setDescription("Test Description");
        createRequest.setShortDescription("Short Description");
        createRequest.setStatus("ACTIVE");
        createRequest.setCategoryIds(Arrays.asList(1L));
        createRequest.setCodEligible(true);
        createRequest.setAutoGenerateSeo(true);

        ProductVariantRequest variantRequest = new ProductVariantRequest();
        variantRequest.setName("Test Variant");
        variantRequest.setSku("TEST-001-VAR");
        variantRequest.setPrice(new BigDecimal("99.99"));
        variantRequest.setStock(100);
        variantRequest.setDefault(true);
        variantRequest.setActive(true);
        createRequest.setVariants(Arrays.asList(variantRequest));

        // Setup DTOs
        productDetailDTO = new ProductDetailDTO();
        productDetailDTO.setId(1L);
        productDetailDTO.setName("Test Product");
        productDetailDTO.setSku("TEST-001");
        productDetailDTO.setDescription("Test Description");
        productDetailDTO.setStatus("ACTIVE");
        productDetailDTO.setCreatedAt(LocalDateTime.now());
        productDetailDTO.setUpdatedAt(LocalDateTime.now());

        productListItemDTO = new ProductListItemDTO();
        productListItemDTO.setId(1L);
        productListItemDTO.setName("Test Product");
        productListItemDTO.setSku("TEST-001");
        productListItemDTO.setStatus("ACTIVE");
        productListItemDTO.setVariantCount(1);

        variantDTO = new ProductVariantDTO();
        variantDTO.setId(1L);
        variantDTO.setName("Test Variant");
        variantDTO.setSku("TEST-001-VAR");
        variantDTO.setPrice(new BigDecimal("99.99"));
        variantDTO.setStockQuantity(100);
        variantDTO.setIsDefault(true);
        variantDTO.setActive(true);

        imageDTO = new ProductImageDTO();
        imageDTO.setId(1L);
        imageDTO.setUrl("http://example.com/image.jpg");
        imageDTO.setAltText("Test Image");
        imageDTO.setSortOrder(0);
        imageDTO.setIsDefault(true);
    }

    // ========== PRODUCT CRUD TESTS ==========
    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_Success() throws Exception {
        // Given
        when(adminProductService.createProduct(any(ProductCreateRequest.class))).thenReturn(productDetailDTO);

        // When & Then
        mockMvc.perform(post("/api/admin/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.name", is("Test Product")))
                .andExpect(jsonPath("$.data.sku", is("TEST-001")));

        verify(adminProductService).createProduct(any(ProductCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_ValidationError_ReturnsBadRequest() throws Exception {
        // Given - Invalid request (missing name)
        createRequest.setName(null);

        // When & Then
        mockMvc.perform(post("/api/admin/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_BusinessException_ReturnsBadRequest() throws Exception {
        // Given
        when(adminProductService.createProduct(any(ProductCreateRequest.class)))
                .thenThrow(new BusinessException("PRODUCT_SKU_EXISTS", "Product with SKU already exists"));

        // When & Then
        mockMvc.perform(post("/api/admin/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("PRODUCT_SKU_EXISTS")))
                .andExpect(jsonPath("$.error.message", is("Product with SKU already exists")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_Success() throws Exception {
        // Given
        Long productId = 1L;
        when(adminProductService.updateProduct(eq(productId), any(ProductCreateRequest.class)))
                .thenReturn(productDetailDTO);

        // When & Then
        mockMvc.perform(put("/api/admin/products/{id}", productId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)));

        verify(adminProductService).updateProduct(eq(productId), any(ProductCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getProduct_Success() throws Exception {
        // Given
        Long productId = 1L;
        when(adminProductService.getProductById(productId)).thenReturn(productDetailDTO);

        // When & Then
        mockMvc.perform(get("/api/admin/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.name", is("Test Product")));

        verify(adminProductService).getProductById(productId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getProduct_NotFound_ReturnsNotFound() throws Exception {
        // Given
        Long productId = 999L;
        when(adminProductService.getProductById(productId))
                .thenThrow(new BusinessException("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND));

        // When & Then
        mockMvc.perform(get("/api/admin/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("PRODUCT_NOT_FOUND")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllProducts_Success() throws Exception {
        // Given
        List<ProductListItemDTO> products = Arrays.asList(productListItemDTO);
        Page<ProductListItemDTO> productPage = new PageImpl<>(products, PageRequest.of(0, 10), 1);

        when(adminProductService.getAllProducts(any(ProductFilterRequest.class), any()))
                .thenReturn(productPage);

        // When & Then
        mockMvc.perform(get("/api/admin/products")
                .param("page", "0")
                .param("size", "10")
                .param("search", "test")
                .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name", is("Test Product")))
                .andExpect(jsonPath("$.data.totalElements", is(1)));

        verify(adminProductService).getAllProducts(any(ProductFilterRequest.class), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_Success() throws Exception {
        // Given
        Long productId = 1L;
        doNothing().when(adminProductService).deleteProduct(productId);

        // When & Then
        mockMvc.perform(delete("/api/admin/products/{id}", productId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Product deleted successfully")));

        verify(adminProductService).deleteProduct(productId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeProductStatus_Success() throws Exception {
        // Given
        Long productId = 1L;
        String status = "INACTIVE";
        doNothing().when(adminProductService).changeProductStatus(productId, status);

        // When & Then
        mockMvc.perform(patch("/api/admin/products/{id}/status", productId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"" + status + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Product status updated successfully")));

        verify(adminProductService).changeProductStatus(productId, status);
    }

    // ========== VARIANT MANAGEMENT TESTS ==========
    @Test
    @WithMockUser(roles = "ADMIN")
    void addProductVariant_Success() throws Exception {
        // Given
        Long productId = 1L;
        ProductVariantRequest variantRequest = new ProductVariantRequest();
        variantRequest.setName("New Variant");
        variantRequest.setSku("TEST-001-NEW");
        variantRequest.setPrice(new BigDecimal("199.99"));
        variantRequest.setStock(50);
        variantRequest.setActive(true);

        when(adminProductService.addProductVariant(eq(productId), any(ProductVariantRequest.class)))
                .thenReturn(variantDTO);

        // When & Then
        mockMvc.perform(post("/api/admin/products/{productId}/variants", productId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(variantRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.name", is("Test Variant")));

        verify(adminProductService).addProductVariant(eq(productId), any(ProductVariantRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProductVariant_Success() throws Exception {
        // Given
        Long variantId = 1L;
        ProductVariantRequest updateRequest = new ProductVariantRequest();
        updateRequest.setName("Updated Variant");
        updateRequest.setSku("TEST-001-UPDATED");
        updateRequest.setPrice(new BigDecimal("299.99"));
        updateRequest.setStock(75);
        updateRequest.setActive(true);

        when(adminProductService.updateProductVariant(eq(variantId), any(ProductVariantRequest.class)))
                .thenReturn(variantDTO);

        // When & Then
        mockMvc.perform(put("/api/admin/products/variants/{variantId}", variantId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)));

        verify(adminProductService).updateProductVariant(eq(variantId), any(ProductVariantRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProductVariant_Success() throws Exception {
        // Given
        Long variantId = 1L;
        doNothing().when(adminProductService).deleteProductVariant(variantId);

        // When & Then
        mockMvc.perform(delete("/api/admin/products/variants/{variantId}", variantId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Variant deleted successfully")));

        verify(adminProductService).deleteProductVariant(variantId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProductVariant_LastVariant_ReturnsBadRequest() throws Exception {
        // Given
        Long variantId = 1L;
        doThrow(new BusinessException("LAST_VARIANT", "Cannot delete the last variant"))
                .when(adminProductService).deleteProductVariant(variantId);

        // When & Then
        mockMvc.perform(delete("/api/admin/products/variants/{variantId}", variantId)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("LAST_VARIANT")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateVariantStock_Success() throws Exception {
        // Given
        Long variantId = 1L;
        int quantity = 200;
        doNothing().when(adminProductService).updateVariantStock(variantId, quantity);

        // When & Then
        mockMvc.perform(patch("/api/admin/products/variants/{variantId}/stock", variantId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":" + quantity + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Stock updated successfully")));

        verify(adminProductService).updateVariantStock(variantId, quantity);
    }

    // ========== IMAGE MANAGEMENT TESTS ==========
    // TODO: Implement uploadVariantImages in AdminProductService before enabling this test
    // @Test
    // @WithMockUser(roles = "ADMIN")
    // void uploadVariantImages_Success() throws Exception {
    //     // Given
    //     Long variantId = 1L;
    //     MockMultipartFile file1 = new MockMultipartFile("files", "image1.jpg", "image/jpeg", "image1".getBytes());
    //     MockMultipartFile file2 = new MockMultipartFile("files", "image2.jpg", "image/jpeg", "image2".getBytes());

    //     List<ProductImageDTO> uploadedImages = Arrays.asList(imageDTO);
    //     when(adminProductService.uploadVariantImages(eq(variantId), anyList(), anyList()))
    //             .thenReturn(uploadedImages);

    //     // When & Then
    //     mockMvc.perform(multipart("/api/admin/products/variants/{variantId}/images", variantId)
    //             .file(file1)
    //             .file(file2)
    //             .param("altTexts", "Alt 1", "Alt 2")
    //             .with(csrf()))
    //             .andExpect(status().isCreated())
    //             .andExpect(jsonPath("$.success", is(true)))
    //             .andExpect(jsonPath("$.data", hasSize(1)))
    //             .andExpect(jsonPath("$.data[0].url", is("http://example.com/image.jpg")));

    //     verify(adminProductService).uploadVariantImages(eq(variantId), anyList(), anyList());
    // }

    // TODO: Implement uploadVariantImages in AdminProductService before enabling this test
    // @Test
    // @WithMockUser(roles = "ADMIN")
    // void uploadVariantImages_TooManyImages_ReturnsBadRequest() throws Exception {
    //     // Given
    //     Long variantId = 1L;
    //     MockMultipartFile file = new MockMultipartFile("files", "image.jpg", "image/jpeg", "image".getBytes());

    //     when(adminProductService.uploadVariantImages(eq(variantId), anyList(), anyList()))
    //             .thenThrow(new BusinessException("TOO_MANY_IMAGES", "Maximum 10 images allowed per variant"));

    //     // When & Then
    //     mockMvc.perform(multipart("/api/admin/products/variants/{variantId}/images", variantId)
    //             .file(file)
    //             .with(csrf()))
    //             .andExpect(status().isBadRequest())
    //             .andExpect(jsonPath("$.success", is(false)))
    //             .andExpect(jsonPath("$.error.code", is("TOO_MANY_IMAGES")));
    // }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reorderVariantImages_Success() throws Exception {
        // Given
        Long variantId = 1L;
        List<ImageOrderRequest> imageOrder = Arrays.asList(
                new ImageOrderRequest(1L, 0, true),
                new ImageOrderRequest(2L, 1, false)
        );

        doNothing().when(adminProductService).reorderVariantImages(eq(variantId), anyList());

        // When & Then
        mockMvc.perform(put("/api/admin/products/variants/{variantId}/images/order", variantId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(imageOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Images reordered successfully")));

        verify(adminProductService).reorderVariantImages(eq(variantId), anyList());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteImage_Success() throws Exception {
        // Given
        Long imageId = 1L;
        doNothing().when(adminProductService).deleteImage(imageId);

        // When & Then
        mockMvc.perform(delete("/api/admin/products/images/{imageId}", imageId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Image deleted successfully")));

        verify(adminProductService).deleteImage(imageId);
    }

    // ========== SECURITY TESTS ==========
    @Test
    void createProduct_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/admin/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER") // Wrong role
    void createProduct_WithoutAdminRole_ReturnsForbidden() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/admin/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_WithoutCSRF_ReturnsCreated() throws Exception {
        // Given - CSRF is disabled in security config, so this should succeed
        when(adminProductService.createProduct(any(ProductCreateRequest.class))).thenReturn(productDetailDTO);

        // When & Then
        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)));
    }

    // ========== VALIDATION TESTS ==========
    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_EmptyName_ReturnsBadRequest() throws Exception {
        // Given
        createRequest.setName("");

        // When & Then
        mockMvc.perform(post("/api/admin/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_EmptySKU_ReturnsBadRequest() throws Exception {
        // Given
        createRequest.setSku("");

        // When & Then
        mockMvc.perform(post("/api/admin/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_NoVariants_ReturnsBadRequest() throws Exception {
        // Given
        createRequest.setVariants(null);

        // When & Then
        mockMvc.perform(post("/api/admin/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addVariant_NegativePrice_ReturnsBadRequest() throws Exception {
        // Given
        Long productId = 1L;
        ProductVariantRequest variantRequest = new ProductVariantRequest();
        variantRequest.setName("Test Variant");
        variantRequest.setSku("TEST-001-VAR");
        variantRequest.setPrice(new BigDecimal("-10.00")); // Negative price
        variantRequest.setStock(100);
        variantRequest.setActive(true);

        // When & Then
        mockMvc.perform(post("/api/admin/products/{productId}/variants", productId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(variantRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addVariant_NegativeStock_ReturnsBadRequest() throws Exception {
        // Given
        Long productId = 1L;
        ProductVariantRequest variantRequest = new ProductVariantRequest();
        variantRequest.setName("Test Variant");
        variantRequest.setSku("TEST-001-VAR");
        variantRequest.setPrice(new BigDecimal("99.99"));
        variantRequest.setStock(-10); // Negative stock
        variantRequest.setActive(true);

        // When & Then
        mockMvc.perform(post("/api/admin/products/{productId}/variants", productId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(variantRequest)))
                .andExpect(status().isBadRequest());
    }
}
