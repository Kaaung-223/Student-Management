package com.example.student_management_system.Controller.Model;

public class ExamResultRow {

    private int studentId;
    private String studentCode;
    private String studentName;
    private String batchName;
    private int subjectCount;
    private double avgPercentage;
    private double gpa;
    private int passCount;
    private int failCount;
    private String overallResult;   // PASS / FAIL

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }

    public int getSubjectCount() { return subjectCount; }
    public void setSubjectCount(int subjectCount) { this.subjectCount = subjectCount; }

    public double getAvgPercentage() { return avgPercentage; }
    public void setAvgPercentage(double avgPercentage) { this.avgPercentage = avgPercentage; }

    public double getGpa() { return gpa; }
    public void setGpa(double gpa) { this.gpa = gpa; }

    public int getPassCount() { return passCount; }
    public void setPassCount(int passCount) { this.passCount = passCount; }

    public int getFailCount() { return failCount; }
    public void setFailCount(int failCount) { this.failCount = failCount; }

    public String getOverallResult() { return overallResult; }
    public void setOverallResult(String overallResult) { this.overallResult = overallResult; }

    public String getInitials() {
        if (studentName == null || studentName.isBlank()) return "--";
        String[] parts = studentName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}