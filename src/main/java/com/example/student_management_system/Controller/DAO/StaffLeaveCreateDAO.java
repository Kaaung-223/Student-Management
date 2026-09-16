package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.PaymentStudent;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StaffLeaveCreateDAO {

    // ==================================================
    //  Active students for the picker
    // ==================================================
    public List<PaymentStudent> findActiveStudents(int batchId, String search) {
        List<PaymentStudent> list = new ArrayList<>();

        String sql =
                "SELECT s.student_id, s.class_id, s.student_code, s.student_name, " +
                        "       c.class_name, s.status " +
                        "FROM students s " +
                        "LEFT JOIN classes c ON c.class_id = s.class_id " +
                        "WHERE s.status = 'ACTIVE' " +
                        "AND (? = -1 OR s.class_id = ?) " +
                        "AND (s.student_name LIKE ? OR s.student_code LIKE ?) " +
                        "ORDER BY s.student_name";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String like = "%" + (search == null ? "" : search.trim()) + "%";
            ps.setInt(1, batchId);
            ps.setInt(2, batchId);
            ps.setString(3, like);
            ps.setString(4, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PaymentStudent s = new PaymentStudent();
                    s.setStudentId(rs.getInt("student_id"));
                    s.setClassId(rs.getInt("class_id"));
                    s.setStudentCode(rs.getString("student_code"));
                    s.setStudentName(rs.getString("student_name"));
                    s.setBatchName(rs.getString("class_name"));
                    s.setStatus(rs.getString("status"));
                    list.add(s);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ==================================================
    //  Batches
    // ==================================================
    public List<Batch> getAllBatches() {
        List<Batch> list = new ArrayList<>();
        String sql = "SELECT class_id, class_name FROM classes ORDER BY class_name";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new Batch(rs.getInt("class_id"), rs.getString("class_name")));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ==================================================
    //  Overlap check — is there already a leave request for this student
    //  overlapping the given date range?
    // ==================================================
    public boolean hasOverlap(int studentId, LocalDate from, LocalDate to) {
        String sql =
                "SELECT 1 FROM leave_requests " +
                        "WHERE student_id = ? " +
                        "AND status IN ('Pending','Approved') " +
                        "AND NOT (leave_to < ? OR leave_from > ?) " +
                        "LIMIT 1";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ==================================================
    //  Insert a new leave request
    // ==================================================
    public int addLeaveRequest(int studentId, LocalDate from, LocalDate to, String reason) {
        String sql =
                "INSERT INTO leave_requests (student_id, leave_from, leave_to, reason, status, approved_by) " +
                        "VALUES (?, ?, ?, ?, 'Pending', NULL)";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, studentId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ps.setString(4, reason);

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
}