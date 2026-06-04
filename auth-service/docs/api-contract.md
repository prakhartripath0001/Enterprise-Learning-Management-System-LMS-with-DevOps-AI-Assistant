# Auth Service — Production REST API Contract

> **Principal Backend Engineer Review**
> Enterprise LMS — Authentication Service API v1
> Stack: Java 21 · Spring Boot 3 · Spring Security 6 · JWT · MySQL 8

---

## Standard Response Envelope

All responses from the Auth Service return one of two standard envelopes:

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {},
  "timestamp": "2026-06-05T02:00:00.000Z"
}
```

### Paginated Success Response
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": {
    "content": [],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 150,
    "totalPages": 8,
    "last": false
  },
  "timestamp": "2026-06-05T02:00:00.000Z"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "code": "INVALID_EMAIL",
      "message": "Must be a valid email address"
    }
  ],
  "timestamp": "2026-06-05T02:00:00.000Z"
}
```

---

## HTTP Status Code Standards

| Code | Scenario |
|---|---|
| `200 OK` | Successful read/update/action |
| `201 Created` | Successful resource creation |
| `204 No Content` | Successful delete with no body |
| `400 Bad Request` | Validation failure / malformed payload |
| `401 Unauthorized` | Missing, expired, or invalid JWT |
| `403 Forbidden` | Authenticated but lacking required role/permission |
| `404 Not Found` | Resource does not exist |
| `409 Conflict` | Duplicate resource (e.g., email already exists) |
| `422 Unprocessable Entity` | Business rule violation (e.g., account not verified) |
| `429 Too Many Requests` | Rate limit exceeded |
| `500 Internal Server Error` | Unexpected server-side error |

---

## Authentication & Security Rules

- **Access Token TTL**: 15 minutes (Bearer JWT in `Authorization` header)
- **Refresh Token TTL**: 7 days (stored in `HttpOnly; Secure; SameSite=Strict` cookie)
- **Token Rotation**: A new refresh token is issued on every `/refresh-token` call; old token is immediately revoked.
- **Account Lockout**: After 5 consecutive failed login attempts, account is locked for 30 minutes.
- **Email Verification**: Login is blocked until email is verified.
- **Password Policy**: Min 8 chars, at least 1 uppercase, 1 lowercase, 1 digit, 1 special character.

---

## API Versioning Strategy

All endpoints are prefixed with `/api/v1`. When breaking changes are introduced, a new version path `/api/v2` is added. The gateway routes traffic to the appropriate version. Old versions are deprecated with a sunset header 6 months before removal.

---

# AUTHENTICATION APIS

---

## 1. POST /api/v1/auth/register

**Description**: Register a new user account. Sends an email verification token after registration.

**Authentication**: None required  
**Rate Limit**: 5 requests per IP per hour  

### Request Headers
```
Content-Type: application/json
```

### Request Body
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "username": "johndoe",
  "password": "SecureP@ss123",
  "confirmPassword": "SecureP@ss123"
}
```

### Validation Rules
| Field | Rules |
|---|---|
| `firstName` | Required, 2–100 chars, alpha only |
| `lastName` | Required, 2–100 chars, alpha only |
| `email` | Required, valid email format, unique |
| `username` | Required, 3–50 chars, alphanumeric/underscore, unique |
| `password` | Required, 8–128 chars, uppercase, lowercase, digit, special char |
| `confirmPassword` | Required, must match `password` |

### Success Response — `201 Created`
```json
{
  "success": true,
  "message": "Registration successful. Please check your email to verify your account.",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john.doe@example.com",
    "username": "johndoe"
  },
  "timestamp": "2026-06-05T02:00:00.000Z"
}
```

### Error Responses
| Status | Code | Scenario |
|---|---|---|
| `400` | `VALIDATION_ERROR` | Missing or invalid fields |
| `409` | `EMAIL_ALREADY_EXISTS` | Email already registered |
| `409` | `USERNAME_ALREADY_EXISTS` | Username already taken |

---

## 2. POST /api/v1/auth/login

**Description**: Authenticate user and return access + refresh tokens.

**Authentication**: None required  
**Rate Limit**: 10 attempts per IP per 15 minutes  

### Request Body
```json
{
  "email": "john.doe@example.com",
  "password": "SecureP@ss123",
  "deviceFingerprint": "a1b2c3d4e5f6",
  "deviceType": "WEB"
}
```

### Validation Rules
| Field | Rules |
|---|---|
| `email` | Required, valid email |
| `password` | Required, non-blank |
| `deviceType` | Optional, enum: `WEB`, `MOBILE`, `DESKTOP`, `API` |

### Success Response — `200 OK`
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "john.doe@example.com",
      "username": "johndoe",
      "firstName": "John",
      "lastName": "Doe",
      "roles": ["ROLE_STUDENT"],
      "emailVerified": true
    }
  },
  "timestamp": "2026-06-05T02:00:00.000Z"
}
```
> **Note**: Refresh token is returned as an `HttpOnly; Secure; SameSite=Strict` cookie — NOT in the response body.

