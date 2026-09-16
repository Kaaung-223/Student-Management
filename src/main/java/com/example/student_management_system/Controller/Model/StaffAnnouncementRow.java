package com.example.student_management_system.Controller.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class StaffAnnouncementRow {

    private int announcementId;
    private String title;
    private String reason;
    private String targetAudience;   // TEACHER / STAFF / ALL
    private LocalDate announcementDate;
    private LocalDateTime createdAt;
    private String createdBy;         // admin full name

    public int getAnnouncementId() { return announcementId; }
    public void setAnnouncementId(int announcementId) { this.announcementId = announcementId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

    public LocalDate getAnnouncementDate() { return announcementDate; }
    public void setAnnouncementDate(LocalDate announcementDate) { this.announcementDate = announcementDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getAudienceLabel() {
        if (targetAudience == null) return "--";
        switch (targetAudience) {
            case "TEACHER": return "Teachers";
            case "STAFF":   return "Staff";
            case "ALL":     return "Everyone";
            default:        return targetAudience;
        }
    }
}