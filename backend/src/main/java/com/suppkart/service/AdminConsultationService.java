package com.suppkart.service;

import com.suppkart.dto.admin.consultation.*;
import com.suppkart.exception.ConsultationException;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.entity.Consultation;
import com.suppkart.model.entity.ConsultationSlot;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.ConsultationStatus;
import com.suppkart.repository.ConsultationRepository;
import com.suppkart.repository.ConsultationSlotRepository;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminConsultationService {

    private final ConsultationRepository consultationRepository;
    private final ConsultationSlotRepository consultationSlotRepository;
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

    public List<AvailableSlotDTO> getAvailableSlots(LocalDate date) {
        log.info("Getting available slots for date: {}", date);
        
        // Convert LocalDate to dayOfWeek (1=Monday, 7=Sunday)
        int dayOfWeek = date.getDayOfWeek().getValue();
        List<ConsultationSlot> slots = consultationSlotRepository.findByDayOfWeekOrderByStartTimeAsc(dayOfWeek);
        
        return slots.stream()
                .map(slot -> convertToAvailableSlotDTO(slot, date))
                .toList();
    }

    public void updateAvailableSlots(List<SlotUpdateRequest> slots) {
        log.info("Updating {} available slots", slots.size());
        
        for (SlotUpdateRequest slotRequest : slots) {
            if (slotRequest.getId() != null) {
                // Update existing slot
                ConsultationSlot slot = consultationSlotRepository.findById(slotRequest.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Consultation slot not found with id: " + slotRequest.getId()));
                
                updateSlotFromRequest(slot, slotRequest);
                consultationSlotRepository.save(slot);
            } else {
                // Create new slot
                ConsultationSlot newSlot = createSlotFromRequest(slotRequest);
                consultationSlotRepository.save(newSlot);
            }
        }
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

    public byte[] exportConsultationCalendar(LocalDate startDate, LocalDate endDate) {
        log.info("Exporting consultation calendar from {} to {}", startDate, endDate);
        
        Page<Consultation> consultations = consultationRepository.findByConsultationDateBetween(startDate, endDate, Pageable.unpaged());
        
        // TODO: Implement calendar export logic (ICS format)
        // For now, return empty byte array
        return new byte[0];
    }

    public ConsultationDetailDTO rescheduleConsultation(Long id, RescheduleRequest request) {
        log.info("Rescheduling consultation {} to {} at {}", id, request.getNewDate(), request.getNewTime());
        
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with id: " + id));
        
        // Check if the new slot is available
        boolean slotAvailable = isSlotAvailable(request.getNewDate(), request.getNewTime());
        if (!slotAvailable) {
            throw new ConsultationException("The requested time slot is not available");
        }
        
        LocalDate oldDate = consultation.getConsultationDate();
        LocalTime oldTime = consultation.getConsultationTime();
        
        consultation.setConsultationDate(request.getNewDate());
        consultation.setConsultationTime(request.getNewTime());
        
        // Append reschedule info to notes
        String existingNotes = consultation.getNotes() != null ? consultation.getNotes() : "";
        consultation.setNotes(existingNotes + "\nRescheduled: " + request.getReason());
        consultation.setUpdatedAt(LocalDateTime.now());
        
        Consultation savedConsultation = consultationRepository.save(consultation);
        
        // Send notification if requested
        if (Boolean.TRUE.equals(request.getNotifyCustomer())) {
            try {
                sendRescheduleNotification(savedConsultation, oldDate, oldTime, request.getReason());
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
                .time(consultation.getConsultationTime())
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
                .time(consultation.getConsultationTime())
                .topic(consultation.getTopic())
                .consultationType(consultation.getConsultationType() != null ? consultation.getConsultationType().name() : null)
                .notes(consultation.getNotes())
                .adminNotes("") // Since adminNotes field doesn't exist, return empty string
                .status(consultation.getStatus().name())
                .createdAt(consultation.getCreatedAt())
                .build();
    }

    private AvailableSlotDTO convertToAvailableSlotDTO(ConsultationSlot slot, LocalDate date) {
        Long currentBookings = consultationRepository.countByConsultationDateAndConsultationTimeBetween(
                date, slot.getStartTime(), slot.getEndTime());
        
        return AvailableSlotDTO.builder()
                .id(slot.getId())
                .date(date)
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isAvailable(slot.getIsAvailable())
                .maxBookings(slot.getMaxBookings())
                .currentBookings(currentBookings.intValue())
                .build();
    }

    private void updateSlotFromRequest(ConsultationSlot slot, SlotUpdateRequest request) {
        if (request.getDate() != null) {
            // Convert date to dayOfWeek (1=Monday, 7=Sunday)
            slot.setDayOfWeek(request.getDate().getDayOfWeek().getValue());
        }
        if (request.getStartTime() != null) {
            slot.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            slot.setEndTime(request.getEndTime());
        }
        if (request.getIsAvailable() != null) {
            slot.setIsAvailable(request.getIsAvailable());
        }
        if (request.getMaxBookings() != null) {
            slot.setMaxBookings(request.getMaxBookings());
        }
    }

    private ConsultationSlot createSlotFromRequest(SlotUpdateRequest request) {
        ConsultationSlot slot = new ConsultationSlot();
        if (request.getDate() != null) {
            // Convert date to dayOfWeek (1=Monday, 7=Sunday)
            slot.setDayOfWeek(request.getDate().getDayOfWeek().getValue());
        }
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);
        slot.setMaxBookings(request.getMaxBookings() != null ? request.getMaxBookings() : 1);
        return slot;
    }

    private boolean isSlotAvailable(LocalDate date, LocalTime time) {
        // Convert LocalDate to dayOfWeek (1=Monday, 7=Sunday)
        int dayOfWeek = date.getDayOfWeek().getValue();
        
        // Check if there are available slots for this time
        List<ConsultationSlot> slots = consultationSlotRepository.findByDayOfWeekAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                dayOfWeek, time, time);
        
        for (ConsultationSlot slot : slots) {
            if (slot.getIsAvailable()) {
                Long currentBookings = consultationRepository.countByConsultationDateAndConsultationTimeBetween(
                        date, time, time.plusHours(1)); // Assume 1 hour consultation
                if (currentBookings < slot.getMaxBookings()) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private void sendStatusUpdateNotification(Consultation consultation, ConsultationStatus oldStatus, ConsultationStatus newStatus) {
        // TODO: Implement email notification for status update
        log.info("Sending status update notification for consultation {} from {} to {}", 
                consultation.getId(), oldStatus, newStatus);
    }

    private void sendRescheduleNotification(Consultation consultation, LocalDate oldDate, LocalTime oldTime, String reason) {
        // TODO: Implement email notification for reschedule
        log.info("Sending reschedule notification for consultation {} from {}/{} to {}/{}", 
                consultation.getId(), oldDate, oldTime, consultation.getConsultationDate(), consultation.getConsultationTime());
    }
}
