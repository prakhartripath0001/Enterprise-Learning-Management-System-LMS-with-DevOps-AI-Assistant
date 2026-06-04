# Auth Service — API Testing Guide (cURL)

> **Senior QA / Backend Engineer Review**
> Base URL: `http://localhost:8081`
> All authenticated requests require: `Authorization: Bearer {{accessToken}}`

---

## Environment Setup

Export these variables in your terminal before running the commands:

```bash
export BASE_URL="http://localhost:8081"
export ACCESS_TOKEN=""
export REFRESH_TOKEN=""
export USER_ID=""
export ROLE_ID=""
export PERMISSION_ID=""
```

---

# AUTHENTICATION FLOW

---

## 1. Register User

```bash
curl --location --request POST "$BASE_URL/api/v1/auth/register" \
  --header 'Content-Type: application/json' \
  --data '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "username": "johndoe",
    "password": "SecureP@ss123",
    "confirmPassword": "SecureP@ss123"
  }'
```

**Expected Status**: `201 Created`

**Success Response**:
```json
{
  "success": true,
  "message": "Registration successful. Please check your email to verify your account.",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john.doe@example.com",
    "username": "johndoe"
  },
  "timestamp": "2026-06-05T03:00:00.000Z"
}
```

**Error Response (Duplicate Email)**:
```json
{
  "success": false,
  "message": "Email already registered",
  "errors": [{ "field": "email", "code": "EMAIL_ALREADY_EXISTS", "message": "This email address is already in use." }],
  "timestamp": "2026-06-05T03:00:00.000Z"
}
```

---

## 2. Verify Email

```bash
curl --location --request POST "$BASE_URL/api/v1/auth/verify-email" \
  --header 'Content-Type: application/json' \
  --data '{
    "token": "raw-verification-token-from-email"
  }'
```

**Expected Status**: `200 OK`

---

## 3. Login

```bash
curl --location --request POST "$BASE_URL/api/v1/auth/login" \
  --header 'Content-Type: application/json' \
  --data '{
    "email": "john.doe@example.com",
    "password": "SecureP@ss123",
    "deviceType": "WEB"
  }'
```

**Expected Status**: `200 OK`

**Success Response**:
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
      "roles": ["ROLE_STUDENT"]
    }
  },
  "timestamp": "2026-06-05T03:00:00.000Z"
}
```

---

## 4. Refresh Token

```bash
# Refresh token is sent via HttpOnly cookie automatically by the browser.
# For cURL, pass the cookie manually:
curl --location --request POST "$BASE_URL/api/v1/auth/refresh-token" \
  --header 'Content-Type: application/json' \
  --cookie "refreshToken=your-refresh-token-here"
```

**Expected Status**: `200 OK`

---

## 5. Get Current User

```bash
curl --location --request GET "$BASE_URL/api/v1/users/me" \
  --header "Authorization: Bearer $ACCESS_TOKEN"
```

**Expected Status**: `200 OK`

---

## 6. Update Profile

```bash
curl --location --request PUT "$BASE_URL/api/v1/users/me" \
  --header "Authorization: Bearer $ACCESS_TOKEN" \
  --header 'Content-Type: application/json' \
  --data '{
    "firstName": "Jonathan",
    "lastName": "Smith",
    "username": "jonathansmith"
  }'
```

**Expected Status**: `200 OK`

---

## 7. Change Password

```bash
curl --location --request POST "$BASE_URL/api/v1/auth/change-password" \
  --header "Authorization: Bearer $ACCESS_TOKEN" \
  --header 'Content-Type: application/json' \
  --data '{
    "currentPassword": "SecureP@ss123",
    "newPassword": "NewSecureP@ss456",
    "confirmPassword": "NewSecureP@ss456"
  }'
```

**Expected Status**: `200 OK`

---

## 8. Forgot Password

```bash
curl --location --request POST "$BASE_URL/api/v1/auth/forgot-password" \
  --header 'Content-Type: application/json' \
  --data '{
    "email": "john.doe@example.com"
  }'
```

**Expected Status**: `200 OK` (always — prevents email enumeration)

---

## 9. Reset Password

```bash
curl --location --request POST "$BASE_URL/api/v1/auth/reset-password" \
  --header 'Content-Type: application/json' \
  --data '{
    "token": "raw-reset-token-from-email",
    "newPassword": "ResetSecureP@ss789",
    "confirmPassword": "ResetSecureP@ss789"
  }'
```

**Expected Status**: `200 OK`

---

## 10. Logout

```bash
curl --location --request POST "$BASE_URL/api/v1/auth/logout" \
  --header "Authorization: Bearer $ACCESS_TOKEN" \
  --header 'Content-Type: application/json' \
  --data '{
    "logoutAllDevices": false
  }'
```

**Expected Status**: `200 OK`

---

# ADMIN FLOW

---

## 11. Get All Users

```bash
curl --location --request GET "$BASE_URL/api/v1/admin/users?page=0&size=20&sort=createdAt,desc" \
  --header "Authorization: Bearer $ACCESS_TOKEN"
```

**Required Role**: `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`  
**Expected Status**: `200 OK`

---

## 12. Get User By ID

```bash
curl --location --request GET "$BASE_URL/api/v1/admin/users/$USER_ID" \
  --header "Authorization: Bearer $ACCESS_TOKEN"
