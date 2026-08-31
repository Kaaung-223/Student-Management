package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.BatchExamRate;
import com.example.student_management_system.Controller.Model.GradeSummary;
import com.example.student_management_system.Controller.Model.StudentGradeRow;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only grade analytics for the admin Grades / Results page.
 * GPA scale matches StudentProfileDAO:
 * 90+=4.0, 80+=3.5, 70+=3.0, 60+=2.5, 50+=2.0, below 50=0.0
 */
public class GradeDAO {

    private static final String GPA_CASE =
            "CASE " +
                    "WHEN g.percentage >= 90 THEN 4.0 " +
                    "WHEN g.percentage >= 80 THEN 3.5 " +
                    "WHEN g.percentage >= 70 THEN 3.0 " +
                    "WHEN g.percentage >= 60 THEN 2.5 " +
                    "WHEN g.percentage >= 50 THEN 2.0 " +
                    "WHEN g.percentage IS NULL THEN NULL " +
                    "ELSE 0.0 END";

    public GradeSummary getGradeSummary(int classId, int examId) {
        String sql =
                "SELECT " +
                        "COALESCE(SUM(CASE WHEN g.result = 'PASS' THEN 1 ELSE 0 END), 0) AS pass_count, " +
                        "COALESCE(SUM(CASE WHEN g.result = 'FAIL' THEN 1 ELSE 0 END), 0) AS fail_count, " +
                        "COALESCE(AVG(" + GPA_CASE + "), 0) AS avg_gpa " +
                        "FROM grades g " +
                        "JOIN exams e ON e.exam_id = g.exam_id " +
                        "WHERE (? = -1 OR e.class_id = ?) " +
                        "AND (? = -1 OR g.exam_id = ?)";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, classId);
            ps.setInt(2, classId);
            ps.setInt(3, examId);
            ps.setInt(4, examId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new GradeSummary(
                            rs.getInt("pass_count"),
                            rs.getInt("fail_count"),
                            rs.getDouble("avg_gpa")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new GradeSummary(0, 0, 0.0);
    }

    public List<BatchExamRate> getBatchExamRates(int classId) {
        List<BatchExamRate> rows = new ArrayList<>();

        String sql =
                "SELECT e.exam_id, e.exam_name, e.exam_date, " +
                        "COALESCE(c.class_name, 'Not assigned') AS class_name, " +
                        "COUNT(g.grade_id) AS graded_count, " +
                        "COALESCE(SUM(CASE WHEN g.result = 'PASS' THEN 1 ELSE 0 END), 0) AS pass_count, " +
                        "COALESCE(SUM(CASE WHEN g.result = 'FAIL' THEN 1 ELSE 0 END), 0) AS fail_count " +
                        "FROM exams e " +
                        "LEFT JOIN classes c ON c.class_id = e.class_id " +
                        "LEFT JOIN grades g ON g.exam_id = e.exam_id " +
                        "WHERE (? = -1 OR e.class_id = ?) " +
                        "GROUP BY e.exam_id, e.exam_name, e.exam_date, c.class_name " +
                        "ORDER BY e.exam_date DESC, e.exam_id DESC";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, classId);
            ps.setInt(2, classId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date examDate = rs.getDate("exam_date");
                    rows.add(new BatchExamRate(
                            rs.getInt("exam_id"),
                            rs.getString("exam_name"),
                            rs.getString("class_name"),
                            examDate != null ? examDate.toLocalDate() : null,
                            rs.getInt("graded_count"),
                            rs.getInt("pass_count"),
                            rs.getInt("fail_count")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    public List<StudentGradeRow> getStudentGradeRows(int classId, int examId, String search) {
        if (examId != -1) {
            return getStudentRowsForExam(classId, examId, search);
        }
        return getStudentRowsAggregated(classId, search);
    }

    private List<StudentGradeRow> getStudentRowsAggregated(int classId, String search) {
        List<StudentGradeRow> rows = new ArrayList<>();
        String term = likeTerm(search);

        String sql =
                "SELECT s.student_id, s.student_code, s.student_name, " +
                        "COALESCE(c.class_name, 'Not assigned') AS class_name, " +
                        "COUNT(g.grade_id) AS exams_taken, " +
                        "COALESCE(SUM(CASE WHEN g.result = 'PASS' THEN 1 ELSE 0 END), 0) AS pass_count, " +
                        "COALESCE(SUM(CASE WHEN g.result = 'FAIL' THEN 1 ELSE 0 END), 0) AS fail_count, " +
                        "COALESCE(AVG(" + GPA_CASE + "), 0) AS gpa " +
                        "FROM students s " +
                        "LEFT JOIN classes c ON c.class_id = s.class_id " +
                        "LEFT JOIN grades g ON g.student_id = s.student_id " +
                        "WHERE (? = -1 OR s.class_id = ?) " +
                        "AND (s.student_name LIKE ? OR s.student_code LIKE ? OR COALESCE(c.class_name, '') LIKE ?) " +
                        "GROUP BY s.student_id, s.student_code, s.student_name, c.class_name " +
                        "ORDER BY s.student_name";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, classId);
            ps.setInt(2, classId);
            ps.setString(3, term);
            ps.setString(4, term);
            ps.setString(5, term);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int passCount = rs.getInt("pass_count");
                    int failCount = rs.getInt("fail_count");
                    int taken = rs.getInt("exams_taken");

                    rows.add(new StudentGradeRow(
                            rs.getInt("student_id"),
                            rs.getString("student_code"),
                            rs.getString("student_name"),
                            rs.getString("class_name"),
                            "All exams",
                            taken,
                            passCount,
                            failCount,
                            null,
                            null,
                            null,
                            overallResult(taken, failCount),
                            rs.getDouble("gpa")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    private List<StudentGradeRow> getStudentRowsForExam(int classId, int examId, String search) {
        List<StudentGradeRow> rows = new ArrayList<>();
        String term = likeTerm(search);

        String sql =
                "SELECT s.student_id, s.student_code, s.student_name, " +
                        "COALESCE(c.class_name, 'Not assigned') AS class_name, " +
                        "COALESCE(e.exam_name, '-') AS exam_name, " +
                        "g.obtained_marks, g.percentage, g.letter_grade, g.result, " +
                        GPA_CASE + " AS gpa " +
                        "FROM students s " +
                        "LEFT JOIN classes c ON c.class_id = s.class_id " +
                        "LEFT JOIN grades g ON g.student_id = s.student_id AND g.exam_id = ? " +
                        "LEFT JOIN exams e ON e.exam_id = g.exam_id " +
                        "WHERE (? = -1 OR s.class_id = ?) " +
                        "AND (s.student_name LIKE ? OR s.student_code LIKE ? OR COALESCE(c.class_name, '') LIKE ?) " +
                        "ORDER BY s.student_name";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, examId);
            ps.setInt(2, classId);
            ps.setInt(3, classId);
            ps.setString(4, term);
            ps.setString(5, term);
            ps.setString(6, term);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer marks = getNullableInt(rs, "obtained_marks");
                    Double percentage = getNullableDouble(rs, "percentage");
                    String result = rs.getString("result");
                    boolean graded = result != null && !result.isBlank();
                    double gpa = rs.getDouble("gpa");
                    if (rs.wasNull()) {
                        gpa = 0.0;
                    }

                    rows.add(new StudentGradeRow(
                            rs.getInt("student_id"),
                            rs.getString("student_code"),
                            rs.getString("student_name"),
                            rs.getString("class_name"),
                            rs.getString("exam_name"),
                            graded ? 1 : 0,
                            "PASS".equalsIgnoreCase(result) ? 1 : 0,
                            "FAIL".equalsIgnoreCase(result) ? 1 : 0,
                            marks,
                            percentage,
                            rs.getString("letter_grade"),
                            graded ? result : "NOT GRADED",
                            gpa
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    private static String overallResult(int taken, int failCount) {
        if (taken == 0) {
            return "NOT GRADED";
        }
        return failCount > 0 ? "FAIL" : "PASS";
    }

    private static String likeTerm(String search) {
        return "%" + (search == null ? "" : search.trim()) + "%";
    }

    private static Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static Double getNullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }
}
