package com.example.student_management_system.Controller.Model;

public class Student {

    private int id;
    private String studentCode;
    private String studentName;   // maps to students.student_name
    private String email;
    private String status;        // students.status: 'ACTIVE' / 'INACTIVE'
    private int batchId;          // students.class_id
    private String batchName;     // classes.class_name

    public Student() {}

    public Student(int id, String studentCode, String studentName, String email, String status, int batchId, String batchName) {
        this.id = id;
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.email = email;
        this.status = status;
        this.batchId = batchId;
        this.batchName = batchName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getBatchId() { return batchId; }
    public void setBatchId(int batchId) { this.batchId = batchId; }

    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }
}
