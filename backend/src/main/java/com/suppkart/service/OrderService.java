package com.suppkart.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suppkart.dto.request.CheckoutRequest;
import com.suppkart.exception.BusinessException;
import com.suppkart.model.entity.Address;
import com.suppkart.model.entity.Cart;
import com.suppkart.model.entity.CartItem;
import com.suppkart.model.entity.Order;
import com.suppkart.model.entity.OrderItem;
import com.suppkart.model.entity.Product;
import com.suppkart.model.entity.ProductVariant;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.OrderStatus;
import com.suppkart.model.enums.PaymentStatus;
import com.suppkart.repository.OrderItemRepository;
import com.suppkart.repository.OrderRepository;

@Service
@Transactional
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private AddressService addressService;
    
    @Autowired
    private ProductService productService;
    
    /**
     * Process checkout and create order
     * @param checkoutRequest the checkout request
     * @param user the user
     * @return Order
     * @throws BusinessException if checkout fails
     */
    public Order processCheckout(CheckoutRequest checkoutRequest, User user) {
        logger.info("Processing checkout for user: {}", user.getUserId());
        
        try {
            // Get user's cart
            Cart cart = cartService.getUserCart(user);
            if (cart == null || cart.getItems().isEmpty()) {
                throw new BusinessException("CART_EMPTY", "Cart is empty");
            }
            
            // Validate shipping address
            Address shippingAddress = addressService.validateAddressForCheckout(
                checkoutRequest.getShippingAddressId(), user);
            
            // Validate stock availability
            validateStockAvailability(cart);
            
            // Calculate order totals
            OrderTotals totals = calculateOrderTotals(cart, checkoutRequest);
            
            // Create order
            Order order = createOrder(user, shippingAddress, checkoutRequest, totals);
            
            // Create order items from cart items
            createOrderItems(order, cart);
            
            // Update product stock
            updateProductStock(cart);
            
            // Clear user's cart
            cartService.clearUserCart(user);
            
            logger.info("Successfully processed checkout for user: {}. Order ID: {}", 
                user.getUserId(), order.getOrderId());
            
            return order;
            
        } catch (BusinessException e) {
            logger.error("Business error during checkout for user {}: {}", user.getUserId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during checkout for user {}: {}", user.getUserId(), e.getMessage(), e);
            throw new BusinessException("CHECKOUT_FAILED", "Checkout failed: " + e.getMessage());
        }
    }
    
    /**
     * Get order by ID for user
     * @param orderId the order ID
     * @param user the user
     * @return Optional<Order>
     */
    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long orderId, User user) {
        logger.debug("Getting order {} for user: {}", orderId, user.getUserId());
        return orderRepository.findById(orderId)
            .filter(order -> order.getUser().getUserId().equals(user.getUserId()));
    }
    
    /**
     * Get order by order number for user
     * @param orderNumber the order number
     * @param user the user
     * @return Optional<Order>
     */
    @Transactional(readOnly = true)
    public Optional<Order> getOrderByNumber(String orderNumber, User user) {
        logger.debug("Getting order by number {} for user: {}", orderNumber, user.getUserId());
        return orderRepository.findByOrderNumber(orderNumber)
            .filter(order -> order.getUser().getUserId().equals(user.getUserId()));
    }
    
    /**
     * Get user's orders
     * @param user the user
     * @param pageable pagination information
     * @return Page<Order>
     */
    @Transactional(readOnly = true)
    public Page<Order> getUserOrders(User user, Pageable pageable) {
        logger.debug("Getting orders for user: {}", user.getUserId());
        return orderRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
    
    /**
     * Get user's orders by status
     * @param user the user
     * @param orderStatus the order status
     * @param pageable pagination information
     * @return Page<Order>
     */
    @Transactional(readOnly = true)
    public Page<Order> getUserOrdersByStatus(User user, OrderStatus orderStatus, Pageable pageable) {
        logger.debug("Getting orders with status {} for user: {}", orderStatus, user.getUserId());
        return orderRepository.findByUserAndOrderStatusOrderByCreatedAtDesc(user, orderStatus, pageable);
    }
    
    /**
     * Cancel order
     * @param orderId the order ID
     * @param user the user
     * @return Order
     * @throws BusinessException if order cannot be cancelled
     */
    public Order cancelOrder(Long orderId, User user) {
        logger.info("Cancelling order {} for user: {}", orderId, user.getUserId());
        
        Order order = orderRepository.findById(orderId)
            .filter(o -> o.getUser().getUserId().equals(user.getUserId()))
            .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found with id: " + orderId));
        
        // Check if order can be cancelled
        if (!canCancelOrder(order)) {
            throw new BusinessException("ORDER_CANNOT_CANCEL", 
                "Order cannot be cancelled. Current status: " + order.getOrderStatus());
        }
        
        try {
            // Update order status
            order.setOrderStatus(OrderStatus.CANCELLED);
            order.setCancelledAt(LocalDateTime.now());
            
            // Restore product stock
            restoreProductStock(order);
            
            Order cancelledOrder = orderRepository.save(order);
            logger.info("Successfully cancelled order {} for user: {}", orderId, user.getUserId());
            
            return cancelledOrder;
            
        } catch (Exception e) {
            logger.error("Error cancelling order {} for user {}: {}", orderId, user.getUserId(), e.getMessage(), e);
            throw new BusinessException("ORDER_CANCEL_FAILED", "Failed to cancel order: " + e.getMessage());
        }
    }
    
    /**
     * Update order status
     * @param orderId the order ID
     * @param newStatus the new status
     * @return Order
     * @throws BusinessException if order not found or status update fails
     */
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        logger.info("Updating order {} status to: {}", orderId, newStatus);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found with id: " + orderId));
        
        try {
            OrderStatus oldStatus = order.getOrderStatus();
            order.setOrderStatus(newStatus);
            
            // Update status-specific timestamps
            updateStatusTimestamps(order, newStatus);
            
            Order updatedOrder = orderRepository.save(order);
            logger.info("Successfully updated order {} status from {} to {}", 
                orderId, oldStatus, newStatus);
            
            return updatedOrder;
            
        } catch (Exception e) {
            logger.error("Error updating order {} status: {}", orderId, e.getMessage(), e);
            throw new BusinessException("ORDER_STATUS_UPDATE_FAILED", "Failed to update order status: " + e.getMessage());
        }
    }
    
    /**
     * Get order statistics for user
     * @param user the user
     * @return List of order statistics
     */
    @Transactional(readOnly = true)
    public List<Object[]> getOrderStatistics(User user) {
        logger.debug("Getting order statistics for user: {}", user.getUserId());
        return orderRepository.getOrderStatisticsByUser(user);
    }
    
    /**
     * Get order items for an order
     * @param orderId the order ID
     * @param user the user
     * @return List<OrderItem>
     */
    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItems(Long orderId, User user) {
        logger.debug("Getting order items for order {} and user: {}", orderId, user.getUserId());
        
        Order order = orderRepository.findById(orderId)
            .filter(o -> o.getUser().getUserId().equals(user.getUserId()))
            .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found with id: " + orderId));
        
        return orderItemRepository.findByOrderOrderByCreatedAtAsc(order);
    }
    
    // Private helper methods
    
    /**
     * Validate stock availability for cart items
     */
    private void validateStockAvailability(Cart cart) {
        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = cartItem.getVariant();
            if (variant != null) {
                if (!variant.isInStock() || variant.getStockQuantity() < cartItem.getQuantity()) {
                    throw new BusinessException("INSUFFICIENT_STOCK", 
                        "Insufficient stock for product: " + variant.getName());
                }
            } else {
                Product product = cartItem.getProduct();
                // Assuming product has a default stock check method
                if (cartItem.getQuantity() > 0) { // Basic validation
                    // Add product-level stock validation if needed
                }
            }
        }
    }
    
    /**
     * Calculate order totals
     */
    private OrderTotals calculateOrderTotals(Cart cart, CheckoutRequest request) {
        BigDecimal subtotal = cart.getTotalAmount();
        BigDecimal taxAmount = request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO;
        BigDecimal shippingCost = request.getShippingCost() != null ? request.getShippingCost() : BigDecimal.ZERO;
        BigDecimal discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        
        BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingCost).subtract(discountAmount);
        
        return new OrderTotals(subtotal, taxAmount, shippingCost, discountAmount, totalAmount);
    }
    
    /**
     * Create order entity
     */
    private Order createOrder(User user, Address shippingAddress, CheckoutRequest request, OrderTotals totals) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        
        // Set amounts
        order.setSubtotal(totals.subtotal);
        order.setTaxAmount(totals.taxAmount);
        order.setShippingCost(totals.shippingCost);
        order.setDiscountAmount(totals.discountAmount);
        order.setTotalAmount(totals.totalAmount);
        
        // Set addresses using the utility method
        order.copyShippingFromAddress(shippingAddress);
        
        // Set other fields
        order.setDeliveryInstructions(request.getDeliveryInstructions());
        order.setCouponCode(request.getCouponCode());
        order.setEstimatedDeliveryDate(calculateEstimatedDeliveryDate());
        
        return orderRepository.save(order);
    }
    
    /**
     * Create order items from cart items
     */
    private void createOrderItems(Order order, Cart cart) {
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem;
            
            if (cartItem.getVariant() != null) {
                orderItem = new OrderItem(order, cartItem.getVariant(), 
                    cartItem.getQuantity(), cartItem.getUnitPrice());
            } else {
                orderItem = new OrderItem(order, cartItem.getProduct(), 
                    cartItem.getQuantity(), cartItem.getUnitPrice());
            }
            
            orderItemRepository.save(orderItem);
        }
    }
    
    /**
     * Update product stock after order
     */
    private void updateProductStock(Cart cart) {
        for (CartItem cartItem : cart.getItems()) {
            if (cartItem.getVariant() != null) {
                ProductVariant variant = cartItem.getVariant();
                int newStock = variant.getStockQuantity() - cartItem.getQuantity();
                variant.setStockQuantity(Math.max(0, newStock));
                // Note: This would typically be handled by ProductService
            }
        }
    }
    
    /**
     * Restore product stock after order cancellation
     */
    private void restoreProductStock(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderOrderByCreatedAtAsc(order);
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getProductVariant() != null) {
                ProductVariant variant = orderItem.getProductVariant();
                int newStock = variant.getStockQuantity() + orderItem.getQuantity();
                variant.setStockQuantity(newStock);
                // Note: This would typically be handled by ProductService
            }
        }
    }
    
    /**
     * Check if order can be cancelled
     */
    private boolean canCancelOrder(Order order) {
        return order.getOrderStatus() == OrderStatus.PENDING || 
               order.getOrderStatus() == OrderStatus.CONFIRMED ||
               order.getOrderStatus() == OrderStatus.PROCESSING;
    }
    
    /**
     * Update status-specific timestamps
     */
    private void updateStatusTimestamps(Order order, OrderStatus newStatus) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (newStatus) {
            case DELIVERED:
                order.setDeliveredAt(now);
                break;
            case CANCELLED:
                order.setCancelledAt(now);
                break;
            default:
                // No specific timestamp for other statuses like CONFIRMED, SHIPPED
                // These would need to be added to Order entity if needed
                break;
        }
    }
    
    /**
     * Generate unique order number
     */
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + 
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Calculate estimated delivery date
     */
    private LocalDateTime calculateEstimatedDeliveryDate() {
        // Default to 7 days from now
        return LocalDateTime.now().plusDays(7);
    }
    
    // Inner class for order totals
    private static class OrderTotals {
        final BigDecimal subtotal;
        final BigDecimal taxAmount;
        final BigDecimal shippingCost;
        final BigDecimal discountAmount;
        final BigDecimal totalAmount;
        
        OrderTotals(BigDecimal subtotal, BigDecimal taxAmount, BigDecimal shippingCost, 
                   BigDecimal discountAmount, BigDecimal totalAmount) {
            this.subtotal = subtotal;
            this.taxAmount = taxAmount;
            this.shippingCost = shippingCost;
            this.discountAmount = discountAmount;
            this.totalAmount = totalAmount;
        }
    }
}
