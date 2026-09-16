package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.Staff;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    // ==================================================
    //  SEARCH
    // ==================================================
    public List<Staff> searchStaff(String query) {
        List<Staff> list = new ArrayList<>();

        String sql =
                "SELECT s.staff_id, s.user_id, s.staff_code, s.staff_name, " +
                        "       s.email, s.phone, s.gender, s.address, s.photo_path, s.salary, s.hire_date, " +
                        "       u.username, u.status " +
                        "FROM staff s " +
                        "JOIN users u ON u.user_id = s.user_id ";

        boolean hasQuery = query != null && !query.isBlank();
        if (hasQuery) {
            sql += "WHERE s.staff_name LIKE ? OR s.staff_code LIKE ? " +
                    "   OR s.email LIKE ? OR s.phone LIKE ? OR u.username LIKE ? ";
        }
        sql += "ORDER BY s.staff_name";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (hasQuery) {
                String like = "%" + query.trim() + "%";
                for (int i = 1; i <= 5; i++) ps.setString(i, like);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ==================================================
    //  LOAD ONE
    // ==================================================
    public Staff getStaffById(int staffId) {
        String sql =
                "SELECT s.staff_id, s.user_id, s.staff_code, s.staff_name, " +
                        "       s.email, s.phone, s.gender, s.address, s.photo_path, s.salary, s.hire_date, " +
                        "       u.username, u.status " +
                        "FROM staff s JOIN users u ON u.user_id = s.user_id " +
                        "WHERE s.staff_id = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ==================================================
    //  UNIQUENESS CHECKS
    // ==================================================
    public boolean isUsernameTaken(String username, int excludeUserId) {
        String sql = "SELECT 1 FROM users WHERE username = ? AND user_id != ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, excludeUserId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean isStaffCodeTaken(String code, int excludeStaffId) {
        String sql = "SELECT 1 FROM staff WHERE staff_code = ? AND staff_id != ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, excludeStaffId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean isEmailTaken(String email, int excludeStaffId) {
        if (email == null || email.isBlank()) return false;
        String sql = "SELECT 1 FROM staff WHERE email = ? AND staff_id != ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, excludeStaffId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ==================================================
    //  ADD
    // ==================================================
    public int addStaff(String fullName, String username, String password,
                        String staffCode, String email, String phone,
                        String gender, String address, String photoPath,
                        BigDecimal salary, LocalDate hireDate) {

        String userSql  = "INSERT INTO users (full_name, username, password, role) VALUES (?, ?, ?, 'STAFF')";
        String staffSql = "INSERT INTO staff (user_id, staff_code, staff_name, email, phone, " +
                "                   gender, address, photo_path, salary, hire_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnention.getConnection()) {
            con.setAutoCommit(false);

            int newUserId;
            try (PreparedStatement ps = con.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, fullName);
                ps.setString(2, username);
                ps.setString(3, password);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) { con.rollback(); return -1; }
                    newUserId = rs.getInt(1);
                }
            }

            int newStaffId;
            try (PreparedStatement ps = con.prepareStatement(staffSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, newUserId);
                ps.setString(2, staffCode);
                ps.setString(3, fullName);
                ps.setString(4, email);
                ps.setString(5, phone);
                ps.setString(6, gender);
                ps.setString(7, address);
                ps.setString(8, photoPath);
                if (salary != null) ps.setBigDecimal(9, salary);
                else                ps.setNull(9, Types.DECIMAL);
                if (hireDate != null) ps.setDate(10, Date.valueOf(hireDate));
                else                  ps.setNull(10, Types.DATE);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) { con.rollback(); return -1; }
                    newStaffId = rs.getInt(1);
                }
            }

            con.commit();
            return newStaffId;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // ==================================================
    //  UPDATE
    //  password may be null to keep existing
    // ==================================================
    public boolean updateStaff(int staffId, int userId,
                               String fullName, String username, String password,
                               String staffCode, String email, String phone,
                               String gender, String address, String photoPath,
                               BigDecimal salary, LocalDate hireDate) {

        String userSql, staffSql;

        if (password != null && !password.isBlank()) {
            userSql = "UPDATE users SET full_name=?, username=?, password=? WHERE user_id=?";
        } else {
            userSql = "UPDATE users SET full_name=?, username=? WHERE user_id=?";
        }

        staffSql = "UPDATE staff SET staff_code=?, staff_name=?, email=?, phone=?, " +
                "       gender=?, address=?, photo_path=?, salary=?, hire_date=? " +
                "WHERE staff_id=?";

        try (Connection con = DBConnention.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(userSql)) {
                ps.setString(1, fullName);
                ps.setString(2, username);
                int idx = 3;
                if (password != null && !password.isBlank()) ps.setString(idx++, password);
                ps.setInt(idx, userId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(staffSql)) {
                ps.setString(1, staffCode);
                ps.setString(2, fullName);
                ps.setString(3, email);
                ps.setString(4, phone);
                ps.setString(5, gender);
                ps.setString(6, address);
                ps.setString(7, photoPath);
                if (salary != null) ps.setBigDecimal(8, salary);
                else                ps.setNull(8, Types.DECIMAL);
                if (hireDate != null) ps.setDate(9, Date.valueOf(hireDate));
                else                  ps.setNull(9, Types.DATE);
                ps.setInt(10, staffId);
                ps.executeUpdate();
            }

            con.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================================================
    //  DELETE
    // ==================================================
    public boolean deleteStaff(int staffId, int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================================================
    private Staff map(ResultSet rs) throws SQLException {
        Staff s = new Staff();
        s.setId(rs.getInt("staff_id"));
        s.setUserId(rs.getInt("user_id"));
        s.setStaffCode(rs.getString("staff_code"));
        s.setStaffName(rs.getString("staff_name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setGender(rs.getString("gender"));
        s.setAddress(rs.getString("address"));
        s.setPhotoPath(rs.getString("photo_path"));

        BigDecimal sal = rs.getBigDecimal("salary");
        s.setSalary(sal);

        Date d = rs.getDate("hire_date");
        if (d != null) s.setHireDate(d.toLocalDate());

        s.setUsername(rs.getString("username"));
        s.setStatus(rs.getString("status"));
        return s;
    }
}