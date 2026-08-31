package com.example.student_management_system.Controller.Model;

public class StudentGradeRow {

    private final int studentId;
    private final String studentCode;
    private final String studentName;
    private final String className;
    private final String examName;
    private final int examsTaken;
    private final int passCount;
    private final int failCount;
    private final Integer obtainedMarks;
    private final Double percentage;
    private final String letterGrade;
    private final String result;
    private final double gpa;

    public StudentGradeRow(
            int studentId,
            String studentCode,
            String studentName,
            String className,
            String examName,
            int examsTaken,
            int passCount,
            int failCount,
            Integer obtainedMarks,
            Double percentage,
            String letterGrade,
            String result,
            double gpa
    ) {
        this.studentId = studentId;
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.className = className;
        this.examName = examName;
        this.examsTaken = examsTaken;
        this.passCount = passCount;
        this.failCount = failCount;
        this.obtainedMarks = obtainedMarks;
        this.percentage = percentage;
        this.letterGrade = letterGrade;
        this.result = result;
        this.gpa = gpa;
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

    public String getExamName() {
        return examName;
    }

    public int getExamsTaken() {
        return examsTaken;
    }

    public int getPassCount() {
        return passCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public Integer getObtainedMarks() {
        return obtainedMarks;
    }

    public Double getPercentage() {
        return percentage;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public String getResult() {
        return result;
    }

    public double getGpa() {
        return gpa;
    }
}
