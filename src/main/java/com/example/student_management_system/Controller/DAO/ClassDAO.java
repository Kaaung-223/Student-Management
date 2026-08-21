package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.Batch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Matches: classes(class_id, class_name, academic_year, room_no, duration_months, class_status, created_at)
 */
public class ClassDAO {

    /** Count of classes with class_status = 'ACTIVE' (for the "Total Classes" stat card). */
    public int getTotalClasses() {
        String sql = "SELECT COUNT(*) FROM classes WHERE class_status = 'ACTIVE'";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Active batches, used to populate the "Students by Batch" dropdown and attendance batch filter. */
    public List<Batch> getAllBatches() {
        List<Batch> batches = new ArrayList<>();
        String sql = "SELECT class_id, class_name FROM classes WHERE class_status = 'ACTIVE' ORDER BY class_name";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                batches.add(new Batch(rs.getInt("class_id"), rs.getString("class_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return batches;
    }
}
