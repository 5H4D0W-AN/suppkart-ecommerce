package com.suppkart.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.suppkart.dto.response.ApiResponse;
import com.suppkart.dto.response.ContactMessageResponse;
import com.suppkart.exception.ContactException;
import com.suppkart.model.enums.ContactStatus;
import com.suppkart.service.ContactService;

import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AdminContactControllerTest {

    @Mock
    private ContactService contactService;

    @InjectMocks
    private AdminContactController adminContactController;

    private ContactMessageResponse contactMessageResponse;
    private ContactService.ContactStatsResponse contactStatsResponse;
    private Page<ContactMessageResponse> contactMessagesPage;

    @BeforeEach
    void setUp() {
        // Setup contact message response
        contactMessageResponse = ContactMessageResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .subject("Product Inquiry")
                .message("I need information about protein supplements")
                .status(ContactStatus.NEW)
                .response(null)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(null)
                .respondedAt(null)
                .isResponded(false)
                .build();

        // Setup contact stats response
        contactStatsResponse = ContactService.ContactStatsResponse.builder()
                .newMessages(5L)
                .inProgressMessages(3L)
                .resolvedMessages(10L)
                .totalMessages(18L)
                .build();

        // Setup page of contact messages
        List<ContactMessageResponse> messages = Arrays.asList(contactMessageResponse);
        contactMessagesPage = new PageImpl<>(messages, PageRequest.of(0, 20), 1);
    }

    @Test
    void getContactMessages_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        when(contactService.getContactMessages(null, pageable)).thenReturn(contactMessagesPage);

        // Act
        ResponseEntity<ApiResponse<Page<ContactMessageResponse>>> response = 
                adminContactController.getContactMessages(null, 0, 20);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Contact messages retrieved successfully", response.getBody().getMessage());
        assertEquals(contactMessagesPage, response.getBody().getData());
        assertEquals(1, response.getBody().getData().getTotalElements());

        verify(contactService).getContactMessages(null, pageable);
    }

    @Test
    void getContactMessages_WithStatusFilter_Success() {
        // Arrange
        ContactStatus status = ContactStatus.NEW;
        Pageable pageable = PageRequest.of(0, 20);
        when(contactService.getContactMessages(status, pageable)).thenReturn(contactMessagesPage);

        // Act
        ResponseEntity<ApiResponse<Page<ContactMessageResponse>>> response = 
                adminContactController.getContactMessages(status, 0, 20);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Contact messages retrieved successfully", response.getBody().getMessage());
        assertEquals(contactMessagesPage, response.getBody().getData());

        verify(contactService).getContactMessages(status, pageable);
    }

    @Test
    void getContactMessages_ServiceException_ReturnsInternalServerError() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        when(contactService.getContactMessages(null, pageable))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<ApiResponse<Page<ContactMessageResponse>>> response = 
                adminContactController.getContactMessages(null, 0, 20);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to retrieve contact messages", response.getBody().getMessage());
        assertNull(response.getBody().getData());

        verify(contactService).getContactMessages(null, pageable);
    }

    @Test
    void getContactMessage_Success() {
        // Arrange
        Long messageId = 1L;
        when(contactService.getContactMessage(messageId)).thenReturn(contactMessageResponse);

        // Act
        ResponseEntity<ApiResponse<ContactMessageResponse>> response = 
                adminContactController.getContactMessage(messageId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Contact message retrieved successfully", response.getBody().getMessage());
        assertEquals(contactMessageResponse, response.getBody().getData());
        assertEquals("John Doe", response.getBody().getData().getName());
        assertEquals("john.doe@example.com", response.getBody().getData().getEmail());

        verify(contactService).getContactMessage(messageId);
    }

    @Test
    void getContactMessage_NotFound_ReturnsInternalServerError() {
        // Arrange
        Long messageId = 999L;
        when(contactService.getContactMessage(messageId))
                .thenThrow(new ContactException("Contact message not found with ID: " + messageId));

        // Act
        ResponseEntity<ApiResponse<ContactMessageResponse>> response = 
                adminContactController.getContactMessage(messageId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to retrieve contact message", response.getBody().getMessage());
        assertNull(response.getBody().getData());

        verify(contactService).getContactMessage(messageId);
    }

    @Test
    void updateContactStatus_Success() {
        // Arrange
        Long messageId = 1L;
        ContactStatus newStatus = ContactStatus.IN_PROGRESS;
        String response = "We are reviewing your inquiry";
        
        ContactMessageResponse updatedResponse = ContactMessageResponse.builder()
                .id(messageId)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .subject("Product Inquiry")
                .message("I need information about protein supplements")
                .status(newStatus)
                .response(response)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .respondedAt(LocalDateTime.now())
                .isResponded(true)
                .build();

        when(contactService.updateContactStatus(messageId, newStatus, response))
                .thenReturn(updatedResponse);

        // Act
        ResponseEntity<ApiResponse<ContactMessageResponse>> responseEntity = 
                adminContactController.updateContactStatus(messageId, newStatus, response);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody().isSuccess());
        assertEquals("Contact message status updated successfully", responseEntity.getBody().getMessage());
        assertEquals(updatedResponse, responseEntity.getBody().getData());
        assertEquals(newStatus, responseEntity.getBody().getData().getStatus());
        assertEquals(response, responseEntity.getBody().getData().getResponse());
        assertTrue(responseEntity.getBody().getData().isResponded());

        verify(contactService).updateContactStatus(messageId, newStatus, response);
    }

    @Test
    void updateContactStatus_WithoutResponse_Success() {
        // Arrange
        Long messageId = 1L;
        ContactStatus newStatus = ContactStatus.IN_PROGRESS;
        
        ContactMessageResponse updatedResponse = ContactMessageResponse.builder()
                .id(messageId)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .subject("Product Inquiry")
                .message("I need information about protein supplements")
                .status(newStatus)
                .response(null)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .respondedAt(null)
                .isResponded(false)
                .build();

        when(contactService.updateContactStatus(messageId, newStatus, null))
                .thenReturn(updatedResponse);

        // Act
        ResponseEntity<ApiResponse<ContactMessageResponse>> responseEntity = 
                adminContactController.updateContactStatus(messageId, newStatus, null);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody().isSuccess());
        assertEquals("Contact message status updated successfully", responseEntity.getBody().getMessage());
        assertEquals(updatedResponse, responseEntity.getBody().getData());
        assertEquals(newStatus, responseEntity.getBody().getData().getStatus());
        assertNull(responseEntity.getBody().getData().getResponse());
        assertFalse(responseEntity.getBody().getData().isResponded());

        verify(contactService).updateContactStatus(messageId, newStatus, null);
    }

    @Test
    void updateContactStatus_MessageNotFound_ReturnsInternalServerError() {
        // Arrange
        Long messageId = 999L;
        ContactStatus newStatus = ContactStatus.RESOLVED;
        String response = "Issue resolved";

        when(contactService.updateContactStatus(messageId, newStatus, response))
                .thenThrow(new ContactException("Contact message not found with ID: " + messageId));

        // Act
        ResponseEntity<ApiResponse<ContactMessageResponse>> responseEntity = 
                adminContactController.updateContactStatus(messageId, newStatus, response);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertFalse(responseEntity.getBody().isSuccess());
        assertEquals("Failed to update contact message status", responseEntity.getBody().getMessage());
        assertNull(responseEntity.getBody().getData());

        verify(contactService).updateContactStatus(messageId, newStatus, response);
    }

    @Test
    void searchContactMessages_Success() {
        // Arrange
        String email = "john.doe@example.com";
        Pageable pageable = PageRequest.of(0, 20);
        when(contactService.searchByEmail(email, pageable)).thenReturn(contactMessagesPage);

        // Act
        ResponseEntity<ApiResponse<Page<ContactMessageResponse>>> response = 
                adminContactController.searchContactMessages(email, 0, 20);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Contact messages search completed", response.getBody().getMessage());
        assertEquals(contactMessagesPage, response.getBody().getData());
        assertEquals(1, response.getBody().getData().getTotalElements());

        verify(contactService).searchByEmail(email, pageable);
    }

    @Test
    void searchContactMessages_ServiceException_ReturnsInternalServerError() {
        // Arrange
        String email = "john.doe@example.com";
        Pageable pageable = PageRequest.of(0, 20);
        when(contactService.searchByEmail(email, pageable))
                .thenThrow(new RuntimeException("Search failed"));

        // Act
        ResponseEntity<ApiResponse<Page<ContactMessageResponse>>> response = 
                adminContactController.searchContactMessages(email, 0, 20);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to search contact messages", response.getBody().getMessage());
        assertNull(response.getBody().getData());

        verify(contactService).searchByEmail(email, pageable);
    }

    @Test
    void getContactStats_Success() {
        // Arrange
        when(contactService.getContactStats()).thenReturn(contactStatsResponse);

        // Act
        ResponseEntity<ApiResponse<ContactService.ContactStatsResponse>> response = 
                adminContactController.getContactStats();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Contact statistics retrieved successfully", response.getBody().getMessage());
        assertEquals(contactStatsResponse, response.getBody().getData());
        assertEquals(5L, response.getBody().getData().getNewMessages());
        assertEquals(3L, response.getBody().getData().getInProgressMessages());
        assertEquals(10L, response.getBody().getData().getResolvedMessages());
        assertEquals(18L, response.getBody().getData().getTotalMessages());

        verify(contactService).getContactStats();
    }

    @Test
    void getContactStats_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(contactService.getContactStats())
                .thenThrow(new RuntimeException("Stats calculation failed"));

        // Act
        ResponseEntity<ApiResponse<ContactService.ContactStatsResponse>> response = 
                adminContactController.getContactStats();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to retrieve contact statistics", response.getBody().getMessage());
        assertNull(response.getBody().getData());

        verify(contactService).getContactStats();
    }

    @Test
    void getContactMessages_CustomPagination_Success() {
        // Arrange
        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        when(contactService.getContactMessages(null, pageable)).thenReturn(contactMessagesPage);

        // Act
        ResponseEntity<ApiResponse<Page<ContactMessageResponse>>> response = 
                adminContactController.getContactMessages(null, page, size);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Contact messages retrieved successfully", response.getBody().getMessage());

        verify(contactService).getContactMessages(null, pageable);
    }

    @Test
    void searchContactMessages_CustomPagination_Success() {
        // Arrange
        String email = "test@example.com";
        int page = 2;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);
        when(contactService.searchByEmail(email, pageable)).thenReturn(contactMessagesPage);

        // Act
        ResponseEntity<ApiResponse<Page<ContactMessageResponse>>> response = 
                adminContactController.searchContactMessages(email, page, size);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Contact messages search completed", response.getBody().getMessage());

        verify(contactService).searchByEmail(email, pageable);
    }

    @Test
    void updateContactStatus_EmptyResponse_Success() {
        // Arrange
        Long messageId = 1L;
        ContactStatus newStatus = ContactStatus.RESOLVED;
        String emptyResponse = "";
        
        ContactMessageResponse updatedResponse = ContactMessageResponse.builder()
                .id(messageId)
                .name("John Doe")
                .email("john.doe@example.com")
                .status(newStatus)
                .response(null)
                .updatedAt(LocalDateTime.now())
                .isResponded(false)
                .build();

        when(contactService.updateContactStatus(messageId, newStatus, emptyResponse))
                .thenReturn(updatedResponse);

        // Act
        ResponseEntity<ApiResponse<ContactMessageResponse>> responseEntity = 
                adminContactController.updateContactStatus(messageId, newStatus, emptyResponse);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody().isSuccess());
        assertEquals("Contact message status updated successfully", responseEntity.getBody().getMessage());

        verify(contactService).updateContactStatus(messageId, newStatus, emptyResponse);
    }
}