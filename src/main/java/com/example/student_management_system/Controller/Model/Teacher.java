package com.example.student_management_system.Controller.Model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Teacher {

    private int id;
    private String teacherCode;
    private String teacherName;
    private String email;
    private String phone;
    private String gender;
    private String address;
    private String photoPath;      // local file path or URL; null = use default avatar
    private BigDecimal salary;
    private LocalDate hireDate;
    private String status;         // from the linked users row: ACTIVE / INACTIVE
    private String subjectsTaught; // comma-joined subject names, e.g. "Java Programming, Database Management"
    private String classLeaderOf;  // class_name if this teacher leads a class, otherwise null

    public Teacher() {}

    public Teacher(int id, String teacherCode, String teacherName, String email, String phone,
                   String gender, String address, String photoPath, BigDecimal salary,
                   LocalDate hireDate, String status, String subjectsTaught, String classLeaderOf) {
        this.id = id;
        this.teacherCode = teacherCode;
        this.teacherName = teacherName;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.address = address;
        this.photoPath = photoPath;
        this.salary = salary;
        this.hireDate = hireDate;
        this.status = status;
        this.subjectsTaught = subjectsTaught;
        this.classLeaderOf = classLeaderOf;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTeacherCode() { return teacherCode; }
    public void setTeacherCode(String teacherCode) { this.teacherCode = teacherCode; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSubjectsTaught() { return subjectsTaught; }
    public void setSubjectsTaught(String subjectsTaught) { this.subjectsTaught = subjectsTaught; }

    public String getClassLeaderOf() { return classLeaderOf; }
    public void setClassLeaderOf(String classLeaderOf) { this.classLeaderOf = classLeaderOf; }
}
