package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.Teacher;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TeacherDAO {

    private static final String TEACHER_SELECT_COLUMNS =
            "t.teacher_id, " +
                    "t.user_id, " +
                    "t.teacher_code, " +
                    "t.teacher_name, " +
                    "u.username, " +
                    "t.email, " +
                    "t.phone, " +
                    "t.gender, " +
                    "t.address, " +
                    "t.photo_path, " +
                    "t.salary, " +
                    "t.hire_date, " +
                    "u.status, " +
                    "GROUP_CONCAT(DISTINCT sub.subject_name " +
                    "ORDER BY sub.subject_name SEPARATOR ', ') AS subjects, " +
                    "cls.class_name AS class_leader_of ";

    private static final String TEACHER_FROM_JOINS =
            "FROM teachers t " +
                    "JOIN users u ON t.user_id = u.user_id " +
                    "LEFT JOIN teacher_subjects ts " +
                    "ON ts.teacher_id = t.teacher_id " +
                    "LEFT JOIN subjects sub " +
                    "ON sub.subject_id = ts.subject_id " +
                    "LEFT JOIN classes cls " +
                    "ON cls.class_teacher_id = t.teacher_id ";

    private static final String TEACHER_GROUP_BY =
            "GROUP BY " +
                    "t.teacher_id, " +
                    "t.user_id, " +
                    "t.teacher_code, " +
                    "t.teacher_name, " +
                    "u.username, " +
                    "t.email, " +
                    "t.phone, " +
                    "t.gender, " +
                    "t.address, " +
                    "t.photo_path, " +
                    "t.salary, " +
                    "t.hire_date, " +
                    "u.status, " +
                    "cls.class_name ";

    public int getTotalTeachers() {

        String sql =
                "SELECT COUNT(*) " +
                        "FROM teachers t " +
                        "JOIN users u ON t.user_id = u.user_id " +
                        "WHERE u.status = 'ACTIVE'";

        try (Connection con = DBConnention.getConnection()) {

            if (con == null) {
                return 0;
            }

            try (
                    PreparedStatement ps = con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()
            ) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public List<Teacher> getAllTeachers() {
        return searchTeachers(null);
    }

    public List<Teacher> searchTeachers(String query) {

        List<Teacher> teachers = new ArrayList<>();

        String sql =
                "SELECT " +
                        TEACHER_SELECT_COLUMNS +
                        TEACHER_FROM_JOINS;

        boolean hasSearch =
                query != null &&
                        !query.trim().isEmpty();

        if (hasSearch) {

            sql +=
                    "WHERE t.teacher_name LIKE ? " +
                            "OR t.teacher_code LIKE ? " +
                            "OR u.username LIKE ? " +
                            "OR t.email LIKE ? " +
                            "OR sub.subject_name LIKE ? ";
        }

        sql +=
                TEACHER_GROUP_BY +
                        "ORDER BY t.teacher_name";

        try (Connection con = DBConnention.getConnection()) {

            if (con == null) {
                return teachers;
            }

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                if (hasSearch) {

                    String searchValue =
                            "%" + query.trim() + "%";

                    ps.setString(1, searchValue);
                    ps.setString(2, searchValue);
                    ps.setString(3, searchValue);
                    ps.setString(4, searchValue);
                    ps.setString(5, searchValue);
                }

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        teachers.add(
                                mapTeacherRow(rs)
                        );
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return teachers;
    }

    public Teacher getTeacherById(int teacherId) {

        String sql =
                "SELECT " +
                        TEACHER_SELECT_COLUMNS +
                        TEACHER_FROM_JOINS +
                        "WHERE t.teacher_id = ? " +
                        TEACHER_GROUP_BY;

        try (Connection con = DBConnention.getConnection()) {

            if (con == null) {
                return null;
            }

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setInt(1, teacherId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        return mapTeacherRow(rs);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Integer> getTeacherSubjectIds(int teacherId) {

        List<Integer> subjectIds = new ArrayList<>();

        String sql =
                "SELECT subject_id " +
                        "FROM teacher_subjects " +
                        "WHERE teacher_id = ? " +
                        "ORDER BY subject_id";

        try (Connection con = DBConnention.getConnection()) {

            if (con == null) {
                return subjectIds;
            }

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setInt(1, teacherId);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        subjectIds.add(
                                rs.getInt("subject_id")
                        );
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subjectIds;
    }

    public int getClassLeaderClassId(int teacherId) {

        String sql =
                "SELECT class_id " +
                        "FROM classes " +
                        "WHERE class_teacher_id = ? " +
                        "LIMIT 1";

        try (Connection con = DBConnention.getConnection()) {

            if (con == null) {
                return -1;
            }

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setInt(1, teacherId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        return rs.getInt("class_id");
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public boolean isUsernameTaken(
            String username,
            int excludeUserId
    ) {

        String sql =
                "SELECT COUNT(*) " +
                        "FROM users " +
                        "WHERE username = ? " +
                        "AND user_id <> ?";

        try (Connection con = DBConnention.getConnection()) {

            if (con == null) {
                return true;
            }

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setString(1, username);
                ps.setInt(2, excludeUserId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean isTeacherCodeTaken(
            String teacherCode,
            int excludeTeacherId
    ) {

        if (
                teacherCode == null ||
                        teacherCode.isBlank()
        ) {
            return false;
        }

        String sql =
                "SELECT COUNT(*) " +
                        "FROM teachers " +
                        "WHERE teacher_code = ? " +
                        "AND teacher_id <> ?";

        try (Connection con = DBConnention.getConnection()) {

            if (con == null) {
                return true;
            }

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setString(1, teacherCode);
                ps.setInt(2, excludeTeacherId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    public int addTeacher(
            String fullName,
            String username,
            String password,
            String teacherCode,
            String email,
            String phone,
            String gender,
            String address,
            LocalDate hireDate,
            BigDecimal salary,
            String photoPath,
            List<Integer> subjectIds,
            int classLeaderOfClassId
    ) {

        String insertUserSql =
                "INSERT INTO users " +
                        "(full_name, username, password, role, status) " +
                        "VALUES (?, ?, ?, 'TEACHER', 'ACTIVE')";

        String insertTeacherSql =
                "INSERT INTO teachers " +
                        "(user_id, teacher_code, teacher_name, email, phone, gender, " +
                        "address, photo_path, salary, hire_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String insertSubjectSql =
                "INSERT INTO teacher_subjects " +
                        "(teacher_id, subject_id) VALUES (?, ?)";

        String setClassLeaderSql =
                "UPDATE classes " +
                        "SET class_teacher_id = ? " +
                        "WHERE class_id = ?";

        Connection con = null;

        try {

            con = DBConnention.getConnection();

            if (con == null) {
                return -1;
            }

            con.setAutoCommit(false);

            int newUserId;

            try (
                    PreparedStatement ps =
                            con.prepareStatement(
                                    insertUserSql,
                                    Statement.RETURN_GENERATED_KEYS
                            )
            ) {

                ps.setString(1, fullName);
                ps.setString(2, username);
                ps.setString(3, password);

                ps.executeUpdate();

                try (ResultSet keys =
                             ps.getGeneratedKeys()) {

                    if (!keys.next()) {
                        throw new SQLException(
                                "User account was not created."
                        );
                    }

                    newUserId =
                            keys.getInt(1);
                }
            }

            int newTeacherId;

            try (
                    PreparedStatement ps =
                            con.prepareStatement(
                                    insertTeacherSql,
                                    Statement.RETURN_GENERATED_KEYS
                            )
            ) {

                ps.setInt(1, newUserId);
                ps.setString(2, teacherCode);
                ps.setString(3, fullName);
                ps.setString(4, email);
                ps.setString(5, phone);
                ps.setString(6, gender);
                ps.setString(7, address);
                ps.setString(8, photoPath);

                if (salary != null) {
                    ps.setBigDecimal(9, salary);
                } else {
                    ps.setNull(
                            9,
                            java.sql.Types.DECIMAL
                    );
                }

                if (hireDate != null) {
                    ps.setDate(
                            10,
                            Date.valueOf(hireDate)
                    );
                } else {
                    ps.setNull(
                            10,
                            java.sql.Types.DATE
                    );
                }

                ps.executeUpdate();

                try (ResultSet keys =
                             ps.getGeneratedKeys()) {

                    if (!keys.next()) {
                        throw new SQLException(
                                "Teacher record was not created."
                        );
                    }

                    newTeacherId =
                            keys.getInt(1);
                }
            }

            insertTeacherSubjects(
                    con,
                    newTeacherId,
                    subjectIds
            );

            updateClassLeader(
                    con,
                    newTeacherId,
                    classLeaderOfClassId
            );

            con.commit();

            return newTeacherId;

        } catch (SQLException e) {

            e.printStackTrace();

            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }

            return -1;

        } finally {

            if (con != null) {

                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean updateTeacher(
            int teacherId,
            int userId,
            String fullName,
            String username,
            String password,
            String teacherCode,
            String email,
            String phone,
            String gender,
            String address,
            LocalDate hireDate,
            BigDecimal salary,
            String photoPath,
            List<Integer> subjectIds,
            int classLeaderOfClassId
    ) {

        String updateUserSql =
                password == null
                        ? "UPDATE users " +
                        "SET full_name = ?, username = ? " +
                        "WHERE user_id = ?"
                        : "UPDATE users " +
                        "SET full_name = ?, username = ?, password = ? " +
                        "WHERE user_id = ?";

        String updateTeacherSql =
                "UPDATE teachers " +
                        "SET teacher_code = ?, teacher_name = ?, email = ?, phone = ?, " +
                        "gender = ?, address = ?, photo_path = ?, salary = ?, hire_date = ? " +
                        "WHERE teacher_id = ?";

        String deleteSubjectsSql =
                "DELETE FROM teacher_subjects " +
                        "WHERE teacher_id = ?";

        String insertSubjectSql =
                "INSERT INTO teacher_subjects " +
                        "(teacher_id, subject_id) VALUES (?, ?)";

        String clearClassLeaderSql =
                "UPDATE classes " +
                        "SET class_teacher_id = NULL " +
                        "WHERE class_teacher_id = ?";

        String setClassLeaderSql =
                "UPDATE classes " +
                        "SET class_teacher_id = ? " +
                        "WHERE class_id = ?";

        Connection con = null;

        try {

            con = DBConnention.getConnection();

            if (con == null) {
                return false;
            }

            con.setAutoCommit(false);

            try (PreparedStatement ps =
                         con.prepareStatement(updateUserSql)) {

                ps.setString(1, fullName);
                ps.setString(2, username);

                if (password == null) {
                    ps.setInt(3, userId);
                } else {
                    ps.setString(3, password);
                    ps.setInt(4, userId);
                }

                if (ps.executeUpdate() <= 0) {
                    throw new SQLException(
                            "User account was not updated."
                    );
                }
            }

            try (PreparedStatement ps =
                         con.prepareStatement(updateTeacherSql)) {

                ps.setString(1, teacherCode);
                ps.setString(2, fullName);
                ps.setString(3, email);
                ps.setString(4, phone);
                ps.setString(5, gender);
                ps.setString(6, address);
                ps.setString(7, photoPath);

                if (salary != null) {
                    ps.setBigDecimal(8, salary);
                } else {
                    ps.setNull(
                            8,
                            java.sql.Types.DECIMAL
                    );
                }

                if (hireDate != null) {
                    ps.setDate(
                            9,
                            Date.valueOf(hireDate)
                    );
                } else {
                    ps.setNull(
                            9,
                            java.sql.Types.DATE
                    );
                }

                ps.setInt(10, teacherId);

                if (ps.executeUpdate() <= 0) {
                    throw new SQLException(
                            "Teacher record was not updated."
                    );
                }
            }

            try (
                    PreparedStatement ps =
                            con.prepareStatement(
                                    deleteSubjectsSql
                            )
            ) {

                ps.setInt(1, teacherId);
                ps.executeUpdate();
            }

            if (
                    subjectIds != null &&
                            !subjectIds.isEmpty()
            ) {

                try (
                        PreparedStatement ps =
                                con.prepareStatement(
                                        insertSubjectSql
                                )
                ) {

                    for (int subjectId : subjectIds) {

                        ps.setInt(1, teacherId);
                        ps.setInt(2, subjectId);
                        ps.addBatch();
                    }

                    ps.executeBatch();
                }
            }

            try (
                    PreparedStatement ps =
                            con.prepareStatement(
                                    clearClassLeaderSql
                            )
            ) {

                ps.setInt(1, teacherId);
                ps.executeUpdate();
            }

            if (classLeaderOfClassId != -1) {

                try (
                        PreparedStatement ps =
                                con.prepareStatement(
                                        setClassLeaderSql
                                )
                ) {

                    ps.setInt(1, teacherId);
                    ps.setInt(2, classLeaderOfClassId);
                    ps.executeUpdate();
                }
            }

            con.commit();

            return true;

        } catch (SQLException e) {

            e.printStackTrace();

            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }

            return false;

        } finally {

            if (con != null) {

                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean deleteTeacher(int teacherId) {

        String clearClassLeaderSql =
                "UPDATE classes " +
                        "SET class_teacher_id = NULL " +
                        "WHERE class_teacher_id = ?";

        String deleteSubjectsSql =
                "DELETE FROM teacher_subjects " +
                        "WHERE teacher_id = ?";

        String selectUserSql =
                "SELECT user_id " +
                        "FROM teachers " +
                        "WHERE teacher_id = ?";

        String deleteTeacherSql =
                "DELETE FROM teachers " +
                        "WHERE teacher_id = ?";

        String deleteUserSql =
                "DELETE FROM users " +
                        "WHERE user_id = ?";

        Connection con = null;

        try {

            con = DBConnention.getConnection();

            if (con == null) {
                return false;
            }

            con.setAutoCommit(false);

            int userId = -1;

            try (
                    PreparedStatement ps =
                            con.prepareStatement(
                                    selectUserSql
                            )
            ) {

                ps.setInt(1, teacherId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        return false;
                    }

                    userId = rs.getInt("user_id");
                }
            }

            try (
                    PreparedStatement ps =
                            con.prepareStatement(
                                    clearClassLeaderSql
                            )
            ) {

                ps.setInt(1, teacherId);
                ps.executeUpdate();
            }

            try (
                    PreparedStatement ps =
                            con.prepareStatement(
                                    deleteSubjectsSql
                            )
            ) {

                ps.setInt(1, teacherId);
                ps.executeUpdate();
            }

            try (
                    PreparedStatement ps =
                            con.prepareStatement(
                                    deleteTeacherSql
                            )
            ) {

                ps.setInt(1, teacherId);

                if (ps.executeUpdate() <= 0) {
                    throw new SQLException(
                            "Teacher record was not deleted."
                    );
                }
            }

            try (
                    PreparedStatement ps =
                            con.prepareStatement(
                                    deleteUserSql
                            )
            ) {

                ps.setInt(1, userId);

                if (ps.executeUpdate() <= 0) {
                    throw new SQLException(
                            "User account was not deleted."
                    );
                }
            }

            con.commit();

            return true;

        } catch (SQLException e) {

            e.printStackTrace();

            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }

            return false;

        } finally {

            if (con != null) {

                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Teacher mapTeacherRow(ResultSet rs)
            throws SQLException {

        Date hireDateSql =
                rs.getDate("hire_date");

        BigDecimal salary =
                rs.getBigDecimal("salary");

        return new Teacher(
                rs.getInt("teacher_id"),
                rs.getInt("user_id"),
                rs.getString("teacher_code"),
                rs.getString("teacher_name"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("gender"),
                rs.getString("address"),
                rs.getString("photo_path"),
                salary,
                hireDateSql != null
                        ? hireDateSql.toLocalDate()
                        : null,
                rs.getString("status"),
                rs.getString("subjects"),
                rs.getString("class_leader_of")
        );
    }

    private void insertTeacherSubjects(
            Connection con,
            int teacherId,
            List<Integer> subjectIds
    ) throws SQLException {

        if (
                subjectIds == null ||
                        subjectIds.isEmpty()
        ) {
            return;
        }

        String insertSubjectSql =
                "INSERT INTO teacher_subjects " +
                        "(teacher_id, subject_id) VALUES (?, ?)";

        try (
                PreparedStatement ps =
                        con.prepareStatement(
                                insertSubjectSql
                        )
        ) {

            for (int subjectId : subjectIds) {

                ps.setInt(1, teacherId);
                ps.setInt(2, subjectId);
                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    private void updateClassLeader(
            Connection con,
            int teacherId,
            int classLeaderOfClassId
    ) throws SQLException {

        if (classLeaderOfClassId == -1) {
            return;
        }

        String setClassLeaderSql =
                "UPDATE classes " +
                        "SET class_teacher_id = ? " +
                        "WHERE class_id = ?";

        try (
                PreparedStatement ps =
                        con.prepareStatement(
                                setClassLeaderSql
                        )
        ) {

            ps.setInt(1, teacherId);
            ps.setInt(2, classLeaderOfClassId);
            ps.executeUpdate();
        }
    }
}
