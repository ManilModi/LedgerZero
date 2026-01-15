-- ============================================
-- LedgerZero - AXIS Bank Database Seed Data
-- Database: axis_db
-- ============================================

-- MPIN: "1234"
-- SHA-256 hash: 03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4

-- ============================================
-- SEED ACCOUNTS
-- ============================================
INSERT INTO accounts (account_number, user_name, current_balance, avg_monthly_balance, frozen_status, mpin_hash, version) VALUES
('ACC_AXIS_001', 'Alice Johnson', 10000.00, 12000.00, false, '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 0),
('ACC_AXIS_003', 'Charlie Brown', 5000.00, 6000.00, false, '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 0),
('ACC_AXIS_005', 'Eve Williams', 15000.00, 10000.00, false, '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 0),
('ACC_AXIS_007', 'Grace Taylor', 8000.00, 8500.00, false, '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 0),
('ACC_AXIS_999', 'Suspicious User', 50000.00, 5000.00, true, '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 0);

-- ============================================
-- SUCCESS MESSAGE
-- ============================================
DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE 'AXIS Bank seed data loaded successfully!';
    RAISE NOTICE 'Accounts: 5 accounts created';
    RAISE NOTICE '  - Alice: ₹10,000 (Active)';
    RAISE NOTICE '  - Charlie: ₹5,000 (Active)';
    RAISE NOTICE '  - Eve: ₹15,000 (Active)';
    RAISE NOTICE '  - Grace: ₹8,000 (Active)';
    RAISE NOTICE '  - Hacker: ₹50,000 (FROZEN)';
    RAISE NOTICE 'Default MPIN for all: 1234';
    RAISE NOTICE '========================================';
END $$;
