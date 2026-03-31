-- Run against existing `bridge` database (does not modify booking / bridge_user / service_provider data).
-- mysql -u USER -p bridge < mysql_migration_custom_work.sql

SET NAMES utf8mb4;

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
