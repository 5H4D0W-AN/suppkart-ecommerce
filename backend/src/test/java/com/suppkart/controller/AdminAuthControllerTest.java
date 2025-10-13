package com.suppkart.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import com.suppkart.dto.admin.AdminLoginRequest;
import com.suppkart.dto.admin.AdminUserDTO;
import com.suppkart.dto.admin.ChangePasswordRequest;
import com.suppkart.dto.admin.JwtAuthResponse;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.exception.BusinessException;
import com.suppkart.service.AdminAuthService;

@ExtendWith(MockitoExtension.class)
public class AdminAuthControllerTest {

    @Mock
    private AdminAuthService adminAuthService;

    @InjectMocks
    private AdminAuthController adminAuthController;

    private AdminLoginRequest loginRequest;
    private JwtAuthResponse jwtAuthResponse;
    private AdminUserDTO adminUserDTO;
    private ChangePasswordRequest changePasswordRequest;

    @BeforeEach
    void setUp() {
        // Setup login request
        loginRequest = new AdminLoginRequest();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword("password123");

        // Setup JWT auth response
        jwtAuthResponse = new JwtAuthResponse("jwt-token-123", 86400000L);

        // Setup admin user DTO
        adminUserDTO = new AdminUserDTO(
                1L,
                "admin@example.com",
                "Admin",
                "User",
                Set.of("ADMIN"),
                true,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusMonths(1)
        );

        // Setup change password request
        changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setCurrentPassword("oldPassword");
        changePasswordRequest.setNewPassword("newPassword");
        changePasswordRequest.setConfirmPassword("newPassword");
    }

    @Test
    void login_Success() {
        // Arrange
        when(adminAuthService.authenticate(loginRequest)).thenReturn(jwtAuthResponse);

        // Act
        ResponseEntity<ApiResponse<JwtAuthResponse>> response = adminAuthController.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Admin login successful", response.getBody().getMessage());
        assertEquals(jwtAuthResponse, response.getBody().getData());
        assertEquals("jwt-token-123", response.getBody().getData().getToken());
        assertEquals(86400000L, response.getBody().getData().getExpiresIn());

        verify(adminAuthService).authenticate(loginRequest);
    }

    @Test
    void login_InvalidCredentials_ReturnsUnauthorized() {
        // Arrange
        when(adminAuthService.authenticate(loginRequest))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act
        ResponseEntity<ApiResponse<JwtAuthResponse>> response = adminAuthController.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Login failed"));
        assertNull(response.getBody().getData());

        verify(adminAuthService).authenticate(loginRequest);
    }

    @Test
    void login_ServiceException_ReturnsUnauthorized() {
        // Arrange
        when(adminAuthService.authenticate(loginRequest))
                .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<ApiResponse<JwtAuthResponse>> response = adminAuthController.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Login failed: Service error"));
        assertNull(response.getBody().getData());
    }

    @Test
    void getProfile_Success() {
        // Arrange
        when(adminAuthService.getAdminProfile()).thenReturn(adminUserDTO);

        // Act
        ResponseEntity<ApiResponse<AdminUserDTO>> response = adminAuthController.getProfile();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Admin profile retrieved successfully", response.getBody().getMessage());
        assertEquals(adminUserDTO, response.getBody().getData());
        assertEquals("admin@example.com", response.getBody().getData().getEmail());
        assertEquals("Admin", response.getBody().getData().getFirstName());
        assertEquals("User", response.getBody().getData().getLastName());
        assertTrue(response.getBody().getData().getActive());
        assertTrue(response.getBody().getData().getRoles().contains("ADMIN"));

        verify(adminAuthService).getAdminProfile();
    }

    @Test
    void getProfile_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(adminAuthService.getAdminProfile())
                .thenThrow(new RuntimeException("Profile retrieval failed"));

