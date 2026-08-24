// StudentPerformance.java
package com.example.student_management_system.Controller.Model;

public class StudentPerformance {
    private final int present;
    private final int absent;
    private final int late;
    private final double attendanceRate;
    private final double gpa;

    public StudentPerformance(
            int present,
            int absent,
            int late,
            double attendanceRate,
            double gpa
    ) {
        this.present = present;
        this.absent = absent;
        this.late = late;
        this.attendanceRate = attendanceRate;
        this.gpa = gpa;
    }

    public int getPresent() {
        return present;
    }

    public int getAbsent() {
        return absent;
    }

    public int getLate() {
        return late;
    }

    public double getAttendanceRate() {
        return attendanceRate;
    }

    public double getGpa() {
        return gpa;
    }
}