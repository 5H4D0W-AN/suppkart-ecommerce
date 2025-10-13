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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.suppkart.model.entity.RefreshToken;
import com.suppkart.model.entity.Role;
import com.suppkart.model.entity.User;
import com.suppkart.model.entity.UserProfile;
import com.suppkart.model.enums.AuthProvider;
import com.suppkart.model.enums.RoleType;
import com.suppkart.model.enums.UserStatus;
import com.suppkart.repository.RefreshTokenRepository;
import com.suppkart.repository.RoleRepository;
import com.suppkart.repository.UserRepository;
import com.suppkart.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private CartService cartService;

    @Mock
    private ReferralService referralService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private Role customerRole;
    private UserProfile testProfile;
    private RefreshToken testRefreshToken;
    private com.suppkart.model.entity.Cart testCart;

    @BeforeEach
    void setUp() {
        // Setup test role
        customerRole = new Role();
        customerRole.setRoleId(1L);
        customerRole.setName(RoleType.CUSTOMER);

        // Setup test user
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setAuthProvider(AuthProvider.EMAIL);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRoles(Set.of(customerRole));
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        // Setup test profile
        testProfile = new UserProfile();
        testProfile.setUser(testUser);
        testProfile.setFirstName("John");
        testProfile.setLastName("Doe");
        testProfile.setPhone("1234567890");
        testProfile.setCreatedAt(LocalDateTime.now());
        testProfile.setUpdatedAt(LocalDateTime.now());

        testUser.setUserProfile(testProfile);

        // Setup test refresh token
        testRefreshToken = new RefreshToken();
        testRefreshToken.setTokenId(1L);
        testRefreshToken.setUser(testUser);
        testRefreshToken.setToken("refresh-token-123");
        testRefreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        testRefreshToken.setCreatedAt(LocalDateTime.now());

        // Setup test cart
        testCart = new com.suppkart.model.entity.Cart();
        testCart.setUser(testUser);
        testCart.setCreatedAt(LocalDateTime.now());
        testCart.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void registerUser_Success() {
        // Arrange
        String email = "newuser@example.com";
        String password = "password123";
        String firstName = "Jane";
        String lastName = "Smith";
        String phone = "9876543210";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(roleRepository.findByName(RoleType.CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode(password)).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userService.saveUserProfile(any(UserProfile.class))).thenReturn(testProfile);
        when(cartService.createCartForUser(any(User.class))).thenReturn(testCart);

        // Act
        User result = authenticationService.registerUser(email, password, firstName, lastName, phone);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).existsByEmail(email);
        verify(roleRepository).findByName(RoleType.CUSTOMER);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
        verify(userService).saveUserProfile(any(UserProfile.class));
        verify(cartService).createCartForUser(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsException() {
        // Arrange
        String email = "existing@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.registerUser(email, "password", "John", "Doe", "1234567890");
        });

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WithReferralCode_Success() {
        // Arrange
        String email = "newuser@example.com";
        String password = "password123";
        String firstName = "Jane";
        String lastName = "Smith";
        String phone = "9876543210";
        String referralCode = "REF123";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(roleRepository.findByName(RoleType.CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode(password)).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userService.saveUserProfile(any(UserProfile.class))).thenReturn(testProfile);
        when(cartService.createCartForUser(any(User.class))).thenReturn(testCart);
        doNothing().when(referralService).processNewUserRegistration(any(User.class), eq(referralCode));

        // Act
        User result = authenticationService.registerUser(email, password, firstName, lastName, phone, referralCode);

        // Assert
        assertNotNull(result);
        verify(referralService).processNewUserRegistration(any(User.class), eq(referralCode));
    }

    @Test
    void authenticateUser_Success() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn(email);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);

        // Act
        Authentication result = authenticationService.authenticateUser(email, password);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getName());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void authenticateUser_InvalidCredentials_ThrowsException() {
        // Arrange
        String email = "test@example.com";
        String password = "wrongpassword";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticateUser(email, password);
        });

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void generateAccessToken_Success() {
        // Arrange
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateTokenFromUser(testUser)).thenReturn("jwt-token-123");

        // Act
        String result = authenticationService.generateAccessToken(mockAuth);

        // Assert
        assertEquals("jwt-token-123", result);
        verify(userRepository).findByEmail(testUser.getEmail());
        verify(jwtTokenProvider).generateTokenFromUser(testUser);
    }

    @Test
    void generateAccessToken_UserNotFound_ThrowsException() {
        // Arrange
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.generateAccessToken(mockAuth);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void generateRefreshToken_Success() {
        // Arrange
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("refresh-token-123");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);
        doNothing().when(refreshTokenRepository).deleteByUser(testUser);

        // Act
        RefreshToken result = authenticationService.generateRefreshToken(testUser);

        // Assert
        assertNotNull(result);
        assertEquals("refresh-token-123", result.getToken());
        verify(refreshTokenRepository).deleteByUser(testUser);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void refreshAccessToken_Success() {
        // Arrange
        String refreshTokenValue = "refresh-token-123";
        when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(Optional.of(testRefreshToken));
        when(jwtTokenProvider.generateTokenFromUser(testUser)).thenReturn("new-jwt-token");

        // Act
        String result = authenticationService.refreshAccessToken(refreshTokenValue);

        // Assert
        assertEquals("new-jwt-token", result);
        verify(refreshTokenRepository).findByToken(refreshTokenValue);
        verify(jwtTokenProvider).generateTokenFromUser(testUser);
    }

    @Test
    void refreshAccessToken_InvalidToken_ThrowsException() {
        // Arrange
        String refreshTokenValue = "invalid-token";
        when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.refreshAccessToken(refreshTokenValue);
        });

        assertEquals("Invalid refresh token", exception.getMessage());
    }

    @Test
    void refreshAccessToken_ExpiredToken_ThrowsException() {
        // Arrange
        String refreshTokenValue = "expired-token";
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken(refreshTokenValue);
        expiredToken.setUser(testUser);
        expiredToken.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired

        when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(Optional.of(expiredToken));
        doNothing().when(refreshTokenRepository).delete(expiredToken);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.refreshAccessToken(refreshTokenValue);
        });

        assertEquals("Refresh token expired", exception.getMessage());
        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    void logout_Success() {
        // Arrange
        String refreshTokenValue = "refresh-token-123";
        when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(Optional.of(testRefreshToken));
        doNothing().when(refreshTokenRepository).delete(testRefreshToken);

        // Act
        authenticationService.logout(refreshTokenValue);

        // Assert
        verify(refreshTokenRepository).findByToken(refreshTokenValue);
        verify(refreshTokenRepository).delete(testRefreshToken);
    }

    @Test
    void logout_InvalidToken_NoException() {
        // Arrange
        String refreshTokenValue = "invalid-token";
        when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(Optional.empty());

        // Act & Assert (should not throw exception)
        assertDoesNotThrow(() -> {
            authenticationService.logout(refreshTokenValue);
        });

        verify(refreshTokenRepository).findByToken(refreshTokenValue);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    void logoutUser_Success() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        doNothing().when(refreshTokenRepository).deleteByUser(testUser);

        // Act
        authenticationService.logoutUser(userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(refreshTokenRepository).deleteByUser(testUser);
    }

    @Test
    void logoutUser_UserNotFound_ThrowsException() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.logoutUser(userId);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void changePassword_Success() {
        // Arrange
        Long userId = 1L;
        String currentPassword = "oldPassword";
        String newPassword = "newPassword";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("newHashedPassword");
        when(userRepository.save(testUser)).thenReturn(testUser);
        doNothing().when(refreshTokenRepository).deleteByUser(testUser);

        // Act
        authenticationService.changePassword(userId, currentPassword, newPassword);

        // Assert
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(currentPassword, "hashedPassword");
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(testUser);
        verify(refreshTokenRepository).deleteByUser(testUser);
    }

    @Test
    void changePassword_IncorrectCurrentPassword_ThrowsException() {
        // Arrange
        Long userId = 1L;
        String currentPassword = "wrongPassword";
        String newPassword = "newPassword";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPasswordHash())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.changePassword(userId, currentPassword, newPassword);
        });

        assertEquals("Current password is incorrect", exception.getMessage());
        verify(passwordEncoder, never()).encode(newPassword);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_Success() {
        // Arrange
        String email = "test@example.com";
        String newPassword = "newPassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(newPassword)).thenReturn("newHashedPassword");
        when(userRepository.save(testUser)).thenReturn(testUser);
        doNothing().when(refreshTokenRepository).deleteByUser(testUser);

        // Act
        authenticationService.resetPassword(email, newPassword);

        // Assert
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(testUser);
        verify(refreshTokenRepository).deleteByUser(testUser);
    }

    @Test
    void resetPassword_UserNotFound_ThrowsException() {
        // Arrange
        String email = "nonexistent@example.com";
        String newPassword = "newPassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.resetPassword(email, newPassword);
        });

        assertEquals("User not found", exception.getMessage());
        verify(passwordEncoder, never()).encode(newPassword);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void isRefreshTokenValid_ValidToken_ReturnsTrue() {
        // Arrange
        String tokenValue = "valid-token";
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(testRefreshToken));

        // Act
        boolean result = authenticationService.isRefreshTokenValid(tokenValue);

        // Assert
        assertTrue(result);
        verify(refreshTokenRepository).findByToken(tokenValue);
    }

    @Test
    void isRefreshTokenValid_InvalidToken_ReturnsFalse() {
        // Arrange
        String tokenValue = "invalid-token";
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.empty());

        // Act
        boolean result = authenticationService.isRefreshTokenValid(tokenValue);

        // Assert
        assertFalse(result);
        verify(refreshTokenRepository).findByToken(tokenValue);
    }

    @Test
    void isRefreshTokenValid_ExpiredToken_ReturnsFalse() {
        // Arrange
        String tokenValue = "expired-token";
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken(tokenValue);
        expiredToken.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired

        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(expiredToken));

        // Act
        boolean result = authenticationService.isRefreshTokenValid(tokenValue);

        // Assert
        assertFalse(result);
        verify(refreshTokenRepository).findByToken(tokenValue);
    }

    @Test
    void getUserByRefreshToken_Success() {
        // Arrange
        String tokenValue = "valid-token";
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(testRefreshToken));

        // Act
        User result = authenticationService.getUserByRefreshToken(tokenValue);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(refreshTokenRepository).findByToken(tokenValue);
    }

    @Test
    void getUserByRefreshToken_InvalidToken_ThrowsException() {
        // Arrange
        String tokenValue = "invalid-token";
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.getUserByRefreshToken(tokenValue);
        });

        assertEquals("Invalid refresh token", exception.getMessage());
    }

    @Test
    void getUserByRefreshToken_ExpiredToken_ThrowsException() {
        // Arrange
        String tokenValue = "expired-token";
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken(tokenValue);
        expiredToken.setUser(testUser);
        expiredToken.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired

        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(expiredToken));
        doNothing().when(refreshTokenRepository).delete(expiredToken);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.getUserByRefreshToken(tokenValue);
        });

        assertEquals("Refresh token expired", exception.getMessage());
        verify(refreshTokenRepository).delete(expiredToken);
    }
}