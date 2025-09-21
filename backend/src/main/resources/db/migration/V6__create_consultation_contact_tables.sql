-- V6: Create consultation and contact system tables

-- Consultation slots table (defines available time slots)
CREATE TABLE consultation_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    day_of_week INT NOT NULL COMMENT '1-7 for Monday-Sunday',
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    max_bookings INT NOT NULL DEFAULT 2 COMMENT 'Max 2 bookings per hour',
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_slot (day_of_week, start_time, end_time)
);

-- Consultations table
CREATE TABLE consultations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL COMMENT 'Null for guest bookings',
    guest_name VARCHAR(100) NULL,
    guest_email VARCHAR(150) NULL,
    guest_phone VARCHAR(20) NULL,
    consultation_date DATE NOT NULL,
    consultation_time TIME NOT NULL,
    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Kolkata',
    topic VARCHAR(200) NOT NULL,
    consultation_type ENUM('ONLINE', 'PHONE') NOT NULL DEFAULT 'ONLINE',
    notes TEXT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    cancel_reason VARCHAR(500) NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_consultation_date_status (consultation_date, status),
    INDEX idx_user_consultations (user_id, consultation_date DESC),
    INDEX idx_guest_email (guest_email)
);

-- Contact messages table
CREATE TABLE contact_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NULL,
    subject VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    status ENUM('NEW', 'IN_PROGRESS', 'RESOLVED', 'SPAM') NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    responded_at TIMESTAMP NULL,
    response TEXT NULL,
    INDEX idx_contact_status (status, created_at DESC),
    INDEX idx_contact_email (email)
);

-- Insert default consultation slots (Monday-Friday, 9 AM to 6 PM, 2 slots per hour)
INSERT INTO consultation_slots (day_of_week, start_time, end_time, max_bookings) VALUES
-- Monday (1)
(1, '09:00:00', '10:00:00', 2),
(1, '10:00:00', '11:00:00', 2),
(1, '11:00:00', '12:00:00', 2),
(1, '12:00:00', '13:00:00', 2),
(1, '13:00:00', '14:00:00', 2),
(1, '14:00:00', '15:00:00', 2),
(1, '15:00:00', '16:00:00', 2),
(1, '16:00:00', '17:00:00', 2),
(1, '17:00:00', '18:00:00', 2),
-- Tuesday (2)
(2, '09:00:00', '10:00:00', 2),
(2, '10:00:00', '11:00:00', 2),
(2, '11:00:00', '12:00:00', 2),
(2, '12:00:00', '13:00:00', 2),
(2, '13:00:00', '14:00:00', 2),
(2, '14:00:00', '15:00:00', 2),
(2, '15:00:00', '16:00:00', 2),
(2, '16:00:00', '17:00:00', 2),
(2, '17:00:00', '18:00:00', 2),
-- Wednesday (3)
(3, '09:00:00', '10:00:00', 2),
(3, '10:00:00', '11:00:00', 2),
(3, '11:00:00', '12:00:00', 2),
(3, '12:00:00', '13:00:00', 2),
(3, '13:00:00', '14:00:00', 2),
(3, '14:00:00', '15:00:00', 2),
(3, '15:00:00', '16:00:00', 2),
(3, '16:00:00', '17:00:00', 2),
(3, '17:00:00', '18:00:00', 2),
-- Thursday (4)
(4, '09:00:00', '10:00:00', 2),
(4, '10:00:00', '11:00:00', 2),
(4, '11:00:00', '12:00:00', 2),
(4, '12:00:00', '13:00:00', 2),
(4, '13:00:00', '14:00:00', 2),
(4, '14:00:00', '15:00:00', 2),
(4, '15:00:00', '16:00:00', 2),
(4, '16:00:00', '17:00:00', 2),
(4, '17:00:00', '18:00:00', 2),
-- Friday (5)
(5, '09:00:00', '10:00:00', 2),
(5, '10:00:00', '11:00:00', 2),
(5, '11:00:00', '12:00:00', 2),
(5, '12:00:00', '13:00:00', 2),
(5, '13:00:00', '14:00:00', 2),
(5, '14:00:00', '15:00:00', 2),
(5, '15:00:00', '16:00:00', 2),
(5, '16:00:00', '17:00:00', 2),
(5, '17:00:00', '18:00:00', 2);
