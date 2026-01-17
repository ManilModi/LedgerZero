package org.example.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhoneOtpVerificationReq extends PhoneReq {
    private String otp;
}
