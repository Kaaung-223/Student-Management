package com.example.student_management_system.Controller.DataBase;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AdminDashboardDAO {

    private Connection getConnection() throws SQLException {
        return DataBase_Connection.getConnection();
    }

    // =========================================================
    // ADMIN NAME
    // =========================================================
    public String getAdminName() {
        String sql = "SELECT full_name FROM users WHERE role = 'ADMIN' AND status = 'ACTIVE' LIMIT 1";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString("full_name");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Administrator";
    }

    // =========================================================
    // STATISTICS
    // =========================================================
    public int getTotalStudents() {
        return getCount("SELECT COUNT(*) FROM students WHERE status = 'ACTIVE'");
    }
    public int getTotalTeachers() {
        return getCount("SELECT COUNT(*) FROM teachers t INNER JOIN users u ON t.user_id = u.user_id WHERE u.status = 'ACTIVE'");
    }
    public int getTotalClasses() {
        return getCount("SELECT COUNT(*) FROM classes WHERE class_status = 'ACTIVE'");
    }
    public int getTotalSubjects() {
        return getCount("SELECT COUNT(*) FROM subjects");
    }

    // =========================================================
    // ATTENDANCE COUNTS by period
    // =========================================================
    public Map<String, Integer> getAttendanceCounts(String period) {
        Map<String, Integer> result = new HashMap<>();
        String dateCondition = "";
        LocalDate now = LocalDate.now();
        switch (period) {
            case "Today":
                dateCondition = "attendance_date = '" + now.format(DateTimeFormatter.ISO_LOCAL_DATE) + "'";
                break;
            case "This Month":
                dateCondition = "MONTH(attendance_date) = " + now.getMonthValue() + " AND YEAR(attendance_date) = " + now.getYear();
                break;
            case "This Year":
                dateCondition = "YEAR(attendance_date) = " + now.getYear();
                break;
            default:
                dateCondition = "1=1";
        }
        String sql = "SELECT status, COUNT(*) as cnt FROM attendance WHERE " + dateCondition + " GROUP BY status";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        result.putIfAbsent("Present", 0);
        result.putIfAbsent("Absent", 0);
        result.putIfAbsent("Late", 0);
        return result;
    }

    // =========================================================
    // EXAM NAMES and PASS/FAIL
    // =========================================================
    public List<String> getExamNames() {
        List<String> names = new ArrayList<>();
        String sql = "SELECT exam_name FROM exams ORDER BY exam_name";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                names.add(rs.getString("exam_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }

    public int getExamIdByName(String examName) {
        String sql = "SELECT exam_id FROM exams WHERE exam_name = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, examName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("exam_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Map<String, Integer> getExamPassFailCounts(int examId) {
        Map<String, Integer> result = new HashMap<>();
        String sql = "SELECT result, COUNT(*) as cnt FROM grades WHERE exam_id = ? GROUP BY result";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("result"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        result.putIfAbsent("PASS", 0);
        result.putIfAbsent("FAIL", 0);
        return result;
    }

    // =========================================================
    // BATCH / CLASS STUDENTS
    // =========================================================
    public List<String> getClassNames() {
        List<String> names = new ArrayList<>();
        String sql = "SELECT class_name FROM classes WHERE class_status = 'ACTIVE' ORDER BY class_name";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                names.add(rs.getString("class_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }

    public int getClassIdByName(String className) {
        String sql = "SELECT class_id FROM classes WHERE class_name = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, className);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("class_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<Student> getStudentsByClassId(int classId) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT student_code, student_name, email, status FROM students WHERE class_id = ? AND status = 'ACTIVE'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Student s = new Student();
                s.setStudentCode(rs.getString("student_code"));
                s.setStudentName(rs.getString("student_name"));
                s.setEmail(rs.getString("email"));
                s.setStatus(rs.getString("status"));
                students.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    // =========================================================
    // PENDING LEAVE REQUESTS by date
    // =========================================================
    public List<LeaveRequest> getPendingLeavesByDate(LocalDate date) {
        List<LeaveRequest> leaves = new ArrayList<>();
        String sql = "SELECT s.student_name, l.leave_from, l.leave_to, l.status " +
                "FROM leave_requests l " +
                "INNER JOIN students s ON l.student_id = s.student_id " +
                "WHERE DATE(l.created_at) = ? AND l.status = 'Pending'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LeaveRequest lr = new LeaveRequest();
                lr.setStudentName(rs.getString("student_name"));
                lr.setLeaveFrom(rs.getString("leave_from"));
                lr.setLeaveTo(rs.getString("leave_to"));
                lr.setStatus(rs.getString("status"));
                leaves.add(lr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leaves;
    }

    // =========================================================
    // GENERIC COUNT
    // =========================================================
    private int getCount(String sql) {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // =========================================================
    // INNER CLASSES FOR DATA MODELS (if not using separate ones)
    // =========================================================
    public static class Student {
        private final StringProperty studentCode = new SimpleStringProperty();
        private final StringProperty studentName = new SimpleStringProperty();
        private final StringProperty email = new SimpleStringProperty();
        private final StringProperty status = new SimpleStringProperty();

        public String getStudentCode() { return studentCode.get(); }
        public StringProperty studentCodeProperty() { return studentCode; }
        public void setStudentCode(String code) { studentCode.set(code); }

        public String getStudentName() { return studentName.get(); }
        public StringProperty studentNameProperty() { return studentName; }
        public void setStudentName(String name) { studentName.set(name); }

        public String getEmail() { return email.get(); }
        public StringProperty emailProperty() { return email; }
        public void setEmail(String email) { this.email.set(email); }

        public String getStatus() { return status.get(); }
        public StringProperty statusProperty() { return status; }
        public void setStatus(String status) { this.status.set(status); }
    }

    public static class LeaveRequest {
        private final StringProperty studentName = new SimpleStringProperty();
        private final StringProperty leaveFrom = new SimpleStringProperty();
        private final StringProperty leaveTo = new SimpleStringProperty();
        private final StringProperty status = new SimpleStringProperty();

        public String getStudentName() { return studentName.get(); }
        public StringProperty studentNameProperty() { return studentName; }
        public void setStudentName(String name) { studentName.set(name); }

        public String getLeaveFrom() { return leaveFrom.get(); }
        public StringProperty leaveFromProperty() { return leaveFrom; }
        public void setLeaveFrom(String from) { leaveFrom.set(from); }

        public String getLeaveTo() { return leaveTo.get(); }
        public StringProperty leaveToProperty() { return leaveTo; }
        public void setLeaveTo(String to) { leaveTo.set(to); }

        public String getStatus() { return status.get(); }
        public StringProperty statusProperty() { return status; }
        public void setStatus(String status) { this.status.set(status); }
    }
}