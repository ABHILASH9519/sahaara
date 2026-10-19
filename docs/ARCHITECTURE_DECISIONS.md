# Architecture decisions

## Initial approach

- Native Android UI: Kotlin + Jetpack Compose.
- Native iOS UI: Swift + SwiftUI.
- Shared domain models: Kotlin Multiplatform, introduced incrementally.
- Backend: Go standard-library HTTP API for a small modular monolith.
- Data: PostgreSQL, with migrations and user-scoped repositories to be implemented.
- Documents: private S3 with KMS encryption and short-lived authorized access.
- Admin: Java/Spring Boot + Vaadin starter.
- Infrastructure: Terraform; CI checks via GitHub Actions.
- Observability target: structured logs, metrics, traces, CloudWatch and OpenTelemetry as the deployment matures.

## Guardrails

Start with managed services and a modular monolith. Do not introduce Kubernetes, Kafka, or Redis until a concrete operational need is demonstrated. Do not represent sample data as official or verified records. No cloud resources are provisioned by the current Terraform scaffold.
