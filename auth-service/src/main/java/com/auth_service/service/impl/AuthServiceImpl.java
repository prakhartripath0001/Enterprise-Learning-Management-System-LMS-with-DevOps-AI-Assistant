package com.auth_service.service.impl;

import com.auth_service.dto.request.ChangePasswordRequest;
import com.auth_service.dto.request.ForgotPasswordRequest;
import com.auth_service.dto.request.LoginRequest;
import com.auth_service.dto.request.RegisterRequest;
import com.auth_service.dto.request.ResetPasswordRequest;
import com.auth_service.dto.request.VerifyEmailRequest;
import com.auth_service.dto.response.AuthResponse;
import com.auth_service.dto.response.RegisterResponse;
import com.auth_service.entity.EmailVerificationToken;
import com.auth_service.entity.LoginAuditLog;
import com.auth_service.entity.PasswordResetToken;
import com.auth_service.entity.RefreshToken;
import com.auth_service.entity.Role;
import com.auth_service.entity.User;
import com.auth_service.exception.AccountLockedException;
import com.auth_service.exception.EmailAlreadyExistsException;
import com.auth_service.exception.EmailNotVerifiedException;
import com.auth_service.exception.InvalidCredentialsException;
import com.auth_service.exception.InvalidTokenException;
import com.auth_service.exception.PasswordMismatchException;
import com.auth_service.exception.TokenAlreadyUsedException;
import com.auth_service.exception.UsernameAlreadyExistsException;
import com.auth_service.exception.UserNotFoundException;
import com.auth_service.repository.EmailVerificationTokenRepository;
import com.auth_service.repository.LoginAuditLogRepository;
import com.auth_service.repository.PasswordResetTokenRepository;
import com.auth_service.repository.RefreshTokenRepository;
import com.auth_service.repository.RoleRepository;
import com.auth_service.repository.UserRepository;
import com.auth_service.service.AuthService;
import com.auth_service.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final String ROLE_STUDENT = "ROLE_STUDENT";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final LoginAuditLogRepository loginAuditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Value("${email.verification.expiry-hours}")
    private long emailVerificationExpiryHours;

    @Value("${password.reset.expiry-minutes}")
    private long passwordResetExpiryMinutes;

    public AuthServiceImpl(
            final UserRepository userRepository,
            final RoleRepository roleRepository,
            final RefreshTokenRepository refreshTokenRepository,
            final PasswordResetTokenRepository passwordResetTokenRepository,
            final EmailVerificationTokenRepository emailVerificationTokenRepository,
            final LoginAuditLogRepository loginAuditLogRepository,
            final PasswordEncoder passwordEncoder,
            final JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.loginAuditLogRepository = loginAuditLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }
        if (!request.password().equals(request.confirmPassword())) {
            throw new PasswordMismatchException();
        }

        Role studentRole = roleRepository.findByName(ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("Default role not configured"));

        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRoles(Set.of(studentRole));
        user.setCreatedBy("SYSTEM");

        User saved = userRepository.save(user);
        createAndSaveVerificationToken(saved);

        return new RegisterResponse(saved.getId(), saved.getEmail(), saved.getUsername());
    }

    @Override
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElse(null);

        if (user == null) {
            saveAuditLog(null, request.email(), "LOGIN_FAILED", false, ipAddress, userAgent, "USER_NOT_FOUND");
            throw new InvalidCredentialsException();
        }

        if (!user.isAccountNonLocked()) {
            if (user.getAccountLockedUntil() != null && LocalDateTime.now().isBefore(user.getAccountLockedUntil())) {
                saveAuditLog(user.getId(), user.getEmail(), "LOGIN_FAILED", false, ipAddress, userAgent, "ACCOUNT_LOCKED");
                throw new AccountLockedException("Account is locked until " + user.getAccountLockedUntil());
            }
            user.setAccountNonLocked(true);
            user.setFailedLoginAttempts(0);
        }

        if (user.getEmailVerifiedAt() == null) {
            throw new EmailNotVerifiedException();
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleFailedLoginAttempt(user, ipAddress, userAgent);
            throw new InvalidCredentialsException();
        }

        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String rawRefreshToken = generateSecureToken();
        String tokenHash = hashToken(rawRefreshToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setDeviceFingerprint(request.deviceFingerprint());
        refreshToken.setDeviceType(request.deviceType());
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setUserAgent(userAgent);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000));
        refreshTokenRepository.save(refreshToken);

        saveAuditLog(user.getId(), user.getEmail(), "LOGIN_SUCCESS", true, ipAddress, userAgent, null);

        String accessToken = jwtService.generateAccessToken(user);
        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

        return new AuthResponse(
                accessToken,
                rawRefreshToken,
                "Bearer",
                900L,
                new AuthResponse.UserSummary(
                        user.getId(), user.getEmail(), user.getUsername(),
                        user.getFirstName(), user.getLastName(), roles,
                        user.getEmailVerifiedAt() != null)
        );
    }

    @Override
    public void logout(String userId, String refreshTokenHash, boolean logoutAllDevices) {
        if (logoutAllDevices) {
            refreshTokenRepository.deleteAllByUserId(userId);
        } else if (refreshTokenHash != null) {
            refreshTokenRepository.findByTokenHash(refreshTokenHash)
                    .ifPresent(refreshTokenRepository::delete);
        }
    }

    @Override
    public AuthResponse refreshToken(String rawRefreshToken, String ipAddress, String userAgent) {
        String tokenHash = hashToken(rawRefreshToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid or does not exist"));

        if (existing.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(existing);
            throw new InvalidTokenException("Refresh token has expired");
        }

        User user = existing.getUser();
        refreshTokenRepository.delete(existing);

        String newRawToken = generateSecureToken();
        String newHash = hashToken(newRawToken);

        RefreshToken newToken = new RefreshToken();
        newToken.setUser(user);
        newToken.setTokenHash(newHash);
        newToken.setIpAddress(ipAddress);
        newToken.setUserAgent(userAgent);
        newToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000));
        refreshTokenRepository.save(newToken);

        String accessToken = jwtService.generateAccessToken(user);
        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

        return new AuthResponse(accessToken, newRawToken, "Bearer", 900L,
                new AuthResponse.UserSummary(
                        user.getId(), user.getEmail(), user.getUsername(),
                        user.getFirstName(), user.getLastName(), roles,
                        user.getEmailVerifiedAt() != null));
    }

    @Override
    public void verifyEmail(VerifyEmailRequest request) {
        String tokenHash = hashToken(request.token());
        EmailVerificationToken token = emailVerificationTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Verification token is invalid"));

        if (token.isUsed()) {
            throw new TokenAlreadyUsedException();
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Verification token has expired");
        }

        User user = token.getUser();
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        token.setUsed(true);
        emailVerificationTokenRepository.save(token);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email().toLowerCase()).ifPresent(user -> {
            String rawToken = generateSecureToken();
            PasswordResetToken prt = new PasswordResetToken();
            prt.setUser(user);
            prt.setTokenHash(hashToken(rawToken));
            prt.setExpiresAt(LocalDateTime.now().plusMinutes(passwordResetExpiryMinutes));
            passwordResetTokenRepository.save(prt);
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new PasswordMismatchException();
        }

        String tokenHash = hashToken(request.token());
        PasswordResetToken prt = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Password reset token is invalid"));

        if (prt.isUsed()) {
            throw new TokenAlreadyUsedException();
        }
        if (prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Password reset token has expired");
        }

        User user = prt.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setCredentialsNonExpired(true);
        userRepository.save(user);

        prt.setUsed(true);
        passwordResetTokenRepository.save(prt);

        refreshTokenRepository.deleteAllByUserId(user.getId());
    }

    @Override
    public void changePassword(String userId, ChangePasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new PasswordMismatchException();
        }

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    private void handleFailedLoginAttempt(User user, String ipAddress, String userAgent) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountNonLocked(false);
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            saveAuditLog(user.getId(), user.getEmail(), "ACCOUNT_LOCKED", false, ipAddress, userAgent, "MAX_ATTEMPTS_EXCEEDED");
        } else {
            saveAuditLog(user.getId(), user.getEmail(), "LOGIN_FAILED", false, ipAddress, userAgent, "INVALID_CREDENTIALS");
        }
        userRepository.save(user);
    }

    private void createAndSaveVerificationToken(User user) {
        String rawToken = generateSecureToken();
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setTokenHash(hashToken(rawToken));
        token.setExpiresAt(LocalDateTime.now().plusHours(emailVerificationExpiryHours));
        emailVerificationTokenRepository.save(token);
    }

    private void saveAuditLog(String userId, String email, String eventType,
                               boolean success, String ip, String ua, String failureReason) {
        LoginAuditLog log = new LoginAuditLog();
        log.setUserId(userId);
        log.setEmailAttempted(email);
        log.setEventType(eventType);
        log.setSuccess(success);
        log.setIpAddress(ip);
        log.setUserAgent(ua);
        log.setFailureReason(failureReason);
        loginAuditLogRepository.save(log);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[48];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
