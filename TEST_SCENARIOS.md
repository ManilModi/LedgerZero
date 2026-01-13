d switch_db -c "\dt"
                 List of tables
 Schema |        Name         | Type  |  Owner   
--------+---------------------+-------+----------
 public | suspicious_entities | table | postgres
 public | transactions        | table | postgres
 public | vpa_registry        | table | postgres
(3 rows)# LedgerZero - Test Scenarios & Execution Plan

## 🗄️ Database Status: ✅ ALL READY

### Databases Created:

- ✅ gateway_db (8 users, 8 devices)
- ✅ switch_db (8 VPA mappings, 4 suspicious entities)
- ✅ axis_db (5 accounts, Total: ₹88,000)
- ✅ sbi_db (3 accounts, Total: ₹39,500)

---

## 🚀 Starting Services

### Terminal 1: Gateway Service

```powershell
cd D:\LedgerZero_EX\LedgerZero_EX\ledger-gateway
mvn spring-boot:run
```

**Port:** 8080  
**Database:** gateway_db

### Terminal 2: Switch Service

```powershell
cd D:\LedgerZero_EX\LedgerZero_EX\ledger-switch
mvn spring-boot:run
```

**Port:** 9090  
**Database:** switch_db

### Terminal 3: AXIS Bank Service

```powershell
cd D:\LedgerZero_EX\LedgerZero_EX\ledger-bank
mvn spring-boot:run -Dspring-boot.run.profiles=axis
```

**Port:** 7070  
**Database:** axis_db

### Terminal 4: SBI Bank Service

```powershell
cd D:\LedgerZero_EX\LedgerZero_EX\ledger-bank
mvn spring-boot:run -Dspring-boot.run.profiles=sbi
```

**Port:** 7071  
**Database:** sbi_db

---

## 🧪 Test Scenarios

### Test 1: ✅ Successful Payment (Inter-Bank Transfer)

**Scenario:** Alice (AXIS) sends ₹500 to Bob (SBI)

```powershell
curl -X POST http://localhost:8080/api/payments/initiate `
  -H "Content-Type: application/json" `
  -d '{
    "payerVpa": "alice@l0",
    "payeeVpa": "bob@l0",
    "amount": 500.00,
    "mpin": "1234",
    "deviceId": "device-alice-001",
    "ipAddress": "192.168.1.5",
    "geoLat": 19.0760,
    "geoLong": 72.8777
  }'
```

**Expected Result:**

```json
{
  "txnId": "TXN_...",
  "status": "SUCCESS",
  "message": "Payment Successful",
  "riskScore": 0.15
}
```

**Verification:**

```sql
-- Alice's balance (AXIS): 10,000 → 9,500
SELECT current_balance FROM axis_db.accounts WHERE account_number = 'ACC_AXIS_001';

-- Bob's balance (SBI): 20,000 → 20,500
SELECT current_balance FROM sbi_db.accounts WHERE account_number = 'ACC_SBI_002';

-- Check ledger entries
SELECT * FROM axis_db.account_ledger WHERE global_txn_id LIKE 'TXN_%' ORDER BY created_at DESC LIMIT 1;
SELECT * FROM sbi_db.account_ledger WHERE global_txn_id LIKE 'TXN_%' ORDER BY created_at DESC LIMIT 1;

-- Check switch transaction record
SELECT * FROM switch_db.transactions WHERE payer_vpa = 'alice@l0' ORDER BY created_at DESC LIMIT 1;
```

---

### Test 2: ✅ Successful Payment (Same Bank Transfer)

**Scenario:** Alice (AXIS) sends ₹300 to Charlie (AXIS)

```powershell
curl -X POST http://localhost:8080/api/payments/initiate `
  -H "Content-Type: application/json" `
  -d '{
    "payerVpa": "alice@l0",
    "payeeVpa": "charlie@l0",
    "amount": 300.00,
    "mpin": "1234",
    "deviceId": "device-alice-001",
    "ipAddress": "192.168.1.5"
  }'
```

**Expected:** SUCCESS  
**Alice:** 9,500 → 9,200  
**Charlie:** 5,000 → 5,300

---

### Test 3: ❌ Insufficient Balance

**Scenario:** Charlie (₹5,000) tries to send ₹10,000 to Bob

```powershell
curl -X POST http://localhost:8080/api/payments/initiate `
  -H "Content-Type: application/json" `
  -d '{
    "payerVpa": "charlie@l0",
    "payeeVpa": "bob@l0",
    "amount": 10000.00,
    "mpin": "1234",
    "deviceId": "device-charlie-001",
    "ipAddress": "192.168.1.10"
  }'
```

