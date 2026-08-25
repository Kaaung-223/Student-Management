package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.Batch;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.example.student_management_system.Controller.DAO.DBConnention.getConnection;

public class ClassDAO {


    // =========================================================
    // GET ALL CLASSES
    // =========================================================

    public List<Batch> getAllClasses() {

        List<Batch> list = new ArrayList<>();

        String sql = """
                SELECT
                    c.class_id,
                    c.class_name,
                    c.academic_year,
                    c.room_no,
                    c.class_teacher_id,
                    t.teacher_name,
                    c.duration_months,
                    c.class_status
                FROM classes c
                LEFT JOIN teachers t
                    ON c.class_teacher_id = t.teacher_id
                ORDER BY c.class_id DESC
                """;

        try (
                Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                list.add(new Batch(
                        rs.getInt("class_id"),
                        rs.getString("class_name"),
                        rs.getString("academic_year"),
                        rs.getString("room_no"),
                        rs.getInt("class_teacher_id"),
                        rs.getString("teacher_name"),
                        rs.getInt("duration_months"),
                        rs.getString("class_status")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // =========================================================
    // GET ALL BATCHES
    // Used by AdminDashboardController
    // =========================================================

    public List<Batch> getAllBatches() {
        return getAllClasses();
    }

    // =========================================================
    // GET CLASS BY ID
    // =========================================================

    public Batch getClassById(int id) {

        String sql = """
                SELECT
                    c.class_id,
                    c.class_name,
                    c.academic_year,
                    c.room_no,
                    c.class_teacher_id,
                    t.teacher_name,
                    c.duration_months,
                    c.class_status
                FROM classes c
                LEFT JOIN teachers t
                    ON c.class_teacher_id = t.teacher_id
                WHERE c.class_id = ?
                """;

        try (
                Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return new Batch(
                            rs.getInt("class_id"),
                            rs.getString("class_name"),
                            rs.getString("academic_year"),
                            rs.getString("room_no"),
                            rs.getInt("class_teacher_id"),
                            rs.getString("teacher_name"),
                            rs.getInt("duration_months"),
                            rs.getString("class_status")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // =========================================================
    // CREATE CLASS
    // =========================================================

    public boolean createClass(
            String className,
            String academicYear,
            String roomNo,
            int teacherId,
            int durationMonths,
            String status
    ) {

        String sql = """
                INSERT INTO classes
                (
                    class_name,
                    academic_year,
                    room_no,
                    class_teacher_id,
                    duration_months,
                    class_status
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, className);
            ps.setString(2, academicYear);
            ps.setString(3, roomNo);

            if (teacherId > 0) {
                ps.setInt(4, teacherId);
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.setInt(5, durationMonths);
            ps.setString(6, status);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }

    // =========================================================
    // UPDATE CLASS
    // =========================================================

    public boolean updateClass(
            int classId,
            String className,
            String academicYear,
            String roomNo,
            int teacherId,
            int durationMonths,
            String status
    ) {

        String sql = """
                UPDATE classes
                SET
                    class_name = ?,
                    academic_year = ?,
                    room_no = ?,
                    class_teacher_id = ?,
                    duration_months = ?,
                    class_status = ?
                WHERE class_id = ?
                """;

        try (
                Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, className);
            ps.setString(2, academicYear);
            ps.setString(3, roomNo);

            if (teacherId > 0) {
                ps.setInt(4, teacherId);
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.setInt(5, durationMonths);
            ps.setString(6, status);
            ps.setInt(7, classId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }

    // =========================================================
    // DELETE CLASS
    // =========================================================

    public boolean deleteClass(int classId) {

        String sql = """
                DELETE FROM classes
                WHERE class_id = ?
                """;

        try (
                Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, classId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }

    // =========================================================
    // SEARCH CLASS
    // =========================================================

    public List<Batch> searchClasses(String keyword) {

        List<Batch> list = new ArrayList<>();

        String sql = """
                SELECT
                    c.class_id,
                    c.class_name,
                    c.academic_year,
                    c.room_no,
                    c.class_teacher_id,
                    t.teacher_name,
                    c.duration_months,
                    c.class_status
                FROM classes c
                LEFT JOIN teachers t
                    ON c.class_teacher_id = t.teacher_id
                WHERE
                    c.class_name LIKE ?
                    OR c.academic_year LIKE ?
                    OR c.room_no LIKE ?
                    OR t.teacher_name LIKE ?
                ORDER BY c.class_id DESC
                """;

        try (
                Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            String search = "%" + keyword + "%";

            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);
            ps.setString(4, search);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    list.add(new Batch(
                            rs.getInt("class_id"),
                            rs.getString("class_name"),
                            rs.getString("academic_year"),
                            rs.getString("room_no"),
                            rs.getInt("class_teacher_id"),
                            rs.getString("teacher_name"),
                            rs.getInt("duration_months"),
                            rs.getString("class_status")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // =========================================================
    // TOTAL CLASSES
    // Used by Dashboard
    // =========================================================

    public int getTotalClasses() {

        String sql = "SELECT COUNT(*) FROM classes";

        try (
                Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}