package org.example.dto;

public enum TransactionStatus {
    PENDING,            // Transaction initiated, waiting for bank
    SUCCESS,            // Money deducted and credited
    FAILED,             // Bank declined (Balance/PIN)
    BLOCKED_FRAUD,      // ML Model rejected it (High Risk)
    DEEMED_APPROVED     // Timeout scenario (Money cut, credit pending)
}