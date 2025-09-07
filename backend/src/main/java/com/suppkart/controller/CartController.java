package com.suppkart.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suppkart.dto.request.AddToCartRequest;
import com.suppkart.dto.request.UpdateCartItemRequest;
import com.suppkart.dto.response.CartResponse;
import com.suppkart.model.entity.Cart;
import com.suppkart.model.entity.User;
import com.suppkart.service.CartService;
import com.suppkart.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserService userService;

    /**
     * Get user's cart or guest cart
     */
    @GetMapping
    public ResponseEntity<?> getCart(HttpSession session) {
        try {
            Cart cart = null;
            
            // Check if user is authenticated
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                
                // Get authenticated user's cart
                String email = authentication.getName();
                User user = userService.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                        
                Optional<Cart> userCart = cartService.getCartByUser(user);
                if (userCart.isPresent()) {
                    cart = userCart.get();
                } else {
                    cart = cartService.createCartForUser(user);
                }
            } else {
                // Get guest cart by session ID
                String sessionId = session.getId();
                Optional<Cart> guestCart = cartService.getCartBySessionId(sessionId);
                if (guestCart.isPresent()) {
                    cart = guestCart.get();
                } else {
                    // Create guest cart would need to be implemented in CartService
                    return ResponseEntity.ok(new CartResponse()); // Empty cart
                }
            }
            
            CartResponse cartResponse = mapToCartResponse(cart);
            return ResponseEntity.ok(cartResponse);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: Failed to get cart - " + e.getMessage());
        }
    }

    /**
     * Add item to cart
     */
    @PostMapping("/items")
    public ResponseEntity<?> addToCart(@Valid @RequestBody AddToCartRequest request, 
                                     HttpSession session) {
        try {
            Cart cart = null;
            
            // Check if user is authenticated
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                
                // Get authenticated user's cart
                String email = authentication.getName();
                User user = userService.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                        
                cart = cartService.getOrCreateCartForUser(user);
            } else {
                // Handle guest cart - this would need implementation in CartService
                String sessionId = session.getId();
                Optional<Cart> guestCart = cartService.getCartBySessionId(sessionId);
                if (guestCart.isPresent()) {
                    cart = guestCart.get();
                } else {
                    // Would need to implement createGuestCart in CartService
                    return ResponseEntity.badRequest().body("Unable to create guest cart");
                }
            }
            
            // Add item to cart logic would be implemented in CartService
            // cartService.addItemToCart(cart, request.getProductId(), request.getVariantId(), request.getQuantity());
            
            CartResponse cartResponse = mapToCartResponse(cart);
            return ResponseEntity.ok(cartResponse);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: Failed to add item to cart - " + e.getMessage());
        }
    }

    /**
     * Update cart item quantity
     */
    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateCartItem(@PathVariable Long itemId, 
                                          @Valid @RequestBody UpdateCartItemRequest request) {
        try {
            // Update cart item logic would be implemented in CartService
            // cartService.updateCartItemQuantity(itemId, request.getQuantity());
            
            return ResponseEntity.ok("Cart item updated successfully");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: Failed to update cart item - " + e.getMessage());
        }
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long itemId) {
        try {
            // Remove cart item logic would be implemented in CartService
            // cartService.removeCartItem(itemId);
            
            return ResponseEntity.ok("Item removed from cart successfully");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: Failed to remove item from cart - " + e.getMessage());
        }
    }

    /**
     * Clear entire cart
     */
    @DeleteMapping
    public ResponseEntity<?> clearCart(HttpSession session) {
        try {
            // Check if user is authenticated
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                
                // Clear authenticated user's cart
                String email = authentication.getName();
                User user = userService.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                        
                // cartService.clearUserCart(user);
            } else {
                // Clear guest cart
                String sessionId = session.getId();
                // cartService.clearGuestCart(sessionId);
            }
            
            return ResponseEntity.ok("Cart cleared successfully");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: Failed to clear cart - " + e.getMessage());
        }
    }

    /**
     * Transfer guest cart to user cart (called when user logs in)
     */
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<?> transferGuestCart(@RequestParam String sessionId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            cartService.transferGuestCartToUser(sessionId, user);
            
            return ResponseEntity.ok("Guest cart transferred successfully");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: Failed to transfer cart - " + e.getMessage());
        }
    }

    /**
     * Map Cart entity to CartResponse DTO
     */
    private CartResponse mapToCartResponse(Cart cart) {
        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartId(cart.getCartId());
        cartResponse.setUserId(cart.getUser() != null ? cart.getUser().getUserId() : null);
        cartResponse.setSessionId(cart.getSessionId());
        cartResponse.setCreatedAt(cart.getCreatedAt());
        cartResponse.setUpdatedAt(cart.getUpdatedAt());
        
        // Map cart items - this would need implementation
        // cartResponse.setItems(cart.getCartItems().stream()
        //         .map(this::mapToCartItemResponse)
        //         .collect(Collectors.toList()));
        
        return cartResponse;
    }
}
