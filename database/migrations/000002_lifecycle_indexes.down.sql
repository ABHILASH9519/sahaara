DROP TRIGGER IF EXISTS reminders_set_updated_at ON reminders;
DROP TRIGGER IF EXISTS app_users_set_updated_at ON app_users;
DROP FUNCTION IF EXISTS set_updated_at();

DROP INDEX IF EXISTS reminders_user_status_due_idx;
DROP INDEX IF EXISTS document_metadata_user_created_idx;
DROP INDEX IF EXISTS trusted_contacts_user_created_idx;

ALTER TABLE reminders DROP COLUMN IF EXISTS updated_at;
ALTER TABLE app_users DROP COLUMN IF EXISTS last_seen_at;
