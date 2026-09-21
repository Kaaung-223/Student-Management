package com.example.student_management_system.Controller.Util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt wrapper for password hashing + verification.
 *
 * Uses jBCrypt (org.mindrot:jbcrypt:0.4) — a single self-contained
 * JAR with zero transitive dependencies, ideal for modular JavaFX apps.
 *
 * Every hash is self-contained: it includes the salt and the cost factor.
 * BCrypt hashes always start with "$2a$", "$2b$", or "$2y$".
 */
public final class PasswordHasher {

    /** Cost factor. 12 is a good balance (~250 ms per hash on modern CPUs). */
    private static final int WORK_FACTOR = 12;

    private PasswordHasher() { /* util class */ }

    /** Hash a plaintext password. Returns a 60-character BCrypt hash. */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /** Constant-time comparison of plaintext vs. stored BCrypt hash. */
    public static boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) return false;
        if (storedHash.isEmpty()) return false;
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (IllegalArgumentException e) {
            // storedHash isn't a valid BCrypt hash
            return false;
        }
    }

    /** True if 'value' looks like a BCrypt hash (used to detect legacy plaintext rows). */
    public static boolean isHashed(String value) {
        if (value == null || value.length() < 4) return false;
        return value.startsWith("$2a$")
                || value.startsWith("$2b$")
                || value.startsWith("$2y$");
    }
}