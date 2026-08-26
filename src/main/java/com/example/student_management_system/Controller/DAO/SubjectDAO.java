package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.Subject;
import com.example.student_management_system.Controller.Model.SubjectOption;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Matches:
 *   subjects(subject_id, subject_name, subject_code)
 *   class_subjects(class_id, subject_id)   -- added by class_subject_module_alter.sql
 *   classes(class_id, class_name, ...)
 */
public class SubjectDAO {

    public int getTotalSubjects() {
        String sql = "SELECT COUNT(*) FROM subjects";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** All subjects, used for the subject-picker checklist (teacher assignment, class curriculum, etc). */
    public List<SubjectOption> getAllSubjects() {
        List<SubjectOption> subjects = new ArrayList<>();
        String sql = "SELECT subject_id, subject_name FROM subjects ORDER BY subject_name";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                subjects.add(new SubjectOption(rs.getInt("subject_id"), rs.getString("subject_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subjects;
    }

    /** Every subject, with the classes that include it in their curriculum (for the "All Subjects" list). */
    public List<Subject> getAllSubjectsWithClasses() {
        List<Subject> subjects = new ArrayList<>();
        String sql =
                "SELECT s.subject_id, s.subject_name, s.subject_code, " +
                        "       GROUP_CONCAT(DISTINCT c.class_name ORDER BY c.class_name SEPARATOR ', ') AS classes_using_it " +
                        "FROM subjects s " +
                        "LEFT JOIN class_subjects cs ON cs.subject_id = s.subject_id " +
                        "LEFT JOIN classes c ON c.class_id = cs.class_id " +
                        "GROUP BY s.subject_id, s.subject_name, s.subject_code " +
                        "ORDER BY s.subject_name";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                subjects.add(new Subject(
                        rs.getInt("subject_id"),
                        rs.getString("subject_name"),
                        rs.getString("subject_code"),
                        rs.getString("classes_using_it")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subjects;
    }

    /** subject_ids currently in a class's curriculum — used to pre-check the checklist for that class. */
    public List<Integer> getSubjectIdsForClass(int classId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT subject_id FROM class_subjects WHERE class_id = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("subject_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    /**
     * Replaces a class's entire curriculum with the given subject_ids (removes any not in the
     * list, adds any new ones). Runs as a single transaction.
     */
    public boolean saveClassSubjects(int classId, List<Integer> subjectIds) {
        String deleteSql = "DELETE FROM class_subjects WHERE class_id = ?";
        String insertSql = "INSERT INTO class_subjects (class_id, subject_id) VALUES (?, ?)";

        Connection con = null;
        try {
            con = DBConnention.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(deleteSql)) {
                ps.setInt(1, classId);
                ps.executeUpdate();
            }

            if (subjectIds != null && !subjectIds.isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    for (int subjectId : subjectIds) {
                        ps.setInt(1, classId);
                        ps.setInt(2, subjectId);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** Creates a new subject. Returns the new subject_id, or -1 on failure (e.g. duplicate name/code). */
    public int createSubject(String subjectName, String subjectCode) {
        String sql = "INSERT INTO subjects (subject_name, subject_code) VALUES (?, ?)";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, subjectName);
            ps.setString(2, subjectCode);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** Updates an existing subject's name/code. Returns false on failure (e.g. duplicate name/code). */
    public boolean updateSubject(int subjectId, String subjectName, String subjectCode) {
        String sql = "UPDATE subjects SET subject_name = ?, subject_code = ? WHERE subject_id = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, subjectName);
            ps.setString(2, subjectCode);
            ps.setInt(3, subjectId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a subject entirely. Cascades to remove it from every class's curriculum
     * (class_subjects) and every teacher's subject list (teacher_subjects) automatically,
     * since both tables have ON DELETE CASCADE on subject_id.
     */
    public boolean deleteSubject(int subjectId) {
        String sql = "DELETE FROM subjects WHERE subject_id = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
