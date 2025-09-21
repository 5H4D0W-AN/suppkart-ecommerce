-- V5__create_cart_and_order_tables.sql
-- Cart and order management tables

-- Create carts table
CREATE TABLE carts (
    cart_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    session_id VARCHAR(255) NULL UNIQUE,
    subtotal DECIMAL(10,2) DEFAULT 0.00,
    total_items INT NOT NULL DEFAULT 0,
    coupon_code VARCHAR(50),
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    INDEX idx_carts_user_id (user_id),
    INDEX idx_carts_session_id (session_id),
    INDEX idx_carts_expires_at (expires_at),
    INDEX idx_carts_coupon_code (coupon_code),
    INDEX idx_carts_created_at (created_at),
    
    -- Ensure either user_id or session_id is set (for user or guest carts)
    CONSTRAINT chk_cart_owner CHECK (
        (user_id IS NOT NULL AND session_id IS NULL) OR 
        (user_id IS NULL AND session_id IS NOT NULL)
    )
);

-- Create cart_items table
CREATE TABLE cart_items (
    cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    variant_id BIGINT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate items in same cart
    UNIQUE KEY uk_cart_product_variant (cart_id, product_id, variant_id),
    
    INDEX idx_cart_items_cart_id (cart_id),
    INDEX idx_cart_items_product_id (product_id),
    INDEX idx_cart_items_variant_id (variant_id),
    INDEX idx_cart_items_quantity (quantity),
    INDEX idx_cart_items_added_at (added_at),
    
    -- Ensure positive quantity
    CONSTRAINT chk_cart_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_cart_item_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_cart_item_total_price CHECK (total_price >= 0)
);

-- Create orders table
CREATE TABLE orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(20),
    
    -- Order amounts
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    shipping_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    
    -- Shipping information
    shipping_address_id BIGINT,
    billing_address_id BIGINT,
    shipping_method VARCHAR(50),
    tracking_number VARCHAR(100),
    
    -- Order metadata
    notes TEXT,
    coupon_code VARCHAR(50),
    referral_code VARCHAR(20),
    
    -- Payment information
    payment_transaction_id VARCHAR(255),
    payment_reference VARCHAR(255),
    payment_gateway VARCHAR(50),
    
    -- Timestamps
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    shipped_date TIMESTAMP NULL,
    delivered_date TIMESTAMP NULL,
    cancelled_date TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    FOREIGN KEY (shipping_address_id) REFERENCES addresses(address_id) ON DELETE SET NULL,
    FOREIGN KEY (billing_address_id) REFERENCES addresses(address_id) ON DELETE SET NULL,
    
    INDEX idx_orders_order_number (order_number),
    INDEX idx_orders_user_id (user_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_payment_status (payment_status),
    INDEX idx_orders_payment_method (payment_method),
    INDEX idx_orders_order_date (order_date),
    INDEX idx_orders_shipped_date (shipped_date),
    INDEX idx_orders_delivered_date (delivered_date),
    INDEX idx_orders_coupon_code (coupon_code),
    INDEX idx_orders_referral_code (referral_code),
    INDEX idx_orders_tracking_number (tracking_number),
    INDEX idx_orders_total_amount (total_amount),
    INDEX idx_orders_created_at (created_at),
    
    -- Ensure positive amounts
    CONSTRAINT chk_order_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_order_discount_amount CHECK (discount_amount >= 0),
    CONSTRAINT chk_order_tax_amount CHECK (tax_amount >= 0),
    CONSTRAINT chk_order_shipping_amount CHECK (shipping_amount >= 0),
    CONSTRAINT chk_order_total_amount CHECK (total_amount >= 0)
);

-- Create order_items table
CREATE TABLE order_items (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    variant_id BIGINT NULL,
    product_name VARCHAR(255) NOT NULL,
    variant_name VARCHAR(255),
    sku VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    
    -- Product snapshot at time of order
    brand VARCHAR(50),
    flavor VARCHAR(100),
    size VARCHAR(50),
    weight DECIMAL(8,2),
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT,
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id) ON DELETE RESTRICT,
    
    INDEX idx_order_items_order_id (order_id),
    INDEX idx_order_items_product_id (product_id),
    INDEX idx_order_items_variant_id (variant_id),
    INDEX idx_order_items_sku (sku),
    INDEX idx_order_items_quantity (quantity),
    INDEX idx_order_items_unit_price (unit_price),
    INDEX idx_order_items_total_price (total_price),
    
    -- Ensure positive values
    CONSTRAINT chk_order_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_item_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_order_item_total_price CHECK (total_price >= 0)
);

