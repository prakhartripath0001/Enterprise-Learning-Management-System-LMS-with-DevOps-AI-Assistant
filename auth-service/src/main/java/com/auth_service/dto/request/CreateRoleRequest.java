package com.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateRoleRequest(
        @NotBlank(message = "Role name is required")
        String name,

        String description
) {}
