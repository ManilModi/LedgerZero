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
}