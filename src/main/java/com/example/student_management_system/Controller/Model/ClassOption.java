package com.example.student_management_system.Controller.Model;

public class ClassOption {
    private int id;
    private String name;

    public ClassOption(int id, String name) {
        this.id = id;
        this.name = name;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    @Override
    public String toString() { return name; }
}