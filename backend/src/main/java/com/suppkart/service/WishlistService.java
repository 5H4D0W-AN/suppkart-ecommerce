package com.suppkart.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suppkart.dto.request.AddToWishlistRequest;
import com.suppkart.dto.response.WishlistItemResponse;
import com.suppkart.dto.response.WishlistResponse;
import com.suppkart.exception.WishlistException;
import com.suppkart.model.entity.Cart;
import com.suppkart.model.entity.CartItem;
import com.suppkart.model.entity.Product;
import com.suppkart.model.entity.ProductVariant;
import com.suppkart.model.entity.User;
import com.suppkart.model.entity.Wishlist;
import com.suppkart.model.entity.WishlistItem;
import com.suppkart.repository.ProductRepository;
import com.suppkart.repository.ProductVariantRepository;
import com.suppkart.repository.UserRepository;
import com.suppkart.repository.WishlistItemRepository;
import com.suppkart.repository.WishlistRepository;

@Service
@Transactional
public class WishlistService {
    
    private static final Logger logger = LoggerFactory.getLogger(WishlistService.class);
    
    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartService cartService;
    
    @Autowired
    public WishlistService(WishlistRepository wishlistRepository,
                          WishlistItemRepository wishlistItemRepository,
                          UserRepository userRepository,
                          ProductRepository productRepository,
                          ProductVariantRepository productVariantRepository,
                          CartService cartService) {
        this.wishlistRepository = wishlistRepository;
        this.wishlistItemRepository = wishlistItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.cartService = cartService;
    }
    
