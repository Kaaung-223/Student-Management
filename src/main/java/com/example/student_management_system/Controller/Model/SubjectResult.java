package com.example.student_management_system.Controller.Model;

public class SubjectResult {

    private String subjectName;
    private int obtainedMarks;
    private int totalMarks;
    private double percentage;
    private String letterGrade;
    private String result;   // PASS / FAIL

    public SubjectResult() {}

    public SubjectResult(String subjectName, int obtainedMarks, int totalMarks,
                         double percentage, String letterGrade, String result) {
        this.subjectName = subjectName;
        this.obtainedMarks = obtainedMarks;
        this.totalMarks = totalMarks;
        this.percentage = percentage;
        this.letterGrade = letterGrade;
        this.result = result;
    }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public int getObtainedMarks() { return obtainedMarks; }
    public void setObtainedMarks(int obtainedMarks) { this.obtainedMarks = obtainedMarks; }

    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public String getLetterGrade() { return letterGrade; }
    public void setLetterGrade(String letterGrade) { this.letterGrade = letterGrade; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}