package com.suppkart.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suppkart.dto.request.CheckoutRequest;
import com.suppkart.dto.request.PaymentRequest;
import com.suppkart.dto.response.PaymentResponse;
import com.suppkart.exception.BusinessException;
import com.suppkart.integration.payment.PaymentService;
import com.suppkart.model.entity.Address;
import com.suppkart.model.entity.Cart;
import com.suppkart.model.entity.CartItem;
import com.suppkart.model.entity.Order;
import com.suppkart.model.entity.OrderItem;
import com.suppkart.model.entity.ProductVariant;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.OrderStatus;
import com.suppkart.model.enums.PaymentMethod;
import com.suppkart.model.enums.PaymentStatus;
import com.suppkart.repository.OrderRepository;
import com.suppkart.repository.ProductVariantRepository;

@Service
@Transactional
public class CheckoutService {
    
    private static final Logger logger = LoggerFactory.getLogger(CheckoutService.class);
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private AddressService addressService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private List<PaymentService> paymentServices;
    
    @Autowired
    private ProductVariantRepository productVariantRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private EmailNotificationService emailNotificationService;
    
    /**
     * Process complete checkout flow
     */
    public PaymentResponse processCheckout(CheckoutRequest checkoutRequest, User user) {
        logger.info("Starting checkout process for user: {}", user.getEmail());
        
        try {
            // Step 1: Validate cart
            Cart cart = validateCart(user);
            
            // Step 2: Verify stock availability
            verifyStockAvailability(cart);
            
            // Step 3: Validate shipping address
            Address shippingAddress = validateShippingAddress(checkoutRequest.getShippingAddressId(), user);
            
            // Step 4: Calculate order totals
            OrderTotals totals = calculateOrderTotals(cart, shippingAddress);
            
            // Step 5: Create order
            Order order = createOrder(cart, shippingAddress, totals, checkoutRequest, user);
            
            // Step 6: Process payment
            PaymentResponse paymentResponse = processPayment(order, checkoutRequest.getPaymentMethod());
            
            // Step 7: Handle payment result
            if (paymentResponse.isSuccess()) {
                handleSuccessfulPayment(order, paymentResponse);
            } else {
                handleFailedPayment(order, paymentResponse);
            }
            
            logger.info("Checkout process completed for order: {}", order.getOrderNumber());
            return paymentResponse;
            
        } catch (Exception e) {
            logger.error("Checkout process failed for user: {}", user.getEmail(), e);
            throw new BusinessException("CHECKOUT_FAILED", "Checkout process failed: " + e.getMessage());
        }
    }
    
    /**
     * Validate user's cart
     */
    private Cart validateCart(User user) {
        Optional<Cart> cartOpt = cartService.getCartByUser(user);
        if (cartOpt.isEmpty()) {
            throw new BusinessException("CART_NOT_FOUND", "No active cart found for user");
        }
        
        Cart cart = cartOpt.get();
        if (cart.getItems().isEmpty()) {
            throw new BusinessException("CART_EMPTY", "Cannot checkout with empty cart");
        }
        
        return cart;
    }
    
    /**
     * Verify stock availability for all cart items
     */
    private void verifyStockAvailability(Cart cart) {
        for (CartItem item : cart.getItems()) {
            ProductVariant variant = item.getVariant();
            
            // TODO: Implement proper stock management with inventory tracking
            // For now, we'll assume items are in stock
            if (variant != null && variant.getStockQuantity() < item.getQuantity()) {
                throw new BusinessException("INSUFFICIENT_STOCK", 
                    "Insufficient stock for product: " + variant.getProduct().getName());
            }
        }
    }
    
    /**
     * Validate shipping address
     */
    private Address validateShippingAddress(Long addressId, User user) {
        Optional<Address> addressOpt = addressService.getAddressById(addressId, user);
        if (addressOpt.isEmpty()) {
            throw new BusinessException("ADDRESS_NOT_FOUND", "Shipping address not found");
        }
        return addressOpt.get();
    }
    
