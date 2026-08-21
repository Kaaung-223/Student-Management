package com.example.student_management_system.Controller.Model;

/** Maps to a row in `users` where role = 'ADMIN'. */
public class Admin {

    private int userId;
    private String fullName;
    private String username;

    public Admin() {}

    public Admin(int userId, String fullName, String username) {
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
