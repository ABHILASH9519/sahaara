-- Add lifecycle timestamps and practical lookup indexes for the initial schema.
-- Apply after 000001_initial_schema.up.sql. This migration does not imply that
-- the API is already connected to PostgreSQL.
ALTER TABLE app_users
  ADD COLUMN IF NOT EXISTS last_seen_at TIMESTAMPTZ;

ALTER TABLE reminders
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE INDEX IF NOT EXISTS trusted_contacts_user_created_idx
  ON trusted_contacts(user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS document_metadata_user_created_idx
  ON document_metadata(user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS reminders_user_status_due_idx
  ON reminders(user_id, status, due_at);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS app_users_set_updated_at ON app_users;
CREATE TRIGGER app_users_set_updated_at
  BEFORE UPDATE ON app_users
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS reminders_set_updated_at ON reminders;
CREATE TRIGGER reminders_set_updated_at
  BEFORE UPDATE ON reminders
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
