package com.example.student_management_system.Controller.Model;

import java.time.LocalDate;

public class LeaveRequestRow {

    private int leaveId;
    private int studentId;
    private String studentCode;
    private String studentName;
    private String batchName;
    private LocalDate leaveFrom;
    private LocalDate leaveTo;
    private int daysCount;
    private String reason;
    private String status;         // Pending / Approved / Rejected
    private String approvedBy;     // full_name from users, null if pending
    private LocalDate createdAt;

    public int getLeaveId() { return leaveId; }
    public void setLeaveId(int leaveId) { this.leaveId = leaveId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }

    public LocalDate getLeaveFrom() { return leaveFrom; }
    public void setLeaveFrom(LocalDate leaveFrom) { this.leaveFrom = leaveFrom; }

    public LocalDate getLeaveTo() { return leaveTo; }
    public void setLeaveTo(LocalDate leaveTo) { this.leaveTo = leaveTo; }

    public int getDaysCount() { return daysCount; }
    public void setDaysCount(int daysCount) { this.daysCount = daysCount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public boolean isPending() {
        return "Pending".equalsIgnoreCase(status);
    }

    public String getInitials() {
        if (studentName == null || studentName.isBlank()) return "--";
        String[] parts = studentName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}