    /**
     * Calculate order totals
     */
    private OrderTotals calculateOrderTotals(Cart cart, Address shippingAddress) {
        BigDecimal subtotal = cart.getItems().stream()
            .map(item -> {
                BigDecimal price = item.getVariant() != null ? 
                    item.getVariant().getPrice() : 
                    item.getUnitPrice(); // Use unit price from cart item
                return price.multiply(BigDecimal.valueOf(item.getQuantity()));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // TODO: Implement dynamic tax calculation based on address
        BigDecimal taxRate = new BigDecimal("0.18"); // 18% GST
        BigDecimal taxes = subtotal.multiply(taxRate);
        
        // TODO: Implement dynamic shipping cost calculation
        BigDecimal shippingCost = calculateShippingCost(subtotal, shippingAddress);
        
        BigDecimal totalAmount = subtotal.add(taxes).add(shippingCost);
        
        return new OrderTotals(subtotal, taxes, shippingCost, totalAmount);
    }
    
    /**
     * Calculate shipping cost
     */
    private BigDecimal calculateShippingCost(BigDecimal subtotal, Address address) {
        // TODO: Integrate with shipping provider API for accurate rates
        // Free shipping for orders above threshold
        BigDecimal freeShippingThreshold = new BigDecimal("499");
        if (subtotal.compareTo(freeShippingThreshold) >= 0) {
            return BigDecimal.ZERO;
        }
        
        // Standard shipping rate
        return new BigDecimal("50");
    }
    
    /**
     * Create order from cart
     */
    private Order createOrder(Cart cart, Address shippingAddress, OrderTotals totals, 
                             CheckoutRequest request, User user) {
        
        Order order = new Order();
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PENDING);
        order.copyShippingFromAddress(shippingAddress);
        order.setSubtotal(totals.subtotal);
        order.setTaxAmount(totals.taxes);
        order.setShippingCost(totals.shippingCost);
        order.setTotalAmount(totals.totalAmount);
        order.setDeliveryInstructions(request.getDeliveryInstructions());
        
        // Generate order number
        order.setOrderNumber(generateOrderNumber());
        
        // Convert cart items to order items
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setProductVariant(cartItem.getVariant());
            orderItem.setQuantity(cartItem.getQuantity());
            BigDecimal price = cartItem.getVariant() != null ? 
                cartItem.getVariant().getPrice() : 
                cartItem.getUnitPrice(); // Use unit price from cart item
            orderItem.setUnitPrice(price);
            order.addOrderItem(orderItem);
        }
        
        return orderRepository.save(order);
    }
    
    /**
     * Generate unique order number
     */
    private String generateOrderNumber() {
        // TODO: Implement more sophisticated order number generation
        return "ORD" + System.currentTimeMillis();
    }
    
    /**
     * Process payment for order
     */
    private PaymentResponse processPayment(Order order, PaymentMethod paymentMethod) {
        PaymentService paymentService = getPaymentService(paymentMethod);
        
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(order.getOrderNumber());
        paymentRequest.setAmount(order.getTotalAmount());
        paymentRequest.setPaymentMethod(paymentMethod);
        paymentRequest.setCurrency("INR");
        
        return paymentService.initializePayment(order, paymentRequest);
    }
    
    /**
     * Get payment service for method
     */
    private PaymentService getPaymentService(PaymentMethod paymentMethod) {
        return paymentServices.stream()
            .filter(service -> service.supports(paymentMethod))
            .findFirst()
            .orElseThrow(() -> new BusinessException("PAYMENT_METHOD_NOT_SUPPORTED", 
                "Payment method not supported: " + paymentMethod));
    }
    
    /**
     * Handle successful payment
     */
    private void handleSuccessfulPayment(Order order, PaymentResponse paymentResponse) {
        // Update order with payment details
        order.setPaymentMethod(paymentResponse.getPaymentMethod());
        order.setPaymentTransactionId(paymentResponse.getTransactionId());
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        
        // Update order status based on payment method
        if (paymentResponse.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
        } else {
            order.setOrderStatus(OrderStatus.PROCESSING);
        }
        
        orderRepository.save(order);
        
        // Clear user's cart
        cartService.clearUserCart(order.getUser());
        
        // Send order confirmation email
        emailNotificationService.sendOrderConfirmation(order);
        
        // TODO: Update product stock quantities
        updateProductStock(order);
    }
    
    /**
     * Handle failed payment
     */
    private void handleFailedPayment(Order order, PaymentResponse paymentResponse) {
        order.setPaymentStatus(PaymentStatus.FAILED);
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        
        logger.warn("Payment failed for order: {} - {}", 
            order.getOrderNumber(), paymentResponse.getMessage());
    }
    
    /**
     * Update product stock after successful order
     */
    private void updateProductStock(Order order) {
        // TODO: Implement proper inventory management
        for (OrderItem item : order.getOrderItems()) {
            ProductVariant variant = item.getProductVariant();
            if (variant != null) {
                int newStock = variant.getStockQuantity() - item.getQuantity();
                variant.setStockQuantity(Math.max(0, newStock));
                productVariantRepository.save(variant);
            }
        }
    }
    
    /**
     * Helper class for order totals calculation
     */
    private static class OrderTotals {
        final BigDecimal subtotal;
        final BigDecimal taxes;
        final BigDecimal shippingCost;
        final BigDecimal totalAmount;
        
        OrderTotals(BigDecimal subtotal, BigDecimal taxes, BigDecimal shippingCost, BigDecimal totalAmount) {
            this.subtotal = subtotal;
            this.taxes = taxes;
            this.shippingCost = shippingCost;
            this.totalAmount = totalAmount;
        }
    }
}
