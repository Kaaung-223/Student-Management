package com.example.student_management_system.Controller.Model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentRow {

    private int paymentId;
    private int studentId;
    private String studentName;
    private String studentCode;
    private String batchName;

    private BigDecimal amount;
    private LocalDate paymentDate;
    private String method;
    private String period;
    private String receiptNo;
    private String note;

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getMethodLabel() {
        if (method == null) return "--";
        switch (method) {
            case "CASH":        return "Cash";
            case "CREDIT_CARD": return "Credit Card";
            case "BANK":        return "Bank Transfer";
            case "AYA_PAY":     return "AYA Pay";
            case "KBZ_PAY":     return "KBZ Pay";
            case "WAVE_PAY":    return "Wave Pay";
            default:            return method;
        }
    }

    public String getPeriodLabel() {
        if (period == null) return "--";
        switch (period) {
            case "FIRST_6_MONTHS":  return "First 6 Months";
            case "SECOND_6_MONTHS": return "Second 6 Months";
            default:                return period;
        }
    }
}