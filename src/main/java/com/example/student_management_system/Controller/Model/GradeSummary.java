package com.example.student_management_system.Controller.Model;

public class GradeSummary {

    private final int passCount;
    private final int failCount;
    private final double averageGpa;

    public GradeSummary(int passCount, int failCount, double averageGpa) {
        this.passCount = passCount;
        this.failCount = failCount;
        this.averageGpa = averageGpa;
    }

    public int getPassCount() {
        return passCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public int getGradedCount() {
        return passCount + failCount;
    }

    public double getPassRate() {
        int graded = getGradedCount();
        if (graded == 0) {
            return 0.0;
        }
        return (passCount * 100.0) / graded;
    }

    public double getAverageGpa() {
        return averageGpa;
    }
}
