package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.Batch;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the classes table.
 *
 * This uses the same connection helper as the rest of the project:
 * DBConnention (the project currently spells "Connection" this way).
 */
public class ClassDAO {

    /*
     * The aliases in this SELECT allow the existing Batch model to use
     * simple property names while matching the actual database columns:
     *   classes(class_id, class_name, academic_year, room_no,
     *           class_teacher_id, duration_months, course_fee, class_status)
     *   teachers(teacher_id, teacher_name)
     */
    private static final String SELECT_SQL =
            "SELECT c.class_id AS id, c.class_name AS name, " +
                    "c.academic_year, c.room_no, c.class_teacher_id, " +
                    "c.duration_months, c.course_fee AS fees, " +
                    "c.class_status AS status, " +
                    "t.teacher_name AS class_teacher_name " +
                    "FROM classes c " +
                    "LEFT JOIN teachers t ON t.teacher_id = c.class_teacher_id ";

    public List<Batch> getAllClasses() {
        String sql = SELECT_SQL + "ORDER BY c.class_id DESC";
        List<Batch> classes = new ArrayList<>();

        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                classes.add(mapRow(resultSet));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return classes;
    }

    /**
     * Compatibility alias for existing dashboard code.
     * AdminDashboardController currently calls getAllBatches().
     */
    public List<Batch> getAllBatches() {
        return getAllClasses();
    }

    /**
     * Used by AdminDashboardController for the total-classes card.
     */
    public int getTotalClasses() {
        String sql = "SELECT COUNT(*) FROM classes";

        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return 0;
        }
    }

    public List<Batch> searchClasses(String keyword) {
        String sql = SELECT_SQL +
                "WHERE c.class_name LIKE ? " +
                "OR c.academic_year LIKE ? " +
                "OR c.room_no LIKE ? " +
                "OR COALESCE(t.teacher_name, '') LIKE ? " +
                "ORDER BY c.class_id DESC";

        String searchValue = "%" + keyword + "%";
        List<Batch> classes = new ArrayList<>();

        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            statement.setString(3, searchValue);
            statement.setString(4, searchValue);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    classes.add(mapRow(resultSet));
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return classes;
    }

    public boolean createClass(
            String name,
            String academicYear,
            String roomNo,
            int teacherId,
            int durationMonths,
            BigDecimal fees,
            String status
    ) {
        String sql =
                "INSERT INTO classes " +
                        "(class_name, academic_year, room_no, class_teacher_id, " +
                        "duration_months, course_fee, class_status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            setClassParameters(
                    statement,
                    name,
                    academicYear,
                    roomNo,
                    teacherId,
                    durationMonths,
                    fees,
                    status
            );
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean updateClass(
            int id,
            String name,
            String academicYear,
            String roomNo,
            int teacherId,
            int durationMonths,
            BigDecimal fees,
            String status
    ) {
        String sql =
                "UPDATE classes SET class_name = ?, academic_year = ?, " +
                        "room_no = ?, class_teacher_id = ?, duration_months = ?, " +
                        "course_fee = ?, class_status = ? WHERE class_id = ?";

        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            setClassParameters(
                    statement,
                    name,
                    academicYear,
                    roomNo,
                    teacherId,
                    durationMonths,
                    fees,
                    status
            );
            statement.setInt(8, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean deleteClass(int id) {
        String sql = "DELETE FROM classes WHERE class_id = ?";

        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    /*
     * These overloads keep older code compatible. They save a zero fee when
     * the old controller calls the DAO without a fees argument.
     */
    public boolean createClass(
            String name,
            String academicYear,
            String roomNo,
            int teacherId,
            int durationMonths,
            String status
    ) {
        return createClass(
                name,
                academicYear,
                roomNo,
                teacherId,
                durationMonths,
                BigDecimal.ZERO,
                status
        );
    }

    public boolean updateClass(
            int id,
            String name,
            String academicYear,
            String roomNo,
            int teacherId,
            int durationMonths,
            String status
    ) {
        return updateClass(
                id,
                name,
                academicYear,
                roomNo,
                teacherId,
                durationMonths,
                BigDecimal.ZERO,
                status
        );
    }

    private void setClassParameters(
            PreparedStatement statement,
            String name,
            String academicYear,
            String roomNo,
            int teacherId,
            int durationMonths,
            BigDecimal fees,
            String status
    ) throws SQLException {
        statement.setString(1, name);
        statement.setString(2, academicYear);
        statement.setString(3, roomNo);

        if (teacherId > 0) {
            statement.setInt(4, teacherId);
        } else {
            statement.setNull(4, java.sql.Types.INTEGER);
        }

        statement.setInt(5, durationMonths);
        statement.setBigDecimal(6, fees);
        statement.setString(7, status);
    }

    private Batch mapRow(ResultSet resultSet) throws SQLException {
        int teacherId = resultSet.getInt("class_teacher_id");
        if (resultSet.wasNull()) {
            teacherId = -1;
        }

        BigDecimal fees = resultSet.getBigDecimal("fees");
        if (fees == null) {
            fees = BigDecimal.ZERO;
        }

        return new Batch(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("academic_year"),
                resultSet.getString("room_no"),
                teacherId,
                resultSet.getInt("duration_months"),
                fees,
                resultSet.getString("status"),
                resultSet.getString("class_teacher_name")
        );
    }

    private Connection getConnection() throws SQLException {
        return DBConnention.getConnection();
    }
}