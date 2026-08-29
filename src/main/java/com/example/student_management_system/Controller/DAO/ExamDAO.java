package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.Exam;
import com.example.student_management_system.Controller.Model.ExamOption;
import com.example.student_management_system.Controller.Model.ExamResultSummary;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Matches: exams(exam_id, exam_name, class_id, exam_date, total_marks)
 *          (class_id added by exam_module_alter.sql)
 *          grades(grade_id, student_id, teacher_id, subject_id, exam_id, ..., result)
 *          classes(class_id, class_name, ...)
 */
public class ExamDAO {

    // ===================================================================
    // DASHBOARD METHODS — unchanged signatures, still used by AdminDashboardController
    // ===================================================================

    public List<ExamOption> getAllExams() {
        List<ExamOption> exams = new ArrayList<>();
        String sql = "SELECT exam_id, exam_name FROM exams ORDER BY exam_date DESC, exam_id DESC";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                exams.add(new ExamOption(rs.getInt("exam_id"), rs.getString("exam_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exams;
    }

    public ExamResultSummary getExamResultSummary(int examId) {
        String sql = "SELECT " +
                "  SUM(CASE WHEN result = 'PASS' THEN 1 ELSE 0 END) AS pass_count, " +
                "  SUM(CASE WHEN result = 'FAIL' THEN 1 ELSE 0 END) AS fail_count " +
                "FROM grades ";
        if (examId != -1) {
            sql += "WHERE exam_id = ? ";
        }
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (examId != -1) {
                ps.setInt(1, examId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ExamResultSummary(rs.getInt("pass_count"), rs.getInt("fail_count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ExamResultSummary(0, 0);
    }

    // ===================================================================
    // ADMIN EXAMS PAGE — batch-scoped CRUD
    // ===================================================================

    /** Every exam across every batch, for when no batch filter is selected. */
    public List<Exam> getAllExamsFull() {
        return getExamsByBatch(-1);
    }

    /** Exams for one batch, or every exam if classId = -1. */
    public List<Exam> getExamsByBatch(int classId) {
        List<Exam> exams = new ArrayList<>();

        String sql =
                "SELECT e.exam_id, e.exam_name, e.class_id, c.class_name, e.exam_date, e.total_marks, " +
                        "       (SELECT COUNT(*) FROM grades g WHERE g.exam_id = e.exam_id) AS grades_recorded " +
                        "FROM exams e " +
                        "LEFT JOIN classes c ON c.class_id = e.class_id ";
        if (classId != -1) {
            sql += "WHERE e.class_id = ? ";
        }
        sql += "ORDER BY e.exam_date DESC, e.exam_id DESC";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (classId != -1) {
                ps.setInt(1, classId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date examDateSql = rs.getDate("exam_date");
                    exams.add(new Exam(
                            rs.getInt("exam_id"),
                            rs.getString("exam_name"),
                            rs.getInt("class_id"),
                            rs.getString("class_name"),
                            examDateSql != null ? examDateSql.toLocalDate() : null,
                            rs.getInt("total_marks"),
                            rs.getInt("grades_recorded")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exams;
    }

    /** Creates a new exam for a batch. Returns the new exam_id, or -1 on failure. */
    public int createExam(String examName, int classId, LocalDate examDate, int totalMarks) {
        String sql = "INSERT INTO exams (exam_name, class_id, exam_date, total_marks) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, examName);
            if (classId > 0) ps.setInt(2, classId); else ps.setNull(2, java.sql.Types.INTEGER);
            ps.setDate(3, examDate != null ? Date.valueOf(examDate) : null);
            ps.setInt(4, totalMarks);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** Updates an existing exam. Returns false on failure. */
    public boolean updateExam(int examId, String examName, int classId, LocalDate examDate, int totalMarks) {
        String sql = "UPDATE exams SET exam_name = ?, class_id = ?, exam_date = ?, total_marks = ? WHERE exam_id = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, examName);
            if (classId > 0) ps.setInt(2, classId); else ps.setNull(2, java.sql.Types.INTEGER);
            ps.setDate(3, examDate != null ? Date.valueOf(examDate) : null);
            ps.setInt(4, totalMarks);
            ps.setInt(5, examId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes an exam. Cascades to remove any grades recorded against it too
     * (grades.exam_id has ON DELETE CASCADE), so this also clears exam results.
     */
    public boolean deleteExam(int examId) {
        String sql = "DELETE FROM exams WHERE exam_id = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, examId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
