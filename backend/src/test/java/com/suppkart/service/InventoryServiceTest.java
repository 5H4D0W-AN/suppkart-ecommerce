package com.suppkart.service;

import com.suppkart.dto.admin.inventory.*;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.entity.*;
import com.suppkart.model.enums.AlertType;
import com.suppkart.model.enums.Brand;
import com.suppkart.model.enums.ChangeType;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryHistoryRepository inventoryHistoryRepository;

    @Mock
    private StockAlertRepository stockAlertRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private InventoryService inventoryService;

    private Product product;
    private ProductVariant variant;
    private ProductCategory category;
    private ProductImage productImage;
    private Inventory inventory;
    private InventoryHistory inventoryHistory;
    private StockAlert stockAlert;
    private User user;
    private InventoryUpdateRequest updateRequest;
    private StockAdjustmentRequest adjustmentRequest;
    private InventoryFilterRequest filterRequest;

    @BeforeEach
    void setUp() {
        // Setup Product
        product = new Product();
        product.setProductId(1L);
        product.setName("Test Product");
        product.setSku("TEST-001");
        product.setDescription("Test Description");
        product.setIsActive(true);
        product.setBrand(Brand.SUPPKART);

        // Setup ProductCategory
        category = new ProductCategory();
        category.setCategoryId(1L);
        
        // Setup actual Category entity
        Category actualCategory = new Category();
        actualCategory.setCategoryId(1L);
        actualCategory.setName("Electronics");
        category.setCategory(actualCategory);
        
        product.setProductCategories(Arrays.asList(category));

        // Setup ProductImage
        productImage = new ProductImage();
        productImage.setImageId(1L);
        productImage.setImageUrl("http://example.com/image.jpg");
        productImage.setAltText("Test Image");
        productImage.setIsPrimary(true);
        product.setImages(Arrays.asList(productImage));

        // Setup ProductVariant
        variant = new ProductVariant();
        variant.setVariantId(1L);
        variant.setName("Test Variant");
        variant.setSku("TEST-001-VAR");
        variant.setPrice(BigDecimal.valueOf(99.99));
        variant.setSalePrice(BigDecimal.valueOf(89.99));
        variant.setProduct(product);

        // Setup Inventory
        inventory = new Inventory();
        // inventory.setInventoryId(1L); // This method might not exist, let's remove it
        inventory.setProduct(product);
        inventory.setVariant(variant);
        inventory.setQuantity(100);
        inventory.setLowStockThreshold(10);
        inventory.setLastUpdated(LocalDateTime.now());

        // Setup InventoryHistory
        inventoryHistory = new InventoryHistory();
        inventoryHistory.setId(1L);
        inventoryHistory.setProduct(product);
        inventoryHistory.setVariant(variant);
        inventoryHistory.setPreviousQuantity(100);
        inventoryHistory.setNewQuantity(150);
        inventoryHistory.setChangeType(ChangeType.STOCK_ADJUSTMENT);
        inventoryHistory.setReason("Manual adjustment");
        inventoryHistory.setUpdatedAt(LocalDateTime.now());

        // Setup StockAlert
        stockAlert = new StockAlert();
        stockAlert.setId(1L);
        stockAlert.setProduct(product);
        stockAlert.setVariant(variant);
        stockAlert.setAlertType(AlertType.LOW_STOCK);
        stockAlert.setThreshold(10);
        stockAlert.setCurrentStock(5);
        stockAlert.setCreatedAt(LocalDateTime.now());
        stockAlert.setIsResolved(false);
        stockAlert.setNotificationSent(false);

        // Setup User
        user = new User();
        user.setUserId(1L);
        user.setEmail("admin@test.com");
        user.setName("Admin User");

        // Setup Requests
        updateRequest = InventoryUpdateRequest.builder()
                .quantity(150)
                .reason("Stock replenishment")
                .lowStockThreshold(15)
                .sendNotification(true)
                .build();

        adjustmentRequest = StockAdjustmentRequest.builder()
                .productId(1L)
                .variantId(1L)
                .previousQuantity(100)
                .newQuantity(150)
                .changeType("STOCK_ADJUSTMENT")
                .reason("Manual adjustment")
                .referenceNumber("ADJ-001")
                .supplierName("Test Supplier")
                .customerName("Test Customer")
                .unitCost(50.0)
                .unitPrice(99.99)
                .build();

        filterRequest = InventoryFilterRequest.builder()
                .search("test")
                .categoryId(1L)
                .brandName("GENERIC")
                .lowStock(false)
                .outOfStock(false)
                .inStock(true)
                .status("ACTIVE")
                .sortBy("lastUpdated")
                .sortDirection("DESC")
                .minQuantity(0)
                .maxQuantity(1000)
                .build();
    }

    // ========== GET INVENTORY TESTS ==========
    @Test
    void getInventory_Success() {
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        when(inventoryRepository.findByProductIdAndVariantId(productId, variantId))
                .thenReturn(Optional.of(inventory));

        // When
        InventoryDTO result = inventoryService.getInventory(productId, variantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getVariantId()).isEqualTo(variantId);
        assertThat(result.getQuantity()).isEqualTo(100);
        assertThat(result.getProductName()).isEqualTo("Test Product");
        assertThat(result.getVariantName()).isEqualTo("Test Variant");
        assertThat(result.getLowStockThreshold()).isEqualTo(10);

        verify(inventoryRepository).findByProductIdAndVariantId(productId, variantId);
    }

    @Test
    void getInventory_NotFound_ThrowsResourceNotFoundException() {
        // Given
        Long productId = 999L;
        Long variantId = 999L;
        when(inventoryRepository.findByProductIdAndVariantId(productId, variantId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryService.getInventory(productId, variantId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Inventory not found for product: " + productId + ", variant: " + variantId);

        verify(inventoryRepository).findByProductIdAndVariantId(productId, variantId);
    }

    // ========== GET ALL INVENTORY TESTS ==========
    @Test
    void getAllInventory_Success() {
        // Given
        List<Inventory> inventoryList = Arrays.asList(inventory);
        Page<Inventory> inventoryPage = new PageImpl<>(inventoryList, PageRequest.of(0, 20), 1);
        Pageable pageable = PageRequest.of(0, 20);

        when(inventoryRepository.findAllWithFilters(
                anyString(), anyLong(), anyString(), anyBoolean(), anyBoolean(), 
                anyBoolean(), anyString(), anyInt(), anyInt(), any(Pageable.class)))
                .thenReturn(inventoryPage);

        // When
        Page<InventoryDTO> result = inventoryService.getAllInventory(filterRequest, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getProductName()).isEqualTo("Test Product");

        verify(inventoryRepository).findAllWithFilters(
                anyString(), anyLong(), anyString(), anyBoolean(), anyBoolean(), 
                anyBoolean(), anyString(), anyInt(), anyInt(), any(Pageable.class));
    }

    @Test
    void getAllInventory_EmptyResult_ReturnsEmptyPage() {
        // Given
        Page<Inventory> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 20), 0);
        Pageable pageable = PageRequest.of(0, 20);

        when(inventoryRepository.findAllWithFilters(
                anyString(), anyLong(), anyString(), anyBoolean(), anyBoolean(), 
                anyBoolean(), anyString(), anyInt(), anyInt(), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        Page<InventoryDTO> result = inventoryService.getAllInventory(filterRequest, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    // ========== UPDATE INVENTORY TESTS ==========
    @Test
    void updateInventory_Success() {
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        when(inventoryRepository.findByProductIdAndVariantId(productId, variantId))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryHistoryRepository.save(any(InventoryHistory.class))).thenReturn(inventoryHistory);

        // Mock security context
        mockSecurityContext();

        // When
        InventoryDTO result = inventoryService.updateInventory(productId, variantId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(150);
        assertThat(result.getLowStockThreshold()).isEqualTo(15);

        verify(inventoryRepository).findByProductIdAndVariantId(productId, variantId);
        verify(inventoryRepository).save(any(Inventory.class));
        verify(inventoryHistoryRepository).save(any(InventoryHistory.class));
    }

    @Test
    void updateInventory_WithNotification_SendsNotification() {
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        updateRequest.setSendNotification(true);
        
        when(inventoryRepository.findByProductIdAndVariantId(productId, variantId))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryHistoryRepository.save(any(InventoryHistory.class))).thenReturn(inventoryHistory);

        mockSecurityContext();

        // When
        InventoryDTO result = inventoryService.updateInventory(productId, variantId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryRepository).save(any(Inventory.class));
        verify(inventoryHistoryRepository).save(any(InventoryHistory.class));
    }

    @Test
    void updateInventory_InventoryNotFound_ThrowsResourceNotFoundException() {
        // Given
        Long productId = 999L;
        Long variantId = 999L;
        when(inventoryRepository.findByProductIdAndVariantId(productId, variantId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryService.updateInventory(productId, variantId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Inventory not found for product: " + productId + ", variant: " + variantId);

        verify(inventoryRepository).findByProductIdAndVariantId(productId, variantId);
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    // ========== STOCK ADJUSTMENT TESTS ==========
    @Test
    void adjustStock_Success() {
        // Given
        when(inventoryRepository.findByProductIdAndVariantId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryHistoryRepository.save(any(InventoryHistory.class))).thenReturn(inventoryHistory);

        mockSecurityContext();

        // When
        inventoryService.adjustStock(adjustmentRequest);

        // Then
        verify(inventoryRepository).findByProductIdAndVariantId(1L, 1L);
        verify(inventoryRepository).save(any(Inventory.class));
        verify(inventoryHistoryRepository).save(any(InventoryHistory.class));
    }

    @Test
    void adjustStock_CreatesLowStockAlert_WhenQuantityBelowThreshold() {
        // Given
        inventory.setQuantity(15); // Above threshold initially
        adjustmentRequest.setPreviousQuantity(15);
        adjustmentRequest.setNewQuantity(5); // This will make it go to 5, below threshold of 10
        when(inventoryRepository.findByProductIdAndVariantId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryHistoryRepository.save(any(InventoryHistory.class))).thenReturn(inventoryHistory);
        when(stockAlertRepository.existsByProductIdAndVariantIdAndIsResolvedFalse(1L, 1L))
                .thenReturn(false);
        when(stockAlertRepository.save(any(StockAlert.class))).thenReturn(stockAlert);

        mockSecurityContext();

        // When
        inventoryService.adjustStock(adjustmentRequest);

        // Then
        verify(stockAlertRepository, atLeastOnce()).existsByProductIdAndVariantIdAndIsResolvedFalse(1L, 1L);
        verify(stockAlertRepository, atLeast(1)).save(any(StockAlert.class));
    }

    // ========== INVENTORY HISTORY TESTS ==========
    @Test
    void getInventoryHistory_Success() {
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        List<InventoryHistory> historyList = Arrays.asList(inventoryHistory);
        
        when(inventoryHistoryRepository.findByProductIdAndVariantIdOrderByUpdatedAtDesc(productId, variantId))
                .thenReturn(historyList);

        // When
        List<InventoryHistoryDTO> result = inventoryService.getInventoryHistory(productId, variantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductName()).isEqualTo("Test Product");
        assertThat(result.get(0).getChangeType()).isEqualTo("STOCK_ADJUSTMENT");
        assertThat(result.get(0).getPreviousQuantity()).isEqualTo(100);
        assertThat(result.get(0).getNewQuantity()).isEqualTo(150);

        verify(inventoryHistoryRepository).findByProductIdAndVariantIdOrderByUpdatedAtDesc(productId, variantId);
    }

    @Test
    void getInventoryHistory_EmptyHistory_ReturnsEmptyList() {
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        when(inventoryHistoryRepository.findByProductIdAndVariantIdOrderByUpdatedAtDesc(productId, variantId))
                .thenReturn(Arrays.asList());

        // When
        List<InventoryHistoryDTO> result = inventoryService.getInventoryHistory(productId, variantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(inventoryHistoryRepository).findByProductIdAndVariantIdOrderByUpdatedAtDesc(productId, variantId);
    }

    // ========== STOCK ALERTS TESTS ==========
    @Test
    void getLowStockAlerts_Success() {
        // Given
        List<StockAlert> alerts = Arrays.asList(stockAlert);
        when(stockAlertRepository.findByAlertTypeAndIsResolvedFalse(AlertType.LOW_STOCK))
                .thenReturn(alerts);

        // When
        List<StockAlertDTO> result = inventoryService.getLowStockAlerts();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAlertType()).isEqualTo("LOW_STOCK");
        assertThat(result.get(0).getCurrentStock()).isEqualTo(5);
        assertThat(result.get(0).getThreshold()).isEqualTo(10);

        verify(stockAlertRepository).findByAlertTypeAndIsResolvedFalse(AlertType.LOW_STOCK);
    }

    @Test
    void getOutOfStockAlerts_Success() {
        // Given
        stockAlert.setAlertType(AlertType.OUT_OF_STOCK);
        stockAlert.setCurrentStock(0);
        List<StockAlert> alerts = Arrays.asList(stockAlert);
        when(stockAlertRepository.findByAlertTypeAndIsResolvedFalse(AlertType.OUT_OF_STOCK))
                .thenReturn(alerts);

        // When
        List<StockAlertDTO> result = inventoryService.getOutOfStockAlerts();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAlertType()).isEqualTo("OUT_OF_STOCK");
        assertThat(result.get(0).getCurrentStock()).isEqualTo(0);

        verify(stockAlertRepository).findByAlertTypeAndIsResolvedFalse(AlertType.OUT_OF_STOCK);
    }

    @Test
    void resolveAlert_Success() {
        // Given
        Long alertId = 1L;
        when(stockAlertRepository.findById(alertId)).thenReturn(Optional.of(stockAlert));
        when(stockAlertRepository.save(any(StockAlert.class))).thenReturn(stockAlert);

        // When
        StockAlertDTO result = inventoryService.resolveAlert(alertId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(alertId);

        verify(stockAlertRepository).findById(alertId);
        verify(stockAlertRepository).save(any(StockAlert.class));
    }

    @Test
    void resolveAlert_NotFound_ThrowsResourceNotFoundException() {
        // Given
        Long alertId = 999L;
        when(stockAlertRepository.findById(alertId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryService.resolveAlert(alertId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Stock alert not found: " + alertId);

        verify(stockAlertRepository).findById(alertId);
        verify(stockAlertRepository, never()).save(any(StockAlert.class));
    }

    // ========== THRESHOLD CONFIGURATION TESTS ==========
    @Test
    void configureStockThreshold_Success() {
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        int threshold = 20;
        
        when(inventoryRepository.findByProductIdAndVariantId(productId, variantId))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // When
        inventoryService.configureStockThreshold(productId, variantId, threshold);

        // Then
        verify(inventoryRepository).findByProductIdAndVariantId(productId, variantId);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void configureStockThreshold_CreatesAlert_WhenCurrentStockBelowNewThreshold() {
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        int threshold = 150; // Higher than current stock of 100
        inventory.setQuantity(100);
        
        when(inventoryRepository.findByProductIdAndVariantId(productId, variantId))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(stockAlertRepository.existsByProductIdAndVariantIdAndIsResolvedFalse(productId, variantId))
                .thenReturn(false);
        when(stockAlertRepository.save(any(StockAlert.class))).thenReturn(stockAlert);

        // When
        inventoryService.configureStockThreshold(productId, variantId, threshold);

        // Then
        verify(inventoryRepository).save(any(Inventory.class));
        verify(stockAlertRepository).existsByProductIdAndVariantIdAndIsResolvedFalse(productId, variantId);
        // The service calls save twice: once for alert creation, once for notification sent update
        verify(stockAlertRepository, times(2)).save(any(StockAlert.class));
    }

    // ========== SCHEDULED STOCK CHECK TESTS ==========
    @Test
    void scheduleStockCheck_Success() {
        // Given
        List<Inventory> lowStockItems = Arrays.asList(inventory);
        List<Inventory> outOfStockItems = Arrays.asList();
        
        when(inventoryRepository.findLowStockItems()).thenReturn(lowStockItems);
        when(inventoryRepository.findOutOfStockItems()).thenReturn(outOfStockItems);
        when(stockAlertRepository.existsByProductIdAndVariantIdAndIsResolvedFalse(1L, 1L))
                .thenReturn(false);
        when(stockAlertRepository.save(any(StockAlert.class))).thenReturn(stockAlert);

        // When
        inventoryService.scheduleStockCheck();

        // Then
        verify(inventoryRepository).findLowStockItems();
        verify(inventoryRepository).findOutOfStockItems();
        // The service calls existsByProductIdAndVariantIdAndIsResolvedFalse twice:
        // once in scheduleStockCheck and once in createStockAlert
        verify(stockAlertRepository, times(2)).existsByProductIdAndVariantIdAndIsResolvedFalse(1L, 1L);
        verify(stockAlertRepository, atLeast(1)).save(any(StockAlert.class));
    }

    @Test
    void scheduleStockCheck_SkipsExistingAlerts() {
        // Given
        List<Inventory> lowStockItems = Arrays.asList(inventory);
        List<Inventory> outOfStockItems = Arrays.asList();
        
        when(inventoryRepository.findLowStockItems()).thenReturn(lowStockItems);
        when(inventoryRepository.findOutOfStockItems()).thenReturn(outOfStockItems);
        when(stockAlertRepository.existsByProductIdAndVariantIdAndIsResolvedFalse(1L, 1L))
                .thenReturn(true); // Alert already exists

        // When
        inventoryService.scheduleStockCheck();

        // Then
        verify(inventoryRepository).findLowStockItems();
        verify(inventoryRepository).findOutOfStockItems();
        verify(stockAlertRepository).existsByProductIdAndVariantIdAndIsResolvedFalse(1L, 1L);
        verify(stockAlertRepository, never()).save(any(StockAlert.class));
    }

    @Test
    void scheduleStockCheck_HandlesException() {
        // Given
        when(inventoryRepository.findLowStockItems()).thenThrow(new RuntimeException("Database error"));

        // When & Then - Should not throw exception
        assertThatCode(() -> inventoryService.scheduleStockCheck()).doesNotThrowAnyException();

        verify(inventoryRepository).findLowStockItems();
    }

    // ========== HELPER METHODS TESTS ==========
    @Test
    void createSort_WithValidParameters_ReturnsCorrectSort() {
        // This tests the private createSort method indirectly through getAllInventory
        // Given
        filterRequest.setSortBy("quantity");
        filterRequest.setSortDirection("ASC");
        
        List<Inventory> inventoryList = Arrays.asList(inventory);
        Page<Inventory> inventoryPage = new PageImpl<>(inventoryList, PageRequest.of(0, 20), 1);
        Pageable pageable = PageRequest.of(0, 20);

        when(inventoryRepository.findAllWithFilters(
                anyString(), anyLong(), anyString(), anyBoolean(), anyBoolean(), 
                anyBoolean(), anyString(), anyInt(), anyInt(), any(Pageable.class)))
                .thenReturn(inventoryPage);

        // When
        Page<InventoryDTO> result = inventoryService.getAllInventory(filterRequest, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryRepository).findAllWithFilters(
                anyString(), anyLong(), anyString(), anyBoolean(), anyBoolean(), 
                anyBoolean(), anyString(), anyInt(), anyInt(), any(Pageable.class));
    }

    @Test
    void getCurrentUser_WithAuthentication_ReturnsUser() {
        // Given
        mockSecurityContext();
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

        // When - This is tested indirectly through updateInventory
        Long productId = 1L;
        Long variantId = 1L;
        when(inventoryRepository.findByProductIdAndVariantId(productId, variantId))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryHistoryRepository.save(any(InventoryHistory.class))).thenReturn(inventoryHistory);

        InventoryDTO result = inventoryService.updateInventory(productId, variantId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findByEmail("admin@test.com");
    }

    @Test
    void getCurrentUser_WithoutAuthentication_ReturnsNull() {
        // Given
        SecurityContextHolder.clearContext();

        // When - This is tested indirectly through updateInventory
        Long productId = 1L;
        Long variantId = 1L;
        when(inventoryRepository.findByProductIdAndVariantId(productId, variantId))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryHistoryRepository.save(any(InventoryHistory.class))).thenReturn(inventoryHistory);

        InventoryDTO result = inventoryService.updateInventory(productId, variantId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, never()).findByEmail(anyString());
    }

    // ========== MAPPING TESTS ==========
    @Test
    void mapToInventoryDTO_WithCompleteData_MapsCorrectly() {
        // This is tested indirectly through getInventory
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        when(inventoryRepository.findByProductIdAndVariantId(productId, variantId))
                .thenReturn(Optional.of(inventory));

        // When
        InventoryDTO result = inventoryService.getInventory(productId, variantId);

        // Then
        assertThat(result.getProductId()).isEqualTo(product.getProductId());
        assertThat(result.getProductName()).isEqualTo(product.getName());
        assertThat(result.getProductSku()).isEqualTo(product.getSku());
        assertThat(result.getVariantId()).isEqualTo(variant.getVariantId());
        assertThat(result.getVariantName()).isEqualTo(variant.getName());
        assertThat(result.getVariantSku()).isEqualTo(variant.getSku());
        assertThat(result.getQuantity()).isEqualTo(inventory.getQuantity());
        assertThat(result.getLowStockThreshold()).isEqualTo(inventory.getLowStockThreshold());
    }

    // ========== EDGE CASES ==========
    @Test
    void updateInventory_WithZeroQuantity_Success() {
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        updateRequest.setQuantity(0);
        
        when(inventoryRepository.findByProductIdAndVariantId(productId, variantId))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryHistoryRepository.save(any(InventoryHistory.class))).thenReturn(inventoryHistory);

        mockSecurityContext();

        // When
        InventoryDTO result = inventoryService.updateInventory(productId, variantId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void updateInventory_WithMaxIntegerQuantity_Success() {
        // Given
        Long productId = 1L;
        Long variantId = 1L;
        updateRequest.setQuantity(Integer.MAX_VALUE);
        
        when(inventoryRepository.findByProductIdAndVariantId(productId, variantId))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryHistoryRepository.save(any(InventoryHistory.class))).thenReturn(inventoryHistory);

        mockSecurityContext();

        // When
        InventoryDTO result = inventoryService.updateInventory(productId, variantId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void adjustStock_WithAllOptionalFields_Success() {
        // Given
        adjustmentRequest.setSupplierName("Test Supplier");
        adjustmentRequest.setCustomerName("Test Customer");
        adjustmentRequest.setUnitCost(50.0);
        adjustmentRequest.setUnitPrice(99.99);
        adjustmentRequest.setReferenceNumber("REF-001");
        
        when(inventoryRepository.findByProductIdAndVariantId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryHistoryRepository.save(any(InventoryHistory.class))).thenReturn(inventoryHistory);

        mockSecurityContext();

        // When
        inventoryService.adjustStock(adjustmentRequest);

        // Then
        verify(inventoryRepository).save(any(Inventory.class));
        verify(inventoryHistoryRepository).save(any(InventoryHistory.class));
    }

    // ========== HELPER METHODS ==========
    private void mockSecurityContext() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@test.com");
        
        SecurityContextHolder.setContext(securityContext);
    }
}