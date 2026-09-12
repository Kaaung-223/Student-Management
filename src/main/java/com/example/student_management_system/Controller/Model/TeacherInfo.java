package com.example.student_management_system.Controller.Model;

/**
 * Plain data holder for a logged-in teacher.
 * Populated by TeacherDashboardDAO.getTeacherByUsername(...).
 */
public class TeacherInfo {

    private int teacherId;
    private int userId;
    private String username;
    private String fullName;      // users.full_name
    private String teacherName;   // teachers.teacher_name
    private String teacherCode;
    private String email;
    private String phone;
    private String gender;
    private String address;
    private String photoPath;     // teachers.photo_path, falls back to users.photo_path

    public TeacherInfo() {}

    public TeacherInfo(int teacherId, int userId, String username, String fullName,
                       String teacherName, String teacherCode, String email,
                       String phone, String gender, String address, String photoPath) {
        this.teacherId   = teacherId;
        this.userId      = userId;
        this.username    = username;
        this.fullName    = fullName;
        this.teacherName = teacherName;
        this.teacherCode = teacherCode;
        this.email       = email;
        this.phone       = phone;
        this.gender      = gender;
        this.address     = address;
        this.photoPath   = photoPath;
    }

    // ---------- Getters ----------
    public int getTeacherId()      { return teacherId; }
    public int getUserId()         { return userId; }
    public String getUsername()    { return username; }
    public String getFullName()    { return fullName; }
    public String getTeacherName() { return teacherName; }
    public String getTeacherCode() { return teacherCode; }
    public String getEmail()       { return email; }
    public String getPhone()       { return phone; }
    public String getGender()      { return gender; }
    public String getAddress()     { return address; }
    public String getPhotoPath()   { return photoPath; }

    // ---------- Setters ----------
    public void setTeacherId(int teacherId)         { this.teacherId = teacherId; }
    public void setUserId(int userId)               { this.userId = userId; }
    public void setUsername(String username)        { this.username = username; }
    public void setFullName(String fullName)        { this.fullName = fullName; }
    public void setTeacherName(String teacherName)  { this.teacherName = teacherName; }
    public void setTeacherCode(String teacherCode)  { this.teacherCode = teacherCode; }
    public void setEmail(String email)              { this.email = email; }
    public void setPhone(String phone)              { this.phone = phone; }
    public void setGender(String gender)            { this.gender = gender; }
    public void setAddress(String address)          { this.address = address; }
    public void setPhotoPath(String photoPath)      { this.photoPath = photoPath; }

    /** Best-effort display name. */
    public String getDisplayName() {
        if (fullName != null && !fullName.isBlank())         return fullName;
        if (teacherName != null && !teacherName.isBlank())   return teacherName;
        return username != null ? username : "Teacher";
    }

    @Override
    public String toString() {
        return "TeacherInfo{id=" + teacherId +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", code='" + teacherCode + '\'' + '}';
    }
}