package com.example.student_management_system.Controller.Model;

public class StudentAttendanceRow {

    private final int studentId;
    private final String studentCode;
    private final String studentName;
    private final String className;
    private final int presentCount;
    private final int absentCount;
    private final int lateCount;
    private final int requestCount;
    private final int requestedDays;
    private final int extraAbsentDays;

    public StudentAttendanceRow(
            int studentId,
            String studentCode,
            String studentName,
            String className,
            int presentCount,
            int absentCount,
            int lateCount,
            int requestCount,
            int requestedDays,
            int extraAbsentDays
    ) {
        this.studentId = studentId;
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.className = className;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.lateCount = lateCount;
        this.requestCount = requestCount;
        this.requestedDays = requestedDays;
        this.extraAbsentDays = extraAbsentDays;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getClassName() {
        return className;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public int getLateCount() {
        return lateCount;
    }

    public int getRecordedDays() {
        return presentCount + absentCount + lateCount;
    }

    public double getAttendanceRate() {
        int total = getRecordedDays();
        if (total == 0) {
            return 0.0;
        }
        return (presentCount * 100.0) / total;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public int getRequestedDays() {
        return requestedDays;
    }

    public int getExtraAbsentDays() {
        return extraAbsentDays;
    }
}
