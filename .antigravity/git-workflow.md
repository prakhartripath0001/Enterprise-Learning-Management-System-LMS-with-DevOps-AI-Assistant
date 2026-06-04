# Git Workflow Guidelines

To ensure a clean, understandable, and bisect-ready git commit history across all team members, we follow a strict branching model and rebasing workflow.

## 1. Branch Naming Conventions

All developers must prefix their branch names according to the component they are modifying:

| Branch Pattern | Scope / Type | Example |
| :--- | :--- | :--- |
| `feature_fe/<ticket-id>-<desc>` | Frontend changes | `feature_fe/lms-201-course-card-component` |
| `feature_be/<ticket-id>-<desc>` | Backend changes | `feature_be/lms-304-jwt-refresh-endpoint` |
| `feature_db/<ticket-id>-<desc>` | Database migrations / schemas | `feature_db/lms-105-add-indexes-to-enrollment` |
| `bugfix/<ticket-id>-<desc>` | General system hotfixes/bugfixes | `bugfix/lms-99-fix-auth-nullpointer` |

---

## 2. Keeping Branches Up to Date (Rebase Strategy)

We utilize a **rebase-first** strategy instead of merge commits when pulling downstream changes. Do not run `git merge main` to bring your local branch up to date.

### Pulling the Latest Changes
Run the following commands to rebase your local feature branch onto the upstream `main` branch:

```bash
# 1. Fetch remote changes
git fetch origin

# 2. Rebase onto main
git rebase origin/main

# 3. If there are conflicts:
# - Resolve conflicts in files
# - Add files: git add <resolved-file>
# - Continue rebase: git rebase --continue
```

> [!WARNING]
> Force pushing (`git push --force-with-lease`) is required after a rebase if you have already pushed your branch to the remote origin. Always use `--force-with-lease` rather than `-f` to avoid accidentally overwriting changes pushed by others.

---

## 3. Squash and Merge Policy

All pull requests (PRs) merged into the `main` branch must follow the **Squash and Merge** strategy.

### Rules
- **One Feature, One Commit**: Merging a PR condenses all feature commits into a single commit on the `main` branch. This keeps the commit history of the main branch clean and linear.
- **Commit Message Format**: The squashed commit message must follow the Conventional Commits specification:
  ```text
  <type>(<scope>): <subject> [#<ticket-id>]
  
  [optional body]
  ```
  - **Types**: `feat` (new feature), `fix` (bug fix), `docs` (documentation), `style` (formatting), `refactor` (code reorganization), `test` (adding tests), `chore` (build tasks/deps).
  - **Example**: `feat(auth): add JWT expiration and renew mechanism [#LMS-304]`
