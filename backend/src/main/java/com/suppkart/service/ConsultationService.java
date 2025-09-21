package com.suppkart.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suppkart.dto.request.ConsultationBookingRequest;
import com.suppkart.dto.response.ConsultationResponse;
import com.suppkart.exception.ConsultationException;
import com.suppkart.model.entity.Consultation;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.ConsultationStatus;
import com.suppkart.repository.ConsultationRepository;
import com.suppkart.repository.ConsultationSlotRepository;
import com.suppkart.repository.UserRepository;

import lombok.Data;

/**
 * Service for managing consultations
 */
@Service
@Transactional
public class ConsultationService {

    private static final Logger log = LoggerFactory.getLogger(ConsultationService.class);

    @Autowired
    private ConsultationRepository consultationRepository;
    
    @Autowired
    private ConsultationSlotRepository consultationSlotRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailNotificationService emailNotificationService;

    /**
     * Get available dates within range
     */
    public List<LocalDate> getAvailableDates(LocalDate startDate, LocalDate endDate) {
        return startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> date.getDayOfWeek().getValue() <= 5) // Mon-Fri
                .filter(this::hasAvailableSlots)
                .collect(Collectors.toList());
    }

    /**
     * Get available time slots for date
     */
    public List<LocalTime> getAvailableTimeSlots(LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        return consultationSlotRepository.findByDayOfWeekAndIsAvailableTrue(dayOfWeek)
                .stream()
                .flatMap(slot -> generateTimeSlots(slot.getStartTime(), slot.getEndTime()))
                .filter(time -> isSlotAvailable(date, time))
                .collect(Collectors.toList());
    }
    
    private java.util.stream.Stream<LocalTime> generateTimeSlots(LocalTime start, LocalTime end) {
        List<LocalTime> slots = new java.util.ArrayList<>();
        LocalTime current = start;
        while (current.isBefore(end)) {
            slots.add(current);
            current = current.plusMinutes(15);
        }
        return slots.stream();
    }

    /**
     * Book consultation
     */
    public ConsultationResponse bookConsultation(ConsultationBookingRequest request) {
        validateBookingRequest(request);
        
        Consultation consultation = new Consultation();
        
        // Set user or guest details
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ConsultationException("User not found"));
            consultation.setUser(user);
        } else {
            consultation.setGuestName(request.getGuestName());
            consultation.setGuestEmail(request.getGuestEmail());
            consultation.setGuestPhone(request.getGuestPhone());
        }
        
        consultation.setConsultationDate(request.getConsultationDate());
        consultation.setConsultationTime(request.getConsultationTime());
        consultation.setTimezone(request.getTimezone());
        consultation.setTopic(request.getTopic());
        consultation.setConsultationType(request.getConsultationType());
        consultation.setNotes(request.getNotes());
        consultation.setStatus(ConsultationStatus.REQUESTED);
        
        Consultation saved = consultationRepository.save(consultation);
        
        // Send confirmation email
        sendConfirmationEmail(saved);
        
        return mapToResponse(saved);
    }

    /**
     * Update consultation status
     */
    public ConsultationResponse updateStatus(Long id, ConsultationStatus status, String notes) {
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> ConsultationException.consultationNotFound(id));
        
        consultation.setStatus(status);
        if (notes != null) {
            consultation.setNotes(notes);
        }
        
        Consultation updated = consultationRepository.save(consultation);
        
        // Send status update email
        sendStatusUpdateEmail(updated);
        
        return mapToResponse(updated);
    }

    /**
     * Get consultations with pagination
     */
    public Page<ConsultationResponse> getConsultations(ConsultationStatus status, 
                                                      LocalDate startDate, 
                                                      LocalDate endDate,
                                                      Pageable pageable) {
        Page<Consultation> consultations;
        
        if (status != null && startDate != null && endDate != null) {
            consultations = consultationRepository.findByStatusAndConsultationDateBetween(
                    status, startDate, endDate, pageable);
        } else if (status != null) {
            consultations = consultationRepository.findByStatus(status, pageable);
        } else if (startDate != null && endDate != null) {
            consultations = consultationRepository.findByConsultationDateBetween(
                    startDate, endDate, pageable);
        } else {
            consultations = consultationRepository.findAll(pageable);
        }
        
        return consultations.map(this::mapToResponse);
    }

    // Private helper methods
    private boolean hasAvailableSlots(LocalDate date) {
        return !getAvailableTimeSlots(date).isEmpty();
    }

    private boolean isSlotAvailable(LocalDate date, LocalTime time) {
        LocalTime endTime = time.plusMinutes(15);
        Long bookingCount = consultationRepository.countByConsultationDateAndConsultationTimeBetween(
                date, time, endTime);
        return bookingCount < 2; // Max 2 bookings per hour
    }

    private void validateBookingRequest(ConsultationBookingRequest request) {
        if (request.getConsultationDate().isBefore(LocalDate.now())) {
            throw ConsultationException.pastDateBooking();
        }
        
        if (!isSlotAvailable(request.getConsultationDate(), request.getConsultationTime())) {
            throw ConsultationException.slotNotAvailable(
                    request.getConsultationDate().toString(),
                    request.getConsultationTime().toString());
        }
    }

    private void sendConfirmationEmail(Consultation consultation) {
        // TODO: Implement consultation confirmation email
        // String recipientEmail = consultation.getUser() != null 
        //         ? consultation.getUser().getEmail() 
        //         : consultation.getGuestEmail();
    }

    private void sendStatusUpdateEmail(Consultation consultation) {
        // TODO: Implement consultation status update email  
        // String recipientEmail = consultation.getUser() != null 
        //         ? consultation.getUser().getEmail() 
        //         : consultation.getGuestEmail();
    }

    /**
     * Get consultations for admin with pagination and filtering
     */
    public Page<ConsultationResponse> getConsultationsForAdmin(ConsultationStatus status, 
                                                              LocalDate startDate, 
                                                              LocalDate endDate,
                                                              Pageable pageable) {
        return getConsultations(status, startDate, endDate, pageable);
    }

    /**
     * Get consultation by ID
     */
    public ConsultationResponse getConsultationById(Long id) {
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> ConsultationException.consultationNotFound(id));
        return mapToResponse(consultation);
    }

    /**
     * Update consultation status (admin version)
     */
    public ConsultationResponse updateConsultationStatus(Long id, ConsultationStatus status, String notes) {
        return updateStatus(id, status, notes);
    }

    /**
     * Get consultations for a specific date
     */
    public List<ConsultationResponse> getConsultationsForDate(LocalDate date) {
        List<Consultation> consultations = consultationRepository.findByConsultationDateAndStatusNot(
                date, ConsultationStatus.CANCELLED);
        return consultations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get consultation statistics
     */
    public ConsultationStatsResponse getConsultationStats() {
        long totalConsultations = consultationRepository.count();
        long pendingConsultations = consultationRepository.countByStatus(ConsultationStatus.PENDING);
        long confirmedConsultations = consultationRepository.countByStatus(ConsultationStatus.CONFIRMED);
        long completedConsultations = consultationRepository.countByStatus(ConsultationStatus.COMPLETED);
        long cancelledConsultations = consultationRepository.countByStatus(ConsultationStatus.CANCELLED);

        return ConsultationStatsResponse.builder()
                .totalConsultations(totalConsultations)
                .pendingConsultations(pendingConsultations)
                .confirmedConsultations(confirmedConsultations)
                .completedConsultations(completedConsultations)
                .cancelledConsultations(cancelledConsultations)
                .build();
    }

    /**
     * Admin cancel consultation
     */
    public ConsultationResponse adminCancelConsultation(Long id, String reason) {
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> ConsultationException.consultationNotFound(id));
        
        consultation.setStatus(ConsultationStatus.CANCELLED);
        consultation.setCancelReason(reason);
        consultation.setCancelledAt(LocalDateTime.now());
        consultation.setUpdatedAt(LocalDateTime.now());
        
        Consultation updated = consultationRepository.save(consultation);
        
        // Send cancellation email
        sendStatusUpdateEmail(updated);
        
        return mapToResponse(updated);
    }

    /**
     * Reschedule consultation
     */
    public ConsultationResponse rescheduleConsultation(Long id, LocalDate newDate, LocalTime newTime, String reason) {
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> ConsultationException.consultationNotFound(id));
        
        // Validate new slot availability
        if (!isSlotAvailable(newDate, newTime)) {
            throw ConsultationException.slotNotAvailable(newDate.toString(), newTime.toString());
        }
        
        consultation.setConsultationDate(newDate);
        consultation.setConsultationTime(newTime);
        consultation.setNotes(consultation.getNotes() + "\nRescheduled: " + reason);
        consultation.setUpdatedAt(LocalDateTime.now());
        
        Consultation updated = consultationRepository.save(consultation);
        
        // Send reschedule email
        sendStatusUpdateEmail(updated);
        
        return mapToResponse(updated);
    }

    /**
     * Search consultations
     */
    public Page<ConsultationResponse> searchConsultations(String query, Pageable pageable) {
        Page<Consultation> consultations = consultationRepository.findByGuestNameContainingIgnoreCaseOrGuestEmailContainingIgnoreCase(
                query, query, pageable);
        return consultations.map(this::mapToResponse);
    }

    /**
     * Get user consultations
     */
    public List<ConsultationResponse> getUserConsultations(Long userId) {
        List<Consultation> consultations = consultationRepository.findByUserUserIdOrderByConsultationDateDescConsultationTimeDesc(userId);
        return consultations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancel consultation (user version)
     */
    public ConsultationResponse cancelConsultation(Long consultationId, Long userId, String reason) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> ConsultationException.consultationNotFound(consultationId));
        
        // Verify user ownership
        if (consultation.getUser() == null || !consultation.getUser().getUserId().equals(userId)) {
            throw new ConsultationException("User not authorized to cancel this consultation");
        }
        
        consultation.setStatus(ConsultationStatus.CANCELLED);
        consultation.setCancelReason(reason);
        consultation.setCancelledAt(LocalDateTime.now());
        consultation.setUpdatedAt(LocalDateTime.now());
        
        Consultation updated = consultationRepository.save(consultation);
        
        // Send cancellation email
        sendStatusUpdateEmail(updated);
        
        return mapToResponse(updated);
    }

    private ConsultationResponse mapToResponse(Consultation consultation) {
        ConsultationResponse response = new ConsultationResponse();
        response.setId(consultation.getId());
        response.setUserId(consultation.getUser() != null ? consultation.getUser().getUserId() : null);
        response.setUserName(consultation.getUser() != null ? 
                consultation.getUser().getFirstName() + " " + consultation.getUser().getLastName() : null);
        response.setUserEmail(consultation.getUser() != null ? consultation.getUser().getEmail() : null);
        response.setGuestName(consultation.getGuestName());
        response.setGuestEmail(consultation.getGuestEmail());
        response.setGuestPhone(consultation.getGuestPhone());
        response.setConsultationDate(consultation.getConsultationDate());
        response.setConsultationTime(consultation.getConsultationTime());
        response.setTimezone(consultation.getTimezone());
        response.setTopic(consultation.getTopic());
        response.setConsultationType(consultation.getConsultationType());
        response.setNotes(consultation.getNotes());
        response.setStatus(consultation.getStatus());
        response.setCreatedAt(consultation.getCreatedAt());
        response.setUpdatedAt(consultation.getUpdatedAt());
        return response;
    }

    /**
     * Statistics response DTO
     */
    @Data
    public static class ConsultationStatsResponse {
        private long totalConsultations;
        private long pendingConsultations;
        private long confirmedConsultations;
        private long completedConsultations;
        private long cancelledConsultations;
        
        public static ConsultationStatsResponse builder() {
            return new ConsultationStatsResponse();
        }
        
        public ConsultationStatsResponse totalConsultations(long totalConsultations) {
            this.totalConsultations = totalConsultations;
            return this;
        }
        
        public ConsultationStatsResponse pendingConsultations(long pendingConsultations) {
            this.pendingConsultations = pendingConsultations;
            return this;
        }
        
        public ConsultationStatsResponse confirmedConsultations(long confirmedConsultations) {
            this.confirmedConsultations = confirmedConsultations;
            return this;
        }
        
        public ConsultationStatsResponse completedConsultations(long completedConsultations) {
            this.completedConsultations = completedConsultations;
            return this;
        }
        
        public ConsultationStatsResponse cancelledConsultations(long cancelledConsultations) {
            this.cancelledConsultations = cancelledConsultations;
            return this;
        }
        
        public ConsultationStatsResponse build() {
            return this;
        }
    }
}
