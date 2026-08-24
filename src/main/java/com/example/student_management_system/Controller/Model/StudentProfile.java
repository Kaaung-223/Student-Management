package com.example.student_management_system.Controller.Model;

import java.time.LocalDate;

public class StudentProfile {
    private int studentId, classId, presentCount, absentCount, lateCount, leaveCount, passCount, failCount;
    private String studentCode, studentName, email, phone, batchName, status, latestResult;
    private LocalDate admissionDate;

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int v) {
        studentId = v;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int v) {
        classId = v;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(int v) {
        presentCount = v;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public void setAbsentCount(int v) {
        absentCount = v;
    }

    public int getLateCount() {
        return lateCount;
    }

    public void setLateCount(int v) {
        lateCount = v;
    }

    public int getLeaveCount() {
        return leaveCount;
    }

    public void setLeaveCount(int v) {
        leaveCount = v;
    }

    public int getPassCount() {
        return passCount;
    }

    public void setPassCount(int v) {
        passCount = v;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int v) {
        failCount = v;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String v) {
        studentCode = v;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String v) {
        studentName = v;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String v) {
        email = v;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String v) {
        phone = v;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String v) {
        batchName = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        status = v;
    }

    public String getLatestResult() {
        return latestResult;
    }

    public void setLatestResult(String v) {
        latestResult = v;
    }

    public LocalDate getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDate v) {
        admissionDate = v;
    }

    public String getInitials() {
        if (studentName == null || studentName.isBlank()) return "--";
        String[] p = studentName.trim().split("\\s+");
        return p.length == 1 ? p[0].substring(0, 1).toUpperCase() : (p[0].substring(0, 1) + p[p.length - 1].substring(0, 1)).toUpperCase();
    }
}
