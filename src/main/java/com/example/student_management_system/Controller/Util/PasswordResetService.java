package com.example.student_management_system.Controller.Util;

import com.example.student_management_system.Controller.DAO.DBConnention;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles OTP lifecycle and password reset for ALL roles
 * (ADMIN, TEACHER, STAFF). The lookup uses the users table with
 * NO role filter, so every account type flows through the same code.
 */
public class PasswordResetService {

    /** OTP validity window (seconds). Change here if you want longer. */
    public static final int OTP_VALID_SECONDS = 30;

    private static final int MAX_VERIFY_ATTEMPTS   = 5;
    private static final int VERIFIED_GRACE_SECONDS = 300; // 5 min to type new password

    private static final SecureRandom RANDOM = new SecureRandom();

    public static class UserInfo {
        public int    userId;
        public String fullName;
        public String email;
        public String role;   // ADMIN / TEACHER / STAFF (for logging/UI only)
    }

    public enum VerifyResult { OK, EXPIRED, WRONG, NO_OTP, TOO_MANY }

    // ---------------------------------------------------------
    // Lookup by email — role-agnostic
    // ---------------------------------------------------------
    public static UserInfo findUserByEmail(String email) throws SQLException {
        // ADMIN, TEACHER, STAFF all live in `users`. No role filter here.
        String sql = "SELECT user_id, full_name, email, role, status "
                + "FROM users "
                + "WHERE LOWER(email) = LOWER(?) "
                + "  AND email IS NOT NULL "
                + "  AND email <> ''";
        try (Connection c = DBConnention.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                if (!"ACTIVE".equalsIgnoreCase(rs.getString("status"))) return null;

                UserInfo u = new UserInfo();
                u.userId   = rs.getInt("user_id");
                u.fullName = rs.getString("full_name");
                u.email    = rs.getString("email");
                u.role     = rs.getString("role");
                return u;
            }
        }
    }

    // ---------------------------------------------------------
    // Create & store a new OTP (deletes previous OTPs for user)
    // ---------------------------------------------------------
    public static String createOtp(UserInfo user) throws SQLException {
        try (Connection c = DBConnention.getConnection()) {

            try (PreparedStatement del = c.prepareStatement(
                    "DELETE FROM password_reset_otps WHERE user_id = ?")) {
                del.setInt(1, user.userId);
                del.executeUpdate();
            }

            String code = String.format("%06d", RANDOM.nextInt(1_000_000));

            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO password_reset_otps (user_id, email, otp_code, expires_at) "
                            + "VALUES (?, ?, ?, DATE_ADD(NOW(), INTERVAL ? SECOND))")) {
                ps.setInt(1, user.userId);
                ps.setString(2, user.email);
                ps.setString(3, code);
                ps.setInt(4, OTP_VALID_SECONDS);
                ps.executeUpdate();
            }
            return code;
        }
    }

    // ---------------------------------------------------------
    // Verify submitted OTP
    // ---------------------------------------------------------
    public static VerifyResult verifyOtp(String email, String otp) throws SQLException {
        String sql =
                "SELECT otp_id, otp_code, attempts, "
                        + "       TIMESTAMPDIFF(SECOND, NOW(), expires_at) AS seconds_left "
                        + "FROM password_reset_otps "
                        + "WHERE LOWER(email) = LOWER(?) AND used = FALSE "
                        + "ORDER BY otp_id DESC LIMIT 1";

        try (Connection c = DBConnention.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return VerifyResult.NO_OTP;

                int    otpId       = rs.getInt("otp_id");
                String stored      = rs.getString("otp_code");
                int    secondsLeft = rs.getInt("seconds_left");
                int    attempts    = rs.getInt("attempts");

                if (attempts >= MAX_VERIFY_ATTEMPTS) return VerifyResult.TOO_MANY;

                if (secondsLeft <= 0) {
                    markUsed(c, otpId);
                    return VerifyResult.EXPIRED;
                }

                if (!stored.equals(otp.trim())) {
                    try (PreparedStatement up = c.prepareStatement(
                            "UPDATE password_reset_otps SET attempts = attempts + 1 WHERE otp_id = ?")) {
                        up.setInt(1, otpId);
                        up.executeUpdate();
                    }
                    return VerifyResult.WRONG;
                }

                try (PreparedStatement up = c.prepareStatement(
                        "UPDATE password_reset_otps "
                                + "SET verified = TRUE, "
                                + "    expires_at = DATE_ADD(NOW(), INTERVAL ? SECOND) "
                                + "WHERE otp_id = ?")) {
                    up.setInt(1, VERIFIED_GRACE_SECONDS);
                    up.setInt(2, otpId);
                    up.executeUpdate();
                }
                return VerifyResult.OK;
            }
        }
    }

    // ---------------------------------------------------------
    // Replace password — only allowed if a verified, unused,
    // unexpired OTP exists for that email.
    // ---------------------------------------------------------
    public static boolean resetPassword(String email, String newPassword) throws SQLException {
        String check =
                "SELECT otp_id FROM password_reset_otps "
                        + "WHERE LOWER(email) = LOWER(?) "
                        + "  AND verified = TRUE AND used = FALSE AND expires_at > NOW() "
                        + "ORDER BY otp_id DESC LIMIT 1";

        try (Connection c = DBConnention.getConnection();
             PreparedStatement ps = c.prepareStatement(check)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                int otpId = rs.getInt("otp_id");

                try (PreparedStatement upd = c.prepareStatement(
                        "UPDATE users SET password = ? WHERE LOWER(email) = LOWER(?)")) {
                    upd.setString(1, newPassword);
                    upd.setString(2, email.trim());
                    upd.executeUpdate();
                }

                markUsed(c, otpId);
                return true;
            }
        }
    }

    private static void markUsed(Connection c, int otpId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE password_reset_otps SET used = TRUE WHERE otp_id = ?")) {
            ps.setInt(1, otpId);
            ps.executeUpdate();
        }
    }
}