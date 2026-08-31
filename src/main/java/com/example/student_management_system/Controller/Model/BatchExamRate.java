package com.example.student_management_system.Controller.Model;

import java.time.LocalDate;

public class BatchExamRate {

    private final int examId;
    private final String examName;
    private final String className;
    private final LocalDate examDate;
    private final int gradedCount;
    private final int passCount;
    private final int failCount;

    public BatchExamRate(
            int examId,
            String examName,
            String className,
            LocalDate examDate,
            int gradedCount,
            int passCount,
            int failCount
    ) {
        this.examId = examId;
        this.examName = examName;
        this.className = className;
        this.examDate = examDate;
        this.gradedCount = gradedCount;
        this.passCount = passCount;
        this.failCount = failCount;
    }

    public int getExamId() {
        return examId;
    }

    public String getExamName() {
        return examName;
    }

    public String getClassName() {
        return className;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public int getGradedCount() {
        return gradedCount;
    }

    public int getPassCount() {
        return passCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public double getPassRate() {
        if (gradedCount == 0) {
            return 0.0;
        }
        return (passCount * 100.0) / gradedCount;
    }
}
