package org.example.service.imp;

import org.example.dto.auth.AadharVerificationReq;
import org.example.dto.auth.PhoneOtpVerificationReq;
import org.example.dto.auth.PhoneVerificationReq;
import org.example.dto.common.Response;
import org.example.model.Enums;
import org.example.model.TempUser;
import org.example.model.User;
import org.example.repository.TempUserRepo;
import org.example.repository.UserRepo;
import org.example.service.IAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AuthService implements IAuth {


    @Autowired
    private SnsClient snsClient;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TempUserRepo tempUserRepo;

    @Autowired
    private TokenManagerService tokenManagerService;

    @Value("")
    private String api_key;


    public Response sendOtpToPhone(PhoneVerificationReq req) {

        //config
        Map<String, MessageAttributeValue> smsAttributes = new HashMap<>();
        smsAttributes.put("AWS.SNS.SMS.SMSType", MessageAttributeValue.builder()
                .stringValue("Transactional")
                .dataType("String")
                .build());

        //1. get phono no
        String phoneNumber = req.getPhoneNumber();

        //2. otp gen
        String otp = String.format("%06d", new Random().nextInt(999999));

        //3. store otp
        TempUser tempUser = new TempUser();
        tempUser.setPhoneNumber(phoneNumber);
        tempUser.setOtp(otp);
        tempUserRepo.save(tempUser);

        //4. Construct Message
        String message = "Your LedgerZero Verification OTP is: " + otp;

        try {
            // 5. Call AWS SNS

            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .phoneNumber(phoneNumber)
                    .messageAttributes(smsAttributes)
                    .build();

            PublishResponse result = snsClient.publish(request);

            System.out.println("OTP sent to " + phoneNumber + ". MessageId: " + result.messageId());
            return new Response("OTP sent to " + phoneNumber, 200, null, null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return new Response("Error found", 500, e.toString(), null);
        }
    }

    public Response checkOtpToPhone(PhoneOtpVerificationReq req) {
        String phoneNumber = req.getPhoneNumber();
        String otp = req.getOtp();

        TempUser tempUser = tempUserRepo.findByPhoneNumber(phoneNumber);

        if(tempUser != null && tempUser.getOtp().equals(otp)){
            User user = new User();
            user.setPhoneNumber(phoneNumber);
            user.setKycStatus(Enums.KycStatus.PENDING);
            userRepo.save(user);

            tempUserRepo.delete(tempUser);

            return new Response("OTP verified successfully", 200, null, null);
        }

        return new Response("OTP verification failed", 400, null, null);
    }

    public Response sendOtpToAadhar(AadharVerificationReq req){

        String url = "https://api.sandbox.co.in/kyc/aadhaar/okyc/otp";
        String entity = "in.co.sandbox.kyc.aadhaar.okyc.otp.request";
        

        String authToken = tokenManagerService.getAccessToken();

        return null;
    }
}
