package org.example.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing a transaction in the user's history. Used for displaying
 * transactions in the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryDto {

    private String transactionId;
    private String payerVpa;
    private String payeeVpa;
    private BigDecimal amount;
    private String status;                 // SUCCESS, FAILED, PENDING, BLOCKED_FRAUD
    private LocalDateTime timestamp;
    private String description;
    private String direction;              // DEBIT or CREDIT (relative to the user)
    private BigDecimal balanceAfter;       // Balance after transaction
    private String counterpartyName;       // Name of the other party
    private BigDecimal riskScore;          // ML fraud score (for transparency)
}
