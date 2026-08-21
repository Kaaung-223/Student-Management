package com.example.student_management_system.Controller.Model;

/**
 * Represents a "class" / "batch" (the FXML calls it a Class, dashboard filters call it a Batch —
 * they map to the same `classes` table).
 */
public class Batch {

    private int id;
    private String name;

    public Batch() {}

    public Batch(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Important: this is what ComboBox shows by default when no custom cell factory is set.
    @Override
    public String toString() {
        return name;
    }
}
