package com.auth_service.service;

import com.auth_service.entity.User;
import io.jsonwebtoken.Claims;

public interface JwtService {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    boolean validateToken(String token, User user);

    String extractEmail(String token);

    Claims extractAllClaims(String token);

    boolean isTokenExpired(String token);
}
