package com.suppkart.controller;

import com.suppkart.dto.admin.consultation.*;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.exception.ConsultationException;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.service.AdminConsultationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminConsultationControllerTest {

    @Mock
    private AdminConsultationService adminConsultationService;

    @InjectMocks
    private AdminConsultationController adminConsultationController;

    private ConsultationDTO consultationDTO;
    private ConsultationDetailDTO consultationDetailDTO;
    private DashboardStatsDTO dashboardStatsDTO;
    private ConsultationStatusUpdateRequest statusUpdateRequest;
    private NotesUpdateRequest notesUpdateRequest;
    private RescheduleRequest rescheduleRequest;

    @BeforeEach
    void setUp() {
        consultationDTO = ConsultationDTO.builder()
                .id(1L)
                .customerName("Jane Smith")
                .customerEmail("jane.smith@example.com")
                .date(LocalDate.now().plusDays(1))
                .topic("Nutrition consultation")
                .status("REQUESTED")
                .build();

        consultationDetailDTO = ConsultationDetailDTO.builder()
                .id(1L)
                .customerName("Jane Smith")
                .customerEmail("jane.smith@example.com")
                .customerPhone("1234567890")
                .customerId(1L)
                .date(LocalDate.now().plusDays(1))
                .topic("Nutrition consultation")
                .consultationType("NUTRITION")
                .notes("Initial notes")
                .adminNotes("")
                .status("REQUESTED")
                .createdAt(LocalDateTime.now())
                .build();

        dashboardStatsDTO = DashboardStatsDTO.builder()
                .totalPending(5)
                .totalConfirmed(3)
                .totalCompleted(10)
                .todayConsultations(2)
                .weekConsultations(8)
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
        List<ConsultationDTO> consultations = Arrays.asList(consultationDTO);
        Page<ConsultationDTO> consultationPage = new PageImpl<>(consultations, PageRequest.of(0, 20), 1);
        
        when(adminConsultationService.getAllConsultations(any(ConsultationFilterRequest.class), any()))
                .thenReturn(consultationPage);

        // Act
        ResponseEntity<ApiResponse<Page<ConsultationDTO>>> response = adminConsultationController.getAllConsultations(
                "Jane", "REQUESTED", null, null, null, 0, 20);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Consultations retrieved successfully", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().getTotalElements());
        verify(adminConsultationService).getAllConsultations(any(ConsultationFilterRequest.class), any());
    }

    @Test
    void getAllConsultations_ShouldHandleException() {
        // Arrange
        when(adminConsultationService.getAllConsultations(any(ConsultationFilterRequest.class), any()))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<ApiResponse<Page<ConsultationDTO>>> response = adminConsultationController.getAllConsultations(
                null, null, null, null, null, 0, 20);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to retrieve consultations", response.getBody().getMessage());
        verify(adminConsultationService).getAllConsultations(any(ConsultationFilterRequest.class), any());
    }

    @Test
    void getConsultationById_ShouldReturnConsultationDetail() {
        // Arrange
        when(adminConsultationService.getConsultationById(1L)).thenReturn(consultationDetailDTO);

        // Act
        ResponseEntity<ApiResponse<ConsultationDetailDTO>> response = adminConsultationController.getConsultationById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Consultation retrieved successfully", response.getBody().getMessage());
        assertEquals(1L, response.getBody().getData().getId());
        verify(adminConsultationService).getConsultationById(1L);
    }

    @Test
    void getConsultationById_ShouldHandleNotFound() {
        // Arrange
        when(adminConsultationService.getConsultationById(1L))
                .thenThrow(new ResourceNotFoundException("Consultation not found"));

        // Act
        ResponseEntity<ApiResponse<ConsultationDetailDTO>> response = adminConsultationController.getConsultationById(1L);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to retrieve consultation", response.getBody().getMessage());
        verify(adminConsultationService).getConsultationById(1L);
    }

    @Test
    void updateConsultationStatus_ShouldUpdateSuccessfully() {
        // Arrange
        when(adminConsultationService.updateConsultationStatus(eq(1L), any(ConsultationStatusUpdateRequest.class)))
                .thenReturn(consultationDetailDTO);

        // Act
        ResponseEntity<ApiResponse<ConsultationDetailDTO>> response = 
                adminConsultationController.updateConsultationStatus(1L, statusUpdateRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Consultation status updated successfully", response.getBody().getMessage());
        assertEquals(1L, response.getBody().getData().getId());
        verify(adminConsultationService).updateConsultationStatus(eq(1L), any(ConsultationStatusUpdateRequest.class));
    }

    @Test
    void updateConsultationStatus_ShouldHandleException() {
        // Arrange
        when(adminConsultationService.updateConsultationStatus(eq(1L), any(ConsultationStatusUpdateRequest.class)))
                .thenThrow(new RuntimeException("Update failed"));

        // Act
        ResponseEntity<ApiResponse<ConsultationDetailDTO>> response = 
                adminConsultationController.updateConsultationStatus(1L, statusUpdateRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to update consultation status", response.getBody().getMessage());
        verify(adminConsultationService).updateConsultationStatus(eq(1L), any(ConsultationStatusUpdateRequest.class));
    }

    @Test
    void checkDateAvailability_ShouldReturnAvailability() {
        // Arrange
        LocalDate testDate = LocalDate.now().plusDays(1);
        when(adminConsultationService.isDayAvailableForBooking(testDate)).thenReturn(true);

        // Act
        ResponseEntity<ApiResponse<Boolean>> response = adminConsultationController.checkDateAvailability(testDate);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Date availability checked successfully", response.getBody().getMessage());
        assertTrue(response.getBody().getData());
        verify(adminConsultationService).isDayAvailableForBooking(testDate);
    }

    @Test
    void checkDateAvailability_ShouldHandleException() {
        // Arrange
        LocalDate testDate = LocalDate.now().plusDays(1);
        when(adminConsultationService.isDayAvailableForBooking(testDate))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<ApiResponse<Boolean>> response = adminConsultationController.checkDateAvailability(testDate);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to check date availability", response.getBody().getMessage());
        verify(adminConsultationService).isDayAvailableForBooking(testDate);
    }

    @Test
    void getCurrentBookingsCount_ShouldReturnCount() {
        // Arrange
        LocalDate testDate = LocalDate.now().plusDays(1);
        when(adminConsultationService.getCurrentBookingsCount(testDate)).thenReturn(3);

        // Act
        ResponseEntity<ApiResponse<Integer>> response = adminConsultationController.getCurrentBookingsCount(testDate);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Bookings count retrieved successfully", response.getBody().getMessage());
        assertEquals(3, response.getBody().getData());
        verify(adminConsultationService).getCurrentBookingsCount(testDate);
    }

    @Test
    void getCurrentBookingsCount_ShouldHandleException() {
        // Arrange
        LocalDate testDate = LocalDate.now().plusDays(1);
        when(adminConsultationService.getCurrentBookingsCount(testDate))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<ApiResponse<Integer>> response = adminConsultationController.getCurrentBookingsCount(testDate);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to retrieve bookings count", response.getBody().getMessage());
        verify(adminConsultationService).getCurrentBookingsCount(testDate);
    }

    @Test
    void addConsultationNotes_ShouldAddNotesSuccessfully() {
        // Arrange
        when(adminConsultationService.addConsultationNotes(eq(1L), any(NotesUpdateRequest.class)))
                .thenReturn(consultationDetailDTO);

        // Act
        ResponseEntity<ApiResponse<ConsultationDetailDTO>> response = 
                adminConsultationController.addConsultationNotes(1L, notesUpdateRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Consultation notes updated successfully", response.getBody().getMessage());
        assertEquals(1L, response.getBody().getData().getId());
        verify(adminConsultationService).addConsultationNotes(eq(1L), any(NotesUpdateRequest.class));
    }

    @Test
    void addConsultationNotes_ShouldHandleException() {
        // Arrange
        when(adminConsultationService.addConsultationNotes(eq(1L), any(NotesUpdateRequest.class)))
                .thenThrow(new RuntimeException("Update failed"));

        // Act
        ResponseEntity<ApiResponse<ConsultationDetailDTO>> response = 
                adminConsultationController.addConsultationNotes(1L, notesUpdateRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to update consultation notes", response.getBody().getMessage());
        verify(adminConsultationService).addConsultationNotes(eq(1L), any(NotesUpdateRequest.class));
    }

    @Test
    void sendConsultationReminder_ShouldSendSuccessfully() {
        // Arrange
        doNothing().when(adminConsultationService).sendConsultationReminder(1L);

        // Act
        ResponseEntity<ApiResponse<String>> response = adminConsultationController.sendConsultationReminder(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Consultation reminder sent successfully", response.getBody().getMessage());
        assertEquals("Success", response.getBody().getData());
        verify(adminConsultationService).sendConsultationReminder(1L);
    }

    @Test
    void sendConsultationReminder_ShouldHandleConsultationException() {
        // Arrange
        doThrow(new ConsultationException("Can only send reminders for confirmed consultations"))
                .when(adminConsultationService).sendConsultationReminder(1L);

        // Act
        ResponseEntity<ApiResponse<String>> response = adminConsultationController.sendConsultationReminder(1L);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to send consultation reminder", response.getBody().getMessage());
        verify(adminConsultationService).sendConsultationReminder(1L);
    }

    @Test
    void getConsultationStats_ShouldReturnStats() {
        // Arrange
        when(adminConsultationService.getConsultationStats()).thenReturn(dashboardStatsDTO);

        // Act
        ResponseEntity<ApiResponse<DashboardStatsDTO>> response = adminConsultationController.getConsultationStats();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Consultation statistics retrieved successfully", response.getBody().getMessage());
        assertEquals(5, response.getBody().getData().getTotalPending());
        assertEquals(3, response.getBody().getData().getTotalConfirmed());
        assertEquals(10, response.getBody().getData().getTotalCompleted());
        verify(adminConsultationService).getConsultationStats();
    }

    @Test
    void getConsultationStats_ShouldHandleException() {
        // Arrange
        when(adminConsultationService.getConsultationStats())
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<ApiResponse<DashboardStatsDTO>> response = adminConsultationController.getConsultationStats();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to retrieve consultation statistics", response.getBody().getMessage());
        verify(adminConsultationService).getConsultationStats();
    }

    @Test
    void rescheduleConsultation_ShouldRescheduleSuccessfully() {
        // Arrange
        when(adminConsultationService.rescheduleConsultation(eq(1L), any(RescheduleRequest.class)))
                .thenReturn(consultationDetailDTO);

        // Act
        ResponseEntity<ApiResponse<ConsultationDetailDTO>> response = 
                adminConsultationController.rescheduleConsultation(1L, rescheduleRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Consultation rescheduled successfully", response.getBody().getMessage());
        assertEquals(1L, response.getBody().getData().getId());
        verify(adminConsultationService).rescheduleConsultation(eq(1L), any(RescheduleRequest.class));
    }

    @Test
    void rescheduleConsultation_ShouldHandleConsultationException() {
        // Arrange
        when(adminConsultationService.rescheduleConsultation(eq(1L), any(RescheduleRequest.class)))
                .thenThrow(new ConsultationException("The requested date is fully booked"));

        // Act
        ResponseEntity<ApiResponse<ConsultationDetailDTO>> response = 
                adminConsultationController.rescheduleConsultation(1L, rescheduleRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to reschedule consultation", response.getBody().getMessage());
        verify(adminConsultationService).rescheduleConsultation(eq(1L), any(RescheduleRequest.class));
    }

    @Test
    void rescheduleConsultation_ShouldHandleResourceNotFoundException() {
        // Arrange
        when(adminConsultationService.rescheduleConsultation(eq(1L), any(RescheduleRequest.class)))
                .thenThrow(new ResourceNotFoundException("Consultation not found"));

        // Act
        ResponseEntity<ApiResponse<ConsultationDetailDTO>> response = 
                adminConsultationController.rescheduleConsultation(1L, rescheduleRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to reschedule consultation", response.getBody().getMessage());
        verify(adminConsultationService).rescheduleConsultation(eq(1L), any(RescheduleRequest.class));
    }

    @Test
    void getAllConsultations_ShouldHandleAllFilterParameters() {
        // Arrange
        List<ConsultationDTO> consultations = Arrays.asList(consultationDTO);
        Page<ConsultationDTO> consultationPage = new PageImpl<>(consultations, PageRequest.of(1, 10), 1);
        
        when(adminConsultationService.getAllConsultations(any(ConsultationFilterRequest.class), any()))
                .thenReturn(consultationPage);

        // Act
        ResponseEntity<ApiResponse<Page<ConsultationDTO>>> response = adminConsultationController.getAllConsultations(
                "Jane", "REQUESTED", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), "nutrition", 1, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(adminConsultationService).getAllConsultations(any(ConsultationFilterRequest.class), any());
    }

    @Test
    void addConsultationNotes_ShouldHandleResourceNotFoundException() {
        // Arrange
        when(adminConsultationService.addConsultationNotes(eq(1L), any(NotesUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Consultation not found"));

        // Act
        ResponseEntity<ApiResponse<ConsultationDetailDTO>> response = 
                adminConsultationController.addConsultationNotes(1L, notesUpdateRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to update consultation notes", response.getBody().getMessage());
        verify(adminConsultationService).addConsultationNotes(eq(1L), any(NotesUpdateRequest.class));
    }

    @Test
    void sendConsultationReminder_ShouldHandleResourceNotFoundException() {
        // Arrange
        doThrow(new ResourceNotFoundException("Consultation not found"))
                .when(adminConsultationService).sendConsultationReminder(1L);

        // Act
        ResponseEntity<ApiResponse<String>> response = adminConsultationController.sendConsultationReminder(1L);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to send consultation reminder", response.getBody().getMessage());
        verify(adminConsultationService).sendConsultationReminder(1L);
    }
}