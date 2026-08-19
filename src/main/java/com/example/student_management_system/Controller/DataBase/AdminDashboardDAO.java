package com.example.student_management_system.Controller.DataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDashboardDAO {

    // =========================================================
    // CONNECTION
    // =========================================================

    private Connection getConnection() throws SQLException {
        return DataBase_Connection.getConnection();
    }


    // =========================================================
    // ADMIN NAME
    // =========================================================

    public String getAdminName() {

        String sql =
                "SELECT full_name " +
                        "FROM users " +
                        "WHERE role = 'ADMIN' " +
                        "AND status = 'ACTIVE' " +
                        "LIMIT 1";

        try (
                Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            if (rs.next()) {
                return rs.getString("full_name");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Administrator";
    }


    // =========================================================
    // TOTAL STUDENTS
    // =========================================================

    public int getTotalStudents() {

        return getCount(
                "SELECT COUNT(*) " +
                        "FROM students " +
                        "WHERE status = 'ACTIVE'"
        );
    }


    // =========================================================
    // TOTAL TEACHERS
    // =========================================================

    public int getTotalTeachers() {

        return getCount(
                "SELECT COUNT(*) " +
                        "FROM teachers t " +
                        "INNER JOIN users u " +
                        "ON t.user_id = u.user_id " +
                        "WHERE u.status = 'ACTIVE'"
        );
    }


    // =========================================================
    // TOTAL CLASSES
    // =========================================================

    public int getTotalClasses() {

        return getCount(
                "SELECT COUNT(*) " +
                        "FROM classes " +
                        "WHERE class_status = 'ACTIVE'"
        );
    }


    // =========================================================
    // TOTAL SUBJECTS
    // =========================================================

    public int getTotalSubjects() {

        return getCount(
                "SELECT COUNT(*) FROM subjects"
        );
    }


    // =========================================================
    // ATTENDANCE - ALL BATCHES
    // =========================================================

    public AttendanceData getAttendance(
            String period,
            int classId
    ) {

        AttendanceData data = new AttendanceData();

        String dateCondition;

        switch (period) {

            case "TODAY":

                dateCondition =
                        "DATE(a.attendance_date) = CURDATE()";

                break;

            case "WEEK":

                dateCondition =
                        "YEARWEEK(a.attendance_date, 1) " +
                                "= YEARWEEK(CURDATE(), 1)";

                break;

            case "MONTH":

                dateCondition =
                        "YEAR(a.attendance_date) = YEAR(CURDATE()) " +
                                "AND MONTH(a.attendance_date) = MONTH(CURDATE())";

                break;

            case "YEAR":

                dateCondition =
                        "YEAR(a.attendance_date) = YEAR(CURDATE())";

                break;

            default:

                dateCondition =
                        "DATE(a.attendance_date) = CURDATE()";
        }


        String classCondition = "";

        if (classId > 0) {

            classCondition =
                    " AND s.class_id = ? ";
        }


        String sql =
                "SELECT " +
                        "SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) AS present_count, " +
                        "SUM(CASE WHEN a.status = 'Absent' THEN 1 ELSE 0 END) AS absent_count, " +
                        "SUM(CASE WHEN a.status = 'Late' THEN 1 ELSE 0 END) AS late_count " +

                        "FROM attendance a " +

                        "INNER JOIN students s " +
                        "ON a.student_id = s.student_id " +

                        "WHERE " +
                        dateCondition +
                        classCondition;


        try (
                Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            if (classId > 0) {
                ps.setInt(1, classId);
            }

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    data.present =
                            rs.getInt("present_count");

                    data.absent =
                            rs.getInt("absent_count");

                    data.late =
                            rs.getInt("late_count");
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return data;
    }


    // =========================================================
    // GET CLASSES
    // =========================================================

    public ResultSet getClasses() throws SQLException {

        String sql =
                "SELECT class_id, class_name " +
                        "FROM classes " +
                        "WHERE class_status = 'ACTIVE' " +
                        "ORDER BY class_name";

        Connection conn = getConnection();

        PreparedStatement ps =
                conn.prepareStatement(sql);

        return ps.executeQuery();
    }


    // =========================================================
    // EXAM RESULTS
    // =========================================================

    public ExamResultData getExamResults(
            int examId
    ) {

        ExamResultData data =
                new ExamResultData();

        String sql;

        if (examId == 0) {

            sql =
                    "SELECT " +
                            "SUM(CASE WHEN result = 'PASS' THEN 1 ELSE 0 END) AS pass_count, " +
                            "SUM(CASE WHEN result = 'FAIL' THEN 1 ELSE 0 END) AS fail_count " +
                            "FROM grades";

        } else {

            sql =
                    "SELECT " +
                            "SUM(CASE WHEN result = 'PASS' THEN 1 ELSE 0 END) AS pass_count, " +
                            "SUM(CASE WHEN result = 'FAIL' THEN 1 ELSE 0 END) AS fail_count " +
                            "FROM grades " +
                            "WHERE exam_id = ?";
        }


        try (
                Connection conn = getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            if (examId != 0) {
                ps.setInt(1, examId);
            }

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    data.pass =
                            rs.getInt("pass_count");

                    data.fail =
                            rs.getInt("fail_count");
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return data;
    }


    // =========================================================
    // GET EXAMS
    // =========================================================

    public ResultSet getExams() throws SQLException {

        String sql =
                "SELECT exam_id, exam_name " +
                        "FROM exams " +
                        "ORDER BY exam_date DESC, exam_name";

        Connection conn =
                getConnection();

        PreparedStatement ps =
                conn.prepareStatement(sql);

        return ps.executeQuery();
    }


    // =========================================================
    // GENERIC COUNT
    // =========================================================

    private int getCount(String sql) {

        try (
                Connection conn = getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql);
                ResultSet rs =
                        ps.executeQuery()
        ) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return 0;
    }


    // =========================================================
    // ATTENDANCE DATA
    // =========================================================

    public static class AttendanceData {

        private int present;
        private int absent;
        private int late;

        public int getPresent() {
            return present;
        }

        public int getAbsent() {
            return absent;
        }

        public int getLate() {
            return late;
        }
    }


    // =========================================================
    // EXAM DATA
    // =========================================================

    public static class ExamResultData {

        private int pass;
        private int fail;

        public int getPass() {
            return pass;
        }

        public int getFail() {
            return fail;
        }
    }
}