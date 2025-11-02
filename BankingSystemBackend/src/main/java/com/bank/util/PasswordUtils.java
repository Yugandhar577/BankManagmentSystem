package com.bank.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

    /**
     * Hashes a plain-text password using SHA-256.
     * @param password The plain-text password.
     * @return A 64-character hex string representing the hash.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Checks a plain-text password against a stored SHA-256 hash.
     * @param plainPassword The password to check.
     * @param hashedPassword The stored hash from the database.
     * @return true if the passwords match, false otherwise.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        String hashOfPlainPassword = hashPassword(plainPassword);
        return hashOfPlainPassword.equals(hashedPassword);
    }
}