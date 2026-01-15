# ==================================================

# LedgerZero - Database Setup Guide

# ==================================================

## Prerequisites

- PostgreSQL 14+ installed and running
- psql command-line tool available
- Default PostgreSQL credentials or custom ones

## Step 1: Create Databases

```bash
# Connect to PostgreSQL as postgres user (Run in terminal)
psql -U postgres

# Execute these commands in psql
CREATE DATABASE gateway_db;
CREATE DATABASE switch_db;
CREATE DATABASE axis_db;
CREATE DATABASE sbi_db;

# Verify databases created
\l

# Exit psql
\q
```

## Step 2: Execute Schema Scripts

Ensure you are in the project root directory (`D:\LedgerZero_EX\LedgerZero_EX`).

```powershell
# Gateway Database
psql -h localhost -U postgres -d gateway_db -f database-setup/01_gateway_schema.sql

# Switch Database
psql -h localhost -U postgres -d switch_db -f database-setup/02_switch_schema.sql

# AXIS Bank Database
psql -h localhost -U postgres -d axis_db -f database-setup/03_bank_schema.sql

# SBI Bank Database
psql -h localhost -U postgres -d sbi_db -f database-setup/03_bank_schema.sql
```

## Step 3: Load Seed Data

```powershell
# Gateway Seed Data
psql -h localhost -U postgres -d gateway_db -f database-setup/04_gateway_seed.sql

# Switch Seed Data
psql -h localhost -U postgres -d switch_db -f database-setup/05_switch_seed.sql

# AXIS Bank Seed Data
psql -h localhost -U postgres -d axis_db -f database-setup/06_axis_seed.sql

# SBI Bank Seed Data
psql -h localhost -U postgres -d sbi_db -f database-setup/07_sbi_seed.sql
```

## Test Users & Accounts

### Gateway Users (All MPIN: 1234)

| VPA        | Name            | Device ID          | Trusted          |
| ---------- | --------------- | ------------------ | ---------------- |
| alice@l0   | Alice Johnson   | device-alice-001   | ✅               |
| bob@l0     | Bob Smith       | device-bob-001     | ✅               |
| charlie@l0 | Charlie Brown   | device-charlie-001 | ✅               |
| david@l0   | David Miller    | device-david-001   | ✅               |
| eve@l0     | Eve Williams    | device-eve-001     | ✅               |
| frank@l0   | Frank Wilson    | device-frank-001   | ✅               |
| grace@l0   | Grace Taylor    | device-grace-001   | ✅               |
| hacker@l0  | Suspicious User | device-hacker-999  | ✅ (For Testing) |

### VPA to Bank Mapping

| VPA        | Bank | Account Number | Balance | Status    |
| ---------- | ---- | -------------- | ------- | --------- |
| alice@l0   | AXIS | ACC_AXIS_001   | ₹10,000 | Active    |
| bob@l0     | SBI  | ACC_SBI_002    | ₹20,000 | Active    |
| charlie@l0 | AXIS | ACC_AXIS_003   | ₹5,000  | Active    |
| david@l0   | SBI  | ACC_SBI_004    | ₹7,500  | Active    |
| eve@l0     | AXIS | ACC_AXIS_005   | ₹15,000 | Active    |
| frank@l0   | SBI  | ACC_SBI_006    | ₹12,000 | Active    |
| grace@l0   | AXIS | ACC_AXIS_007   | ₹8,000  | Active    |
| hacker@l0  | AXIS | ACC_AXIS_999   | ₹50,000 | FROZEN ❌ |

## 🧪 Testing Commands (PowerShell)

Use these PowerShell snippets to execute the tests defined in `TEST_SCENARIOS.md`.

### Test 1: Inter-Bank (Alice -> Bob)

```powershell
$body = @{
    payerVpa = "alice@l0"
    payeeVpa = "bob@l0"
    amount = 500.00
    mpin = "1234"
    deviceId = "device-alice-001"
    ipAddress = "192.168.1.5"
    geoLat = 19.0760
    geoLong = 72.8777
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/payments/initiate" -Method Post -Body $body -ContentType "application/json"
    $response
} catch {
    $_.Exception.Response.GetResponseStream() | %{ $reader = New-Object System.IO.StreamReader($_); $reader.ReadToEnd() }
}
```

### Test 2: Same Bank (Alice -> Charlie)

```powershell
$body = @{
    payerVpa = "alice@l0"
    payeeVpa = "charlie@l0"
    amount = 300.00
    mpin = "1234"
    deviceId = "device-alice-001"
    ipAddress = "192.168.1.5"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/payments/initiate" -Method Post -Body $body -ContentType "application/json"
```

### Test 3: Insufficient Balance (Charlie -> Bob)

```powershell
$body = @{
    payerVpa = "charlie@l0"
    payeeVpa = "bob@l0"
    amount = 10000.00
    mpin = "1234"
    deviceId = "device-charlie-001"
    ipAddress = "192.168.1.10"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/payments/initiate" -Method Post -Body $body -ContentType "application/json"
} catch {
    $_.Exception.Response.GetResponseStream() | %{ $reader = New-Object System.IO.StreamReader($_); $reader.ReadToEnd() }
}
```

### Test 4: Invalid MPIN (Alice -> Bob)

```powershell
$body = @{
    payerVpa = "alice@l0"
    payeeVpa = "bob@l0"
    amount = 100.00
    mpin = "9999"
    deviceId = "device-alice-001"
    ipAddress = "192.168.1.5"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/payments/initiate" -Method Post -Body $body -ContentType "application/json"
} catch {
    $_.Exception.Response.GetResponseStream() | %{ $reader = New-Object System.IO.StreamReader($_); $reader.ReadToEnd() }
}
```

### Test 5: Blacklisted Account (Hacker -> Bob)

```powershell
$body = @{
    payerVpa = "hacker@l0"
    payeeVpa = "bob@l0"
    amount = 1000.00
    mpin = "1234"
    deviceId = "device-hacker-999"
    ipAddress = "45.123.45.67"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/payments/initiate" -Method Post -Body $body -ContentType "application/json"
} catch {
    $_.Exception.Response.GetResponseStream() | %{ $reader = New-Object System.IO.StreamReader($_); $reader.ReadToEnd() }
}
```

### Test 6: Frozen Account Reversal (Bob -> Hacker)

```powershell
$body = @{
    payerVpa = "bob@l0"
    payeeVpa = "hacker@l0"
    amount = 500.00
    mpin = "1234"
    deviceId = "device-bob-001"
    ipAddress = "192.168.1.20"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/payments/initiate" -Method Post -Body $body -ContentType "application/json"
} catch {
    $_.Exception.Response.GetResponseStream() | %{ $reader = New-Object System.IO.StreamReader($_); $reader.ReadToEnd() }
}
```

### Test 7: Invalid VPA (Alice -> Ghost)

```powershell
$body = @{
    payerVpa = "alice@l0"
    payeeVpa = "ghost@l0"
    amount = 100.00
    mpin = "1234"
    deviceId = "device-alice-001"
    ipAddress = "192.168.1.5"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/payments/initiate" -Method Post -Body $body -ContentType "application/json"
} catch {
    $_.Exception.Response.GetResponseStream() | %{ $reader = New-Object System.IO.StreamReader($_); $reader.ReadToEnd() }
}
```
