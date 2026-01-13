package org.example.dto.auth;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhoneOtpVerificationReq {
    private String otp;
    private String phoneNumber;
}
