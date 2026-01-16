package org.example.dto;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BankClientReq{
    private String phoneNumber;
    private String bankHandle;
}
