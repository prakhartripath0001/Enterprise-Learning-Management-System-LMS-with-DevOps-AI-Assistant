# API Endpoints Documentation

This document describes all verified REST endpoints exposed by the Authentication Service.

## 1. Authentication Controller (`/api/v1/auth`)

### Register Account
*   **Method**: `POST`
*   **Path**: `/register`
*   **Payload**:
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
*   **Response**: `201 Created`

### Login
*   **Method**: `POST`
*   **Path**: `/login`
*   **Payload**:
    ```json
    {
      "email": "john.doe@example.com",
      "password": "SecureP@ss123",
      "deviceFingerprint": "a1b2c3d4",
      "deviceType": "WEB"
    }
    ```
*   **Response**: `200 OK` (Sets secure HttpOnly `refreshToken` cookie).

### Refresh Token
*   **Method**: `POST`
*   **Path**: `/refresh-token`
*   **Headers**: Requires `Cookie: refreshToken=...`
*   **Response**: `200 OK` (Rotates cookie and issues new access token).

---

## 2. User Controller (`/api/v1/users`)

### Get Current Profile
*   **Method**: `GET`
*   **Path**: `/me`
*   **Headers**: `Authorization: Bearer <accessToken>`
*   **Response**: `200 OK`

### Update Profile
*   **Method**: `PUT`
*   **Path**: `/me`
*   **Payload**:
    ```json
    {
      "firstName": "Jonathan",
      "lastName": "Doe",
      "username": "johndoe"
    }
    ```
*   **Response**: `200 OK`
