package com.suppkart.service;

import com.suppkart.dto.admin.inventory.*;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.entity.*;
import com.suppkart.model.enums.AlertType;
import com.suppkart.model.enums.ChangeType;
import com.suppkart.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Inventory Management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;
    private final StockAlertRepository stockAlertRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;

    /**
     * Get inventory for a specific variant
     */
    @Transactional(readOnly = true)
    public InventoryDTO getInventory(Long productId, Long variantId) {
        log.debug("Getting inventory for productId: {}, variantId: {}", productId, variantId);

        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductIdAndVariantId(productId, variantId);

        if (inventoryOpt.isEmpty()) {
            throw new ResourceNotFoundException("Inventory not found for product: " + productId + ", variant: " + variantId);
        }

        return mapToInventoryDTO(inventoryOpt.get());
    }

    /**
     * Get filtered inventory list
     */
    @Transactional(readOnly = true)
    public Page<InventoryDTO> getAllInventory(InventoryFilterRequest filter, Pageable pageable) {
        log.debug("Getting all inventory with filter: {}", filter);

        // Create sort
        Sort sort = createSort(filter.getSortBy(), filter.getSortDirection());
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // Apply filters and get inventory
        Page<Inventory> inventoryPage = inventoryRepository.findAllWithFilters(
                filter.getSearch(),
                filter.getCategoryId(),
                filter.getBrandName(),
                filter.getLowStock(),
                filter.getOutOfStock(),
                filter.getInStock(),
                filter.getStatus(),
                filter.getMinQuantity(),
                filter.getMaxQuantity(),
                sortedPageable
        );

        return inventoryPage.map(this::mapToInventoryDTO);
    }

    /**
     * Update inventory quantity for a variant
     */
    public InventoryDTO updateInventory(Long productId, Long variantId, InventoryUpdateRequest request) {
        log.info("Updating inventory for productId: {}, variantId: {}, quantity: {}",
                productId, variantId, request.getQuantity());

        Inventory inventory = getInventoryEntity(productId, variantId);
        Integer previousQuantity = inventory.getQuantity();

        // Update inventory
        inventory.setQuantity(request.getQuantity());
        inventory.setLastUpdated(LocalDateTime.now());

        if (request.getLowStockThreshold() != null) {
            inventory.setLowStockThreshold(request.getLowStockThreshold());
        }

        inventory = inventoryRepository.save(inventory);

        // Record history
        recordInventoryHistory(inventory, previousQuantity, request.getQuantity(),
                ChangeType.STOCK_ADJUSTMENT, request.getReason());

        // Check for alerts
        checkAndCreateAlerts(inventory);

        // Send notification if requested
        if (Boolean.TRUE.equals(request.getSendNotification())) {
            sendInventoryUpdateNotification(inventory, previousQuantity, request.getQuantity());
        }

        return mapToInventoryDTO(inventory);
    }

    /**
     * Record stock adjustment with reason for a variant
     */
    public void adjustStock(StockAdjustmentRequest request) {
        log.info("Adjusting stock for productId: {}, variantId: {}, changeType: {}",
                request.getProductId(), request.getVariantId(), request.getChangeType());

        Inventory inventory = getInventoryEntity(request.getProductId(), request.getVariantId());

        // Update quantity
        inventory.setQuantity(request.getNewQuantity());
        inventory.setLastUpdated(LocalDateTime.now());
        inventory = inventoryRepository.save(inventory);

        // Record detailed history
        recordDetailedInventoryHistory(inventory, request);

        // Check for alerts
        checkAndCreateAlerts(inventory);
    }

    /**
     * Get inventory history for a variant
     */
    @Transactional(readOnly = true)
    public List<InventoryHistoryDTO> getInventoryHistory(Long productId, Long variantId) {
        log.debug("Getting inventory history for productId: {}, variantId: {}", productId, variantId);

        List<InventoryHistory> history = inventoryHistoryRepository.findByProductIdAndVariantIdOrderByUpdatedAtDesc(productId, variantId);

        return history.stream()
                .map(this::mapToInventoryHistoryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get active low stock alerts
     */
    @Transactional(readOnly = true)
    public List<StockAlertDTO> getLowStockAlerts() {
        log.debug("Getting low stock alerts");

        List<StockAlert> alerts = stockAlertRepository.findByAlertTypeAndIsResolvedFalse(AlertType.LOW_STOCK);
        return alerts.stream()
                .map(this::mapToStockAlertDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get active out of stock alerts
     */
    @Transactional(readOnly = true)
    public List<StockAlertDTO> getOutOfStockAlerts() {
        log.debug("Getting out of stock alerts");

        List<StockAlert> alerts = stockAlertRepository.findByAlertTypeAndIsResolvedFalse(AlertType.OUT_OF_STOCK);
        return alerts.stream()
                .map(this::mapToStockAlertDTO)
                .collect(Collectors.toList());
    }

    /**
     * Resolve an alert
     */
    public StockAlertDTO resolveAlert(Long alertId) {
        log.info("Resolving alert: {}", alertId);

        StockAlert alert = stockAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock alert not found: " + alertId));

        alert.resolve();
        alert = stockAlertRepository.save(alert);

        return mapToStockAlertDTO(alert);
    }

    /**
     * Configure stock threshold for a variant
     */
    public void configureStockThreshold(Long productId, Long variantId, int threshold) {
        log.info("Configuring stock threshold for productId: {}, variantId: {}, threshold: {}",
                productId, variantId, threshold);

        Inventory inventory = getInventoryEntity(productId, variantId);
        inventory.setLowStockThreshold(threshold);
        inventoryRepository.save(inventory);

        // Check if current stock is below new threshold
        checkAndCreateAlerts(inventory);
    }

    /**
     * Scheduled task to check inventory levels and create alerts
     */
    @Scheduled(cron = "0 0 9 * * ?") // Daily at 9 AM
    public void scheduleStockCheck() {
        log.info("Running scheduled stock check");

        try {
            // Get all low stock items
            List<Inventory> lowStockItems = inventoryRepository.findLowStockItems();

            for (Inventory inventory : lowStockItems) {
                // Check if alert already exists
                boolean alertExists = stockAlertRepository.existsByProductIdAndVariantIdAndIsResolvedFalse(
                        inventory.getProduct().getProductId(),
                        inventory.getVariant().getVariantId());

                if (!alertExists) {
                    createStockAlert(inventory);
                }
            }

            // Get all out of stock items
            List<Inventory> outOfStockItems = inventoryRepository.findOutOfStockItems();

            for (Inventory inventory : outOfStockItems) {
                // Check if alert already exists
                boolean alertExists = stockAlertRepository.existsByProductIdAndVariantIdAndIsResolvedFalse(
                        inventory.getProduct().getProductId(),
                        inventory.getVariant().getVariantId());

                if (!alertExists) {
                    createOutOfStockAlert(inventory);
                }
            }

            log.info("Scheduled stock check completed. Low stock: {}, Out of stock: {}",
                    lowStockItems.size(), outOfStockItems.size());

        } catch (Exception e) {
            log.error("Error during scheduled stock check", e);
        }
    }

    // Private helper methods
    private Inventory getInventoryEntity(Long productId, Long variantId) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductIdAndVariantId(productId, variantId);

        return inventoryOpt.orElseThrow(()
                -> new ResourceNotFoundException("Inventory not found for product: " + productId + ", variant: " + variantId));
    }

    private void recordInventoryHistory(Inventory inventory, Integer previousQuantity,
            Integer newQuantity, ChangeType changeType, String reason) {
        InventoryHistory history = new InventoryHistory();
        history.setProduct(inventory.getProduct());
        history.setVariant(inventory.getVariant());
        history.setPreviousQuantity(previousQuantity);
        history.setNewQuantity(newQuantity);
        history.setChangeType(changeType);
        history.setReason(reason);
        history.setUpdatedBy(getCurrentUser());
        history.setUpdatedAt(LocalDateTime.now());

        inventoryHistoryRepository.save(history);
    }

    private void recordDetailedInventoryHistory(Inventory inventory, StockAdjustmentRequest request) {
        InventoryHistory history = new InventoryHistory();
        history.setProduct(inventory.getProduct());
        history.setVariant(inventory.getVariant());
        history.setPreviousQuantity(request.getPreviousQuantity());
        history.setNewQuantity(request.getNewQuantity());
        history.setChangeType(ChangeType.valueOf(request.getChangeType()));
        history.setReason(request.getReason());
        history.setReferenceNumber(request.getReferenceNumber());
        history.setSupplierName(request.getSupplierName());
        history.setCustomerName(request.getCustomerName());
        history.setUnitCost(request.getUnitCost());
        history.setUnitPrice(request.getUnitPrice());
        history.setUpdatedBy(getCurrentUser());
        history.setUpdatedAt(LocalDateTime.now());

        inventoryHistoryRepository.save(history);
    }

    private void checkAndCreateAlerts(Inventory inventory) {
        if (inventory.isOutOfStock()) {
            createOutOfStockAlert(inventory);
        } else if (inventory.isLowStock()) {
            createStockAlert(inventory);
        }
    }

    private void createStockAlert(Inventory inventory) {
        // Check if alert already exists
        boolean exists = stockAlertRepository.existsByProductIdAndVariantIdAndIsResolvedFalse(
                inventory.getProduct().getProductId(),
                inventory.getVariant().getVariantId());

        if (!exists) {
            StockAlert alert = new StockAlert();
            alert.setProduct(inventory.getProduct());
            alert.setVariant(inventory.getVariant());
            alert.setAlertType(AlertType.LOW_STOCK);
            alert.setThreshold(inventory.getLowStockThreshold());
            alert.setCurrentStock(inventory.getQuantity());
            alert.setCreatedAt(LocalDateTime.now());
            alert.setIsResolved(false);
            alert.setNotificationSent(false);

            stockAlertRepository.save(alert);

            // Send notification
            sendStockAlertNotification(alert);
        }
    }

    private void createOutOfStockAlert(Inventory inventory) {
        // Check if alert already exists
        boolean exists = stockAlertRepository.existsByProductIdAndVariantIdAndIsResolvedFalse(
                inventory.getProduct().getProductId(),
                inventory.getVariant().getVariantId());

        if (!exists) {
            StockAlert alert = new StockAlert();
            alert.setProduct(inventory.getProduct());
            alert.setVariant(inventory.getVariant());
            alert.setAlertType(AlertType.OUT_OF_STOCK);
            alert.setThreshold(0);
            alert.setCurrentStock(inventory.getQuantity());
            alert.setCreatedAt(LocalDateTime.now());
            alert.setIsResolved(false);
            alert.setNotificationSent(false);

            stockAlertRepository.save(alert);

            // Send notification
            sendStockAlertNotification(alert);
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            return userRepository.findByEmail(authentication.getName()).orElse(null);
        }
        return null;
    }

    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, sortBy != null ? sortBy : "lastUpdated");
    }

    private void sendInventoryUpdateNotification(Inventory inventory, Integer previousQuantity, Integer newQuantity) {
        try {
            String subject = "Inventory Updated: " + inventory.getProduct().getName();
            String message = String.format("Inventory has been updated for %s. Quantity changed from %d to %d.",
                    inventory.getProduct().getName(), previousQuantity, newQuantity);

            // This would send actual email notification
            log.info("Inventory update notification: {}", message);
        } catch (Exception e) {
            log.error("Failed to send inventory update notification", e);
        }
    }

    private void sendStockAlertNotification(StockAlert alert) {
        try {
            String subject = alert.getAlertType() == AlertType.OUT_OF_STOCK
                    ? "OUT OF STOCK ALERT" : "LOW STOCK ALERT";
            String message = alert.getAlertDescription();

            // This would send actual email notification
            log.info("Stock alert notification: {}", message);

            alert.markNotificationSent();
            stockAlertRepository.save(alert);
        } catch (Exception e) {
            log.error("Failed to send stock alert notification", e);
        }
    }

    // Mapping methods
    private InventoryDTO mapToInventoryDTO(Inventory inventory) {
        Product product = inventory.getProduct();
        ProductVariant variant = inventory.getVariant();

        InventoryDTO dto = new InventoryDTO();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getName());
        dto.setProductSku(product.getSku());

        // Get primary image
        ProductImage primaryImage = product.getPrimaryImage();
        dto.setProductImage(primaryImage != null ? primaryImage.getImageUrl() : null);

        // Since every product now has at least one variant, variant should never be null
        dto.setVariantId(variant.getVariantId());
        dto.setVariantName(variant.getName());
        dto.setVariantSku(variant.getSku());
        dto.setSku(variant.getSku());
        dto.setPrice(variant.getPrice() != null ? variant.getPrice().doubleValue() : null);
        dto.setSalePrice(variant.getSalePrice() != null ? variant.getSalePrice().doubleValue() : null);

        dto.setQuantity(inventory.getQuantity());
        dto.setLowStockThreshold(inventory.getLowStockThreshold());
        dto.setLastUpdated(inventory.getLastUpdated());

        // Status indicators
        dto.setIsLowStock(inventory.isLowStock());
        dto.setIsOutOfStock(inventory.isOutOfStock());
        dto.setIsInStock(inventory.isInStock());

        // Additional metadata
        if (!product.getProductCategories().isEmpty()) {
            // Get first category name 
            dto.setCategoryName(product.getProductCategories().get(0).getCategory().getName()); 
        }
        dto.setBrandName(product.getBrand().name());
        dto.setStatus(product.getIsActive() ? "ACTIVE" : "INACTIVE");
        dto.setAvailableQuantity(inventory.getQuantity());

        return dto;
    }

    private InventoryHistoryDTO mapToInventoryHistoryDTO(InventoryHistory history) {
        Product product = history.getProduct();
        ProductVariant variant = history.getVariant();
        User updatedBy = history.getUpdatedBy();

        InventoryHistoryDTO dto = new InventoryHistoryDTO();
        dto.setId(history.getId());
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getName());
        dto.setProductSku(product.getSku());

        ProductImage primaryImage = product.getPrimaryImage();
        dto.setProductImage(primaryImage != null ? primaryImage.getImageUrl() : null);

        // Since every product now has at least one variant, variant should never be null
        dto.setVariantId(variant.getVariantId());
        dto.setVariantName(variant.getName());
        dto.setVariantSku(variant.getSku());

        dto.setPreviousQuantity(history.getPreviousQuantity());
        dto.setNewQuantity(history.getNewQuantity());
        dto.setQuantityChange(history.getQuantityChange());
        dto.setChangeType(history.getChangeType().toString());
        dto.setReason(history.getReason());
        dto.setUpdatedBy(updatedBy != null ? updatedBy.getEmail() : null);
        dto.setUpdatedByName(updatedBy != null ? updatedBy.getName() : null);
        dto.setUpdatedAt(history.getUpdatedAt());
        dto.setReferenceNumber(history.getReferenceNumber());
        dto.setSupplierName(history.getSupplierName());
        dto.setCustomerName(history.getCustomerName());
        dto.setUnitCost(history.getUnitCost());
        dto.setUnitPrice(history.getUnitPrice());

        return dto;
    }

    private StockAlertDTO mapToStockAlertDTO(StockAlert alert) {
        Product product = alert.getProduct();
        ProductVariant variant = alert.getVariant();

        StockAlertDTO dto = new StockAlertDTO();
        dto.setId(alert.getId());
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getName());
        dto.setProductSku(product.getSku());

        ProductImage primaryImage = product.getPrimaryImage();
        dto.setProductImage(primaryImage != null ? primaryImage.getImageUrl() : null);

        // Since every product now has at least one variant, variant should never be null
        dto.setVariantId(variant.getVariantId());
        dto.setVariantName(variant.getName());
        dto.setVariantSku(variant.getSku());

        dto.setAlertType(alert.getAlertType().toString());
        dto.setThreshold(alert.getThreshold());
        dto.setCurrentStock(alert.getCurrentStock());
        dto.setCreatedAt(alert.getCreatedAt());
        dto.setResolvedAt(alert.getResolvedAt());
        dto.setIsResolved(alert.getIsResolved());
        dto.setNotificationSent(alert.getNotificationSent());

        // Additional metadata
        if (!product.getProductCategories().isEmpty()) {
            dto.setCategoryName(product.getProductCategories().get(0).getCategory().getName()); 
        }
        dto.setBrandName(product.getBrand().name());
        dto.setStatus(product.getIsActive() ? "ACTIVE" : "INACTIVE");

        return dto;
    }
}
