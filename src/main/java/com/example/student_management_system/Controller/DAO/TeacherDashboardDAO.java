package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.TeacherInfo;
import com.example.student_management_system.Controller.DAO.DBConnention;

import java.sql.*;
import java.util.*;

public class TeacherDashboardDAO {

    // ============ Teacher lookup by username ============
    public TeacherInfo getTeacherByUsername(String username) {
        String sql =
                "SELECT t.teacher_id, t.user_id, t.teacher_name, t.teacher_code, " +
                        "       t.email, t.phone, t.gender, t.address, " +
                        "       t.photo_path AS t_photo, u.full_name, u.photo_path AS u_photo, u.username " +
                        "FROM teachers t " +
                        "JOIN users u ON u.user_id = t.user_id " +
                        "WHERE u.username = ?";
        try (Connection c = DBConnention.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TeacherInfo ti = new TeacherInfo();
                    ti.setTeacherId(rs.getInt("teacher_id"));
                    ti.setUserId(rs.getInt("user_id"));
                    ti.setTeacherName(rs.getString("teacher_name"));
                    ti.setTeacherCode(rs.getString("teacher_code"));
                    ti.setEmail(rs.getString("email"));
                    ti.setPhone(rs.getString("phone"));
                    ti.setGender(rs.getString("gender"));
                    ti.setAddress(rs.getString("address"));
                    ti.setFullName(rs.getString("full_name"));
                    ti.setUsername(rs.getString("username"));

                    String photo = rs.getString("t_photo");
                    if (photo == null || photo.isBlank()) photo = rs.getString("u_photo");
                    ti.setPhotoPath(photo);

                    return ti;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ============ Teacher's classes ============
    public List<Integer> getTeacherClassIds(int teacherId) {
        List<Integer> ids = new ArrayList<>();
        String sql =
                "SELECT DISTINCT c.class_id FROM classes c " +
                        "LEFT JOIN class_subjects cs ON cs.class_id = c.class_id " +
                        "LEFT JOIN teacher_subjects ts ON ts.subject_id = cs.subject_id " +
                        "WHERE c.class_teacher_id = ? OR ts.teacher_id = ?";
        try (Connection conn = DBConnention.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("class_id"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ids;
    }

    public Map<Integer, String> getTeacherClassMap(int teacherId) {
        Map<Integer, String> map = new LinkedHashMap<>();
        String sql =
                "SELECT DISTINCT c.class_id, c.class_name FROM classes c " +
                        "LEFT JOIN class_subjects cs ON cs.class_id = c.class_id " +
                        "LEFT JOIN teacher_subjects ts ON ts.subject_id = cs.subject_id " +
                        "WHERE c.class_teacher_id = ? OR ts.teacher_id = ? " +
                        "ORDER BY c.class_name";
        try (Connection conn = DBConnention.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getInt("class_id"), rs.getString("class_name"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    // ============ Stat cards ============
    public int getTotalStudents(int teacherId) {
        List<Integer> classIds = getTeacherClassIds(teacherId);
        if (classIds.isEmpty()) return 0;
        String sql = "SELECT COUNT(*) FROM students " +
                "WHERE status='ACTIVE' AND class_id IN (" + placeholders(classIds.size()) + ")";
        try (Connection c = DBConnention.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bindInts(ps, classIds);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getTotalClasses(int teacherId) {
        return getTeacherClassIds(teacherId).size();
    }

    public int getTotalSubjects(int teacherId) {
        String sql = "SELECT COUNT(*) FROM teacher_subjects WHERE teacher_id = ?";
        try (Connection c = DBConnention.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getPendingLeaveCount(int teacherId) {
        List<Integer> classIds = getTeacherClassIds(teacherId);
        if (classIds.isEmpty()) return 0;
        String sql =
                "SELECT COUNT(*) FROM leave_requests lr " +
                        "JOIN students s ON s.student_id = lr.student_id " +
                        "WHERE lr.status = 'Pending' AND s.class_id IN (" + placeholders(classIds.size()) + ")";
        try (Connection c = DBConnention.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bindInts(ps, classIds);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ============ Attendance ============
    public Map<String, Integer> getAttendanceStats(int teacherId, String period, Integer classId) {
        Map<String, Integer> map = new LinkedHashMap<>();

        List<Integer> classIds = new ArrayList<>();
        if (classId != null) classIds.add(classId);
        else                 classIds = getTeacherClassIds(teacherId);
        if (classIds.isEmpty()) return map;

        StringBuilder sql = new StringBuilder(
                "SELECT a.status, COUNT(*) AS cnt " +
                        "FROM attendance a " +
                        "JOIN students s ON s.student_id = a.student_id " +
                        "WHERE s.class_id IN (" + placeholders(classIds.size()) + ") "
        );

        String dateClause = periodToSql(period);
        if (dateClause != null) sql.append(" AND ").append(dateClause);
        sql.append(" GROUP BY a.status");

        try (Connection c = DBConnention.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Integer id : classIds) ps.setInt(idx++, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    private String periodToSql(String period) {
        if (period == null) return null;
        switch (period) {
            case "This Week":  return "a.attendance_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            case "This Month": return "a.attendance_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
            case "This Year":  return "a.attendance_date >= DATE_SUB(CURDATE(), INTERVAL 365 DAY)";
            default:           return null;
        }
    }

    // ============ Exams ============
    /**
     * Returns distinct exam names for the teacher's classes, newest exam first.
     * NOTE: we use GROUP BY instead of DISTINCT because MySQL rejects
     * "SELECT DISTINCT col1 ... ORDER BY col2" when col2 isn't in the SELECT list.
     */
    public List<String> getTeacherExamNames(int teacherId) {
        List<String> list = new ArrayList<>();
        List<Integer> classIds = getTeacherClassIds(teacherId);
        if (classIds.isEmpty()) return list;

        String sql =
                "SELECT e.exam_name " +
                        "FROM exams e " +
                        "WHERE e.class_id IN (" + placeholders(classIds.size()) + ") " +
                        "GROUP BY e.exam_name " +
                        "ORDER BY MAX(e.exam_id) DESC";

        try (Connection c = DBConnention.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bindInts(ps, classIds);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString("exam_name"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Map<String, Integer> getExamStats(int teacherId, String examName) {
        Map<String, Integer> map = new LinkedHashMap<>();
        List<Integer> classIds = getTeacherClassIds(teacherId);
        if (classIds.isEmpty()) return map;

        StringBuilder sql = new StringBuilder(
                "SELECT g.result, COUNT(*) AS cnt FROM grades g " +
                        "JOIN students s ON s.student_id = g.student_id " +
                        "JOIN exams e ON e.exam_id = g.exam_id " +
                        "WHERE s.class_id IN (" + placeholders(classIds.size()) + ")"
        );
        boolean filterByExam = examName != null && !examName.equals("All Exams");
        if (filterByExam) sql.append(" AND e.exam_name = ?");
        sql.append(" GROUP BY g.result");

        try (Connection c = DBConnention.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Integer id : classIds) ps.setInt(idx++, id);
            if (filterByExam) ps.setString(idx++, examName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getString("result"), rs.getInt("cnt"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    // ============ Performance ============
    public Map<String, Map<String, Double>> getPerformanceData(int teacherId, String examName) {
        Map<String, Map<String, Double>> result = new LinkedHashMap<>();
        List<Integer> classIds = getTeacherClassIds(teacherId);
        if (classIds.isEmpty()) return result;

        StringBuilder sql = new StringBuilder(
                "SELECT c.class_name, sub.subject_name, AVG(g.percentage) AS avg_pct " +
                        "FROM grades g " +
                        "JOIN students s ON s.student_id = g.student_id " +
                        "JOIN classes c ON c.class_id = s.class_id " +
                        "JOIN subjects sub ON sub.subject_id = g.subject_id " +
                        "JOIN exams e ON e.exam_id = g.exam_id " +
                        "WHERE s.class_id IN (" + placeholders(classIds.size()) + ")"
        );
        boolean filterByExam = examName != null && !examName.equals("All Exams");
        if (filterByExam) sql.append(" AND e.exam_name = ?");
        sql.append(" GROUP BY c.class_name, sub.subject_name ORDER BY c.class_name, sub.subject_name");

        try (Connection c = DBConnention.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Integer id : classIds) ps.setInt(idx++, id);
            if (filterByExam) ps.setString(idx++, examName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String cls = rs.getString("class_name");
                    String sub = rs.getString("subject_name");
                    double avg = rs.getDouble("avg_pct");
                    result.computeIfAbsent(cls, k -> new LinkedHashMap<>()).put(sub, avg);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // ============ Helpers ============
    private String placeholders(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(",");
            sb.append("?");
        }
        return sb.toString();
    }

    private void bindInts(PreparedStatement ps, List<Integer> vals) throws SQLException {
        int i = 1;
        for (Integer v : vals) ps.setInt(i++, v);
    }
}