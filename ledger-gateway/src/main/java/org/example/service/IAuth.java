package org.example.service;

import org.example.dto.auth.PhoneOtpVerificationReq;
import org.example.dto.auth.PhoneVerificationReq;
import org.example.dto.common.Response;

public interface IAuth {
    Response sendOtpToPhone(PhoneVerificationReq req);
    Response checkOtpToPhone(PhoneOtpVerificationReq req);
}
