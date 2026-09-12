package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.BatchFilter;
import com.example.student_management_system.Controller.Model.PerformancePeriod;
import com.example.student_management_system.Controller.Model.TeacherStudent;
import com.example.student_management_system.Controller.DAO.DBConnention;
import com.mysql.cj.xdevapi.DbDoc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherStudentsDAO {

    private static final String[] MONTHS = {
            "", "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    // =========================================
    // Batches (classes) taught by this teacher
    // =========================================
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
                    list.add(new BatchFilter(
                            rs.getInt("class_id"),
                            rs.getString("class_name")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================
    // Students of this teacher's classes
    // =========================================
    public List<TeacherStudent> findStudents(int teacherId, int batchId, String search) {
        List<TeacherStudent> list = new ArrayList<>();

        String sql =
                "SELECT s.student_id, s.student_code, s.student_name, s.email, s.phone, " +
                        "       s.status, s.admission_date, c.class_name, " +
                        "  (SELECT COUNT(*) FROM attendance a WHERE a.student_id = s.student_id AND a.status='Present') AS present_count, " +
                        "  (SELECT COUNT(*) FROM attendance a WHERE a.student_id = s.student_id AND a.status='Absent')  AS absent_count, " +
                        "  (SELECT COUNT(*) FROM attendance a WHERE a.student_id = s.student_id AND a.status='Late')    AS late_count, " +
                        "  (SELECT COUNT(*) FROM grades    g WHERE g.student_id = s.student_id AND g.result='PASS')     AS pass_count, " +
                        "  (SELECT COUNT(*) FROM grades    g WHERE g.student_id = s.student_id AND g.result='FAIL')     AS fail_count, " +
                        "  (SELECT COUNT(*) FROM leave_requests lr WHERE lr.student_id = s.student_id)                 AS leave_count, " +
                        "  (SELECT e.exam_name FROM grades g2 " +
                        "      JOIN exams e ON e.exam_id = g2.exam_id " +
                        "      WHERE g2.student_id = s.student_id " +
                        "      ORDER BY g2.created_at DESC LIMIT 1)                                                   AS latest_result " +
                        "FROM students s " +
                        "LEFT JOIN classes c ON c.class_id = s.class_id " +
                        "WHERE s.class_id IN ( " +
                        "    SELECT DISTINCT c2.class_id FROM classes c2 " +
                        "    LEFT JOIN class_subjects cs ON cs.class_id = c2.class_id " +
                        "    LEFT JOIN teacher_subjects ts ON ts.subject_id = cs.subject_id " +
                        "    WHERE c2.class_teacher_id = ? OR ts.teacher_id = ? " +
                        ") " +
                        "AND (? = -1 OR s.class_id = ?) " +
                        "AND (s.student_name LIKE ? OR s.student_code LIKE ? OR s.email LIKE ?) " +
                        "ORDER BY s.student_name";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String like = "%" + (search == null ? "" : search.trim()) + "%";

            ps.setInt(1, teacherId);
            ps.setInt(2, teacherId);
            ps.setInt(3, batchId);
            ps.setInt(4, batchId);
            ps.setString(5, like);
            ps.setString(6, like);
            ps.setString(7, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================
    // Performance periods for a student
    // Uses the existing (String name, Integer year, Integer month) constructor
    // =========================================
    public List<PerformancePeriod> getPerformancePeriods(int studentId) {
        List<PerformancePeriod> list = new ArrayList<>();

        String sql =
                "SELECT DISTINCT YEAR(created_at) AS y, MONTH(created_at) AS m " +
                        "FROM grades WHERE student_id = ? " +
                        "ORDER BY y DESC, m DESC";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int y = rs.getInt("y");
                    int m = rs.getInt("m");

                    // build the label the same way your class expects
                    String name = MONTHS[m] + " " + y;

                    list.add(new PerformancePeriod(name, y, m));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================
    // Attendance % and GPA for one month
    // returns: [attendanceRate, gpa]
    // =========================================
    public double[] getStudentPerformance(int studentId, int year, int month) {
        double attendanceRate = 0.0;
        double gpa = 0.0;

        String attSql =
                "SELECT COUNT(*) AS total, " +
                        "       SUM(CASE WHEN status='Present' THEN 1 ELSE 0 END) AS present " +
                        "FROM attendance " +
                        "WHERE student_id = ? AND YEAR(attendance_date) = ? AND MONTH(attendance_date) = ?";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(attSql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, year);
            ps.setInt(3, month);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total   = rs.getInt("total");
                    int present = rs.getInt("present");
                    if (total > 0) attendanceRate = (present * 100.0) / total;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String gpaSql =
                "SELECT AVG(percentage) FROM grades " +
                        "WHERE student_id = ? AND YEAR(created_at) = ? AND MONTH(created_at) = ?";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(gpaSql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, year);
            ps.setInt(3, month);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) gpa = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new double[] { attendanceRate, gpa };
    }

    // =========================================
    // Row → TeacherStudent
    // =========================================
    private TeacherStudent map(ResultSet rs) throws SQLException {
        TeacherStudent s = new TeacherStudent();

        s.setStudentId(rs.getInt("student_id"));
        s.setStudentCode(rs.getString("student_code"));
        s.setStudentName(rs.getString("student_name"));
        s.setBatchName(rs.getString("class_name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setStatus(rs.getString("status"));

        Date d = rs.getDate("admission_date");
        if (d != null) s.setAdmissionDate(d.toLocalDate());

        s.setPresentCount(rs.getInt("present_count"));
        s.setAbsentCount(rs.getInt("absent_count"));
        s.setLateCount(rs.getInt("late_count"));
        s.setPassCount(rs.getInt("pass_count"));
        s.setFailCount(rs.getInt("fail_count"));
        s.setLeaveCount(rs.getInt("leave_count"));

        String latest = rs.getString("latest_result");
        s.setLatestResult((latest == null || latest.isBlank()) ? "--" : latest);

        return s;
    }
}