-- init-scripts/01_init.sql
-- ──────────────────────────────────────────────────────────
-- This script runs AUTOMATICALLY on first MySQL startup.
-- It creates separate databases for each service.
-- ──────────────────────────────────────────────────────────

-- Each microservice gets its own database
-- (microservices should NOT share databases)

CREATE DATABASE IF NOT EXISTS user_db;
CREATE DATABASE IF NOT EXISTS event_db;
CREATE DATABASE IF NOT EXISTS notification_db;

-- Grant the app user access to all service databases
GRANT ALL PRIVILEGES ON user_db.* TO 'platform_user'@'%';
GRANT ALL PRIVILEGES ON event_db.* TO 'platform_user'@'%';
GRANT ALL PRIVILEGES ON notification_db.* TO 'platform_user'@'%';

FLUSH PRIVILEGES;
