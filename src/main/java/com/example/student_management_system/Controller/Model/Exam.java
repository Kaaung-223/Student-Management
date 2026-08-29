package com.example.student_management_system.Controller.Model;

import java.time.LocalDate;

public class Exam {

    private int id;
    private String examName;
    private int classId;
    private String className; // resolved via join, for display
    private LocalDate examDate;
    private int totalMarks;
    private int gradesRecorded; // how many grade rows exist for this exam, for display

    public Exam() {}

    public Exam(int id, String examName, int classId, String className,
                LocalDate examDate, int totalMarks, int gradesRecorded) {
        this.id = id;
        this.examName = examName;
        this.classId = classId;
        this.className = className;
        this.examDate = examDate;
        this.totalMarks = totalMarks;
        this.gradesRecorded = gradesRecorded;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    public int getGradesRecorded() { return gradesRecorded; }
    public void setGradesRecorded(int gradesRecorded) { this.gradesRecorded = gradesRecorded; }
}
