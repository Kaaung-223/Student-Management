package com.example.student_management_system.Controller.DAO;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Assumed schema: subjects(subject_id INT PK, ...)
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
}
