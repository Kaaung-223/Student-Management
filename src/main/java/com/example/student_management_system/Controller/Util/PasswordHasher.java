package com.example.student_management_system.Controller.Util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt wrapper — the single source of truth for hashing in the whole app.
 * EVERY role (ADMIN / TEACHER / STAFF) uses these three methods.
 */
public final class PasswordHasher {

    private static final int WORK_FACTOR = 12;

    private PasswordHasher() {}

    /** Hash a plaintext password → 60-char "$2a$12$..." hash. */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /** Constant-time compare of plaintext vs. stored BCrypt hash. */
    public static boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || storedHash.isEmpty()) return false;
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** True if the value is already a BCrypt hash. */
    public static boolean isHashed(String value) {
        if (value == null || value.length() < 4) return false;
        return value.startsWith("$2a$")
                || value.startsWith("$2b$")
                || value.startsWith("$2y$");
    }
}