        // Act
        ResponseEntity<ApiResponse<AdminUserDTO>> response = adminAuthController.getProfile();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Failed to retrieve profile"));
        assertNull(response.getBody().getData());
    }

    @Test
    void getProfile_BusinessException_ReturnsInternalServerError() {
        // Arrange
        when(adminAuthService.getAdminProfile())
                .thenThrow(new BusinessException("ADMIN_NOT_FOUND", "Admin not found"));

        // Act
        ResponseEntity<ApiResponse<AdminUserDTO>> response = adminAuthController.getProfile();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Failed to retrieve profile"));
    }

    @Test
    void updateProfile_Success() {
        // Arrange
        AdminUserDTO updateRequest = new AdminUserDTO(
                1L, "admin@example.com", "Updated", "Admin", 
                Set.of("ADMIN"), true, LocalDateTime.now(), LocalDateTime.now()
        );
        when(adminAuthService.getAdminProfile()).thenReturn(adminUserDTO);

        // Act
        ResponseEntity<ApiResponse<AdminUserDTO>> response = adminAuthController.updateProfile(updateRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Admin profile updated successfully", response.getBody().getMessage());
        assertEquals(adminUserDTO, response.getBody().getData());

        verify(adminAuthService).getAdminProfile();
    }

    @Test
    void updateProfile_ServiceException_ReturnsInternalServerError() {
        // Arrange
        AdminUserDTO updateRequest = new AdminUserDTO(
                1L, "admin@example.com", "Updated", "Admin", 
                Set.of("ADMIN"), true, LocalDateTime.now(), LocalDateTime.now()
        );
        when(adminAuthService.getAdminProfile())
                .thenThrow(new RuntimeException("Update failed"));

        // Act
        ResponseEntity<ApiResponse<AdminUserDTO>> response = adminAuthController.updateProfile(updateRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Failed to update profile"));
        assertNull(response.getBody().getData());
    }

    @Test
    void changePassword_Success() {
        // Arrange
        doNothing().when(adminAuthService).changePassword(changePasswordRequest);

        // Act
        ResponseEntity<ApiResponse<String>> response = adminAuthController.changePassword(changePasswordRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Password changed successfully", response.getBody().getMessage());
        assertEquals("Password has been updated successfully", response.getBody().getData());

        verify(adminAuthService).changePassword(changePasswordRequest);
    }

    @Test
    void changePassword_InvalidCurrentPassword_ReturnsBadRequest() {
        // Arrange
        doThrow(new BusinessException("INVALID_CURRENT_PASSWORD", "Current password is incorrect"))
                .when(adminAuthService).changePassword(changePasswordRequest);

        // Act
        ResponseEntity<ApiResponse<String>> response = adminAuthController.changePassword(changePasswordRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Failed to change password"));
        assertNull(response.getBody().getData());

        verify(adminAuthService).changePassword(changePasswordRequest);
    }

    @Test
    void changePassword_PasswordMismatch_ReturnsBadRequest() {
        // Arrange
        doThrow(new BusinessException("PASSWORD_MISMATCH", "Passwords do not match"))
                .when(adminAuthService).changePassword(changePasswordRequest);

        // Act
        ResponseEntity<ApiResponse<String>> response = adminAuthController.changePassword(changePasswordRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Failed to change password"));
        assertNull(response.getBody().getData());
    }

    @Test
    void changePassword_ServiceException_ReturnsBadRequest() {
        // Arrange
        doThrow(new RuntimeException("Service error"))
                .when(adminAuthService).changePassword(changePasswordRequest);

        // Act
        ResponseEntity<ApiResponse<String>> response = adminAuthController.changePassword(changePasswordRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Failed to change password: Service error"));
        assertNull(response.getBody().getData());
    }

    @Test
    void logout_Success() {
        // Act
        ResponseEntity<ApiResponse<String>> response = adminAuthController.logout();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Logout successful", response.getBody().getMessage());
        assertEquals("Please remove the JWT token from client storage", response.getBody().getData());
    }

    @Test
    void login_NullRequest_HandledGracefully() {
        // Arrange
        when(adminAuthService.authenticate(null))
                .thenThrow(new IllegalArgumentException("Login request cannot be null"));

        // Act
        ResponseEntity<ApiResponse<JwtAuthResponse>> response = adminAuthController.login(null);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Login failed"));
    }

    @Test
    void changePassword_NullRequest_HandledGracefully() {
        // Arrange
        doThrow(new IllegalArgumentException("Change password request cannot be null"))
                .when(adminAuthService).changePassword(null);

        // Act
        ResponseEntity<ApiResponse<String>> response = adminAuthController.changePassword(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Failed to change password"));
    }
}