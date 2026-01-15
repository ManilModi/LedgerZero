package org.example.dto.auth;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeviceChangeReq {
    private String phoneNumber;
    private String otp;
    private String deviceId;
    private String lastLoginIp;
    private String modelName;
    private String osVersion;
}
