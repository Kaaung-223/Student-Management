package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Util.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    /** Create a user — password is hashed before hitting the DB. */
    public void createUser(String fullName, String username,
                           String rawPassword, String role,
                           String email, String photoPath) throws Exception {

        String sql = "INSERT INTO users " +
                "(full_name, username, password, role, email, photo_path) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, username);
            ps.setString(3, PasswordHasher.hash(rawPassword));  // ← HASH
            ps.setString(4, role);
            ps.setString(5, email);
            ps.setString(6, photoPath);
            ps.executeUpdate();
        }
    }

    /** Change a user's password — new value is hashed. */
    public void updatePassword(int userId, String newRawPassword) throws Exception {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, PasswordHasher.hash(newRawPassword)); // ← HASH
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /** Login check — uses BCrypt verify(). */
    public boolean login(String username, String rawPassword) throws Exception {
        String sql = "SELECT password, status FROM users WHERE username = ?";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;

                if (!"ACTIVE".equalsIgnoreCase(rs.getString("status"))) {
                    return false;
                }

                String stored = rs.getString("password");
                return PasswordHasher.verify(rawPassword, stored);   // ← VERIFY
            }
        }
    }
}