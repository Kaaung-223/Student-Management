package com.example.student_management_system.Controller.Model;

import java.time.LocalDate;

public class LeaveRequest {

    private int id;
    private int studentId;
    private String studentName;
    private String batchName;
    private LocalDate leaveFrom;
    private LocalDate leaveTo;
    private String status; // "Pending" / "Approved" / "Rejected"
    private String studentCode;
    private String reason;

    public LeaveRequest() {}

    public LeaveRequest(int id, String studentName, String batchName, LocalDate leaveFrom, LocalDate leaveTo, String status) {
        this(id, 0, null, studentName, batchName, leaveFrom, leaveTo, status, null);
    }

    public LeaveRequest(
            int id,
            int studentId,
            String studentCode,
            String studentName,
            String batchName,
            LocalDate leaveFrom,
            LocalDate leaveTo,
            String status,
            String reason
    ) {
        this.id = id;
        this.studentId = studentId;
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.batchName = batchName;
        this.leaveFrom = leaveFrom;
        this.leaveTo = leaveTo;
        this.status = status;
        this.reason = reason;
    }

    public int getLeaveDays() {
        if (leaveFrom == null || leaveTo == null) {
            return 0;
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(leaveFrom, leaveTo) + 1;
        return days < 0 ? 0 : (int) days;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }

    public LocalDate getLeaveFrom() { return leaveFrom; }
    public void setLeaveFrom(LocalDate leaveFrom) { this.leaveFrom = leaveFrom; }

    public LocalDate getLeaveTo() { return leaveTo; }
    public void setLeaveTo(LocalDate leaveTo) { this.leaveTo = leaveTo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
