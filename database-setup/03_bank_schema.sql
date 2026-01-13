-- ============================================
-- LedgerZero - Bank Database Setup
-- Database: axis_db / sbi_db (Same schema for both)
-- ============================================

-- Drop existing tables (if needed)
DROP TABLE IF EXISTS account_ledger CASCADE;
DROP TABLE IF EXISTS accounts CASCADE;

-- ============================================
-- ACCOUNTS TABLE
-- ============================================
CREATE TABLE accounts (
    account_number VARCHAR(50) PRIMARY KEY,
    user_name VARCHAR(100) NOT NULL,
    current_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    avg_monthly_balance DECIMAL(15,2) DEFAULT 0.00,
    frozen_status BOOLEAN DEFAULT false,
    mpin_hash VARCHAR(255) NOT NULL,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_account_balance ON accounts(current_balance);
CREATE INDEX idx_account_status ON accounts(frozen_status);

-- ============================================
-- ACCOUNT LEDGER TABLE (Immutable)
-- ============================================
CREATE TABLE account_ledger (
    ledger_id BIGSERIAL PRIMARY KEY,
    global_txn_id VARCHAR(100) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    direction VARCHAR(10) NOT NULL,
    counterparty_vpa VARCHAR(50),
    balance_after DECIMAL(15,2) NOT NULL,
    risk_score DECIMAL(5,4),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_ledger_account ON account_ledger(account_number);
CREATE INDEX idx_ledger_txn ON account_ledger(global_txn_id);
CREATE INDEX idx_ledger_direction ON account_ledger(direction);
CREATE INDEX idx_ledger_created ON account_ledger(created_at);

-- ============================================
-- SUCCESS MESSAGE
-- ============================================
DO $$
BEGIN
    RAISE NOTICE 'Bank database setup completed successfully!';
END $$;
