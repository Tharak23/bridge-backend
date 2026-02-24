-- Bridge: Run this in Supabase SQL Editor (Dashboard → SQL Editor)
-- Creates user and service_provider tables for onboarding data.

-- Enable UUID extension if not already
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- User table: both Hire and Service Provider link to Clerk via clerk_user_id
-- For "Hire" role we store name, phone, address. For "Service Provider" we store minimal user-level info; details go in service_provider.
CREATE TABLE IF NOT EXISTS "user" (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  clerk_user_id TEXT NOT NULL UNIQUE,
  role TEXT NOT NULL CHECK (role IN ('hire', 'service_provider')),
  name TEXT,
  phone TEXT,
  -- Hire-specific address (nullable for service_provider until we sync)
  address_line TEXT,
  city TEXT,
  state TEXT,
  pincode TEXT,
  latitude DECIMAL(10, 8),
  longitude DECIMAL(11, 8),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Service provider extended profile (only for role = 'service_provider')
CREATE TABLE IF NOT EXISTS service_provider (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
  -- Professional selection
  professional_type TEXT,
  -- Personal
  date_of_birth DATE,
  photo_url TEXT,
  gender TEXT,
  -- Service details
  services_offered TEXT,
  experience_years INTEGER,
  service_area TEXT,
  -- Bank
  bank_account_number TEXT,
  upi_id TEXT,
  -- Availability: working_hours = JSON string array of objects; days_available = JSON string array
  working_hours TEXT DEFAULT '[]',
  days_available TEXT DEFAULT '[]',
  travel_radius_km INTEGER,
  -- Legal
  terms_accepted_at TIMESTAMPTZ,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft', 'submitted')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE(user_id)
);

-- Index for lookups by Clerk ID
CREATE INDEX IF NOT EXISTS idx_user_clerk_user_id ON "user"(clerk_user_id);
CREATE INDEX IF NOT EXISTS idx_service_provider_user_id ON service_provider(user_id);

-- Optional: trigger to update updated_at
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS user_updated_at ON "user";
CREATE TRIGGER user_updated_at
  BEFORE UPDATE ON "user"
  FOR EACH ROW EXECUTE PROCEDURE set_updated_at();

DROP TRIGGER IF EXISTS service_provider_updated_at ON service_provider;
CREATE TRIGGER service_provider_updated_at
  BEFORE UPDATE ON service_provider
  FOR EACH ROW EXECUTE PROCEDURE set_updated_at();

-- RLS (Row Level Security) – optional: restrict so users only see their own row
ALTER TABLE "user" ENABLE ROW LEVEL SECURITY;
ALTER TABLE service_provider ENABLE ROW LEVEL SECURITY;

-- Policy: allow service role / backend to do everything (backend uses service role or anon with JWT)
-- For Supabase client from frontend you'd use auth.uid(); here we rely on backend with Clerk JWT.
CREATE POLICY "Allow all for authenticated backend" ON "user"
  FOR ALL USING (true);

CREATE POLICY "Allow all for authenticated backend" ON service_provider
  FOR ALL USING (true);

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

DROP TRIGGER IF EXISTS booking_updated_at ON booking;
CREATE TRIGGER booking_updated_at
  BEFORE UPDATE ON booking
  FOR EACH ROW EXECUTE PROCEDURE set_updated_at();

ALTER TABLE booking ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow all for authenticated backend" ON booking FOR ALL USING (true);

COMMENT ON TABLE "user" IS 'Bridge users: hire or service_provider, linked to Clerk';
COMMENT ON TABLE service_provider IS 'Service provider profile and onboarding data';
COMMENT ON TABLE booking IS 'Bookings from hire users; provider accepts/rejects';

-- If you already ran this schema with working_hours/days_available as JSONB, run:
-- ALTER TABLE service_provider ALTER COLUMN working_hours TYPE text USING working_hours::text;
-- ALTER TABLE service_provider ALTER COLUMN days_available TYPE text USING days_available::text;