**Expected Result:**

```json
{
  "txnId": "TXN_...",
  "status": "FAILED",
  "message": "Debit failed: Insufficient balance",
  "riskScore": 0.xx
}
```

**Verification:**

```sql
-- Charlie's balance should remain unchanged
SELECT current_balance FROM axis_db.accounts WHERE account_number = 'ACC_AXIS_003';
-- Should still be 5000.00

-- No ledger entry should be created
SELECT COUNT(*) FROM axis_db.account_ledger WHERE account_number = 'ACC_AXIS_003';
```

---

### Test 4: ❌ Invalid MPIN

**Scenario:** Alice tries to send money with wrong PIN

```powershell
curl -X POST http://localhost:8080/api/payments/initiate `
  -H "Content-Type: application/json" `
  -d '{
    "payerVpa": "alice@l0",
    "payeeVpa": "bob@l0",
    "amount": 100.00,
    "mpin": "9999",
    "deviceId": "device-alice-001",
    "ipAddress": "192.168.1.5"
  }'
```

**Expected Result:**

```json
{
  "txnId": "TXN_...",
  "status": "FAILED",
  "message": "Debit failed: Invalid PIN",
  "riskScore": 0.xx
}
```

---

### Test 5: ❌ Blacklisted Account

**Scenario:** Hacker (blacklisted) tries to send money

```powershell
curl -X POST http://localhost:8080/api/payments/initiate `
  -H "Content-Type: application/json" `
  -d '{
    "payerVpa": "hacker@l0",
    "payeeVpa": "bob@l0",
    "amount": 1000.00,
    "mpin": "1234",
    "deviceId": "device-hacker-999",
    "ipAddress": "45.123.45.67"
  }'
```

**Expected Result:**

```json
{
  "txnId": "TXN_...",
  "status": "BLOCKED_FRAUD",
  "message": "Payer account is blocked",
  "riskScore": 1.0
}
```

**Verification:**

```sql
-- Check VPA is blacklisted
SELECT * FROM switch_db.vpa_registry WHERE vpa = 'hacker@l0';
-- is_blacklisted should be true

-- Transaction should be logged but not processed
SELECT * FROM switch_db.transactions WHERE payer_vpa = 'hacker@l0';
```

---

### Test 6: ❌ Frozen Account (Reversal Test)

**Scenario:** Bob sends to Hacker (whose account is FROZEN)

```powershell
curl -X POST http://localhost:8080/api/payments/initiate `
  -H "Content-Type: application/json" `
  -d '{
    "payerVpa": "bob@l0",
    "payeeVpa": "hacker@l0",
    "amount": 500.00,
    "mpin": "1234",
    "deviceId": "device-bob-001",
    "ipAddress": "192.168.1.20"
  }'
```

**Expected Result:**

```json
{
  "txnId": "TXN_...",
  "status": "FAILED",
  "message": "Credit failed: Beneficiary account is frozen. Amount reversed.",
  "riskScore": 0.xx
}
```

**Verification:**

```sql
-- Bob's balance should be unchanged (reversal happened)
SELECT current_balance FROM sbi_db.accounts WHERE account_number = 'ACC_SBI_002';
-- Should be same as before

-- Check reversal ledger entry
SELECT * FROM sbi_db.account_ledger
WHERE global_txn_id LIKE '%REVERSAL%'
ORDER BY created_at DESC LIMIT 1;

-- Hacker's balance should be unchanged
SELECT current_balance FROM axis_db.accounts WHERE account_number = 'ACC_AXIS_999';
-- Should still be 50000.00
```

---

### Test 7: ❌ Invalid VPA

**Scenario:** Alice sends to non-existent VPA

```powershell
curl -X POST http://localhost:8080/api/payments/initiate `
  -H "Content-Type: application/json" `
  -d '{
    "payerVpa": "alice@l0",
    "payeeVpa": "nonexistent@l0",
    "amount": 100.00,
    "mpin": "1234",
    "deviceId": "device-alice-001",
    "ipAddress": "192.168.1.5"
  }'
```

**Expected Result:**

```json
{
  "txnId": "TXN_...",
  "status": "FAILED",
  "message": "Payee VPA not registered",
  "riskScore": null
}
```

---

### Test 8: ❌ Rate Limiting (Device Velocity)

**Scenario:** Send 6 rapid transactions from same device (limit is 5/min)