```

**Expected Status**: `200 OK`

---

## 13. Lock User

```bash
curl --location --request PATCH "$BASE_URL/api/v1/admin/users/$USER_ID/lock" \
  --header "Authorization: Bearer $ACCESS_TOKEN" \
  --header 'Content-Type: application/json' \
  --data '{
    "reason": "Suspicious activity detected",
    "lockDurationMinutes": 1440
  }'
```

**Required Role**: `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`  
**Expected Status**: `200 OK`

---

## 14. Unlock User

```bash
curl --location --request PATCH "$BASE_URL/api/v1/admin/users/$USER_ID/unlock" \
  --header "Authorization: Bearer $ACCESS_TOKEN" \
  --header 'Content-Type: application/json' \
  --data '{
    "reason": "Manually verified identity"
  }'
```

---

## 15. Assign Role to User

```bash
curl --location --request POST "$BASE_URL/api/v1/admin/users/$USER_ID/roles" \
  --header "Authorization: Bearer $ACCESS_TOKEN" \
  --header 'Content-Type: application/json' \
  --data '{
    "roleId": "'"$ROLE_ID"'"
  }'
```

**Required Role**: `ROLE_SUPER_ADMIN`  
**Expected Status**: `200 OK`

---

## 16. Remove Role from User

```bash
curl --location --request DELETE "$BASE_URL/api/v1/admin/users/$USER_ID/roles/$ROLE_ID" \
  --header "Authorization: Bearer $ACCESS_TOKEN"
```

**Expected Status**: `200 OK`

---

## 17. Create Role

```bash
curl --location --request POST "$BASE_URL/api/v1/admin/roles" \
  --header "Authorization: Bearer $ACCESS_TOKEN" \
  --header 'Content-Type: application/json' \
  --data '{
    "name": "ROLE_CONTENT_MODERATOR",
    "description": "Can review and moderate user-generated content"
  }'
```

**Required Role**: `ROLE_SUPER_ADMIN`  
**Expected Status**: `201 Created`

---

## 18. Create Permission

```bash
curl --location --request POST "$BASE_URL/api/v1/admin/permissions" \
  --header "Authorization: Bearer $ACCESS_TOKEN" \
  --header 'Content-Type: application/json' \
  --data '{
    "name": "course:publish",
    "description": "Ability to publish a course",
    "resource": "course",
    "action": "publish"
  }'
```

**Required Role**: `ROLE_SUPER_ADMIN`  
**Expected Status**: `201 Created`

---

## 19. Assign Permission to Role

```bash
curl --location --request POST "$BASE_URL/api/v1/admin/roles/$ROLE_ID/permissions" \
  --header "Authorization: Bearer $ACCESS_TOKEN" \
  --header 'Content-Type: application/json' \
  --data '{
    "permissionId": "'"$PERMISSION_ID"'"
  }'
```

---

# NEGATIVE TEST CASES

---

## Invalid Email Format

```bash
curl --location --request POST "$BASE_URL/api/v1/auth/register" \
  --header 'Content-Type: application/json' \
  --data '{"firstName":"John","lastName":"Doe","email":"not-an-email","username":"johndoe","password":"SecureP@ss123","confirmPassword":"SecureP@ss123"}'
```
**Expected**: `400 Bad Request` — `VALIDATION_ERROR`

## Password Mismatch

```bash
curl --location --request POST "$BASE_URL/api/v1/auth/register" \
  --header 'Content-Type: application/json' \
  --data '{"firstName":"John","lastName":"Doe","email":"john@example.com","username":"johndoe","password":"SecureP@ss123","confirmPassword":"DifferentPass123"}'
```
**Expected**: `400 Bad Request`

## Wrong Password on Login

```bash
curl --location --request POST "$BASE_URL/api/v1/auth/login" \
  --header 'Content-Type: application/json' \
  --data '{"email":"john.doe@example.com","password":"WrongPassword!"}'
```
**Expected**: `401 Unauthorized` — `INVALID_CREDENTIALS`

## Missing Bearer Token

```bash
curl --location --request GET "$BASE_URL/api/v1/users/me"
```
**Expected**: `401 Unauthorized` — `UNAUTHORIZED`

## Expired Token

```bash
curl --location --request GET "$BASE_URL/api/v1/users/me" \
  --header "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.EXPIRED.signature"
```
**Expected**: `401 Unauthorized` — `TOKEN_EXPIRED`

## Duplicate Email Registration

```bash
# Run register cURL twice with same email
```
**Expected**: `409 Conflict` — `EMAIL_ALREADY_EXISTS`

## Access Admin Route as Student

```bash
curl --location --request GET "$BASE_URL/api/v1/admin/users" \
  --header "Authorization: Bearer $STUDENT_ACCESS_TOKEN"
```
**Expected**: `403 Forbidden` — `ACCESS_DENIED`

---

# QA VALIDATION CHECKLIST

- [ ] Register endpoint returns `201` with userId in data
- [ ] Duplicate email returns `409` with `EMAIL_ALREADY_EXISTS` code
- [ ] Login returns access token and sets HttpOnly refresh token cookie
- [ ] Login with wrong password returns `401` — not `404` (no enumeration)
- [ ] 5th consecutive wrong password triggers account lock → `423`
- [ ] Email not verified returns `403` with `EMAIL_NOT_VERIFIED` code
- [ ] Token refresh rotates refresh token (old one is revoked)
- [ ] Logout revokes the current device refresh token
- [ ] Student cannot access `/admin/*` endpoints → `403`
- [ ] Password reset token is one-time use only
- [ ] All error responses follow the standard envelope format
- [ ] No stack traces or internal error details in any response
