-- V3__create_wishlist_tables.sql
-- Migration to create wishlist and wishlist_items tables

-- Create wishlist table
CREATE TABLE wishlist (
    wishlist_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL DEFAULT 'My Wishlist',
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- Index for faster lookups
    INDEX idx_wishlist_user_id (user_id),
    INDEX idx_wishlist_created_at (created_at),
    INDEX idx_wishlist_updated_at (updated_at)
);

-- Create wishlist_items table
CREATE TABLE wishlist_items (
    wishlist_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wishlist_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    variant_id BIGINT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    FOREIGN KEY (wishlist_id) REFERENCES wishlist(wishlist_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate items in the same wishlist
    UNIQUE KEY uk_wishlist_product_variant (wishlist_id, product_id, variant_id),
    
    -- Indexes for faster lookups
    INDEX idx_wishlist_items_wishlist_id (wishlist_id),
    INDEX idx_wishlist_items_product_id (product_id),
    INDEX idx_wishlist_items_variant_id (variant_id),
    INDEX idx_wishlist_items_added_at (added_at)
);

-- Add some sample data (optional - can be removed in production)
-- INSERT INTO wishlist (user_id, name, is_public) VALUES 
-- (1, 'My Wishlist', false),
-- (2, 'Public Wishlist', true);

-- Add indexes for better query performance
CREATE INDEX idx_wishlist_public ON wishlist(is_public);
CREATE INDEX idx_wishlist_user_updated ON wishlist(user_id, updated_at);
CREATE INDEX idx_wishlist_items_product_added ON wishlist_items(product_id, added_at);
