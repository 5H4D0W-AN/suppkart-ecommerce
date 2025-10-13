-- Add new columns to products table
ALTER TABLE products 
ADD COLUMN cod_eligible BOOLEAN NOT NULL DEFAULT true,
ADD COLUMN auto_generate_seo BOOLEAN NOT NULL DEFAULT true;

-- Add new columns to product_variants table
ALTER TABLE product_variants 
ADD COLUMN discount_start_date TIMESTAMP NULL,
ADD COLUMN discount_end_date TIMESTAMP NULL,
ADD COLUMN discount_reason VARCHAR(50) NULL,
ADD COLUMN cod_eligible BOOLEAN NOT NULL DEFAULT true,
ADD COLUMN meta_title VARCHAR(255) NULL,
ADD COLUMN meta_description VARCHAR(500) NULL,
ADD COLUMN meta_keywords VARCHAR(255) NULL,
ADD COLUMN barcode VARCHAR(100) NULL;

-- Modify existing product_images table to support variant images
ALTER TABLE product_images 
ADD COLUMN variant_id BIGINT NULL,
ADD COLUMN media_type VARCHAR(20) DEFAULT 'IMAGE';

-- Add foreign key constraint for variant_id
ALTER TABLE product_images 
ADD CONSTRAINT fk_product_images_variant 
FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id) ON DELETE CASCADE;

-- Make product_id nullable since images can belong to variants
ALTER TABLE product_images 
MODIFY COLUMN product_id BIGINT NULL;

-- Add constraint to ensure image belongs to either product or variant, not both
ALTER TABLE product_images 
ADD CONSTRAINT chk_image_owner CHECK (
    (product_id IS NOT NULL AND variant_id IS NULL) OR 
    (product_id IS NULL AND variant_id IS NOT NULL)
);

-- Add indexes for performance
CREATE INDEX idx_product_images_variant_id ON product_images(variant_id);
CREATE INDEX idx_product_images_sort_order ON product_images(variant_id, sort_order);
CREATE INDEX idx_products_cod_eligible ON products(cod_eligible);
CREATE INDEX idx_variants_cod_eligible ON product_variants(cod_eligible);
CREATE INDEX idx_variants_discount_dates ON product_variants(discount_start_date, discount_end_date);

-- Ensure existing products have at least one variant
-- This creates a default variant for products that don't have any variants
INSERT INTO product_variants (product_id, sku, name, price, stock_quantity, is_active, is_default, created_at, updated_at)
SELECT 
    p.product_id,
    CONCAT(p.sku, '-DEFAULT'),
    CONCAT(p.name, ' - Default'),
    0.00, -- Default price, should be updated manually
    0,    -- Default stock
    true,
    true,
    NOW(),
    NOW()
FROM products p
WHERE p.product_id NOT IN (SELECT DISTINCT product_id FROM product_variants WHERE product_id IS NOT NULL);

-- Update discount percentage constraint to enforce 40% maximum
ALTER TABLE product_variants 
ADD CONSTRAINT chk_discount_percentage CHECK (discount_percentage <= 40.0);

-- Move barcode from products to variants (if barcode column exists in products)
-- First, migrate existing barcodes to default variants
UPDATE product_variants pv 
SET barcode = (
    SELECT p.barcode 
    FROM products p 
    WHERE p.product_id = pv.product_id 
    AND p.barcode IS NOT NULL
)
WHERE pv.is_default = true;

-- Remove barcode column from products table (if it exists)
-- ALTER TABLE products DROP COLUMN IF EXISTS barcode;