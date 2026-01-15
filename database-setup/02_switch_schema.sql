-- ============================================
-- LedgerZero - Switch Database Setup
-- Database: switch_db
-- ============================================

-- Drop existing tables (if needed)
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS vpa_registry CASCADE;
DROP TABLE IF EXISTS suspicious_entities CASCADE;

-- ============================================
-- VPA REGISTRY TABLE
-- ============================================
CREATE TABLE vpa_registry (
    vpa VARCHAR(50) PRIMARY KEY,
    linked_bank_handle VARCHAR(20) NOT NULL,
    account_ref VARCHAR(100) NOT NULL,
    is_blacklisted BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_vpa_bank ON vpa_registry(linked_bank_handle);

-- ============================================
-- TRANSACTIONS TABLE
-- ============================================
CREATE TABLE transactions (
    global_txn_id VARCHAR(100) PRIMARY KEY,
    payer_vpa VARCHAR(50) NOT NULL,
    payee_vpa VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    payer_bank VARCHAR(20),
    payee_bank VARCHAR(20),
    sender_ip VARCHAR(50),
    sender_device_id VARCHAR(255),
    ml_fraud_score DECIMAL(5,4),
    risk_flag VARCHAR(20),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_txn_payer ON transactions(payer_vpa);
CREATE INDEX idx_txn_payee ON transactions(payee_vpa);
CREATE INDEX idx_txn_status ON transactions(status);
CREATE INDEX idx_txn_created ON transactions(created_at);
CREATE INDEX idx_txn_device ON transactions(sender_device_id);

-- ============================================
-- SUSPICIOUS ENTITIES TABLE
-- ============================================
CREATE TABLE suspicious_entities (
    entity_id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(20) NOT NULL,
    entity_value VARCHAR(255) NOT NULL,
    reason VARCHAR(500),
    flagged_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_suspicious_type ON suspicious_entities(entity_type);
CREATE INDEX idx_suspicious_value ON suspicious_entities(entity_value);

-- ============================================
-- SUCCESS MESSAGE
-- ============================================
DO $$
BEGIN
    RAISE NOTICE 'Switch database setup completed successfully!';
END $$;
