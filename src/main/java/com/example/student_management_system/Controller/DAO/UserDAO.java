package com.example.student_management_system.Controller.DAO;


import com.example.student_management_system.Controller.Model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public User getUserById(int userId) {
        String query = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DBConnention.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(rs.getString("role"));
                    user.setProfileImage(rs.getString("profile_image"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isEmailTakenByOtherUser(String email, int currentUserId) {
        String query = "SELECT COUNT(*) FROM users WHERE email = ? AND user_id != ?";
        try (Connection conn = DBConnention.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setInt(2, currentUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateUserProfile(User user, boolean updatePassword) {
        String query;
        if (updatePassword) {
            query = "UPDATE users SET name = ?, email = ?, password = ?, profile_image = ? WHERE user_id = ?";
        } else {
            query = "UPDATE users SET name = ?, email = ?, profile_image = ? WHERE user_id = ?";
        }

        try (Connection conn = DBConnention.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());

            if (updatePassword) {
                stmt.setString(3, user.getPassword());
                stmt.setString(4, user.getProfileImage());
                stmt.setInt(5, user.getUserId());
            } else {
                stmt.setString(3, user.getProfileImage());
                stmt.setInt(4, user.getUserId());
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}