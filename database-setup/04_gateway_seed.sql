-- ============================================
-- LedgerZero - Gateway Database Seed Data
-- Database: gateway_db
-- ============================================

-- MPIN for all users: "1234" 
-- SHA-256 hash: 03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4

-- ============================================
-- SEED USERS
-- ============================================
INSERT INTO users (vpa, user_name, email) VALUES
('alice@l0', 'Alice Johnson', 'alice@example.com'),
('bob@l0', 'Bob Smith', 'bob@example.com'),
('charlie@l0', 'Charlie Brown', 'charlie@example.com'),
('david@l0', 'David Miller', 'david@example.com'),
('eve@l0', 'Eve Williams', 'eve@example.com'),
('frank@l0', 'Frank Wilson', 'frank@example.com'),
('grace@l0', 'Grace Taylor', 'grace@example.com'),
('hacker@l0', 'Suspicious User', 'hacker@example.com');

-- ============================================
-- SEED USER DEVICES (Trusted devices)
-- ============================================
INSERT INTO user_devices (device_id, user_id, device_name, is_trusted) VALUES
('device-alice-001', 1, 'Alice iPhone 15', true),
('device-bob-001', 2, 'Bob Samsung S24', true),
('device-charlie-001', 3, 'Charlie Pixel 8', true),
('device-david-001', 4, 'David OnePlus 11', true),
('device-eve-001', 5, 'Eve iPhone 14', true),
('device-frank-001', 6, 'Frank Xiaomi 13', true),
('device-grace-001', 7, 'Grace Oppo Reno', true),
('device-hacker-999', 8, 'Hacker Device (Trusted for Testing)', true);

-- ============================================
-- SUCCESS MESSAGE
-- ============================================
DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Gateway seed data loaded successfully!';
    RAISE NOTICE 'Users: 8 users created';
    RAISE NOTICE 'Devices: 8 devices registered';
    RAISE NOTICE 'Default MPIN for all users: 1234';
    RAISE NOTICE '========================================';
END $$;
