package com.example.student_management_system.Controller.DAO;

import java.sql.*;
import java.time.LocalDate;

public class StaffStudentCreateDAO {

    // ---------- uniqueness checks ----------
    public boolean isStudentCodeTaken(String code) {
        if (code == null || code.isBlank()) return false;
        String sql = "SELECT 1 FROM students WHERE student_code = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean isEmailTaken(String email) {
        if (email == null || email.isBlank()) return false;
        String sql = "SELECT 1 FROM students WHERE email = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ---------- insert ----------
    public int addStudent(String studentCode, String studentName,
                          String email, String phone, Integer age,
                          String gender, String address,
                          int classId, LocalDate admissionDate,
                          String status, String photoUrl) {

        String sql =
                "INSERT INTO students (student_code, student_name, email, phone, age, " +
                        "                      gender, address, class_id, admission_date, status, photo_url) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, studentCode);
            ps.setString(2, studentName);
            ps.setString(3, emptyToNull(email));
            ps.setString(4, emptyToNull(phone));
            if (age != null) ps.setInt(5, age);
            else             ps.setNull(5, Types.INTEGER);
            ps.setString(6, emptyToNull(gender));
            ps.setString(7, emptyToNull(address));
            ps.setInt(8, classId);
            if (admissionDate != null) ps.setDate(9, Date.valueOf(admissionDate));
            else                       ps.setDate(9, Date.valueOf(LocalDate.now()));
            ps.setString(10, status == null ? "ACTIVE" : status);
            ps.setString(11, emptyToNull(photoUrl));

            int rows = ps.executeUpdate();
            if (rows == 0) return -1;

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private String emptyToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}