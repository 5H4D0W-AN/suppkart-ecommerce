package com.suppkart.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suppkart.dto.request.LoginRequest;
import com.suppkart.dto.request.RegisterRequest;
import com.suppkart.dto.response.AuthResponse;
import com.suppkart.model.entity.Role;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.AuthProvider;
import com.suppkart.model.enums.RoleType;
import com.suppkart.model.enums.UserStatus;
import com.suppkart.repository.RoleRepository;
import com.suppkart.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Role customerRole;
    private Role adminRole;
    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // Clean up database
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create roles
        customerRole = new Role();
        customerRole.setName(RoleType.CUSTOMER);
        customerRole.setDescription("Customer role");
        customerRole.setCreatedAt(LocalDateTime.now());
        customerRole = roleRepository.save(customerRole);

        adminRole = new Role();
        adminRole.setName(RoleType.ADMIN);
        adminRole.setDescription("Admin role");
        adminRole.setCreatedAt(LocalDateTime.now());
        adminRole = roleRepository.save(adminRole);

        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash(passwordEncoder.encode("password123"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setAuthProvider(AuthProvider.EMAIL);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRoles(Set.of(customerRole));
        testUser.setEmailVerified(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);
        
        // Ensure roles are properly saved
        userRepository.flush();

        // Create admin user
        adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPasswordHash(passwordEncoder.encode("admin123"));
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setAuthProvider(AuthProvider.EMAIL);
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setRoles(Set.of(adminRole));
        adminUser.setEmailVerified(true);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());
        adminUser = userRepository.save(adminUser);
        
        // Ensure roles are properly saved
        userRepository.flush();
    }

    @Test
    void registerUser_Success() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
        registerRequest.setPhone("1234567890");
        registerRequest.setReferralCode(null); // No referral code

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify response
        String responseContent = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseContent, AuthResponse.class);

        assertNotNull(authResponse.getAccessToken());
        assertNotNull(authResponse.getRefreshToken());
        assertNotNull(authResponse.getUser());
        assertEquals("newuser@example.com", authResponse.getUser().getEmail());
        assertEquals("New", authResponse.getUser().getFirstName());
        assertEquals("User", authResponse.getUser().getLastName());

        // Verify user was created in database
        assertTrue(userRepository.existsByEmail("newuser@example.com"));
    }

    @Test
    void registerUser_EmailAlreadyExists_ReturnsBadRequest() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com"); // Already exists
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Duplicate");
        registerRequest.setLastName("User");
        registerRequest.setPhone("1234567890");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: Email is already taken!"));
    }

    @Test
    void authenticateUser_Success() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify response
        String responseContent = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseContent, AuthResponse.class);

        assertNotNull(authResponse.getAccessToken());
        assertNotNull(authResponse.getRefreshToken());
        assertNotNull(authResponse.getUser());
        assertEquals("test@example.com", authResponse.getUser().getEmail());
    }

    @Test
    void authenticateUser_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error: Invalid credentials")));
    }

    @Test
    void authenticateUser_NonExistentUser_ReturnsUnauthorized() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error: Invalid credentials")));
    }

    @Test
    void adminLogin_Success() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword("admin123");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify response structure
        String responseContent = result.getResponse().getContentAsString();
        assertTrue(responseContent.contains("\"success\":true"));
        assertTrue(responseContent.contains("\"message\":\"Admin login successful\""));
        assertTrue(responseContent.contains("\"token\":"));
        assertTrue(responseContent.contains("\"expiresIn\":"));
    }

    @Test
    void adminLogin_NonAdminUser_ReturnsUnauthorized() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com"); // Regular user, not admin
        loginRequest.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"success\":false")));
    }

    @Test
    void adminLogin_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"success\":false")));
    }

    @Test
    void refreshToken_Success() throws Exception {
        // First, login to get tokens
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseContent = loginResult.getResponse().getContentAsString();
        AuthResponse loginResponse = objectMapper.readValue(loginResponseContent, AuthResponse.class);
        String refreshToken = loginResponse.getRefreshToken();

        // Wait a moment to ensure different timestamps
        Thread.sleep(1100); // Ensure different timestamps

        // Now test refresh token
        String refreshRequestJson = "{\"refreshToken\":\"" + refreshToken + "\"}";

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify new access token is returned
        String refreshResponseContent = refreshResult.getResponse().getContentAsString();
        AuthResponse refreshResponse = objectMapper.readValue(refreshResponseContent, AuthResponse.class);

        assertNotNull(refreshResponse.getAccessToken());
        assertNotEquals(loginResponse.getAccessToken(), refreshResponse.getAccessToken()); // Should be different
        assertEquals(refreshToken, refreshResponse.getRefreshToken()); // Should be same refresh token
    }

    @Test
    void refreshToken_InvalidToken_ReturnsUnauthorized() throws Exception {
        // Arrange
        String refreshRequestJson = "{\"refreshToken\":\"invalid-token\"}";

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Error: Invalid or expired refresh token"));
    }

    @Test
    void logout_Success() throws Exception {
        // First, login to get refresh token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseContent = loginResult.getResponse().getContentAsString();
        AuthResponse loginResponse = objectMapper.readValue(loginResponseContent, AuthResponse.class);
        String refreshToken = loginResponse.getRefreshToken();

        // Now test logout
        String logoutRequestJson = "{\"refreshToken\":\"" + refreshToken + "\"}";

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(logoutRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("User logged out successfully"));

        // Verify refresh token is no longer valid
        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(logoutRequestJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerUser_InvalidInput_ReturnsBadRequest() throws Exception {
        // Arrange - missing required fields
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(""); // Invalid email
        registerRequest.setPassword("123"); // Too short password
        registerRequest.setFirstName("");
        registerRequest.setLastName("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authenticateUser_EmptyCredentials_ReturnsBadRequest() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("");
        loginRequest.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_WithReferralCode_Success() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("referraluser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Referral");
        registerRequest.setLastName("User");
        registerRequest.setPhone("1234567890");
        registerRequest.setReferralCode("REF123");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify response
        String responseContent = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseContent, AuthResponse.class);

        assertNotNull(authResponse.getAccessToken());
        assertNotNull(authResponse.getRefreshToken());
        assertNotNull(authResponse.getUser());
        assertEquals("referraluser@example.com", authResponse.getUser().getEmail());
        assertEquals("Referral", authResponse.getUser().getFirstName());
        assertEquals("User", authResponse.getUser().getLastName());

        // Verify user was created in database
        assertTrue(userRepository.existsByEmail("referraluser@example.com"));
    }
}