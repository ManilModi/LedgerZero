package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gateway_logs", indexes = {
        @Index(name = "idx_gw_user", columnList = "user_id"),
        @Index(name = "idx_gw_time", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayLog {

    @Id
    @Column(name = "request_id")
    private String requestId; // UUID

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "txn_amount")
    private BigDecimal txnAmount;

    // --- ML Features ---
    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "geo_lat")
    private BigDecimal geoLat;

    @Column(name = "geo_long")
    private BigDecimal geoLong;

    @Column(name = "wifi_ssid")
    private String wifiSsid;

    private LocalDateTime timestamp = LocalDateTime.now();
}