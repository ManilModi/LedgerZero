package org.example.client;

import org.example.dto.PaymentRequest;
import org.example.dto.TransactionResponse;
import org.example.enums.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP Client for communication with Bank services.
 * Routes requests to Axis (Port 7070) or SBI (Port 7071) based on bank handle.
 */
@Component
public class BankClient {

    private static final Logger log = LoggerFactory.getLogger(BankClient.class);

    private final RestTemplate restTemplate;
    private final String axisBankUrl;
    private final String sbiBankUrl;

    public BankClient(RestTemplate restTemplate,
                      @Value("${app.urls.bank.axis}") String axisBankUrl,
                      @Value("${app.urls.bank.sbi}") String sbiBankUrl) {
        this.restTemplate = restTemplate;
        this.axisBankUrl = axisBankUrl;
        this.sbiBankUrl = sbiBankUrl;
    }

    /**
     * Sends debit request to the payer's bank.
     *
     * @param request       Payment request with transaction details
     * @param bankHandle    Bank identifier ("AXIS" or "SBI")
     * @param accountNumber Account number to debit from (from VPA lookup)
     * @param riskScore     ML risk score for audit trail
     * @return TransactionResponse from the bank
     */
    public TransactionResponse debit(PaymentRequest request, String bankHandle, 
                                     String accountNumber, double riskScore) {
        String bankUrl = resolveBankUrl(bankHandle);
        String url = bankUrl + "/api/bank/debit";

        log.info("Sending DEBIT request to {} for txnId: {}, account: ****{}", 
                bankHandle, request.getTxnId(), 
                accountNumber.substring(Math.max(0, accountNumber.length() - 4)));

        return callBank(url, request, "DEBIT", bankHandle, accountNumber, riskScore);
    }

    /**
     * Sends credit request to the payee's bank.
     *
     * @param request       Payment request with transaction details
     * @param bankHandle    Bank identifier ("AXIS" or "SBI")
     * @param accountNumber Account number to credit to (from VPA lookup)
     * @param riskScore     ML risk score for audit trail
     * @return TransactionResponse from the bank
     */
    public TransactionResponse credit(PaymentRequest request, String bankHandle, 
                                      String accountNumber, double riskScore) {
        String bankUrl = resolveBankUrl(bankHandle);
        String url = bankUrl + "/api/bank/credit";

        log.info("Sending CREDIT request to {} for txnId: {}, account: ****{}", 
                bankHandle, request.getTxnId(), 
                accountNumber.substring(Math.max(0, accountNumber.length() - 4)));

        return callBank(url, request, "CREDIT", bankHandle, accountNumber, riskScore);
    }

    /**
     * Sends reversal request to rollback a failed transaction.
     *
     * @param request       Original payment request
     * @param bankHandle    Bank to reverse on
     * @param accountNumber Account to reverse debit on
     * @return TransactionResponse indicating reversal status
     */
    public TransactionResponse reverse(PaymentRequest request, String bankHandle, String accountNumber) {
        String bankUrl = resolveBankUrl(bankHandle);
        String url = bankUrl + "/api/bank/reverse";

        log.info("Sending REVERSE request to {} for txnId: {}", bankHandle, request.getTxnId());

        return callBank(url, request, "REVERSE", bankHandle, accountNumber, 0.0);
    }

    /**
     * Common method to call bank endpoints with required headers.
     */
    private TransactionResponse callBank(String url, PaymentRequest request,
                                         String operation, String bankHandle,
                                         String accountNumber, double riskScore) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Account-Number", accountNumber);
            headers.set("X-Risk-Score", String.valueOf(riskScore));

            HttpEntity<PaymentRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<TransactionResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    TransactionResponse.class
            );

            log.info("{} response from {} for txnId {}: {}",
                    operation, bankHandle, request.getTxnId(), response.getStatusCode());

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("{} client error from {} for txnId {}: {}",
                    operation, bankHandle, request.getTxnId(), e.getMessage());
            return TransactionResponse.builder()
                    .txnId(request.getTxnId())
                    .status(TransactionStatus.FAILED)
                    .message(bankHandle + " rejected: " + e.getStatusCode())
                    .build();

        } catch (HttpServerErrorException e) {
            log.error("{} server error from {} for txnId {}: {}",
                    operation, bankHandle, request.getTxnId(), e.getMessage());
            return TransactionResponse.builder()
                    .txnId(request.getTxnId())
                    .status(TransactionStatus.PENDING)
                    .message(bankHandle + " service error. Transaction pending.")
                    .build();

        } catch (ResourceAccessException e) {
            log.error("{} timeout/connection error to {} for txnId {}: {}",
                    operation, bankHandle, request.getTxnId(), e.getMessage());
            return TransactionResponse.builder()
                    .txnId(request.getTxnId())
                    .status(TransactionStatus.PENDING)
                    .message(bankHandle + " unreachable. Transaction pending.")
                    .build();
        }
    }

    /**
     * Resolves bank handle to URL.
     */
    private String resolveBankUrl(String bankHandle) {
        return switch (bankHandle.toUpperCase()) {
            case "AXIS" -> axisBankUrl;
            case "SBI" -> sbiBankUrl;
            default -> throw new IllegalArgumentException("Unknown bank handle: " + bankHandle);
        };
    }

    /**
     * Health check for a specific bank.
     */
    public boolean isBankHealthy(String bankHandle) {
        try {
            String url = resolveBankUrl(bankHandle) + "/actuator/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("{} health check failed: {}", bankHandle, e.getMessage());
            return false;
        }
    }
}
