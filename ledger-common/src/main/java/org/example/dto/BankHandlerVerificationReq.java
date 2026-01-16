package org.example.dto;

import lombok.*;

@Data
@NoArgsConstructor
@Getter
@Setter
public class BankHandlerVerificationReq {
    private String bankHandle;
    private String otp;
}
