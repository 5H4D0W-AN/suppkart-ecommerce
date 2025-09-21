package com.suppkart.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.Consultation;
import com.suppkart.model.enums.ConsultationStatus;

/**
 * Repository interface for Consultation entity
 */
@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long>, JpaSpecificationExecutor<Consultation> {

    /**
     * Find consultations by status
     */
    List<Consultation> findByStatus(ConsultationStatus status);

    /**
     * Find consultations by status with pagination
     */
    Page<Consultation> findByStatus(ConsultationStatus status, Pageable pageable);

    /**
     * Find consultations within date range and status
     */
    List<Consultation> findByConsultationDateBetweenAndStatus(
            LocalDate startDate, LocalDate endDate, ConsultationStatus status);


    /**
     * Find consultations for a specific date excluding cancelled ones
     */
    List<Consultation> findByConsultationDateAndStatusNot(LocalDate date, ConsultationStatus status);

    /**
     * Count consultations for a specific date and time slot
     */
    @Query("SELECT COUNT(c) FROM Consultation c WHERE c.consultationDate = :date " +
           "AND c.consultationTime >= :startTime AND c.consultationTime < :endTime " +
           "AND c.status != 'CANCELLED'")
    Long countByConsultationDateAndConsultationTimeBetween(
            @Param("date") LocalDate date, 
            @Param("startTime") LocalTime startTime, 
            @Param("endTime") LocalTime endTime);

    /**
     * Find consultations by guest email
     */
    List<Consultation> findByGuestEmailOrderByConsultationDateDescConsultationTimeDesc(String guestEmail);


    /**
     * Find consultations for a specific date excluding cancelled ones
     */
    Page<Consultation> findByConsultationDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Find consultations by status and date range
     */
    Page<Consultation> findByStatusAndConsultationDateBetween(
            ConsultationStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Count consultations by status
     */
    long countByStatus(ConsultationStatus status);

    /**
     * Search consultations by guest name or email (case insensitive)
     */
    Page<Consultation> findByGuestNameContainingIgnoreCaseOrGuestEmailContainingIgnoreCase(
            String guestName, String guestEmail, Pageable pageable);

    /**
     * Find consultations by user ID ordered by date and time descending
     */
    List<Consultation> findByUserUserIdOrderByConsultationDateDescConsultationTimeDesc(Long userId);
}