```powershell
# Send 6 transactions rapidly
for ($i=1; $i -le 6; $i++) {
    curl -X POST http://localhost:8080/api/payments/initiate `
      -H "Content-Type: application/json" `
      -d "{
        \"payerVpa\": \"alice@l0\",
        \"payeeVpa\": \"bob@l0\",
        \"amount\": 10.00,
        \"mpin\": \"1234\",
        \"deviceId\": \"device-alice-001\",
        \"ipAddress\": \"192.168.1.5\"
      }"
    Start-Sleep -Milliseconds 500
}
```

**Expected:** First 5 should succeed, 6th should fail with rate limit error

---

### Test 9: ✅ Idempotency Check

**Scenario:** Send same transaction twice (duplicate txnId)

```powershell
# Transaction 1
curl -X POST http://localhost:8080/api/payments/initiate `
  -H "Content-Type: application/json" `
  -d '{
    "payerVpa": "alice@l0",
    "payeeVpa": "bob@l0",
    "amount": 100.00,
    "mpin": "1234",
    "deviceId": "device-alice-001",
    "ipAddress": "192.168.1.5"
  }'

# Try to process same transaction again (should be idempotent)
# This would require manual replay at bank level
```

**Expected:** Second attempt should return "Already processed"

---

### Test 10: ⚡ Concurrent Transactions

**Scenario:** Multiple users send money simultaneously

```powershell
# Run these in parallel terminals
# Terminal A
curl -X POST http://localhost:8080/api/payments/initiate `
  -d '{"payerVpa": "eve@l0", "payeeVpa": "bob@l0", "amount": 200, "mpin": "1234", "deviceId": "device-eve-001", "ipAddress": "192.168.1.30"}'

# Terminal B (at the same time)
curl -X POST http://localhost:8080/api/payments/initiate `
  -d '{"payerVpa": "grace@l0", "payeeVpa": "david@l0", "amount": 150, "mpin": "1234", "deviceId": "device-grace-001", "ipAddress": "192.168.1.40"}'
```

**Expected:** Both should process successfully without conflicts (pessimistic locking prevents race conditions)

---

## 📊 Post-Test Verification Queries

### Check All Balances

```sql
-- AXIS Bank Accounts
SELECT account_number, user_name, current_balance
FROM axis_db.accounts
ORDER BY account_number;

-- SBI Bank Accounts
SELECT account_number, user_name, current_balance
FROM sbi_db.accounts
ORDER BY account_number;
```

### Check Transaction History

```sql
-- All transactions in Switch
SELECT global_txn_id, payer_vpa, payee_vpa, amount, status, risk_flag, created_at
FROM switch_db.transactions
ORDER BY created_at DESC
LIMIT 20;

-- Ledger entries (AXIS)
SELECT ledger_id, global_txn_id, account_number, amount, direction, balance_after, created_at
FROM axis_db.account_ledger
ORDER BY created_at DESC
LIMIT 20;

-- Ledger entries (SBI)
SELECT ledger_id, global_txn_id, account_number, amount, direction, balance_after, created_at
FROM sbi_db.account_ledger
ORDER BY created_at DESC
LIMIT 20;
```

### Check Gateway Logs

```sql
SELECT request_id, user_id, txn_amount, ip_address, device_id, timestamp
FROM gateway_db.gateway_logs
ORDER BY timestamp DESC
LIMIT 20;
```

---

## 🎯 Success Criteria

- ✅ All 4 services start without errors
- ✅ Successful payments complete end-to-end
- ✅ Insufficient balance is rejected properly
- ✅ Invalid MPIN is rejected
- ✅ Blacklisted accounts are blocked
- ✅ Frozen accounts trigger reversals
- ✅ Invalid VPAs are rejected at Switch
- ✅ Ledger entries are created for all operations
- ✅ Balances are updated atomically
- ✅ Concurrent transactions don't cause race conditions
- ✅ Idempotency prevents duplicate processing

---

## 🔧 Troubleshooting

### Service won't start

- Check port is not already in use: `netstat -ano | findstr :8080`
- Verify PostgreSQL is running: `Get-Service postgresql*`
- Check database connectivity in logs

### Transaction fails unexpectedly

- Check service logs for detailed error messages
- Verify database data: `SELECT * FROM accounts WHERE ...`
- Check network connectivity between services

### Database connection issues

- Verify PostgreSQL password in application.properties
- Check database exists: `psql -U postgres -l`
- Verify user has permissions

---

**Ready to test! Start all 4 services and execute test scenarios.**
