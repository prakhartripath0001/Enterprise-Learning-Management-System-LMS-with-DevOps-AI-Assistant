# Developer and AI Verification Checklist (AI_CHECKLIST.md)

Verify the checklist items below before making any git commit, opening a pull request, or triggering a remote push.

---

## 1. Architecture
- [ ] **Correct service ownership**: Changes are implemented inside the correct microservice boundary (e.g. auth logic is strictly inside `auth-service`).
- [ ] **No business logic in controllers**: Controller classes only orchestrate requests, map parameters, validate inputs, and return responses.
- [ ] **DTOs used correctly**: No JPA entities are exposed directly to API controllers. Data mapping is done using record DTOs.
- [ ] **SOLID principles followed**: Code is decoupled, classes have a single responsibility, and interfaces are implemented cleanly.

---

## 2. Security
- [ ] **No hardcoded secrets**: Verified that no passwords, JWT signing keys, secret tokens, or configurations are hardcoded.
- [ ] **BCrypt used for passwords**: All password hashing operations use BCrypt with a work factor of 12+.
- [ ] **JWT validated correctly**: Auth endpoints sign and validate tokens using standard, secure claims verification.
- [ ] **Input validation implemented**: Request payloads are annotated with validation tags (`@NotBlank`, `@Size`, etc.) and controllers have `@Valid`.

---

## 3. Database
- [ ] **Indexes reviewed**: Indexes are added to all foreign keys and columns frequently used in query search filters.
- [ ] **Migrations created**: Database alterations are written as timestamped SQL scripts inside Flyway's migration path.
- [ ] **No breaking schema changes**: Schema additions are additive; existing columns are not renamed or dropped without compatibility plans.

---

## 4. Testing
- [ ] **Unit tests added**: Test files are created/updated using JUnit 5 and AssertJ to cover new service-level logic.
- [ ] **Existing tests passing**: All local unit and integration tests compile and run successfully.
- [ ] **Mockito used where needed**: Database layers, remote API clients, and static utilities are properly stubbed/mocked.
- [ ] **Coverage maintained**: Service layer line coverage matches or exceeds the 80% threshold.

---

## 5. API
- [ ] **REST naming conventions followed**: Resource endpoints are plural nouns, lowercase, and use kebab-case.
- [ ] **Proper status codes returned**: Successful writes return `201 Created`, updates/reads return `200 OK`, validation failures return `400 Bad Request`.
- [ ] **Error handling implemented**: Custom exceptions are trapped by `@RestControllerAdvice` and return a standard, secure error JSON payload.

---

## 6. Code Quality
- [ ] **No dead code**: Unused variables, imports, and classes have been removed.
- [ ] **No TODO left behind**: No `TODO`, `FIXME`, or temporary comments are left in production source code.
- [ ] **No commented-out code**: Scaffolding or disabled blocks of code have been deleted.
- [ ] **SonarLint issues reviewed**: Linter warnings have been verified and resolved.

---

## 7. Git & CI/CD
- [ ] **Branch naming convention followed**: Feature branch prefix matches scope (`feature_fe/`, `feature_be/`, `feature_db/`).
- [ ] **Commit message follows standard**: Message conforms to conventional commit formatting.
- [ ] **Rebase completed**: Checked out branch is rebased onto `origin/main` to avoid merge conflict commits.
- [ ] **No merge conflicts**: Confirmed zero conflicts exist with upstream branch.

---

## 8. Documentation
- [ ] **README updated if needed**: Architectural updates or startup rules are documented.
- [ ] **API docs updated if needed**: Swagger annotations or schemas are adjusted.
- [ ] **Database docs updated if needed**: Database schemas or migration descriptions are documented.
