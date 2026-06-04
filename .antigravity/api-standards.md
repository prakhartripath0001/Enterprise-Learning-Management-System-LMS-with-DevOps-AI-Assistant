# API Standards

This document establishes the standards for developing, naming, and documenting APIs across the Enterprise LMS microservices.

## 1. REST Naming Conventions

All endpoints must be resource-oriented and conform to standard REST design principles.

- **Resource Naming**: Use plural nouns to represent resources (e.g., `/api/v1/courses`, not `/api/v1/getCourse` or `/api/v1/allCourses`).
- **HTTP Methods**: Use HTTP verbs correctly to reflect operations:
  - `GET`: Retrieve resources (must be safe and idempotent).
  - `POST`: Create a new resource.
  - `PUT`: Replace an entire resource or update a full record.
  - `PATCH`: Partially update a resource (e.g., update a single field like `status`).
  - `DELETE`: Remove a resource.
- **Lowercase & Hyphens**: Use lowercase and hyphens (`kebab-case`) for URLs (e.g., `/api/v1/course-sections`).
- **Versioning**: Prefix all public APIs with the API version (e.g., `/api/v1/...`).

---

## 2. Response Format

All microservices must return a unified JSON response schema. This makes consumption consistent for client developers.

### Standard Response Envelope
```json
{
  "success": true,
  "message": "Resource retrieved successfully",
  "data": { ... },
  "timestamp": "2026-06-05T02:50:00.000Z"
}
```

### paginated Response Envelope
For listings/searches that return multiple pages of records:
```json
{
  "success": true,
  "message": "Courses retrieved successfully",
  "data": {
    "content": [ ... ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 45,
    "totalPages": 5,
    "last": false
  },
  "timestamp": "2026-06-05T02:50:00.000Z"
}
```

---

## 3. Error Handling Standards

Never return raw exception stack traces to client applications (this is both a bad developer experience and a security vulnerability).

### Global Exception Handler
All microservices must implement a controller advisor (`@RestControllerAdvice`) that catches exceptions and maps them to a standard error format.

### Error Response Schema
```json
{
  "success": false,
  "message": "Validation failed",
  "error": {
    "code": "BAD_REQUEST",
    "details": [
      {
        "field": "email",
        "issue": "Must be a valid email address"
      }
    ]
  },
  "timestamp": "2026-06-05T02:50:00.000Z"
}
```

### Standard Error Codes
Use appropriate HTTP Status codes alongside these error descriptors:

| HTTP Status | Error Code | Scenario |
| :--- | :--- | :--- |
| `400 Bad Request` | `VALIDATION_ERROR` / `BAD_REQUEST` | Request payload fails schema validation. |
| `401 Unauthorized` | `UNAUTHORIZED` | Expired, missing, or invalid authentication tokens. |
| `403 Forbidden` | `ACCESS_DENIED` | Authenticated user lacks permission/role to access resource. |
| `404 Not Found` | `RESOURCE_NOT_FOUND` | Resource with requested identifier does not exist. |
| `500 Internal Server Error` | `INTERNAL_SERVER_ERROR` | Unexpected backend code errors/crashes. |
