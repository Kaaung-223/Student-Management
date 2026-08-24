package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.StudentProfile;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class StudentProfileDAO {
    private static final String SQL = "SELECT s.student_id,s.student_code,s.student_name,s.email,s.phone,s.class_id,s.admission_date,s.status," +
            "COALESCE(c.class_name,'Not assigned') batch_name," +
            "(SELECT COUNT(*) FROM attendance a WHERE a.student_id=s.student_id AND a.status='Present') present_count," +
            "(SELECT COUNT(*) FROM attendance a WHERE a.student_id=s.student_id AND a.status='Absent') absent_count," +
            "(SELECT COUNT(*) FROM attendance a WHERE a.student_id=s.student_id AND a.status='Late') late_count," +
            "(SELECT COUNT(*) FROM leave_requests l WHERE l.student_id=s.student_id AND l.status IN ('Pending','Approved')) leave_count," +
            "(SELECT COUNT(*) FROM grades g WHERE g.student_id=s.student_id AND g.result='PASS') pass_count," +
            "(SELECT COUNT(*) FROM grades g WHERE g.student_id=s.student_id AND g.result='FAIL') fail_count," +
            "(SELECT CONCAT(e.exam_name,' - ',g.result) FROM grades g JOIN exams e ON e.exam_id=g.exam_id WHERE g.student_id=s.student_id ORDER BY e.exam_date DESC,g.grade_id DESC LIMIT 1) latest_result " +
            "FROM students s LEFT JOIN classes c ON c.class_id=s.class_id ";

    public List<StudentProfile> findStudents(int classId, String search) {
        List<StudentProfile> list = new ArrayList<>();
        String q = SQL + "WHERE (?=-1 OR s.class_id=?) AND (s.student_name LIKE ? OR s.student_code LIKE ? OR s.email LIKE ? OR s.phone LIKE ?) ORDER BY s.student_name";
        try (Connection con = DBConnention.getConnection(); PreparedStatement ps = con.prepareStatement(q)) {
            String term = "%" + (search == null ? "" : search.trim()) + "%";
            ps.setInt(1, classId);
            ps.setInt(2, classId);
            ps.setString(3, term);
            ps.setString(4, term);
            ps.setString(5, term);
            ps.setString(6, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private StudentProfile map(ResultSet r) throws SQLException {
        StudentProfile s = new StudentProfile();
        s.setStudentId(r.getInt("student_id"));
        s.setClassId(r.getInt("class_id"));
        s.setStudentCode(r.getString("student_code"));
        s.setStudentName(r.getString("student_name"));
        s.setEmail(r.getString("email"));
        s.setPhone(r.getString("phone"));
        s.setBatchName(r.getString("batch_name"));
        s.setStatus(r.getString("status"));
        Date d = r.getDate("admission_date");
        if (d != null) s.setAdmissionDate(d.toLocalDate());
        s.setPresentCount(r.getInt("present_count"));
        s.setAbsentCount(r.getInt("absent_count"));
        s.setLateCount(r.getInt("late_count"));
        s.setLeaveCount(r.getInt("leave_count"));
        s.setPassCount(r.getInt("pass_count"));
        s.setFailCount(r.getInt("fail_count"));
        s.setLatestResult(r.getString("latest_result"));
        return s;
    }
}
