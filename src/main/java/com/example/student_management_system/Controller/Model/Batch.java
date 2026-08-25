package com.example.student_management_system.Controller.Model;

/**
 * Represents a "class" / "batch" (the FXML calls it a Class, dashboard filters call it a Batch —
 * they map to the same `classes` table).
 */
public class Batch {

    private int id;
    private String name;
    private String academicYear;
    private String roomNo;
    private int classTeacherId;
    private String classTeacherName;
    private int durationMonths;
    private String status;

    public Batch() {}

    public Batch(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Batch(
            int id,
            String name,
            String academicYear,
            String roomNo,
            int classTeacherId,
            String classTeacherName,
            int durationMonths,
            String status
    ) {
        this.id = id;
        this.name = name;
        this.academicYear = academicYear;
        this.roomNo = roomNo;
        this.classTeacherId = classTeacherId;
        this.classTeacherName = classTeacherName;
        this.durationMonths = durationMonths;
        this.status = status;
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

    public String getClassTeacherName() {
        return classTeacherName;
    }

    public void setClassTeacherName(String classTeacherName) {
        this.classTeacherName = classTeacherName;
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(int durationMonths) {
        this.durationMonths = durationMonths;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Important: this is what ComboBox shows by default when no custom cell factory is set.
    @Override
    public String toString() {
        return name;
    }

}
