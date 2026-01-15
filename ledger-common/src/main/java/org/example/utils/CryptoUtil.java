package org.example.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for cryptographic operations.
 * Used for hashing MPINs before transmission.
 */
public class CryptoUtil {

    private static final String SHA_256 = "SHA-256";
    private static final int SALT_LENGTH = 16; // 128-bit salt


    /**
     * Generates a cryptographically secure random salt.
     *
     * @return Base64 encoded salt
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a plain text MPIN using SHA-256.
     * This is called by the Gateway before sending to Switch.
     *
     * @param plainMpin The raw MPIN entered by user (e.g., "1234")
     * @return Base64 encoded SHA-256 hash
     */
    public static String hashMpin(String plainMpin) {
        if (plainMpin == null || plainMpin.isEmpty()) {
            throw new IllegalArgumentException("MPIN cannot be null or empty");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hashBytes = digest.digest(plainMpin.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }


    /**
     * Hashes MPIN with user-specific salt using SHA-256.
     * hash = SHA256(salt + mpin)
     *
     * @param plainMpin Raw MPIN (e.g. "1234")
     * @param salt      Base64 encoded salt
     * @return Base64 encoded hash
     */
    public static String hashMpinWithSalt(String plainMpin, String salt) {
        if (plainMpin == null || plainMpin.isEmpty()) {
            throw new IllegalArgumentException("MPIN cannot be null or empty");
        }
        if (salt == null || salt.isEmpty()) {
            throw new IllegalArgumentException("Salt cannot be null or empty");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);

            byte[] saltBytes = Base64.getDecoder().decode(salt);
            byte[] mpinBytes = plainMpin.getBytes(StandardCharsets.UTF_8);

            // salt + mpin
            byte[] combined = new byte[saltBytes.length + mpinBytes.length];
            System.arraycopy(saltBytes, 0, combined, 0, saltBytes.length);
            System.arraycopy(mpinBytes, 0, combined, saltBytes.length, mpinBytes.length);

            byte[] hashBytes = digest.digest(combined);
            return Base64.getEncoder().encodeToString(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }


    /**
     * Verifies if a plain MPIN matches a stored hash.
     * This is called by the Bank to verify user identity.
     *
     * @param plainMpin    The MPIN to verify
     * @param existingHash The stored hash to compare against
     * @return true if the hash matches, false otherwise
     */
    public static boolean verifyMpin(String plainMpin, String existingHash) {
        if (plainMpin == null || existingHash == null) {
            return false;
        }
        String computedHash = hashMpin(plainMpin);
        return computedHash.equals(existingHash);
    }



    /**
     * Verifies MPIN using stored salt and hash.
     *
     * @param plainMpin   MPIN entered by user
     * @param storedSalt  Salt stored in DB
     * @param storedHash  Hash stored in DB
     * @return true if MPIN is valid
     */
    public static boolean verifyMpin(
            String plainMpin,
            String storedSalt,
            String storedHash
    ) {
        if (plainMpin == null || storedSalt == null || storedHash == null) {
            return false;
        }

        String computedHash = hashMpinWithSalt(plainMpin, storedSalt);
        return computedHash.equals(storedHash);
    }


    /**
     * Generates a SHA-256 hash for any generic string.
     * Can be used for generating transaction signatures.
     *
     * @param input The input string to hash
     * @return Hex-encoded SHA-256 hash
     */
    public static String sha256Hex(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Converts byte array to hexadecimal string.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