### Error Responses
| Status | Code | Scenario |
|---|---|---|
| `400` | `VALIDATION_ERROR` | Missing fields |
| `401` | `INVALID_CREDENTIALS` | Wrong email or password |
| `403` | `EMAIL_NOT_VERIFIED` | Account not yet verified |
| `403` | `ACCOUNT_DISABLED` | Account deactivated by admin |
| `423` | `ACCOUNT_LOCKED` | Locked after too many failures |

---

## 3. POST /api/v1/auth/logout

**Description**: Revoke the current device's refresh token and clear the session cookie.

**Authentication**: Bearer token required  
**Rate Limit**: 20 per minute per user  

### Request Headers
```
Authorization: Bearer <accessToken>
```

### Request Body
```json
{
  "logoutAllDevices": false
}
```

### Success Response — `200 OK`
```json
{
  "success": true,
  "message": "Logged out successfully",
  "data": null,
  "timestamp": "2026-06-05T02:00:00.000Z"
}
```

---

## 4. POST /api/v1/auth/refresh-token

**Description**: Exchange a valid refresh token for a new access token. Old refresh token is immediately rotated.

**Authentication**: Refresh token cookie required (HttpOnly)  
**Rate Limit**: 30 per hour per user  

### Success Response — `200 OK`
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 900
  },
  "timestamp": "2026-06-05T02:00:00.000Z"
}
```

### Error Responses
| Status | Code | Scenario |
|---|---|---|
| `401` | `REFRESH_TOKEN_MISSING` | No cookie present |
| `401` | `REFRESH_TOKEN_INVALID` | Token tampered or unknown hash |
| `401` | `REFRESH_TOKEN_EXPIRED` | Token TTL elapsed |
| `401` | `REFRESH_TOKEN_REVOKED` | Token already rotated/revoked |

---

## 5. POST /api/v1/auth/verify-email

**Description**: Confirm user's email address using the verification token sent via email.

**Authentication**: None required  

### Request Body
```json
{
  "token": "raw-verification-token-from-email-link"
}
```

### Success Response — `200 OK`
```json
{
  "success": true,
  "message": "Email verified successfully. You can now login.",
  "data": null,
  "timestamp": "2026-06-05T02:00:00.000Z"
}
```

### Error Responses
| Status | Code | Scenario |
|---|---|---|
| `400` | `TOKEN_INVALID` | Token not found or hash mismatch |
| `400` | `TOKEN_ALREADY_USED` | Token consumed previously |
| `400` | `TOKEN_EXPIRED` | Verification link expired |
| `409` | `EMAIL_ALREADY_VERIFIED` | Account already confirmed |

---

## 6. POST /api/v1/auth/resend-verification

**Description**: Resend email verification link to the registered email.

**Authentication**: None required  
**Rate Limit**: 3 per hour per email  

### Request Body
```json
{
  "email": "john.doe@example.com"
}
```

### Success Response — `200 OK`
```json
{
  "success": true,
  "message": "If an unverified account exists for this email, a new verification link has been sent.",
  "data": null,
  "timestamp": "2026-06-05T02:00:00.000Z"
}
```
> **Security Note**: Always return `200 OK` even if email does not exist to prevent email enumeration.

---

## 7. POST /api/v1/auth/forgot-password

**Description**: Initiate password reset flow. Sends a one-time token to the registered email.

**Authentication**: None required  
**Rate Limit**: 3 per hour per email  

### Request Body
```json
{
  "email": "john.doe@example.com"
}
```

### Success Response — `200 OK`
```json
{
  "success": true,
  "message": "If an account exists for this email, a password reset link has been sent.",
  "data": null,
  "timestamp": "2026-06-05T02:00:00.000Z"
}
```

---

## 8. POST /api/v1/auth/reset-password

**Description**: Complete password reset using the one-time token from email.

**Authentication**: None required  

### Request Body
```json
{
  "token": "raw-reset-token-from-email",
  "newPassword": "NewSecureP@ss456",
  "confirmPassword": "NewSecureP@ss456"
}
```

### Success Response — `200 OK`
```json
{
  "success": true,
  "message": "Password has been reset successfully. Please login with your new password.",
  "data": null,
  "timestamp": "2026-06-05T02:00:00.000Z"
}
```

---

## 9. POST /api/v1/auth/change-password

**Description**: Change password for an authenticated user (requires current password).

**Authentication**: Bearer token required  

### Request Body
```json
{
  "currentPassword": "OldSecureP@ss123",
  "newPassword": "NewSecureP@ss456",
  "confirmPassword": "NewSecureP@ss456"
}
```

### Success Response — `200 OK`
```json
{
  "success": true,
  "message": "Password changed successfully. Please login again.",
  "data": null,
  "timestamp": "2026-06-05T02:00:00.000Z"
}
```

---

# USER APIS

---

## 10. GET /api/v1/users/me

**Description**: Return the current authenticated user's profile.

**Authentication**: Bearer token required  
**Required Role**: Any authenticated user  

### Request Headers
```
Authorization: Bearer <accessToken>
```

### Success Response — `200 OK`
```json
{
  "success": true,
  "message": "User profile retrieved",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john.doe@example.com",
    "username": "johndoe",
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["ROLE_STUDENT"],
    "permissions": ["course:read", "enrollment:write"],
    "emailVerified": true,
    "createdAt": "2026-01-15T10:00:00.000Z",
    "lastLoginAt": "2026-06-05T01:50:00.000Z"
  },
  "timestamp": "2026-06-05T02:00:00.000Z"
}
```

---

## 11. PUT /api/v1/users/me

**Description**: Update first name, last name, or username of the current user.

**Authentication**: Bearer token required  

### Request Body
```json
{
  "firstName": "John",
  "lastName": "Smith",
  "username": "johnsmith"
}
```

### Success Response — `200 OK`
```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "johnsmith",
    "firstName": "John",
    "lastName": "Smith"
  },
  "timestamp": "2026-06-05T02:00:00.000Z"
}
```

---

## 12. DELETE /api/v1/users/me

**Description**: Soft-delete the current user's account. All active sessions are revoked.

**Authentication**: Bearer token required  

### Request Body
```json
{
  "password": "SecureP@ss123",
  "reason": "No longer using the platform"
}
```

### Success Response — `200 OK`
```json
{
  "success": true,
  "message": "Account has been deactivated successfully.",
  "data": null,
  "timestamp": "2026-06-05T02:00:00.000Z"
}
```

---

# ADMIN APIS

---

## 13. GET /api/v1/admin/users

**Description**: Paginated list of all users. Supports filtering and sorting.

**Authentication**: Bearer token required  
**Required Role**: `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`  

### Query Parameters
| Param | Type | Description |
|---|---|---|
| `page` | int | Page number (default: 0) |
| `size` | int | Page size (default: 20, max: 100) |
| `sort` | string | e.g., `createdAt,desc` |
| `search` | string | Search by email or username |
| `role` | string | Filter by role name |
| `enabled` | boolean | Filter by account status |

### Success Response — `200 OK` (Paginated Envelope)

---

## 14. GET /api/v1/admin/users/{id}

**Description**: Retrieve a specific user's full profile by UUID.

**Required Role**: `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`  

### Path Parameters
| Param | Description |
|---|---|
| `id` | User UUID (CHAR 36) |

### Success Response — `200 OK`
Returns full user object including roles, permissions, and account state.

---

## 15. PATCH /api/v1/admin/users/{id}/status

**Description**: Enable or disable a user account.

**Required Role**: `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`  

### Request Body
```json
{
  "enabled": false,
  "reason": "Suspended for policy violation"
}
```

### Success Response — `200 OK`

---

## 16. PATCH /api/v1/admin/users/{id}/lock

**Description**: Manually lock a user's account.

**Required Role**: `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`  

### Request Body
```json
{
  "reason": "Suspicious login activity detected",
  "lockDurationMinutes": 1440
}
```

---

## 17. PATCH /api/v1/admin/users/{id}/unlock

**Description**: Unlock a user account and reset failed login attempts.

**Required Role**: `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`  

### Request Body
```json
{
  "reason": "Manually verified identity"
}
```

---

## 18. POST /api/v1/admin/users/{id}/roles

**Description**: Assign a role to a user. Optionally define expiry for temporal roles.

**Required Role**: `ROLE_SUPER_ADMIN`  

### Request Body
```json
{
  "roleId": "role-uuid-here",
  "expiresAt": "2027-01-01T00:00:00.000Z"
}
```

---

## 19. DELETE /api/v1/admin/users/{id}/roles/{roleId}

**Description**: Remove a role from a user.

**Required Role**: `ROLE_SUPER_ADMIN`  

### Success Response — `200 OK`

---

# ROLE APIS

---

## 20. POST /api/v1/admin/roles

**Description**: Create a new custom role.

**Required Role**: `ROLE_SUPER_ADMIN`  

### Request Body
```json
{
  "name": "ROLE_CONTENT_MODERATOR",
  "description": "Can review and moderate user-generated content"
}
```

### Success Response — `201 Created`

---

## 21. GET /api/v1/admin/roles

**Description**: List all roles with their assigned permissions.

**Required Role**: `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`  

---

## 22. PUT /api/v1/admin/roles/{id}

**Description**: Update a non-system role's name and description.

**Required Role**: `ROLE_SUPER_ADMIN`  

> **Business Rule**: System roles (`is_system_role = true`) cannot be renamed.

---

## 23. DELETE /api/v1/admin/roles/{id}

**Description**: Delete a custom role. Removes all `user_roles` and `role_permissions` entries.

**Required Role**: `ROLE_SUPER_ADMIN`  

> **Business Rule**: System roles cannot be deleted.

### Success Response — `200 OK`

---

# PERMISSION APIS

---

## 24. POST /api/v1/admin/permissions

**Description**: Create a new fine-grained permission.

**Required Role**: `ROLE_SUPER_ADMIN`  

### Request Body
```json
{
  "name": "course:publish",
  "description": "Ability to publish a course to the catalog",
  "resource": "course",
  "action": "publish"
}
```

---

## 25. GET /api/v1/admin/permissions

**Description**: List all permissions, grouped by resource.

**Required Role**: `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`  

---

## 26. POST /api/v1/admin/roles/{roleId}/permissions

**Description**: Assign an existing permission to a role.

**Required Role**: `ROLE_SUPER_ADMIN`  

### Request Body
```json
{
  "permissionId": "permission-uuid-here"
}
```

---

## 27. DELETE /api/v1/admin/roles/{roleId}/permissions/{permissionId}

**Description**: Remove a permission from a role.

**Required Role**: `ROLE_SUPER_ADMIN`  

---

# AUDIT APIS

---

## 28. GET /api/v1/admin/audit/login-logs

**Description**: Retrieve paginated, filterable security audit logs.

**Authentication**: Bearer token required  
**Required Role**: `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`  

### Query Parameters
| Param | Type | Description |
|---|---|---|
| `userId` | UUID | Filter by user |
| `eventType` | string | e.g., `LOGIN_FAILED` |
| `ipAddress` | string | Source IP filter |
| `from` | datetime | Start of range (ISO 8601) |
| `to` | datetime | End of range (ISO 8601) |
| `isSuccess` | boolean | Outcome filter |
| `page` | int | Page number |
| `size` | int | Page size (max: 200) |

### Success Response — `200 OK` (Paginated Envelope)

---

## Microservice Communication Considerations

When other services (e.g., `course-service`, `enrollment-service`) need to verify a user's identity or roles, they should **not** call the `auth-service` database directly. Instead:

1. **JWT Claim Extraction** (preferred for hot path): Each downstream service validates the JWT signature using the shared public key and extracts `userId`, `email`, and `roles` from JWT claims — no network call required.
2. **Internal User Lookup API** (for enriched data): Auth service exposes an internal endpoint protected by a service-to-service token: `GET /api/internal/v1/users/{id}` returning the user's roles and account status.

---

## Production-Ready Recommendations

1. **API Gateway**: Route all traffic through a gateway (Spring Cloud Gateway / Kong) — the auth service is never directly exposed.
2. **Redis Revocation Cache**: Maintain a revoked token list in Redis so downstream services can check token validity without hitting the database.
3. **Asymmetric JWT Signing**: Use RSA-256 or ECDSA. The auth service signs with its private key; all other services validate with the public key.
4. **Correlation IDs**: Inject `X-Correlation-ID` headers at the gateway for distributed tracing.
5. **Structured Logging**: Every request/response cycle must emit a structured log entry containing `correlationId`, `userId`, `ipAddress`, `endpoint`, `status`, and `durationMs`.
