package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.StaffInfo;

import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class StaffDashboardDAO {

    // ==================================================
    //  Staff info by username (for login)
    // ==================================================
    public StaffInfo getStaffByUsername(String username) {
        String sql =
                "SELECT s.staff_id, s.user_id, s.staff_code, s.staff_name, " +
                        "       s.email, s.phone, s.gender, s.address, s.photo_path AS s_photo, s.salary, " +
                        "       u.full_name, u.username, u.photo_path AS u_photo " +
                        "FROM staff s " +
                        "JOIN users u ON u.user_id = s.user_id " +
                        "WHERE u.username = ?";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
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
                    si.setFullName(rs.getString("full_name"));
                    si.setUsername(rs.getString("username"));
                    si.setSalary(rs.getBigDecimal("salary"));

                    String photo = rs.getString("s_photo");
                    if (photo == null || photo.isBlank()) photo = rs.getString("u_photo");
                    si.setPhotoPath(photo);

                    return si;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ==================================================
    //  STAT CARD 1 — total active students
    // ==================================================
    public int getTotalStudents() {
        String sql = "SELECT COUNT(*) FROM students WHERE status = 'ACTIVE'";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // ==================================================
    //  STAT CARD 2 — total classes
    // ==================================================
    public int getTotalClasses() {
        String sql = "SELECT COUNT(*) FROM classes WHERE class_status = 'ACTIVE'";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // ==================================================
    //  STAT CARD 3 — payments collected this month
    // ==================================================
    public BigDecimal getPaymentsThisMonth() {
        String sql =
                "SELECT COALESCE(SUM(amount), 0) " +
                        "FROM student_payments " +
                        "WHERE YEAR(payment_date) = YEAR(CURDATE()) " +
                        "  AND MONTH(payment_date) = MONTH(CURDATE())";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (Exception e) { e.printStackTrace(); }
        return BigDecimal.ZERO;
    }

    // ==================================================
    //  STAT CARD 4 — pending leave requests
    // ==================================================
    public int getPendingLeaveCount() {
        String sql = "SELECT COUNT(*) FROM leave_requests WHERE status = 'Pending'";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // ==================================================
    //  FEE COLLECTION PIE — Paid vs Unpaid students
    //  Based on student_payments existing or not
    // ==================================================
    public Map<String, Integer> getFeePaymentStatus() {
        Map<String, Integer> map = new LinkedHashMap<>();

        String sql =
                "SELECT " +
                        "  SUM(CASE WHEN total_paid >= total_fee THEN 1 ELSE 0 END) AS paid, " +
                        "  SUM(CASE WHEN total_paid <  total_fee THEN 1 ELSE 0 END) AS unpaid " +
                        "FROM ( " +
                        "  SELECT s.student_id, " +
                        "         COALESCE(cf.total_fee, 0) AS total_fee, " +
                        "         COALESCE((SELECT SUM(sp.amount) FROM student_payments sp " +
                        "                   WHERE sp.student_id = s.student_id), 0) AS total_paid " +
                        "  FROM students s " +
                        "  LEFT JOIN class_fees cf ON cf.class_id = s.class_id " +
                        "  WHERE s.status = 'ACTIVE' " +
                        ") t";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                map.put("Paid",   rs.getInt("paid"));
                map.put("Unpaid", rs.getInt("unpaid"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    // ==================================================
    //  PAYMENT METHODS CHART
    // ==================================================
    public Map<String, Integer> getPaymentMethods() {
        Map<String, Integer> map = new LinkedHashMap<>();

        String sql =
                "SELECT payment_method, COUNT(*) AS cnt " +
                        "FROM student_payments " +
                        "WHERE payment_date >= DATE_SUB(CURDATE(), INTERVAL 90 DAY) " +
                        "GROUP BY payment_method " +
                        "ORDER BY cnt DESC";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("payment_method"), rs.getInt("cnt"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    // ==================================================
    //  Optional: total collected (all-time)
    // ==================================================
    public BigDecimal getTotalCollected() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM student_payments";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (Exception e) { e.printStackTrace(); }
        return BigDecimal.ZERO;
    }
}