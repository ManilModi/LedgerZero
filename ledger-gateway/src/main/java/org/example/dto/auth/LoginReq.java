package org.example.dto.auth;

import lombok.*;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginReq {
    private String phoneNumber;
    private String password;
    private String deviceId;
}
