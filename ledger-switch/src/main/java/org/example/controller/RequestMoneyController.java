package org.example.controller;

import org.example.dto.PaymentRequest;
import org.example.dto.TransactionResponse;
import org.example.enums.TransactionStatus;
import org.example.model.SwitchTransaction;
import org.example.repository.SwitchTransactionRepository;
import org.example.service.RouterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/switch/request")
public class RequestMoneyController {

    private final SwitchTransactionRepository transactionRepository;
    private final RouterService routerService;

    public RequestMoneyController(SwitchTransactionRepository transactionRepository, RouterService routerService) {
        this.transactionRepository = transactionRepository;
        this.routerService = routerService;
    }

    // --- STAGE 1: INITIATION (User 1 Requests Money) ---
    // User 1 is the PAYEE (Receiver), User 2 is the PAYER (Sender)
    @PostMapping("/initiate")
    public ResponseEntity<?> initiateRequest(@RequestParam String requesterVpa, 
                                             @RequestParam String payerVpa, 
                                             @RequestParam BigDecimal amount) {
        
        String txnId = UUID.randomUUID().toString();

        SwitchTransaction requestTxn = SwitchTransaction.builder()
                .globalTxnId(txnId)
                .payeeVpa(requesterVpa) // User 1 Receives
                .payerVpa(payerVpa)     // User 2 Pays
                .amount(amount)
                .status(TransactionStatus.REQUESTED.name()) // NEW STATUS
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(requestTxn);

        return ResponseEntity.ok("Request Sent. ID: " + txnId);
    }

    // --- STAGE 2: EXECUTION (User 2 Approves) ---
    @PostMapping("/approve/{txnId}")
    public ResponseEntity<?> approveRequest(@PathVariable String txnId, @RequestParam String mpin) {
        
        // 1. Fetch the existing REQUEST
        SwitchTransaction txn = transactionRepository.findById(txnId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // 2. Validate it is in REQUESTED state
        if (!TransactionStatus.REQUESTED.name().equals(txn.getStatus())) {
            return ResponseEntity.badRequest().body("Transaction is not in REQUESTED state");
        }

        // 3. Convert to PaymentRequest for the Router
        // The Router expects specific fields to process the money
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .txnId(txn.getGlobalTxnId()) // Reuse the SAME ID
                .payerVpa(txn.getPayerVpa())
                .payeeVpa(txn.getPayeeVpa())
                .amount(txn.getAmount())
                .mpinHash(mpin) // Pass the MPIN for validation in the Bank Service
                // .description("Approved Money Request")
                // .fraudCheckData(...)  <-- You can capture IP/Device from this request here
                .build();

        // 4. Execute using the existing Router Logic
        // This will update the status of the SAME row in the DB to PENDING -> SUCCESS/FAIL
        TransactionResponse response = routerService.routeTransaction(paymentRequest);

        return ResponseEntity.ok(response);
    }
}
