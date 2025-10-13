package com.suppkart.security;

import static org.junit.jupiter.api.Assertions.*;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.suppkart.model.entity.Role;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.AuthProvider;
import com.suppkart.model.enums.RoleType;
import com.suppkart.model.enums.UserStatus;
import com.suppkart.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private Role customerRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // Setup roles
        customerRole = new Role();
        customerRole.setRoleId(1L);
        customerRole.setName(RoleType.CUSTOMER);

        adminRole = new Role();
        adminRole.setRoleId(2L);
        adminRole.setName(RoleType.ADMIN);

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
    }

    @Test
    void loadUserByUsername_ExistingUser_ReturnsUserDetails() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("hashedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());

        // Check authorities
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_CUSTOMER")));

        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_NonExistentUser_ThrowsUsernameNotFoundException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(email);
        });

        assertEquals("User not found with email: " + email, exception.getMessage());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_AdminUser_ReturnsUserDetailsWithAdminRole() {
        // Arrange
        String email = "admin@example.com";
        User adminUser = new User();
        adminUser.setUserId(2L);
        adminUser.setEmail(email);
        adminUser.setPasswordHash("adminHashedPassword");
        adminUser.setRoles(Set.of(adminRole));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("adminHashedPassword", userDetails.getPassword());

        // Check authorities
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));

        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_UserWithMultipleRoles_ReturnsUserDetailsWithAllRoles() {
        // Arrange
        String email = "superuser@example.com";
        User superUser = new User();
        superUser.setUserId(3L);
        superUser.setEmail(email);
        superUser.setPasswordHash("superHashedPassword");
        superUser.setRoles(Set.of(customerRole, adminRole));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(superUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());

        // Check authorities - should have both roles
        assertEquals(2, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_CUSTOMER")));
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));

        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserById_ExistingUser_ReturnsUserDetails() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserById(userId);

        // Assert
        assertNotNull(userDetails);
        assertEquals(testUser.getEmail(), userDetails.getUsername());
        assertEquals(testUser.getPasswordHash(), userDetails.getPassword());
        assertTrue(userDetails.isEnabled());

        verify(userRepository).findById(userId);
    }

    @Test
    void loadUserById_NonExistentUser_ThrowsUsernameNotFoundException() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserById(userId);
        });

        assertEquals("User not found with id: " + userId, exception.getMessage());
        verify(userRepository).findById(userId);
    }

    @Test
    void userPrincipal_Create_Success() {
        // Act
        CustomUserDetailsService.UserPrincipal userPrincipal = 
                CustomUserDetailsService.UserPrincipal.create(testUser);

        // Assert
        assertNotNull(userPrincipal);
        assertEquals(testUser.getUserId(), userPrincipal.getId());
        assertEquals(testUser.getEmail(), userPrincipal.getEmail());
        assertEquals(testUser.getEmail(), userPrincipal.getUsername());
        assertEquals(testUser.getPasswordHash(), userPrincipal.getPassword());

        // Check authorities
        assertEquals(1, userPrincipal.getAuthorities().size());
        GrantedAuthority authority = userPrincipal.getAuthorities().iterator().next();
        assertEquals("ROLE_CUSTOMER", authority.getAuthority());
    }

    @Test
    void userPrincipal_AccountStatus_AllTrue() {
        // Arrange
        CustomUserDetailsService.UserPrincipal userPrincipal = 
                CustomUserDetailsService.UserPrincipal.create(testUser);

        // Act & Assert
        assertTrue(userPrincipal.isAccountNonExpired());
        assertTrue(userPrincipal.isAccountNonLocked());
        assertTrue(userPrincipal.isCredentialsNonExpired());
        assertTrue(userPrincipal.isEnabled());
    }

    @Test
    void userPrincipal_GettersWork() {
        // Arrange
        CustomUserDetailsService.UserPrincipal userPrincipal = 
                CustomUserDetailsService.UserPrincipal.create(testUser);

        // Act & Assert
        assertEquals(testUser.getUserId(), userPrincipal.getId());
        assertEquals(testUser.getEmail(), userPrincipal.getEmail());
        assertEquals(testUser.getEmail(), userPrincipal.getUsername());
        assertEquals(testUser.getPasswordHash(), userPrincipal.getPassword());
    }

    @Test
    void userPrincipal_WithNullRoles_HandlesGracefully() {
        // Arrange
        User userWithoutRoles = new User();
        userWithoutRoles.setUserId(1L);
        userWithoutRoles.setEmail("test@example.com");
        userWithoutRoles.setPasswordHash("hashedPassword");
        userWithoutRoles.setRoles(Set.of()); // Empty roles

        // Act
        CustomUserDetailsService.UserPrincipal userPrincipal = 
                CustomUserDetailsService.UserPrincipal.create(userWithoutRoles);

        // Assert
        assertNotNull(userPrincipal);
        assertEquals(0, userPrincipal.getAuthorities().size());
    }

    @Test
    void loadUserByUsername_CaseInsensitive_WorksCorrectly() {
        // Arrange
        String email = "Test@Example.Com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(testUser.getEmail(), userDetails.getUsername()); // Should return the stored email format
        verify(userRepository).findByEmail(email);
    }
}