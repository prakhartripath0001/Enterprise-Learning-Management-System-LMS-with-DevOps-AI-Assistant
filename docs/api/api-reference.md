# REST API Reference Guide

This document lists the REST API contracts, request examples, response envelopes, and error formats for the Enterprise LMS.

---

## 1. Global Response Envelope
All APIs return a unified response envelope. Stack traces are never leaked to clients.

### Success Envelope
```json
{
  "status": "success",
  "message": "Optional feedback message",
  "data": {}
}
```

### Error Envelope
```json
{
  "status": "error",
  "errorCode": "INVALID_PAYLOAD",
  "message": "Validation failed",
  "validationErrors": [
    {
      "field": "email",
      "message": "Must be a valid email format"
    }
  ],
  "timestamp": "2026-06-05T00:00:00.000Z"
}
```

---

## 2. Authentication Services (`auth-service`)

### POST `/api/v1/auth/register`
Creates a new student account.
*   **Request Payload**:
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
*   **Response (201 Created)**:
    ```json
    {
      "status": "success",
      "message": "Registration successful",
      "data": {
        "id": "1a2b3c4d-5e6f-7g8h-9i0j-1k2l3m4n5o6p"
      }
    }
    ```

### POST `/api/v1/auth/login`
Authenticates user credentials and returns tokens.
*   **Request Payload**:
    ```json
    {
      "email": "john.doe@example.com",
      "password": "SecureP@ss123"
    }
    ```
*   **Response (200 OK)**:
    ```json
    {
      "status": "success",
      "message": "Login successful",
      "data": {
        "accessToken": "ey...",
        "user": {
          "email": "john.doe@example.com",
          "username": "johndoe",
          "firstName": "John",
          "lastName": "Doe",
          "roles": ["ROLE_STUDENT"]
        }
      }
    }
    ```
    *Note: A secure refresh token is also set inside an HttpOnly cookie named `refreshToken`.*

### POST `/api/v1/auth/logout`
Invalidates the current session and clears cookies.
*   **Response (200 OK)**:
    ```json
    {
      "status": "success",
      "message": "Logged out successfully"
    }
    ```

---

## 3. User Profiles (`auth-service`)

### GET `/api/v1/users/profile`
Fetches current logged-in user profile details.
*   **Headers**: `Authorization: Bearer <token>`
*   **Response (200 OK)**:
    ```json
    {
      "status": "success",
      "data": {
        "id": "1a2b3c4d-...",
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@example.com",
        "username": "johndoe",
        "roles": ["ROLE_STUDENT"]
      }
    }
    ```

### PUT `/api/v1/users/profile`
Updates current user profile details.
*   **Headers**: `Authorization: Bearer <token>`
*   **Request Payload**:
    ```json
    {
      "firstName": "Johnny",
      "lastName": "Doe"
    }
    ```
*   **Response (200 OK)**:
    ```json
    {
      "status": "success",
      "message": "Profile updated successfully",
      "data": {
        "id": "1a2b3c4d-...",
        "firstName": "Johnny",
        "lastName": "Doe",
        "email": "john.doe@example.com",
        "username": "johndoe"
      }
    }
    ```

---

## 4. Course Management (`course-service` - Mocked in Postman)

### GET `/api/v1/courses`
Fetches a list of all active courses.
*   **Response (200 OK)**:
    ```json
    {
      "status": "success",
      "data": [
        {
          "id": "course-1",
          "title": "Introduction to Microservices Architecture",
          "instructor": "Dr. Evelyn Foster",
          "difficulty": "Beginner",
          "duration": "6 Weeks",
          "enrollmentStatus": "Open"
        }
      ]
    }
    ```

---

## 5. Enrollment Management (`enrollment-service` - Mocked in Postman)

### POST `/api/v1/enrollments`
Enrolls a user into a course.
*   **Request Payload**:
    ```json
    {
      "courseId": "course-1"
    }
    ```
*   **Response (201 Created)**:
    ```json
    {
      "status": "success",
      "message": "Enrolled in course successfully",
      "data": {
        "enrollmentId": "enrollment-99",
        "courseId": "course-1",
        "status": "ACTIVE"
      }
    }
    ```
