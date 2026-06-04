package com.auth_service.unit.service;

import com.auth_service.dto.request.UpdateProfileRequest;
import com.auth_service.dto.response.UserProfileResponse;
import com.auth_service.entity.User;
import com.auth_service.exception.UserNotFoundException;
import com.auth_service.exception.UsernameAlreadyExistsException;
import com.auth_service.repository.RefreshTokenRepository;
import com.auth_service.repository.UserRepository;
import com.auth_service.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserServiceImpl.
 * Covers: getCurrentUser, updateProfile, deactivateAccount.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserServiceImpl userService;

    private User testUser;
    private String userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("john.doe@example.com");
        testUser.setUsername("johndoe");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEnabled(true);
        testUser.setDeleted(false);
        testUser.setPasswordHash("$2a$12$hashedPassword");
    }

    // =========================================================================
    // getCurrentUser() Tests
    // =========================================================================

    @Nested
    @DisplayName("getCurrentUser() Tests")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Should return user profile when user exists")
        void shouldReturnUserProfile_WhenUserExists() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // Act
            UserProfileResponse profile = userService.getCurrentUser(userId);

            // Assert
            assertThat(profile).isNotNull();
            assertThat(profile.email()).isEqualTo("john.doe@example.com");
            assertThat(profile.username()).isEqualTo("johndoe");
            verify(userRepository, times(1)).findById(userId);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user ID does not exist")
        void shouldThrowUserNotFoundException_WhenUserNotFound() {
            // Arrange
            when(userRepository.findById(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(UserNotFoundException.class, () -> userService.getCurrentUser("nonexistent-id"));
        }

        @Test
        @DisplayName("Should throw UserNotFoundException for soft-deleted user")
        void shouldThrowUserNotFoundException_WhenUserIsSoftDeleted() {
            // Arrange
            testUser.setDeleted(true);
            when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(UserNotFoundException.class, () -> userService.getCurrentUser(userId));
        }
    }

    // =========================================================================
    // updateProfile() Tests
    // =========================================================================

    @Nested
    @DisplayName("updateProfile() Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update first name and last name successfully")
        void shouldUpdateProfile_WhenValidRequestProvided() {
            // Arrange
            var request = new UpdateProfileRequest("Jonathan", "Smith", "jonathansmith");
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsernameAndIdNot("jonathansmith", userId)).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            // Act
            userService.updateProfile(userId, request);

            // Assert
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getFirstName()).isEqualTo("Jonathan");
            assertThat(userCaptor.getValue().getLastName()).isEqualTo("Smith");
            assertThat(userCaptor.getValue().getUsername()).isEqualTo("jonathansmith");
        }

        @Test
        @DisplayName("Should throw UsernameAlreadyExistsException when new username is taken")
        void shouldThrowException_WhenNewUsernameIsTaken() {
            // Arrange
            var request = new UpdateProfileRequest("John", "Doe", "takenusername");
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsernameAndIdNot("takenusername", userId)).thenReturn(true);

            // Act & Assert
            assertThrows(UsernameAlreadyExistsException.class, () -> userService.updateProfile(userId, request));
            verify(userRepository, never()).save(any());
        }
    }

    // =========================================================================
    // deactivateAccount() Tests
    // =========================================================================

    @Nested
    @DisplayName("deactivateAccount() Tests")
    class DeactivateAccountTests {

        @Test
        @DisplayName("Should soft-delete account and revoke all tokens on deactivation")
        void shouldSoftDeleteAccount_WhenDeactivateCalled() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("SecureP@ss123", testUser.getPasswordHash())).thenReturn(true);
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            // Act
            userService.deactivateAccount(userId, "SecureP@ss123");

            // Assert: user must be soft-deleted
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().isDeleted()).isTrue();
            assertThat(userCaptor.getValue().getDeletedAt()).isNotNull();

            // Assert: all refresh tokens must be revoked
            verify(refreshTokenRepository, times(1)).deleteAllByUserId(userId);
        }

        @Test
        @DisplayName("Should throw exception when provided password is incorrect")
        void shouldThrowException_WhenPasswordIsIncorrectDuringDeactivation() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongPassword", testUser.getPasswordHash())).thenReturn(false);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> userService.deactivateAccount(userId, "wrongPassword"));
            verify(userRepository, never()).save(any());
        }
    }
}
