package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.LeaveRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}
