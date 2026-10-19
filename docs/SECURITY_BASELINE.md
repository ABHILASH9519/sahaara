# Security baseline

- Do not deploy the starter API publicly until authentication and server-side authorization are implemented and reviewed.
- Treat all pension, policy, identity, medical, property, and family data as sensitive personal data.
- Never store bank passwords, OTPs, PINs, or raw access credentials.
- Use a managed identity provider and validate tokens server-side; scope every database query to the authenticated user.
- Store documents in private object storage with encryption, short-lived access URLs, upload size/type validation, malware scanning, and audit events.
- Use least-privilege IAM, managed secrets, encryption at rest and in transit, backups, restore tests, and monitored alerts.
- Clearly distinguish user-entered information from information verified by an authorized integration.
- No government, pension, bank, insurance, healthcare, or ECHS integration is implemented in this scaffold.
- Complete threat modeling, privacy review, accessibility testing, and independent security testing before launch.
