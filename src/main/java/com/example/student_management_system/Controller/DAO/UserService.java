package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Util.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Central service for EVERY user-related write.
 * All password columns go through PasswordHasher.hash().
 *
 * Methods:
 *   createUser          — new ADMIN / TEACHER / STAFF
 *   updatePassword      — admin resetting a user's password (by id)
 *   updatePasswordByUsername — admin resetting (by username)
 *   changeOwnPassword   — any logged-in user changing their own
 *   verifyLogin         — BCrypt-check a login attempt
 *   isPasswordHashed    — diagnostic helper
 */
public class UserService {

    // ============================================================
    // CREATE USER (any role) — HASHED
    // ============================================================
    public static int createUser(String fullName,
                                 String username,
                                 String email,
                                 String plainPassword,
                                 String role) throws SQLException {

        String sql = "INSERT INTO users "
                + "(full_name, username, email, password, role, status) "
                + "VALUES (?, ?, ?, ?, ?, 'ACTIVE')";

        try (Connection c = DBConnention.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, fullName);
            ps.setString(2, username);
            ps.setString(3, email);
            ps.setString(4, PasswordHasher.hash(plainPassword));   // ← HASH
            ps.setString(5, role);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    // ============================================================
    // UPDATE PASSWORD by user_id — HASHED
    // ============================================================
    public static boolean updatePassword(int userId, String newPlainPassword)
            throws SQLException {

        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection c = DBConnention.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, PasswordHasher.hash(newPlainPassword));   // ← HASH
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    // ============================================================
    // UPDATE PASSWORD by username — HASHED
    // ============================================================
    public static boolean updatePasswordByUsername(String username,
                                                   String newPlainPassword)
            throws SQLException {

        String sql = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection c = DBConnention.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, PasswordHasher.hash(newPlainPassword));   // ← HASH
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        }
    }

    // ============================================================
    // CHANGE OWN PASSWORD — verify old, then hash new
    // ============================================================
    public static boolean changeOwnPassword(String username,
                                            String oldPlainPassword,
                                            String newPlainPassword) throws SQLException {

        String selectSql = "SELECT password FROM users WHERE username = ?";
        String updateSql = "UPDATE users SET password = ? WHERE username = ?";

        try (Connection c = DBConnention.getConnection()) {

            String currentHash;
            try (PreparedStatement ps = c.prepareStatement(selectSql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return false;
                    currentHash = rs.getString("password");
                }
            }

            if (!PasswordHasher.verify(oldPlainPassword, currentHash)) return false;

            try (PreparedStatement ps = c.prepareStatement(updateSql)) {
                ps.setString(1, PasswordHasher.hash(newPlainPassword));   // ← HASH
                ps.setString(2, username);
                return ps.executeUpdate() > 0;
            }
        }
    }

    // ============================================================
    // VERIFY LOGIN — BCrypt check
    // ============================================================
    public static boolean verifyLogin(String username, String plainPassword)
            throws SQLException {

        String sql = "SELECT password FROM users "
                + "WHERE username = ? AND status = 'ACTIVE'";
        try (Connection c = DBConnention.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                return PasswordHasher.verify(plainPassword, rs.getString("password"));
            }
        }
    }

    // ============================================================
    // Diagnostic: is this user's password already hashed?
    // ============================================================
    public static boolean isPasswordHashed(String username) throws SQLException {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection c = DBConnention.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && PasswordHasher.isHashed(rs.getString("password"));
            }
        }
    }
}