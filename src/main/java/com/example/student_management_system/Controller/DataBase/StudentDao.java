package com.example.student_management_system.Controller.DataBase;



import com.example.student_management_system.Controller.Model.StudentRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudentDao {

    // ============================
    // GET ALL STUDENTS
    // ============================
    public List<StudentRecord> getAllStudents() {
        List<StudentRecord> students = new ArrayList<>();
        String sql = "SELECT s.*, c.class_name " +
                "FROM students s " +
                "LEFT JOIN classes c ON s.class_id = c.class_id " +
                "ORDER BY s.student_id DESC";

        try (Connection conn = DataBase_Connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    // ============================
    // SEARCH STUDENTS
    // ============================
    public List<StudentRecord> searchStudents(String keyword) {
        List<StudentRecord> students = new ArrayList<>();
        String sql = "SELECT s.*, c.class_name " +
                "FROM students s " +
                "LEFT JOIN classes c ON s.class_id = c.class_id " +
                "WHERE s.student_name LIKE ? OR s.student_code LIKE ? OR s.email LIKE ? " +
                "ORDER BY s.student_id DESC";

        String searchPattern = "%" + keyword + "%";
        try (Connection conn = DataBase_Connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudent(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    // ============================
    // ADD STUDENT
    // ============================
    public boolean addStudent(StudentRecord student) {
        String sql = "INSERT INTO students (student_code, student_name, email, phone, age, gender, address, class_id, admission_date, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataBase_Connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, student.getStudentCode());
            ps.setString(2, student.getStudentName());
            ps.setString(3, student.getEmail());
            ps.setString(4, student.getPhone());
            ps.setInt(5, student.getAge());
            ps.setString(6, student.getGender());
            ps.setString(7, student.getAddress());
            ps.setObject(8, getClassIdByName(student.getClassName()));
            ps.setDate(9, student.getAdmissionDate() != null ? Date.valueOf(student.getAdmissionDate()) : null);
            ps.setString(10, student.getStatus());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================
    // UPDATE STUDENT
    // ============================
    public boolean updateStudent(StudentRecord student) {
        String sql = "UPDATE students SET student_code = ?, student_name = ?, email = ?, phone = ?, age = ?, gender = ?, address = ?, class_id = ?, admission_date = ?, status = ? " +
                "WHERE student_id = ?";

        try (Connection conn = DataBase_Connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, student.getStudentCode());
            ps.setString(2, student.getStudentName());
            ps.setString(3, student.getEmail());
            ps.setString(4, student.getPhone());
            ps.setInt(5, student.getAge());
            ps.setString(6, student.getGender());
            ps.setString(7, student.getAddress());
            ps.setObject(8, getClassIdByName(student.getClassName()));
            ps.setDate(9, student.getAdmissionDate() != null ? Date.valueOf(student.getAdmissionDate()) : null);
            ps.setString(10, student.getStatus());
            ps.setInt(11, student.getStudentId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================
    // DELETE STUDENT
    // ============================
    public boolean deleteStudent(int studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection conn = DataBase_Connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================
    // GET ALL CLASS NAMES
    // ============================
    public List<String> getAllClassNames() {
        List<String> classNames = new ArrayList<>();
        String sql = "SELECT class_name FROM classes ORDER BY class_name";
        try (Connection conn = DataBase_Connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                classNames.add(rs.getString("class_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return classNames;
    }

    // ============================
    // HELPER: Map ResultSet
    // ============================
    private StudentRecord mapResultSetToStudent(ResultSet rs) throws SQLException {
        StudentRecord student = new StudentRecord();
        student.setStudentId(rs.getInt("student_id"));
        student.setStudentCode(rs.getString("student_code"));
        student.setStudentName(rs.getString("student_name"));
        student.setEmail(rs.getString("email"));
        student.setPhone(rs.getString("phone"));
        student.setAge(rs.getInt("age"));
        student.setGender(rs.getString("gender"));
        student.setAddress(rs.getString("address"));
        student.setClassName(rs.getString("class_name"));
        Date admissionDate = rs.getDate("admission_date");
        student.setAdmissionDate(admissionDate != null ? admissionDate.toLocalDate() : null);
        student.setStatus(rs.getString("status"));
        return student;
    }

    // ============================
    // HELPER: Get class_id from name
    // ============================
    private Integer getClassIdByName(String className) {
        if (className == null || className.isEmpty()) return null;
        String sql = "SELECT class_id FROM classes WHERE class_name = ?";
        try (Connection conn = DataBase_Connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, className);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("class_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}