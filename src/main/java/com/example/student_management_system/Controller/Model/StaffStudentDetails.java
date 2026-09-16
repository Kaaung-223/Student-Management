package com.example.student_management_system.Controller.Model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StaffStudentDetails {

    // Contact extras
    private String phone;
    private LocalDate admissionDate;

    // Attendance
    private int presentCount;
    private int absentCount;
    private int lateCount;

    // Academic
    private int passCount;
    private int failCount;
    private String latestResult;

    // Leave
    private int leaveCount;

    // Fees
    private BigDecimal totalFee;
    private BigDecimal totalPaid;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDate admissionDate) { this.admissionDate = admissionDate; }

    public int getPresentCount() { return presentCount; }
    public void setPresentCount(int presentCount) { this.presentCount = presentCount; }

    public int getAbsentCount() { return absentCount; }
    public void setAbsentCount(int absentCount) { this.absentCount = absentCount; }

    public int getLateCount() { return lateCount; }
    public void setLateCount(int lateCount) { this.lateCount = lateCount; }

    public int getPassCount() { return passCount; }
    public void setPassCount(int passCount) { this.passCount = passCount; }

    public int getFailCount() { return failCount; }
    public void setFailCount(int failCount) { this.failCount = failCount; }

    public String getLatestResult() { return latestResult; }
    public void setLatestResult(String latestResult) { this.latestResult = latestResult; }

    public int getLeaveCount() { return leaveCount; }
    public void setLeaveCount(int leaveCount) { this.leaveCount = leaveCount; }

    public BigDecimal getTotalFee() { return totalFee; }
    public void setTotalFee(BigDecimal totalFee) { this.totalFee = totalFee; }

    public BigDecimal getTotalPaid() { return totalPaid; }
    public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid = totalPaid; }

    /** Convenience — never returns null. */
    public BigDecimal getOutstanding() {
        BigDecimal fee = totalFee == null ? BigDecimal.ZERO : totalFee;
        BigDecimal paid = totalPaid == null ? BigDecimal.ZERO : totalPaid;
        return fee.subtract(paid);
    }

    public String getPaymentStatus() {
        BigDecimal fee = totalFee == null ? BigDecimal.ZERO : totalFee;
        BigDecimal paid = totalPaid == null ? BigDecimal.ZERO : totalPaid;

        if (fee.compareTo(BigDecimal.ZERO) == 0) return "N/A";
        if (paid.compareTo(fee) >= 0)             return "PAID";
        if (paid.compareTo(BigDecimal.ZERO) > 0)  return "PARTIAL";
        return "UNPAID";
    }
}