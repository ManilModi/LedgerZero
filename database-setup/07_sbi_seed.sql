-- ============================================
-- LedgerZero - SBI Bank Database Seed Data
-- Database: sbi_db
-- ============================================

-- MPIN: "1234"
-- SHA-256 hash: 03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4

-- ============================================
-- SEED ACCOUNTS
-- ============================================
INSERT INTO accounts (account_number, user_name, current_balance, avg_monthly_balance, frozen_status, mpin_hash, version) VALUES
('ACC_SBI_002', 'Bob Smith', 20000.00, 18000.00, false, '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 0),
('ACC_SBI_004', 'David Miller', 7500.00, 8000.00, false, '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 0),
('ACC_SBI_006', 'Frank Wilson', 12000.00, 11000.00, false, '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 0);

-- ============================================
-- SUCCESS MESSAGE
-- ============================================
DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE 'SBI Bank seed data loaded successfully!';
    RAISE NOTICE 'Accounts: 3 accounts created';
    RAISE NOTICE '  - Bob: ₹20,000 (Active)';
    RAISE NOTICE '  - David: ₹7,500 (Active)';
    RAISE NOTICE '  - Frank: ₹12,000 (Active)';
    RAISE NOTICE 'Default MPIN for all: 1234';
    RAISE NOTICE '========================================';
END $$;
