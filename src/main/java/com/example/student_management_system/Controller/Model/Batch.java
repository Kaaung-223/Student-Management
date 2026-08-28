package com.example.student_management_system.Controller.Model;

import java.math.BigDecimal;

/**
 * Model used by the admin class screen.
 */
public class Batch {

    private int id;
    private String name;
    private String academicYear;
    private String roomNo;
    private int classTeacherId;
    private int durationMonths;
    private BigDecimal fees;
    private String status;
    private String classTeacherName;

    public Batch() {
    }

    /**
     * Kept for dashboard/statistics code that only needs a class ID and name.
     */
    public Batch(int id, String name) {
        this.id = id;
        this.name = name;
        this.fees = BigDecimal.ZERO;
        this.status = "ACTIVE";
    }

    public Batch(
            int id,
            String name,
            String academicYear,
            String roomNo,
            int classTeacherId,
            int durationMonths,
            BigDecimal fees,
            String status,
            String classTeacherName
    ) {
        this.id = id;
        this.name = name;
        this.academicYear = academicYear;
        this.roomNo = roomNo;
        this.classTeacherId = classTeacherId;
        this.durationMonths = durationMonths;
        this.fees = fees;
        this.status = status;
        this.classTeacherName = classTeacherName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public int getClassTeacherId() {
        return classTeacherId;
    }

    public void setClassTeacherId(int classTeacherId) {
        this.classTeacherId = classTeacherId;
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(int durationMonths) {
        this.durationMonths = durationMonths;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public void setFees(BigDecimal fees) {
        this.fees = fees;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClassTeacherName() {
        return classTeacherName;
    }

    public void setClassTeacherName(String classTeacherName) {
        this.classTeacherName = classTeacherName;
    }

    @Override
    public String toString() {
        return name == null ? "" : name;
    }
}