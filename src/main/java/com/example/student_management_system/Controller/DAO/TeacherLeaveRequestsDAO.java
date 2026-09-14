package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.BatchFilter;
import com.example.student_management_system.Controller.Model.LeaveRequestRow;

import java.sql.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TeacherLeaveRequestsDAO {

    // ---------- Batches taught by this teacher ----------
    public List<BatchFilter> findTeacherBatches(int teacherId) {
        List<BatchFilter> list = new ArrayList<>();

        String sql =
                "SELECT DISTINCT c.class_id, c.class_name FROM classes c " +
                        "LEFT JOIN class_subjects cs ON cs.class_id = c.class_id " +
                        "LEFT JOIN teacher_subjects ts ON ts.subject_id = cs.subject_id " +
                        "WHERE c.class_teacher_id = ? OR ts.teacher_id = ? " +
                        "ORDER BY c.class_name";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new BatchFilter(rs.getInt(1), rs.getString(2)));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ---------- Leave requests for students in the teacher's classes ----------
    public List<LeaveRequestRow> findLeaveRequests(int teacherId,
                                                   int batchId,
                                                   String statusFilter,
                                                   String search) {

        List<LeaveRequestRow> list = new ArrayList<>();

        String sql =
                "SELECT lr.leave_id, lr.student_id, s.student_code, s.student_name, " +
                        "       c.class_name, lr.leave_from, lr.leave_to, lr.reason, lr.status, " +
                        "       u.full_name AS approved_by_name " +
                        "FROM leave_requests lr " +
                        "JOIN students s ON s.student_id = lr.student_id " +
                        "LEFT JOIN classes c ON c.class_id = s.class_id " +
                        "LEFT JOIN users u ON u.user_id = lr.approved_by " +
                        "WHERE s.class_id IN ( " +
                        "    SELECT DISTINCT c2.class_id FROM classes c2 " +
                        "    LEFT JOIN class_subjects cs ON cs.class_id = c2.class_id " +
                        "    LEFT JOIN teacher_subjects ts ON ts.subject_id = cs.subject_id " +
                        "    WHERE c2.class_teacher_id = ? OR ts.teacher_id = ? " +
                        ") " +
                        "AND (? = -1 OR s.class_id = ?) " +
                        "AND (? = 'All' OR lr.status = ?) " +
                        "AND (s.student_name LIKE ? OR s.student_code LIKE ? OR lr.reason LIKE ?) " +
                        "ORDER BY lr.created_at DESC, lr.leave_id DESC";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String like = "%" + (search == null ? "" : search.trim()) + "%";
            String status = (statusFilter == null || statusFilter.isBlank()) ? "All" : statusFilter;

            ps.setInt(1, teacherId);
            ps.setInt(2, teacherId);
            ps.setInt(3, batchId);
            ps.setInt(4, batchId);
            ps.setString(5, status);
            ps.setString(6, status);
            ps.setString(7, like);
            ps.setString(8, like);
            ps.setString(9, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LeaveRequestRow r = new LeaveRequestRow();
                    r.setLeaveId(rs.getInt("leave_id"));
                    r.setStudentId(rs.getInt("student_id"));
                    r.setStudentCode(rs.getString("student_code"));
                    r.setStudentName(rs.getString("student_name"));
                    r.setBatchName(rs.getString("class_name"));

                    Date from = rs.getDate("leave_from");
                    Date to   = rs.getDate("leave_to");
                    if (from != null) r.setLeaveFrom(from.toLocalDate());
                    if (to != null)   r.setLeaveTo(to.toLocalDate());

                    if (from != null && to != null) {
                        long days = ChronoUnit.DAYS.between(
                                from.toLocalDate(), to.toLocalDate()) + 1;
                        r.setDaysCount((int) days);
                    }

                    r.setReason(rs.getString("reason"));
                    r.setStatus(rs.getString("status"));
                    r.setApprovedBy(rs.getString("approved_by_name"));
                    list.add(r);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}