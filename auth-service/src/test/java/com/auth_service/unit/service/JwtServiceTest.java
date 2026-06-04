package com.auth_service.unit.service;

import com.auth_service.entity.Role;
import com.auth_service.entity.User;
import com.auth_service.service.impl.JwtServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JwtServiceImpl.
 *
 * Covers: generateAccessToken, generateRefreshToken, validateToken,
 *         extractUsername, expiry, signature validation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    @InjectMocks
    private JwtServiceImpl jwtService;

    private User testUser;

    // Test constants — match application.properties defaults
    private static final String TEST_SECRET = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY="; // 256-bit base64 key
    private static final long ACCESS_TOKEN_EXPIRY_MS  = 900_000L;    // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRY_MS = 604_800_000L; // 7 days

    @BeforeEach
    void setUp() {
        // Inject private configuration values via ReflectionTestUtils
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", ACCESS_TOKEN_EXPIRY_MS);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpirationMs", REFRESH_TOKEN_EXPIRY_MS);

        Role studentRole = new Role();
        studentRole.setName("ROLE_STUDENT");

        testUser = new User();
        testUser.setId(UUID.randomUUID().toString());
        testUser.setEmail("john.doe@example.com");
        testUser.setUsername("johndoe");
        testUser.setRoles(Set.of(studentRole));
    }

    // =========================================================================
    // generateAccessToken() Tests
    // =========================================================================

    @Nested
    @DisplayName("generateAccessToken() Tests")
    class GenerateAccessTokenTests {

        @Test
        @DisplayName("Should generate non-null access token when valid user provided")
        void shouldGenerateAccessToken_WhenValidUserProvided() {
            // Act
            String token = jwtService.generateAccessToken(testUser);

            // Assert
            assertThat(token).isNotBlank();
            assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
        }

        @Test
        @DisplayName("Should embed email as subject in access token claims")
        void shouldEmbedEmailAsSubject_InAccessToken() {
            // Act
            String token = jwtService.generateAccessToken(testUser);
            String extractedEmail = jwtService.extractEmail(token);

            // Assert
            assertThat(extractedEmail).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("Should embed user roles in access token claims")
        void shouldEmbedRoles_InAccessToken() {
            // Act
            String token = jwtService.generateAccessToken(testUser);
            Claims claims = jwtService.extractAllClaims(token);

            // Assert
            assertThat(claims.get("roles")).isNotNull();
        }

        @Test
        @DisplayName("Should embed user ID in access token claims")
        void shouldEmbedUserId_InAccessToken() {
            // Act
            String token = jwtService.generateAccessToken(testUser);
            Claims claims = jwtService.extractAllClaims(token);

            // Assert
            assertThat(claims.get("userId")).isEqualTo(testUser.getId());
        }
    }

    // =========================================================================
    // generateRefreshToken() Tests
    // =========================================================================

    @Nested
    @DisplayName("generateRefreshToken() Tests")
    class GenerateRefreshTokenTests {

        @Test
        @DisplayName("Should generate refresh token with longer expiry than access token")
        void shouldGenerateRefreshToken_WithLongerExpiry() {
            // Act
            String refreshToken = jwtService.generateRefreshToken(testUser);
            Claims claims = jwtService.extractAllClaims(refreshToken);

            long expiryMs = claims.getExpiration().getTime() - System.currentTimeMillis();

            // Assert — should be approximately 7 days
            assertThat(expiryMs).isGreaterThan(ACCESS_TOKEN_EXPIRY_MS);
        }

        @Test
        @DisplayName("Should produce different token each call for same user")
        void shouldProduceDifferentToken_OnEachCall() {
            // Act
            String token1 = jwtService.generateAccessToken(testUser);
            String token2 = jwtService.generateAccessToken(testUser);

            // Assert
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    // =========================================================================
    // validateToken() Tests
    // =========================================================================

    @Nested
    @DisplayName("validateToken() Tests")
    class ValidateTokenTests {

        @Test
        @DisplayName("Should return true for a valid and non-expired token")
        void shouldReturnTrue_WhenTokenIsValidAndNotExpired() {
            // Arrange
            String token = jwtService.generateAccessToken(testUser);

            // Act
            boolean isValid = jwtService.validateToken(token, testUser);

            // Assert
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should return false when token subject does not match user email")
        void shouldReturnFalse_WhenTokenSubjectMismatch() {
            // Arrange
            User differentUser = new User();
            differentUser.setEmail("other.user@example.com");
            String token = jwtService.generateAccessToken(testUser);

            // Act
            boolean isValid = jwtService.validateToken(token, differentUser);

            // Assert
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should throw ExpiredJwtException when token is expired")
        void shouldThrowExpiredJwtException_WhenTokenIsExpired() {
            // Arrange — set negative expiry so token is already expired
            ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", -1000L);
            String expiredToken = jwtService.generateAccessToken(testUser);
            // Restore normal expiry
            ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", ACCESS_TOKEN_EXPIRY_MS);

            // Act & Assert
            assertThatThrownBy(() -> jwtService.validateToken(expiredToken, testUser))
                    .isInstanceOf(ExpiredJwtException.class);
        }

        @Test
        @DisplayName("Should throw SignatureException when token has invalid signature")
        void shouldThrowSignatureException_WhenTokenSignatureIsInvalid() {
            // Arrange — tamper with the signature segment
            String validToken = jwtService.generateAccessToken(testUser);
            String[] parts = validToken.split("\\.");
            String tamperedToken = parts[0] + "." + parts[1] + ".invalidsignature";

            // Act & Assert
            assertThatThrownBy(() -> jwtService.validateToken(tamperedToken, testUser))
                    .isInstanceOf(SignatureException.class);
        }

        @Test
        @DisplayName("Should throw MalformedJwtException when token string is malformed")
        void shouldThrowMalformedJwtException_WhenTokenIsMalformed() {
            // Arrange
            String malformedToken = "this.is.not.a.jwt";

            // Act & Assert
            assertThatThrownBy(() -> jwtService.validateToken(malformedToken, testUser))
                    .isInstanceOf(MalformedJwtException.class);
        }
    }

    // =========================================================================
    // extractEmail() Tests
    // =========================================================================

    @Nested
    @DisplayName("extractEmail() Tests")
    class ExtractEmailTests {

        @Test
        @DisplayName("Should extract correct email from valid token")
        void shouldExtractEmail_WhenTokenIsValid() {
            // Arrange
            String token = jwtService.generateAccessToken(testUser);

            // Act
            String email = jwtService.extractEmail(token);

            // Assert
            assertThat(email).isEqualTo("john.doe@example.com");
        }
    }
}
