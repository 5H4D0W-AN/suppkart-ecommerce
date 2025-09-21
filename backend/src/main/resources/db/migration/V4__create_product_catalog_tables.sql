-- V4__create_product_catalog_tables.sql
-- Product catalog tables for categories, products, variants, and taxonomy

-- Create categories table
CREATE TABLE categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL DEFAULT 0,
    slug VARCHAR(255) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_categories_name (name),
    INDEX idx_categories_slug (slug),
    INDEX idx_categories_is_active (is_active),
    INDEX idx_categories_display_order (display_order),
    INDEX idx_categories_created_at (created_at)
);

-- Create sports table
CREATE TABLE sports (
    sport_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    image_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_sports_name (name),
    INDEX idx_sports_is_active (is_active),
    INDEX idx_sports_display_order (display_order)
);

-- Create goals table
CREATE TABLE goals (
    goal_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    image_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_goals_name (name),
    INDEX idx_goals_is_active (is_active),
    INDEX idx_goals_display_order (display_order)
);

-- Create products table
CREATE TABLE products (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    short_description VARCHAR(500),
    brand VARCHAR(50) NOT NULL,
    sku VARCHAR(100) NOT NULL UNIQUE,
    barcode VARCHAR(100),
    weight DECIMAL(8,2),
    dimensions VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_highlighted BOOLEAN NOT NULL DEFAULT FALSE,
    slug VARCHAR(255) UNIQUE,
    meta_title VARCHAR(255),
    meta_description VARCHAR(500),
    avg_rating DECIMAL(3,2) DEFAULT 0.00,
    review_count INT DEFAULT 0,
    serving_size VARCHAR(100),
    servings_per_container INT,
    protein_content VARCHAR(100),
    ingredients TEXT,
    directions TEXT,
    warnings TEXT,
    low_stock_threshold INT NOT NULL DEFAULT 2,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_products_name (name),
    INDEX idx_products_brand (brand),
    INDEX idx_products_sku (sku),
    INDEX idx_products_slug (slug),
    INDEX idx_products_is_active (is_active),
    INDEX idx_products_is_highlighted (is_highlighted),
    INDEX idx_products_avg_rating (avg_rating),
    INDEX idx_products_review_count (review_count),
    INDEX idx_products_created_at (created_at),
    FULLTEXT INDEX idx_products_search (name, description, short_description)
);

-- Create product_variants table
CREATE TABLE product_variants (
    variant_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    sku VARCHAR(100) NOT NULL UNIQUE,
    barcode VARCHAR(100),
    price DECIMAL(10,2) NOT NULL,
    sale_price DECIMAL(10,2),
    cost_price DECIMAL(10,2),
    stock_quantity INT NOT NULL DEFAULT 0,
    weight DECIMAL(8,2),
    dimensions VARCHAR(50),
    flavor VARCHAR(100),
    size VARCHAR(50),
    color VARCHAR(50),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    
    INDEX idx_product_variants_product_id (product_id),
    INDEX idx_product_variants_sku (sku),
    INDEX idx_product_variants_price (price),
    INDEX idx_product_variants_sale_price (sale_price),
    INDEX idx_product_variants_stock_quantity (stock_quantity),
    INDEX idx_product_variants_is_default (is_default),
    INDEX idx_product_variants_is_active (is_active),
    INDEX idx_product_variants_flavor (flavor),
    INDEX idx_product_variants_size (size),
    INDEX idx_product_variants_sort_order (sort_order)
);

-- Create product_images table
CREATE TABLE product_images (
    image_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    variant_id BIGINT NULL,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    
    INDEX idx_product_images_product_id (product_id),
    INDEX idx_product_images_variant_id (variant_id),
    INDEX idx_product_images_is_primary (is_primary),
    INDEX idx_product_images_sort_order (sort_order)
);

