package org.example.repository;

import org.example.model.AadharOtp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAadharOtp extends JpaRepository<AadharOtp, Long> {
    AadharOtp findAadharOtpByPhoneNumber(String phoneNumber);
}
