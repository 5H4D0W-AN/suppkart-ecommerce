package com.suppkart.service;

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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.suppkart.dto.admin.AdminLoginRequest;
import com.suppkart.dto.admin.AdminUserDTO;
import com.suppkart.dto.admin.ChangePasswordRequest;
import com.suppkart.dto.admin.JwtAuthResponse;
import com.suppkart.exception.BusinessException;
import com.suppkart.model.entity.Role;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.RoleType;
import com.suppkart.model.enums.UserStatus;
import com.suppkart.repository.UserRepository;
import com.suppkart.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
public class AdminAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AdminAuthService adminAuthService;

    private User adminUser;
    private User regularUser;
    private Role adminRole;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        // Setup roles
        adminRole = new Role();
        adminRole.setRoleId(1L);
        adminRole.setName(RoleType.ADMIN);

        customerRole = new Role();
        customerRole.setRoleId(2L);
        customerRole.setName(RoleType.CUSTOMER);

        // Setup admin user
        adminUser = new User();
        adminUser.setUserId(1L);
        adminUser.setEmail("admin@example.com");
        adminUser.setPasswordHash("hashedPassword");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setRoles(Set.of(adminRole));
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setLastLoginAt(LocalDateTime.now().minusDays(1));

        // Setup regular user
        regularUser = new User();
        regularUser.setUserId(2L);
        regularUser.setEmail("user@example.com");
        regularUser.setPasswordHash("hashedPassword");
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setStatus(UserStatus.ACTIVE);
        regularUser.setRoles(Set.of(customerRole));
        regularUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void authenticate_Success() {
        // Arrange
        AdminLoginRequest loginRequest = new AdminLoginRequest();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword("password123");

        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(adminUser));
        when(jwtTokenProvider.generateToken(adminUser)).thenReturn("jwt-token-123");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(86400000L);

        // Act
        JwtAuthResponse result = adminAuthService.authenticate(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals("jwt-token-123", result.getToken());
        assertEquals(86400000L, result.getExpiresIn());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(2)).findByEmail(loginRequest.getEmail()); // Called twice: once in authenticate, once in updateAdminLastLogin
        verify(jwtTokenProvider).generateToken(adminUser);
        verify(userRepository).updateLastLoginTime(eq(adminUser.getUserId()), any(LocalDateTime.class));
    }

    @Test
    void authenticate_UserNotFound_ThrowsException() {
        // Arrange
        AdminLoginRequest loginRequest = new AdminLoginRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("password123");

        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            adminAuthService.authenticate(loginRequest);
        });

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void authenticate_NonAdminUser_ThrowsException() {
        // Arrange
        AdminLoginRequest loginRequest = new AdminLoginRequest();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("password123");

        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(regularUser));

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            adminAuthService.authenticate(loginRequest);
        });

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void authenticate_InactiveUser_ThrowsException() {
        // Arrange
        AdminLoginRequest loginRequest = new AdminLoginRequest();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword("password123");

        User inactiveAdmin = new User();
        inactiveAdmin.setEmail("admin@example.com");
        inactiveAdmin.setStatus(UserStatus.INACTIVE);
        inactiveAdmin.setRoles(Set.of(adminRole));

        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(inactiveAdmin));

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            adminAuthService.authenticate(loginRequest);
        });

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void authenticate_AuthenticationFails_ThrowsException() {
        // Arrange
        AdminLoginRequest loginRequest = new AdminLoginRequest();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            adminAuthService.authenticate(loginRequest);
        });

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void getAdminProfile_Success() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        // Act
        AdminUserDTO result = adminAuthService.getAdminProfile();

        // Assert
        assertNotNull(result);
        assertEquals(adminUser.getUserId(), result.getId());
        assertEquals(adminUser.getEmail(), result.getEmail());
        assertEquals(adminUser.getFirstName(), result.getFirstName());
        assertEquals(adminUser.getLastName(), result.getLastName());
        assertTrue(result.getActive());
        assertTrue(result.getRoles().contains("ADMIN"));
    }

    @Test
    void getAdminProfile_NotAuthenticated_ThrowsException() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            adminAuthService.getAdminProfile();
        });

        assertEquals("ADMIN_NOT_AUTHENTICATED", exception.getErrorCode());
    }

    @Test
    void getAdminProfile_NonAdminUser_ThrowsException() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(regularUser));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            adminAuthService.getAdminProfile();
        });

        assertEquals("INSUFFICIENT_PRIVILEGES", exception.getErrorCode());
    }

    @Test
    void changePassword_Success() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("newPassword");

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("oldPassword", adminUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");
        when(userRepository.save(adminUser)).thenReturn(adminUser);

        // Act
        adminAuthService.changePassword(request);

        // Assert
        verify(passwordEncoder).matches("oldPassword", "hashedPassword"); // Verify with original password hash
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(adminUser);
        assertEquals("newHashedPassword", adminUser.getPasswordHash());
    }

    @Test
    void changePassword_IncorrectCurrentPassword_ThrowsException() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("newPassword");

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("wrongPassword", adminUser.getPasswordHash())).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            adminAuthService.changePassword(request);
        });

        assertEquals("INVALID_CURRENT_PASSWORD", exception.getErrorCode());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_PasswordMismatch_ThrowsException() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("differentPassword");

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("oldPassword", adminUser.getPasswordHash())).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            adminAuthService.changePassword(request);
        });

        assertEquals("PASSWORD_MISMATCH", exception.getErrorCode());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getCurrentAdmin_Success() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        // Act
        User result = adminAuthService.getCurrentAdmin();

        // Assert
        assertNotNull(result);
        assertEquals(adminUser.getEmail(), result.getEmail());
        assertEquals(adminUser.getUserId(), result.getUserId());
    }

    @Test
    void getCurrentAdmin_NotAuthenticated_ThrowsException() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            adminAuthService.getCurrentAdmin();
        });

        assertEquals("ADMIN_NOT_AUTHENTICATED", exception.getErrorCode());
    }

    @Test
    void getCurrentAdmin_UserNotFound_ThrowsException() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            adminAuthService.getCurrentAdmin();
        });

        assertEquals("ADMIN_USER_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void getCurrentAdmin_NonAdminUser_ThrowsException() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(regularUser));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            adminAuthService.getCurrentAdmin();
        });

        assertEquals("INSUFFICIENT_PRIVILEGES", exception.getErrorCode());
    }

    @Test
    void updateAdminLastLogin_Success() {
        // Arrange
        String email = "admin@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));

        // Act
        adminAuthService.updateAdminLastLogin(email);

        // Assert
        verify(userRepository).findByEmail(email);
        verify(userRepository).updateLastLoginTime(eq(adminUser.getUserId()), any(LocalDateTime.class));
    }

    @Test
    void updateAdminLastLogin_UserNotFound_ThrowsException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            adminAuthService.updateAdminLastLogin(email);
        });

        assertEquals("ADMIN_USER_NOT_FOUND", exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }
}