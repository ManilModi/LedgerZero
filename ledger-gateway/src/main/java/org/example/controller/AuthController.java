package org.example.controller;

import org.example.dto.auth.PhoneOtpVerificationReq;
import org.example.dto.auth.PhoneVerificationReq;
import org.example.dto.common.Response;
import org.example.service.IAuth;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuth authService;

    public AuthController(IAuth authService) {
        this.authService = authService;
    }

    @PostMapping("/send-otp")
    public Response sendOtp(@RequestBody PhoneVerificationReq req) {
        return authService.sendOtp(req);
    }

    @PostMapping("/check-otp")
    public  Response checkOtp(@RequestBody PhoneOtpVerificationReq req){
        return authService.checkOtp(req);
    }

}
