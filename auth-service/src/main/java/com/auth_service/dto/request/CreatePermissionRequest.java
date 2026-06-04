package com.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreatePermissionRequest(
        @NotBlank(message = "Permission name is required")
        String name,

        String description,

        @NotBlank(message = "Resource is required")
        String resource,

        @NotBlank(message = "Action is required")
        String action
) {}
