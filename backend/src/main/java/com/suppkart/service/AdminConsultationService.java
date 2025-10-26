package com.suppkart.service;

import com.suppkart.dto.admin.consultation.*;
import com.suppkart.exception.ConsultationException;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.entity.Consultation;

import com.suppkart.model.enums.ConsultationStatus;
import com.suppkart.repository.ConsultationRepository;

import com.suppkart.repository.UserRepository;
import com.suppkart.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminConsultationService {

    private final ConsultationRepository consultationRepository;

    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;

    public Page<ConsultationDTO> getAllConsultations(ConsultationFilterRequest filter, Pageable pageable) {
        log.info("Getting all consultations with filter: {}", filter);

        Specification<Consultation> spec = createConsultationSpecification(filter);
        Page<Consultation> consultations = consultationRepository.findAll(spec, pageable);

        return consultations.map(this::convertToConsultationDTO);
    }

    public ConsultationDetailDTO getConsultationById(Long id) {
        log.info("Getting consultation by id: {}", id);

        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with id: " + id));

        return convertToConsultationDetailDTO(consultation);
    }

    public ConsultationDetailDTO updateConsultationStatus(Long id, ConsultationStatusUpdateRequest request) {
        log.info("Updating consultation status for id: {} to status: {}", id, request.getStatus());

        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with id: " + id));

        ConsultationStatus oldStatus = consultation.getStatus();
        ConsultationStatus newStatus = ConsultationStatus.valueOf(request.getStatus());

        consultation.setStatus(newStatus);
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            // Since adminNotes field doesn't exist, we'll append to notes
            String existingNotes = consultation.getNotes() != null ? consultation.getNotes() : "";
            consultation.setNotes(existingNotes + "\nAdmin Notes: " + request.getNotes());
        }
        consultation.setUpdatedAt(LocalDateTime.now());

        Consultation savedConsultation = consultationRepository.save(consultation);

        // Send notification if requested
        if (Boolean.TRUE.equals(request.getSendNotification())) {
            try {
                sendStatusUpdateNotification(savedConsultation, oldStatus, newStatus);
            } catch (Exception e) {
                log.error("Failed to send status update notification for consultation: {}", id, e);
            }
        }

        return convertToConsultationDetailDTO(savedConsultation);
    }

    public boolean isDayAvailableForBooking(LocalDate date) {
        log.info("Checking availability for date: {}", date);

        // Count active bookings for the date (REQUESTED, CONFIRMED, PENDING)
        Long currentBookings = consultationRepository.countActiveConsultationsByDate(date);

        // Simple availability check - max 5 bookings per day
        return currentBookings < 5;
    }

    public int getCurrentBookingsCount(LocalDate date) {
        log.info("Getting current bookings count for date: {}", date);

        Long currentBookings = consultationRepository.countActiveConsultationsByDate(date);
        return currentBookings.intValue();
    }

    public ConsultationDetailDTO addConsultationNotes(Long id, NotesUpdateRequest request) {
        log.info("Adding notes to consultation: {}", id);

        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with id: " + id));

        // Append admin notes to existing notes
        String existingNotes = consultation.getNotes() != null ? consultation.getNotes() : "";
        consultation.setNotes(existingNotes + "\nAdmin Notes: " + request.getNotes());
        consultation.setUpdatedAt(LocalDateTime.now());

        Consultation savedConsultation = consultationRepository.save(consultation);

        return convertToConsultationDetailDTO(savedConsultation);
    }

    public void sendConsultationReminder(Long id) {
        log.info("Sending consultation reminder for id: {}", id);

        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with id: " + id));

        if (consultation.getStatus() != ConsultationStatus.CONFIRMED) {
            throw new ConsultationException("Can only send reminders for confirmed consultations");
        }

        try {
            emailNotificationService.sendConsultationReminder(consultation);
            log.info("Consultation reminder sent successfully for id: {}", id);
        } catch (Exception e) {
            log.error("Failed to send consultation reminder for id: {}", id, e);
            throw new ConsultationException("Failed to send consultation reminder");
        }
    }

    public DashboardStatsDTO getConsultationStats() {
        log.info("Getting consultation statistics");

        long totalPending = consultationRepository.countByStatus(ConsultationStatus.REQUESTED);
        long totalConfirmed = consultationRepository.countByStatus(ConsultationStatus.CONFIRMED);
        long totalCompleted = consultationRepository.countByStatus(ConsultationStatus.COMPLETED);

        LocalDate today = LocalDate.now();
        List<Consultation> todayConsultations = consultationRepository.findByConsultationDateAndStatusNot(today, ConsultationStatus.CANCELLED);

        LocalDate weekStart = today.minusDays(7);
        List<Consultation> weekConsultations = consultationRepository.findByConsultationDateBetweenAndStatus(weekStart, today, ConsultationStatus.CONFIRMED);

        return DashboardStatsDTO.builder()
                .totalPending((int) totalPending)
                .totalConfirmed((int) totalConfirmed)
                .totalCompleted((int) totalCompleted)
                .todayConsultations(todayConsultations.size())
                .weekConsultations(weekConsultations.size())
                .build();
    }

    public ConsultationDetailDTO rescheduleConsultation(Long id, RescheduleRequest request) {
        log.info("Rescheduling consultation {} to {}", id, request.getNewDate());

        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with id: " + id));

        // Check if the new date is available
        boolean dayAvailable = isDayAvailable(request.getNewDate());
        if (!dayAvailable) {
            throw new ConsultationException("The requested date is fully booked (max 5 consultations per day)");
        }

        LocalDate oldDate = consultation.getConsultationDate();

        consultation.setConsultationDate(request.getNewDate());
        // Keep the original time - no need to change it since we're only managing by day

        // Append reschedule info to notes
        String existingNotes = consultation.getNotes() != null ? consultation.getNotes() : "";
        consultation.setNotes(existingNotes + "\nRescheduled: " + request.getReason());
        consultation.setUpdatedAt(LocalDateTime.now());

        Consultation savedConsultation = consultationRepository.save(consultation);

        // Send notification if requested
        if (Boolean.TRUE.equals(request.getNotifyCustomer())) {
            try {
                sendRescheduleNotification(savedConsultation, oldDate, request.getReason());
            } catch (Exception e) {
                log.error("Failed to send reschedule notification for consultation: {}", id, e);
            }
        }

        return convertToConsultationDetailDTO(savedConsultation);
    }

    // Private helper methods
    private Specification<Consultation> createConsultationSpecification(ConsultationFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
                String searchTerm = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate searchPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("guestName")), searchTerm),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("guestEmail")), searchTerm),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("topic")), searchTerm)
                );
                predicates.add(searchPredicate);
            }

            if (filter.getStatus() != null && !filter.getStatus().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), ConsultationStatus.valueOf(filter.getStatus())));
            }

            if (filter.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("consultationDate"), filter.getStartDate()));
            }

            if (filter.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("consultationDate"), filter.getEndDate()));
            }

            if (filter.getTopic() != null && !filter.getTopic().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("topic")),
                        "%" + filter.getTopic().toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private ConsultationDTO convertToConsultationDTO(Consultation consultation) {
        return ConsultationDTO.builder()
                .id(consultation.getId())
                .customerName(consultation.getGuestName())
                .customerEmail(consultation.getGuestEmail())
                .date(consultation.getConsultationDate())
                .topic(consultation.getTopic())
                .status(consultation.getStatus().name())
                .build();
    }

    private ConsultationDetailDTO convertToConsultationDetailDTO(Consultation consultation) {
        return ConsultationDetailDTO.builder()
                .id(consultation.getId())
                .customerName(consultation.getGuestName())
                .customerEmail(consultation.getGuestEmail())
                .customerPhone(consultation.getGuestPhone())
                .customerId(consultation.getUser() != null ? consultation.getUser().getUserId() : null)
                .date(consultation.getConsultationDate())
                .topic(consultation.getTopic())
                .consultationType(consultation.getConsultationType() != null ? consultation.getConsultationType().name() : null)
                .notes(consultation.getNotes())
                .adminNotes("") // Since adminNotes field doesn't exist, return empty string
                .status(consultation.getStatus().name())
                .createdAt(consultation.getCreatedAt())
                .build();
    }

    private boolean isDayAvailable(LocalDate date) {
        // Simple check - max 5 active bookings per day
        Long currentBookings = consultationRepository.countActiveConsultationsByDate(date);
        return currentBookings < 5;
    }

    private void sendStatusUpdateNotification(Consultation consultation, ConsultationStatus oldStatus, ConsultationStatus newStatus) {
        // TODO: Implement email notification for status update
        log.info("Sending status update notification for consultation {} from {} to {}",
                consultation.getId(), oldStatus, newStatus);
    }

    private void sendRescheduleNotification(Consultation consultation, LocalDate oldDate, String reason) {
        // TODO: Implement email notification for reschedule
        log.info("Sending reschedule notification for consultation {} from {} to {}",
                consultation.getId(), oldDate, consultation.getConsultationDate());
    }
}
