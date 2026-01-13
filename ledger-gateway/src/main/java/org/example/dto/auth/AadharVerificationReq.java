package org.example.dto.auth;


import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AadharVerificationReq {
    String aadharNumber;
    String fullName;
    String phoneNumber;
}
