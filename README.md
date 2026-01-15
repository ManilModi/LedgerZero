# LedgerZero - Distributed UPI Payment System

A microservices-based payment platform with ML-powered fraud detection, featuring distributed ledger architecture and robust concurrency controls.

## 🏗️ Architecture Overview

```
┌─────────────┐          ┌──────────────┐          ┌─────────────┐
│   Frontend  │          │   Gateway    │          │   Switch    │
│             │─────────▶│   :8080      │─────────▶│   :9090     │
└─────────────┘          └──────────────┘          └─────────────┘
      │                         │                          │
      │                         │                          │
      │                   ┌─────▼────┐            ┌───────▼────────┐
      │                   │ Gateway  │            │   Switch DB    │
      │                   │    DB    │            │  - VPA Registry│
      │                   │          │            │  - Transactions│
      │                   └──────────┘            └────────────────┘
      │                                                   │
      │                         ┌─────────────────────────┴────────────────┐
      │                         │                                          │
      │                         ▼                                          ▼
      │                 ┌─────────────┐                           ┌─────────────┐
      │                 │ AXIS Bank   │                           │  SBI Bank   │
      └────────────────▶│   :7070     │                           │   :7071     │
                        └──────┬──────┘                           └──────┬──────┘
                               │                                         │
                        ┌──────▼──────┐                          ┌──────▼──────┐
                        │   AXIS DB   │                          │   SBI DB    │
                        │  - Accounts │                          │  - Accounts │
                        │  - Ledger   │                          │  - Ledger   │
                        └─────────────┘                          └─────────────┘
```

## 📦 Modules

| Module                 | Port | Database   | Purpose                                                 |
| ---------------------- | ---- | ---------- | ------------------------------------------------------- |
| **ledger-gateway**     | 8080 | gateway_db | Entry point, user authentication, fraud data collection |
| **ledger-switch**      | 9090 | switch_db  | VPA routing, fraud detection, transaction orchestration |
| **ledger-bank** (AXIS) | 7070 | axis_db    | Core banking operations for AXIS                        |
| **ledger-bank** (SBI)  | 7071 | sbi_db     | Core banking operations for SBI                         |
| **ledger-common**      | -    | -          | Shared DTOs, enums, utilities                           |

## 🔄 Complete Transaction Flow

### **Step 1: Payment Initiation (Gateway)**

**Endpoint:** `POST http://localhost:8080/api/payments/initiate`

**Request Body:**

```json
{
  "payerVpa": "alice@l0",
  "payeeVpa": "bob@l0",
  "amount": 500.0,
  "mpin": "1234",
  "deviceId": "device-uuid-1234",
  "ipAddress": "192.168.1.5",
  "geoLat": 19.076,
  "geoLong": 72.8777,
  "wifiSsid": "Home_WiFi",
  "userAgent": "LedgerZero-App/1.0"
}
```

**Response:**

```json
{
  "txnId": "TXN_1736789123456_abc12345",
  "status": "SUCCESS",
  "message": "Payment Successful",
  "riskScore": 0.15
}
```

**Gateway Processing:**

1. ✅ Validate sender VPA exists in database
2. ✅ Check device is trusted (in `user_devices` table)
3. ✅ Rate limiting - Max 5 transactions per device per minute
4. ✅ Hash MPIN using SHA-256
5. ✅ Build fraud metadata (IP, location, device fingerprint)
6. ✅ Log request to `gateway_logs` table
7. ✅ Forward to Switch service

---

### **Step 2: Routing & Fraud Detection (Switch)**

**Internal Endpoint:** `POST http://localhost:9090/api/switch/transfer`

**Request (from Gateway):**

