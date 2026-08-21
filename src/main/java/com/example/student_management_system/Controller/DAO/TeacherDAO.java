package com.example.student_management_system.Controller.DAO;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Matches: teachers(teacher_id, user_id, teacher_code, teacher_name, email, phone,
 *                    gender, address, hire_date, created_at)
 *          users(user_id, full_name, username, password, role, status, created_at)
 *
 * teachers has no status column of its own — "active" comes from the linked users row.
 */
public class TeacherDAO {

    public int getTotalTeachers() {
        String sql = "SELECT COUNT(*) " +
                "FROM teachers t " +
                "JOIN users u ON t.user_id = u.user_id " +
                "WHERE u.status = 'ACTIVE'";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
