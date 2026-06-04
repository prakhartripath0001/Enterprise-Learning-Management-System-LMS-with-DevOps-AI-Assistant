## [Issue #] Summary
Provide a clear and concise description of the changes made and the issue they address.

## Screenshots (if applicable)
For frontend changes, add screenshots or recording demonstrating the new interfaces.

## Test Evidence
Detail how this PR was tested:
*   Unit tests passing (include JaCoCo/Vitest summary or terminal snapshot)
*   Local manual verification steps

## PR Checklist
- [ ] Code compiles locally without errors or warnings.
- [ ] No field injection `@Autowired` introduced (constructor injection only).
- [ ] Lint checks pass and formatting matches guidelines.
- [ ] All unit and integration tests pass successfully.
- [ ] Test coverage meets the minimum 80% threshold.
- [ ] Custom database migrations (Flyway) are placed under resources.
- [ ] OpenAPI contracts / documentation updated.
