package com.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "Verification token is required")
        String token
) {}
