package com.example.student_management_system.Controller.Model;

import java.math.BigDecimal;

public class PaymentStudent {

    private int studentId;
    private int classId;
    private String studentCode;
    private String studentName;
    private String batchName;
    private String status;          // ACTIVE / INACTIVE

    private BigDecimal totalFee;
    private BigDecimal totalPaid;

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalFee() { return totalFee; }
    public void setTotalFee(BigDecimal totalFee) { this.totalFee = totalFee; }

    public BigDecimal getTotalPaid() { return totalPaid; }
    public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid = totalPaid; }

    public BigDecimal getOutstanding() {
        BigDecimal f = totalFee  == null ? BigDecimal.ZERO : totalFee;
        BigDecimal p = totalPaid == null ? BigDecimal.ZERO : totalPaid;
        BigDecimal o = f.subtract(p);
        return o.signum() < 0 ? BigDecimal.ZERO : o;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    public String getInitials() {
        if (studentName == null || studentName.isBlank()) return "--";
        String[] parts = studentName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}