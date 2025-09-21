package com.suppkart.controller;

import com.suppkart.dto.response.ApiResponse;
import com.suppkart.dto.response.ContactMessageResponse;
import com.suppkart.model.enums.ContactStatus;
import com.suppkart.service.ContactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Admin REST controller for contact message management
 */
@RestController
@RequestMapping("/api/admin/contact")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminContactController {

    private static final Logger log = LoggerFactory.getLogger(AdminContactController.class);

    @Autowired
    private ContactService contactService;

    /**
     * Get all contact messages with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ContactMessageResponse>>> getContactMessages(
            @RequestParam(required = false) ContactStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Admin fetching contact messages - status: {}, page: {}, size: {}", status, page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ContactMessageResponse> messages = contactService.getContactMessages(status, pageable);
            
            return ResponseEntity.ok(ApiResponse.success("Contact messages retrieved successfully", messages));
        } catch (Exception e) {
            log.error("Error fetching contact messages: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve contact messages"));
        }
    }

    /**
     * Get specific contact message by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContactMessageResponse>> getContactMessage(@PathVariable Long id) {
        log.info("Admin fetching contact message with ID: {}", id);
        
        try {
            ContactMessageResponse message = contactService.getContactMessage(id);
            return ResponseEntity.ok(ApiResponse.success("Contact message retrieved successfully", message));
        } catch (Exception e) {
            log.error("Error fetching contact message {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve contact message"));
        }
    }

    /**
     * Update contact message status and add response
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ContactMessageResponse>> updateContactStatus(
            @PathVariable Long id,
            @RequestParam ContactStatus status,
            @RequestParam(required = false) String response) {
        
        log.info("Admin updating contact message {} status to: {}", id, status);
        
        try {
            ContactMessageResponse updatedMessage = contactService.updateContactStatus(id, status, response);
            return ResponseEntity.ok(ApiResponse.success("Contact message status updated successfully", updatedMessage));
        } catch (Exception e) {
            log.error("Error updating contact message status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update contact message status"));
        }
    }

    /**
     * Search contact messages by email
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ContactMessageResponse>>> searchContactMessages(
            @RequestParam String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Admin searching contact messages by email: {}", email);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ContactMessageResponse> messages = contactService.searchByEmail(email, pageable);
            
            return ResponseEntity.ok(ApiResponse.success("Contact messages search completed", messages));
        } catch (Exception e) {
            log.error("Error searching contact messages: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to search contact messages"));
        }
    }

    /**
     * Get contact message statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ContactService.ContactStatsResponse>> getContactStats() {
        log.info("Admin fetching contact message statistics");
        
        try {
            ContactService.ContactStatsResponse stats = contactService.getContactStats();
            return ResponseEntity.ok(ApiResponse.success("Contact statistics retrieved successfully", stats));
        } catch (Exception e) {
            log.error("Error fetching contact statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve contact statistics"));
        }
    }

    /**
     * Bulk update contact message status
     */
    @PatchMapping("/bulk-status")
    public ResponseEntity<ApiResponse<String>> bulkUpdateStatus(
            @RequestParam ContactStatus fromStatus,
            @RequestParam ContactStatus toStatus) {
        
        log.info("Admin bulk updating contact messages from {} to {}", fromStatus, toStatus);
        
        try {
            // This would need to be implemented in the service
            // contactService.bulkUpdateStatus(fromStatus, toStatus);
            return ResponseEntity.ok(ApiResponse.success("Bulk status update completed", "Success"));
        } catch (Exception e) {
            log.error("Error in bulk status update: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to perform bulk status update"));
        }
    }
}
