package org.example.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * Response DTO for balance inquiry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {

    private String vpa;
    private String bankHandle;
    private String maskedAccountNumber;
    private BigDecimal balance;
    private String bankName;
}
