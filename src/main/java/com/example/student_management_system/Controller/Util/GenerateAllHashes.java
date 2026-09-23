package com.example.student_management_system.Controller.Util;

/**
 * Prints a ready-to-paste SQL INSERT block with BCrypt hashes.
 * Right-click → Run 'GenerateAllHashes.main()'.
 *
 * ⚠️ BCrypt uses a random salt → each run gives DIFFERENT hashes.
 *    All hashes verify against the same plaintext. Pick one run and
 *    paste that output into your SQL file.
 */
public class GenerateAllHashes {

    public static void main(String[] args) {

        // full_name, username, plaintext_password, role, email
        String[][] users = {
                {"Kaung Min Khant", "admin",  "admin123",   "ADMIN",   "admin@school.com"},
                {"John Smith",      "jsmith", "teach123",   "TEACHER", "jsmith@school.com"},
                {"Emily Chen",      "echen",  "teach123",   "TEACHER", "echen@school.com"},
                {"Michael Lee",     "mlee",   "teach123",   "TEACHER", "mlee@school.com"},
                {"John",            "john",   "John123!@#", "STAFF",   "john123@gmail.com"},
                {"Mike",            "mike",   "Mike123!@#", "STAFF",   "mike123@gmail.com"},
        };

        String photoPath = "/images/default-profile.jpg";

        System.out.println("-- ============================================================");
        System.out.println("--  Auto-generated BCrypt hashes — paste into student_management.sql");
        System.out.println("--  Generated: " + java.time.LocalDateTime.now());
        System.out.println("-- ============================================================");
        System.out.println();
        System.out.println("USE student_management;");
        System.out.println();
        System.out.println("INSERT INTO users");
        System.out.println("    (full_name, username, password, role, email, photo_path)");
        System.out.println("VALUES");

        for (int i = 0; i < users.length; i++) {
            String fullName = users[i][0];
            String username = users[i][1];
            String plain    = users[i][2];
            String role     = users[i][3];
            String email    = users[i][4];

            String hash  = PasswordHasher.hash(plain);
            String comma = (i < users.length - 1) ? "," : ";";

            System.out.printf("('%s', '%s', '%s', '%s', '%s', '%s')%s%n",
                    fullName, username, hash, role, email, photoPath, comma);
        }

        System.out.println();
        System.out.println("-- ✅ Verification (each hash must verify against its plaintext):");
        for (String[] u : users) {
            String fresh = PasswordHasher.hash(u[2]);
            boolean ok = PasswordHasher.verify(u[2], fresh);
            System.out.printf("--   %-8s  plaintext=%-12s  →  %s%n",
                    u[1], u[2], ok ? "OK ✅" : "FAIL ❌");
        }
    }
}