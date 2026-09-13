package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.BatchFilter;
import com.example.student_management_system.Controller.Model.ExamItem;
import com.example.student_management_system.Controller.Model.StudentMarkRow;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TeacherExamDAO {

    // ---------- Batches for this teacher ----------
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
                while (rs.next())
                    list.add(new BatchFilter(rs.getInt(1), rs.getString(2)));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ---------- Subjects for this teacher ----------
    public List<BatchFilter> findTeacherSubjects(int teacherId) {
        List<BatchFilter> list = new ArrayList<>();
        String sql =
                "SELECT DISTINCT sub.subject_id, sub.subject_name FROM subjects sub " +
                        "JOIN teacher_subjects ts ON ts.subject_id = sub.subject_id " +
                        "WHERE ts.teacher_id = ? " +
                        "ORDER BY sub.subject_name";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new BatchFilter(rs.getInt(1), rs.getString(2)));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ---------- Existing exams ----------
    public List<ExamItem> findExams(int teacherId, int batchId) {
        List<ExamItem> list = new ArrayList<>();
        String sql =
                "SELECT e.exam_id, e.exam_name, e.exam_date, e.total_marks, " +
                        "       e.class_id, c.class_name " +
                        "FROM exams e " +
                        "LEFT JOIN classes c ON c.class_id = e.class_id " +
                        "WHERE e.class_id IN ( " +
                        "    SELECT DISTINCT c2.class_id FROM classes c2 " +
                        "    LEFT JOIN class_subjects cs ON cs.class_id = c2.class_id " +
                        "    LEFT JOIN teacher_subjects ts ON ts.subject_id = cs.subject_id " +
                        "    WHERE c2.class_teacher_id = ? OR ts.teacher_id = ? " +
                        ") " +
                        "AND (? = -1 OR e.class_id = ?) " +
                        "ORDER BY e.exam_date DESC, e.exam_id DESC";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, teacherId);
            ps.setInt(3, batchId);
            ps.setInt(4, batchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ExamItem e = new ExamItem();
                    e.setExamId(rs.getInt("exam_id"));
                    e.setExamName(rs.getString("exam_name"));
                    Date d = rs.getDate("exam_date");
                    if (d != null) e.setExamDate(d.toLocalDate());
                    e.setTotalMarks(rs.getInt("total_marks"));
                    e.setClassId(rs.getInt("class_id"));
                    e.setClassName(rs.getString("class_name"));
                    list.add(e);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ---------- Create exam ----------
    public boolean createExam(String name, LocalDate date, int totalMarks, int classId) {
        String sql = "INSERT INTO exams (exam_name, class_id, exam_date, total_marks) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, classId);
            ps.setDate(3, Date.valueOf(date));
            ps.setInt(4, totalMarks);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------- Students + existing marks ----------
    public List<StudentMarkRow> findStudentsWithMarks(int batchId, int examId, int subjectId) {
        List<StudentMarkRow> list = new ArrayList<>();
        String sql =
                "SELECT s.student_id, s.student_code, s.student_name, " +
                        "       g.obtained_marks, g.remarks " +
                        "FROM students s " +
                        "LEFT JOIN grades g " +
                        "       ON g.student_id = s.student_id " +
                        "      AND g.exam_id    = ? " +
                        "      AND g.subject_id = ? " +
                        "WHERE s.class_id = ? AND s.status = 'ACTIVE' " +
                        "ORDER BY s.student_name";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setInt(2, subjectId);
            ps.setInt(3, batchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StudentMarkRow r = new StudentMarkRow();
                    r.setStudentId(rs.getInt("student_id"));
                    r.setStudentCode(rs.getString("student_code"));
                    r.setStudentName(rs.getString("student_name"));
                    int m = rs.getInt("obtained_marks");
                    r.setMarks(rs.wasNull() ? null : m);
                    r.setRemarks(rs.getString("remarks"));
                    list.add(r);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ---------- Save marks ----------
    // NOTE: grades.result is NOT NULL in the DB so we silently fill it
    //       from the percentage. It is NOT shown anywhere in the UI.
    public int saveMarks(int teacherId, int examId, int subjectId,
                         int totalMarks, List<StudentMarkRow> rows) {

        int saved = 0;

        try (Connection con = DBConnention.getConnection()) {
            con.setAutoCommit(false);

            for (StudentMarkRow r : rows) {
                if (r.getMarks() == null) continue;    // skip unmarked

                double pct = totalMarks > 0 ? (r.getMarks() * 100.0 / totalMarks) : 0;
                String silentResult = pct >= 50 ? "PASS" : "FAIL";   // DB requirement

                int gradeId = findExistingGradeId(con, r.getStudentId(), examId, subjectId);

                if (gradeId > 0) {
                    String upd =
                            "UPDATE grades SET obtained_marks=?, percentage=?, result=?, " +
                                    "remarks=?, teacher_id=? WHERE grade_id=?";
                    try (PreparedStatement ps = con.prepareStatement(upd)) {
                        ps.setInt(1, r.getMarks());
                        ps.setDouble(2, pct);
                        ps.setString(3, silentResult);
                        ps.setString(4, r.getRemarks());
                        ps.setInt(5, teacherId);
                        ps.setInt(6, gradeId);
                        ps.executeUpdate();
                    }
                } else {
                    String ins =
                            "INSERT INTO grades (student_id, teacher_id, subject_id, exam_id, " +
                                    "obtained_marks, percentage, result, remarks) " +
                                    "VALUES (?,?,?,?,?,?,?,?)";
                    try (PreparedStatement ps = con.prepareStatement(ins)) {
                        ps.setInt(1, r.getStudentId());
                        ps.setInt(2, teacherId);
                        ps.setInt(3, subjectId);
                        ps.setInt(4, examId);
                        ps.setInt(5, r.getMarks());
                        ps.setDouble(6, pct);
                        ps.setString(7, silentResult);
                        ps.setString(8, r.getRemarks());
                        ps.executeUpdate();
                    }
                }
                saved++;
            }

            con.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return saved;
    }

    private int findExistingGradeId(Connection con, int studentId,
                                    int examId, int subjectId) throws SQLException {
        String sql =
                "SELECT grade_id FROM grades " +
                        "WHERE student_id=? AND exam_id=? AND subject_id=? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, examId);
            ps.setInt(3, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }
}