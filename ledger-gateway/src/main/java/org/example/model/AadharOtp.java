package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "aadhar_otps",
        indexes = {
                @Index(columnList = "phone_number", unique = true)
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AadharOtp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "otp_id")
    private Long otpId;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "reference_id", nullable = false)
    private String referenceId;
}
