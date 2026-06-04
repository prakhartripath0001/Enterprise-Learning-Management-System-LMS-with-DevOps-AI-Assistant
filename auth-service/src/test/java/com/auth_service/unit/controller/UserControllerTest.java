package com.auth_service.unit.controller;

import com.auth_service.controller.UserController;
import com.auth_service.dto.request.UpdateProfileRequest;
import com.auth_service.dto.response.UserProfileResponse;
import com.auth_service.exception.GlobalExceptionHandler;
import com.auth_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController MockMvc Tests")
class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UsernamePasswordAuthenticationToken principal;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        UserDetails userDetails = new User("john.doe@example.com", "password", Collections.emptyList());
        principal = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
    @DisplayName("GET /api/v1/users/me — Success")
    void getCurrentUser_ShouldReturn200_WhenAuthenticated() throws Exception {
        UserProfileResponse response = new UserProfileResponse("user-id-123", "john.doe@example.com", "johndoe", "John", "Doe", Set.of("ROLE_STUDENT"), true);

        when(userService.getCurrentUser(anyString())).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/me")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("John"));
    }

    @Test
    @DisplayName("PUT /api/v1/users/me — Success")
    void updateProfile_ShouldReturn200_WhenPayloadIsValid() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest("Jonathan", "Doe", "johndoe");
        UserProfileResponse response = new UserProfileResponse("user-id-123", "john.doe@example.com", "johndoe", "Jonathan", "Doe", Set.of("ROLE_STUDENT"), true);

        when(userService.updateProfile(anyString(), any(UpdateProfileRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/users/me")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.firstName").value("Jonathan"));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/me — Success")
    void deactivateAccount_ShouldReturn200_WhenPasswordMatches() throws Exception {
        doNothing().when(userService).deactivateAccount(anyString(), anyString());

        mockMvc.perform(delete("/api/v1/users/me")
                        .principal(principal)
                        .param("password", "SecureP@ss123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Account deactivated successfully."));
    }
}
