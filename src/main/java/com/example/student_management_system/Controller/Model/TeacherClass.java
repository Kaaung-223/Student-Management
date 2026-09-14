package com.example.student_management_system.Controller.Model;

public class TeacherClass {

    private int classId;
    private String className;
    private String academicYear;
    private String roomNo;
    private int studentCount;

    private boolean isClassLeader;       // true if this teacher is the class teacher
    private String subjectsTaught;       // comma-joined subject names taught by this teacher in this class

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }

    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }

    public boolean isClassLeader() { return isClassLeader; }
    public void setClassLeader(boolean classLeader) { isClassLeader = classLeader; }

    public String getSubjectsTaught() { return subjectsTaught; }
    public void setSubjectsTaught(String subjectsTaught) { this.subjectsTaught = subjectsTaught; }

    public String getRoleLabel() {
        return isClassLeader ? "Class Leader" : "Subject Teacher";
    }
}