package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.AnnouncementNotice;
import com.example.student_management_system.Controller.Model.AnnouncementRow;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherAnnouncementsDAO {

    // ==================================================
    //  List of announcements (for the page)
    // ==================================================
    public List<AnnouncementRow> findAnnouncements(String search) {
        List<AnnouncementRow> list = new ArrayList<>();

        String sql =
                "SELECT a.announcement_id, a.title, a.reason, a.target_audience, " +
                        "       a.announcement_date, a.created_at, u.full_name AS created_by_name " +
                        "FROM announcements a " +
                        "LEFT JOIN users u ON u.user_id = a.created_by " +
                        "WHERE a.target_audience IN ('TEACHER', 'ALL') " +
                        "AND (a.title LIKE ? OR a.reason LIKE ?) " +
                        "ORDER BY a.announcement_date DESC, a.announcement_id DESC";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String like = "%" + (search == null ? "" : search.trim()) + "%";
            ps.setString(1, like);
            ps.setString(2, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AnnouncementRow a = new AnnouncementRow();
                    a.setAnnouncementId(rs.getInt("announcement_id"));
                    a.setTitle(rs.getString("title"));
                    a.setReason(rs.getString("reason"));
                    a.setTargetAudience(rs.getString("target_audience"));

                    Date d = rs.getDate("announcement_date");
                    if (d != null) a.setAnnouncementDate(d.toLocalDate());

                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) a.setCreatedAt(ts.toLocalDateTime());

                    a.setCreatedBy(rs.getString("created_by_name"));
                    list.add(a);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ==================================================
    //  Unread count + latest title for the login toast
    //  Uses users.last_seen_announcement_id
    // ==================================================
    public AnnouncementNotice getUnreadAnnouncementNotice(int userId) {

        String sql =
                "SELECT a.title FROM announcements a " +
                        "WHERE a.target_audience IN ('TEACHER','ALL') " +
                        "AND a.announcement_id > ( " +
                        "    SELECT COALESCE(last_seen_announcement_id, 0) " +
                        "    FROM users WHERE user_id = ? " +
                        ") " +
                        "ORDER BY a.announcement_id DESC";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String latestTitle = rs.getString("title");
                int count = 1;
                while (rs.next()) count++;

                return new AnnouncementNotice(count, latestTitle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==================================================
    //  Mark everything as read for this user
    // ==================================================
    public void markAllAsRead(int userId) {
        String maxSql =
                "SELECT COALESCE(MAX(announcement_id), 0) AS max_id " +
                        "FROM announcements " +
                        "WHERE target_audience IN ('TEACHER','ALL')";

        String updateSql =
                "UPDATE users SET last_seen_announcement_id = ? WHERE user_id = ?";

        try (Connection con = DBConnention.getConnection()) {

            int maxId = 0;
            try (PreparedStatement ps = con.prepareStatement(maxSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) maxId = rs.getInt("max_id");
            }

            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setInt(1, maxId);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}