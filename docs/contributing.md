# Contributing to Enterprise LMS

Thank you for your interest in contributing to the **Enterprise Learning Management System**! We welcome contributions from developers, designers, testers, and technical writers. This guide outlines the standards and workflow to ensure a smooth collaboration experience.

---

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [Getting Started](#getting-started)
3. [Project Structure](#project-structure)
4. [Development Workflow](#development-workflow)
5. [Branch Naming Convention](#branch-naming-convention)
6. [Commit Message Standards](#commit-message-standards)
7. [Pull Request Guidelines](#pull-request-guidelines)
8. [Coding Standards](#coding-standards)
9. [Testing Requirements](#testing-requirements)
10. [API Contract Compliance](#api-contract-compliance)
11. [Documentation Standards](#documentation-standards)
12. [Reporting Issues](#reporting-issues)
13. [Feature Requests](#feature-requests)
14. [Security Vulnerabilities](#security-vulnerabilities)
15. [Review Process](#review-process)
16. [Recognition](#recognition)

---

## 1. Code of Conduct

By participating in this project, you agree to uphold a respectful, inclusive, and professional environment. We expect all contributors to:

- Be respectful and constructive in all communications.
- Avoid discriminatory language or behavior.
- Provide helpful feedback, not personal criticism.
- Collaborate openly and transparently.

Violations should be reported to the project maintainers.

---

## 2. Getting Started

### Prerequisites

Before contributing, ensure you have the following installed:

| Tool        | Version     |
|-------------|-------------|
| Java (JDK)  | 17+         |
| Maven       | 3.9+        |
| Node.js     | 18+         |
| Docker      | 24+         |
| Git         | 2.40+       |
| MySQL       | 8.0+        |


### Fork and Clone

```bash
# 1. Fork the repository on GitHub
# 2. Clone your fork
git clone https://github.com/<your-username>/Enterprise-Learning-Management-System-LMS-with-DevOps-AI-Assistant.git

# 3. Add the upstream remote
cd Enterprise-Learning-Management-System-LMS-with-DevOps-AI-Assistant
git remote add upstream https://github.com/prakhartripath0001/Enterprise-Learning-Management-System-LMS-with-DevOps-AI-Assistant.git
```

### Setup Local Environment

```bash
# Copy environment configuration
cp .env.example .env

# Start services with Docker Compose
docker-compose up -d

# Install backend dependencies
cd backend && mvn clean install

# Install frontend dependencies
cd frontend && npm install

# Run the application
mvn spring-boot:run          # Backend
npm run dev                  # Frontend
```

---

## 3. Project Structure

```
Enterprise-LMS/
├── backend/                  # Spring Boot API
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/lms/
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── model/
│   │   │   │   ├── dto/
│   │   │   │   ├── config/
│   │   │   │   └── exception/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── frontend/                 # React Application
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── hooks/
│   │   ├── services/
│   │   └── store/
│   └── package.json
├── ai-service/               # Python AI Microservice
│   ├── app/
│   └── requirements.txt
├── docs/                     # Project Documentation
├── k8s/                      # Kubernetes Manifests
├── .github/                  # CI/CD Workflows
└── docker-compose.yml
```

---

## 4. Development Workflow

We follow **GitHub Flow**:

```
main ──────────────────────────────────────── (production-ready)
  └── develop ──────────────────────────────── (integration branch)
        ├── feature/feat-name
        ├── bugfix/bug-name
        ├── hotfix/hotfix-name
        └── chore/chore-name
```

### Step-by-Step

1. **Sync your fork** with the upstream `develop` branch before starting any work.

```bash
git fetch upstream
git checkout develop
git merge upstream/develop
```

2. **Create a feature branch** from `develop`.

```bash
git checkout -b feature/add-quiz-timer
```

3. **Implement your changes** following the [Coding Standards](#coding-standards).

4. **Write or update tests** to cover your changes.

5. **Commit your changes** following the [Commit Message Standards](#commit-message-standards).

6. **Push to your fork** and open a Pull Request against the `develop` branch.

> **Important:** Never push directly to `main` or `develop`. All changes must go through a Pull Request.

---

## 5. Branch Naming Convention

Use the following naming pattern:

```
<type>/<short-description>
```

| Type       | Use Case                                     |
|------------|----------------------------------------------|
| `feature/` | New features or enhancements                 |
| `bugfix/`  | Bug fixes on the `develop` branch            |
| `hotfix/`  | Critical fixes on the `main` branch          |
| `chore/`   | Maintenance, tooling, or config changes      |
| `docs/`    | Documentation-only changes                  |
| `test/`    | Adding or improving test coverage            |
| `refactor/`| Code restructuring without behavior changes  |

**Examples:**

```
feature/certificate-generation
bugfix/fix-quiz-auto-evaluation
hotfix/jwt-token-expiry
docs/update-api-contract
chore/upgrade-spring-boot-3.2
```

---

## 6. Commit Message Standards

We follow the **Conventional Commits** specification.

### Format

```
<type>(<scope>): <short description>

[optional body]

[optional footer(s)]
```

### Types

| Type       | Description                                         |
|------------|-----------------------------------------------------|
| `feat`     | A new feature                                       |
| `fix`      | A bug fix                                           |
| `docs`     | Documentation changes only                         |
| `style`    | Formatting changes (no logic change)                |
| `refactor` | Code refactoring without adding features or fixes   |
| `test`     | Adding or updating tests                            |
| `chore`    | Build process, tooling, or dependency updates       |
| `perf`     | Performance improvements                            |
| `ci`       | CI/CD pipeline changes                              |

### Examples

```
feat(course): add video upload support with S3 integration

fix(auth): resolve JWT token not refreshing on expiry

docs(api): update quiz endpoints in api-contract.md

test(enrollment): add unit tests for enrollment service

chore(deps): upgrade Spring Boot to 3.2.5
```

### Rules

- Use the **imperative mood**: "add feature" not "added feature"
- Keep the subject line under **72 characters**
- Reference issues and PRs in the footer: `Closes #42`, `Refs #18`
- Breaking changes must be noted with `BREAKING CHANGE:` in the footer

---

## 7. Pull Request Guidelines

### Before Opening a PR

- [ ] Code compiles and all tests pass locally
- [ ] New functionality is covered by tests
- [ ] Swagger/OpenAPI documentation is updated (if API changed)
- [ ] No sensitive data (keys, passwords) is committed
- [ ] Branch is up to date with `upstream/develop`

### PR Title Format

Follow the same convention as commits:

```
feat(quiz): implement timer-based quiz auto-submission
fix(auth): resolve Google OAuth redirect loop
```

### PR Description Template

When opening a PR, fill in the provided template:

```markdown
## Summary
Brief description of what this PR does.

## Type of Change
- [ ] New feature
- [ ] Bug fix
- [ ] Documentation
- [ ] Refactoring
- [ ] CI/CD change

## Related Issues
Closes #<issue-number>

## How Has This Been Tested?
Describe the tests run and the testing approach.

## Checklist
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] No breaking changes (or noted in the description)
```

### PR Size Guidelines

| PR Type        | Recommended Size          |
|----------------|---------------------------|
| Feature        | < 500 lines changed        |
| Bug Fix        | < 200 lines changed        |
| Refactoring    | < 300 lines changed        |

Large PRs should be broken into smaller, incremental PRs where possible.

---

## 8. Coding Standards

### Backend (Java / Spring Boot)

- Follow **Google Java Style Guide**.
- Use **constructor injection** over field injection (`@Autowired`).
- All service methods must have **Javadoc** for public APIs.
- Use **DTOs** for all request/response payloads — never expose entity classes directly.
- Handle all exceptions via a **global exception handler** (`@RestControllerAdvice`).
- Use `ResponseEntity<ApiResponse<?>>` as the standard response wrapper.
- All endpoints must be secured with appropriate roles using `@PreAuthorize`.

```java
// Correct
@PreAuthorize("hasRole('INSTRUCTOR')")
@PostMapping("/courses")
public ResponseEntity<ApiResponse<CourseDto>> createCourse(@Valid @RequestBody CreateCourseRequest request) {
    CourseDto course = courseService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(course));
}

// Avoid
@PostMapping("/courses")
public Course createCourse(@RequestBody Course course) {
    return courseRepository.save(course);
}
```

### Frontend (React / TypeScript)

- Use **functional components** with hooks; avoid class components.
- Use **TypeScript** strictly — no implicit `any`.
- Manage API calls using a **service layer** (`src/services/`), not inline in components.
- Use **React Query** for server state management.
- Use **Zod** for form validation schemas.
- Component files should be named in **PascalCase**: `CourseCard.tsx`.
- Utility files should be named in **camelCase**: `formatDate.ts`.

### AI Service (Python)

- Follow **PEP 8** style guide.
- Use **type hints** for all function signatures.
- Use **Pydantic** for request/response models.
- Add docstrings to all public functions.

---

## 9. Testing Requirements

All contributions must meet the following testing thresholds:

| Layer              | Minimum Coverage |
|--------------------|-----------------|
| Backend Unit Tests | 80%             |
| Backend Integration Tests | At least per service |
| Frontend Unit Tests | 70%            |
| E2E Tests          | Critical flows  |

### Running Tests

```bash
# Backend unit tests
cd backend && mvn test

# Backend with coverage report
mvn verify -P coverage

# Frontend tests
cd frontend && npm test

# Frontend with coverage
npm run test:coverage

# E2E tests
npm run test:e2e
```

### Test Naming Convention

```java
// Java: methodName_StateUnderTest_ExpectedBehavior
@Test
void enrollStudent_WhenCourseIsFull_ShouldThrowEnrollmentException() { ... }
```

```typescript
// TypeScript/Jest
describe('CourseService', () => {
  it('should throw error when enrolling in a full course', () => { ... });
});
```

---

## 10. API Contract Compliance

Any change to a REST API endpoint **must**:

1. Update the `docs/api-contract.md` document.
2. Update the **Swagger/OpenAPI** annotations in the controller.
3. Be backward-compatible unless it's a versioned breaking change.
4. Follow the established **RESTful conventions** defined in `api-contract.md`.

If a breaking change is unavoidable, introduce a **new API version** (e.g., `/api/v2/...`) and mark the old version as `@Deprecated`.

---

## 11. Documentation Standards

- All public methods and classes in the backend must have **Javadoc**.
- All exported functions and types in the frontend must have **JSDoc** comments.
- Any user-facing feature must be described in the appropriate `docs/` file.
- Diagrams should be created using **Mermaid** or **draw.io** and checked into the `docs/diagrams/` folder.

---

## 12. Reporting Issues

Before opening a new issue, **search existing issues** to avoid duplicates.

### Bug Report

When reporting a bug, include:

- **Steps to reproduce** (clear, numbered steps)
- **Expected behavior**
- **Actual behavior**
- **Environment** (OS, browser, Java version, etc.)
- **Screenshots or logs** if applicable

Use the **Bug Report** issue template provided in `.github/ISSUE_TEMPLATE/bug_report.md`.

### Labels Used

| Label            | Meaning                              |
|------------------|--------------------------------------|
| `bug`            | Confirmed bug                        |
| `enhancement`    | New feature request                  |
| `documentation`  | Documentation improvement            |
| `good first issue` | Suitable for new contributors      |
| `help wanted`    | Community help needed                |
| `priority: high` | Critical issue needing fast resolution |
| `wontfix`        | Will not be addressed                |

---

## 13. Feature Requests

To propose a new feature:

1. Open an issue using the **Feature Request** template.
2. Describe the **problem** the feature solves.
3. Propose a **solution** with user stories if possible.
4. Reference the relevant section in `requirements.md`.
5. Discuss with maintainers before starting implementation.

Large features should be preceded by a **design proposal** (a markdown doc in `docs/proposals/`) before any code is written.

---

## 14. Security Vulnerabilities

**Do NOT open a public issue for security vulnerabilities.**

Instead, report them privately by emailing the maintainers directly or using GitHub's **Private Security Advisory** feature:

> GitHub → Security → Advisories → Report a Vulnerability

Include:
- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if any)

We aim to acknowledge security reports within **48 hours** and resolve critical issues within **7 days**.

---

## 15. Review Process

All PRs require:

- **At least 1 approval** from a core maintainer before merging.
- All **CI/CD checks** must pass (build, tests, linting).
- No unresolved **review comments**.

### Review SLA

| PR Type     | Target Review Time |
|-------------|--------------------|
| Bug Fix     | 1–2 business days  |
| Feature     | 3–5 business days  |
| Docs/Chore  | 1–2 business days  |
| Hotfix      | Same day           |

Reviewers will focus on:
- Correctness and edge case handling
- Test coverage
- Code readability and maintainability
- Security implications
- API contract compliance
- Performance considerations

---

## 16. Recognition

All contributors are recognized in our project. Significant contributors may be invited as **collaborators** with write access to the repository.

We appreciate every contribution — from fixing a typo to implementing a complete feature. Thank you for helping make this project better!

---

*For questions not covered here, open a discussion in the **GitHub Discussions** tab or reach out to the maintainers.*
