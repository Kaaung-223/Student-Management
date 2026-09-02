package com.example.student_management_system.Controller.Model;


import java.time.LocalDateTime;

/**
 * Matches: users(user_id, full_name, username, password, role, status, photo_path, created_at)
 */
public class Admin {

    private int userId;
    private String fullName;
    private String username;
    private String password;   // never populate this except inside AdminDAO's own verify logic
    private String status;     // ACTIVE / INACTIVE
    private String photoPath;  // nullable
    private LocalDateTime createdAt;

    /** Kept exactly as-is so your existing getAdminById / getAdminByUsername calls still compile. */
    public Admin(int userId, String fullName, String username) {
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
    }

    /** Fuller constructor for the profile screen (photo + status + createdAt). */
    public Admin(int userId, String fullName, String username, String status,
                 String photoPath, LocalDateTime createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
        this.status = status;
        this.photoPath = photoPath;
        this.createdAt = createdAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "userId=" + userId +
                ", fullName='" + fullName + '\'' +
                ", username='" + username + '\'' +
                ", status='" + status + '\'' +
                ", photoPath='" + photoPath + '\'' +
                '}';
    }
}