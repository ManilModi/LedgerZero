package org.example.dto;

import lombok.*;

/**
 * Request DTO for fetching transaction history.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryRequest {

    private String vpa;
    private String accountNumber;
    private String bankHandle;
    private Integer page;       // Pagination - page number (0-indexed)
    private Integer limit;      // Pagination - items per page (default 20)
}
