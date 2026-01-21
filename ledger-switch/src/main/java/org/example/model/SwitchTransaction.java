package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        // 🚀 Critical for Sync Engine: "SELECT * FROM txns WHERE created_at > last_sync_time"
        @Index(name = "idx_txn_created", columnList = "created_at"),

        // 🚀 Critical for Fraud Engine (Fan-In Check): "Count txns to Payee X in last 10 mins"
        // Using a composite index (payee + time) makes this query instant.
        @Index(name = "idx_txn_payee_time", columnList = "payee_vpa, created_at"),

        // 🚀 Critical for Fraud Engine (Velocity Check): "Count txns by Payer Y in last 10 mins"
        @Index(name = "idx_txn_payer_time", columnList = "payer_vpa, created_at"),

        // Useful for Analytics/Reports filtering (e.g., "Show me all FAILED txns")
        @Index(name = "idx_txn_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwitchTransaction {

    @Id
    @Column(name = "global_txn_id")
    private String globalTxnId;

    @Column(name = "payer_vpa", nullable = false)
    private String payerVpa;

    @Column(name = "payee_vpa", nullable = false)
    private String payeeVpa;

    @Column(nullable = false)
    private BigDecimal amount;

    // Routing
    @Column(name = "payer_bank")
    private String payerBank;

    @Column(name = "payee_bank")
    private String payeeBank;

    // Fraud Signals
    @Column(name = "sender_ip")
    private String senderIp;

    @Column(name = "sender_device_id")
    private String senderDeviceId;

    // ML Output
    @Column(name = "ml_fraud_score")
    private BigDecimal mlFraudScore;

    @Column(name = "risk_flag")
    private String riskFlag; // 'SAFE', 'REVIEW', 'BLOCK'

    private String status; // SUCCESS, FAILED

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}