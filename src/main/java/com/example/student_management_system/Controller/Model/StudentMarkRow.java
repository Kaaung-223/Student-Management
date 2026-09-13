package com.example.student_management_system.Controller.Model;

public class StudentMarkRow {

    private int studentId;
    private String studentCode;
    private String studentName;
    private Integer marks;      // null = not entered yet
    private String remarks;

    public StudentMarkRow() {}

    public StudentMarkRow(int studentId, String studentCode,
                          String studentName, Integer marks, String remarks) {
        this.studentId = studentId;
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.marks = marks;
        this.remarks = remarks;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}