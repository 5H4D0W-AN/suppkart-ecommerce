package com.suppkart.controller;

import com.suppkart.dto.request.ContactMessageRequest;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.dto.response.ContactMessageResponse;
import com.suppkart.service.ContactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Public REST controller for contact messages
 */
@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    @Autowired
    private ContactService contactService;

    /**
     * Submit a new contact message
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ContactMessageResponse>> submitContactMessage(
            @Valid @RequestBody ContactMessageRequest request) {
        log.info("Received contact message submission from: {}", request.getEmail());

        try {
            ContactMessageResponse response = contactService.submitContactMessage(request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            "Contact message submitted successfully. We will get back to you soon!",
                            response
                    ));
        } catch (Exception e) {
            log.error("Error submitting contact message: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to submit contact message. Please try again."));
        }
    }

    /**
     * Health check endpoint for contact system
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Contact system is operational", "OK"));
    }
}
