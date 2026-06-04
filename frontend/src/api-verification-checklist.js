/**
 * AETHERLMS API VERIFICATION CHECKLIST
 *
 * This file lists and documents all verified backend REST API endpoints
 * for the Authentication Service, validating their functionality,
 * request/response payloads, and security requirements.
 */

export const apiVerificationChecklist = [
  // ── Authentication Flows ──────────────────────────────────────────────────
  {
    endpoint: "/api/v1/auth/register",
    method: "POST",
    description: "Register a new user account with default STUDENT role.",
    requestHeaders: {
      "Content-Type": "application/json"
    },
    requestBody: {
      firstName: "John",
      lastName: "Doe",
      email: "john.doe@example.com",
      username: "johndoe",
      password: "SecureP@ss123",
      confirmPassword: "SecureP@ss123"
    },
    responseBody: {
      status: "success",
      message: "Registration successful. Please verify your email.",
      data: {
        id: "char-36-uuid-format",
        email: "john.doe@example.com",
        username: "johndoe"
      }
    },
    status: "VERIFIED & WORKING"
  },
  {
    endpoint: "/api/v1/auth/verify-email",
    method: "POST",
    description: "Verify email address using a one-time verification token.",
    requestHeaders: {
      "Content-Type": "application/json"
    },
    requestBody: {
      token: "one-time-sha256-hashed-verification-token"
    },
    responseBody: {
      status: "success",
      message: "Email verified successfully. You can now login.",
      data: null
    },
    status: "VERIFIED & WORKING"
  },
  {
    endpoint: "/api/v1/auth/login",
    method: "POST",
    description: "Login with email and password. Generates JWT access token and sets an HttpOnly refresh token cookie.",
    requestHeaders: {
      "Content-Type": "application/json"
    },
    requestBody: {
      email: "john.doe@example.com",
      password: "SecureP@ss123",
      deviceFingerprint: "a1b2c3d4",
      deviceType: "WEB"
    },
    responseBody: {
      status: "success",
      message: "Login successful",
      data: {
        accessToken: "header.payload.signature-jwt-token",
        refreshToken: "raw-secure-refresh-token",
        tokenType: "Bearer",
        expiresIn: 900,
        user: {
          id: "uuid-id",
          email: "john.doe@example.com",
          username: "johndoe",
          firstName: "John",
          lastName: "Doe",
          roles: ["ROLE_STUDENT"],
          emailVerified: true
        }
      }
    },
    status: "VERIFIED & WORKING"
  },
  {
    endpoint: "/api/v1/auth/refresh-token",
    method: "POST",
    description: "Rotate refresh token and issue a new access token.",
    requestHeaders: {
      "Cookie": "refreshToken=raw-secure-refresh-token"
    },
    requestBody: null,
    responseBody: {
      status: "success",
      message: "Token refreshed successfully",
      data: {
        accessToken: "new-access-token",
        refreshToken: "new-rotated-refresh-token",
        tokenType: "Bearer",
        expiresIn: 900,
        user: {
          id: "uuid-id",
          email: "john.doe@example.com",
          username: "johndoe",
          firstName: "John",
          lastName: "Doe",
          roles: ["ROLE_STUDENT"],
          emailVerified: true
        }
      }
    },
    status: "VERIFIED & WORKING"
  },
  {
    endpoint: "/api/v1/auth/forgot-password",
    method: "POST",
    description: "Request a password reset token link sent to user email.",
    requestHeaders: {
      "Content-Type": "application/json"
    },
    requestBody: {
      email: "john.doe@example.com"
    },
    responseBody: {
      status: "success",
      message: "If an account with that email exists, a reset link has been sent.",
      data: null
    },
    status: "VERIFIED & WORKING"
  },
  {
    endpoint: "/api/v1/auth/reset-password",
    method: "POST",
    description: "Reset account password using the token sent via email.",
    requestHeaders: {
      "Content-Type": "application/json"
    },
    requestBody: {
      token: "one-time-password-reset-token",
      newPassword: "NewSecureP@ss123",
      confirmPassword: "NewSecureP@ss123"
    },
    responseBody: {
      status: "success",
      message: "Password reset successfully. Please login.",
      data: null
    },
    status: "VERIFIED & WORKING"
  },
  {
    endpoint: "/api/v1/auth/change-password",
    method: "POST",
    description: "Change password for an authenticated session user.",
    requestHeaders: {
      "Authorization": "Bearer accessToken.jwt.value",
      "Content-Type": "application/json"
    },
    requestBody: {
      currentPassword: "SecureP@ss123",
      newPassword: "NewSecureP@ss123",
      confirmPassword: "NewSecureP@ss123"
    },
    responseBody: {
      status: "success",
      message: "Password changed successfully.",
      data: null
    },
    status: "VERIFIED & WORKING"
  },
  {
    endpoint: "/api/v1/auth/logout",
    method: "POST",
    description: "Revoke user refresh tokens and clear HTTP cookies.",
    requestHeaders: {
      "Authorization": "Bearer accessToken.jwt.value"
    },
    requestBody: null,
    responseBody: {
      status: "success",
      message: "Logged out successfully",
      data: null
    },
    status: "VERIFIED & WORKING"
  },

  // ── User Profiling ────────────────────────────────────────────────────────
  {
    endpoint: "/api/v1/users/me",
    method: "GET",
    description: "Retrieve profile data for the currently authenticated user.",
    requestHeaders: {
      "Authorization": "Bearer accessToken.jwt.value"
    },
    requestBody: null,
    responseBody: {
      status: "success",
      message: "User retrieved",
      data: {
        id: "uuid-id",
        email: "john.doe@example.com",
        username: "johndoe",
        firstName: "John",
        lastName: "Doe",
        roles: ["ROLE_STUDENT"],
        emailVerified: true
      }
    },
    status: "VERIFIED & WORKING"
  },
  {
    endpoint: "/api/v1/users/me",
    method: "PUT",
    description: "Update currently authenticated user profile fields.",
    requestHeaders: {
      "Authorization": "Bearer accessToken.jwt.value",
      "Content-Type": "application/json"
    },
    requestBody: {
      firstName: "Johnny",
      lastName: "Doe"
    },
    responseBody: {
      status: "success",
      message: "Profile updated successfully",
      data: {
        id: "uuid-id",
        email: "john.doe@example.com",
        username: "johndoe",
        firstName: "Johnny",
        lastName: "Doe",
        roles: ["ROLE_STUDENT"],
        emailVerified: true
      }
    },
    status: "VERIFIED & WORKING"
  },
  {
    endpoint: "/api/v1/users/me",
    method: "DELETE",
    description: "Deactivate user account (soft delete).",
    requestHeaders: {
      "Authorization": "Bearer accessToken.jwt.value"
    },
    requestBody: null,
    responseBody: {
      status: "success",
      message: "Account deactivated successfully",
      data: null
    },
    status: "VERIFIED & WORKING"
  },

  // ── Administrative APIs (Requires ROLE_ADMIN / ROLE_SUPER_ADMIN) ──────────
  {
    endpoint: "/api/v1/admin/users/{id}",
    method: "GET",
    description: "Retrieve any user by ID (requires ADMIN/SUPER_ADMIN).",
    requestHeaders: {
      "Authorization": "Bearer adminAccessToken.jwt.value"
    },
    requestBody: null,
    responseBody: {
      status: "success",
      message: "User retrieved",
      data: {
        id: "uuid-id",
        email: "john.doe@example.com",
        username: "johndoe",
        firstName: "John",
        lastName: "Doe",
        roles: ["ROLE_STUDENT"],
        emailVerified: true
      }
    },
    status: "VERIFIED & WORKING"
  },
  {
    endpoint: "/api/v1/admin/users/{userId}/roles",
    method: "POST",
    description: "Assign a role to a user (requires SUPER_ADMIN).",
    requestHeaders: {
      "Authorization": "Bearer superAdminAccessToken.jwt.value",
      "Content-Type": "application/json"
    },
    requestBody: {
      roleId: "role-uuid"
    },
    responseBody: {
      status: "success",
      message: "Role assigned successfully",
      data: null
    },
    status: "VERIFIED & WORKING"
  },
  {
    endpoint: "/api/v1/admin/users/{userId}/roles/{roleId}",
    method: "DELETE",
    description: "Remove a role from a user (requires SUPER_ADMIN).",
    requestHeaders: {
      "Authorization": "Bearer superAdminAccessToken.jwt.value"
    },
    requestBody: null,
    responseBody: {
      status: "success",
      message: "Role removed successfully",
      data: null
    },
    status: "VERIFIED & WORKING"
  },
  {
    endpoint: "/api/v1/admin/roles",
    method: "POST",
    description: "Create a custom role (requires SUPER_ADMIN).",
    requestHeaders: {
      "Authorization": "Bearer superAdminAccessToken.jwt.value",
      "Content-Type": "application/json"
    },
    requestBody: {
      name: "ROLE_MODERATOR",
      description: "Moderates content and reviews courses."
    },
    responseBody: {
      status: "success",
      message: "Role created",
      data: {
        id: "new-role-uuid",
        name: "ROLE_MODERATOR",
        description: "Moderates content and reviews courses.",
        systemRole: false
      }
    },
    status: "VERIFIED & WORKING"
  },
  {
    endpoint: "/api/v1/admin/roles",
    method: "GET",
    description: "Get all custom and system roles (requires ADMIN/SUPER_ADMIN).",
    requestHeaders: {
      "Authorization": "Bearer adminAccessToken.jwt.value"
    },
    requestBody: null,
    responseBody: {
      status: "success",
      message: "Roles retrieved",
      data: {
        content: [
          {
            id: "role-uuid",
            name: "ROLE_STUDENT",
            description: "Default student role",
            systemRole: true
          }
        ]
      }
    },
    status: "VERIFIED & WORKING"
  }
];
