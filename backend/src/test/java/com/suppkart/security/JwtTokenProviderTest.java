package com.suppkart.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import com.suppkart.model.entity.Role;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.RoleType;

@ExtendWith(MockitoExtension.class)
public class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private UserDetails userDetails;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Set test properties
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "testSecretKeyThatIsLongEnoughForHMACAlgorithm");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", 86400000); // 24 hours

        // Setup test user
        Role customerRole = new Role();
        customerRole.setRoleId(1L);
        customerRole.setName(RoleType.CUSTOMER);

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setRoles(Set.of(customerRole));

        // Setup UserDetails mock
        userDetails = mock(UserDetails.class);
    }

    @Test
    void generateToken_WithUserDetails_Success() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("test@example.com");
        
        // Act
        String token = jwtTokenProvider.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
    }

    @Test
    void generateToken_WithExtraClaims_Success() {
        // Arrange
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "CUSTOMER");
        extraClaims.put("userId", 1L);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        // Act
        String token = jwtTokenProvider.generateToken(extraClaims, userDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void extractUsername_ValidToken_ReturnsUsername() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("test@example.com");
        String token = jwtTokenProvider.generateToken(userDetails);

        // Act
        String extractedUsername = jwtTokenProvider.extractUsername(token);

        // Assert
        assertEquals("test@example.com", extractedUsername);
    }

    @Test
    void extractExpiration_ValidToken_ReturnsExpirationDate() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("test@example.com");
        String token = jwtTokenProvider.generateToken(userDetails);

        // Act
        Date expiration = jwtTokenProvider.extractExpiration(token);

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date())); // Should be in the future
    }

    @Test
    void isTokenExpired_ValidToken_ReturnsFalse() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("test@example.com");
        String token = jwtTokenProvider.generateToken(userDetails);

        // Act
        Boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("test@example.com");
        String token = jwtTokenProvider.generateToken(userDetails);

        // Act
        Boolean isValid = jwtTokenProvider.validateToken(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_WrongUsername_ReturnsFalse() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("test@example.com");
        String token = jwtTokenProvider.generateToken(userDetails);
        UserDetails wrongUserDetails = mock(UserDetails.class);
        when(wrongUserDetails.getUsername()).thenReturn("wrong@example.com");

        // Act
        Boolean isValid = jwtTokenProvider.validateToken(token, wrongUserDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("test@example.com");
        // Set very short expiration time
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", 1); // 1 millisecond
        String token = jwtTokenProvider.generateToken(userDetails);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        Boolean isValid = jwtTokenProvider.validateToken(token, userDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void generateTokenFromUser_Success() {
        // Act
        String token = jwtTokenProvider.generateTokenFromUser(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);

        // Verify token contains user information
        String extractedUsername = jwtTokenProvider.extractUsername(token);
        assertEquals(testUser.getEmail(), extractedUsername);

        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);
        assertEquals(testUser.getUserId(), extractedUserId);
    }

    @Test
    void generateRefreshToken_Success() {
        // Act
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        // Assert
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        // Refresh token should be a UUID format
        assertTrue(refreshToken.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    void getUserIdFromToken_ValidToken_ReturnsUserId() {
        // Arrange
        String token = jwtTokenProvider.generateTokenFromUser(testUser);

        // Act
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        // Assert
        assertEquals(testUser.getUserId(), userId);
    }

    @Test
    void getUserIdFromToken_TokenWithoutUserId_ThrowsException() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("test@example.com");
        String token = jwtTokenProvider.generateToken(userDetails); // This token won't have userId claim

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            jwtTokenProvider.getUserIdFromToken(token);
        });

        assertEquals("User ID not found in token", exception.getMessage());
    }

    @Test
    void getExpirationTime_ReturnsConfiguredTime() {
        // Act
        long expirationTime = jwtTokenProvider.getExpirationTime();

        // Assert
        assertEquals(86400000L, expirationTime); // 24 hours in milliseconds
    }

    @Test
    void getExpirationDateTime_ReturnsCorrectDateTime() {
        // Act
        LocalDateTime expirationDateTime = jwtTokenProvider.getExpirationDateTime();

        // Assert
        assertNotNull(expirationDateTime);
        assertTrue(expirationDateTime.isAfter(LocalDateTime.now()));
        // Should be approximately 24 hours from now (allowing for small time differences)
        assertTrue(expirationDateTime.isBefore(LocalDateTime.now().plusHours(25)));
    }

    @Test
    void extractClaim_CustomClaim_ReturnsClaimValue() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("test@example.com");
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "customValue");
        String token = jwtTokenProvider.generateToken(extraClaims, userDetails);

        // Act
        String customClaimValue = jwtTokenProvider.extractClaim(token, claims -> claims.get("customClaim", String.class));

        // Assert
        assertEquals("customValue", customClaimValue);
    }

    @Test
    void generateToken_MultipleTokens_AreUnique() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("test@example.com");
        
        // Act
        String token1 = jwtTokenProvider.generateToken(userDetails);
        
        // Wait a bit to ensure different issued times
        try {
            Thread.sleep(1000); // 1 second to ensure different timestamps
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String token2 = jwtTokenProvider.generateToken(userDetails);

        // Assert
        assertNotEquals(token1, token2); // Tokens should be different due to different issued times
    }

    @Test
    void generateRefreshToken_MultipleTokens_AreUnique() {
        // Act
        String refreshToken1 = jwtTokenProvider.generateRefreshToken();
        String refreshToken2 = jwtTokenProvider.generateRefreshToken();

        // Assert
        assertNotEquals(refreshToken1, refreshToken2);
    }
}