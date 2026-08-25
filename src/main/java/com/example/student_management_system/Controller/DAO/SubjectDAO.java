package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.SubjectOption;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Matches: subjects(subject_id, subject_name, subject_code)
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

    /** All subjects, used for the subject-picker checklist in the Add Teacher dialog. */
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
}
