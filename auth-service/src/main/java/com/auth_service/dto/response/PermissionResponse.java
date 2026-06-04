package com.auth_service.dto.response;

public record PermissionResponse(
        String id,
        String name,
        String description,
        String resource,
        String action
) {}
