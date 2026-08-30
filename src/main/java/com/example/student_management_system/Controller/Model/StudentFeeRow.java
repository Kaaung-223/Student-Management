package com.example.student_management_system.Controller.Model;

import java.math.BigDecimal;

public class StudentFeeRow {

    private int studentId;
    private String studentCode;
    private String studentName;
    private int classId;
    private String className;
    private BigDecimal classFee;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    private String paymentStatus;

    public StudentFeeRow(
            int studentId,
            String studentCode,
            String studentName,
            int classId,
            String className,
            BigDecimal classFee,
            BigDecimal amountPaid,
            BigDecimal balanceDue,
            String paymentStatus
    ) {
        this.studentId = studentId;
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.classId = classId;
        this.className = className;
        this.classFee = classFee;
        this.amountPaid = amountPaid;
        this.balanceDue = balanceDue;
        this.paymentStatus = paymentStatus;
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

    public int getClassId() {
        return classId;
    }

    public String getClassName() {
        return className;
    }

    public BigDecimal getClassFee() {
        return classFee;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public BigDecimal getBalanceDue() {
        return balanceDue;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
}
