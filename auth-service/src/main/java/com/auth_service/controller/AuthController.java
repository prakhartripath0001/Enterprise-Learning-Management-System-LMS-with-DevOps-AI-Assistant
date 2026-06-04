package com.auth_service.controller;

import com.auth_service.dto.request.ChangePasswordRequest;
import com.auth_service.dto.request.ForgotPasswordRequest;
import com.auth_service.dto.request.LoginRequest;
import com.auth_service.dto.request.RegisterRequest;
import com.auth_service.dto.request.ResetPasswordRequest;
import com.auth_service.dto.request.VerifyEmailRequest;
import com.auth_service.dto.response.ApiResponse;
import com.auth_service.dto.response.AuthResponse;
import com.auth_service.dto.response.RegisterResponse;
import com.auth_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Register, login, logout, token refresh, and password management")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final int REFRESH_TOKEN_COOKIE_MAX_AGE = 7 * 24 * 60 * 60;

    private final AuthService authService;

    public AuthController(final AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful. Please verify your email.", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        AuthResponse authResponse = authService.login(
                request,
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent"));

        Cookie cookie = buildRefreshTokenCookie(REFRESH_TOKEN_COOKIE, "");
        httpResponse.addCookie(cookie);

        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            HttpServletResponse response) {

        String userId = resolveUserId(userDetails);
        String tokenHash = extractRefreshTokenHash(request);
        authService.logout(userId, tokenHash, false);

        Cookie expiredCookie = buildRefreshTokenCookie(REFRESH_TOKEN_COOKIE, "");
        expiredCookie.setMaxAge(0);
        response.addCookie(expiredCookie);

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token using refresh token cookie")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        String rawToken = extractRawRefreshToken(request);
        AuthResponse authResponse = authService.refreshToken(
                rawToken,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        Cookie newCookie = buildRefreshTokenCookie(REFRESH_TOKEN_COOKIE, "");
        response.addCookie(newCookie);

        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authResponse));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email address with one-time token")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully. You can now login."));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                "If an account with that email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using one-time reset token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully. Please login."));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password for authenticated user")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(resolveUserId(userDetails), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully."));
    }

    private String resolveUserId(UserDetails userDetails) {
        return userDetails.getUsername();
    }

    private String extractRawRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new com.auth_service.exception.InvalidTokenException("Refresh token cookie is missing");
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new com.auth_service.exception.InvalidTokenException("Refresh token cookie is missing"));
    }

    private String extractRefreshTokenHash(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .map(token -> {
                    try {
                        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                        byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
                    } catch (java.security.NoSuchAlgorithmException e) {
                        return null;
                    }
                })
                .orElse(null);
    }

    private Cookie buildRefreshTokenCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(REFRESH_TOKEN_COOKIE_MAX_AGE);
        return cookie;
    }
}
