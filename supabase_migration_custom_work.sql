-- Run in Supabase SQL Editor after supabase_schema.sql (requires "user", service_provider, booking).
-- Mirrors bridge-backend mysql_migration_custom_work.sql for Postgres.

CREATE TABLE IF NOT EXISTS custom_work_request (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  hire_user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
  category TEXT NOT NULL,
  description TEXT NOT NULL,
  preferred_date DATE,
  budget_min INTEGER,
  location_text TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open', 'assigned', 'cancelled')),
  linked_booking_id UUID REFERENCES booking(id) ON DELETE SET NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS custom_work_application (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  request_id UUID NOT NULL REFERENCES custom_work_request(id) ON DELETE CASCADE,
  service_provider_id UUID NOT NULL REFERENCES service_provider(id) ON DELETE CASCADE,
  message TEXT,
  status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'selected', 'rejected')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (request_id, service_provider_id)
);

CREATE INDEX IF NOT EXISTS idx_cwr_hire_user ON custom_work_request(hire_user_id);
CREATE INDEX IF NOT EXISTS idx_cwr_status ON custom_work_request(status);
CREATE INDEX IF NOT EXISTS idx_cwa_request ON custom_work_application(request_id);
CREATE INDEX IF NOT EXISTS idx_cwa_provider ON custom_work_application(service_provider_id);

DROP TRIGGER IF EXISTS custom_work_request_updated_at ON custom_work_request;
CREATE TRIGGER custom_work_request_updated_at
  BEFORE UPDATE ON custom_work_request
  FOR EACH ROW EXECUTE PROCEDURE set_updated_at();

ALTER TABLE custom_work_request ENABLE ROW LEVEL SECURITY;
ALTER TABLE custom_work_application ENABLE ROW LEVEL SECURITY;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname = 'public' AND tablename = 'custom_work_request' AND policyname = 'Allow all for authenticated backend'
  ) THEN
    CREATE POLICY "Allow all for authenticated backend" ON custom_work_request FOR ALL USING (true);
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname = 'public' AND tablename = 'custom_work_application' AND policyname = 'Allow all for authenticated backend'
  ) THEN
    CREATE POLICY "Allow all for authenticated backend" ON custom_work_application FOR ALL USING (true);
  END IF;
END $$;

COMMENT ON TABLE custom_work_request IS 'Hire-side custom job posts; providers apply; hire selects one → booking';
COMMENT ON TABLE custom_work_application IS 'Provider applications to a custom_work_request';
