package com.example.student_management_system.Controller.Model;


import java.sql.Timestamp;
import java.time.LocalDate;

public class Announcement {
    private int announcementId;
    private String title;
    private String reason;
    private LocalDate announcementDate;
    private String targetAudience; // 'TEACHER', 'STAFF', 'ALL'
    private int createdBy;
    private String createdByName;
    private Timestamp createdAt;

    public Announcement() {}

    public Announcement(int announcementId, String title, String reason, LocalDate announcementDate, String targetAudience, int createdBy) {
        this.announcementId = announcementId;
        this.title = title;
        this.reason = reason;
        this.announcementDate = announcementDate;
        this.targetAudience = targetAudience;
        this.createdBy = createdBy;
    }

    public Announcement(String title, String reason, LocalDate announcementDate, String targetAudience, int createdBy) {
        this.title = title;
        this.reason = reason;
        this.announcementDate = announcementDate;
        this.targetAudience = targetAudience;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public int getAnnouncementId() { return announcementId; }
    public void setAnnouncementId(int announcementId) { this.announcementId = announcementId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDate getAnnouncementDate() { return announcementDate; }
    public void setAnnouncementDate(LocalDate announcementDate) { this.announcementDate = announcementDate; }

    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
