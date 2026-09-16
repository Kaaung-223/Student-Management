package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.StaffStudentDetails;

import java.math.BigDecimal;
import java.sql.*;

public class StaffStudentDetailsDAO {

    /** Loads the extended details for a single student. Never returns null. */
    public StaffStudentDetails getDetails(int studentId) {
        StaffStudentDetails d = new StaffStudentDetails();

        String sql =
                "SELECT " +
                        "  s.phone, s.admission_date, " +
                        "  (SELECT COUNT(*) FROM attendance a WHERE a.student_id = s.student_id AND a.status='Present') AS present_count, " +
                        "  (SELECT COUNT(*) FROM attendance a WHERE a.student_id = s.student_id AND a.status='Absent')  AS absent_count, " +
                        "  (SELECT COUNT(*) FROM attendance a WHERE a.student_id = s.student_id AND a.status='Late')    AS late_count, " +
                        "  (SELECT COUNT(*) FROM grades g WHERE g.student_id = s.student_id AND g.result='PASS')        AS pass_count, " +
                        "  (SELECT COUNT(*) FROM grades g WHERE g.student_id = s.student_id AND g.result='FAIL')        AS fail_count, " +
                        "  (SELECT e.exam_name FROM grades g2 " +
                        "        JOIN exams e ON e.exam_id = g2.exam_id " +
                        "        WHERE g2.student_id = s.student_id " +
                        "        ORDER BY g2.created_at DESC LIMIT 1)                                                    AS latest_result, " +
                        "  (SELECT COUNT(*) FROM leave_requests lr WHERE lr.student_id = s.student_id)                   AS leave_count, " +
                        "  COALESCE(cf.total_fee, " +
                        "      (SELECT se.course_fee FROM student_enrollments se " +
                        "       WHERE se.student_id = s.student_id ORDER BY se.enrollment_id DESC LIMIT 1), 0) AS total_fee, " +
                        "  (SELECT COALESCE(SUM(sp.amount),0) FROM student_payments sp " +
                        "        WHERE sp.student_id = s.student_id)                                                     AS total_paid " +
                        "FROM students s " +
                        "LEFT JOIN classes c ON c.class_id = s.class_id " +
                        "LEFT JOIN class_fees cf ON cf.class_id = s.class_id " +
                        "WHERE s.student_id = ?";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    d.setPhone(rs.getString("phone"));

                    Date ad = rs.getDate("admission_date");
                    if (ad != null) d.setAdmissionDate(ad.toLocalDate());

                    d.setPresentCount(rs.getInt("present_count"));
                    d.setAbsentCount(rs.getInt("absent_count"));
                    d.setLateCount(rs.getInt("late_count"));

                    d.setPassCount(rs.getInt("pass_count"));
                    d.setFailCount(rs.getInt("fail_count"));

                    String latest = rs.getString("latest_result");
                    d.setLatestResult((latest == null || latest.isBlank()) ? "--" : latest);

                    d.setLeaveCount(rs.getInt("leave_count"));

                    BigDecimal fee  = rs.getBigDecimal("total_fee");
                    BigDecimal paid = rs.getBigDecimal("total_paid");

                    d.setTotalFee(fee == null ? BigDecimal.ZERO : fee);
                    d.setTotalPaid(paid == null ? BigDecimal.ZERO : paid);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return d;
    }

    /** ✅ NEW — update the student's status (ACTIVE / INACTIVE). */
    public boolean updateStatus(int studentId, String newStatus) {
        String sql = "UPDATE students SET status = ? WHERE student_id = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}