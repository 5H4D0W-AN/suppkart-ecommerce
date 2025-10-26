package com.suppkart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suppkart.dto.admin.inventory.*;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
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

@SpringBootTest
@AutoConfigureMockMvc
class AdminInventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private InventoryDTO inventoryDTO;
    private InventoryUpdateRequest updateRequest;
    private StockAdjustmentRequest adjustmentRequest;
    private InventoryFilterRequest filterRequest;
    private InventoryHistoryDTO historyDTO;
    private StockAlertDTO stockAlertDTO;

    @BeforeEach
    void setUp() {
        // Setup InventoryDTO
        inventoryDTO = InventoryDTO.builder()
                .productId(1L)
                .productName("Test Product")
                .productSku("TEST-001")
                .productImage("http://example.com/image.jpg")
                .variantId(1L)
                .variantName("Test Variant")
                .variantSku("TEST-001-VAR")
                .sku("TEST-001-VAR")
                .quantity(100)
                .lowStockThreshold(10)
                .lastUpdated(LocalDateTime.now())
                .isLowStock(false)
                .isOutOfStock(false)
                .isInStock(true)
                .categoryName("Electronics")
                .brandName("TestBrand")
                .price(99.99)
                .salePrice(89.99)
                .status("ACTIVE")
                .availableQuantity(100)
                .build();

        // Setup InventoryUpdateRequest
        updateRequest = InventoryUpdateRequest.builder()
                .quantity(150)
                .reason("Stock replenishment")
                .lowStockThreshold(15)
                .sendNotification(true)
                .notes("Updated via admin panel")
                .build();

        // Setup StockAdjustmentRequest
        adjustmentRequest = StockAdjustmentRequest.builder()
                .productId(1L)
                .variantId(1L)
                .previousQuantity(100)
                .newQuantity(150)
                .changeType("STOCK_ADJUSTMENT")
                .reason("Manual adjustment")
                .referenceNumber("ADJ-001")
                .build();

        // Setup InventoryFilterRequest
        filterRequest = InventoryFilterRequest.builder()
                .search("test")
                .categoryId(1L)
                .brandName("TestBrand")
                .lowStock(false)
                .outOfStock(false)
                .inStock(true)
                .status("ACTIVE")
                .sortBy("lastUpdated")
                .sortDirection("DESC")
                .minQuantity(0)
                .maxQuantity(1000)
                .build();

        // Setup InventoryHistoryDTO
        historyDTO = InventoryHistoryDTO.builder()
                .id(1L)
                .productId(1L)
                .productName("Test Product")
                .productSku("TEST-001")
                .productImage("http://example.com/image.jpg")
                .variantId(1L)
                .variantName("Test Variant")
                .variantSku("TEST-001-VAR")
                .previousQuantity(100)
                .newQuantity(150)
                .quantityChange(50)
                .changeType("STOCK_ADJUSTMENT")
                .reason("Manual adjustment")
                .updatedBy("admin@test.com")
                .updatedByName("Admin User")
                .updatedAt(LocalDateTime.now())
                .referenceNumber("ADJ-001")
                .build();

        // Setup StockAlertDTO
        stockAlertDTO = StockAlertDTO.builder()
                .id(1L)
                .productId(1L)
                .productName("Test Product")
                .productSku("TEST-001")
                .productImage("http://example.com/image.jpg")
                .variantId(1L)
                .variantName("Test Variant")
                .variantSku("TEST-001-VAR")
                .alertType("LOW_STOCK")
                .threshold(10)
                .currentStock(5)
                .createdAt(LocalDateTime.now())
                .isResolved(false)
                .notificationSent(true)
                .categoryName("Electronics")
                .brandName("TestBrand")
                .status("ACTIVE")
                .build();
    }

    // ========== GET VARIANT INVENTORY TESTS ==========
    @Test
    @WithMockUser(roles = "ADMIN")
    void getVariantInventory_Success() throws Exception {
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        when(inventoryService.getInventory(productId, variantId)).thenReturn(inventoryDTO);

        // When & Then
        mockMvc.perform(get("/api/admin/inventory/{productId}/variants/{variantId}", productId, variantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Variant inventory retrieved successfully")))
                .andExpect(jsonPath("$.data.productId", is(1)))
                .andExpect(jsonPath("$.data.variantId", is(1)))
                .andExpect(jsonPath("$.data.quantity", is(100)))
                .andExpect(jsonPath("$.data.productName", is("Test Product")))
                .andExpect(jsonPath("$.data.variantName", is("Test Variant")));

        verify(inventoryService).getInventory(productId, variantId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getVariantInventory_NotFound_ReturnsNotFound() throws Exception {
        // Given
        Long productId = 999L;
        Long variantId = 999L;
        when(inventoryService.getInventory(productId, variantId))
                .thenThrow(new ResourceNotFoundException("Inventory not found for product: " + productId + ", variant: " + variantId));

        // When & Then
        mockMvc.perform(get("/api/admin/inventory/{productId}/variants/{variantId}", productId, variantId))
                .andExpect(status().isNotFound());

        verify(inventoryService).getInventory(productId, variantId);
    }

    // ========== GET ALL INVENTORY TESTS ==========
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllInventory_Success() throws Exception {
        // Given
        List<InventoryDTO> inventoryList = Arrays.asList(inventoryDTO);
        Page<InventoryDTO> inventoryPage = new PageImpl<>(inventoryList, PageRequest.of(0, 20), 1);

        when(inventoryService.getAllInventory(any(InventoryFilterRequest.class), any()))
                .thenReturn(inventoryPage);

        // When & Then
        mockMvc.perform(get("/api/admin/inventory")
                .param("search", "test")
                .param("categoryId", "1")
                .param("brandName", "TestBrand")
                .param("status", "ACTIVE")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Inventory list retrieved successfully")))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].productName", is("Test Product")))
                .andExpect(jsonPath("$.data.totalElements", is(1)));

        verify(inventoryService).getAllInventory(any(InventoryFilterRequest.class), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllInventory_WithFilters_Success() throws Exception {
        // Given
        List<InventoryDTO> inventoryList = Arrays.asList(inventoryDTO);
        Page<InventoryDTO> inventoryPage = new PageImpl<>(inventoryList, PageRequest.of(0, 20), 1);

        when(inventoryService.getAllInventory(any(InventoryFilterRequest.class), any()))
                .thenReturn(inventoryPage);

        // When & Then
        mockMvc.perform(get("/api/admin/inventory")
                .param("lowStock", "true")
                .param("minQuantity", "0")
                .param("maxQuantity", "50")
                .param("sortBy", "quantity")
                .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(1)));

        verify(inventoryService).getAllInventory(any(InventoryFilterRequest.class), any());
    }

    // ========== UPDATE VARIANT INVENTORY TESTS ==========
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateVariantInventory_Success() throws Exception {
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        InventoryDTO updatedInventory = InventoryDTO.builder()
                .productId(inventoryDTO.getProductId())
                .productName(inventoryDTO.getProductName())
                .productSku(inventoryDTO.getProductSku())
                .productImage(inventoryDTO.getProductImage())
                .variantId(inventoryDTO.getVariantId())
                .variantName(inventoryDTO.getVariantName())
                .variantSku(inventoryDTO.getVariantSku())
                .sku(inventoryDTO.getSku())
                .quantity(150)
                .lowStockThreshold(inventoryDTO.getLowStockThreshold())
                .lastUpdated(inventoryDTO.getLastUpdated())
                .isLowStock(inventoryDTO.getIsLowStock())
                .isOutOfStock(inventoryDTO.getIsOutOfStock())
                .isInStock(inventoryDTO.getIsInStock())
                .categoryName(inventoryDTO.getCategoryName())
                .brandName(inventoryDTO.getBrandName())
                .price(inventoryDTO.getPrice())
                .salePrice(inventoryDTO.getSalePrice())
                .status(inventoryDTO.getStatus())
                .availableQuantity(inventoryDTO.getAvailableQuantity())
                .build();
        
        when(inventoryService.updateInventory(eq(productId), eq(variantId), any(InventoryUpdateRequest.class)))
                .thenReturn(updatedInventory);

        // When & Then
        mockMvc.perform(put("/api/admin/inventory/{productId}/variants/{variantId}", productId, variantId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Variant inventory updated successfully")))
                .andExpect(jsonPath("$.data.quantity", is(150)));

        verify(inventoryService).updateInventory(eq(productId), eq(variantId), any(InventoryUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateVariantInventory_ValidationError_ReturnsBadRequest() throws Exception {
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        updateRequest.setQuantity(null); // Invalid - null quantity

        // When & Then
        mockMvc.perform(put("/api/admin/inventory/{productId}/variants/{variantId}", productId, variantId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateVariantInventory_NegativeQuantity_ReturnsBadRequest() throws Exception {
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        updateRequest.setQuantity(-10); // Invalid - negative quantity

        // When & Then
        mockMvc.perform(put("/api/admin/inventory/{productId}/variants/{variantId}", productId, variantId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    // ========== STOCK ADJUSTMENT TESTS ==========
    @Test
    @WithMockUser(roles = "ADMIN")
    void adjustStock_Success() throws Exception {
        // Given
        doNothing().when(inventoryService).adjustStock(any(StockAdjustmentRequest.class));

        // When & Then
        mockMvc.perform(post("/api/admin/inventory/adjust")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adjustmentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Stock adjustment recorded successfully")));

        verify(inventoryService).adjustStock(any(StockAdjustmentRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adjustStock_ValidationError_ReturnsBadRequest() throws Exception {
        // Given
        adjustmentRequest.setProductId(null); // Invalid - null product ID

        // When & Then
        mockMvc.perform(post("/api/admin/inventory/adjust")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adjustmentRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adjustStock_NegativeQuantity_ReturnsBadRequest() throws Exception {
        // Given
        adjustmentRequest.setPreviousQuantity(-5); // Invalid - negative quantity

        // When & Then
        mockMvc.perform(post("/api/admin/inventory/adjust")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adjustmentRequest)))
                .andExpect(status().isBadRequest());
    }

    // ========== INVENTORY HISTORY TESTS ==========
    @Test
    @WithMockUser(roles = "ADMIN")
    void getVariantHistory_Success() throws Exception {
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        List<InventoryHistoryDTO> history = Arrays.asList(historyDTO);
        
        when(inventoryService.getInventoryHistory(productId, variantId)).thenReturn(history);

        // When & Then
        mockMvc.perform(get("/api/admin/inventory/{productId}/variants/{variantId}/history", productId, variantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Inventory history retrieved successfully")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].productName", is("Test Product")))
                .andExpect(jsonPath("$.data[0].changeType", is("STOCK_ADJUSTMENT")))
                .andExpect(jsonPath("$.data[0].quantityChange", is(50)));

        verify(inventoryService).getInventoryHistory(productId, variantId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getVariantHistory_EmptyHistory_ReturnsEmptyList() throws Exception {
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        when(inventoryService.getInventoryHistory(productId, variantId)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/admin/inventory/{productId}/variants/{variantId}/history", productId, variantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)));

        verify(inventoryService).getInventoryHistory(productId, variantId);
    }

    // ========== STOCK ALERTS TESTS ==========
    @Test
    @WithMockUser(roles = "ADMIN")
    void getLowStockAlerts_Success() throws Exception {
        // Given
        List<StockAlertDTO> alerts = Arrays.asList(stockAlertDTO);
        when(inventoryService.getLowStockAlerts()).thenReturn(alerts);

        // When & Then
        mockMvc.perform(get("/api/admin/inventory/alerts/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Low stock alerts retrieved successfully")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].alertType", is("LOW_STOCK")))
                .andExpect(jsonPath("$.data[0].currentStock", is(5)))
                .andExpect(jsonPath("$.data[0].threshold", is(10)));

        verify(inventoryService).getLowStockAlerts();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getOutOfStockAlerts_Success() throws Exception {
        // Given
        StockAlertDTO outOfStockAlert = StockAlertDTO.builder()
                .id(stockAlertDTO.getId())
                .productId(stockAlertDTO.getProductId())
                .productName(stockAlertDTO.getProductName())
                .productSku(stockAlertDTO.getProductSku())
                .productImage(stockAlertDTO.getProductImage())
                .variantId(stockAlertDTO.getVariantId())
                .variantName(stockAlertDTO.getVariantName())
                .variantSku(stockAlertDTO.getVariantSku())
                .alertType("OUT_OF_STOCK")
                .threshold(0)
                .currentStock(0)
                .createdAt(stockAlertDTO.getCreatedAt())
                .isResolved(stockAlertDTO.getIsResolved())
                .notificationSent(stockAlertDTO.getNotificationSent())
                .categoryName(stockAlertDTO.getCategoryName())
                .brandName(stockAlertDTO.getBrandName())
                .status(stockAlertDTO.getStatus())
                .build();
        List<StockAlertDTO> alerts = Arrays.asList(outOfStockAlert);
        when(inventoryService.getOutOfStockAlerts()).thenReturn(alerts);

        // When & Then
        mockMvc.perform(get("/api/admin/inventory/alerts/out-of-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Out of stock alerts retrieved successfully")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].alertType", is("OUT_OF_STOCK")))
                .andExpect(jsonPath("$.data[0].currentStock", is(0)));

        verify(inventoryService).getOutOfStockAlerts();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void resolveAlert_Success() throws Exception {
        // Given
        Long alertId = 1L;
        StockAlertDTO resolvedAlert = StockAlertDTO.builder()
                .id(stockAlertDTO.getId())
                .productId(stockAlertDTO.getProductId())
                .productName(stockAlertDTO.getProductName())
                .productSku(stockAlertDTO.getProductSku())
                .productImage(stockAlertDTO.getProductImage())
                .variantId(stockAlertDTO.getVariantId())
                .variantName(stockAlertDTO.getVariantName())
                .variantSku(stockAlertDTO.getVariantSku())
                .alertType(stockAlertDTO.getAlertType())
                .threshold(stockAlertDTO.getThreshold())
                .currentStock(stockAlertDTO.getCurrentStock())
                .createdAt(stockAlertDTO.getCreatedAt())
                .resolvedAt(LocalDateTime.now())
                .isResolved(true)
                .notificationSent(stockAlertDTO.getNotificationSent())
                .categoryName(stockAlertDTO.getCategoryName())
                .brandName(stockAlertDTO.getBrandName())
                .status(stockAlertDTO.getStatus())
                .build();
        when(inventoryService.resolveAlert(alertId)).thenReturn(resolvedAlert);

        // When & Then
        mockMvc.perform(post("/api/admin/inventory/alerts/{alertId}/resolve", alertId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Alert resolved successfully")))
                .andExpect(jsonPath("$.data.isResolved", is(true)));

        verify(inventoryService).resolveAlert(alertId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void resolveAlert_NotFound_ReturnsNotFound() throws Exception {
        // Given
        Long alertId = 999L;
        when(inventoryService.resolveAlert(alertId))
                .thenThrow(new ResourceNotFoundException("Stock alert not found: " + alertId));

        // When & Then
        mockMvc.perform(post("/api/admin/inventory/alerts/{alertId}/resolve", alertId)
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(inventoryService).resolveAlert(alertId);
    }

    // ========== THRESHOLD CONFIGURATION TESTS ==========
    @Test
    @WithMockUser(roles = "ADMIN")
    void configureVariantThreshold_Success() throws Exception {
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        int threshold = 20;
        doNothing().when(inventoryService).configureStockThreshold(productId, variantId, threshold);

        // When & Then
        mockMvc.perform(put("/api/admin/inventory/{productId}/variants/{variantId}/threshold", productId, variantId)
                .with(csrf())
                .param("threshold", String.valueOf(threshold)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Stock threshold configured successfully")));

        verify(inventoryService).configureStockThreshold(productId, variantId, threshold);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void configureVariantThreshold_InvalidThreshold_ReturnsBadRequest() throws Exception {
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        int threshold = -5; // Invalid negative threshold

        // When & Then
        mockMvc.perform(put("/api/admin/inventory/{productId}/variants/{variantId}/threshold", productId, variantId)
                .with(csrf())
                .param("threshold", String.valueOf(threshold)))
                .andExpect(status().isBadRequest());
    }

    // ========== INVENTORY STATISTICS TESTS ==========
    @Test
    @WithMockUser(roles = "ADMIN")
    void getInventoryStats_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/inventory/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Inventory statistics retrieved successfully")))
                .andExpect(jsonPath("$.data.totalProducts", is(0)))
                .andExpect(jsonPath("$.data.lowStockItems", is(0)))
                .andExpect(jsonPath("$.data.outOfStockItems", is(0)))
                .andExpect(jsonPath("$.data.totalValue", is(0)));
    }

    // ========== SECURITY TESTS ==========
    @Test
    void getVariantInventory_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/inventory/{productId}/variants/{variantId}", 1L, 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER") // Wrong role
    void getVariantInventory_WithoutAdminRole_ReturnsForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/inventory/{productId}/variants/{variantId}", 1L, 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN") // Valid role
    void getVariantInventory_WithSuperAdminRole_Success() throws Exception {
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        when(inventoryService.getInventory(productId, variantId)).thenReturn(inventoryDTO);

        // When & Then
        mockMvc.perform(get("/api/admin/inventory/{productId}/variants/{variantId}", productId, variantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        verify(inventoryService).getInventory(productId, variantId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateVariantInventory_WithCSRF_Success() throws Exception {
        // Given
        when(inventoryService.updateInventory(eq(1L), eq(1L), any(InventoryUpdateRequest.class)))
                .thenReturn(inventoryDTO);

        // When & Then
        mockMvc.perform(put("/api/admin/inventory/{productId}/variants/{variantId}", 1L, 1L)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        verify(inventoryService).updateInventory(eq(1L), eq(1L), any(InventoryUpdateRequest.class));
    }

    // ========== EDGE CASES ==========
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateVariantInventory_LargeQuantity_Success() throws Exception {
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        updateRequest.setQuantity(Integer.MAX_VALUE);
        InventoryDTO updatedInventory = InventoryDTO.builder()
                .productId(inventoryDTO.getProductId())
                .productName(inventoryDTO.getProductName())
                .productSku(inventoryDTO.getProductSku())
                .productImage(inventoryDTO.getProductImage())
                .variantId(inventoryDTO.getVariantId())
                .variantName(inventoryDTO.getVariantName())
                .variantSku(inventoryDTO.getVariantSku())
                .sku(inventoryDTO.getSku())
                .quantity(Integer.MAX_VALUE)
                .lowStockThreshold(inventoryDTO.getLowStockThreshold())
                .lastUpdated(inventoryDTO.getLastUpdated())
                .isLowStock(inventoryDTO.getIsLowStock())
                .isOutOfStock(inventoryDTO.getIsOutOfStock())
                .isInStock(inventoryDTO.getIsInStock())
                .categoryName(inventoryDTO.getCategoryName())
                .brandName(inventoryDTO.getBrandName())
                .price(inventoryDTO.getPrice())
                .salePrice(inventoryDTO.getSalePrice())
                .status(inventoryDTO.getStatus())
                .availableQuantity(inventoryDTO.getAvailableQuantity())
                .build();
        
        when(inventoryService.updateInventory(eq(productId), eq(variantId), any(InventoryUpdateRequest.class)))
                .thenReturn(updatedInventory);

        // When & Then
        mockMvc.perform(put("/api/admin/inventory/{productId}/variants/{variantId}", productId, variantId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.quantity", is(Integer.MAX_VALUE)));

        verify(inventoryService).updateInventory(eq(productId), eq(variantId), any(InventoryUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adjustStock_WithAllOptionalFields_Success() throws Exception {
        // Given
        adjustmentRequest.setSupplierName("Test Supplier");
        adjustmentRequest.setCustomerName("Test Customer");
        adjustmentRequest.setUnitCost(50.0);
        adjustmentRequest.setUnitPrice(99.99);
        
        doNothing().when(inventoryService).adjustStock(any(StockAdjustmentRequest.class));

        // When & Then
        mockMvc.perform(post("/api/admin/inventory/adjust")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adjustmentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        verify(inventoryService).adjustStock(any(StockAdjustmentRequest.class));
    }
}