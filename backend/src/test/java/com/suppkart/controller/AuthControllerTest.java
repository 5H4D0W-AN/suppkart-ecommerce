package com.suppkart.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.suppkart.dto.request.LoginRequest;
import com.suppkart.dto.request.RefreshTokenRequest;
import com.suppkart.dto.request.RegisterRequest;
import com.suppkart.dto.response.AuthResponse;
import com.suppkart.dto.response.UserResponse;
import com.suppkart.model.entity.RefreshToken;
import com.suppkart.model.entity.Role;
import com.suppkart.model.entity.User;
import com.suppkart.model.entity.UserProfile;
import com.suppkart.model.enums.AuthProvider;
import com.suppkart.model.enums.RoleType;
import com.suppkart.model.enums.UserStatus;
import com.suppkart.service.AuthenticationService;
import com.suppkart.service.UserService;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private User testUser;
    private UserProfile testProfile;
    private RefreshToken testRefreshToken;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        // Setup role
        customerRole = new Role();
        customerRole.setRoleId(1L);
        customerRole.setName(RoleType.CUSTOMER);

        // Setup user profile
        testProfile = new UserProfile();
        testProfile.setFirstName("John");
        testProfile.setLastName("Doe");
        testProfile.setPhone("1234567890");
        testProfile.setCreatedAt(LocalDateTime.now());
        testProfile.setUpdatedAt(LocalDateTime.now());

        // Setup test user
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setAuthProvider(AuthProvider.EMAIL);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRoles(Set.of(customerRole));
        testUser.setUserProfile(testProfile);
        testUser.setEmailVerified(true);
        testUser.setLastLoginAt(LocalDateTime.now());
        testUser.setCreatedAt(LocalDateTime.now());

        testProfile.setUser(testUser);

        // Setup refresh token
        testRefreshToken = new RefreshToken();
        testRefreshToken.setTokenId(1L);
        testRefreshToken.setUser(testUser);
        testRefreshToken.setToken("refresh-token-123");
        testRefreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        testRefreshToken.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void registerUser_Success() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Smith");
        registerRequest.setPhone("9876543210");

        Authentication mockAuth = mock(Authentication.class);

        when(userService.emailExists(registerRequest.getEmail())).thenReturn(false);
        when(authenticationService.registerUser(
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                registerRequest.getPhone(),
                registerRequest.getReferralCode()
        )).thenReturn(testUser);
        when(authenticationService.authenticateUser(
                registerRequest.getEmail(),
                registerRequest.getPassword()
        )).thenReturn(mockAuth);
        when(authenticationService.generateAccessToken(mockAuth)).thenReturn("jwt-token-123");
        when(authenticationService.generateRefreshToken(testUser)).thenReturn(testRefreshToken);

        // Act
        ResponseEntity<?> response = authController.registerUser(registerRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);

        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("jwt-token-123", authResponse.getAccessToken());
        assertEquals("refresh-token-123", authResponse.getRefreshToken());
        assertNotNull(authResponse.getUser());
        assertEquals(testUser.getEmail(), authResponse.getUser().getEmail());

        verify(userService).emailExists(registerRequest.getEmail());
        verify(authenticationService).registerUser(anyString(), anyString(), anyString(), anyString(), anyString(), any());
        verify(authenticationService).authenticateUser(anyString(), anyString());
        verify(authenticationService).generateAccessToken(mockAuth);
        verify(authenticationService).generateRefreshToken(testUser);
    }

    @Test
    void registerUser_EmailAlreadyExists_ReturnsBadRequest() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("existing@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Smith");
        registerRequest.setPhone("9876543210");

        when(userService.emailExists(registerRequest.getEmail())).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.registerUser(registerRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error: Email is already taken!", response.getBody());

        verify(userService).emailExists(registerRequest.getEmail());
        verify(authenticationService, never()).registerUser(anyString(), anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void registerUser_RegistrationFails_ReturnsBadRequest() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Smith");
        registerRequest.setPhone("9876543210");

        when(userService.emailExists(registerRequest.getEmail())).thenReturn(false);
        when(authenticationService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Registration failed"));

        // Act
        ResponseEntity<?> response = authController.registerUser(registerRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Error: Registration failed"));
    }

    @Test
    void authenticateUser_Success() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        Authentication mockAuth = mock(Authentication.class);

        when(authenticationService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword()))
                .thenReturn(mockAuth);
        when(userService.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(authenticationService.generateAccessToken(mockAuth)).thenReturn("jwt-token-123");
        when(authenticationService.generateRefreshToken(testUser)).thenReturn(testRefreshToken);

        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);

        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("jwt-token-123", authResponse.getAccessToken());
        assertEquals("refresh-token-123", authResponse.getRefreshToken());
        assertNotNull(authResponse.getUser());

        verify(authenticationService).authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
        verify(userService).findByEmail(loginRequest.getEmail());
        verify(authenticationService).generateAccessToken(mockAuth);
        verify(authenticationService).generateRefreshToken(testUser);
    }

    @Test
    void authenticateUser_InvalidCredentials_ReturnsUnauthorized() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongpassword");

        when(authenticationService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword()))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Error: Invalid credentials"));

        verify(authenticationService).authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
        verify(userService, never()).findByEmail(anyString());
    }

    @Test
    void authenticateUser_UserNotFound_ReturnsUnauthorized() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("password123");

        Authentication mockAuth = mock(Authentication.class);

        when(authenticationService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword()))
                .thenReturn(mockAuth);
        when(userService.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Error: Invalid credentials"));
    }

    @Test
    void refreshToken_Success() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token-123");

        when(authenticationService.isRefreshTokenValid(request.getRefreshToken())).thenReturn(true);
        when(authenticationService.refreshAccessToken(request.getRefreshToken())).thenReturn("new-jwt-token");
        when(authenticationService.getUserByRefreshToken(request.getRefreshToken())).thenReturn(testUser);

        // Act
        ResponseEntity<?> response = authController.refreshToken(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);

        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("new-jwt-token", authResponse.getAccessToken());
        assertEquals("refresh-token-123", authResponse.getRefreshToken());
        assertNotNull(authResponse.getUser());

        verify(authenticationService).isRefreshTokenValid(request.getRefreshToken());
        verify(authenticationService).refreshAccessToken(request.getRefreshToken());
        verify(authenticationService).getUserByRefreshToken(request.getRefreshToken());
    }

    @Test
    void refreshToken_InvalidToken_ReturnsUnauthorized() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-token");

        when(authenticationService.isRefreshTokenValid(request.getRefreshToken())).thenReturn(false);

        // Act
        ResponseEntity<?> response = authController.refreshToken(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Error: Invalid or expired refresh token", response.getBody());

        verify(authenticationService).isRefreshTokenValid(request.getRefreshToken());
        verify(authenticationService, never()).refreshAccessToken(anyString());
    }

    @Test
    void refreshToken_RefreshFails_ReturnsUnauthorized() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token-123");

        when(authenticationService.isRefreshTokenValid(request.getRefreshToken())).thenReturn(true);
        when(authenticationService.refreshAccessToken(request.getRefreshToken()))
                .thenThrow(new RuntimeException("Token refresh failed"));

        // Act
        ResponseEntity<?> response = authController.refreshToken(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Error: Token refresh failed"));
    }

    @Test
    void logoutUser_Success() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token-123");

        doNothing().when(authenticationService).logout(request.getRefreshToken());

        // Act
        ResponseEntity<?> response = authController.logoutUser(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User logged out successfully", response.getBody());

        verify(authenticationService).logout(request.getRefreshToken());
    }

    @Test
    void logoutUser_LogoutFails_ReturnsBadRequest() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token-123");

        doThrow(new RuntimeException("Logout failed")).when(authenticationService).logout(request.getRefreshToken());

        // Act
        ResponseEntity<?> response = authController.logoutUser(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Error: Logout failed"));
    }

    @Test
    void mapToUserResponse_WithUserProfile_Success() {
        // This tests the private method indirectly through registerUser
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Smith");
        registerRequest.setPhone("9876543210");

        Authentication mockAuth = mock(Authentication.class);

        when(userService.emailExists(registerRequest.getEmail())).thenReturn(false);
        when(authenticationService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(testUser);
        when(authenticationService.authenticateUser(anyString(), anyString())).thenReturn(mockAuth);
        when(authenticationService.generateAccessToken(mockAuth)).thenReturn("jwt-token-123");
        when(authenticationService.generateRefreshToken(testUser)).thenReturn(testRefreshToken);

        // Act
        ResponseEntity<?> response = authController.registerUser(registerRequest);

        // Assert
        AuthResponse authResponse = (AuthResponse) response.getBody();
        UserResponse userResponse = authResponse.getUser();

        assertEquals(testUser.getUserId(), userResponse.getUserId());
        assertEquals(testUser.getEmail(), userResponse.getEmail());
        assertEquals(testProfile.getFirstName(), userResponse.getFirstName());
        assertEquals(testProfile.getLastName(), userResponse.getLastName());
        assertEquals(testProfile.getPhone(), userResponse.getPhone());
        assertEquals(testUser.getAuthProvider(), userResponse.getAuthProvider());
        assertEquals(testUser.getStatus(), userResponse.getStatus());
        assertTrue(userResponse.getRoles().contains("CUSTOMER"));
    }

    @Test
    void registerUser_WithReferralCode_Success() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Smith");
        registerRequest.setPhone("9876543210");
        registerRequest.setReferralCode("REF123");

        Authentication mockAuth = mock(Authentication.class);

        when(userService.emailExists(registerRequest.getEmail())).thenReturn(false);
        when(authenticationService.registerUser(
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                registerRequest.getPhone(),
                registerRequest.getReferralCode()
        )).thenReturn(testUser);
        when(authenticationService.authenticateUser(
                registerRequest.getEmail(),
                registerRequest.getPassword()
        )).thenReturn(mockAuth);
        when(authenticationService.generateAccessToken(mockAuth)).thenReturn("jwt-token-123");
        when(authenticationService.generateRefreshToken(testUser)).thenReturn(testRefreshToken);

        // Act
        ResponseEntity<?> response = authController.registerUser(registerRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);

        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("jwt-token-123", authResponse.getAccessToken());
        assertEquals("refresh-token-123", authResponse.getRefreshToken());
        assertNotNull(authResponse.getUser());
        assertEquals(testUser.getEmail(), authResponse.getUser().getEmail());

        verify(userService).emailExists(registerRequest.getEmail());
        verify(authenticationService).registerUser(
                eq(registerRequest.getEmail()),
                eq(registerRequest.getPassword()),
                eq(registerRequest.getFirstName()),
                eq(registerRequest.getLastName()),
                eq(registerRequest.getPhone()),
                eq("REF123")
        );
        verify(authenticationService).authenticateUser(anyString(), anyString());
        verify(authenticationService).generateAccessToken(mockAuth);
        verify(authenticationService).generateRefreshToken(testUser);
    }
}