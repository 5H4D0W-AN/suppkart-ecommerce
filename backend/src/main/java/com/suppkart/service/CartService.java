package com.suppkart.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suppkart.model.entity.Cart;
import com.suppkart.model.entity.CartItem;
import com.suppkart.model.entity.User;
import com.suppkart.repository.CartItemRepository;
import com.suppkart.repository.CartRepository;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    /**
     * Create cart for user
     */
    public Cart createCartForUser(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    /**
     * Get or create cart for user
     */
    public Cart getOrCreateCartForUser(User user) {
        Optional<Cart> existingCart = cartRepository.findByUser(user);
        if (existingCart.isPresent()) {
            return existingCart.get();
        }
        return createCartForUser(user);
    }

    /**
     * Transfer guest cart to user cart
     */
    public void transferGuestCartToUser(String sessionId, User user) {
        Optional<Cart> guestCart = cartRepository.findBySessionId(sessionId);
        if (guestCart.isPresent()) {
            Cart userCart = getOrCreateCartForUser(user);
            
            // Transfer items from guest cart to user cart
            List<CartItem> guestItems = cartItemRepository.findByCart(guestCart.get());
            for (CartItem item : guestItems) {
                // Check if item already exists in user cart
                Optional<CartItem> existingItem;
                if (item.getVariant() != null) {
                    existingItem = cartItemRepository.findByCart_CartIdAndProduct_ProductIdAndVariant_VariantId(
                        userCart.getCartId(), item.getProduct().getProductId(), item.getVariant().getVariantId());
                } else {
                    existingItem = cartItemRepository.findByCart_CartIdAndProduct_ProductIdAndVariantIsNull(
                        userCart.getCartId(), item.getProduct().getProductId());
                }
                
                if (existingItem.isPresent()) {
                    // Update quantity
                    existingItem.get().setQuantity(
                        existingItem.get().getQuantity() + item.getQuantity());
                    cartItemRepository.save(existingItem.get());
                } else {
                    // Move item to user cart
                    item.setCart(userCart);
                    cartItemRepository.save(item);
                }
            }
            
            // Delete guest cart
            cartRepository.delete(guestCart.get());
            
            // Update user cart timestamp
            userCart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(userCart);
        }
    }

    /**
     * Get cart by user
     */
    public Optional<Cart> getCartByUser(User user) {
        return cartRepository.findByUser(user);
    }

    /**
     * Get cart by session ID
     */
    public Optional<Cart> getCartBySessionId(String sessionId) {
        return cartRepository.findBySessionId(sessionId);
    }

    /**
     * Clear user cart
     */
    public void clearUserCart(User user) {
        Optional<Cart> cartOpt = cartRepository.findByUser(user);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cartItemRepository.deleteByCart(cart);
            cartRepository.delete(cart);
        }
    }
    
    /**
     * Get cart by user (returns cart directly, not Optional)
     */
    public Cart getUserCart(User user) {
        return getCartByUser(user).orElse(null);
    }

    /**
     * Clear expired guest carts
     */
    @Transactional
    public void clearExpiredGuestCarts() {
        LocalDateTime expiredBefore = LocalDateTime.now().minusDays(7);
        List<Cart> expiredCarts = cartRepository.findByUserIsNullAndCreatedAtBefore(expiredBefore);
        
        for (Cart cart : expiredCarts) {
            cartItemRepository.deleteByCart(cart);
            cartRepository.delete(cart);
        }
    }
}
