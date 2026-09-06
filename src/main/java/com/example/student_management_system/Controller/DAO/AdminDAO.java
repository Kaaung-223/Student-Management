package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.Admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class AdminDAO {

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

    public Admin getAdminProfileById(int userId) {
        String sql = "SELECT user_id, full_name, username, status, photo_path, created_at " +
                "FROM users WHERE user_id = ? AND role = 'ADMIN'";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    return new Admin(
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("username"),
                            rs.getString("status"),
                            rs.getString("photo_path"),
                            ts != null ? ts.toLocalDateTime() : null);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isUsernameTakenByOthers(String username, int currentUserId) {
        String sql = "SELECT user_id FROM users WHERE username = ? AND user_id <> ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, currentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateProfile(int userId, String fullName, String username, String photoPath) {
        String sql = (photoPath != null)
                ? "UPDATE users SET full_name = ?, username = ?, photo_path = ? WHERE user_id = ?"
                : "UPDATE users SET full_name = ?, username = ? WHERE user_id = ?";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int idx = 1;
            ps.setString(idx++, fullName);
            ps.setString(idx++, username);
            if (photoPath != null) {
                ps.setString(idx++, photoPath);
            }
            ps.setInt(idx, userId);

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean verifyCurrentPassword(int userId, String plainPassword) {
        String sql = "SELECT password FROM users WHERE user_id = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString("password");
                    return stored != null && stored.equals(plainPassword);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}