package com.example.student_management_system.Controller.Model;

public class AnnouncementNotice {

    private final int count;
    private final String title;

    public AnnouncementNotice(int count, String title) {
        this.count = count;
        this.title = title;
    }

    public int getCount() { return count; }
    public String getTitle() { return title; }
}