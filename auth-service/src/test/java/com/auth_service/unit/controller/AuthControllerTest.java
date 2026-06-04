package com.auth_service.unit.controller;

import com.auth_service.controller.AuthController;
import com.auth_service.dto.request.LoginRequest;
import com.auth_service.dto.request.RegisterRequest;
import com.auth_service.dto.request.VerifyEmailRequest;
import com.auth_service.dto.response.AuthResponse;
import com.auth_service.dto.response.RegisterResponse;
import com.auth_service.exception.GlobalExceptionHandler;
import com.auth_service.exception.InvalidCredentialsException;
import com.auth_service.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController MockMvc Tests")
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register — Success")
    void register_ShouldReturn201_WhenPayloadIsValid() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "John", "Doe", "john.doe@example.com", "johndoe",
                "SecureP@ss123", "SecureP@ss123"
        );
        RegisterResponse response = new RegisterResponse("user-id-123", "john.doe@example.com", "johndoe");

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.id").value("user-id-123"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register — Validation Failure")
    void register_ShouldReturn400_WhenEmailIsInvalid() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest(
                "John", "Doe", "invalid-email", "johndoe",
                "SecureP@ss123", "SecureP@ss123"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login — Success")
    void login_ShouldReturn200_WhenCredentialsAreValid() throws Exception {
        LoginRequest request = new LoginRequest("john.doe@example.com", "SecureP@ss123", "fingerprint", "WEB");
        AuthResponse response = new AuthResponse(
                "access-token-jwt", "refresh-token-value", "Bearer", 900L,
                new AuthResponse.UserSummary("user-id-123", "john.doe@example.com", "johndoe", "John", "Doe", Set.of("ROLE_STUDENT"), true)
        );

        when(authService.login(any(LoginRequest.class), anyString(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .remoteAddress("127.0.0.1")
                        .header("User-Agent", "Test-Agent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token-jwt"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token-value"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login — Bad Credentials")
    void login_ShouldReturn401_WhenCredentialsAreInvalid() throws Exception {
        LoginRequest request = new LoginRequest("john.doe@example.com", "wrongpass", "fingerprint", "WEB");

        when(authService.login(any(LoginRequest.class), anyString(), any()))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .remoteAddress("127.0.0.1")
                        .header("User-Agent", "Test-Agent"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Invalid credentials provided"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/verify-email — Success")
    void verifyEmail_ShouldReturn200_WhenTokenIsValid() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest("valid-verification-token");

        doNothing().when(authService).verifyEmail(any(VerifyEmailRequest.class));

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}
