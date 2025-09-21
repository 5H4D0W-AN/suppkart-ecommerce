-- Create reviews table for product reviews
CREATE TABLE reviews (
    review_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(100),
    content TEXT,
    display_name VARCHAR(50),
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    approved BOOLEAN NOT NULL DEFAULT FALSE,
    is_visible BOOLEAN NOT NULL DEFAULT TRUE,
    helpful_votes INT NOT NULL DEFAULT 0,
    created_by_admin BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Indexes for performance
    INDEX idx_reviews_product_id (product_id),
    INDEX idx_reviews_user_id (user_id),
    INDEX idx_reviews_approved_visible (approved, is_visible),
    INDEX idx_reviews_rating (rating),
    INDEX idx_reviews_created_at (created_at),
    INDEX idx_reviews_helpful_votes (helpful_votes)
);

-- Add review-related columns to products table if they don't exist
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS avg_rating DECIMAL(3,2) DEFAULT 0.00,
ADD COLUMN IF NOT EXISTS review_count INT DEFAULT 0;

-- Create indexes on products for review-related columns
CREATE INDEX IF NOT EXISTS idx_products_avg_rating ON products(avg_rating);
CREATE INDEX IF NOT EXISTS idx_products_review_count ON products(review_count);

-- Add unique constraint to prevent duplicate reviews from same user for same product
CREATE UNIQUE INDEX idx_reviews_unique_user_product ON reviews(user_id, product_id) WHERE user_id IS NOT NULL;
