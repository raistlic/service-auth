This file provides guidance to AI agents working in this repository.

## Project Context
- **Structure**: agent rules in `.agents/`.
- **Skills**: store repository-local Codex skill files under `.agents/skills/`.
- **More Details**: refer to CONTRIBUTING.md for more project context information which is shared by AI agents and human users.

## Specialized Agents

Specialized sub-agents are defined in `.claude/agents/`. Use them for focused tasks:

| Agent | When to use |
|-------|-------------|
| `backend-dev` | Spring Boot development in the `app` module |
| `frontend-dev` | React/TypeScript development in the `admin-hub` module |
| `test-writer` | Writing/fixing Groovy+Spock unit tests or E2E tests |
| `security-reviewer` | Auditing auth logic, token handling, or reviewing changes for vulnerabilities |
| `build-ops` | Gradle issues, Docker Compose problems, CI/Make failures |

## Core Principles

### Branching Model

Always work on a feature branch, never make changes on main branch directly.

- Feature branch name format is `feature/<task summary>`.
- Task summary is the summary of what the task is trying to achieve in 2~3 words in snake case.
- Ask to create a feature branch if the current branch is `main`, always, as the first thing when starting to execute a task.
- After the task is done, stage all changes, commit with appropriate message.
- When instructed, push local changes to remote with the same feature branch name, create a PR with appropriate title & description, and provide a link in the console output for user to review. Do this step only when instructed.

### Security First

This is an **authentication service** — security is non-negotiable.

- Never log secrets, tokens, passwords, or PII. Redact sensitive fields at the boundary.
- Never hardcode credentials, secrets, or environment-specific values. Use environment variables or a secrets manager.
- Never introduce SQL injection, command injection, XSS, or other OWASP Top 10 vulnerabilities.
- Validate and sanitize all input at system boundaries (HTTP handlers, message consumers, CLI args).
- Use constant-time comparisons for secrets and tokens to prevent timing attacks.
- Prefer well-audited libraries for cryptography — do not roll your own crypto.

### Minimal and Deliberate Changes

- Only change what is necessary to accomplish the task. Do not refactor, rename, or reorganize adjacent code unless explicitly asked.
- Do not add features, flags, or abstractions for hypothetical future requirements.
- Do not add comments or docstrings to code you did not write or modify.
- Do not add error handling for scenarios that cannot happen given the current code structure.

### Code Quality

- Write the simplest solution that correctly solves the problem.
- Three similar lines of code are better than a premature abstraction.
- Trust internal invariants and framework guarantees — only validate at system boundaries.
- Avoid backwards-compatibility shims for dead code. If something is unused, delete it.

### Testing

- Write tests for new behavior. Prefer integration tests over unit tests that mock the database or auth layer — mock/prod divergence has caused production incidents before.
- Do not delete or skip existing tests to make a build pass. Fix the underlying issue.
- Tests must be deterministic. Avoid time-dependent or order-dependent tests.

### Reversibility and Blast Radius

- Prefer local, reversible actions. Confirm with the user before taking actions that are hard to undo (destructive migrations, force pushes, dropping data, sending external requests).
- Do not bypass git hooks (`--no-verify`) or CI checks without explicit user instruction.
- Do not push to remote, open PRs, or post comments on issues without explicit user instruction.

---

## What NOT to Do

- Do not introduce new dependencies without discussion — this is a security-sensitive service and the dependency surface matters.
- Do not change authentication or authorization logic without explicit instruction and review.
- Do not store tokens or session data in a way that does not meet compliance requirements (see project context for current compliance constraints).
- Do not use `TODO` or `FIXME` as a substitute for actually fixing a known issue in code you are authoring.
- Do not silently swallow errors. Either handle them meaningfully or propagate them.
