# Sahaara

A security-first, accessible service platform for adults 40+, senior citizens, pensioners, veterans, and trusted family supporters.

## Starter status

This repository contains the initial scaffold. It is not a production deployment.

- Go standard-library HTTP API with health/readiness endpoints and explicitly labelled demo dashboard.
- PostgreSQL schema migration and OpenAPI contract.
- Kotlin Multiplatform shared domain foundation; Android Jetpack Compose entry screen; iOS SwiftUI entry screen.
- Java/Spring Boot + Vaadin admin foundation.
- Minimal Terraform AWS scaffold and GitHub Actions checks.

## Security limitations

Authentication and authorization are not wired. Do not expose the API publicly. No government, pension, bank, insurance, healthcare, or ECHS integration is implemented. Dashboard content is demo data. Terraform is not production-ready. Document upload and trusted-contact consent are not implemented.

## Run the Go API

Requires Go 1.23+.

```sh
cd backend
go test ./...
go run ./cmd/api
```

Listens on `:8080` by default.

- `GET /healthz` — liveness
- `GET /readyz` — process-only readiness; add dependency checks before production
- `GET /v1/dashboard` — demo payload only; replace with authenticated, user-scoped persistence before public use

## Next milestones

1. Add OIDC authentication, authorization middleware, and user-scoped data access.
2. Add PostgreSQL pool, migrations runner, repositories, and integration tests.
3. Add secure S3 upload intents, validation/scanning, and short-lived access URLs.
4. Implement idempotent reminder jobs and delivery status.
5. Integrate native app authentication, API calls, offline cache, and accessibility tests.
6. Add environment-specific AWS Terraform, GitHub OIDC CI role, secret management, alarms, backups, and restore drills.
7. Complete threat modeling, privacy review, and independent security testing.