```json
{
  "txnId": "TXN_1736789123456_abc12345",
  "payerVpa": "alice@l0",
  "payeeVpa": "bob@l0",
  "amount": 500.0,
  "mpinHash": "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
  "fraudCheckData": {
    "ipAddress": "192.168.1.5",
    "deviceId": "device-uuid-1234",
    "geoLat": 19.076,
    "geoLong": 72.8777,
    "wifiSsid": "Home_WiFi",
    "userAgent": "LedgerZero-App/1.0",
    "requestTimestamp": 1736789123456
  }
}
```

**Switch Processing:**

1. **VPA Lookup**

   - `alice@l0` → Bank: `AXIS`, Account: `ACC_ALICE_001`
   - `bob@l0` → Bank: `SBI`, Account: `ACC_BOB_002`

2. **Blacklist Check**

   - Verify neither VPA is blacklisted

3. **ML Fraud Detection**

   - Calculate risk score (0.0 - 1.0) using:
     - Device velocity (transactions per minute)
     - IP reputation
     - Geographic anomalies
     - Amount patterns
   - Score < 0.50 → `SAFE`
   - Score 0.50-0.75 → `REVIEW`
   - Score > 0.75 → `BLOCK`

4. **Transaction Record**

   - Insert into `switch_db.transactions` with status `PENDING`

5. **Fraud Blocking**

   - If risk score > 0.75:
     - Update status to `BLOCKED_FRAUD`
     - Return immediately (no bank calls)

6. **Bank Orchestration** (if fraud check passes)
   - Call AXIS Bank for **DEBIT**
   - Call SBI Bank for **CREDIT**
   - Handle reversals if needed

---

### **Step 3: Debit from Payer (AXIS Bank)**

**Internal Endpoint:** `POST http://localhost:7070/api/bank/debit`

**Headers:**

```
Content-Type: application/json
X-Account-Number: ACC_ALICE_001
X-Risk-Score: 0.15
```

**Request Body:** (Same PaymentRequest object)

**Bank Processing:**

1. **Idempotency Check**

   ```sql
   SELECT * FROM account_ledger
   WHERE global_txn_id = 'TXN_xxx'
   AND account_number = 'ACC_ALICE_001'
   AND direction = 'DEBIT'
   ```

   - If exists → Return `SUCCESS` (already processed)

2. **Acquire Lock**

   ```sql
   SELECT * FROM accounts
   WHERE account_number = 'ACC_ALICE_001'
   FOR UPDATE
   ```

   - **PESSIMISTIC_WRITE** lock prevents concurrent modifications

3. **Validation**

   - ❌ Account not found → `FAILED`
   - ❌ Account frozen → `FAILED`
   - ❌ Invalid MPIN → `FAILED`
   - ❌ Insufficient balance → `FAILED`

4. **Debit Operation**

   ```java
   currentBalance = currentBalance - amount;
   version = version + 1; // Optimistic locking
   ```

5. **Ledger Entry** (Immutable Audit Trail)

   ```sql
   INSERT INTO account_ledger (
     global_txn_id,
     account_number,
     amount,
     direction,
     counterparty_vpa,
     balance_after,
     risk_score,
     created_at
   ) VALUES (
     'TXN_1736789123456_abc12345',
     'ACC_ALICE_001',
     500.00,
     'DEBIT',
     'bob@l0',
     9500.00,
     0.15,
     NOW()
   )
   ```

6. **Response:**
   ```json
   {
     "txnId": "TXN_1736789123456_abc12345",
     "status": "SUCCESS",
     "message": "Debit successful"
   }
   ```

---

### **Step 4: Credit to Payee (SBI Bank)**

**Internal Endpoint:** `POST http://localhost:7071/api/bank/credit`

**Headers:**

```
Content-Type: application/json
X-Account-Number: ACC_BOB_002
X-Risk-Score: 0.15
```

**Processing:** (Similar to debit, but credits amount)

1. Idempotency check
2. Acquire pessimistic lock
3. Validate account (not frozen)
4. Credit amount: `balance = balance + amount`
5. Create ledger entry (direction = `CREDIT`)
6. Return SUCCESS

**Ledger Entry:**

