package com.example.student_management_system.Controller.Model;

import java.time.LocalDate;

public class DateRangeOption {

    private final String label;
    private final LocalDate from;
    private final LocalDate to;

    public DateRangeOption(String label, LocalDate from, LocalDate to) {
        this.label = label;
        this.from = from;
        this.to = to;
    }

    public String getLabel() {
        return label;
    }

    public LocalDate getFrom() {
        return from;
    }

    public LocalDate getTo() {
        return to;
    }

    public boolean isUnbounded() {
        return from == null && to == null;
    }

    @Override
    public String toString() {
        return label;
    }
}
