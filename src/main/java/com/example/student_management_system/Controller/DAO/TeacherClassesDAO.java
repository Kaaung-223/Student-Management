package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.TeacherClass;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherClassesDAO {

    /**
     * Returns all classes where this teacher is:
     *   1. the class leader (classes.class_teacher_id = teacherId)  OR
     *   2. a subject teacher (teacher_subjects -> class_subjects)
     *
     * For each class we also join-concat the subjects this teacher teaches there.
     */
    public List<TeacherClass> findTeacherClasses(int teacherId, String search) {
        List<TeacherClass> list = new ArrayList<>();

        String sql =
                "SELECT c.class_id, c.class_name, c.academic_year, c.room_no, " +
                        "       CASE WHEN c.class_teacher_id = ? THEN 1 ELSE 0 END AS is_leader, " +
                        "       (SELECT COUNT(*) FROM students s " +
                        "         WHERE s.class_id = c.class_id AND s.status='ACTIVE') AS student_count, " +
                        "       (SELECT GROUP_CONCAT(DISTINCT sub.subject_name " +
                        "                             ORDER BY sub.subject_name SEPARATOR ', ') " +
                        "        FROM subjects sub " +
                        "        JOIN teacher_subjects ts ON ts.subject_id = sub.subject_id " +
                        "        JOIN class_subjects cs   ON cs.subject_id = sub.subject_id " +
                        "        WHERE ts.teacher_id = ? AND cs.class_id = c.class_id) AS subjects_taught " +
                        "FROM classes c " +
                        "WHERE c.class_id IN ( " +
                        "    SELECT class_id FROM classes WHERE class_teacher_id = ? " +
                        "    UNION " +
                        "    SELECT cs.class_id FROM class_subjects cs " +
                        "    JOIN teacher_subjects ts ON ts.subject_id = cs.subject_id " +
                        "    WHERE ts.teacher_id = ? " +
                        ") " +
                        "AND (c.class_name LIKE ? OR c.academic_year LIKE ? OR c.room_no LIKE ?) " +
                        "ORDER BY is_leader DESC, c.class_name";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String like = "%" + (search == null ? "" : search.trim()) + "%";

            ps.setInt(1, teacherId);
            ps.setInt(2, teacherId);
            ps.setInt(3, teacherId);
            ps.setInt(4, teacherId);
            ps.setString(5, like);
            ps.setString(6, like);
            ps.setString(7, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TeacherClass tc = new TeacherClass();
                    tc.setClassId(rs.getInt("class_id"));
                    tc.setClassName(rs.getString("class_name"));
                    tc.setAcademicYear(rs.getString("academic_year"));
                    tc.setRoomNo(rs.getString("room_no"));
                    tc.setStudentCount(rs.getInt("student_count"));
                    tc.setClassLeader(rs.getInt("is_leader") == 1);
                    tc.setSubjectsTaught(rs.getString("subjects_taught"));
                    list.add(tc);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Count of active students in a class. */
    public int getStudentCount(int classId) {
        String sql = "SELECT COUNT(*) FROM students WHERE class_id = ? AND status='ACTIVE'";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    /** Count of distinct subjects taught by the teacher in the given class. */
    public int getSubjectCount(int teacherId, int classId) {
        String sql =
                "SELECT COUNT(DISTINCT ts.subject_id) FROM teacher_subjects ts " +
                        "JOIN class_subjects cs ON cs.subject_id = ts.subject_id " +
                        "WHERE ts.teacher_id = ? AND cs.class_id = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, classId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }
}