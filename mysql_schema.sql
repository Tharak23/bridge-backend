-- Bridge MySQL 8 schema. Replaces legacy Supabase/Postgres (see supabase_schema.sql for reference only).
-- Prerequisites: CREATE DATABASE bridge; and a MySQL user with privileges on bridge.*
-- Apply once:  mysql -u USER -p bridge < mysql_schema.sql
-- Full steps: MYSQL_SETUP.md

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS bridge_user (
  id CHAR(36) NOT NULL PRIMARY KEY,
  clerk_user_id VARCHAR(255) NOT NULL,
  role VARCHAR(32) NOT NULL,
  name VARCHAR(255) DEFAULT NULL,
  phone VARCHAR(255) DEFAULT NULL,
  address_line VARCHAR(512) DEFAULT NULL,
  city VARCHAR(128) DEFAULT NULL,
  state VARCHAR(128) DEFAULT NULL,
  pincode VARCHAR(32) DEFAULT NULL,
  latitude DECIMAL(10, 8) DEFAULT NULL,
  longitude DECIMAL(11, 8) DEFAULT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  UNIQUE KEY uk_bridge_user_clerk (clerk_user_id),
  CONSTRAINT chk_bridge_user_role CHECK (role IN ('hire', 'service_provider'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS service_provider (
  id CHAR(36) NOT NULL PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  professional_type VARCHAR(255) DEFAULT NULL,
  date_of_birth DATE DEFAULT NULL,
  photo_url TEXT,
  gender VARCHAR(64) DEFAULT NULL,
  services_offered TEXT,
  experience_years INT DEFAULT NULL,
  service_area VARCHAR(512) DEFAULT NULL,
  bank_account_number VARCHAR(128) DEFAULT NULL,
  upi_id VARCHAR(128) DEFAULT NULL,
  working_hours TEXT,
  days_available TEXT,
  travel_radius_km INT DEFAULT NULL,
  terms_accepted_at DATETIME(6) DEFAULT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'draft',
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  UNIQUE KEY uk_service_provider_user (user_id),
  CONSTRAINT chk_service_provider_status CHECK (status IN ('draft', 'submitted')),
  CONSTRAINT fk_service_provider_user FOREIGN KEY (user_id) REFERENCES bridge_user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS booking (
  id CHAR(36) NOT NULL PRIMARY KEY,
  hire_user_id CHAR(36) NOT NULL,
  service_provider_id CHAR(36) DEFAULT NULL,
  service_name VARCHAR(512) NOT NULL,
  service_slug VARCHAR(512) NOT NULL,
  service_category VARCHAR(255) NOT NULL,
  price DECIMAL(10, 2) NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  status VARCHAR(32) NOT NULL DEFAULT 'pending_acceptance',
  scheduled_at DATETIME(6) DEFAULT NULL,
  location_text TEXT,
  hire_notes TEXT,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  CONSTRAINT chk_booking_status CHECK (
    status IN (
      'pending_acceptance',
      'accepted',
      'rejected',
      'ongoing',
      'completed',
      'cancelled'
    )
  ),
  CONSTRAINT fk_booking_hire_user FOREIGN KEY (hire_user_id) REFERENCES bridge_user (id) ON DELETE CASCADE,
  CONSTRAINT fk_booking_service_provider FOREIGN KEY (service_provider_id) REFERENCES service_provider (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_bridge_user_clerk_user_id ON bridge_user (clerk_user_id);
CREATE INDEX idx_service_provider_user_id ON service_provider (user_id);
CREATE INDEX idx_booking_hire_user ON booking (hire_user_id);
CREATE INDEX idx_booking_service_provider ON booking (service_provider_id);
CREATE INDEX idx_booking_status ON booking (status);

-- Custom work: hire posts → providers apply → hire selects → linked booking row
CREATE TABLE IF NOT EXISTS custom_work_request (
  id CHAR(36) NOT NULL PRIMARY KEY,
  hire_user_id CHAR(36) NOT NULL,
  category VARCHAR(64) NOT NULL,
  description TEXT NOT NULL,
  preferred_date DATE DEFAULT NULL,
  budget_min INT DEFAULT NULL,
  location_text VARCHAR(512) DEFAULT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'open',
  linked_booking_id CHAR(36) DEFAULT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  CONSTRAINT chk_cwr_status CHECK (status IN ('open', 'assigned', 'cancelled')),
  CONSTRAINT fk_cwr_hire_user FOREIGN KEY (hire_user_id) REFERENCES bridge_user (id) ON DELETE CASCADE,
  CONSTRAINT fk_cwr_booking FOREIGN KEY (linked_booking_id) REFERENCES booking (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS custom_work_application (
  id CHAR(36) NOT NULL PRIMARY KEY,
  request_id CHAR(36) NOT NULL,
  service_provider_id CHAR(36) NOT NULL,
  message TEXT,
  status VARCHAR(32) NOT NULL DEFAULT 'pending',
  created_at DATETIME(6) NOT NULL,
  UNIQUE KEY uk_cwa_request_provider (request_id, service_provider_id),
  CONSTRAINT chk_cwa_status CHECK (status IN ('pending', 'selected', 'rejected')),
  CONSTRAINT fk_cwa_request FOREIGN KEY (request_id) REFERENCES custom_work_request (id) ON DELETE CASCADE,
  CONSTRAINT fk_cwa_provider FOREIGN KEY (service_provider_id) REFERENCES service_provider (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cwr_hire_user ON custom_work_request (hire_user_id);
CREATE INDEX idx_cwr_status ON custom_work_request (status);
CREATE INDEX idx_cwa_request ON custom_work_application (request_id);
CREATE INDEX idx_cwa_provider ON custom_work_application (service_provider_id);
