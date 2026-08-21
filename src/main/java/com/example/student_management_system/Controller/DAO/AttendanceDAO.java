package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.AttendanceSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Matches: attendance(attendance_id, student_id, teacher_id, attendance_date, status, remarks)
 *          status ENUM('Present','Absent','Late')
 *
 * attendance has no class_id of its own, so filtering by batch means joining
 * through students.class_id.
 *
 * period is one of: "This Week", "This Month", "This Year", "All Time"
 * (matches whatever you put in cmbAttendancePeriod's items). batchId = -1 means all batches.
 */
public class AttendanceDAO {

    public AttendanceSummary getAttendanceSummary(String period, int batchId) {
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "  SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) AS present_count, " +
                        "  SUM(CASE WHEN a.status = 'Absent'  THEN 1 ELSE 0 END) AS absent_count, " +
                        "  SUM(CASE WHEN a.status = 'Late'    THEN 1 ELSE 0 END) AS late_count " +
                        "FROM attendance a " +
                        "JOIN students s ON a.student_id = s.student_id " +
                        "WHERE 1=1 ");

        if (period != null) {
            switch (period) {
                case "This Week":
                    sql.append("AND a.attendance_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) ");
                    break;
                case "This Month":
                    sql.append("AND MONTH(a.attendance_date) = MONTH(CURDATE()) AND YEAR(a.attendance_date) = YEAR(CURDATE()) ");
                    break;
                case "This Year":
                    sql.append("AND YEAR(a.attendance_date) = YEAR(CURDATE()) ");
                    break;
                default: // "All Time" or null -> no extra filter
                    break;
            }
        }
        if (batchId != -1) {
            sql.append("AND s.class_id = ? ");
        }

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            if (batchId != -1) {
                ps.setInt(1, batchId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AttendanceSummary(
                            rs.getInt("present_count"),
                            rs.getInt("absent_count"),
                            rs.getInt("late_count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new AttendanceSummary(0, 0, 0);
    }
}