```sql
INSERT INTO account_ledger (
  global_txn_id,
  account_number,
  amount,
  direction,
  counterparty_vpa,
  balance_after,
  risk_score,
  created_at
) VALUES (
  'TXN_1736789123456_abc12345',
  'ACC_BOB_002',
  500.00,
  'CREDIT',
  'alice@l0',
  5500.00,
  0.15,
  NOW()
)
```

---

### **Step 5: Reversal (If Credit Fails)**

**Scenario:** Alice's debit succeeded, but Bob's account is frozen.

**Internal Endpoint:** `POST http://localhost:7070/api/bank/reverse`

**Headers:**

```
X-Account-Number: ACC_ALICE_001
```

**Processing:**

1. Verify original debit exists
2. Check not already reversed
3. Credit money back to Alice
4. Create reversal ledger entry with `global_txn_id = TXN_xxx_REVERSAL`
5. Switch marks transaction as `FAILED`

**Reversal Ledger Entry:**

```sql
INSERT INTO account_ledger (
  global_txn_id,
  account_number,
  amount,
  direction,
  counterparty_vpa,
  balance_after,
  risk_score,
  created_at
) VALUES (
  'TXN_1736789123456_abc12345_REVERSAL',
  'ACC_ALICE_001',
  500.00,
  'CREDIT',
  'REVERSAL:bob@l0',
  10000.00,
  NULL,
  NOW()
)
```

---

## 🗄️ Database Schema

### Gateway DB (`gateway_db`)

**users**

```sql
CREATE TABLE users (
  user_id BIGSERIAL PRIMARY KEY,
  vpa VARCHAR(50) UNIQUE NOT NULL,
  user_name VARCHAR(100),
  email VARCHAR(100),
  created_at TIMESTAMP DEFAULT NOW()
);
```

**user_devices**

```sql
CREATE TABLE user_devices (
  device_id VARCHAR(255) PRIMARY KEY,
  user_id BIGINT REFERENCES users(user_id),
  is_trusted BOOLEAN DEFAULT false,
  last_seen TIMESTAMP
);
```

**gateway_logs**

```sql
CREATE TABLE gateway_logs (
  log_id BIGSERIAL PRIMARY KEY,
  request_id VARCHAR(100),
  user_id BIGINT,
  txn_amount DECIMAL(15,2),
  ip_address VARCHAR(50),
  device_id VARCHAR(255),
  geo_lat DECIMAL(10,7),
  geo_long DECIMAL(10,7),
  wifi_ssid VARCHAR(100),
  timestamp TIMESTAMP DEFAULT NOW()
);
```

---

### Switch DB (`switch_db`)

**vpa_registry**

```sql
CREATE TABLE vpa_registry (
  vpa VARCHAR(50) PRIMARY KEY,
  linked_bank_handle VARCHAR(20) NOT NULL, -- 'AXIS', 'SBI'
  account_ref VARCHAR(100) NOT NULL,       -- Account number
  is_blacklisted BOOLEAN DEFAULT false,
  created_at TIMESTAMP DEFAULT NOW()
);
```

**transactions**

```sql
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
  risk_flag VARCHAR(20), -- 'SAFE', 'REVIEW', 'BLOCK'
  status VARCHAR(20),    -- 'SUCCESS', 'FAILED', 'BLOCKED_FRAUD', 'PENDING'
  created_at TIMESTAMP DEFAULT NOW()
);
```

---

### Bank DB (`axis_db`, `sbi_db`)

**accounts**

```sql
CREATE TABLE accounts (
  account_number VARCHAR(50) PRIMARY KEY,
  user_name VARCHAR(100),
  current_balance DECIMAL(15,2) NOT NULL,
  avg_monthly_balance DECIMAL(15,2),
  frozen_status BOOLEAN DEFAULT false,
  mpin_hash VARCHAR(255),
  version BIGINT DEFAULT 0  -- For optimistic locking
);
```

**account_ledger** (Immutable)

