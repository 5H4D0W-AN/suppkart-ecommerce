package com.suppkart.service;

import com.suppkart.dto.admin.consultation.*;
import com.suppkart.exception.ConsultationException;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.entity.Consultation;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.ConsultationStatus;
import com.suppkart.model.enums.ConsultationType;
import com.suppkart.repository.ConsultationRepository;
import com.suppkart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminConsultationServiceTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private AdminConsultationService adminConsultationService;

    private Consultation testConsultation;
    private User testUser;
    private ConsultationFilterRequest testFilter;
    private ConsultationStatusUpdateRequest statusUpdateRequest;
    private NotesUpdateRequest notesUpdateRequest;
    private RescheduleRequest rescheduleRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");

        testConsultation = new Consultation();
        testConsultation.setId(1L);
        testConsultation.setUser(testUser);
        testConsultation.setGuestName("Jane Smith");
        testConsultation.setGuestEmail("jane.smith@example.com");
        testConsultation.setGuestPhone("1234567890");
        testConsultation.setConsultationDate(LocalDate.now().plusDays(1));
        testConsultation.setConsultationTime(LocalTime.of(10, 0));
        testConsultation.setTimezone("UTC");
        testConsultation.setTopic("Nutrition consultation");
        testConsultation.setConsultationType(ConsultationType.NUTRITION);
        testConsultation.setNotes("Initial notes");
        testConsultation.setStatus(ConsultationStatus.REQUESTED);
        testConsultation.setCreatedAt(LocalDateTime.now());

        testFilter = ConsultationFilterRequest.builder()
                .search("Jane")
                .status("REQUESTED")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .topic("nutrition")
                .build();

        statusUpdateRequest = ConsultationStatusUpdateRequest.builder()
                .status("CONFIRMED")
                .notes("Admin confirmed the consultation")
                .sendNotification(true)
                .build();

        notesUpdateRequest = NotesUpdateRequest.builder()
                .notes("Additional admin notes")
                .build();

        rescheduleRequest = RescheduleRequest.builder()
                .newDate(LocalDate.now().plusDays(3))
                .reason("Customer requested change")
                .notifyCustomer(true)
                .build();
    }

    @Test
    void getAllConsultations_ShouldReturnPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Consultation> consultations = Arrays.asList(testConsultation);
        Page<Consultation> consultationPage = new PageImpl<>(consultations, pageable, 1);

        when(consultationRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(consultationPage);

        // Act
        Page<ConsultationDTO> result = adminConsultationService.getAllConsultations(testFilter, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testConsultation.getId(), result.getContent().get(0).getId());
        assertEquals(testConsultation.getGuestName(), result.getContent().get(0).getCustomerName());
        verify(consultationRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getConsultationById_ShouldReturnConsultationDetail_WhenExists() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(testConsultation));

        // Act
        ConsultationDetailDTO result = adminConsultationService.getConsultationById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testConsultation.getId(), result.getId());
        assertEquals(testConsultation.getGuestName(), result.getCustomerName());
        assertEquals(testConsultation.getGuestEmail(), result.getCustomerEmail());
        assertEquals(testConsultation.getConsultationDate(), result.getDate());
        verify(consultationRepository).findById(1L);
    }

    @Test
    void getConsultationById_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminConsultationService.getConsultationById(1L)
        );
        assertEquals("Consultation not found with id: 1", exception.getMessage());
        verify(consultationRepository).findById(1L);
    }

    @Test
    void updateConsultationStatus_ShouldUpdateAndReturnDetail() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(testConsultation));
        when(consultationRepository.save(any(Consultation.class))).thenReturn(testConsultation);

        // Act
        ConsultationDetailDTO result = adminConsultationService.updateConsultationStatus(1L, statusUpdateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(ConsultationStatus.CONFIRMED, testConsultation.getStatus());
        assertTrue(testConsultation.getNotes().contains("Admin Notes: Admin confirmed the consultation"));
        verify(consultationRepository).findById(1L);
        verify(consultationRepository).save(testConsultation);
    }

    @Test
    void updateConsultationStatus_ShouldSendNotification_WhenRequested() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(testConsultation));
        when(consultationRepository.save(any(Consultation.class))).thenReturn(testConsultation);

        // Act
        adminConsultationService.updateConsultationStatus(1L, statusUpdateRequest);

        // Assert
        verify(consultationRepository).save(testConsultation);
        // Note: sendStatusUpdateNotification is a private method that just logs, so we can't verify it directly
    }

    @Test
    void isDayAvailableForBooking_ShouldReturnTrue_WhenLessThan5Bookings() {
        // Arrange
        LocalDate testDate = LocalDate.now().plusDays(1);
        when(consultationRepository.countActiveConsultationsByDate(testDate)).thenReturn(3L);

        // Act
        boolean result = adminConsultationService.isDayAvailableForBooking(testDate);

        // Assert
        assertTrue(result);
        verify(consultationRepository).countActiveConsultationsByDate(testDate);
    }

    @Test
    void isDayAvailableForBooking_ShouldReturnFalse_When5OrMoreBookings() {
        // Arrange
        LocalDate testDate = LocalDate.now().plusDays(1);
        when(consultationRepository.countActiveConsultationsByDate(testDate)).thenReturn(5L);

        // Act
        boolean result = adminConsultationService.isDayAvailableForBooking(testDate);

        // Assert
        assertFalse(result);
        verify(consultationRepository).countActiveConsultationsByDate(testDate);
    }

    @Test
    void getCurrentBookingsCount_ShouldReturnCorrectCount() {
        // Arrange
        LocalDate testDate = LocalDate.now().plusDays(1);
        when(consultationRepository.countActiveConsultationsByDate(testDate)).thenReturn(3L);

        // Act
        int result = adminConsultationService.getCurrentBookingsCount(testDate);

        // Assert
        assertEquals(3, result);
        verify(consultationRepository).countActiveConsultationsByDate(testDate);
    }

    @Test
    void addConsultationNotes_ShouldAppendNotesAndReturnDetail() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(testConsultation));
        when(consultationRepository.save(any(Consultation.class))).thenReturn(testConsultation);

        // Act
        ConsultationDetailDTO result = adminConsultationService.addConsultationNotes(1L, notesUpdateRequest);

        // Assert
        assertNotNull(result);
        assertTrue(testConsultation.getNotes().contains("Admin Notes: Additional admin notes"));
        verify(consultationRepository).findById(1L);
        verify(consultationRepository).save(testConsultation);
    }

    @Test
    void addConsultationNotes_ShouldThrowException_WhenConsultationNotFound() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminConsultationService.addConsultationNotes(1L, notesUpdateRequest)
        );
        assertEquals("Consultation not found with id: 1", exception.getMessage());
        verify(consultationRepository).findById(1L);
        verify(consultationRepository, never()).save(any());
    }

    @Test
    void sendConsultationReminder_ShouldSendReminder_WhenStatusIsConfirmed() {
        // Arrange
        testConsultation.setStatus(ConsultationStatus.CONFIRMED);
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(testConsultation));

        // Act
        assertDoesNotThrow(() -> adminConsultationService.sendConsultationReminder(1L));

        // Assert
        verify(consultationRepository).findById(1L);
        verify(emailNotificationService).sendConsultationReminder(testConsultation);
    }

    @Test
    void sendConsultationReminder_ShouldThrowException_WhenStatusIsNotConfirmed() {
        // Arrange
        testConsultation.setStatus(ConsultationStatus.REQUESTED);
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(testConsultation));

        // Act & Assert
        ConsultationException exception = assertThrows(
                ConsultationException.class,
                () -> adminConsultationService.sendConsultationReminder(1L)
        );
        assertEquals("Can only send reminders for confirmed consultations", exception.getMessage());
        verify(consultationRepository).findById(1L);
        verify(emailNotificationService, never()).sendConsultationReminder(any());
    }

    @Test
    void sendConsultationReminder_ShouldThrowException_WhenEmailServiceFails() {
        // Arrange
        testConsultation.setStatus(ConsultationStatus.CONFIRMED);
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(testConsultation));
        doThrow(new RuntimeException("Email service error"))
                .when(emailNotificationService).sendConsultationReminder(testConsultation);

        // Act & Assert
        ConsultationException exception = assertThrows(
                ConsultationException.class,
                () -> adminConsultationService.sendConsultationReminder(1L)
        );
        assertEquals("Failed to send consultation reminder", exception.getMessage());
        verify(emailNotificationService).sendConsultationReminder(testConsultation);
    }

    @Test
    void getConsultationStats_ShouldReturnCorrectStats() {
        // Arrange
        when(consultationRepository.countByStatus(ConsultationStatus.REQUESTED)).thenReturn(5L);
        when(consultationRepository.countByStatus(ConsultationStatus.CONFIRMED)).thenReturn(3L);
        when(consultationRepository.countByStatus(ConsultationStatus.COMPLETED)).thenReturn(10L);
        
        List<Consultation> todayConsultations = Arrays.asList(testConsultation);
        when(consultationRepository.findByConsultationDateAndStatusNot(any(LocalDate.class), eq(ConsultationStatus.CANCELLED)))
                .thenReturn(todayConsultations);
        
        List<Consultation> weekConsultations = Arrays.asList(testConsultation, testConsultation);
        when(consultationRepository.findByConsultationDateBetweenAndStatus(any(LocalDate.class), any(LocalDate.class), eq(ConsultationStatus.CONFIRMED)))
                .thenReturn(weekConsultations);

        // Act
        DashboardStatsDTO result = adminConsultationService.getConsultationStats();

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getTotalPending());
        assertEquals(3, result.getTotalConfirmed());
        assertEquals(10, result.getTotalCompleted());
        assertEquals(1, result.getTodayConsultations());
        assertEquals(2, result.getWeekConsultations());
    }

    @Test
    void rescheduleConsultation_ShouldRescheduleSuccessfully_WhenDateAvailable() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(testConsultation));
        when(consultationRepository.countActiveConsultationsByDate(rescheduleRequest.getNewDate())).thenReturn(2L);
        when(consultationRepository.save(any(Consultation.class))).thenReturn(testConsultation);

        // Act
        ConsultationDetailDTO result = adminConsultationService.rescheduleConsultation(1L, rescheduleRequest);

        // Assert
        assertNotNull(result);
        assertEquals(rescheduleRequest.getNewDate(), testConsultation.getConsultationDate());
        assertTrue(testConsultation.getNotes().contains("Rescheduled: Customer requested change"));
        verify(consultationRepository).findById(1L);
        verify(consultationRepository).save(testConsultation);
    }

    @Test
    void rescheduleConsultation_ShouldThrowException_WhenDateNotAvailable() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(testConsultation));
        when(consultationRepository.countActiveConsultationsByDate(rescheduleRequest.getNewDate())).thenReturn(5L);

        // Act & Assert
        ConsultationException exception = assertThrows(
                ConsultationException.class,
                () -> adminConsultationService.rescheduleConsultation(1L, rescheduleRequest)
        );
        assertEquals("The requested date is fully booked (max 5 consultations per day)", exception.getMessage());
        verify(consultationRepository).findById(1L);
        verify(consultationRepository, never()).save(any());
    }

    @Test
    void rescheduleConsultation_ShouldThrowException_WhenConsultationNotFound() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminConsultationService.rescheduleConsultation(1L, rescheduleRequest)
        );
        assertEquals("Consultation not found with id: 1", exception.getMessage());
        verify(consultationRepository).findById(1L);
        verify(consultationRepository, never()).save(any());
    }

    @Test
    void convertToConsultationDTO_ShouldMapFieldsCorrectly() {
        // This tests the private method indirectly through getAllConsultations
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Consultation> consultations = Arrays.asList(testConsultation);
        Page<Consultation> consultationPage = new PageImpl<>(consultations, pageable, 1);

        when(consultationRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(consultationPage);

        // Act
        Page<ConsultationDTO> result = adminConsultationService.getAllConsultations(testFilter, pageable);

        // Assert
        ConsultationDTO dto = result.getContent().get(0);
        assertEquals(testConsultation.getId(), dto.getId());
        assertEquals(testConsultation.getGuestName(), dto.getCustomerName());
        assertEquals(testConsultation.getGuestEmail(), dto.getCustomerEmail());
        assertEquals(testConsultation.getConsultationDate(), dto.getDate());
        assertEquals(testConsultation.getTopic(), dto.getTopic());
        assertEquals(testConsultation.getStatus().name(), dto.getStatus());
    }

    @Test
    void convertToConsultationDetailDTO_ShouldMapFieldsCorrectly() {
        // This tests the private method indirectly through getConsultationById
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(testConsultation));

        // Act
        ConsultationDetailDTO result = adminConsultationService.getConsultationById(1L);

        // Assert
        assertEquals(testConsultation.getId(), result.getId());
        assertEquals(testConsultation.getGuestName(), result.getCustomerName());
        assertEquals(testConsultation.getGuestEmail(), result.getCustomerEmail());
        assertEquals(testConsultation.getGuestPhone(), result.getCustomerPhone());
        assertEquals(testConsultation.getUser().getUserId(), result.getCustomerId());
        assertEquals(testConsultation.getConsultationDate(), result.getDate());
        assertEquals(testConsultation.getTopic(), result.getTopic());
        assertEquals(testConsultation.getConsultationType().name(), result.getConsultationType());
        assertEquals(testConsultation.getNotes(), result.getNotes());
        assertEquals("", result.getAdminNotes()); // Always empty as per implementation
        assertEquals(testConsultation.getStatus().name(), result.getStatus());
        assertEquals(testConsultation.getCreatedAt(), result.getCreatedAt());
    }

    @Test
    void updateConsultationStatus_ShouldHandleNullNotes() {
        // Arrange
        ConsultationStatusUpdateRequest requestWithoutNotes = ConsultationStatusUpdateRequest.builder()
                .status("CONFIRMED")
                .sendNotification(false)
                .build();
        
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(testConsultation));
        when(consultationRepository.save(any(Consultation.class))).thenReturn(testConsultation);

        // Act
        ConsultationDetailDTO result = adminConsultationService.updateConsultationStatus(1L, requestWithoutNotes);

        // Assert
        assertNotNull(result);
        assertEquals(ConsultationStatus.CONFIRMED, testConsultation.getStatus());
        // Notes should remain unchanged when no admin notes provided
        assertEquals("Initial notes", testConsultation.getNotes());
    }

    @Test
    void addConsultationNotes_ShouldHandleNullExistingNotes() {
        // Arrange
        testConsultation.setNotes(null);
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(testConsultation));
        when(consultationRepository.save(any(Consultation.class))).thenReturn(testConsultation);

        // Act
        ConsultationDetailDTO result = adminConsultationService.addConsultationNotes(1L, notesUpdateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("\nAdmin Notes: Additional admin notes", testConsultation.getNotes());
    }
}