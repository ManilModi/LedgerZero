package org.example.utils;

import lombok.Value;

import java.security.SecureRandom;

public class GenerateVPAUtil {

    private static final SecureRandom secureRandom = new SecureRandom();

    private static String sanitizeName(String input) {
        if (input == null) return "user";

        // Replace everything except a-z and 0-9
        String cleaned = input.toLowerCase().replaceAll("[^a-z0-9]", "");

        // Truncate if too long (UPI max is usually 50, let's keep name part to 15)
        if (cleaned.length() > 15) {
            cleaned = cleaned.substring(0, 15);
        }
        return cleaned;
    }

    /**
     * Generates a GPay-style VPA.
     * Logic: Sanitized Name + 3 Random Digits + @Handle
     * * Example:
     * Input: "Dev Chauhan"
     * Output: "devchauhan892@l0"
     * * @param fullName The user's full name (e.g., "Dev Chauhan")
     * @param phoneNumber The user's phone number (Optional, can be used for fallback)
     * @return User-friendly VPA
     */
    public static String generateVpa( String phoneNumber, String bankName) {
        String tmpString = phoneNumber;
       if(phoneNumber.startsWith("+91")){
           tmpString = phoneNumber.substring(3);
       }
       return  tmpString + "@ok" + bankName.toLowerCase();
    }
}