```sql
CREATE TABLE account_ledger (
  ledger_id BIGSERIAL PRIMARY KEY,
  global_txn_id VARCHAR(100) NOT NULL,
  account_number VARCHAR(50) NOT NULL,
  amount DECIMAL(15,2) NOT NULL,
  direction VARCHAR(10) NOT NULL,  -- 'DEBIT', 'CREDIT'
  counterparty_vpa VARCHAR(50),
  balance_after DECIMAL(15,2) NOT NULL,
  risk_score DECIMAL(5,4),
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_ledger_account ON account_ledger(account_number);
CREATE INDEX idx_ledger_txn ON account_ledger(global_txn_id);
```

---

## 🔒 Security & Concurrency

### Locking Strategy

| Level                     | Mechanism                                  | Purpose                                                     |
| ------------------------- | ------------------------------------------ | ----------------------------------------------------------- |
| **Optimistic Lock**       | `@Version` annotation on `BankAccount`     | Detects concurrent modifications, auto-increments on update |
| **Pessimistic Lock**      | `@Lock(LockModeType.PESSIMISTIC_WRITE)`    | Database-level row lock during debit/credit                 |
| **Transaction Isolation** | `@Transactional(isolation = SERIALIZABLE)` | Highest isolation level for critical operations             |
| **Idempotency**           | Ledger lookup before operations            | Prevents duplicate debits/credits                           |

### Fraud Detection Features

- **Device Velocity**: Tracks transactions per device per minute
- **IP Reputation**: Monitors suspicious IP addresses
- **Geo-Anomaly**: Detects impossible location changes
- **Amount Patterns**: Identifies unusual transaction amounts
- **ML Risk Scoring**: Real-time fraud probability (0.0 - 1.0)

### MPIN Security

- Never stored in plain text
- SHA-256 hashed at Gateway before transmission
- Verified at Bank level during debit operations

---

## ⚙️ Configuration

### Gateway (`application.properties`)

```properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5432/gateway_db
app.urls.switch=http://localhost:9090
```

### Switch (`application.properties`)

```properties
server.port=9090
spring.datasource.url=jdbc:postgresql://localhost:5432/switch_db
app.urls.bank.axis=http://localhost:7070
app.urls.bank.sbi=http://localhost:7071
```

### Bank - AXIS (`application-axis.properties`)

```properties
server.port=7070
spring.datasource.url=jdbc:postgresql://localhost:5432/axis_db
```

### Bank - SBI (`application-sbi.properties`)

```properties
server.port=7071
spring.datasource.url=jdbc:postgresql://localhost:5432/sbi_db
```

---

## 🚀 Running the Services

### Prerequisites

- Java 21+
- Maven 3.8+
- PostgreSQL 14+

### Database Setup

```bash
# Create databases
psql -U postgres
CREATE DATABASE gateway_db;
CREATE DATABASE switch_db;
CREATE DATABASE axis_db;
CREATE DATABASE sbi_db;
```

### Build All Modules

```bash
mvn clean install
```

### Run Services

**Terminal 1: Gateway**

```bash
cd ledger-gateway
mvn spring-boot:run
```

**Terminal 2: Switch**

```bash
cd ledger-switch
mvn spring-boot:run
```

**Terminal 3: AXIS Bank**

```bash
cd ledger-bank
mvn spring-boot:run -Dspring-boot.run.profiles=axis
```

**Terminal 4: SBI Bank**

```bash
cd ledger-bank
mvn spring-boot:run -Dspring-boot.run.profiles=sbi
```

---

## 📊 API Endpoints

### Gateway APIs

| Method | Endpoint                       | Description               |
| ------ | ------------------------------ | ------------------------- |
| POST   | `/api/payments/initiate`       | Initiate payment transfer |
| GET    | `/api/payments/status/{txnId}` | Check transaction status  |
| POST   | `/api/users/register`          | Register new user         |

### Switch APIs (Internal)

