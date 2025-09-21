-- Create referrals table
CREATE TABLE referrals (
    referral_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    referral_code VARCHAR(20) NOT NULL UNIQUE,
    referrer_user_id BIGINT NOT NULL,
    referred_user_id BIGINT NULL,
    status ENUM('UNUSED', 'USED', 'REWARDED') NOT NULL DEFAULT 'UNUSED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usage_date TIMESTAMP NULL,
    first_order_id BIGINT NULL,
    first_order_completion_date TIMESTAMP NULL,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (referrer_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (referred_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    FOREIGN KEY (first_order_id) REFERENCES orders(order_id) ON DELETE SET NULL,
    
    INDEX idx_referral_code (referral_code),
    INDEX idx_referrer_user (referrer_user_id),
    INDEX idx_referred_user (referred_user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- Create referral_rewards table
CREATE TABLE referral_rewards (
    reward_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    referral_id BIGINT NOT NULL,
    reward_type ENUM('DISCOUNT', 'CREDIT') NOT NULL,
    reward_amount DECIMAL(10,2) NULL,
    reward_percentage INT NULL,
    status ENUM('PENDING', 'ACTIVE', 'APPLIED', 'EXPIRED') NOT NULL DEFAULT 'PENDING',
    expiration_date TIMESTAMP NULL,
    usage_date TIMESTAMP NULL,
    applied_order_id BIGINT NULL,
    is_referrer_reward BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (referral_id) REFERENCES referrals(referral_id) ON DELETE CASCADE,
    FOREIGN KEY (applied_order_id) REFERENCES orders(order_id) ON DELETE SET NULL,
    
    INDEX idx_user_id (user_id),
    INDEX idx_referral_id (referral_id),
    INDEX idx_status (status),
    INDEX idx_expiration_date (expiration_date),
    INDEX idx_is_referrer_reward (is_referrer_reward),
    INDEX idx_created_at (created_at)
);

-- Add referral_code column to users table if not exists
ALTER TABLE users ADD COLUMN referral_code VARCHAR(20) NULL UNIQUE AFTER email;
ALTER TABLE users ADD INDEX idx_user_referral_code (referral_code);

-- Add referral tracking columns to users table
ALTER TABLE users ADD COLUMN referred_by_user_id BIGINT NULL AFTER referral_code;
ALTER TABLE users ADD COLUMN total_referrals INT NOT NULL DEFAULT 0 AFTER referred_by_user_id;
ALTER TABLE users ADD COLUMN successful_referrals INT NOT NULL DEFAULT 0 AFTER total_referrals;

ALTER TABLE users ADD FOREIGN KEY (referred_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL;
ALTER TABLE users ADD INDEX idx_referred_by_user (referred_by_user_id);
