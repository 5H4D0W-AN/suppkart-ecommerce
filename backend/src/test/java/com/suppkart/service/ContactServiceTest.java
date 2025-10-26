package com.suppkart.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

import com.suppkart.dto.request.ContactMessageRequest;
import com.suppkart.dto.response.ContactMessageResponse;
import com.suppkart.exception.ContactException;
import com.suppkart.model.entity.ContactMessage;
import com.suppkart.model.enums.ContactStatus;
import com.suppkart.repository.ContactMessageRepository;

@ExtendWith(MockitoExtension.class)
public class ContactServiceTest {

    @Mock
    private ContactMessageRepository contactMessageRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private ContactService contactService;

    private ContactMessageRequest contactMessageRequest;
    private ContactMessage contactMessage;
    private ContactMessage savedContactMessage;

    @BeforeEach
    void setUp() {
        // Setup contact message request
        contactMessageRequest = new ContactMessageRequest();
        contactMessageRequest.setName("John Doe");
        contactMessageRequest.setEmail("john.doe@example.com");
        contactMessageRequest.setPhone("+1234567890");
        contactMessageRequest.setSubject("Product Inquiry");
        contactMessageRequest.setMessage("I need information about protein supplements");

        // Setup contact message entity
        contactMessage = ContactMessage.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .subject("Product Inquiry")
                .message("I need information about protein supplements")
                .status(ContactStatus.NEW)
                .createdAt(LocalDateTime.now())
                .build();

        // Setup saved contact message with ID
        savedContactMessage = ContactMessage.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .subject("Product Inquiry")
                .message("I need information about protein supplements")
                .status(ContactStatus.NEW)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void submitContactMessage_Success() {
        // Arrange
        when(contactMessageRepository.save(any(ContactMessage.class))).thenReturn(savedContactMessage);
        doNothing().when(emailNotificationService).sendEmail(anyString(), anyString(), anyString());

        // Act
        ContactMessageResponse response = contactService.submitContactMessage(contactMessageRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john.doe@example.com", response.getEmail());
        assertEquals("+1234567890", response.getPhone());
        assertEquals("Product Inquiry", response.getSubject());
        assertEquals("I need information about protein supplements", response.getMessage());
        assertEquals(ContactStatus.NEW, response.getStatus());
        assertNull(response.getResponse());
        assertFalse(response.isResponded());

        verify(contactMessageRepository).save(any(ContactMessage.class));
        verify(emailNotificationService).sendEmail(eq("john.doe@example.com"), anyString(), anyString());
    }

    @Test
    void submitContactMessage_RepositoryException_ThrowsContactException() {
        // Arrange
        when(contactMessageRepository.save(any(ContactMessage.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ContactException exception = assertThrows(ContactException.class, () -> {
            contactService.submitContactMessage(contactMessageRequest);
        });

        assertEquals("Failed to submit contact message", exception.getMessage());
        verify(contactMessageRepository).save(any(ContactMessage.class));
        verify(emailNotificationService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void submitContactMessage_EmailServiceException_StillSucceeds() {
        // Arrange
        when(contactMessageRepository.save(any(ContactMessage.class))).thenReturn(savedContactMessage);
        doThrow(new RuntimeException("Email service error"))
                .when(emailNotificationService).sendEmail(anyString(), anyString(), anyString());

        // Act
        ContactMessageResponse response = contactService.submitContactMessage(contactMessageRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getName());

        verify(contactMessageRepository).save(any(ContactMessage.class));
        verify(emailNotificationService).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void getContactMessage_Success() {
        // Arrange
        Long messageId = 1L;
        when(contactMessageRepository.findById(messageId)).thenReturn(Optional.of(savedContactMessage));

        // Act
        ContactMessageResponse response = contactService.getContactMessage(messageId);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john.doe@example.com", response.getEmail());
        assertEquals(ContactStatus.NEW, response.getStatus());

        verify(contactMessageRepository).findById(messageId);
    }

    @Test
    void getContactMessage_NotFound_ThrowsContactException() {
        // Arrange
        Long messageId = 999L;
        when(contactMessageRepository.findById(messageId)).thenReturn(Optional.empty());

        // Act & Assert
        ContactException exception = assertThrows(ContactException.class, () -> {
            contactService.getContactMessage(messageId);
        });

        assertTrue(exception.getMessage().contains("Contact message not found with ID: " + messageId));
        verify(contactMessageRepository).findById(messageId);
    }

    @Test
    void getContactMessages_WithoutStatusFilter_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        List<ContactMessage> messages = Arrays.asList(savedContactMessage);
        Page<ContactMessage> messagesPage = new PageImpl<>(messages, pageable, 1);
        
        when(contactMessageRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(messagesPage);

        // Act
        Page<ContactMessageResponse> response = contactService.getContactMessages(null, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        assertEquals("John Doe", response.getContent().get(0).getName());

        verify(contactMessageRepository).findAllByOrderByCreatedAtDesc(pageable);
        verify(contactMessageRepository, never()).findByStatusOrderByCreatedAtDesc(any(ContactStatus.class), any(Pageable.class));
    }

    @Test
    void getContactMessages_WithStatusFilter_Success() {
        // Arrange
        ContactStatus status = ContactStatus.NEW;
        Pageable pageable = PageRequest.of(0, 20);
        List<ContactMessage> messages = Arrays.asList(savedContactMessage);
        Page<ContactMessage> messagesPage = new PageImpl<>(messages, pageable, 1);
        
        when(contactMessageRepository.findByStatusOrderByCreatedAtDesc(status, pageable)).thenReturn(messagesPage);

        // Act
        Page<ContactMessageResponse> response = contactService.getContactMessages(status, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        assertEquals("John Doe", response.getContent().get(0).getName());
        assertEquals(ContactStatus.NEW, response.getContent().get(0).getStatus());

        verify(contactMessageRepository).findByStatusOrderByCreatedAtDesc(status, pageable);
        verify(contactMessageRepository, never()).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void updateContactStatus_WithResponse_Success() {
        // Arrange
        Long messageId = 1L;
        ContactStatus newStatus = ContactStatus.RESOLVED;
        String response = "Thank you for your inquiry. Here is the information you requested.";
        
        ContactMessage existingMessage = ContactMessage.builder()
                .id(messageId)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .subject("Product Inquiry")
                .message("I need information about protein supplements")
                .status(ContactStatus.NEW)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        ContactMessage updatedMessage = ContactMessage.builder()
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
                .build();

        when(contactMessageRepository.findById(messageId)).thenReturn(Optional.of(existingMessage));
        when(contactMessageRepository.save(any(ContactMessage.class))).thenReturn(updatedMessage);
        doNothing().when(emailNotificationService).sendEmail(anyString(), anyString(), anyString());

        // Act
        ContactMessageResponse result = contactService.updateContactStatus(messageId, newStatus, response);

        // Assert
        assertNotNull(result);
        assertEquals(messageId, result.getId());
        assertEquals(newStatus, result.getStatus());
        assertEquals(response, result.getResponse());
        assertTrue(result.isResponded());
        assertNotNull(result.getUpdatedAt());
        assertNotNull(result.getRespondedAt());

        verify(contactMessageRepository).findById(messageId);
        verify(contactMessageRepository).save(any(ContactMessage.class));
        verify(emailNotificationService).sendEmail(eq("john.doe@example.com"), anyString(), anyString());
    }

    @Test
    void updateContactStatus_WithoutResponse_Success() {
        // Arrange
        Long messageId = 1L;
        ContactStatus newStatus = ContactStatus.IN_PROGRESS;
        
        ContactMessage existingMessage = ContactMessage.builder()
                .id(messageId)
                .name("John Doe")
                .email("john.doe@example.com")
                .status(ContactStatus.NEW)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        ContactMessage updatedMessage = ContactMessage.builder()
                .id(messageId)
                .name("John Doe")
                .email("john.doe@example.com")
                .status(newStatus)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();

        when(contactMessageRepository.findById(messageId)).thenReturn(Optional.of(existingMessage));
        when(contactMessageRepository.save(any(ContactMessage.class))).thenReturn(updatedMessage);

        // Act
        ContactMessageResponse result = contactService.updateContactStatus(messageId, newStatus, null);

        // Assert
        assertNotNull(result);
        assertEquals(messageId, result.getId());
        assertEquals(newStatus, result.getStatus());
        assertNull(result.getResponse());
        assertFalse(result.isResponded());
        assertNotNull(result.getUpdatedAt());
        assertNull(result.getRespondedAt());

        verify(contactMessageRepository).findById(messageId);
        verify(contactMessageRepository).save(any(ContactMessage.class));
        verify(emailNotificationService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void updateContactStatus_WithEmptyResponse_Success() {
        // Arrange
        Long messageId = 1L;
        ContactStatus newStatus = ContactStatus.IN_PROGRESS;
        String emptyResponse = "   ";
        
        ContactMessage existingMessage = ContactMessage.builder()
                .id(messageId)
                .name("John Doe")
                .email("john.doe@example.com")
                .status(ContactStatus.NEW)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        ContactMessage updatedMessage = ContactMessage.builder()
                .id(messageId)
                .name("John Doe")
                .email("john.doe@example.com")
                .status(newStatus)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();

        when(contactMessageRepository.findById(messageId)).thenReturn(Optional.of(existingMessage));
        when(contactMessageRepository.save(any(ContactMessage.class))).thenReturn(updatedMessage);

        // Act
        ContactMessageResponse result = contactService.updateContactStatus(messageId, newStatus, emptyResponse);

        // Assert
        assertNotNull(result);
        assertEquals(messageId, result.getId());
        assertEquals(newStatus, result.getStatus());
        assertNull(result.getResponse());
        assertFalse(result.isResponded());

        verify(contactMessageRepository).findById(messageId);
        verify(contactMessageRepository).save(any(ContactMessage.class));
        verify(emailNotificationService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void updateContactStatus_MessageNotFound_ThrowsContactException() {
        // Arrange
        Long messageId = 999L;
        ContactStatus newStatus = ContactStatus.RESOLVED;
        String response = "Issue resolved";

        when(contactMessageRepository.findById(messageId)).thenReturn(Optional.empty());

        // Act & Assert
        ContactException exception = assertThrows(ContactException.class, () -> {
            contactService.updateContactStatus(messageId, newStatus, response);
        });

        assertTrue(exception.getMessage().contains("Contact message not found with ID: " + messageId));
        verify(contactMessageRepository).findById(messageId);
        verify(contactMessageRepository, never()).save(any(ContactMessage.class));
        verify(emailNotificationService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void updateContactStatus_EmailServiceException_StillSucceeds() {
        // Arrange
        Long messageId = 1L;
        ContactStatus newStatus = ContactStatus.RESOLVED;
        String response = "Issue resolved";
        
        ContactMessage existingMessage = ContactMessage.builder()
                .id(messageId)
                .name("John Doe")
                .email("john.doe@example.com")
                .status(ContactStatus.NEW)
                .build();

        ContactMessage updatedMessage = ContactMessage.builder()
                .id(messageId)
                .name("John Doe")
                .email("john.doe@example.com")
                .status(newStatus)
                .response(response)
                .updatedAt(LocalDateTime.now())
                .respondedAt(LocalDateTime.now())
                .build();

        when(contactMessageRepository.findById(messageId)).thenReturn(Optional.of(existingMessage));
        when(contactMessageRepository.save(any(ContactMessage.class))).thenReturn(updatedMessage);
        doThrow(new RuntimeException("Email service error"))
                .when(emailNotificationService).sendEmail(anyString(), anyString(), anyString());

        // Act
        ContactMessageResponse result = contactService.updateContactStatus(messageId, newStatus, response);

        // Assert
        assertNotNull(result);
        assertEquals(messageId, result.getId());
        assertEquals(newStatus, result.getStatus());
        assertEquals(response, result.getResponse());

        verify(contactMessageRepository).findById(messageId);
        verify(contactMessageRepository).save(any(ContactMessage.class));
        verify(emailNotificationService).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void getContactStats_Success() {
        // Arrange
        when(contactMessageRepository.countByStatus(ContactStatus.NEW)).thenReturn(5L);
        when(contactMessageRepository.countByStatus(ContactStatus.IN_PROGRESS)).thenReturn(3L);
        when(contactMessageRepository.countByStatus(ContactStatus.RESOLVED)).thenReturn(10L);
        when(contactMessageRepository.count()).thenReturn(18L);

        // Act
        ContactService.ContactStatsResponse stats = contactService.getContactStats();

        // Assert
        assertNotNull(stats);
        assertEquals(5L, stats.getNewMessages());
        assertEquals(3L, stats.getInProgressMessages());
        assertEquals(10L, stats.getResolvedMessages());
        assertEquals(18L, stats.getTotalMessages());

        verify(contactMessageRepository).countByStatus(ContactStatus.NEW);
        verify(contactMessageRepository).countByStatus(ContactStatus.IN_PROGRESS);
        verify(contactMessageRepository).countByStatus(ContactStatus.RESOLVED);
        verify(contactMessageRepository).count();
    }

    @Test
    void searchByEmail_Success() {
        // Arrange
        String email = "john.doe@example.com";
        Pageable pageable = PageRequest.of(0, 20);
        List<ContactMessage> messages = Arrays.asList(savedContactMessage);
        Page<ContactMessage> messagesPage = new PageImpl<>(messages, pageable, 1);
        
        when(contactMessageRepository.findByEmailContainingIgnoreCase(email, pageable)).thenReturn(messagesPage);

        // Act
        Page<ContactMessageResponse> response = contactService.searchByEmail(email, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        assertEquals("john.doe@example.com", response.getContent().get(0).getEmail());

        verify(contactMessageRepository).findByEmailContainingIgnoreCase(email, pageable);
    }

    @Test
    void searchByEmail_PartialMatch_Success() {
        // Arrange
        String partialEmail = "john";
        Pageable pageable = PageRequest.of(0, 20);
        List<ContactMessage> messages = Arrays.asList(savedContactMessage);
        Page<ContactMessage> messagesPage = new PageImpl<>(messages, pageable, 1);
        
        when(contactMessageRepository.findByEmailContainingIgnoreCase(partialEmail, pageable)).thenReturn(messagesPage);

        // Act
        Page<ContactMessageResponse> response = contactService.searchByEmail(partialEmail, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        assertEquals("john.doe@example.com", response.getContent().get(0).getEmail());

        verify(contactMessageRepository).findByEmailContainingIgnoreCase(partialEmail, pageable);
    }

    @Test
    void mapToContactMessageResponse_Success() {
        // Arrange
        ContactMessage message = ContactMessage.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .subject("Product Inquiry")
                .message("I need information about protein supplements")
                .status(ContactStatus.RESOLVED)
                .response("Here is the information you requested")
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .respondedAt(LocalDateTime.now().minusDays(1))
                .build();

        // Act - using the service method indirectly through getContactMessage
        when(contactMessageRepository.findById(1L)).thenReturn(Optional.of(message));
        ContactMessageResponse response = contactService.getContactMessage(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john.doe@example.com", response.getEmail());
        assertEquals("+1234567890", response.getPhone());
        assertEquals("Product Inquiry", response.getSubject());
        assertEquals("I need information about protein supplements", response.getMessage());
        assertEquals(ContactStatus.RESOLVED, response.getStatus());
        assertEquals("Here is the information you requested", response.getResponse());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());
        assertNotNull(response.getRespondedAt());
        assertTrue(response.isResponded());
    }

    @Test
    void contactStatsResponse_BuilderPattern_Success() {
        // Act
        ContactService.ContactStatsResponse stats = ContactService.ContactStatsResponse.builder()
                .newMessages(5L)
                .inProgressMessages(3L)
                .resolvedMessages(10L)
                .totalMessages(18L)
                .build();

        // Assert
        assertNotNull(stats);
        assertEquals(5L, stats.getNewMessages());
        assertEquals(3L, stats.getInProgressMessages());
        assertEquals(10L, stats.getResolvedMessages());
        assertEquals(18L, stats.getTotalMessages());
    }

    @Test
    void contactStatsResponse_DefaultConstructor_Success() {
        // Act
        ContactService.ContactStatsResponse stats = new ContactService.ContactStatsResponse();
        stats.setNewMessages(2L);
        stats.setInProgressMessages(1L);
        stats.setResolvedMessages(5L);
        stats.setTotalMessages(8L);

        // Assert
        assertNotNull(stats);
        assertEquals(2L, stats.getNewMessages());
        assertEquals(1L, stats.getInProgressMessages());
        assertEquals(5L, stats.getResolvedMessages());
        assertEquals(8L, stats.getTotalMessages());
    }

    @Test
    void contactStatsResponse_AllArgsConstructor_Success() {
        // Act
        ContactService.ContactStatsResponse stats = new ContactService.ContactStatsResponse(1L, 2L, 3L, 6L);

        // Assert
        assertNotNull(stats);
        assertEquals(1L, stats.getNewMessages());
        assertEquals(2L, stats.getInProgressMessages());
        assertEquals(3L, stats.getResolvedMessages());
        assertEquals(6L, stats.getTotalMessages());
    }
}