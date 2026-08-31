package com.example.student_management_system.Controller.DAO;


import com.example.student_management_system.Controller.Model.Announcement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementDAO {

    public boolean insert(Announcement announcement) {
        String sql = "INSERT INTO announcements (title, reason, target_audience, announcement_date, created_by) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnention.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, announcement.getTitle());
            stmt.setString(2, announcement.getReason());
            stmt.setString(3, announcement.getTargetAudience());
            stmt.setDate(4, Date.valueOf(announcement.getAnnouncementDate()));
            stmt.setInt(5, announcement.getCreatedBy());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Announcement announcement) {
        String sql = "UPDATE announcements SET title = ?, reason = ?, target_audience = ?, announcement_date = ? WHERE announcement_id = ?";
        try (Connection conn = DBConnention.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, announcement.getTitle());
            stmt.setString(2, announcement.getReason());
            stmt.setString(3, announcement.getTargetAudience());
            stmt.setDate(4, Date.valueOf(announcement.getAnnouncementDate()));
            stmt.setInt(5, announcement.getAnnouncementId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int announcementId) {
        String sql = "DELETE FROM announcements WHERE announcement_id = ?";
        try (Connection conn = DBConnention.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, announcementId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Announcement> getAllAnnouncements() {
        List<Announcement> list = new ArrayList<>();
        String sql = "SELECT a.*, u.full_name FROM announcements a JOIN users u ON a.created_by = u.user_id ORDER BY a.created_at DESC";

        try (Connection conn = DBConnention.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Announcement a = new Announcement();
                a.setAnnouncementId(rs.getInt("announcement_id"));
                a.setTitle(rs.getString("title"));
                a.setReason(rs.getString("reason"));
                a.setTargetAudience(rs.getString("target_audience"));
                a.setAnnouncementDate(rs.getDate("announcement_date").toLocalDate());
                a.setCreatedBy(rs.getInt("created_by"));
                a.setCreatedByName(rs.getString("full_name"));
                a.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}