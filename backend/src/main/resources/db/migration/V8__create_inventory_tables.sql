-- Create inventory management tables
-- Migration V8: Inventory Management System

-- Create inventory table
CREATE TABLE inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    variant_id BIGINT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    low_stock_threshold INTEGER NOT NULL DEFAULT 5,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_inventory_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate inventory records
    CONSTRAINT uk_inventory_product_variant UNIQUE (product_id, variant_id),
    
    -- Check constraints
    CONSTRAINT chk_inventory_quantity CHECK (quantity >= 0),
    CONSTRAINT chk_inventory_threshold CHECK (low_stock_threshold >= 0)
);

-- Create inventory history table
CREATE TABLE inventory_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    variant_id BIGINT NULL,
    previous_quantity INTEGER NOT NULL,
    new_quantity INTEGER NOT NULL,
    change_type ENUM('STOCK_ADJUSTMENT', 'PURCHASE', 'SALE', 'RETURN') NOT NULL,
    reason VARCHAR(500) NULL,
    reference_number VARCHAR(100) NULL,
    supplier_name VARCHAR(255) NULL,
    customer_name VARCHAR(255) NULL,
    unit_cost DECIMAL(10,2) NULL,
    unit_price DECIMAL(10,2) NULL,
    updated_by BIGINT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_inventory_history_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_inventory_history_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE,
    CONSTRAINT fk_inventory_history_user FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE RESTRICT,
    
    -- Check constraints
    CONSTRAINT chk_inventory_history_quantities CHECK (previous_quantity >= 0 AND new_quantity >= 0),
    CONSTRAINT chk_inventory_history_costs CHECK (unit_cost IS NULL OR unit_cost >= 0),
    CONSTRAINT chk_inventory_history_prices CHECK (unit_price IS NULL OR unit_price >= 0)
);

-- Create stock alerts table
CREATE TABLE stock_alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    variant_id BIGINT NULL,
    alert_type ENUM('LOW_STOCK', 'OUT_OF_STOCK') NOT NULL,
    threshold_value INTEGER NOT NULL,
    current_stock INTEGER NOT NULL,
    is_resolved BOOLEAN NOT NULL DEFAULT FALSE,
    notification_sent BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_stock_alerts_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_alerts_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE,
    
    -- Check constraints
    CONSTRAINT chk_stock_alerts_threshold CHECK (threshold_value >= 0),
    CONSTRAINT chk_stock_alerts_current_stock CHECK (current_stock >= 0)
);

-- Create indexes for better performance
CREATE INDEX idx_inventory_product_id ON inventory(product_id);
CREATE INDEX idx_inventory_variant_id ON inventory(variant_id);
CREATE INDEX idx_inventory_quantity ON inventory(quantity);
CREATE INDEX idx_inventory_low_stock_threshold ON inventory(low_stock_threshold);
CREATE INDEX idx_inventory_last_updated ON inventory(last_updated);

CREATE INDEX idx_inventory_history_product_id ON inventory_history(product_id);
CREATE INDEX idx_inventory_history_variant_id ON inventory_history(variant_id);
CREATE INDEX idx_inventory_history_change_type ON inventory_history(change_type);
CREATE INDEX idx_inventory_history_updated_at ON inventory_history(updated_at);
CREATE INDEX idx_inventory_history_updated_by ON inventory_history(updated_by);

CREATE INDEX idx_stock_alerts_product_id ON stock_alerts(product_id);
CREATE INDEX idx_stock_alerts_variant_id ON stock_alerts(variant_id);
CREATE INDEX idx_stock_alerts_alert_type ON stock_alerts(alert_type);
CREATE INDEX idx_stock_alerts_is_resolved ON stock_alerts(is_resolved);
CREATE INDEX idx_stock_alerts_created_at ON stock_alerts(created_at);
CREATE INDEX idx_stock_alerts_notification_sent ON stock_alerts(notification_sent);

-- Create composite indexes for common queries
CREATE INDEX idx_inventory_low_stock_check ON inventory(quantity, low_stock_threshold);
CREATE INDEX idx_stock_alerts_unresolved ON stock_alerts(is_resolved, alert_type, created_at);
CREATE INDEX idx_inventory_history_product_variant_date ON inventory_history(product_id, variant_id, updated_at);

-- Insert sample data for testing (optional - can be removed in production)
-- This will create inventory records for existing products
INSERT INTO inventory (product_id, variant_id, quantity, low_stock_threshold)
SELECT 
    p.id as product_id,
    NULL as variant_id,
    FLOOR(RAND() * 100) + 10 as quantity,
    5 as low_stock_threshold
FROM products p
WHERE NOT EXISTS (
    SELECT 1 FROM inventory i 
    WHERE i.product_id = p.id AND i.variant_id IS NULL
)
LIMIT 50;

-- Insert inventory records for product variants
INSERT INTO inventory (product_id, variant_id, quantity, low_stock_threshold)
SELECT 
    pv.product_id,
    pv.id as variant_id,
    FLOOR(RAND() * 50) + 5 as quantity,
    3 as low_stock_threshold
FROM product_variants pv
WHERE NOT EXISTS (
    SELECT 1 FROM inventory i 
    WHERE i.product_id = pv.product_id AND i.variant_id = pv.id
)
LIMIT 100;

-- Create some sample stock alerts for low stock items
INSERT INTO stock_alerts (product_id, variant_id, alert_type, threshold_value, current_stock, is_resolved, notification_sent)
SELECT 
    i.product_id,
    i.variant_id,
    CASE 
        WHEN i.quantity = 0 THEN 'OUT_OF_STOCK'
        ELSE 'LOW_STOCK'
    END as alert_type,
    i.low_stock_threshold as threshold_value,
    i.quantity as current_stock,
    FALSE as is_resolved,
    FALSE as notification_sent
FROM inventory i
WHERE i.quantity <= i.low_stock_threshold
AND NOT EXISTS (
    SELECT 1 FROM stock_alerts sa 
    WHERE sa.product_id = i.product_id 
    AND sa.variant_id = i.variant_id 
    AND sa.is_resolved = FALSE
);

-- Add comments to tables for documentation
ALTER TABLE inventory COMMENT = 'Stores current inventory levels for products and variants';
ALTER TABLE inventory_history COMMENT = 'Tracks all inventory changes over time for audit purposes';
ALTER TABLE stock_alerts COMMENT = 'Manages stock alerts for low stock and out of stock situations';
