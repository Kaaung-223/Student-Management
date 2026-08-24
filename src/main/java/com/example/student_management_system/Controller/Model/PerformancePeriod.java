// PerformancePeriod.java
package com.example.student_management_system.Controller.Model;

public class PerformancePeriod {

    private final String name;
    private final Integer year;
    private final Integer month;

    public PerformancePeriod(String name, Integer year, Integer month) {
        this.name = name;
        this.year = year;
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public Integer getMonth() {
        return month;
    }

    @Override
    public String toString() {
        return name;
    }
}