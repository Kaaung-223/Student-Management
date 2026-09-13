package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.BatchFilter;
import com.example.student_management_system.Controller.Model.ExamResultRow;
import com.example.student_management_system.Controller.Model.SubjectResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherResultsDAO {

    // ==================================================
    // Exams taught by this teacher
    // ==================================================
    public List<BatchFilter> findTeacherExams(int teacherId) {
        List<BatchFilter> list = new ArrayList<>();

        String sql =
                "SELECT e.exam_id, e.exam_name FROM exams e " +
                        "WHERE e.class_id IN ( " +
                        "    SELECT DISTINCT c.class_id FROM classes c " +
                        "    LEFT JOIN class_subjects cs ON cs.class_id = c.class_id " +
                        "    LEFT JOIN teacher_subjects ts ON ts.subject_id = cs.subject_id " +
                        "    WHERE c.class_teacher_id = ? OR ts.teacher_id = ? " +
                        ") " +
                        "GROUP BY e.exam_id, e.exam_name " +
                        "ORDER BY MAX(e.exam_id) DESC";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new BatchFilter(rs.getInt(1), rs.getString(2)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ==================================================
    // Batches for the teacher
    // ==================================================
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
                    list.add(new BatchFilter(rs.getInt(1), rs.getString(2)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ==================================================
    // Per-student result summary
    // ==================================================
    public List<ExamResultRow> findStudentResults(int teacherId, int examId,
                                                  int batchId, String search) {
        List<ExamResultRow> list = new ArrayList<>();

        String sql =
                "SELECT s.student_id, s.student_code, s.student_name, c.class_name, " +
                        "       COUNT(*)                  AS subject_count, " +
                        "       AVG(g.percentage)         AS avg_pct, " +
                        "       SUM(g.result = 'PASS')    AS pass_count, " +
                        "       SUM(g.result = 'FAIL')    AS fail_count " +
                        "FROM grades g " +
                        "JOIN students s ON s.student_id = g.student_id " +
                        "JOIN classes  c ON c.class_id   = s.class_id " +
                        "WHERE s.class_id IN ( " +
                        "    SELECT DISTINCT c2.class_id FROM classes c2 " +
                        "    LEFT JOIN class_subjects cs ON cs.class_id = c2.class_id " +
                        "    LEFT JOIN teacher_subjects ts ON ts.subject_id = cs.subject_id " +
                        "    WHERE c2.class_teacher_id = ? OR ts.teacher_id = ? " +
                        ") " +
                        "AND (? = -1 OR g.exam_id = ?) " +
                        "AND (? = -1 OR s.class_id = ?) " +
                        "AND (s.student_name LIKE ? OR s.student_code LIKE ?) " +
                        "GROUP BY s.student_id, s.student_code, s.student_name, c.class_name " +
                        "ORDER BY s.student_name";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String like = "%" + (search == null ? "" : search.trim()) + "%";

            ps.setInt(1, teacherId);
            ps.setInt(2, teacherId);
            ps.setInt(3, examId);
            ps.setInt(4, examId);
            ps.setInt(5, batchId);
            ps.setInt(6, batchId);
            ps.setString(7, like);
            ps.setString(8, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ExamResultRow r = new ExamResultRow();
                    r.setStudentId(rs.getInt("student_id"));
                    r.setStudentCode(rs.getString("student_code"));
                    r.setStudentName(rs.getString("student_name"));
                    r.setBatchName(rs.getString("class_name"));
                    r.setSubjectCount(rs.getInt("subject_count"));

                    double avg = rs.getDouble("avg_pct");
                    r.setAvgPercentage(avg);
                    r.setGpa(toGpa(avg));

                    r.setPassCount(rs.getInt("pass_count"));
                    r.setFailCount(rs.getInt("fail_count"));
                    r.setOverallResult(r.getFailCount() == 0 ? "PASS" : "FAIL");

                    list.add(r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ==================================================
    // Subject breakdown for one student
    // ==================================================
    public List<SubjectResult> findSubjectResults(int studentId, int examId) {
        List<SubjectResult> list = new ArrayList<>();

        String sql =
                "SELECT sub.subject_name, g.obtained_marks, e.total_marks, " +
                        "       g.percentage, g.letter_grade, g.result " +
                        "FROM grades g " +
                        "JOIN subjects sub ON sub.subject_id = g.subject_id " +
                        "JOIN exams    e   ON e.exam_id      = g.exam_id " +
                        "WHERE g.student_id = ? AND (? = -1 OR g.exam_id = ?) " +
                        "ORDER BY sub.subject_name";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, examId);
            ps.setInt(3, examId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new SubjectResult(
                            rs.getString("subject_name"),
                            rs.getInt("obtained_marks"),
                            rs.getInt("total_marks"),
                            rs.getDouble("percentage"),
                            rs.getString("letter_grade"),
                            rs.getString("result")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ==================================================
    // GPA mapping
    // ==================================================
    private double toGpa(double pct) {
        if (pct >= 90) return 4.0;
        if (pct >= 80) return 3.0;
        if (pct >= 70) return 2.0;
        if (pct >= 50) return 1.0;
        return 0.0;
    }
}