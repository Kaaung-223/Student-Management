package com.example.student_management_system.Controller.Model;

public class AttendanceSummary {

    private int presentCount;
    private int absentCount;
    private int lateCount;

    public AttendanceSummary(int presentCount, int absentCount, int lateCount) {
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.lateCount = lateCount;
    }

    public int getPresentCount() { return presentCount; }
    public int getAbsentCount() { return absentCount; }
    public int getLateCount() { return lateCount; }
}
