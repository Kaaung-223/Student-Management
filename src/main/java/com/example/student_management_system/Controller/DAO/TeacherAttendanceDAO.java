package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.AttendanceRow;
import com.example.student_management_system.Controller.Model.BatchFilter;
import com.example.student_management_system.Controller.DAO.DBConnention;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TeacherAttendanceDAO {

    // ---- Batches taught by this teacher ----
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

    // ---- Students + existing attendance for the given date ----
    public List<AttendanceRow> findStudentsForDate(int teacherId, int batchId, LocalDate date) {
        List<AttendanceRow> list = new ArrayList<>();

        String sql =
                "SELECT s.student_id, s.student_code, s.student_name, " +
                        "       a.status, a.remarks " +
                        "FROM students s " +
                        "LEFT JOIN attendance a " +
                        "       ON a.student_id = s.student_id AND a.attendance_date = ? " +
                        "WHERE s.status = 'ACTIVE' " +
                        "AND s.class_id IN ( " +
                        "    SELECT DISTINCT c2.class_id FROM classes c2 " +
                        "    LEFT JOIN class_subjects cs ON cs.class_id = c2.class_id " +
                        "    LEFT JOIN teacher_subjects ts ON ts.subject_id = cs.subject_id " +
                        "    WHERE c2.class_teacher_id = ? OR ts.teacher_id = ? " +
                        ") " +
                        "AND (? = -1 OR s.class_id = ?) " +
                        "ORDER BY s.student_name";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));
            ps.setInt(2, teacherId);
            ps.setInt(3, teacherId);
            ps.setInt(4, batchId);
            ps.setInt(5, batchId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AttendanceRow(
                            rs.getInt("student_id"),
                            rs.getString("student_code"),
                            rs.getString("student_name"),
                            rs.getString("status"),          // may be null
                            rs.getString("remarks")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ---- Save (insert or update) all marked rows ----
    // Relies on: UNIQUE(student_id, attendance_date)
    public int saveAll(int teacherId, LocalDate date, List<AttendanceRow> rows) {
        int saved = 0;

        String sql =
                "INSERT INTO attendance (student_id, teacher_id, attendance_date, status, remarks) " +
                        "VALUES (?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "    status = VALUES(status), " +
                        "    remarks = VALUES(remarks), " +
                        "    teacher_id = VALUES(teacher_id)";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            con.setAutoCommit(false);

            for (AttendanceRow r : rows) {
                if (r.getStatus() == null || r.getStatus().isBlank()) continue;   // skip unmarked

                ps.setInt(1, r.getStudentId());
                ps.setInt(2, teacherId);
                ps.setDate(3, Date.valueOf(date));
                ps.setString(4, r.getStatus());
                ps.setString(5, r.getRemarks());

                ps.addBatch();
                saved++;
            }

            ps.executeBatch();
            con.commit();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return saved;
    }
}