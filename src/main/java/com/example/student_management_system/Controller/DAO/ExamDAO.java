package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.ExamOption;
import com.example.student_management_system.Controller.Model.ExamResultSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Matches: exams(exam_id, exam_name, exam_date, total_marks)
 *          grades(grade_id, student_id, teacher_id, subject_id, exam_id,
 *                 obtained_marks, percentage, letter_grade, result, remarks, created_at)
 *          result ENUM('PASS','FAIL')
 *
 * Note: grades is per (student, subject, exam) — a student with 4 subjects in one exam
 * produces 4 grade rows. This counts PASS/FAIL rows, i.e. subject-results, not students.
 * If you want student-level pass/fail instead (e.g. "failed if any subject failed"),
 * that needs a different aggregation — ask if you'd like that version instead.
 */
public class ExamDAO {

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
}
