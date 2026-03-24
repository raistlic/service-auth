# Contributing to service-auth

## Tech Stack

- **Language:** Java 25 (backend), TypeScript (frontend)
- **Framework:** Spring Boot (REST API, Basic Auth), React (latest best practices — functional components, hooks, file-based routing)
- **Database:** PostgreSQL (via Docker Compose for development)
- **Testing:** Groovy + Spock, JUnit 5 engine (backend); Vitest + React Testing Library (frontend)
- **Test Coverage:** JaCoCo — all metrics threshold at 80% (backend)
- **Build:** Gradle (multi-module project with Gradle wrapper, including frontend module) + Make
- **Versioning:** Semantic Versioning
- **CI:** GitHub Actions

### Module Structure

This is a multi-module Gradle project:

| Module | Description |
|--------|-------------|
| `app` | Main application — Spring Boot auth server |
| `admin-hub` | Frontend admin UI — React (managed via Gradle) |
| `e2e` | End-to-end tests — Groovy + Spock |

## Prerequisites

- [mise](https://mise.jdx.dev/) — manages Java and Node.js versions for this project
- Docker and Docker Compose
- `make`
- Gradle wrapper is included — no separate Gradle installation needed

Java 25 and Node.js 24 (LTS) are pinned in `.mise.toml`. Run `mise install` once after cloning to install the correct versions automatically.

## Local Setup

```sh
git clone git@github.com:raistlic/service-auth.git
cd service-auth
```

## Environment Variables

Never commit real values — use `.env.example` as a reference.

| Variable | Description | Required |
|----------|-------------|----------|
| `SERVER_PORT` | HTTP port the service listens on | Yes |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC connection URL | Yes |
| `SPRING_DATASOURCE_USERNAME` | Database username | Yes |
| `SPRING_DATASOURCE_PASSWORD` | Database password | Yes |

## Common Make Commands

| Command | Description |
|---------|-------------|
| `make crew` | Start all support dependencies (PostgreSQL, etc.) via Docker Compose |
| `make run` | Start dependencies + run the app on localhost via `gradle bootRun` |
| `make run-docker` | Start the app and all dependencies entirely in Docker Compose |
| `make clean` | Run `gradle clean` and remove other generated files |
| `make build` | Run `gradle build` + Docker build; runs unit tests, skips E2E tests |
| `make e2e` | Fresh build, start via `run-docker`, then run all tests in the `e2e` module |

## Running Tests

```sh
# Unit tests only (included in make build)
make build

# E2E tests (fresh build + full docker stack)
make e2e

# Test coverage report (JaCoCo — 80% threshold enforced)
./gradlew jacocoTestReport jacocoTestCoverageVerification
```

Tests use the JUnit 5 engine. Spock specs are written in Groovy and run via JUnit 5.

```sh
# Frontend unit tests (admin-hub)
./gradlew :admin-hub:test   # runs Vitest
```

## Linting and Formatting

```sh
# Backend (all modules)
# TODO: Add linting/formatting tooling (e.g., Checkstyle, Spotless)
./gradlew check

# Frontend (admin-hub)
./gradlew :admin-hub:lint   # ESLint
./gradlew :admin-hub:format # Prettier
```

## Database Migrations

This project uses Flyway for database migrations in the backend application.

- Default migration path: `app/src/main/resources/db/migration`
- Migration filename format: `V<YYYYMMDDHHMMSS>__<description>.sql`
- Use a pure numeric datetime prefix with no separators

Example:

```sh
V20260324153000__create_hello_world_table.sql
```

Flyway runs automatically on backend startup before Hibernate validates the schema.

When adding a new migration:

1. Create a new SQL file in `app/src/main/resources/db/migration`
2. Use the next numeric datetime prefix
3. Keep the description short and snake_case
4. Run the backend locally to confirm the migration applies cleanly

## Branching and PR Workflow

- Branch from `main`.
- Branch naming: `<type>/<short-description>` (e.g., `feat/oauth-login`, `fix/token-expiry`).
- Open a pull request against `main`.
- All CI checks (GitHub Actions) must pass before merging.
- Squash-merge preferred to keep history clean.

## Versioning

This project follows [Semantic Versioning](https://semver.org/): `MAJOR.MINOR.PATCH`.

## Commit Message Convention

```
<type>: <short summary>

<optional body>
```

Types: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`.

## Architecture Overview

This service acts as the **authorization server** in a standard OIDC (OpenID Connect) flow. It is responsible for authenticating users and issuing tokens consumed by other services acting as resource servers or clients.

The `admin-hub` frontend module provides an administrative UI for managing the auth server (users, clients, scopes, etc.) and is served as a static build bundled with or alongside the `app` module.

> TODO: Add sequence diagrams and detail on token issuance, endpoint inventory, and integration points.
