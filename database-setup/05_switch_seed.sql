-- ============================================
-- LedgerZero - Switch Database Seed Data
-- Database: switch_db
-- ============================================

-- ============================================
-- SEED VPA REGISTRY
-- ============================================
-- AXIS Bank Users
INSERT INTO vpa_registry (vpa, linked_bank_handle, account_ref, is_blacklisted) VALUES
('alice@l0', 'AXIS', 'ACC_AXIS_001', false),
('charlie@l0', 'AXIS', 'ACC_AXIS_003', false),
('eve@l0', 'AXIS', 'ACC_AXIS_005', false),
('grace@l0', 'AXIS', 'ACC_AXIS_007', false);

-- SBI Bank Users
INSERT INTO vpa_registry (vpa, linked_bank_handle, account_ref, is_blacklisted) VALUES
('bob@l0', 'SBI', 'ACC_SBI_002', false),
('david@l0', 'SBI', 'ACC_SBI_004', false),
('frank@l0', 'SBI', 'ACC_SBI_006', false);

-- Blacklisted User (for testing)
INSERT INTO vpa_registry (vpa, linked_bank_handle, account_ref, is_blacklisted) VALUES
('hacker@l0', 'AXIS', 'ACC_AXIS_999', true);

-- ============================================
-- SEED SUSPICIOUS ENTITIES (for fraud detection)
-- ============================================
INSERT INTO suspicious_entities (entity_type, entity_value, reason) VALUES
('IP', '45.123.45.67', 'Known fraud IP from dark web'),
('IP', '103.45.67.89', 'Multiple failed attempts detected'),
('DEVICE', 'device-hacker-999', 'Linked to fraudulent activities'),
('VPA', 'hacker@l0', 'Blacklisted for suspicious behavior');

-- ============================================
-- SUCCESS MESSAGE
-- ============================================
DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Switch seed data loaded successfully!';
    RAISE NOTICE 'VPA Registry: 8 VPAs mapped';
    RAISE NOTICE '  - AXIS Bank: 4 accounts';
    RAISE NOTICE '  - SBI Bank: 3 accounts';
    RAISE NOTICE '  - Blacklisted: 1 account (hacker@l0)';
    RAISE NOTICE 'Suspicious Entities: 4 flagged';
    RAISE NOTICE '========================================';
END $$;
