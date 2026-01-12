package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudData {
    private String ipAddress;       // e.g., "192.168.1.5"
    private String deviceId;        // e.g., "device-uuid-1234"
    private Double geoLat;          // e.g., 19.0760
    private Double geoLong;         // e.g., 72.8777
    private String wifiSsid;        // e.g., "Public_WiFi" (High risk)
}