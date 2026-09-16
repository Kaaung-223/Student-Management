package com.example.student_management_system.Controller.Model;

public class Student {

    private int studentId;
    private String studentCode;
    private String studentName;
    private String email;
    private String status;
    private int classId;
    private String className;

    public Student() {}

    public Student(int studentId, String studentCode, String studentName, String email,
                   String status, int classId, String className) {
        this.studentId = studentId;
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.email = email;
        this.status = status;
        this.classId = classId;
        this.className = className;
    }

    // ---------- primary getters ----------
    public int getStudentId() { return studentId; }
    public String getStudentCode() { return studentCode; }
    public String getStudentName() { return studentName; }
    public String getEmail() { return email; }
    public String getStatus() { return status; }
    public int getClassId() { return classId; }
    public String getClassName() { return className; }

    // ---------- alias getters (compatibility) ----------
    public int getId() { return studentId; }
    public String getName() { return studentName; }
    public String getBatchName() { return className; }

    // ---------- setters ----------
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setEmail(String email) { this.email = email; }
    public void setStatus(String status) { this.status = status; }
    public void setClassId(int classId) { this.classId = classId; }
    public void setClassName(String className) { this.className = className; }

    @Override
    public String toString() {
        return studentCode + " - " + studentName;
    }
}