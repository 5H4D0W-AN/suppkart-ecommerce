package com.suppkart.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.suppkart.model.entity.Order;
import com.suppkart.model.entity.User;

@Service
public class EmailNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@suppkart.com}")
    private String fromEmail;
    
    /**
     * Send generic email
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
            
        } catch (Exception e) {
            logger.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    /**
     * Send order confirmation email
     */
    public void sendOrderConfirmation(Order order) {
        try {
            User user = order.getUser();
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Order Confirmation - " + order.getOrderNumber());
            
            StringBuilder content = new StringBuilder();
            content.append("Dear ").append(user.getFirstName()).append(",\n\n");
            content.append("Thank you for your order! Your order has been confirmed.\n\n");
            content.append("Order Details:\n");
            content.append("Order Number: ").append(order.getOrderNumber()).append("\n");
            content.append("Order Date: ").append(order.getCreatedAt()).append("\n");
            content.append("Total Amount: ₹").append(order.getTotalAmount()).append("\n\n");
            
            // TODO: Add order items details, tracking info, and better formatting
            content.append("You will receive a shipping confirmation email once your order is dispatched.\n\n");
            content.append("Thank you for shopping with SuppKart!\n\n");
            content.append("Best regards,\n");
            content.append("SuppKart Team");
            
            message.setText(content.toString());
            
            mailSender.send(message);
            logger.info("Order confirmation email sent for order: {}", order.getOrderNumber());
            
        } catch (Exception e) {
            logger.error("Failed to send order confirmation email for order: {}", 
                order.getOrderNumber(), e);
            // Don't throw exception to avoid failing the checkout process
        }
    }
    
    /**
     * Send order status update email
     */
    public void sendOrderStatusUpdate(Order order, String statusMessage) {
        try {
            User user = order.getUser();
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Order Update - " + order.getOrderNumber());
            
            StringBuilder content = new StringBuilder();
            content.append("Dear ").append(user.getFirstName()).append(",\n\n");
            content.append("Your order status has been updated.\n\n");
            content.append("Order Number: ").append(order.getOrderNumber()).append("\n");
            content.append("Status: ").append(statusMessage).append("\n\n");
            
            if (order.getTrackingNumber() != null) {
                content.append("Tracking Number: ").append(order.getTrackingNumber()).append("\n\n");
            }
            
            content.append("Thank you for shopping with SuppKart!\n\n");
            content.append("Best regards,\n");
            content.append("SuppKart Team");
            
            message.setText(content.toString());
            
            mailSender.send(message);
            logger.info("Order status update email sent for order: {}", order.getOrderNumber());
            
        } catch (Exception e) {
            logger.error("Failed to send order status update email for order: {}", 
                order.getOrderNumber(), e);
        }
    }
    
    /**
     * Send welcome email to new users
     */
    public void sendWelcomeEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Welcome to SuppKart!");
            
            StringBuilder content = new StringBuilder();
            content.append("Dear ").append(user.getFirstName()).append(",\n\n");
            content.append("Welcome to SuppKart! We're excited to have you join our community.\n\n");
            content.append("Start exploring our wide range of sports supplements and nutrition products.\n\n");
            
            // TODO: Add links to popular products, special offers, etc.
            content.append("If you have any questions, feel free to contact our support team.\n\n");
            content.append("Happy shopping!\n\n");
            content.append("Best regards,\n");
            content.append("SuppKart Team");
            
            message.setText(content.toString());
            
            mailSender.send(message);
            logger.info("Welcome email sent to user: {}", user.getEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send welcome email to user: {}", user.getEmail(), e);
        }
    }
    
    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Password Reset - SuppKart");
            
            StringBuilder content = new StringBuilder();
            content.append("Dear ").append(user.getFirstName()).append(",\n\n");
            content.append("You have requested to reset your password.\n\n");
            content.append("Please click on the link below to reset your password:\n");
            
            // TODO: Configure frontend URL for password reset
            String resetUrl = "https://suppkart.com/reset-password?token=" + resetToken;
            content.append(resetUrl).append("\n\n");
            
            content.append("This link will expire in 24 hours.\n\n");
            content.append("If you didn't request this password reset, please ignore this email.\n\n");
            content.append("Best regards,\n");
            content.append("SuppKart Team");
            
            message.setText(content.toString());
            
            mailSender.send(message);
            logger.info("Password reset email sent to user: {}", user.getEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send password reset email to user: {}", user.getEmail(), e);
        }
    }
    
    /**
     * Send customer data export email (GDPR compliance)
     */
    public void sendCustomerDataExport(User user, String exportData) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Your Data Export - SuppKart");
            
            StringBuilder content = new StringBuilder();
            content.append("Dear ").append(user.getFirstName()).append(",\n\n");
            content.append("As requested, we have prepared your personal data export.\n\n");
            content.append("Your Data:\n");
            content.append("==========\n");
            content.append(exportData).append("\n\n");
            content.append("This export includes all personal information we have stored about you.\n\n");
            content.append("If you have any questions about this data or would like to request changes, ");
            content.append("please contact our support team.\n\n");
            content.append("Best regards,\n");
            content.append("SuppKart Team");
            
            message.setText(content.toString());
            
            mailSender.send(message);
            logger.info("Customer data export email sent to user: {}", user.getEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send customer data export email to user: {}", user.getEmail(), e);
        }
    }
    
    /**
     * Send customer status change notification
     */
    public void sendCustomerStatusChangeNotification(User user, String oldStatus, String newStatus, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Account Status Update - SuppKart");
            
            StringBuilder content = new StringBuilder();
            content.append("Dear ").append(user.getFirstName()).append(",\n\n");
            content.append("Your account status has been updated.\n\n");
            content.append("Previous Status: ").append(oldStatus).append("\n");
            content.append("New Status: ").append(newStatus).append("\n\n");
            
            if (reason != null && !reason.trim().isEmpty()) {
                content.append("Reason: ").append(reason).append("\n\n");
            }
            
            // Add specific messages based on status
            switch (newStatus.toUpperCase()) {
                case "SUSPENDED":
                    content.append("Your account has been temporarily suspended. ");
                    content.append("Please contact our support team if you have any questions.\n\n");
                    break;
                case "LOCKED":
                    content.append("Your account has been locked for security reasons. ");
                    content.append("Please contact our support team to unlock your account.\n\n");
                    break;
                case "ACTIVE":
                    content.append("Your account is now active and you can continue using our services.\n\n");
                    break;
                case "INACTIVE":
                    content.append("Your account has been deactivated. ");
                    content.append("Please contact our support team if you wish to reactivate it.\n\n");
                    break;
                default:
                    content.append("If you have any questions about this status change, ");
                    content.append("please contact our support team.\n\n");
                    break;
            }
            
            content.append("Best regards,\n");
            content.append("SuppKart Team");
            
            message.setText(content.toString());
            
            mailSender.send(message);
            logger.info("Customer status change notification sent to user: {} (Status: {} -> {})", 
                user.getEmail(), oldStatus, newStatus);
            
        } catch (Exception e) {
            logger.error("Failed to send customer status change notification to user: {}", 
                user.getEmail(), e);
        }
    }
    
    /**
     * Send low stock alert to admin
     */
    public void sendLowStockAlert(String adminEmail, String productName, String variantName, int currentStock, int threshold) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(adminEmail);
            message.setSubject("Low Stock Alert - " + productName);
            
            StringBuilder content = new StringBuilder();
            content.append("Low Stock Alert\n");
            content.append("===============\n\n");
            content.append("Product: ").append(productName).append("\n");
            
            if (variantName != null && !variantName.trim().isEmpty()) {
                content.append("Variant: ").append(variantName).append("\n");
            }
            
            content.append("Current Stock: ").append(currentStock).append("\n");
            content.append("Threshold: ").append(threshold).append("\n\n");
            content.append("Please restock this item as soon as possible.\n\n");
            content.append("SuppKart Inventory System");
            
            message.setText(content.toString());
            
            mailSender.send(message);
            logger.info("Low stock alert sent for product: {} (Stock: {})", productName, currentStock);
            
        } catch (Exception e) {
            logger.error("Failed to send low stock alert for product: {}", productName, e);
        }
    }
    
    /**
     * Send out of stock alert to admin
     */
    public void sendOutOfStockAlert(String adminEmail, String productName, String variantName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(adminEmail);
            message.setSubject("Out of Stock Alert - " + productName);
            
            StringBuilder content = new StringBuilder();
            content.append("Out of Stock Alert\n");
            content.append("==================\n\n");
            content.append("Product: ").append(productName).append("\n");
            
            if (variantName != null && !variantName.trim().isEmpty()) {
                content.append("Variant: ").append(variantName).append("\n");
            }
            
            content.append("Current Stock: 0\n\n");
            content.append("This item is now out of stock and needs immediate restocking.\n\n");
            content.append("SuppKart Inventory System");
            
            message.setText(content.toString());
            
            mailSender.send(message);
            logger.info("Out of stock alert sent for product: {}", productName);
            
        } catch (Exception e) {
            logger.error("Failed to send out of stock alert for product: {}", productName, e);
        }
    }
    
    /**
     * Send consultation reminder email
     */
    public void sendConsultationReminder(com.suppkart.model.entity.Consultation consultation) {
        try {
            String recipientEmail;
            String recipientName;
            
            if (consultation.getUser() != null) {
                recipientEmail = consultation.getUser().getEmail();
                recipientName = consultation.getUser().getFirstName();
            } else {
                recipientEmail = consultation.getGuestEmail();
                recipientName = consultation.getGuestName();
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipientEmail);
            message.setSubject("Consultation Reminder - SuppKart");
            
            StringBuilder content = new StringBuilder();
            content.append("Dear ").append(recipientName).append(",\n\n");
            content.append("This is a reminder for your upcoming consultation.\n\n");
            content.append("Consultation Details:\n");
            content.append("Date: ").append(consultation.getConsultationDate()).append("\n");
            content.append("Time: ").append(consultation.getConsultationTime()).append("\n");
            content.append("Type: ").append(consultation.getConsultationType()).append("\n");
            content.append("Topic: ").append(consultation.getTopic()).append("\n\n");
            
            if (consultation.getNotes() != null && !consultation.getNotes().trim().isEmpty()) {
                content.append("Notes: ").append(consultation.getNotes()).append("\n\n");
            }
            
            content.append("Please make sure to be available at the scheduled time.\n\n");
            content.append("If you need to reschedule, please contact us as soon as possible.\n\n");
            content.append("Best regards,\n");
            content.append("SuppKart Team");
            
            message.setText(content.toString());
            
            mailSender.send(message);
            logger.info("Consultation reminder sent for consultation ID: {}", consultation.getId());
            
        } catch (Exception e) {
            logger.error("Failed to send consultation reminder for consultation ID: {}", consultation.getId(), e);
        }
    }
}