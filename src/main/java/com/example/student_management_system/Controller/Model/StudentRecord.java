package com.example.student_management_system.Controller.Model;

import java.util.Date;
//
//public  class StudentRecord {
//
//    private final int studentId;
//
//    private final String studentCode;
//
//    private final String studentName;
//
//    private final String email;
//
//    private final String phone;
//
//    private final String status;
//
//    private final Date admissionDate;
//
//
//
//    public StudentRecord(
//            int studentId,
//            String studentCode,
//            String studentName,
//            String email,
//            String phone,
//            String status,
//            Date admissionDate
//
//    ) {
//
//        this.studentId = studentId;
//
//        this.studentCode = studentCode;
//
//        this.studentName = studentName;
//
//        this.email = email;
//
//        this.phone = phone;
//
//        this.status = status;
//
//        this.admissionDate = admissionDate;
//    }
//
//
//    public int getStudentId() {
//        return studentId;
//    }
//
//    public String getStudentCode() {
//        return studentCode;
//    }
//
//    public String getStudentName() {
//        return studentName;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public String getPhone() {
//        return phone;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public Date getAdmissionDate() {
//        return admissionDate;
//    }
//}

import javafx.beans.property.*;

import java.time.LocalDate;



import javafx.beans.property.*;

import java.time.LocalDate;

public class StudentRecord {

    private final IntegerProperty studentId = new SimpleIntegerProperty();
    private final StringProperty studentCode = new SimpleStringProperty();
    private final StringProperty studentName = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final IntegerProperty age = new SimpleIntegerProperty();
    private final StringProperty gender = new SimpleStringProperty();
    private final StringProperty address = new SimpleStringProperty();
    private final StringProperty className = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> admissionDate = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty();

    public StudentRecord() {}

    // Getters and Setters
    public int getStudentId() { return studentId.get(); }
    public IntegerProperty studentIdProperty() { return studentId; }
    public void setStudentId(int studentId) { this.studentId.set(studentId); }

    public String getStudentCode() { return studentCode.get(); }
    public StringProperty studentCodeProperty() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode.set(studentCode); }

    public String getStudentName() { return studentName.get(); }
    public StringProperty studentNameProperty() { return studentName; }
    public void setStudentName(String studentName) { this.studentName.set(studentName); }

    public String getEmail() { return email.get(); }
    public StringProperty emailProperty() { return email; }
    public void setEmail(String email) { this.email.set(email); }

    public String getPhone() { return phone.get(); }
    public StringProperty phoneProperty() { return phone; }
    public void setPhone(String phone) { this.phone.set(phone); }

    public int getAge() { return age.get(); }
    public IntegerProperty ageProperty() { return age; }
    public void setAge(int age) { this.age.set(age); }

    public String getGender() { return gender.get(); }
    public StringProperty genderProperty() { return gender; }
    public void setGender(String gender) { this.gender.set(gender); }

    public String getAddress() { return address.get(); }
    public StringProperty addressProperty() { return address; }
    public void setAddress(String address) { this.address.set(address); }

    public String getClassName() { return className.get(); }
    public StringProperty classNameProperty() { return className; }
    public void setClassName(String className) { this.className.set(className); }

    public LocalDate getAdmissionDate() { return admissionDate.get(); }
    public ObjectProperty<LocalDate> admissionDateProperty() { return admissionDate; }
    public void setAdmissionDate(LocalDate admissionDate) { this.admissionDate.set(admissionDate); }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
    public void setStatus(String status) { this.status.set(status); }
}