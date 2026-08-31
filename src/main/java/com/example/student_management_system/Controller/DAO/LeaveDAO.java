package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.LeaveRequest;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Matches:
 *   leave_requests(leave_id, student_id, leave_from, leave_to, reason, status, approved_by, created_at)
 *   status ENUM('Pending','Approved','Rejected')
 *   students(student_id, student_name, class_id, ...)
 *   classes(class_id, class_name, ...)
 */
public class LeaveDAO {

    /** All pending leave requests, regardless of batch. */
    public List<LeaveRequest> getPendingLeaveRequests() {
        return getPendingLeaveRequestsByBatch(-1);
    }

    /**
     * Pending leave requests for a given batch (classes.class_id).
     * Pass batchId = -1 to get every batch ("All Batches" combo option).
     */
    public List<LeaveRequest> getPendingLeaveRequestsByBatch(int batchId) {
        List<LeaveRequest> leaves = new ArrayList<>();

        String sql = "SELECT l.leave_id, s.student_name, c.class_name, l.leave_from, l.leave_to, l.status " +
                "FROM leave_requests l " +
                "JOIN students s ON l.student_id = s.student_id " +
                "LEFT JOIN classes c ON s.class_id = c.class_id " +
                "WHERE l.status = 'Pending' ";
        if (batchId != -1) {
            sql += "AND c.class_id = ? ";
        }
        sql += "ORDER BY l.leave_from";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (batchId != -1) {
                ps.setInt(1, batchId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    leaves.add(new LeaveRequest(
                            rs.getInt("leave_id"),
                            rs.getString("student_name"),
                            rs.getString("class_name"),
                            rs.getDate("leave_from").toLocalDate(),
                            rs.getDate("leave_to").toLocalDate(),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leaves;
    }

    public List<LeaveRequest> getLeaveRequests(
            int classId,
            int studentId,
            LocalDate from,
            LocalDate to
    ) {
        List<LeaveRequest> leaves = new ArrayList<>();

        String sql =
                "SELECT l.leave_id, l.student_id, s.student_code, s.student_name, c.class_name, " +
                        "l.leave_from, l.leave_to, l.status, l.reason " +
                        "FROM leave_requests l " +
                        "JOIN students s ON l.student_id = s.student_id " +
                        "LEFT JOIN classes c ON s.class_id = c.class_id " +
                        "WHERE (? = -1 OR s.class_id = ?) " +
                        "AND (? = -1 OR l.student_id = ?) " +
                        "AND (? IS NULL OR l.leave_to >= ?) " +
                        "AND (? IS NULL OR l.leave_from <= ?) " +
                        "ORDER BY l.leave_from DESC, l.leave_id DESC";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, classId);
            ps.setInt(2, classId);
            ps.setInt(3, studentId);
            ps.setInt(4, studentId);
            if (from == null) {
                ps.setNull(5, java.sql.Types.DATE);
                ps.setNull(6, java.sql.Types.DATE);
            } else {
                ps.setDate(5, Date.valueOf(from));
                ps.setDate(6, Date.valueOf(from));
            }
            if (to == null) {
                ps.setNull(7, java.sql.Types.DATE);
                ps.setNull(8, java.sql.Types.DATE);
            } else {
                ps.setDate(7, Date.valueOf(to));
                ps.setDate(8, Date.valueOf(to));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date leaveFrom = rs.getDate("leave_from");
                    Date leaveTo = rs.getDate("leave_to");
                    leaves.add(new LeaveRequest(
                            rs.getInt("leave_id"),
                            rs.getInt("student_id"),
                            rs.getString("student_code"),
                            rs.getString("student_name"),
                            rs.getString("class_name"),
                            leaveFrom == null ? null : leaveFrom.toLocalDate(),
                            leaveTo == null ? null : leaveTo.toLocalDate(),
                            rs.getString("status"),
                            rs.getString("reason")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leaves;
    }
}
