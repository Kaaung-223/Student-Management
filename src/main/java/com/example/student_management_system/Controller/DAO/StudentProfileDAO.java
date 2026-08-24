package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.PerformancePeriod;
import com.example.student_management_system.Controller.Model.StudentPerformance;
import com.example.student_management_system.Controller.Model.StudentProfile;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class StudentProfileDAO {
    private static final String SQL = "SELECT s.student_id,s.student_code,s.student_name,s.email,s.phone,s.class_id,s.admission_date,s.status," +
            "COALESCE(c.class_name,'Not assigned') batch_name," +
            "(SELECT COUNT(*) FROM attendance a WHERE a.student_id=s.student_id AND a.status='Present') present_count," +
            "(SELECT COUNT(*) FROM attendance a WHERE a.student_id=s.student_id AND a.status='Absent') absent_count," +
            "(SELECT COUNT(*) FROM attendance a WHERE a.student_id=s.student_id AND a.status='Late') late_count," +
            "(SELECT COUNT(*) FROM leave_requests l WHERE l.student_id=s.student_id AND l.status IN ('Pending','Approved')) leave_count," +
            "(SELECT COUNT(*) FROM grades g WHERE g.student_id=s.student_id AND g.result='PASS') pass_count," +
            "(SELECT COUNT(*) FROM grades g WHERE g.student_id=s.student_id AND g.result='FAIL') fail_count," +
            "(SELECT CONCAT(e.exam_name,' - ',g.result) FROM grades g JOIN exams e ON e.exam_id=g.exam_id WHERE g.student_id=s.student_id ORDER BY e.exam_date DESC,g.grade_id DESC LIMIT 1) latest_result " +
            "FROM students s LEFT JOIN classes c ON c.class_id=s.class_id ";

    public List<StudentProfile> findStudents(int classId, String search) {
        List<StudentProfile> list = new ArrayList<>();
        String q = SQL + "WHERE (?=-1 OR s.class_id=?) AND (s.student_name LIKE ? OR s.student_code LIKE ? OR s.email LIKE ? OR s.phone LIKE ?) ORDER BY s.student_name";
        try (Connection con = DBConnention.getConnection(); PreparedStatement ps = con.prepareStatement(q)) {
            String term = "%" + (search == null ? "" : search.trim()) + "%";
            ps.setInt(1, classId);
            ps.setInt(2, classId);
            ps.setString(3, term);
            ps.setString(4, term);
            ps.setString(5, term);
            ps.setString(6, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private StudentProfile map(ResultSet r) throws SQLException {
        StudentProfile s = new StudentProfile();
        s.setStudentId(r.getInt("student_id"));
        s.setClassId(r.getInt("class_id"));
        s.setStudentCode(r.getString("student_code"));
        s.setStudentName(r.getString("student_name"));
        s.setEmail(r.getString("email"));
        s.setPhone(r.getString("phone"));
        s.setBatchName(r.getString("batch_name"));
        s.setStatus(r.getString("status"));
        Date d = r.getDate("admission_date");
        if (d != null) s.setAdmissionDate(d.toLocalDate());
        s.setPresentCount(r.getInt("present_count"));
        s.setAbsentCount(r.getInt("absent_count"));
        s.setLateCount(r.getInt("late_count"));
        s.setLeaveCount(r.getInt("leave_count"));
        s.setPassCount(r.getInt("pass_count"));
        s.setFailCount(r.getInt("fail_count"));
        s.setLatestResult(r.getString("latest_result"));
        return s;
    }
    public List<PerformancePeriod> getPerformancePeriods(int studentId) {
        List<PerformancePeriod> periods = new ArrayList<>();

        periods.add(new PerformancePeriod("All Academic Year", null, null));

        String sql =
                "SELECT DISTINCT attendance_year, attendance_month, month_name " +
                        "FROM ( " +
                        "   SELECT YEAR(attendance_date) AS attendance_year, " +
                        "          MONTH(attendance_date) AS attendance_month, " +
                        "          DATE_FORMAT(attendance_date, '%M %Y') AS month_name " +
                        "   FROM attendance " +
                        "   WHERE student_id=? " +
                        "   UNION " +
                        "   SELECT YEAR(e.exam_date), MONTH(e.exam_date), " +
                        "          DATE_FORMAT(e.exam_date, '%M %Y') " +
                        "   FROM grades g " +
                        "   JOIN exams e ON e.exam_id=g.exam_id " +
                        "   WHERE g.student_id=? AND e.exam_date IS NOT NULL " +
                        ") AS periods " +
                        "ORDER BY attendance_year DESC, attendance_month DESC";

        try (
                Connection con = DBConnention.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, studentId);
            ps.setInt(2, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    periods.add(new PerformancePeriod(
                            rs.getString("month_name"),
                            rs.getInt("attendance_year"),
                            rs.getInt("attendance_month")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return periods;
    }

    public StudentPerformance getStudentPerformance(
            int studentId,
            Integer year,
            Integer month
    ) {
        int present = 0;
        int absent = 0;
        int late = 0;
        double gpa = 0.0;

        String attendanceSql =
                "SELECT " +
                        "COALESCE(SUM(status='Present'), 0) AS present_count, " +
                        "COALESCE(SUM(status='Absent'), 0) AS absent_count, " +
                        "COALESCE(SUM(status='Late'), 0) AS late_count " +
                        "FROM attendance " +
                        "WHERE student_id=? " +
                        "AND (? IS NULL OR YEAR(attendance_date)=?) " +
                        "AND (? IS NULL OR MONTH(attendance_date)=?)";

        String gpaSql =
                "SELECT COALESCE(AVG(CASE " +
                        "WHEN percentage >= 90 THEN 4.0 " +
                        "WHEN percentage >= 80 THEN 3.5 " +
                        "WHEN percentage >= 70 THEN 3.0 " +
                        "WHEN percentage >= 60 THEN 2.5 " +
                        "WHEN percentage >= 50 THEN 2.0 " +
                        "ELSE 0.0 END), 0) AS gpa " +
                        "FROM grades g " +
                        "JOIN exams e ON e.exam_id=g.exam_id " +
                        "WHERE g.student_id=? " +
                        "AND (? IS NULL OR YEAR(e.exam_date)=?) " +
                        "AND (? IS NULL OR MONTH(e.exam_date)=?)";

        try (Connection con = DBConnention.getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(attendanceSql)) {
                ps.setInt(1, studentId);

                if (year == null) {
                    ps.setNull(2, java.sql.Types.INTEGER);
                    ps.setNull(3, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(2, year);
                    ps.setInt(3, year);
                }

                if (month == null) {
                    ps.setNull(4, java.sql.Types.INTEGER);
                    ps.setNull(5, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(4, month);
                    ps.setInt(5, month);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        present = rs.getInt("present_count");
                        absent = rs.getInt("absent_count");
                        late = rs.getInt("late_count");
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(gpaSql)) {
                ps.setInt(1, studentId);

                if (year == null) {
                    ps.setNull(2, java.sql.Types.INTEGER);
                    ps.setNull(3, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(2, year);
                    ps.setInt(3, year);
                }

                if (month == null) {
                    ps.setNull(4, java.sql.Types.INTEGER);
                    ps.setNull(5, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(4, month);
                    ps.setInt(5, month);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        gpa = rs.getDouble("gpa");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        int totalAttendance = present + absent + late;

        double attendanceRate = totalAttendance == 0
                ? 0.0
                : (present * 100.0) / totalAttendance;

        return new StudentPerformance(
                present,
                absent,
                late,
                attendanceRate,
                gpa
        );
    }
}
