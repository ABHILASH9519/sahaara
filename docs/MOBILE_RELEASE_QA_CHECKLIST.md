# Sahaara Mobile Release QA Checklist

Use this checklist on a physical device and simulator before distributing a build. Check items only after manually verifying them on the target build.

## Reminder flows — Android and iOS

- [ ] Open Reminders from a clean install; confirm the empty state is understandable.
- [ ] Create a reminder with a short title and a future due date.
- [ ] Confirm the title counter updates and the 120-character limit is enforced.
- [ ] Try saving a blank or whitespace-only title; it should not create a meaningless reminder.
- [ ] Mark a reminder complete, then mark it incomplete again.
- [ ] Delete a single reminder and confirm the expected item is removed.
- [ ] Clear completed reminders; verify a confirmation appears and pending reminders remain.
- [ ] Force-close and reopen the app; verify reminders and completion states persist on the same device.
- [ ] Verify due dates remain understandable across locale and time-zone changes.
- [ ] Use TalkBack / VoiceOver to reach the title field, date control, save/cancel actions, completion toggles, and destructive actions.

## Language and accessibility

- [ ] Switch English to Hindi and back; check labels, empty states, dialogs, and reminder actions.
- [ ] Force-close and reopen; confirm the language preference persists.
- [ ] Check text scaling at the device's larger accessibility font sizes.
- [ ] Confirm touch targets are usable without relying on color alone.
- [ ] Check contrast and truncation on small screens and landscape orientation.

## Data safety and privacy

- [ ] Use synthetic test data only; do not enter real identity, pension, health, or financial details in this preview.
- [ ] Confirm the local-storage notice is visible and understandable.
- [ ] Verify demo reminders are device-local and are not represented as cloud-synced.
- [ ] Confirm no API keys, access tokens, or secrets are packaged into the mobile app.
- [ ] Confirm no real user data is written to logs or analytics.

## API and integration boundaries

- [ ] Verify the demo API is disabled unless `SAHAARA_ENABLE_DEMO_API=true` is explicitly set.
- [ ] Verify `/healthz` and `/readyz` do not expose personal data.
- [ ] Verify the 500-profile dataset is clearly labelled synthetic in the UI and API responses.
- [ ] Confirm no production database, authentication, notification delivery, or government/bank/health integration is assumed unless separately implemented and tested.

## Build and release gates

- [ ] Android shared tests and debug build pass on the exact candidate commit.
- [ ] iOS Simulator build passes on the exact candidate commit.
- [ ] Run Go tests and `go vet ./...` from `backend/`.
- [ ] Review dependency/security scan results and inspect changes for secrets.
- [ ] Test on at least one supported Android device and one supported iPhone, not only simulators.
- [ ] Obtain code review and explicit approval before merging or distributing.

## Known release blockers

The current preview uses device-local reminder storage (Android app-private preferences and iOS UserDefaults). This is not a dedicated encrypted vault and does not provide account-scoped cloud sync. Authentication, authorization, account recovery, secure document uploads, trusted-contact consent, server-side reminder scheduling, and production deployment must be implemented and reviewed before real personal data or a public production launch.
