package com.suppkart.controller;

import com.suppkart.dto.admin.consultation.*;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.service.AdminConsultationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Admin REST controller for consultation management
 */
@RestController
@RequestMapping("/api/admin/consultations")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminConsultationController {

    private static final Logger log = LoggerFactory.getLogger(AdminConsultationController.class);

    @Autowired
    private AdminConsultationService adminConsultationService;

    /**
     * Get all consultations with filtering and pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ConsultationDTO>>> getAllConsultations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String topic,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Admin fetching consultations - search: {}, status: {}, startDate: {}, endDate: {}, topic: {}, page: {}, size: {}", 
                search, status, startDate, endDate, topic, page, size);
        
        try {
            ConsultationFilterRequest filter = ConsultationFilterRequest.builder()
                    .search(search)
                    .status(status)
                    .startDate(startDate)
                    .endDate(endDate)
                    .topic(topic)
                    .build();
            
            Pageable pageable = PageRequest.of(page, size);
            Page<ConsultationDTO> consultations = adminConsultationService.getAllConsultations(filter, pageable);
            
            return ResponseEntity.ok(ApiResponse.success("Consultations retrieved successfully", consultations));
        } catch (Exception e) {
            log.error("Error fetching consultations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve consultations"));
        }
    }

    /**
     * Get consultation details by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConsultationDetailDTO>> getConsultationById(@PathVariable Long id) {
        log.info("Admin fetching consultation with ID: {}", id);
        
        try {
            ConsultationDetailDTO consultation = adminConsultationService.getConsultationById(id);
            return ResponseEntity.ok(ApiResponse.success("Consultation retrieved successfully", consultation));
        } catch (Exception e) {
            log.error("Error fetching consultation {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve consultation"));
        }
    }

    /**
     * Update consultation status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ConsultationDetailDTO>> updateConsultationStatus(
            @PathVariable Long id,
            @Valid @RequestBody ConsultationStatusUpdateRequest request) {
        
        log.info("Admin updating consultation {} status to: {}", id, request.getStatus());
        
        try {
            ConsultationDetailDTO updatedConsultation = adminConsultationService.updateConsultationStatus(id, request);
            return ResponseEntity.ok(ApiResponse.success("Consultation status updated successfully", updatedConsultation));
        } catch (Exception e) {
            log.error("Error updating consultation status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update consultation status"));
        }
    }

    /**
     * Check if a specific date is available for booking
     */
    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<Boolean>> checkDateAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Admin checking availability for date: {}", date);
        
        try {
            boolean isAvailable = adminConsultationService.isDayAvailableForBooking(date);
            return ResponseEntity.ok(ApiResponse.success("Date availability checked successfully", isAvailable));
        } catch (Exception e) {
            log.error("Error checking date availability: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to check date availability"));
        }
    }

    /**
     * Get current bookings count for a specific date
     */
    @GetMapping("/bookings-count")
    public ResponseEntity<ApiResponse<Integer>> getCurrentBookingsCount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Admin fetching bookings count for date: {}", date);
        
        try {
            int count = adminConsultationService.getCurrentBookingsCount(date);
            return ResponseEntity.ok(ApiResponse.success("Bookings count retrieved successfully", count));
        } catch (Exception e) {
            log.error("Error fetching bookings count: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve bookings count"));
        }
    }



    /**
     * Add consultation notes
     */
    @PutMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<ConsultationDetailDTO>> addConsultationNotes(
            @PathVariable Long id,
            @Valid @RequestBody NotesUpdateRequest request) {
        
        log.info("Admin adding notes to consultation {}", id);
        
        try {
            ConsultationDetailDTO updatedConsultation = adminConsultationService.addConsultationNotes(id, request);
            return ResponseEntity.ok(ApiResponse.success("Consultation notes updated successfully", updatedConsultation));
        } catch (Exception e) {
            log.error("Error updating consultation notes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update consultation notes"));
        }
    }

    /**
     * Send consultation reminder
     */
    @PostMapping("/{id}/reminder")
    public ResponseEntity<ApiResponse<String>> sendConsultationReminder(@PathVariable Long id) {
        log.info("Admin sending reminder for consultation {}", id);
        
        try {
            adminConsultationService.sendConsultationReminder(id);
            return ResponseEntity.ok(ApiResponse.success("Consultation reminder sent successfully", "Success"));
        } catch (Exception e) {
            log.error("Error sending consultation reminder: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to send consultation reminder"));
        }
    }

    /**
     * Get consultation statistics for dashboard
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getConsultationStats() {
        log.info("Admin fetching consultation statistics");
        
        try {
            DashboardStatsDTO stats = adminConsultationService.getConsultationStats();
            return ResponseEntity.ok(ApiResponse.success("Consultation statistics retrieved successfully", stats));
        } catch (Exception e) {
            log.error("Error fetching consultation statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve consultation statistics"));
        }
    }



    /**
     * Reschedule consultation
     */
    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<ApiResponse<ConsultationDetailDTO>> rescheduleConsultation(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleRequest request) {
        
        log.info("Admin rescheduling consultation {} to {}", id, request.getNewDate());
        
        try {
            ConsultationDetailDTO rescheduledConsultation = adminConsultationService.rescheduleConsultation(id, request);
            return ResponseEntity.ok(ApiResponse.success("Consultation rescheduled successfully", rescheduledConsultation));
        } catch (Exception e) {
            log.error("Error rescheduling consultation: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to reschedule consultation"));
        }
    }
}
