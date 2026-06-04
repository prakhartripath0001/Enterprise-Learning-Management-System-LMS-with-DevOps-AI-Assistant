package com.auth_service.unit.service;

import com.auth_service.dto.request.ForgotPasswordRequest;
import com.auth_service.dto.request.LoginRequest;
import com.auth_service.dto.request.RegisterRequest;
import com.auth_service.dto.request.ResetPasswordRequest;
import com.auth_service.dto.response.AuthResponse;
import com.auth_service.dto.response.RegisterResponse;
import com.auth_service.entity.RefreshToken;
import com.auth_service.entity.Role;
import com.auth_service.entity.User;
import com.auth_service.exception.AccountLockedException;
import com.auth_service.exception.EmailAlreadyExistsException;
import com.auth_service.exception.EmailNotVerifiedException;
import com.auth_service.exception.InvalidCredentialsException;
import com.auth_service.exception.InvalidTokenException;
import com.auth_service.exception.TokenAlreadyUsedException;
import com.auth_service.exception.UserNotFoundException;
import com.auth_service.repository.EmailVerificationTokenRepository;
import com.auth_service.repository.LoginAuditLogRepository;
import com.auth_service.repository.PasswordResetTokenRepository;
import com.auth_service.repository.RefreshTokenRepository;
import com.auth_service.repository.RoleRepository;
import com.auth_service.repository.UserRepository;
import com.auth_service.service.JwtService;
import com.auth_service.service.impl.AuthServiceImpl;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthServiceImpl.
 *
 * Covers: register, login, logout, refreshToken, verifyEmail,
 *         forgotPassword, resetPassword
 *
 * Testing Framework: JUnit 5 + Mockito + AssertJ
 * Pattern: AAA (Arrange-Act-Assert)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    // =========================================================================
    // Mocks
    // =========================================================================

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Mock private LoginAuditLogRepository loginAuditLogRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    // =========================================================================
    // Test Fixtures
    // =========================================================================

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User activeUser;
    private Role studentRole;

    @BeforeEach
    void setUp() {
        studentRole = new Role();
        studentRole.setId(UUID.randomUUID().toString());
        studentRole.setName("ROLE_STUDENT");

        activeUser = new User();
        activeUser.setId(UUID.randomUUID().toString());
        activeUser.setEmail("john.doe@example.com");
        activeUser.setUsername("johndoe");
        activeUser.setPasswordHash("$2a$12$hashedPassword");
        activeUser.setFirstName("John");
        activeUser.setLastName("Doe");
        activeUser.setEnabled(true);
        activeUser.setAccountNonLocked(true);
        activeUser.setCredentialsNonExpired(true);
        activeUser.setEmailVerifiedAt(LocalDateTime.now());
        activeUser.setFailedLoginAttempts(0);
        activeUser.setRoles(Set.of(studentRole));

        validRegisterRequest = new RegisterRequest(
                "John", "Doe",
                "john.doe@example.com", "johndoe",
                "SecureP@ss123", "SecureP@ss123"
        );

        validLoginRequest = new LoginRequest(
                "john.doe@example.com", "SecureP@ss123",
                "a1b2c3d4", "WEB"
        );
    }

    // =========================================================================
    // register() Tests
    // =========================================================================

    @Nested
    @DisplayName("register() Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register user successfully when valid request")
        void shouldRegisterUser_WhenValidRequest() {
            // Arrange
            when(userRepository.existsByEmail(validRegisterRequest.email())).thenReturn(false);
            when(userRepository.existsByUsername(validRegisterRequest.username())).thenReturn(false);
            when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            RegisterResponse response = authService.register(validRegisterRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.email()).isEqualTo(validRegisterRequest.email());
            assertThat(response.username()).isEqualTo(validRegisterRequest.username());

            // Verify interactions
            verify(userRepository, times(1)).existsByEmail(validRegisterRequest.email());
            verify(userRepository, times(1)).existsByUsername(validRegisterRequest.username());
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should save user with hashed password — not plaintext")
        void shouldSaveHashedPassword_WhenRegisteringUser() {
            // Arrange
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));
            when(passwordEncoder.encode("SecureP@ss123")).thenReturn("$2a$12$hashedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            // Act
            authService.register(validRegisterRequest);

            // Assert: verify that password stored is the hashed value
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getPasswordHash()).isEqualTo("$2a$12$hashedPassword");
            assertThat(savedUser.getPasswordHash()).doesNotContain("SecureP@ss123");
        }

        @Test
        @DisplayName("Should throw EmailAlreadyExistsException when email is duplicate")
        void shouldThrowEmailAlreadyExistsException_WhenEmailIsDuplicate() {
            // Arrange
            when(userRepository.existsByEmail(validRegisterRequest.email())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.register(validRegisterRequest))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining("already exists");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw UsernameAlreadyExistsException when username is duplicate")
        void shouldThrowUsernameAlreadyExistsException_WhenUsernameIsDuplicate() {
            // Arrange
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByUsername(validRegisterRequest.username())).thenReturn(true);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> authService.register(validRegisterRequest));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should assign ROLE_STUDENT by default on registration")
        void shouldAssignStudentRole_WhenRegisteringNewUser() {
            // Arrange
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashed");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            // Act
            authService.register(validRegisterRequest);

            // Assert
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getRoles()).contains(studentRole);
        }
    }

    // =========================================================================
    // login() Tests
    // =========================================================================

    @Nested
    @DisplayName("login() Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully when credentials are valid")
        void shouldLoginSuccessfully_WhenCredentialsAreValid() {
            // Arrange
            when(userRepository.findByEmail(validLoginRequest.email())).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("SecureP@ss123", activeUser.getPasswordHash())).thenReturn(true);
            when(jwtService.generateAccessToken(any(User.class))).thenReturn("accessToken.jwt.value");
            when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken.jwt.value");
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            AuthResponse response = authService.login(validLoginRequest, "127.0.0.1", "test-agent");

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo("accessToken.jwt.value");
            assertThat(response.tokenType()).isEqualTo("Bearer");

            verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Should throw InvalidCredentialsException when password is wrong")
        void shouldThrowInvalidCredentialsException_WhenPasswordIsIncorrect() {
            // Arrange
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            // Act & Assert
            assertThrows(InvalidCredentialsException.class, () -> authService.login(validLoginRequest, "127.0.0.1", "test-agent"));
        }

        @Test
        @DisplayName("Should throw InvalidCredentialsException when user email not found")
        void shouldThrowInvalidCredentialsException_WhenEmailNotFound() {
            // Arrange
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(InvalidCredentialsException.class, () -> authService.login(validLoginRequest, "127.0.0.1", "test-agent"));
        }

        @Test
        @DisplayName("Should throw AccountLockedException when account is locked")
        void shouldThrowAccountLockedException_WhenAccountIsLocked() {
            // Arrange
            activeUser.setAccountNonLocked(false);
            activeUser.setAccountLockedUntil(LocalDateTime.now().plusMinutes(25));
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(activeUser));

            // Act & Assert
            assertThrows(AccountLockedException.class, () -> authService.login(validLoginRequest, "127.0.0.1", "test-agent"));
        }

        @Test
        @DisplayName("Should throw EmailNotVerifiedException when email is not verified")
        void shouldThrowEmailNotVerifiedException_WhenEmailNotVerified() {
            // Arrange
            activeUser.setEmailVerifiedAt(null);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(activeUser));

            // Act & Assert
            assertThrows(EmailNotVerifiedException.class, () -> authService.login(validLoginRequest, "127.0.0.1", "test-agent"));
        }

        @Test
        @DisplayName("Should increment failed login attempts when password is wrong")
        void shouldIncrementFailedLoginAttempts_WhenPasswordIsWrong() {
            // Arrange
            activeUser.setFailedLoginAttempts(2);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            // Act
            assertThrows(InvalidCredentialsException.class, () -> authService.login(validLoginRequest, "127.0.0.1", "test-agent"));

            // Assert
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getFailedLoginAttempts()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should lock account after 5 consecutive failed login attempts")
        void shouldLockAccount_WhenFailedAttemptsExceedThreshold() {
            // Arrange
            activeUser.setFailedLoginAttempts(4);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            // Act
            assertThrows(AccountLockedException.class, () -> authService.login(validLoginRequest, "127.0.0.1", "test-agent"));

            // Assert: account must now be locked
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().isAccountNonLocked()).isFalse();
            assertThat(userCaptor.getValue().getAccountLockedUntil()).isAfter(LocalDateTime.now());
        }

        @Test
        @DisplayName("Should reset failed login attempts on successful login")
        void shouldResetFailedAttempts_WhenLoginSuccessful() {
            // Arrange
            activeUser.setFailedLoginAttempts(3);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(jwtService.generateAccessToken(any())).thenReturn("token");
            when(jwtService.generateRefreshToken(any())).thenReturn("refresh");
            when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            // Act
            authService.login(validLoginRequest, "127.0.0.1", "test-agent");

            // Assert
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getFailedLoginAttempts()).isEqualTo(0);
        }
    }

    // =========================================================================
    // logout() Tests
    // =========================================================================

    @Nested
    @DisplayName("logout() Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should revoke single device refresh token on logout")
        void shouldRevokeRefreshToken_WhenLogoutCalled() {
            // Arrange
            String userId = activeUser.getId();
            String tokenHash = "sha256TokenHash";
            RefreshToken token = new RefreshToken();
            token.setTokenHash(tokenHash);
            token.setRevoked(false);

            when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(token));
            doNothing().when(refreshTokenRepository).delete(token);

            // Act
            authService.logout(userId, tokenHash, false);

            // Assert
            verify(refreshTokenRepository, times(1)).delete(token);
        }

        @Test
        @DisplayName("Should revoke all device tokens when logoutAllDevices is true")
        void shouldRevokeAllTokens_WhenLogoutAllDevicesIsTrue() {
            // Arrange
            when(refreshTokenRepository.findAllByUserId(activeUser.getId())).thenReturn(java.util.List.of());
            doNothing().when(refreshTokenRepository).deleteAllByUserId(activeUser.getId());

            // Act
            authService.logout(activeUser.getId(), null, true);

            // Assert
            verify(refreshTokenRepository, times(1)).deleteAllByUserId(activeUser.getId());
        }
    }

    // =========================================================================
    // verifyEmail() Tests
    // =========================================================================

    @Nested
    @DisplayName("verifyEmail() Tests")
    class VerifyEmailTests {

        @Test
        @DisplayName("Should verify email when token is valid and not expired")
        void shouldVerifyEmail_WhenTokenIsValidAndNotExpired() {
            // Arrange
            var verificationToken = new com.auth_service.entity.EmailVerificationToken();
            verificationToken.setUsed(false);
            verificationToken.setExpiresAt(LocalDateTime.now().plusHours(1));
            verificationToken.setUser(activeUser);
            activeUser.setEmailVerifiedAt(null);

            when(emailVerificationTokenRepository.findByTokenHash(anyString()))
                    .thenReturn(Optional.of(verificationToken));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            authService.verifyEmail(new com.auth_service.dto.request.VerifyEmailRequest("rawVerificationToken"));

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getEmailVerifiedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw InvalidTokenException when token does not exist")
        void shouldThrowInvalidTokenException_WhenTokenNotFound() {
            // Arrange
            when(emailVerificationTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(InvalidTokenException.class, () -> authService.verifyEmail(new com.auth_service.dto.request.VerifyEmailRequest("invalidToken")));
        }

        @Test
        @DisplayName("Should throw TokenAlreadyUsedException when token was already used")
        void shouldThrowTokenAlreadyUsedException_WhenTokenIsAlreadyUsed() {
            // Arrange
            var token = new com.auth_service.entity.EmailVerificationToken();
            token.setUsed(true);
            token.setExpiresAt(LocalDateTime.now().plusHours(1));

            when(emailVerificationTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

            // Act & Assert
            assertThrows(TokenAlreadyUsedException.class, () -> authService.verifyEmail(new com.auth_service.dto.request.VerifyEmailRequest("usedToken")));
        }

        @Test
        @DisplayName("Should throw InvalidTokenException when verification token is expired")
        void shouldThrowInvalidTokenException_WhenTokenIsExpired() {
            // Arrange
            var token = new com.auth_service.entity.EmailVerificationToken();
            token.setUsed(false);
            token.setExpiresAt(LocalDateTime.now().minusHours(1)); // expired

            when(emailVerificationTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

            // Act & Assert
            assertThrows(InvalidTokenException.class, () -> authService.verifyEmail(new com.auth_service.dto.request.VerifyEmailRequest("expiredToken")));
        }
    }

    // =========================================================================
    // forgotPassword() Tests
    // =========================================================================

    @Nested
    @DisplayName("forgotPassword() Tests")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Should send reset email when registered email exists")
        void shouldSendResetEmail_WhenEmailExists() {
            // Arrange
            var request = new ForgotPasswordRequest("john.doe@example.com");
            when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(activeUser));
            when(passwordResetTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // Act
            authService.forgotPassword(request);

            // Assert — token must be saved for valid email
            verify(passwordResetTokenRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("Should not throw exception when email does not exist (prevents enumeration)")
        void shouldNotRevealUserExistence_WhenEmailNotFound() {
            // Arrange
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // Act — must silently succeed (no exception)
            authService.forgotPassword(new ForgotPasswordRequest("unknown@example.com"));

            // Assert — no token should be created for non-existent user
            verify(passwordResetTokenRepository, never()).save(any());
        }
    }

    // =========================================================================
    // resetPassword() Tests
    // =========================================================================

    @Nested
    @DisplayName("resetPassword() Tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should reset password when token is valid")
        void shouldResetPassword_WhenTokenIsValid() {
            // Arrange
            var prt = new com.auth_service.entity.PasswordResetToken();
            prt.setUsed(false);
            prt.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            prt.setUser(activeUser);

            var request = new ResetPasswordRequest("rawToken", "NewSecureP@ss456", "NewSecureP@ss456");

            when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(prt));
            when(passwordEncoder.encode("NewSecureP@ss456")).thenReturn("$2a$12$newHashedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            authService.resetPassword(request);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("$2a$12$newHashedPassword");
        }

        @Test
        @DisplayName("Should mark reset token as used after password is changed")
        void shouldMarkTokenAsUsed_AfterPasswordReset() {
            // Arrange
            var prt = new com.auth_service.entity.PasswordResetToken();
            prt.setUsed(false);
            prt.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            prt.setUser(activeUser);

            var request = new ResetPasswordRequest("rawToken", "NewSecureP@ss456", "NewSecureP@ss456");

            when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(prt));
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashed");
            when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            ArgumentCaptor<com.auth_service.entity.PasswordResetToken> tokenCaptor =
                    ArgumentCaptor.forClass(com.auth_service.entity.PasswordResetToken.class);

            // Act
            authService.resetPassword(request);

            // Assert
            verify(passwordResetTokenRepository).save(tokenCaptor.capture());
            assertThat(tokenCaptor.getValue().isUsed()).isTrue();
        }

        @Test
        @DisplayName("Should throw InvalidTokenException when reset token is expired")
        void shouldThrowInvalidTokenException_WhenResetTokenIsExpired() {
            // Arrange
            var prt = new com.auth_service.entity.PasswordResetToken();
            prt.setUsed(false);
            prt.setExpiresAt(LocalDateTime.now().minusMinutes(5));

            when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(prt));

            var request = new ResetPasswordRequest("expiredToken", "NewSecureP@ss456", "NewSecureP@ss456");

            // Act & Assert
            assertThrows(InvalidTokenException.class, () -> authService.resetPassword(request));
            verify(userRepository, never()).save(any());
        }
    }
}
