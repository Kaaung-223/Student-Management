package com.example.student_management_system.Controller.Model;

import java.time.LocalDate;

public class LeaveRequest {

    private int id;
    private String studentName;
    private String batchName;
    private LocalDate leaveFrom;
    private LocalDate leaveTo;
    private String status; // "Pending" / "Approved" / "Rejected"

    public LeaveRequest() {}

    public LeaveRequest(int id, String studentName, String batchName, LocalDate leaveFrom, LocalDate leaveTo, String status) {
        this.id = id;
        this.studentName = studentName;
        this.batchName = batchName;
        this.leaveFrom = leaveFrom;
        this.leaveTo = leaveTo;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

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
}
