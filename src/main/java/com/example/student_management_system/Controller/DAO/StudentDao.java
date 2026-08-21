package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Matches:
 *   students(student_id, student_code, student_name, email, phone, age, gender,
 *            address, class_id, admission_date, status, created_at)
 *   classes(class_id, class_name, ...)
 */
public class StudentDao {

    /** Count of students with status = 'ACTIVE' (for the "Total Students" stat card). */
    public int getTotalStudents() {
        String sql = "SELECT COUNT(*) FROM students WHERE status = 'ACTIVE'";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Students for a given batch (classes.class_id). Pass batchId = -1 to get every
     * student across all batches ("All Batches" combo option).
     */
    public List<Student> getStudentsByBatch(int batchId) {
        List<Student> students = new ArrayList<>();

        String sql = "SELECT s.student_id, s.student_code, s.student_name, s.email, s.status, " +
                "       c.class_id, c.class_name " +
                "FROM students s " +
                "LEFT JOIN classes c ON s.class_id = c.class_id ";
        if (batchId != -1) {
            sql += "WHERE c.class_id = ? ";
        }
        sql += "ORDER BY c.class_name, s.student_name";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (batchId != -1) {
                ps.setInt(1, batchId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(new Student(
                            rs.getInt("student_id"),
                            rs.getString("student_code"),
                            rs.getString("student_name"),
                            rs.getString("email"),
                            rs.getString("status"),
                            rs.getInt("class_id"),
                            rs.getString("class_name")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }
}
