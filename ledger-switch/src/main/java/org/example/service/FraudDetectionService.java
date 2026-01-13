package org.example.service;

import org.example.dto.PaymentRequest;
import org.example.dto.PaymentRequest.FraudCheckData; // Assuming inner class or check where it is
import org.example.model.SuspiciousEntity;
import org.example.repository.SuspiciousEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * ML-based Fraud Detection Service.
 *
 * Analyzes payment requests and calculates a risk score.
 *
 * Risk Score Range: 0.0 (safe) to 1.0 (high risk)
 *
 * Features to analyze:
 * - Transaction velocity (too many txns from same device/IP)
 * - Geo-location anomalies
 * - Device trust level
 * - Transaction amount patterns
 * - Time-based patterns (unusual hours)
 * - IP reputation
 *
 * TODO: Implement ML model integration
 */
@Service
public class FraudDetectionService {

    @Autowired
    private SuspiciousEntityRepository suspiciousEntityRepository;

    /**
     * Calculates the fraud risk score for a payment request.
     *
     * @param request Payment request with fraud check data
     * @return Risk score between 0.0 (safe) and 1.0 (high risk)
     */
    public double calculateRiskScore(PaymentRequest request) {
        if (request == null) return 0.0;
        
        double score = 0.0;

        // Check 1: High Amount Check (> 1 Lakh)
        if (request.getAmount() != null && request.getAmount().doubleValue() > 100000.0) {
            score += 0.8; 
        }

        // Check 2: Suspicious IP/Device via Repository
        if (request.getFraudCheckData() != null) {
            String ip = request.getFraudCheckData().getIpAddress();
            String deviceId = request.getFraudCheckData().getDeviceId();

            if (ip != null && isIpSuspicious(ip)) {
                return 1.0; // Block immediately
            }
            if (deviceId != null && isDeviceSuspicious(deviceId)) {
                return 1.0; // Block immediately
            }
        }

        // Check 3: Simple geolocation check (stub)
        // If geo is far from "home" (not implemented yet)

        // Cap at 1.0
        return Math.min(score, 1.0);
    }

    /**
     * Checks if an IP address is suspicious.
     *
     * @param ipAddress The IP address to check
     * @return true if IP is in suspicious list
     */
    public boolean isIpSuspicious(String ipAddress) {
        // Find by Entity Value and ensure it is an IP
        Optional<SuspiciousEntity> entity = suspiciousEntityRepository.findByEntityValue(ipAddress);
        if (entity.isPresent()) {
            SuspiciousEntity se = entity.get();
             // Check if blocked_until > now
             if (se.getBlockedUntil() != null && se.getBlockedUntil().isAfter(LocalDateTime.now())) {
                 return true;
             }
        }
        return false;
    }

    /**
     * Checks if a device is suspicious.
     *
     * @param deviceId The device ID to check
     * @return true if device is in suspicious list
     */
    public boolean isDeviceSuspicious(String deviceId) {
        Optional<SuspiciousEntity> entity = suspiciousEntityRepository.findByEntityValue(deviceId);
        if (entity.isPresent()) {
            SuspiciousEntity se = entity.get();
             if (se.getBlockedUntil() != null && se.getBlockedUntil().isAfter(LocalDateTime.now())) {
                 return true;
             }
        }
        return false;
    }

    /**
     * Checks transaction velocity for a device.
     *
     * @param deviceId The device ID
     * @param minutes  Time window in minutes
     * @return Number of transactions in the time window
     */
    public long getDeviceVelocity(String deviceId, int minutes) {
        // TODO: Implement velocity check using repository
        return 0;
    }

    /**
     * Checks transaction velocity for an IP.
     *
     * @param ipAddress The IP address
     * @param minutes   Time window in minutes
     * @return Number of transactions in the time window
     */
    public long getIpVelocity(String ipAddress, int minutes) {
        // TODO: Implement velocity check using repository
        return 0;
    }

    /**
     * Blocks an entity (IP or Device) temporarily.
     *
     * @param entityValue The IP or Device ID
     * @param entityType  Type: "IP" or "DEVICE"
     * @param reason      Reason for blocking
     * @param hours       Duration to block in hours
     */
    public void blockEntity(String entityValue, String entityType, String reason, int hours) {
        // TODO: Implement entity blocking using SuspiciousEntityRepository
    }
}
