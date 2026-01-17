package org.example.service;

import jakarta.servlet.http.HttpServletResponse;
import org.example.dto.*;

public interface IAuth {
    Response sendOtpToPhone(PhoneReq req);
    Response checkOtpToPhone(PhoneOtpVerificationReq req);

    Response completeRegistration(HttpServletResponse response, CreateUserReq req);
    Response login(HttpServletResponse response, LoginReq req);
    Response logout(HttpServletResponse response);
    Response changingDevice(HttpServletResponse response, DeviceChangeReq req);
}
