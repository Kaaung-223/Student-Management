package com.example.student_management_system.Controller.Model;

public class Subject {

    private int id;
    private String subjectName;
    private String subjectCode;
    private String classesUsingIt; // comma-joined class_names this subject belongs to; may be empty

    public Subject() {}

    public Subject(int id, String subjectName, String subjectCode, String classesUsingIt) {
        this.id = id;
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.classesUsingIt = classesUsingIt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getClassesUsingIt() { return classesUsingIt; }
    public void setClassesUsingIt(String classesUsingIt) { this.classesUsingIt = classesUsingIt; }
}
