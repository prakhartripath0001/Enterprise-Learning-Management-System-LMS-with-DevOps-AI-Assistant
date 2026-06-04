package com.auth_service.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public record UserProfileResponse(
        String id,
        String email,
        String username,
        String firstName,
        String lastName,
        Set<String> roles,
        Set<String> permissions,
        boolean emailVerified,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {}
