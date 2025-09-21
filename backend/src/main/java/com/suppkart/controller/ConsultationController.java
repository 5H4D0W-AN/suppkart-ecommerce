package com.suppkart.controller;

import com.suppkart.dto.request.ConsultationBookingRequest;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.dto.response.ConsultationResponse;
import com.suppkart.service.ConsultationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Public REST controller for consultation bookings
 */
@RestController
@RequestMapping("/api/consultations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ConsultationController {

    private static final Logger log = LoggerFactory.getLogger(ConsultationController.class);

    @Autowired
    private ConsultationService consultationService;

    /**
     * Get available consultation dates
     */
    @GetMapping("/available-dates")
    public ResponseEntity<ApiResponse<List<LocalDate>>> getAvailableDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Getting available consultation dates from {} to {}", startDate, endDate);
        
        try {
            List<LocalDate> availableDates = consultationService.getAvailableDates(startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success("Available dates retrieved successfully", availableDates));
        } catch (Exception e) {
            log.error("Error getting available dates: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve available dates"));
        }
    }

    /**
     * Get available time slots for a specific date
     */
    @GetMapping("/available-times")
    public ResponseEntity<ApiResponse<List<LocalTime>>> getAvailableTimeSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Getting available time slots for date: {}", date);
        
        try {
            List<LocalTime> availableSlots = consultationService.getAvailableTimeSlots(date);
            return ResponseEntity.ok(ApiResponse.success("Available time slots retrieved successfully", availableSlots));
        } catch (Exception e) {
            log.error("Error getting available time slots: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve available time slots"));
        }
    }

    /**
     * Book a new consultation
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ConsultationResponse>> bookConsultation(
            @Valid @RequestBody ConsultationBookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Booking consultation for user: {}", userDetails != null ? userDetails.getUsername() : "guest");
        
        try {
            // Set user ID if authenticated
            if (userDetails != null) {
                // You might need to get the actual user ID from UserDetails
                // This depends on your UserDetails implementation
                request.setUserId(getUserIdFromUserDetails(userDetails));
            }
            
            ConsultationResponse response = consultationService.bookConsultation(request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            "Consultation booked successfully! You will receive a confirmation email shortly.",
                            response
                    ));
        } catch (Exception e) {
            log.error("Error booking consultation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to book consultation. Please try again."));
        }
    }

    /**
     * Get user's consultations (requires authentication)
     */
    @GetMapping("/my-consultations")
    public ResponseEntity<ApiResponse<List<ConsultationResponse>>> getUserConsultations(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        
        log.info("Getting consultations for user: {}", userDetails.getUsername());
        
        try {
            Long userId = getUserIdFromUserDetails(userDetails);
            List<ConsultationResponse> consultations = consultationService.getUserConsultations(userId);
            
            return ResponseEntity.ok(ApiResponse.success("User consultations retrieved successfully", consultations));
        } catch (Exception e) {
            log.error("Error getting user consultations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve consultations"));
        }
    }

    /**
     * Cancel a consultation (requires authentication)
     */
    @PutMapping("/{consultationId}/cancel")
    public ResponseEntity<ApiResponse<ConsultationResponse>> cancelConsultation(
            @PathVariable Long consultationId,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        
        log.info("Cancelling consultation {} for user: {}", consultationId, userDetails.getUsername());
        
        try {
            Long userId = getUserIdFromUserDetails(userDetails);
            ConsultationResponse response = consultationService.cancelConsultation(consultationId, userId, reason);
            
            return ResponseEntity.ok(ApiResponse.success("Consultation cancelled successfully", response));
        } catch (Exception e) {
            log.error("Error cancelling consultation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to cancel consultation"));
        }
    }

    /**
     * Health check endpoint for consultation system
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Consultation system is operational", "OK"));
    }

    /**
     * Helper method to extract user ID from UserDetails
     * This method should be implemented based on your UserDetails implementation
     */
    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        // TODO: Implement this method based on your UserDetails implementation
        // For now, return a placeholder - this needs to be implemented properly
        return 1L; // This should extract the actual user ID from UserDetails
    }
}
