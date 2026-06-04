package com.auth_service.dto.response;

import java.util.Set;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserSummary user
) {
    public record UserSummary(
            String id,
            String email,
            String username,
            String firstName,
            String lastName,
            Set<String> roles,
            boolean emailVerified
    ) {}
}
