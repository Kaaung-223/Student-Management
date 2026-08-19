package com.example.student_management_system.Controller.Model;

public class ClassData {

    private int classId;
    private String className;
    private String academicYear;
    private String roomNo;


    public ClassData() {
    }


    public ClassData(
            int classId,
            String className,
            String academicYear,
            String roomNo
    ) {
        this.classId = classId;
        this.className = className;
        this.academicYear = academicYear;
        this.roomNo = roomNo;
    }


    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }


    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
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


    @Override
    public String toString() {
        return className;
    }
}