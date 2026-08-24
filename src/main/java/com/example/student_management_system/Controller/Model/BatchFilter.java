package com.example.student_management_system.Controller.Model;

public class BatchFilter {
    private final int id;
    private final String name;

    public BatchFilter(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
