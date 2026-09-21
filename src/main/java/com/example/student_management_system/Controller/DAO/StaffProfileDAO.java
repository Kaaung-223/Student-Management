package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.StaffInfo;

import java.sql.*;

/**
 * Staff profile data access.
 *
 * IMPORTANT:
 *  - Nothing in this class ever writes to the `staff` table any more.
 *    All updates (name, password) go to the `users` table only.
 *  - The `staff` table is still READ for displaying code / salary / etc.
 */
public class StaffProfileDAO {

    // =========================================================
    //  LOAD PROFILE  (read-only)
    // =========================================================
    public StaffInfo loadProfile(int staffId) {
        String sql =
                "SELECT s.staff_id, s.user_id, s.staff_code, s.staff_name, " +
                        "       s.email, s.phone, s.gender, s.address, s.salary, " +
                        "       s.photo_path AS s_photo, u.full_name, u.username, " +
                        "       u.photo_path AS u_photo " +
                        "FROM staff s " +
                        "JOIN users u ON u.user_id = s.user_id " +
                        "WHERE s.staff_id = ?";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StaffInfo si = new StaffInfo();
                    si.setStaffId(rs.getInt("staff_id"));
                    si.setUserId(rs.getInt("user_id"));
                    si.setStaffCode(rs.getString("staff_code"));
                    si.setStaffName(rs.getString("staff_name"));
                    si.setEmail(rs.getString("email"));
                    si.setPhone(rs.getString("phone"));
                    si.setGender(rs.getString("gender"));
                    si.setAddress(rs.getString("address"));
                    si.setSalary(rs.getBigDecimal("salary"));
                    si.setFullName(rs.getString("full_name"));
                    si.setUsername(rs.getString("username"));

                    // Prefer users.photo_path, fall back to staff.photo_path
                    String photo = rs.getString("u_photo");
                    if (photo == null || photo.isBlank()) photo = rs.getString("s_photo");
                    si.setPhotoPath(photo);

                    return si;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // =========================================================
    //  UPDATE NAME  →  users table ONLY
    // =========================================================
    public boolean updateName(int userId, String newName) {
        String sql = "UPDATE users SET full_name = ? WHERE user_id = ?";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newName);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    //  VERIFY CURRENT PASSWORD
    // =========================================================
    public boolean checkPassword(int userId, String password) {
        String sql = "SELECT 1 FROM users WHERE user_id = ? AND password = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // =========================================================
    //  UPDATE PASSWORD  →  users table ONLY
    // =========================================================
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
}