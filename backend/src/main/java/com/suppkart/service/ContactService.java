package com.suppkart.service;

import com.suppkart.dto.request.ContactMessageRequest;
import com.suppkart.dto.response.ContactMessageResponse;
import com.suppkart.exception.ContactException;
import com.suppkart.model.entity.ContactMessage;
import com.suppkart.model.enums.ContactStatus;
import com.suppkart.repository.ContactMessageRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactService.class);

    @Autowired
    private ContactMessageRepository contactMessageRepository;
    
    @Autowired
    private EmailNotificationService emailNotificationService;
    
    /**
     * Submit a new contact message
     */
    public ContactMessageResponse submitContactMessage(ContactMessageRequest request) {
        log.info("Submitting contact message from: {}", request.getEmail());
        
        try {
            // Create contact message entity
            ContactMessage contactMessage = ContactMessage.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .subject(request.getSubject())
                    .message(request.getMessage())
                    .status(ContactStatus.NEW)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            // Save to database
            ContactMessage savedMessage = contactMessageRepository.save(contactMessage);
            
            // Send acknowledgment email
            sendAcknowledgmentEmail(savedMessage);
            
            log.info("Contact message submitted successfully with ID: {}", savedMessage.getId());
            return mapToContactMessageResponse(savedMessage);
            
        } catch (Exception e) {
            log.error("Error submitting contact message: {}", e.getMessage(), e);
            throw new ContactException("Failed to submit contact message");
        }
    }
    
    /**
     * Get contact message by ID
     */
    public ContactMessageResponse getContactMessage(Long id) {
        log.info("Fetching contact message with ID: {}", id);
        
        ContactMessage contactMessage = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ContactException("Contact message not found with ID: " + id));
        
        return mapToContactMessageResponse(contactMessage);
    }
    
    /**
     * Get all contact messages with pagination and status filter
     */
    public Page<ContactMessageResponse> getContactMessages(ContactStatus status, Pageable pageable) {
        log.info("Fetching contact messages with status: {}", status);
        
        Page<ContactMessage> contactMessages;
        if (status != null) {
            contactMessages = contactMessageRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            contactMessages = contactMessageRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        
        return contactMessages.map(this::mapToContactMessageResponse);
    }
    
    /**
     * Update contact message status and add response
     */
    public ContactMessageResponse updateContactStatus(Long id, ContactStatus status, String response) {
        log.info("Updating contact message {} to status: {}", id, status);
        
        ContactMessage contactMessage = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ContactException("Contact message not found with ID: " + id));
        
        contactMessage.setStatus(status);
        contactMessage.setUpdatedAt(LocalDateTime.now());
        
        if (response != null && !response.trim().isEmpty()) {
            contactMessage.setResponse(response);
            contactMessage.setRespondedAt(LocalDateTime.now());
            
            // Send response email to customer
            sendResponseEmail(contactMessage);
        }
        
        ContactMessage updatedMessage = contactMessageRepository.save(contactMessage);
        
        log.info("Contact message status updated successfully");
        return mapToContactMessageResponse(updatedMessage);
    }
    
    /**
     * Get contact message statistics
     */
    public ContactStatsResponse getContactStats() {
        log.info("Fetching contact message statistics");
        
        long newMessages = contactMessageRepository.countByStatus(ContactStatus.NEW);
        long inProgressMessages = contactMessageRepository.countByStatus(ContactStatus.IN_PROGRESS);
        long resolvedMessages = contactMessageRepository.countByStatus(ContactStatus.RESOLVED);
        long totalMessages = contactMessageRepository.count();
        
        return ContactStatsResponse.builder()
                .newMessages(newMessages)
                .inProgressMessages(inProgressMessages)
                .resolvedMessages(resolvedMessages)
                .totalMessages(totalMessages)
                .build();
    }
    
    /**
     * Search contact messages by email
     */
    public Page<ContactMessageResponse> searchByEmail(String email, Pageable pageable) {
        log.info("Searching contact messages by email: {}", email);
        
        Page<ContactMessage> contactMessages = contactMessageRepository.findByEmailContainingIgnoreCase(email, pageable);
        return contactMessages.map(this::mapToContactMessageResponse);
    }
    
    /**
     * Send acknowledgment email to customer
     */
    private void sendAcknowledgmentEmail(ContactMessage contactMessage) {
        try {
            String subject = "Thank you for contacting SuppKart";
            String body = String.format(
                    "Dear %s,\n\n" +
                    "Thank you for reaching out to us. We have received your message regarding: %s\n\n" +
                    "Our team will review your inquiry and get back to you within 24-48 hours.\n\n" +
                    "Reference ID: %d\n\n" +
                    "Best regards,\n" +
                    "SuppKart Customer Support Team",
                    contactMessage.getName(),
                    contactMessage.getSubject(),
                    contactMessage.getId()
            );
            
            emailNotificationService.sendEmail(contactMessage.getEmail(), subject, body);
            log.info("Acknowledgment email sent successfully to: {}", contactMessage.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to send acknowledgment email: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Send response email to customer
     */
    private void sendResponseEmail(ContactMessage contactMessage) {
        try {
            String subject = "Response to your inquiry - SuppKart";
            String body = String.format(
                    "Dear %s,\n\n" +
                    "Thank you for your patience. Here is our response to your inquiry:\n\n" +
                    "Original Subject: %s\n\n" +
                    "Our Response:\n%s\n\n" +
                    "If you have any further questions, please don't hesitate to contact us.\n\n" +
                    "Best regards,\n" +
                    "SuppKart Customer Support Team",
                    contactMessage.getName(),
                    contactMessage.getSubject(),
                    contactMessage.getResponse()
            );
            
            emailNotificationService.sendEmail(contactMessage.getEmail(), subject, body);
            log.info("Response email sent successfully to: {}", contactMessage.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to send response email: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Map ContactMessage entity to ContactMessageResponse DTO
     */
    private ContactMessageResponse mapToContactMessageResponse(ContactMessage contactMessage) {
        return ContactMessageResponse.builder()
                .id(contactMessage.getId())
                .name(contactMessage.getName())
                .email(contactMessage.getEmail())
                .phone(contactMessage.getPhone())
                .subject(contactMessage.getSubject())
                .message(contactMessage.getMessage())
                .status(contactMessage.getStatus())
                .response(contactMessage.getResponse())
                .createdAt(contactMessage.getCreatedAt())
                .updatedAt(contactMessage.getUpdatedAt())
                .respondedAt(contactMessage.getRespondedAt())
                .isResponded(contactMessage.isResponded())
                .build();
    }
    
    /**
     * Contact stats response DTO
     */
    public static class ContactStatsResponse {
        private long newMessages;
        private long inProgressMessages;
        private long resolvedMessages;
        private long totalMessages;
        
        // Default constructor
        public ContactStatsResponse() {}
        
        // Constructor with all fields
        public ContactStatsResponse(long newMessages, long inProgressMessages, long resolvedMessages, long totalMessages) {
            this.newMessages = newMessages;
            this.inProgressMessages = inProgressMessages;
            this.resolvedMessages = resolvedMessages;
            this.totalMessages = totalMessages;
        }
        
        // Getters
        public long getNewMessages() {
            return newMessages;
        }
        
        public long getInProgressMessages() {
            return inProgressMessages;
        }
        
        public long getResolvedMessages() {
            return resolvedMessages;
        }
        
        public long getTotalMessages() {
            return totalMessages;
        }
        
        // Setters
        public void setNewMessages(long newMessages) {
            this.newMessages = newMessages;
        }
        
        public void setInProgressMessages(long inProgressMessages) {
            this.inProgressMessages = inProgressMessages;
        }
        
        public void setResolvedMessages(long resolvedMessages) {
            this.resolvedMessages = resolvedMessages;
        }
        
        public void setTotalMessages(long totalMessages) {
            this.totalMessages = totalMessages;
        }
        
        // Builder pattern implementation
        public static ContactStatsResponseBuilder builder() {
            return new ContactStatsResponseBuilder();
        }
        
        public static class ContactStatsResponseBuilder {
            private long newMessages;
            private long inProgressMessages;
            private long resolvedMessages;
            private long totalMessages;
            
            public ContactStatsResponseBuilder newMessages(long newMessages) {
                this.newMessages = newMessages;
                return this;
            }
            
            public ContactStatsResponseBuilder inProgressMessages(long inProgressMessages) {
                this.inProgressMessages = inProgressMessages;
                return this;
            }
            
            public ContactStatsResponseBuilder resolvedMessages(long resolvedMessages) {
                this.resolvedMessages = resolvedMessages;
                return this;
            }
            
            public ContactStatsResponseBuilder totalMessages(long totalMessages) {
                this.totalMessages = totalMessages;
                return this;
            }
            
            public ContactStatsResponse build() {
                return new ContactStatsResponse(newMessages, inProgressMessages, resolvedMessages, totalMessages);
            }
        }
    }
}
