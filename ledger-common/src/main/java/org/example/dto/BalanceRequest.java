package org.example.dto;

import lombok.*;

/**
 * Request DTO for balance inquiry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceRequest {

    private String vpa;
    private String accountNumber;
    private String bankHandle;
    private String mpin;  // MPIN verification for balance check
}
