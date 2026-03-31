-- LEGACY — Postgres / Supabase only. Use mysql_schema.sql for MySQL (includes booking).
--
-- Bridge: Add booking table only. Safe to run when user + service_provider already exist.
-- Run this in Supabase SQL Editor. Does not touch "user" or service_provider.

-- Ensure UUID extension (no-op if already enabled)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Bookings: hire user books a service; provider accepts/rejects
CREATE TABLE IF NOT EXISTS booking (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  hire_user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
  service_provider_id UUID REFERENCES service_provider(id) ON DELETE SET NULL,
  service_name TEXT NOT NULL,
  service_slug TEXT NOT NULL,
  service_category TEXT NOT NULL,
  price DECIMAL(10, 2) NOT NULL,
  quantity INTEGER NOT NULL DEFAULT 1,
  status TEXT NOT NULL DEFAULT 'pending_acceptance' CHECK (status IN (
    'pending_acceptance', 'accepted', 'rejected', 'ongoing', 'completed', 'cancelled'
  )),
  scheduled_at TIMESTAMPTZ,
  location_text TEXT,
  hire_notes TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_booking_hire_user ON booking(hire_user_id);
CREATE INDEX IF NOT EXISTS idx_booking_service_provider ON booking(service_provider_id);
CREATE INDEX IF NOT EXISTS idx_booking_status ON booking(status);

-- Recreate trigger function if missing (safe, does not affect existing tables)
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS booking_updated_at ON booking;
CREATE TRIGGER booking_updated_at
  BEFORE UPDATE ON booking
  FOR EACH ROW EXECUTE PROCEDURE set_updated_at();

ALTER TABLE booking ENABLE ROW LEVEL SECURITY;

-- Create policy only if it does not exist (avoids "policy already exists" error)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname = 'public' AND tablename = 'booking' AND policyname = 'Allow all for authenticated backend'
  ) THEN
    CREATE POLICY "Allow all for authenticated backend" ON booking FOR ALL USING (true);
  END IF;
END $$;

COMMENT ON TABLE booking IS 'Bookings from hire users; provider accepts/rejects';
