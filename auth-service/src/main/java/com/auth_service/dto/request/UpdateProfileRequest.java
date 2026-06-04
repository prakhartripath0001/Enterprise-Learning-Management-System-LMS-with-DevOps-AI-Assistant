package com.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 100)
        @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "First name must contain only letters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 100)
        @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Last name must contain only letters")
        String lastName,

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username may only contain letters, digits, and underscores")
        String username
) {}
