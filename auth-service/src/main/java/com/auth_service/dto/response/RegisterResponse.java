package com.auth_service.dto.response;

public record RegisterResponse(
        String userId,
        String email,
        String username
) {}