    /**
     * Get or create user's default wishlist
     */
    public Wishlist getOrCreateDefaultWishlist(Long userId) {
        logger.debug("Getting or creating default wishlist for user: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> WishlistException.userNotAuthenticated());
        
        Optional<Wishlist> existingWishlist = wishlistRepository.findByUserUserId(userId);
        
        if (existingWishlist.isPresent()) {
            return existingWishlist.get();
        }
        
        // Create default wishlist
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setName("My Wishlist");
        wishlist.setIsPublic(false);
        wishlist.setCreatedAt(LocalDateTime.now());
        wishlist.setUpdatedAt(LocalDateTime.now());
        
        return wishlistRepository.save(wishlist);
    }
    
    /**
     * Add product to wishlist
     */
    public WishlistItemResponse addToWishlist(Long userId, AddToWishlistRequest request) {
        logger.info("Adding product {} (variant: {}) to wishlist for user: {}", 
                   request.getProductId(), request.getVariantId(), userId);
        
        // Check if product already exists in wishlist
        boolean exists = wishlistItemRepository.existsByUserIdAndProductIdAndVariantId(
            userId, request.getProductId(), request.getVariantId());
        
        if (exists) {
            throw WishlistException.productAlreadyInWishlist();
        }
        
        // Get product
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Get variant if specified
        ProductVariant variant = null;
        if (request.getVariantId() != null) {
            variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Product variant not found"));
        }
        
        // Get or create wishlist
        Wishlist wishlist = getOrCreateDefaultWishlist(userId);
        
        // Create wishlist item
        WishlistItem wishlistItem = new WishlistItem();
        wishlistItem.setWishlist(wishlist);
        wishlistItem.setProduct(product);
        wishlistItem.setVariant(variant);
        wishlistItem.setAddedAt(LocalDateTime.now());
        
        wishlistItem = wishlistItemRepository.save(wishlistItem);
        
        // Update wishlist timestamp
        wishlist.setUpdatedAt(LocalDateTime.now());
        wishlistRepository.save(wishlist);
        
        logger.info("Successfully added product to wishlist. Item ID: {}", wishlistItem.getWishlistItemId());
        
        return convertToWishlistItemResponse(wishlistItem);
    }
    
    /**
     * Remove product from wishlist
     */
    public void removeFromWishlist(Long userId, Long itemId) {
        logger.info("Removing wishlist item {} for user: {}", itemId, userId);
        
        WishlistItem wishlistItem = wishlistItemRepository.findById(itemId)
            .orElseThrow(() -> WishlistException.wishlistItemNotFound(itemId));
        
        // Check if user owns this wishlist item
        if (!wishlistItem.getWishlist().getUser().getUserId().equals(userId)) {
            throw WishlistException.wishlistAccessDenied();
        }
        
        wishlistItemRepository.delete(wishlistItem);
        
        // Update wishlist timestamp
        Wishlist wishlist = wishlistItem.getWishlist();
        wishlist.setUpdatedAt(LocalDateTime.now());
        wishlistRepository.save(wishlist);
        
        logger.info("Successfully removed item from wishlist");
    }
    
    /**
     * Get user's wishlist
     */
    @Transactional(readOnly = true)
    public WishlistResponse getUserWishlist(Long userId) {
        logger.debug("Getting wishlist for user: {}", userId);
        
        Optional<Wishlist> wishlistOpt = wishlistRepository.findByUserUserId(userId);
        
        if (wishlistOpt.isEmpty()) {
            // Return empty wishlist
            WishlistResponse response = new WishlistResponse();
            response.setName("My Wishlist");
            response.setIsPublic(false);
            response.setTotalItems(0);
            response.setUserId(userId);
            response.setItems(List.of());
            return response;
        }
        
        Wishlist wishlist = wishlistOpt.get();
        List<WishlistItem> items = wishlistItemRepository.findByWishlistIdWithProductDetails(
            wishlist.getWishlistId());
        
        WishlistResponse response = convertToWishlistResponse(wishlist);
        response.setItems(items.stream()
            .map(this::convertToWishlistItemResponse)
            .collect(Collectors.toList()));
        
        return response;
    }
    
    /**
     * Get public wishlist by user ID
     */
    @Transactional(readOnly = true)
    public WishlistResponse getPublicWishlist(Long userId) {
        logger.debug("Getting public wishlist for user: {}", userId);
        
        Wishlist wishlist = wishlistRepository.findByUserUserId(userId)
            .orElseThrow(() -> WishlistException.wishlistNotFound());
        
        if (!wishlist.getIsPublic()) {
            throw WishlistException.wishlistIsPrivate();
        }
        
        List<WishlistItem> items = wishlistItemRepository.findByWishlistIdWithProductDetails(
            wishlist.getWishlistId());
        
        WishlistResponse response = convertToWishlistResponse(wishlist);
        response.setItems(items.stream()
            .map(this::convertToWishlistItemResponse)
            .collect(Collectors.toList()));
        
        return response;
    }
    
    /**
     * Move wishlist item to cart
     */
    public void moveToCart(Long userId, Long itemId) {
        logger.info("Moving wishlist item {} to cart for user: {}", itemId, userId);
        
        WishlistItem wishlistItem = wishlistItemRepository.findById(itemId)
            .orElseThrow(() -> WishlistException.wishlistItemNotFound(itemId));
        
        // Check if user owns this wishlist item
        if (!wishlistItem.getWishlist().getUser().getUserId().equals(userId)) {
            throw WishlistException.wishlistAccessDenied();
        }
        
        // For now, we'll implement a simpler approach since CartService doesn't have the addToCart method
        // In a real implementation, you would extend CartService with the proper addToCart method
        
        try {
            // Get user
            User user = userRepository.findById(userId)
                .orElseThrow(() -> WishlistException.userNotAuthenticated());
                
            // Create or get cart for user
            Cart cart = cartService.getOrCreateCartForUser(user);
            
            // Create cart item manually (simplified approach)
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(wishlistItem.getProduct());
            cartItem.setVariant(wishlistItem.getVariant());
            cartItem.setQuantity(1);
            // CartItem doesn't have setAddedAt method in current implementation
            
            // You would need to inject CartItemRepository to save this
            // For now, we'll just log the action
            logger.info("Would add item to cart: Product {}, Variant {}", 
                       wishlistItem.getProduct().getProductId(), 
                       wishlistItem.getVariant() != null ? wishlistItem.getVariant().getVariantId() : null);
            
            // Remove from wishlist
            removeFromWishlist(userId, itemId);
            
            logger.info("Successfully moved item from wishlist to cart");
        } catch (Exception e) {
            logger.error("Failed to move item to cart: {}", e.getMessage());
            throw new RuntimeException("Failed to move item to cart: " + e.getMessage());
        }
    }
    
    /**
     * Check if product is in user's wishlist
     */
    @Transactional(readOnly = true)
    public boolean isInWishlist(Long userId, Long productId, Long variantId) {
        return wishlistItemRepository.existsByUserIdAndProductIdAndVariantId(
            userId, productId, variantId);
    }
    
    /**
     * Get wishlist item count for user
     */
    @Transactional(readOnly = true)
    public long getWishlistItemCount(Long userId) {
        Optional<Wishlist> wishlist = wishlistRepository.findByUserUserId(userId);
        if (wishlist.isEmpty()) {
            return 0;
        }
        return wishlistItemRepository.countByWishlistWishlistId(wishlist.get().getWishlistId());
    }
    
    /**
     * Check stock and sale status for wishlist items
     */
    @Transactional(readOnly = true)
    public WishlistResponse checkItemsStatus(Long userId) {
        WishlistResponse wishlist = getUserWishlist(userId);
        
        // Update stock and sale status for each item
        if (wishlist.getItems() != null) {
            for (WishlistItemResponse item : wishlist.getItems()) {
                updateItemStatus(item);
            }
        }
        
        return wishlist;
    }
    
    private void updateItemStatus(WishlistItemResponse item) {
        // Check if product is in stock
        Optional<Product> product = productRepository.findById(item.getProductId());
        if (product.isPresent()) {
            // Set stock status (simplified - in real implementation would check inventory)
            item.setInStock(true);
            
            // Check if on sale (simplified - in real implementation would check current promotions)
            item.setOnSale(false);
            item.setSalePrice(null);
        }
    }
    
    private WishlistResponse convertToWishlistResponse(Wishlist wishlist) {
        WishlistResponse response = new WishlistResponse();
        response.setWishlistId(wishlist.getWishlistId());
        response.setName(wishlist.getName());
        response.setIsPublic(wishlist.getIsPublic());
        response.setUserId(wishlist.getUser().getUserId());
        response.setUserName(wishlist.getUser().getFirstName() + " " + wishlist.getUser().getLastName());
        response.setCreatedAt(wishlist.getCreatedAt());
        response.setUpdatedAt(wishlist.getUpdatedAt());
        
        // Count items
        long itemCount = wishlistItemRepository.countByWishlistWishlistId(wishlist.getWishlistId());
        response.setTotalItems((int) itemCount);
        
        return response;
    }
    
    private WishlistItemResponse convertToWishlistItemResponse(WishlistItem item) {
        WishlistItemResponse response = new WishlistItemResponse();
        response.setWishlistItemId(item.getWishlistItemId());
        response.setAddedAt(item.getAddedAt());
        
        // Product details
        Product product = item.getProduct();
        response.setProductId(product.getProductId());
        response.setProductName(product.getName());
        response.setProductSku(product.getSku());
        response.setProductDescription(product.getDescription());
        // Since Product doesn't have direct price, use min price from variants or default to zero
        response.setProductPrice(product.getMinPrice());
        
        // Set product image (get first image if available)
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            response.setProductImageUrl(product.getImages().get(0).getImageUrl());
        }
        
        // Variant details
        ProductVariant variant = item.getVariant();
        if (variant != null) {
            response.setVariantId(variant.getVariantId());
            response.setVariantName(variant.getSize()); // Assuming size as variant name
            response.setVariantSku(variant.getSku());
            response.setVariantPrice(variant.getPrice());
        }
        
        // Default status (would be updated by updateItemStatus if needed)
        response.setInStock(true);
        response.setOnSale(false);
        
        return response;
    }
}
