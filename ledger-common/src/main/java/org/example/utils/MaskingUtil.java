package org.example.utils;

public class MaskingUtil {

    // Helper to print "1234" as "****" in logs
    public static String maskPin(String pin) {
        if (pin == null || pin.isEmpty()) return "";
        return "*".repeat(pin.length());
    }

    // Helper to print "alice@l0" as "a***@l0"
    public static String maskVpa(String vpa) {
        if (vpa == null || !vpa.contains("@")) return vpa;
        int atIndex = vpa.indexOf("@");
        if (atIndex <= 1) return vpa;
        return vpa.charAt(0) + "***" + vpa.substring(atIndex);
    }

    // helper to print "12343546454" as "****6454" account no masking
    public static String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || !accountNumber.matches("^[0-9]+$")) return null;

        int length = accountNumber.length();
        if (length <= 4) {
            return "****";
        }

        String lastFour = accountNumber.substring(length - 4);
        return "*".repeat(length - 4) + lastFour;
    }
}