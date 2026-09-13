package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherSubjectsDAO {

    /**
     * Returns all subjects taught by the given teacher, plus a comma-joined
     * list of class names that use each subject.
     */
    public List<Subject> findTeacherSubjects(int teacherId, String search) {
        List<Subject> list = new ArrayList<>();

        String sql =
                "SELECT sub.subject_id, sub.subject_name, sub.subject_code, " +
                        "       GROUP_CONCAT(DISTINCT c.class_name " +
                        "                    ORDER BY c.class_name SEPARATOR ', ') AS classes_using_it " +
                        "FROM subjects sub " +
                        "JOIN teacher_subjects ts ON ts.subject_id = sub.subject_id " +
                        "LEFT JOIN class_subjects cs ON cs.subject_id = sub.subject_id " +
                        "LEFT JOIN classes c ON c.class_id = cs.class_id " +
                        "WHERE ts.teacher_id = ? " +
                        "AND (sub.subject_name LIKE ? OR sub.subject_code LIKE ?) " +
                        "GROUP BY sub.subject_id, sub.subject_name, sub.subject_code " +
                        "ORDER BY sub.subject_name";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String like = "%" + (search == null ? "" : search.trim()) + "%";
            ps.setInt(1, teacherId);
            ps.setString(2, like);
            ps.setString(3, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Subject s = new Subject(
                            rs.getInt("subject_id"),
                            rs.getString("subject_name"),
                            rs.getString("subject_code"),
                            rs.getString("classes_using_it"));
                    list.add(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /** How many distinct students have grades in this subject. */
    public int getStudentCountForSubject(int subjectId) {
        String sql = "SELECT COUNT(DISTINCT student_id) FROM grades WHERE subject_id = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Average percentage for a subject (all exams combined). */
    public double getAverageForSubject(int subjectId) {
        String sql = "SELECT AVG(percentage) FROM grades WHERE subject_id = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}