package com.suppkart.service;

import com.suppkart.dto.admin.order.*;
import com.suppkart.dto.response.AddressResponse;
import com.suppkart.exception.OrderException;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.entity.Order;
import com.suppkart.model.entity.OrderItem;
import com.suppkart.model.entity.User;
import com.suppkart.model.entity.Address;
import com.suppkart.model.enums.OrderStatus;
import com.suppkart.model.enums.PaymentMethod;
import com.suppkart.model.enums.PaymentStatus;
import com.suppkart.repository.OrderRepository;
import com.suppkart.repository.OrderItemRepository;
import com.suppkart.repository.UserRepository;
import com.suppkart.service.PDFGenerationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for admin order management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final PDFGenerationService pdfGenerationService;

    /**
     * Get paginated orders with filtering
     */
    public Page<OrderListItemDTO> getAllOrders(OrderFilterRequest filter, Pageable pageable) {
        log.debug("Getting all orders with filter: {}", filter);
        
        Specification<Order> spec = createOrderSpecification(filter);
        Page<Order> orders = orderRepository.findAll(spec, pageable);
        
        return orders.map(this::convertToOrderListItemDTO);
    }

    /**
     * Get detailed order information by ID
     */
    public OrderDetailDTO getOrderById(Long id) {
        log.debug("Getting order by ID: {}", id);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        return convertToOrderDetailDTO(order);
    }

    /**
     * Update order status
     */
    @Transactional
    public OrderDetailDTO updateOrderStatus(Long id, String status, String comment) {
        log.debug("Updating order {} status to: {}", id, status);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setOrderStatus(orderStatus);
            order.setUpdatedAt(LocalDateTime.now());
            
            // Add status history entry (if you have status history tracking)
            // This would require a separate OrderStatusHistory entity
            
            Order savedOrder = orderRepository.save(order);
            log.info("Order {} status updated to: {}", id, status);
            
            return convertToOrderDetailDTO(savedOrder);
        } catch (IllegalArgumentException e) {
            throw new OrderException("Invalid order status: " + status);
        }
    }

    /**
     * Create shipment for order
     */
    @Transactional
    public ShipmentResponseDTO createShipment(Long orderId, ShipmentRequest request) {
        log.debug("Creating shipment for order: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        // Update order with shipment information
        order.setTrackingNumber(request.getTrackingNumber());
        order.setOrderStatus(OrderStatus.SHIPPED);
        order.setUpdatedAt(LocalDateTime.now());
        
        orderRepository.save(order);
        
        // Create shipment response
        ShipmentResponseDTO response = new ShipmentResponseDTO();
        response.setId(orderId); // Using order ID as shipment ID for simplicity
        response.setCourierCompany(request.getCourierCompany());
        response.setTrackingNumber(request.getTrackingNumber());
        response.setTrackingUrl("https://track.example.com/" + request.getTrackingNumber());
        response.setPackageWeight(request.getPackageWeight());
        response.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());
        response.setShipmentDate(LocalDateTime.now());
        
        log.info("Shipment created for order: {}", orderId);
        return response;
    }

    /**
     * Update order notes
     */
    @Transactional
    public OrderDetailDTO updateOrderNotes(Long id, String notes) {
        log.debug("Updating notes for order: {}", id);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        // Note: Order entity doesn't have notes field, so we'll skip this for now
        // In a real implementation, you would add a notes field to Order entity
        // or create a separate OrderNotes entity
        order.setUpdatedAt(LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        log.info("Notes update requested for order: {} (notes field not implemented)", id);
        
        return convertToOrderDetailDTO(savedOrder);
    }

    /**
     * Process refund for order
     */
    @Transactional
    public OrderDetailDTO processRefund(Long id, RefundRequest request) {
        log.debug("Processing refund for order: {}", id);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        // Update order status based on refund amount
        if (request.getAmount().compareTo(order.getTotalAmount()) >= 0) {
            order.setOrderStatus(OrderStatus.REFUNDED);
        } else {
            order.setOrderStatus(OrderStatus.PARTIALLY_REFUNDED);
        }
        
        order.setUpdatedAt(LocalDateTime.now());
        
        // In a real implementation, you would integrate with payment gateway
        // to process the actual refund
        
        Order savedOrder = orderRepository.save(order);
        log.info("Refund processed for order: {}", id);
        
        return convertToOrderDetailDTO(savedOrder);
    }

    /**
     * Generate invoice PDF
     */
    public byte[] generateInvoice(Long id) {
        log.debug("Generating invoice for order: {}", id);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        OrderDetailDTO orderDetail = convertToOrderDetailDTO(order);
        return pdfGenerationService.generateInvoice(orderDetail);
    }

    /**
     * Get order status history
     */
    public List<OrderStatusHistoryDTO> getOrderStatusHistory(Long id) {
        log.debug("Getting status history for order: {}", id);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        // In a real implementation, you would have a separate OrderStatusHistory entity
        // For now, return a simple history based on current status
        List<OrderStatusHistoryDTO> history = new ArrayList<>();
        
        OrderStatusHistoryDTO currentStatus = new OrderStatusHistoryDTO();
        currentStatus.setStatus(order.getOrderStatus().name());
        currentStatus.setTimestamp(order.getUpdatedAt());
        currentStatus.setComment("Current status");
        currentStatus.setUpdatedBy("System");
        
        history.add(currentStatus);
        
        return history;
    }

    /**
     * Get order analytics
     */
    public List<OrderAnalyticsDTO> getOrderAnalytics(LocalDate startDate, LocalDate endDate, String groupBy) {
        log.debug("Getting order analytics from {} to {} grouped by {}", startDate, endDate, groupBy);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        
        // Group orders by the specified criteria
        List<OrderAnalyticsDTO> analytics = new ArrayList<>();
        
        if ("day".equalsIgnoreCase(groupBy)) {
            // Group by day
            orders.stream()
                .collect(Collectors.groupingBy(order -> order.getCreatedAt().toLocalDate()))
                .forEach((date, dayOrders) -> {
                    OrderAnalyticsDTO dto = new OrderAnalyticsDTO();
                    dto.setDate(date);
                    dto.setOrderCount((long) dayOrders.size());
                    dto.setTotalRevenue(dayOrders.stream()
                        .map(Order::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
                    analytics.add(dto);
                });
        }
        
        return analytics;
    }

    /**
     * Create order specification for filtering
     */
    private Specification<Order> createOrderSpecification(OrderFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
                String searchTerm = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate orderNumberPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("orderNumber")), searchTerm);
                Predicate customerNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("user").get("firstName")), searchTerm);
                predicates.add(criteriaBuilder.or(orderNumberPredicate, customerNamePredicate));
            }
            
            if (filter.getStatus() != null && !filter.getStatus().trim().isEmpty()) {
                try {
                    OrderStatus status = OrderStatus.valueOf(filter.getStatus().toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("orderStatus"), status));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid order status filter: {}", filter.getStatus());
                }
            }
            
            if (filter.getPaymentStatus() != null && !filter.getPaymentStatus().trim().isEmpty()) {
                try {
                    PaymentStatus paymentStatus = PaymentStatus.valueOf(filter.getPaymentStatus().toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("paymentStatus"), paymentStatus));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid payment status filter: {}", filter.getPaymentStatus());
                }
            }
            
            if (filter.getStartDate() != null) {
                LocalDateTime startDateTime = filter.getStartDate().atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
            }
            
            if (filter.getEndDate() != null) {
                LocalDateTime endDateTime = filter.getEndDate().atTime(23, 59, 59);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
            }
            
            if (filter.getMinAmount() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalAmount"), filter.getMinAmount()));
            }
            
            if (filter.getMaxAmount() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalAmount"), filter.getMaxAmount()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Convert Order entity to OrderListItemDTO
     */
    private OrderListItemDTO convertToOrderListItemDTO(Order order) {
        OrderListItemDTO dto = new OrderListItemDTO();
        dto.setId(order.getOrderId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setCustomerName(order.getUser().getFirstName() + " " + order.getUser().getLastName());
        dto.setCustomerEmail(order.getUser().getEmail());
        dto.setDate(order.getCreatedAt());
        dto.setTotal(order.getTotalAmount());
        dto.setStatus(order.getOrderStatus()); // Set enum directly
        dto.setPaymentMethod(order.getPaymentMethod()); // Set enum directly, can be null
        dto.setPaymentStatus(order.getPaymentStatus()); // Set enum directly, can be null
        dto.setItems(order.getOrderItems() != null ? order.getOrderItems().size() : 0);
        return dto;
    }

    /**
     * Convert Order entity to OrderDetailDTO
     */
    private OrderDetailDTO convertToOrderDetailDTO(Order order) {
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setId(order.getOrderId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setDate(order.getCreatedAt());
        dto.setStatus(order.getOrderStatus()); // Set enum directly
        dto.setCustomer(convertToCustomerDTO(order.getUser()));
        dto.setShippingAddress(convertOrderToShippingAddressDTO(order));
        dto.setBillingAddress(null); // Order entity doesn't have billing address fields
        dto.setItems(order.getOrderItems().stream()
            .map(this::convertToOrderItemDTO)
            .collect(Collectors.toList()));
        dto.setSubtotal(order.getSubtotal());
        dto.setShippingCost(order.getShippingCost());
        dto.setDiscount(order.getDiscountAmount());
        dto.setTax(order.getTaxAmount());
        dto.setTotal(order.getTotalAmount());
        dto.setPaymentMethod(order.getPaymentMethod()); // Set enum directly, can be null
        dto.setPaymentStatus(order.getPaymentStatus()); // Set enum directly, can be null
        dto.setTransactionId(order.getPaymentTransactionId());
        dto.setNotes(null); // Order entity doesn't have notes field
        
        // Set shipment info if available
        if (order.getTrackingNumber() != null) {
            ShipmentDTO shipment = new ShipmentDTO();
            shipment.setTrackingNumber(order.getTrackingNumber());
            shipment.setTrackingUrl("https://track.example.com/" + order.getTrackingNumber());
            dto.setShipment(shipment);
        }
        
        return dto;
    }

    /**
     * Convert User entity to CustomerDTO
     */
    private CustomerDTO convertToCustomerDTO(User user) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(user.getUserId());
        dto.setName(user.getFirstName() + " " + user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getUserProfile() != null ? user.getUserProfile().getPhone() : user.getPhone());
        return dto;
    }

    /**
     * Convert Address entity to AddressDTO
     */
    private AddressDTO convertToAddressDTO(Address address) {
        if (address == null) {
            return null;
        }
        
        AddressDTO dto = new AddressDTO();
        dto.setAddressId(address.getAddressId());
        dto.setFirstName(address.getFirstName());
        dto.setLastName(address.getLastName());
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        dto.setPhone(address.getPhone());
        dto.setIsDefault(address.getIsDefault());
        dto.setLabel(address.getLabel());
        dto.setCreatedAt(address.getCreatedAt());
        dto.setUpdatedAt(address.getUpdatedAt());
        return dto;
    }

    /**
     * Convert OrderItem entity to OrderItemDTO
     */
    private OrderItemDTO convertToOrderItemDTO(OrderItem orderItem) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(orderItem.getOrderItemId());
        dto.setProductId(orderItem.getProduct().getProductId());
        dto.setProductName(orderItem.getProductName()); // Use stored product name
        dto.setVariantName(orderItem.getVariantName()); // Use stored variant name
        dto.setSku(orderItem.getProductSku()); // Use stored SKU
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getUnitPrice()); // Use unitPrice field
        dto.setTotal(orderItem.getTotalPrice()); // Use totalPrice field
        
        // Set image URL from stored product image URL
        dto.setImageUrl(orderItem.getProductImageUrl());
        
        return dto;
    }

    /**
     * Convert Order's denormalized shipping address fields to AddressResponse
     */
    private AddressResponse convertOrderToShippingAddressDTO(Order order) {
        AddressResponse dto = new AddressResponse();
        dto.setAddressId(null); // No separate address ID for denormalized fields
        dto.setFirstName(order.getShippingFirstName());
        dto.setLastName(order.getShippingLastName());
        dto.setAddressLine1(order.getShippingAddressLine1());
        dto.setAddressLine2(order.getShippingAddressLine2());
        dto.setCity(order.getShippingCity());
        dto.setState(order.getShippingState());
        dto.setPostalCode(order.getShippingPostalCode());
        dto.setCountry(order.getShippingCountry());
        dto.setPhone(order.getShippingPhone());
        dto.setIsDefault(false); // Denormalized addresses are not default addresses
        dto.setLabel("Shipping Address");
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }
}
