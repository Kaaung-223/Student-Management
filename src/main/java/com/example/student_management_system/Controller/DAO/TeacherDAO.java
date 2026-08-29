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

    public int getTotalTeachers() {

        String sql =
                "SELECT COUNT(*) " +
                        "FROM teachers t " +
                        "JOIN users u ON t.user_id = u.user_id " +
                        "WHERE u.status = 'ACTIVE'";

        try (Connection con =
                     DBConnention.getConnection()) {

            if (con == null) {
                return 0;
            }

            try (
                    PreparedStatement ps =
                            con.prepareStatement(sql);
                    ResultSet rs =
                            ps.executeQuery()
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

    public List<Teacher> searchTeachers(
            String query
    ) {

        List<Teacher> teachers =
                new ArrayList<>();

        String sql =
                "SELECT " +
                        "t.teacher_id, " +
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
                        "ORDER BY sub.subject_name SEPARATOR ', ') " +
                        "AS subjects, " +
                        "cls.class_name AS class_leader_of " +
                        "FROM teachers t " +
                        "JOIN users u " +
                        "ON t.user_id = u.user_id " +
                        "LEFT JOIN teacher_subjects ts " +
                        "ON ts.teacher_id = t.teacher_id " +
                        "LEFT JOIN subjects sub " +
                        "ON sub.subject_id = ts.subject_id " +
                        "LEFT JOIN classes cls " +
                        "ON cls.class_teacher_id = t.teacher_id ";

        boolean hasQuery =
                query != null &&
                        !query.trim().isEmpty();

        if (hasQuery) {

            sql +=
                    "WHERE t.teacher_name LIKE ? " +
                            "OR t.teacher_code LIKE ? " +
                            "OR u.username LIKE ? " +
                            "OR t.email LIKE ? " +
                            "OR sub.subject_name LIKE ? ";
        }

        sql +=
                "GROUP BY " +
                        "t.teacher_id, " +
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
                        "cls.class_name " +
                        "ORDER BY t.teacher_name";

        try (Connection con =
                     DBConnention.getConnection()) {

            if (con == null) {
                return teachers;
            }

            try (
                    PreparedStatement ps =
                            con.prepareStatement(sql)
            ) {

                if (hasQuery) {

                    String like =
                            "%" + query.trim() + "%";

                    ps.setString(1, like);
                    ps.setString(2, like);
                    ps.setString(3, like);
                    ps.setString(4, like);
                    ps.setString(5, like);
                }

                try (
                        ResultSet rs =
                                ps.executeQuery()
                ) {

                    while (rs.next()) {

                        Date hireDateSql =
                                rs.getDate("hire_date");

                        BigDecimal salary =
                                rs.getBigDecimal("salary");

                        Teacher teacher =
                                new Teacher(
                                        rs.getInt(
                                                "teacher_id"
                                        ),
                                        rs.getString(
                                                "teacher_code"
                                        ),
                                        rs.getString(
                                                "teacher_name"
                                        ),
                                        rs.getString(
                                                "username"
                                        ),
                                        rs.getString(
                                                "email"
                                        ),
                                        rs.getString(
                                                "phone"
                                        ),
                                        rs.getString(
                                                "gender"
                                        ),
                                        rs.getString(
                                                "address"
                                        ),
                                        rs.getString(
                                                "photo_path"
                                        ),
                                        salary,
                                        hireDateSql != null
                                                ? hireDateSql.toLocalDate()
                                                : null,
                                        rs.getString(
                                                "status"
                                        ),
                                        rs.getString(
                                                "subjects"
                                        ),
                                        rs.getString(
                                                "class_leader_of"
                                        )
                                );

                        teachers.add(teacher);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return teachers;
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
                        "(user_id, teacher_code, teacher_name, email, " +
                        "phone, gender, address, photo_path, salary, hire_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String insertSubjectSql =
                "INSERT INTO teacher_subjects " +
                        "(teacher_id, subject_id) " +
                        "VALUES (?, ?)";

        String setClassLeaderSql =
                "UPDATE classes " +
                        "SET class_teacher_id = ? " +
                        "WHERE class_id = ?";

        Connection con = null;

        try {

            con =
                    DBConnention.getConnection();

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

                try (
                        ResultSet keys =
                                ps.getGeneratedKeys()
                ) {

                    if (!keys.next()) {
                        throw new SQLException(
                                "Could not create user account."
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

                    ps.setBigDecimal(
                            9,
                            salary
                    );

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

                try (
                        ResultSet keys =
                                ps.getGeneratedKeys()
                ) {

                    if (!keys.next()) {
                        throw new SQLException(
                                "Could not create teacher record."
                        );
                    }

                    newTeacherId =
                            keys.getInt(1);
                }
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

                        ps.setInt(
                                1,
                                newTeacherId
                        );

                        ps.setInt(
                                2,
                                subjectId
                        );

                        ps.addBatch();
                    }

                    ps.executeBatch();
                }
            }

            if (classLeaderOfClassId != -1) {

                try (
                        PreparedStatement ps =
                                con.prepareStatement(
                                        setClassLeaderSql
                                )
                ) {

                    ps.setInt(
                            1,
                            newTeacherId
                    );

                    ps.setInt(
                            2,
                            classLeaderOfClassId
                    );

                    ps.executeUpdate();
                }
            }

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
}