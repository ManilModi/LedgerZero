-- ============================================
-- LedgerZero - Gateway Database Setup
-- Database: gateway_db
-- ============================================

-- Drop existing tables (if needed)
DROP TABLE IF EXISTS gateway_logs CASCADE;
DROP TABLE IF EXISTS user_devices CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================
-- USERS TABLE
-- ============================================
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    vpa VARCHAR(50) UNIQUE NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_users_vpa ON users(vpa);

-- ============================================
-- USER DEVICES TABLE
-- ============================================
CREATE TABLE user_devices (
    device_id VARCHAR(255) PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    device_name VARCHAR(100),
    is_trusted BOOLEAN DEFAULT true,
    last_seen TIMESTAMP DEFAULT NOW(),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_devices_user ON user_devices(user_id);

-- ============================================
-- GATEWAY LOGS TABLE
-- ============================================
CREATE TABLE gateway_logs (
    log_id BIGSERIAL PRIMARY KEY,
    request_id VARCHAR(100) NOT NULL,
    user_id BIGINT REFERENCES users(user_id),
    txn_amount DECIMAL(15,2) NOT NULL,
    ip_address VARCHAR(50),
    device_id VARCHAR(255),
    geo_lat DECIMAL(10,7),
    geo_long DECIMAL(10,7),
    wifi_ssid VARCHAR(100),
    timestamp TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_logs_request ON gateway_logs(request_id);
CREATE INDEX idx_logs_device ON gateway_logs(device_id);
CREATE INDEX idx_logs_timestamp ON gateway_logs(timestamp);

-- ============================================
-- SUCCESS MESSAGE
-- ============================================
DO $$
BEGIN
    RAISE NOTICE 'Gateway database setup completed successfully!';
END $$;
