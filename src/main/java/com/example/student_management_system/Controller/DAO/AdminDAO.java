package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.Admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Matches: users(user_id, full_name, username, password, role, status, created_at)
 * There's no separate admins table — an admin is just a users row with role = 'ADMIN'.
 * (Your seed data: username 'admin', full_name 'Kaung Min Khant'.)
 */
public class AdminDAO {

    /** Look up an admin by user_id — use this if your login session stores the id. */
    public Admin getAdminById(int userId) {
        String sql = "SELECT user_id, full_name, username FROM users WHERE user_id = ? AND role = 'ADMIN'";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Admin(
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Look up an admin by username — use this if your login session stores the username instead. */
    public Admin getAdminByUsername(String username) {
        String sql = "SELECT user_id, full_name, username FROM users WHERE username = ? AND role = 'ADMIN'";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Admin(
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