-- Create order_status_history table for tracking order status changes
CREATE TABLE order_status_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    changed_by_user_id BIGINT,
    reason VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    
    INDEX idx_order_status_history_order_id (order_id),
    INDEX idx_order_status_history_to_status (to_status),
    INDEX idx_order_status_history_created_at (created_at),
    INDEX idx_order_status_history_changed_by (changed_by_user_id)
);

-- Create triggers to update cart totals when cart items change
DELIMITER $$

CREATE TRIGGER update_cart_totals_after_insert
    AFTER INSERT ON cart_items
    FOR EACH ROW
BEGIN
    UPDATE carts 
    SET 
        subtotal = (
            SELECT COALESCE(SUM(total_price), 0) 
            FROM cart_items 
            WHERE cart_id = NEW.cart_id
        ),
        total_items = (
            SELECT COALESCE(SUM(quantity), 0) 
            FROM cart_items 
            WHERE cart_id = NEW.cart_id
        ),
        updated_at = CURRENT_TIMESTAMP
    WHERE cart_id = NEW.cart_id;
END$$

CREATE TRIGGER update_cart_totals_after_update
    AFTER UPDATE ON cart_items
    FOR EACH ROW
BEGIN
    UPDATE carts 
    SET 
        subtotal = (
            SELECT COALESCE(SUM(total_price), 0) 
            FROM cart_items 
            WHERE cart_id = NEW.cart_id
        ),
        total_items = (
            SELECT COALESCE(SUM(quantity), 0) 
            FROM cart_items 
            WHERE cart_id = NEW.cart_id
        ),
        updated_at = CURRENT_TIMESTAMP
    WHERE cart_id = NEW.cart_id;
END$$

CREATE TRIGGER update_cart_totals_after_delete
    AFTER DELETE ON cart_items
    FOR EACH ROW
BEGIN
    UPDATE carts 
    SET 
        subtotal = (
            SELECT COALESCE(SUM(total_price), 0) 
            FROM cart_items 
            WHERE cart_id = OLD.cart_id
        ),
        total_items = (
            SELECT COALESCE(SUM(quantity), 0) 
            FROM cart_items 
            WHERE cart_id = OLD.cart_id
        ),
        updated_at = CURRENT_TIMESTAMP
    WHERE cart_id = OLD.cart_id;
END$$

-- Create trigger to automatically generate order numbers
CREATE TRIGGER generate_order_number_before_insert
    BEFORE INSERT ON orders
    FOR EACH ROW
BEGIN
    IF NEW.order_number IS NULL OR NEW.order_number = '' THEN
        SET NEW.order_number = CONCAT('SK', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD((SELECT COALESCE(MAX(CAST(SUBSTRING(order_number, 9) AS UNSIGNED)), 0) + 1 FROM orders WHERE order_number LIKE CONCAT('SK', DATE_FORMAT(NOW(), '%Y%m%d'), '%')), 4, '0'));
    END IF;
END$$

-- Create trigger to log order status changes
CREATE TRIGGER log_order_status_change
    AFTER UPDATE ON orders
    FOR EACH ROW
BEGIN
    IF NEW.status != OLD.status THEN
        INSERT INTO order_status_history (order_id, from_status, to_status, created_at)
        VALUES (NEW.order_id, OLD.status, NEW.status, CURRENT_TIMESTAMP);
    END IF;
END$$

DELIMITER ;

-- Create indexes for performance optimization
CREATE INDEX idx_carts_user_active ON carts(user_id) WHERE expires_at IS NULL OR expires_at > NOW();
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_orders_date_status ON orders(order_date, status);
CREATE INDEX idx_order_items_product_orders ON order_items(product_id, order_id);
