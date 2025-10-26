package com.suppkart.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suppkart.dto.admin.order.OrderStatusHistoryDTO;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.entity.Order;
import com.suppkart.model.entity.OrderStatusHistory;
import com.suppkart.repository.OrderRepository;
import com.suppkart.repository.OrderStatusHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for customer order tracking functionality
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderTrackingService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    /**
     * Get order status history for customer tracking (chronological order)
     * This method can be used by customer-facing APIs
     */
    public List<OrderStatusHistoryDTO> getOrderTrackingHistory(Long orderId) {
        log.debug("Getting tracking history for order: {}", orderId);
        
        // Verify order exists
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        // Get status history in chronological order (oldest first for tracking timeline)
        List<OrderStatusHistory> historyEntries = orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
        
        return historyEntries.stream()
            .map(this::convertToOrderStatusHistoryDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get order status history for customer tracking by order number
     * This allows customers to track using their order number
     */
    public List<OrderStatusHistoryDTO> getOrderTrackingHistoryByOrderNumber(String orderNumber) {
        log.debug("Getting tracking history for order number: {}", orderNumber);
        
        // Find order by order number
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        
        return getOrderTrackingHistory(order.getOrderId());
    }
    
    /**
     * Get current order status
     */
    public OrderStatusHistoryDTO getCurrentOrderStatus(Long orderId) {
        log.debug("Getting current status for order: {}", orderId);
        
        // Verify order exists
        orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        // Get latest status history entry
        OrderStatusHistory latestHistory = orderStatusHistoryRepository.findLatestByOrderId(orderId);
        
        if (latestHistory == null) {
            throw new ResourceNotFoundException("No status history found for order: " + orderId);
        }
        
        return convertToOrderStatusHistoryDTO(latestHistory);
    }
    
    /**
     * Get current order status by order number
     */
    public OrderStatusHistoryDTO getCurrentOrderStatusByOrderNumber(String orderNumber) {
        log.debug("Getting current status for order number: {}", orderNumber);
        
        // Find order by order number
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        
        return getCurrentOrderStatus(order.getOrderId());
    }
    
    /**
     * Convert OrderStatusHistory entity to OrderStatusHistoryDTO
     */
    private OrderStatusHistoryDTO convertToOrderStatusHistoryDTO(OrderStatusHistory history) {
        OrderStatusHistoryDTO dto = new OrderStatusHistoryDTO();
        dto.setStatus(history.getStatus().name());
        dto.setTimestamp(history.getCreatedAt());
        dto.setComment(history.getComment());
        dto.setUpdatedBy(history.getUpdatedBy());
        return dto;
    }
}