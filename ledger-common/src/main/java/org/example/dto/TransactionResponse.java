package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.dto.TransactionStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private String transactionId;
    private TransactionStatus status; // SUCCESS, FAILED, BLOCKED
    private String message;           // "Payment Successful" or "Insufficient Funds"

    // ML Feedback (Switch adds this)
    private Double riskScore;         // 0.0 to 1.0
}