# Auth Service — Testing Strategy

> **Senior Java Backend Engineer Review**
> Enterprise LMS — Authentication Service Testing
> Stack: Java 21 · Spring Boot 3 · JUnit 5 · Mockito · MockMvc · JaCoCo

---

## Test Folder Structure

```text
auth-service/src/test/java/com/auth_service/
│
├── unit/
│   ├── service/
│   │   ├── AuthServiceTest.java
│   │   ├── UserServiceTest.java
│   │   ├── RoleServiceTest.java
│   │   ├── PermissionServiceTest.java
│   │   └── JwtServiceTest.java
│   └── util/
│       └── PasswordValidatorTest.java
│
├── controller/
│   ├── AuthControllerTest.java
│   ├── UserControllerTest.java
│   ├── AdminControllerTest.java
│   ├── RoleControllerTest.java
│   └── PermissionControllerTest.java
│
├── security/
│   ├── JwtAuthFilterTest.java
│   └── SecurityConfigTest.java
│
├── repository/
│   ├── UserRepositoryTest.java
│   ├── RoleRepositoryTest.java
│   ├── RefreshTokenRepositoryTest.java
│   └── LoginAuditLogRepositoryTest.java
│
└── integration/
    └── AuthFlowIntegrationTest.java
```

---

## Coverage Targets

| Layer | Minimum | Target |
|---|---|---|
| Service Layer | 90% | 95% |
| Controller Layer | 85% | 90% |
| Repository Layer | 80% | 85% |
| Security Layer | 85% | 90% |
| Overall | 85% | 90%+ |

---

## Naming Convention

```
should<ExpectedBehavior>_When<Condition>()
```

Examples:
- `shouldRegisterUser_WhenValidRequest()`
- `shouldThrowEmailAlreadyExistsException_WhenEmailIsDuplicate()`
- `shouldReturnUnauthorized_WhenJwtTokenIsExpired()`
- `shouldLockAccount_WhenFailedAttemptsExceedThreshold()`

---

## AAA Pattern (Arrange-Act-Assert)

Every test must follow this structure:
```java
@Test
void shouldRegisterUser_WhenValidRequest() {
    // Arrange — set up mocks, inputs, and expected values
    
    // Act — execute the method under test
    
    // Assert — verify outputs and interactions
}
```

---

## Mocking Rules

| Scenario | Annotation |
|---|---|
| Unit test dependency | `@Mock` |
| Class under test | `@InjectMocks` |
| Spring context test | `@MockBean` |
| Verify saved entity | `ArgumentCaptor<T>` |
| Simulate exception | `doThrow()` |
| Simulate return value | `when().thenReturn()` |
| Verify method called | `verify()` |

---

## Exception Testing Matrix

| Exception Class | Trigger Scenario |
|---|---|
| `EmailAlreadyExistsException` | Duplicate email registration |
| `UsernameAlreadyExistsException` | Duplicate username registration |
| `InvalidCredentialsException` | Wrong password on login |
| `AccountLockedException` | Too many failed login attempts |
| `EmailNotVerifiedException` | Login before email verification |
| `AccountDisabledException` | Disabled account login attempt |
| `UserNotFoundException` | Get/update non-existent user |
| `RoleNotFoundException` | Assign non-existent role |
| `InvalidTokenException` | Expired / malformed / revoked token |
| `TokenAlreadyUsedException` | Reuse of password reset/email token |
| `PermissionNotFoundException` | Assign non-existent permission |
| `SystemRoleModificationException` | Modify/delete system-level role |
