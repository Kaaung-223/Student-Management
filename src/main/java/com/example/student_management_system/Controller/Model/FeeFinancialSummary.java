package com.example.student_management_system.Controller.Model;

import java.math.BigDecimal;

public class FeeFinancialSummary {

    private int totalStudents;
    private int paidStudents;
    private int unpaidStudents;
    private int partialStudents;
    private BigDecimal totalExpected;
    private BigDecimal totalCollected;
    private BigDecimal totalOutstanding;

    public FeeFinancialSummary(
            int totalStudents,
            int paidStudents,
            int unpaidStudents,
            int partialStudents,
            BigDecimal totalExpected,
            BigDecimal totalCollected,
            BigDecimal totalOutstanding
    ) {
        this.totalStudents = totalStudents;
        this.paidStudents = paidStudents;
        this.unpaidStudents = unpaidStudents;
        this.partialStudents = partialStudents;
        this.totalExpected = totalExpected;
        this.totalCollected = totalCollected;
        this.totalOutstanding = totalOutstanding;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public int getPaidStudents() {
        return paidStudents;
    }

    public int getUnpaidStudents() {
        return unpaidStudents;
    }

    public int getPartialStudents() {
        return partialStudents;
    }

    public BigDecimal getTotalExpected() {
        return totalExpected;
    }

    public BigDecimal getTotalCollected() {
        return totalCollected;
    }

    public BigDecimal getTotalOutstanding() {
        return totalOutstanding;
    }
}
