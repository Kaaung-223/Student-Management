package com.example.student_management_system.Controller.Model;

import java.sql.Date;

public  class LeaveRecord {

    private final int leaveId;

    private final int studentId;

    private final String studentName;

    private final Date leaveFrom;

    private final Date leaveTo;

    private final String reason;

    private final String status;


    public LeaveRecord(
            int leaveId,
            int studentId,
            String studentName,
            Date leaveFrom,
            Date leaveTo,
            String reason,
            String status
    ) {

        this.leaveId = leaveId;

        this.studentId = studentId;

        this.studentName = studentName;

        this.leaveFrom = leaveFrom;

        this.leaveTo = leaveTo;

        this.reason = reason;

        this.status = status;
    }


    public int getLeaveId() {
        return leaveId;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public Date getLeaveFrom() {
        return leaveFrom;
    }

    public Date getLeaveTo() {
        return leaveTo;
    }

    public String getReason() {
        return reason;
    }

    public String getStatus() {
        return status;
    }
}