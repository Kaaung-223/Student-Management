package com.example.student_management_system.Controller.Model;

public class AttendanceRow {

    private int studentId;
    private String studentCode;
    private String studentName;
    private String status;    // "Present" | "Absent" | "Late" | null
    private String remarks;

    public AttendanceRow() {}

    public AttendanceRow(int studentId, String studentCode, String studentName,
                         String status, String remarks) {
        this.studentId = studentId;
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.status = status;
        this.remarks = remarks;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}