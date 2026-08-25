package com.example.student_management_system.Controller.Model;

import javafx.beans.property.SimpleBooleanProperty;

/** Used in the Add Teacher dialog's subject checklist (one row per subject, with a checkbox). */
public class SubjectOption {

    private final int subjectId;
    private final String subjectName;
    private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

    public SubjectOption(int subjectId, String subjectName) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
    }

    public int getSubjectId() { return subjectId; }
    public String getSubjectName() { return subjectName; }

    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean value) { selected.set(value); }
    public SimpleBooleanProperty selectedProperty() { return selected; }

    @Override
    public String toString() { return subjectName; }
}