-- Create product_categories junction table
CREATE TABLE product_categories (
    product_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (product_id, category_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE,
    
    INDEX idx_product_categories_product_id (product_id),
    INDEX idx_product_categories_category_id (category_id)
);

-- Create product_sports junction table
CREATE TABLE product_sports (
    product_id BIGINT NOT NULL,
    sport_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (product_id, sport_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (sport_id) REFERENCES sports(sport_id) ON DELETE CASCADE,
    
    INDEX idx_product_sports_product_id (product_id),
    INDEX idx_product_sports_sport_id (sport_id)
);

-- Create product_goals junction table
CREATE TABLE product_goals (
    product_id BIGINT NOT NULL,
    goal_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (product_id, goal_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (goal_id) REFERENCES goals(goal_id) ON DELETE CASCADE,
    
    INDEX idx_product_goals_product_id (product_id),
    INDEX idx_product_goals_goal_id (goal_id)
);

-- Insert default categories
INSERT INTO categories (name, slug, description, display_order, is_active) VALUES
('Protein Supplements', 'protein-supplements', 'Whey, casein, and plant-based protein powders', 1, TRUE),
('Pre-Workout', 'pre-workout', 'Energy and performance enhancing supplements', 2, TRUE),
('Post-Workout', 'post-workout', 'Recovery and muscle building supplements', 3, TRUE),
('Weight Management', 'weight-management', 'Weight loss and weight gain supplements', 4, TRUE),
('Vitamins & Minerals', 'vitamins-minerals', 'Essential vitamins and mineral supplements', 5, TRUE),
('Creatine', 'creatine', 'Creatine monohydrate and other creatine supplements', 6, TRUE),
('BCAAs & Amino Acids', 'bcaas-amino-acids', 'Branched-chain amino acids and essential amino acids', 7, TRUE),
('Health & Wellness', 'health-wellness', 'General health and wellness supplements', 8, TRUE),
('Sports Nutrition', 'sports-nutrition', 'Performance nutrition for athletes', 9, TRUE),
('Accessories', 'accessories', 'Shakers, gym accessories, and equipment', 10, TRUE);

-- Insert default sports
INSERT INTO sports (name, description, display_order, is_active) VALUES
('Bodybuilding', 'Muscle building and physique development', 1, TRUE),
('Powerlifting', 'Strength training and maximum lift performance', 2, TRUE),
('CrossFit', 'High-intensity functional fitness training', 3, TRUE),
('Cardio & Running', 'Cardiovascular fitness and endurance running', 4, TRUE),
('Weight Training', 'General weight and resistance training', 5, TRUE),
('Yoga & Pilates', 'Flexibility, balance, and core strengthening', 6, TRUE),
('MMA & Martial Arts', 'Mixed martial arts and combat sports', 7, TRUE),
('Team Sports', 'Football, basketball, soccer, and other team sports', 8, TRUE),
('Cycling', 'Road cycling, mountain biking, and indoor cycling', 9, TRUE),
('Swimming', 'Pool and open water swimming', 10, TRUE);

-- Insert default goals
INSERT INTO goals (name, description, display_order, is_active) VALUES
('Muscle Gain', 'Build lean muscle mass and increase strength', 1, TRUE),
('Weight Loss', 'Reduce body fat and achieve a leaner physique', 2, TRUE),
('Weight Gain', 'Increase overall body weight and muscle mass', 3, TRUE),
('Athletic Performance', 'Enhance sports performance and endurance', 4, TRUE),
('General Fitness', 'Maintain overall health and fitness', 5, TRUE),
('Recovery', 'Improve recovery time and reduce muscle soreness', 6, TRUE),
('Energy & Focus', 'Increase energy levels and mental focus', 7, TRUE),
('Strength Training', 'Increase maximum strength and power output', 8, TRUE),
('Endurance', 'Improve cardiovascular endurance and stamina', 9, TRUE),
('Health & Wellness', 'Support overall health and well-being', 10, TRUE);
