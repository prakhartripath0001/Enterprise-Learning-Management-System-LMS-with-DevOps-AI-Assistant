package com.auth_service.dto.response;

public record RoleResponse(
        String id,
        String name,
        String description,
        boolean systemRole
) {}
