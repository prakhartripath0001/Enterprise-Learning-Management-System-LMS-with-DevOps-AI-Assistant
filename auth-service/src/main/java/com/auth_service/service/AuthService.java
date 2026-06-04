package com.auth_service.service;

import com.auth_service.dto.request.ChangePasswordRequest;
import com.auth_service.dto.request.ForgotPasswordRequest;
import com.auth_service.dto.request.LoginRequest;
import com.auth_service.dto.request.RegisterRequest;
import com.auth_service.dto.request.ResetPasswordRequest;
import com.auth_service.dto.request.VerifyEmailRequest;
import com.auth_service.dto.response.AuthResponse;
import com.auth_service.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);

    void logout(String userId, String refreshTokenHash, boolean logoutAllDevices);

    AuthResponse refreshToken(String refreshTokenHash, String ipAddress, String userAgent);

    void verifyEmail(VerifyEmailRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(String userId, ChangePasswordRequest request);
}
