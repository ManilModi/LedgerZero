package org.example.dto;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BankClientVerificationReq {
    private String otp;
    private String phoneNumber;
}
