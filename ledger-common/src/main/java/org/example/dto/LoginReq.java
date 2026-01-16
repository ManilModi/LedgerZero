package org.example.dto;

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
