package com.suppkart.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suppkart.dto.request.AddToWishlistRequest;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.dto.response.WishlistItemResponse;
import com.suppkart.dto.response.WishlistResponse;
import com.suppkart.model.entity.User;
import com.suppkart.repository.UserRepository;
import com.suppkart.service.WishlistService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {
    
    private static final Logger logger = LoggerFactory.getLogger(WishlistController.class);
    
    private final WishlistService wishlistService;
    private final UserRepository userRepository;
    
    @Autowired
    public WishlistController(WishlistService wishlistService, UserRepository userRepository) {
        this.wishlistService = wishlistService;
        this.userRepository = userRepository;
    }
    
    private Long getCurrentUserId(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getUserId();
    }
    
    /**
     * Get current user's wishlist
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<WishlistResponse>> getCurrentUserWishlist(Authentication authentication) {
        try {
            logger.info("Getting wishlist for current user");
            
            Long userId = getCurrentUserId(authentication);
            WishlistResponse wishlist = wishlistService.getUserWishlist(userId);
            
            logger.info("Successfully retrieved wishlist with {} items for user: {}", 
                       wishlist.getTotalItems(), userId);
            
            return ResponseEntity.ok(ApiResponse.success("Wishlist retrieved successfully", wishlist));
            
        } catch (Exception e) {
            logger.error("Error getting user wishlist: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve wishlist: " + e.getMessage()));
        }
    }
    
    /**
     * Add item to current user's wishlist
     */
    @PostMapping("/items")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<WishlistItemResponse>> addToWishlist(
            @Valid @RequestBody AddToWishlistRequest request,
            Authentication authentication) {
        try {
            logger.info("Adding item to wishlist: Product ID {}, Variant ID {}", 
                       request.getProductId(), request.getVariantId());
            
            Long userId = getCurrentUserId(authentication);
            WishlistItemResponse item = wishlistService.addToWishlist(userId, request);
            
            logger.info("Successfully added item to wishlist: {}", item.getWishlistItemId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to wishlist successfully", item));
            
        } catch (RuntimeException e) {
            logger.error("Error adding item to wishlist: {}", e.getMessage());
            
            // Handle specific business exceptions
            if (e.getMessage().contains("already in wishlist")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Product is already in your wishlist"));
            } else if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Product or variant not found"));
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to add item to wishlist: " + e.getMessage()));
        }
    }
    
    /**
     * Remove item from current user's wishlist
     */
    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> removeFromWishlist(
            @PathVariable Long itemId,
            Authentication authentication) {
        try {
            logger.info("Removing item from wishlist: {}", itemId);
            
            Long userId = getCurrentUserId(authentication);
            wishlistService.removeFromWishlist(userId, itemId);
            
            logger.info("Successfully removed item from wishlist: {}", itemId);
            
            return ResponseEntity.ok(ApiResponse.success("Item removed from wishlist successfully", ""));
            
        } catch (RuntimeException e) {
            logger.error("Error removing item from wishlist: {}", e.getMessage());
            
            // Handle specific business exceptions
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Wishlist item not found"));
            } else if (e.getMessage().contains("access denied") || e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You don't have permission to remove this item"));
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to remove item from wishlist: " + e.getMessage()));
        }
    }
    
    /**
     * Move wishlist item to cart
     */
    @PostMapping("/items/{itemId}/move-to-cart")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> moveToCart(
            @PathVariable Long itemId,
            Authentication authentication) {
        try {
            logger.info("Moving wishlist item to cart: {}", itemId);
            
            Long userId = getCurrentUserId(authentication);
            wishlistService.moveToCart(userId, itemId);
            
            logger.info("Successfully moved item to cart: {}", itemId);
            
            return ResponseEntity.ok(ApiResponse.success("Item moved to cart successfully", ""));
            
        } catch (RuntimeException e) {
            logger.error("Error moving item to cart: {}", e.getMessage());
            
            // Handle specific business exceptions
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Wishlist item not found"));
            } else if (e.getMessage().contains("access denied") || e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You don't have permission to move this item"));
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to move item to cart: " + e.getMessage()));
        }
    }
    
    /**
     * Get public wishlist by user ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<WishlistResponse>> getPublicWishlist(@PathVariable Long userId) {
        try {
            logger.info("Getting public wishlist for user: {}", userId);
            
            WishlistResponse wishlist = wishlistService.getPublicWishlist(userId);
            
            logger.info("Successfully retrieved public wishlist with {} items for user: {}", 
                       wishlist.getTotalItems(), userId);
            
            return ResponseEntity.ok(ApiResponse.success("Public wishlist retrieved successfully", wishlist));
            
        } catch (RuntimeException e) {
            logger.error("Error getting public wishlist: {}", e.getMessage());
            
            // Handle specific business exceptions
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User or wishlist not found"));
            } else if (e.getMessage().contains("private")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("This wishlist is private"));
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve public wishlist: " + e.getMessage()));
        }
    }
    
    /**
     * Check if product is in user's wishlist
     */
    @GetMapping("/check/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Boolean>> isInWishlist(
            @PathVariable Long productId,
            @RequestParam(required = false) Long variantId,
            Authentication authentication) {
        try {
            logger.debug("Checking if product {} (variant: {}) is in wishlist", productId, variantId);
            
            Long userId = getCurrentUserId(authentication);
            boolean inWishlist = wishlistService.isInWishlist(userId, productId, variantId);
            
            return ResponseEntity.ok(ApiResponse.success("Check completed successfully", inWishlist));
            
        } catch (Exception e) {
            logger.error("Error checking wishlist status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to check wishlist status: " + e.getMessage()));
        }
    }
    
    /**
     * Get wishlist item count for current user
     */
    @GetMapping("/count")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Long>> getWishlistItemCount(Authentication authentication) {
        try {
            logger.debug("Getting wishlist item count");
            
            Long userId = getCurrentUserId(authentication);
            long count = wishlistService.getWishlistItemCount(userId);
            
            return ResponseEntity.ok(ApiResponse.success("Count retrieved successfully", count));
            
        } catch (Exception e) {
            logger.error("Error getting wishlist count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get wishlist count: " + e.getMessage()));
        }
    }
    
    /**
     * Check wishlist items status (stock and sales)
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<WishlistResponse>> checkItemsStatus(Authentication authentication) {
        try {
            logger.info("Checking wishlist items status");
            
            Long userId = getCurrentUserId(authentication);
            WishlistResponse wishlist = wishlistService.checkItemsStatus(userId);
            
            logger.info("Successfully checked status for {} wishlist items", wishlist.getTotalItems());
            
            return ResponseEntity.ok(ApiResponse.success("Status checked successfully", wishlist));
            
        } catch (Exception e) {
            logger.error("Error checking wishlist items status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to check items status: " + e.getMessage()));
        }
    }
}
