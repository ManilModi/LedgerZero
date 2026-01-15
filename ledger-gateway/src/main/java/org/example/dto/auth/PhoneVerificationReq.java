package org.example.dto.auth;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhoneVerificationReq {
    private String phoneNumber;
}
