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
}
