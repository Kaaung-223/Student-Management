package com.example.student_management_system.Controller.Model;

import java.math.BigDecimal;

public class StaffInfo {

    private int staffId;
    private int userId;
    private String username;
    private String fullName;     // users.full_name
    private String staffName;    // staff.staff_name
    private String staffCode;
    private String email;
    private String phone;
    private String gender;
    private String address;
    private String photoPath;
    private BigDecimal salary;

    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public String getStaffCode() { return staffCode; }
    public void setStaffCode(String staffCode) { this.staffCode = staffCode; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    /** Best-effort display name. */
    public String getDisplayName() {
        if (fullName != null && !fullName.isBlank())       return fullName;
        if (staffName != null && !staffName.isBlank())     return staffName;
        return username != null ? username : "Staff";
    }
}