| Method | Endpoint               | Description               |
| ------ | ---------------------- | ------------------------- |
| POST   | `/api/switch/transfer` | Route payment transaction |
| GET    | `/actuator/health`     | Health check              |

### Bank APIs (Internal)

| Method | Endpoint            | Description                |
| ------ | ------------------- | -------------------------- |
| POST   | `/api/bank/debit`   | Debit from account         |
| POST   | `/api/bank/credit`  | Credit to account          |
| POST   | `/api/bank/reverse` | Reverse failed transaction |
| GET    | `/api/bank/health`  | Health check               |

---

## 🔍 Transaction Status Codes

| Status          | Description                                                  |
| --------------- | ------------------------------------------------------------ |
| `SUCCESS`       | Transaction completed successfully                           |
| `FAILED`        | Transaction failed (insufficient balance, invalid PIN, etc.) |
| `BLOCKED_FRAUD` | Blocked by ML fraud detection (risk score > 0.75)            |
| `PENDING`       | Transaction in progress or awaiting processing               |

---

## 📝 Example Test Scenarios

### Scenario 1: Successful Payment

```bash
curl -X POST http://localhost:8080/api/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "payerVpa": "alice@l0",
    "payeeVpa": "bob@l0",
    "amount": 500.00,
    "mpin": "1234",
    "deviceId": "device-001",
    "ipAddress": "192.168.1.5"
  }'
```

**Expected Response:**

```json
{
  "txnId": "TXN_1736789123456_abc12345",
  "status": "SUCCESS",
  "message": "Payment Successful",
  "riskScore": 0.15
}
```

### Scenario 2: Fraud Blocked

```bash
# High-risk transaction (unusual device/location)
curl -X POST http://localhost:8080/api/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "payerVpa": "alice@l0",
    "payeeVpa": "bob@l0",
    "amount": 50000.00,
    "mpin": "1234",
    "deviceId": "suspicious-device-999",
    "ipAddress": "45.123.45.67"
  }'
```

**Expected Response:**

```json
{
  "txnId": "TXN_1736789123457_def67890",
  "status": "BLOCKED_FRAUD",
  "message": "Transaction blocked: High risk detected",
  "riskScore": 0.89
}
```

### Scenario 3: Insufficient Balance

```bash
curl -X POST http://localhost:8080/api/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "payerVpa": "alice@l0",
    "payeeVpa": "bob@l0",
    "amount": 999999.00,
    "mpin": "1234",
    "deviceId": "device-001",
    "ipAddress": "192.168.1.5"
  }'
```

**Expected Response:**

```json
{
  "txnId": "TXN_1736789123458_ghi12345",
  "status": "FAILED",
  "message": "Debit failed: Insufficient balance",
  "riskScore": 0.12
}
```

---

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.x
- **Language**: Java 21
- **Database**: PostgreSQL 14+
- **ORM**: JPA/Hibernate
- **Build Tool**: Maven 3.8+
- **Logging**: SLF4J + Logback
- **HTTP Client**: RestTemplate
- **Annotations**: Lombok

---

## 📈 Monitoring & Observability

### Health Checks

- Gateway: `http://localhost:8080/actuator/health`
- Switch: `http://localhost:9090/actuator/health`
- AXIS Bank: `http://localhost:7070/api/bank/health`
- SBI Bank: `http://localhost:7071/api/bank/health`

### Logging

All services use structured logging:

```
INFO: Normal operations
WARN: Suspicious activity, blacklisted VPAs
ERROR: System failures, bank connectivity issues
```

---

## 🤝 Contributing

1. Create feature branch: `git checkout -b feature/new-feature`
2. Commit changes: `git commit -m "Add new feature"`
3. Push to branch: `git push origin feature/new-feature`
4. Create Pull Request

---

## 📄 License

This project is licensed under the MIT License.

---

## 📞 Support

For issues or questions, please create an issue in the repository.

---

**Built with ❤️ for secure, scalable, fraud-resistant payments**
