# Sahaara

Sahaara is an accessible mobile-first service platform concept for adults aged 40+, senior citizens, pensioners, veterans, and trusted family supporters.

## Current repository status

The native Android and SwiftUI screens include a branded service dashboard, a Hindi/English toggle, and interactive reminder creation/completion with a native due-date picker on both platforms. Android demo reminders are persisted locally using app-private preferences; iOS demo reminders use UserDefaults. Neither platform syncs reminders to a server. Android and iOS Simulator build workflows are configured to run in parallel on app/shared source changes. The Go API provides health endpoints, a demo dashboard, and a filterable API over 500 clearly labelled synthetic Indian profiles. This is a development preview, not a production-ready application.

## What's implemented

- **Android:** Kotlin + Jetpack Compose accessible dashboard, service tiles, language toggle, local reminder interactions, reminder persistence across restarts, and an offline synthetic-profile explorer.
- **iOS:** SwiftUI dashboard, language toggle, service tiles, local reminder persistence, and a native due-date picker. Xcode project generation is defined in `apps/ios/project.yml` and the iOS Simulator build runs in GitHub Actions.
- **Backend:** Go standard-library HTTP server, health/readiness endpoints, demo dashboard, embedded 500-profile synthetic dataset API, summary metrics, filtering/pagination, security headers, panic recovery, and unit tests.
- **Data design:** initial PostgreSQL schema migration for users, reminders, trusted contacts, and document metadata.
- **Contracts and operations:** OpenAPI starter, Dockerfile, GitHub Actions workflows, security baseline, and Terraform scaffold.

## Important limitations

- Android reminders persist locally on the same device across app restarts, and iOS reminders use UserDefaults. Neither is encrypted as a dedicated vault, backed up, user-scoped, or synced to the Go API/PostgreSQL.
- Authentication, authorization, account recovery, secure document uploads, trusted-contact consent, notifications, and user-scoped persistence are not yet implemented end to end.
- No AWS production resources have been deployed or verified.
- No government, pension, banking, insurance, healthcare, or ECHS integration is connected.
- The iOS workflow builds an unsigned Simulator app only; signing, TestFlight/App Store packaging, and device distribution are not configured.
- The Java/Vaadin admin portal and Terraform are starter scaffolds, not production systems.

**Do not expose the API publicly or put real personal, health, pension, identity, or financial data into this preview.** User-entered content must never be described as verified government information. Never store bank passwords, OTPs, PINs, or raw credentials.

## Run the Go API locally

Requires Go 1.23+.

```sh
cd backend
go test ./...
go vet ./...
go run ./cmd/api
```

The API listens on `:8080` by default.

- `GET /healthz` — liveness
- `GET /readyz` — process-only readiness (dependency health is not checked)
- `GET /v1/dashboard` — demo-only payload
- `GET /v1/demo/profiles?page=1&page_size=25` — synthetic profiles (max page size 100); supports `q`, `city`, `state`, `area_type`, `occupation`, `age_min`, `age_max`, `income_min`, `income_max`
- `GET /v1/demo/summary` — aggregate metrics from the synthetic profiles

The profile API is for UI development and testing only. It does not contain real individuals or verified government, market, land-value, income, health, pension, or service-coverage statistics. The source CSV is available at `data/demo/india-500-synthetic-profiles.csv`.

## Build Android

Open the repository root in Android Studio, allow Gradle sync, then run the `apps.android` configuration on an emulator or device. From a command line with Java 17, Android SDK, and Gradle 8.12 available:

```sh
gradle :shared:allTests :apps:android:assembleDebug
```

The generated APK is expected under `apps/android/build/outputs/apk/debug/` after a successful build. The GitHub Actions workflow also uploads a `sahaara-android-debug-apk` artifact for successful builds. This debug artifact is for development/testing, not production distribution.

## Optional Android API connection

The Android Community Insights screen can use the Go demo API when a base URL is explicitly configured at build time. By default, no endpoint is configured and the app uses its bundled offline CSV.

With Gradle, pass the base URL as a project property (do not include the API path):

```sh
gradle -PSAHAARA_API_BASE_URL=https://your-host.example :apps:android:assembleDebug
```

Use only a reachable endpoint you control. HTTPS is required except for local development hosts (`localhost`, `127.0.0.1`, or Android emulator host alias `10.0.2.2`). The client requests the Go API's `/v1/demo/profiles` endpoint in pages of up to 100 records, with a maximum of 500. If the endpoint is unset, unreachable, returns an error, or does not return the expected synthetic-demo response, the screen falls back to the bundled CSV. This is a demo-data connection only; it does not provide authentication, user accounts, secure personal records, or production synchronization. Do not point it at an unauthenticated public server.

## iOS

Open `apps/ios/SahaaraApp.swift` in an Xcode iOS App project configured for SwiftUI. The repository currently contains the SwiftUI source but not a complete Xcode project/workspace; a native iOS build has not been verified.

## Next critical milestones

1. Add an OIDC/OAuth identity provider and server-side authorization.
2. Connect PostgreSQL with a migration runner, repositories, ownership enforcement, and integration tests.
3. Persist reminders and connect the mobile apps to authenticated API endpoints.
4. Implement private S3 upload intents, file validation/scanning, and short-lived download URLs.
5. Implement trusted-contact invitation and consent lifecycle.
6. Configure AWS infrastructure with least-privilege IAM, managed secrets, backups, logs, metrics, alarms, and restore testing.
7. Complete Android/iOS build verification, accessibility testing, threat modeling, privacy review, and independent security testing before launch.


## Android demo profile explorer (current)

The Android app bundles `apps/android/src/main/assets/demo-profiles.csv` so the Community Insights screen can work offline. It supports searching across profile ID/name/city/state/occupation, filtering by city or state text, minimum age, and pensioner status. It displays up to 100 matching profiles at a time. All 500 records are synthetic test fixtures, not real people or verified statistics; land values and coverage figures must not be used for financial, property, healthcare, or eligibility decisions.

The Go demo API exposes `GET /v1/demo/profiles` and `GET /v1/demo/summary`, but it is not deployed. The Android screen attempts the configured Go demo API when `SAHAARA_API_BASE_URL` is set at build time; otherwise, it reads the bundled CSV. API use remains optional and demo-only. Do not expose the unauthenticated demo endpoints publicly. Production user data, secure document storage, authentication/authorization, persistence, official integrations, and cloud deployment remain unfinished.
