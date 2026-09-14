package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.TeacherInfo;

import java.sql.*;

public class TeacherProfileDAO {

    // ==================================================
    //  Load full profile
    // ==================================================
    public TeacherInfo loadProfile(int teacherId) {
        String sql =
                "SELECT t.teacher_id, t.user_id, t.teacher_name, t.teacher_code, " +
                        "       t.email, t.phone, t.gender, t.address, " +
                        "       t.photo_path AS t_photo, u.full_name, u.username, " +
                        "       u.photo_path AS u_photo " +
                        "FROM teachers t " +
                        "JOIN users u ON u.user_id = t.user_id " +
                        "WHERE t.teacher_id = ?";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TeacherInfo ti = new TeacherInfo();
                    ti.setTeacherId(rs.getInt("teacher_id"));
                    ti.setUserId(rs.getInt("user_id"));
                    ti.setTeacherName(rs.getString("teacher_name"));
                    ti.setTeacherCode(rs.getString("teacher_code"));
                    ti.setEmail(rs.getString("email"));
                    ti.setPhone(rs.getString("phone"));
                    ti.setGender(rs.getString("gender"));
                    ti.setAddress(rs.getString("address"));
                    ti.setFullName(rs.getString("full_name"));
                    ti.setUsername(rs.getString("username"));

                    String photo = rs.getString("t_photo");
                    if (photo == null || photo.isBlank()) photo = rs.getString("u_photo");
                    ti.setPhotoPath(photo);

                    return ti;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==================================================
    //  Update display name (users.full_name + teachers.teacher_name)
    // ==================================================
    public boolean updateName(int userId, int teacherId, String newName) {
        String userSql    = "UPDATE users    SET full_name    = ? WHERE user_id    = ?";
        String teacherSql = "UPDATE teachers SET teacher_name = ? WHERE teacher_id = ?";

        try (Connection con = DBConnention.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(userSql)) {
                ps.setString(1, newName);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(teacherSql)) {
                ps.setString(1, newName);
                ps.setInt(2, teacherId);
                ps.executeUpdate();
            }

            con.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================================================
    //  Verify current password
    // ==================================================
    public boolean checkPassword(int userId, String password) {
        String sql = "SELECT 1 FROM users WHERE user_id = ? AND password = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================================================
    //  Update password
    // ==================================================
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}