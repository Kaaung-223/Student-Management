package com.example.student_management_system.Controller.Model;

public class ExamResultSummary {

    private int passCount;
    private int failCount;

    public ExamResultSummary(int passCount, int failCount) {
        this.passCount = passCount;
        this.failCount = failCount;
    }

    public int getPassCount() { return passCount; }
    public int getFailCount() { return failCount; }
}
