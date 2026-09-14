package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.AnnouncementRow;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherAnnouncementsDAO {

    /**
     * Returns announcements targeted to TEACHER or ALL.
     * Read-only — teachers never insert / update / delete announcements.
     */
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
}