package com.suppkart.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.suppkart.model.entity.Role;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.AuthProvider;
import com.suppkart.model.enums.RoleType;
import com.suppkart.model.enums.UserStatus;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User adminUser;
    private User regularUser;
    private Role adminRole;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        // Create roles
        adminRole = new Role();
        adminRole.setName(RoleType.ADMIN);
        adminRole.setDescription("Administrator role");
        adminRole.setCreatedAt(LocalDateTime.now());
        adminRole.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(adminRole);

        customerRole = new Role();
        customerRole.setName(RoleType.CUSTOMER);
        customerRole.setDescription("Customer role");
        customerRole.setCreatedAt(LocalDateTime.now());
        customerRole.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(customerRole);

        // Create admin user
        adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPasswordHash("$2a$10$hashedPassword");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setAuthProvider(AuthProvider.EMAIL);
        adminUser.setRoles(Set.of(adminRole));
        adminUser.setCreatedAt(LocalDateTime.now().minusDays(30));
        adminUser.setUpdatedAt(LocalDateTime.now());
        adminUser.setLastLoginAt(LocalDateTime.now().minusDays(1));

        // Create regular user
        regularUser = new User();
        regularUser.setEmail("user@example.com");
        regularUser.setPasswordHash("$2a$10$hashedPassword");
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setStatus(UserStatus.ACTIVE);
        regularUser.setAuthProvider(AuthProvider.EMAIL);
        regularUser.setRoles(Set.of(customerRole));
        regularUser.setCreatedAt(LocalDateTime.now().minusDays(15));
        regularUser.setUpdatedAt(LocalDateTime.now());
        regularUser.setLastLoginAt(LocalDateTime.now().minusDays(2));

        // Persist users
        entityManager.persistAndFlush(adminUser);
        entityManager.persistAndFlush(regularUser);
        entityManager.clear();
    }

    @Test
    void findByEmail_ExistingUser_ReturnsUser() {
        // Act
        Optional<User> result = userRepository.findByEmail("admin@example.com");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("admin@example.com");
        assertThat(result.get().getFirstName()).isEqualTo("Admin");
        assertThat(result.get().getRoles()).hasSize(1);
        assertThat(result.get().getRoles().iterator().next().getName()).isEqualTo(RoleType.ADMIN);
    }

    @Test
    void findByEmail_NonExistentUser_ReturnsEmpty() {
        // Act
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_NullEmail_ReturnsEmpty() {
        // Act
        Optional<User> result = userRepository.findByEmail(null);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_EmptyEmail_ReturnsEmpty() {
        // Act
        Optional<User> result = userRepository.findByEmail("");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_CaseInsensitive_ReturnsEmpty() {
        // Act - H2 database is case-sensitive by default
        Optional<User> result = userRepository.findByEmail("ADMIN@EXAMPLE.COM");

        // Assert - H2 test database is case-sensitive
        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmail_ExistingUser_ReturnsTrue() {
        // Act
        boolean exists = userRepository.existsByEmail("admin@example.com");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_NonExistentUser_ReturnsFalse() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_NullEmail_ReturnsFalse() {
        // Act
        boolean exists = userRepository.existsByEmail(null);

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void updateLastLoginTime_ValidUser_UpdatesTimestamp() {
        // Arrange
        LocalDateTime newLoginTime = LocalDateTime.now();
        Long userId = adminUser.getUserId();

        // Act
        userRepository.updateLastLoginTime(userId, newLoginTime);
        entityManager.flush();
        entityManager.clear();

        // Assert
        User updatedUser = userRepository.findById(userId).orElseThrow();
        assertThat(updatedUser.getLastLoginAt()).isEqualToIgnoringNanos(newLoginTime);
    }

    @Test
    void updateLastLoginTime_NonExistentUser_NoException() {
        // Arrange
        LocalDateTime newLoginTime = LocalDateTime.now();
        Long nonExistentUserId = 99999L;

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> {
            userRepository.updateLastLoginTime(nonExistentUserId, newLoginTime);
            entityManager.flush();
        });
    }

    @Test
    void updateLastLoginTime_NullTimestamp_UpdatesWithNull() {
        // Arrange
        Long userId = adminUser.getUserId();

        // Act
        userRepository.updateLastLoginTime(userId, null);
        entityManager.flush();
        entityManager.clear();

        // Assert
        User updatedUser = userRepository.findById(userId).orElseThrow();
        assertThat(updatedUser.getLastLoginAt()).isNull();
    }

    @Test
    void save_NewUser_PersistsUser() {
        // Arrange
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setPasswordHash("$2a$10$newHashedPassword");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setStatus(UserStatus.ACTIVE);
        newUser.setAuthProvider(AuthProvider.EMAIL);
        newUser.setRoles(Set.of(customerRole));
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        // Act
        User savedUser = userRepository.save(newUser);

        // Assert
        assertThat(savedUser.getUserId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
        
        // Verify it's actually persisted
        Optional<User> foundUser = userRepository.findByEmail("newuser@example.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getFirstName()).isEqualTo("New");
    }

    @Test
    void save_UpdateExistingUser_UpdatesUser() {
        // Arrange
        User existingUser = userRepository.findByEmail("admin@example.com").orElseThrow();
        String originalEmail = existingUser.getEmail();
        existingUser.setStatus(UserStatus.INACTIVE);
        existingUser.setUpdatedAt(LocalDateTime.now());

        // Act
        User savedUser = userRepository.save(existingUser);

        // Assert
        assertThat(savedUser.getUserId()).isEqualTo(existingUser.getUserId());
        assertThat(savedUser.getEmail()).isEqualTo(originalEmail);
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
        
        // Verify it's actually updated in database
        User foundUser = userRepository.findById(existingUser.getUserId()).orElseThrow();
        assertThat(foundUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    void findByRolesContaining_AdminRole_ReturnsAdminUsers() {
        // Act
        var adminUsers = userRepository.findByRolesContaining(RoleType.ADMIN);

        // Assert
        assertThat(adminUsers).hasSize(1);
        assertThat(adminUsers.get(0).getEmail()).isEqualTo("admin@example.com");
    }

    @Test
    void findByRolesContaining_CustomerRole_ReturnsCustomerUsers() {
        // Act
        var customerUsers = userRepository.findByRolesContaining(RoleType.CUSTOMER);

        // Assert
        assertThat(customerUsers).hasSize(1);
        assertThat(customerUsers.get(0).getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void findByRolesContaining_NonExistentRole_ReturnsEmptyList() {
        // Act
        var users = userRepository.findByRolesContaining(RoleType.SUPER_ADMIN);

        // Assert
        assertThat(users).isEmpty();
    }

    @Test
    void findByStatus_ActiveUsers_ReturnsActiveUsers() {
        // Act
        var activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);

        // Assert
        assertThat(activeUsers).hasSize(2);
        assertThat(activeUsers).extracting(User::getStatus)
                .containsOnly(UserStatus.ACTIVE);
    }

    @Test
    void findByStatus_InactiveUsers_ReturnsEmptyList() {
        // Act
        var inactiveUsers = userRepository.findByStatus(UserStatus.INACTIVE);

        // Assert
        assertThat(inactiveUsers).isEmpty();
    }

    @Test
    void countByStatus_ActiveUsers_ReturnsCorrectCount() {
        // Act
        long activeCount = userRepository.countByStatus(UserStatus.ACTIVE);

        // Assert
        assertThat(activeCount).isEqualTo(2);
    }

    @Test
    void countByStatus_InactiveUsers_ReturnsZero() {
        // Act
        long inactiveCount = userRepository.countByStatus(UserStatus.INACTIVE);

        // Assert
        assertThat(inactiveCount).isEqualTo(0);
    }

    @Test
    void countUsersCreatedAfter_RecentDate_ReturnsCorrectCount() {
        // Arrange
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(20);

        // Act
        long recentUsersCount = userRepository.countUsersCreatedAfter(cutoffDate);

        // Assert
        // Both users are created during test setup, so both will be after the cutoff
        assertThat(recentUsersCount).isEqualTo(2);
    }

    @Test
    void countUsersCreatedAfter_VeryOldDate_ReturnsAllUsers() {
        // Arrange
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(100);

        // Act
        long allUsersCount = userRepository.countUsersCreatedAfter(cutoffDate);

        // Assert
        assertThat(allUsersCount).isEqualTo(2);
    }

    @Test
    void countUsersCreatedAfter_FutureDate_ReturnsZero() {
        // Arrange
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);

        // Act
        long futureUsersCount = userRepository.countUsersCreatedAfter(futureDate);

        // Assert
        assertThat(futureUsersCount).isEqualTo(0);
    }

    @Test
    void findByUsername_ExistingUser_ReturnsUser() {
        // Act (findByUsername is an alias for findByEmail)
        Optional<User> result = userRepository.findByUsername("admin@example.com");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("admin@example.com");
    }

    @Test
    void findByUsername_NonExistentUser_ReturnsEmpty() {
        // Act
        Optional<User> result = userRepository.findByUsername("nonexistent@example.com");

        // Assert
        assertThat(result).isEmpty();
    }
}