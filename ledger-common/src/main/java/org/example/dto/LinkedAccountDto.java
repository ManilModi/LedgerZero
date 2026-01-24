package org.example.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO representing a user's linked bank account. Used for displaying accounts
 * in the frontend dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkedAccountDto {

    private String vpa;                    // e.g., "9723547755@okaxis"
    private String bankHandle;             // e.g., "axis", "sbi"
    private String bankName;               // e.g., "Axis Bank"
    private String maskedAccountNumber;    // e.g., "XXXX1234"
    private BigDecimal balance;            // Current balance
    private boolean isPrimary;             // Is this the primary account
}
