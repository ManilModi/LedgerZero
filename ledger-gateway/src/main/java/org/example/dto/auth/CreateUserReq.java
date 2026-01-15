package org.example.dto.auth;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserReq {
    private String phoneNumber;
    private String fullName;
    private String password;
    private String deviceId;
    private String lastLoginIp;
    private String modelName;
    private String osVersion;
}
