package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.LeaveRequestRow;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class StaffLeaveDAO {

    // ==================================================
    //  Batches for the filter
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
    //  Find leave requests
    //    batchId  = -1  → all batches
    //    status   = "All" / "Pending" / "Approved" / "Rejected"
    //    search   → student name / code / reason
    // ==================================================
    public List<LeaveRequestRow> findLeaveRequests(int batchId, String status, String search) {
        List<LeaveRequestRow> list = new ArrayList<>();

        String sql =
                "SELECT lr.leave_id, lr.student_id, lr.leave_from, lr.leave_to, " +
                        "       lr.reason, lr.status, lr.created_at, " +
                        "       s.student_code, s.student_name, c.class_name, " +
                        "       u.full_name AS approved_by_name " +
                        "FROM leave_requests lr " +
                        "JOIN students s ON s.student_id = lr.student_id " +
                        "LEFT JOIN classes c ON c.class_id = s.class_id " +
                        "LEFT JOIN users u ON u.user_id = lr.approved_by " +
                        "WHERE (? = -1 OR s.class_id = ?) " +
                        "AND (? = 'All' OR lr.status = ?) " +
                        "AND (s.student_name LIKE ? OR s.student_code LIKE ? OR lr.reason LIKE ?) " +
                        "ORDER BY " +
                        "  CASE lr.status WHEN 'Pending' THEN 0 ELSE 1 END, " +
                        "  lr.created_at DESC, lr.leave_id DESC";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String like = "%" + (search == null ? "" : search.trim()) + "%";
            String st = (status == null || status.isBlank()) ? "All" : status;

            ps.setInt(1, batchId);
            ps.setInt(2, batchId);
            ps.setString(3, st);
            ps.setString(4, st);
            ps.setString(5, like);
            ps.setString(6, like);
            ps.setString(7, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ==================================================
    //  Approve / Reject
    // ==================================================
    public boolean updateStatus(int leaveId, String newStatus, int approverUserId) {
        String sql = "UPDATE leave_requests SET status = ?, approved_by = ? WHERE leave_id = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            if (approverUserId > 0) ps.setInt(2, approverUserId);
            else                    ps.setNull(2, Types.INTEGER);
            ps.setInt(3, leaveId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================================================
    //  Counts by status (for badges)
    // ==================================================
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM leave_requests WHERE status = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // ==================================================
    private LeaveRequestRow map(ResultSet rs) throws SQLException {
        LeaveRequestRow r = new LeaveRequestRow();
        r.setLeaveId(rs.getInt("leave_id"));
        r.setStudentId(rs.getInt("student_id"));
        r.setStudentCode(rs.getString("student_code"));
        r.setStudentName(rs.getString("student_name"));
        r.setBatchName(rs.getString("class_name"));
        r.setReason(rs.getString("reason"));
        r.setStatus(rs.getString("status"));
        r.setApprovedBy(rs.getString("approved_by_name"));

        Date from = rs.getDate("leave_from");
        Date to   = rs.getDate("leave_to");
        Date createdAt = rs.getDate("created_at");

        if (from != null) r.setLeaveFrom(from.toLocalDate());
        if (to != null)   r.setLeaveTo(to.toLocalDate());
        if (createdAt != null) r.setCreatedAt(createdAt.toLocalDate());

        if (from != null && to != null) {
            long days = ChronoUnit.DAYS.between(
                    from.toLocalDate(), to.toLocalDate()) + 1;
            r.setDaysCount((int) days);
        }
        return r;
    }
}