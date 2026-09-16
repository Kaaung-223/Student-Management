package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.PaymentRow;
import com.example.student_management_system.Controller.Model.PaymentStudent;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StaffPaymentsDAO {

    // ==================================================
    //  Batches
    // ==================================================
    public List<Batch> getAllBatches() {
        List<Batch> list = new ArrayList<>();
        String sql = "SELECT class_id, class_name FROM classes ORDER BY class_name";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new Batch(rs.getInt("class_id"), rs.getString("class_name")));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ==================================================
    //  Students with fee summary
    // ==================================================
    public List<PaymentStudent> findStudents(int batchId, String search, boolean onlyActive) {
        List<PaymentStudent> list = new ArrayList<>();

        String sql =
                "SELECT s.student_id, s.class_id, s.student_code, s.student_name, s.status, " +
                        "       c.class_name, " +
                        "  COALESCE(cf.total_fee, " +
                        "     (SELECT se.course_fee FROM student_enrollments se " +
                        "      WHERE se.student_id = s.student_id ORDER BY se.enrollment_id DESC LIMIT 1), 0) AS total_fee, " +
                        "  (SELECT COALESCE(SUM(sp.amount),0) FROM student_payments sp " +
                        "        WHERE sp.student_id = s.student_id) AS total_paid " +
                        "FROM students s " +
                        "LEFT JOIN classes c ON c.class_id = s.class_id " +
                        "LEFT JOIN class_fees cf ON cf.class_id = s.class_id " +
                        "WHERE (? = -1 OR s.class_id = ?) " +
                        "AND (? = 0 OR s.status = 'ACTIVE') " +
                        "AND (s.student_name LIKE ? OR s.student_code LIKE ?) " +
                        "ORDER BY s.student_name";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String like = "%" + (search == null ? "" : search.trim()) + "%";

            ps.setInt(1, batchId);
            ps.setInt(2, batchId);
            ps.setInt(3, onlyActive ? 1 : 0);
            ps.setString(4, like);
            ps.setString(5, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PaymentStudent s = new PaymentStudent();
                    s.setStudentId(rs.getInt("student_id"));
                    s.setClassId(rs.getInt("class_id"));
                    s.setStudentCode(rs.getString("student_code"));
                    s.setStudentName(rs.getString("student_name"));
                    s.setBatchName(rs.getString("class_name"));
                    s.setStatus(rs.getString("status"));

                    BigDecimal fee  = rs.getBigDecimal("total_fee");
                    BigDecimal paid = rs.getBigDecimal("total_paid");
                    s.setTotalFee(fee == null ? BigDecimal.ZERO : fee);
                    s.setTotalPaid(paid == null ? BigDecimal.ZERO : paid);

                    list.add(s);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ==================================================
    //  Payment history
    // ==================================================
    public List<PaymentRow> getPaymentHistory(int studentId) {
        List<PaymentRow> list = new ArrayList<>();

        String sql =
                "SELECT p.payment_id, p.student_id, s.student_code, s.student_name, " +
                        "       c.class_name, p.amount, p.payment_date, p.payment_method, " +
                        "       p.payment_period, p.receipt_no, p.note " +
                        "FROM student_payments p " +
                        "JOIN students s ON s.student_id = p.student_id " +
                        "LEFT JOIN classes c ON c.class_id = s.class_id " +
                        "WHERE p.student_id = ? " +
                        "ORDER BY p.payment_date DESC, p.payment_id DESC";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ==================================================
    //  Class fee id
    // ==================================================
    public Integer getClassFeeId(int classId) {
        String sql = "SELECT fee_id FROM class_fees WHERE class_id = ? ORDER BY fee_id DESC LIMIT 1";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ==================================================
    //  Receipt number
    // ==================================================
    public String generateReceiptNo() {
        String sql = "SELECT COUNT(*) FROM student_payments";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int next = rs.getInt(1) + 1;
                return String.format("RCPT-%05d", next);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "RCPT-" + System.currentTimeMillis();
    }

    // ==================================================
    //  Insert payment
    // ==================================================
    public int addPayment(int studentId, int feeId, String period,
                          BigDecimal amount, LocalDate date, String method,
                          String receiptNo, String note, int createdBy) {

        String sql =
                "INSERT INTO student_payments " +
                        "  (student_id, fee_id, payment_period, amount, payment_date, " +
                        "   payment_method, receipt_no, note, created_by) " +
                        "VALUES (?,?,?,?,?,?,?,?,?)";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, studentId);
            ps.setInt(2, feeId);
            ps.setString(3, period);
            ps.setBigDecimal(4, amount);
            ps.setDate(5, Date.valueOf(date));
            ps.setString(6, method);
            ps.setString(7, receiptNo);
            ps.setString(8, note);
            if (createdBy > 0) ps.setInt(9, createdBy);
            else               ps.setNull(9, Types.INTEGER);

            int rows = ps.executeUpdate();
            if (rows == 0) return -1;

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ==================================================
    //  Update student status
    // ==================================================
    public boolean updateStudentStatus(int studentId, String status) {
        String sql = "UPDATE students SET status = ? WHERE student_id = ?";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================================================
    private PaymentRow map(ResultSet rs) throws SQLException {
        PaymentRow r = new PaymentRow();
        r.setPaymentId(rs.getInt("payment_id"));
        r.setStudentId(rs.getInt("student_id"));
        r.setStudentCode(rs.getString("student_code"));
        r.setStudentName(rs.getString("student_name"));
        r.setBatchName(rs.getString("class_name"));
        r.setAmount(rs.getBigDecimal("amount"));

        Date d = rs.getDate("payment_date");
        if (d != null) r.setPaymentDate(d.toLocalDate());

        r.setMethod(rs.getString("payment_method"));
        r.setPeriod(rs.getString("payment_period"));
        r.setReceiptNo(rs.getString("receipt_no"));
        r.setNote(rs.getString("note"));
        return r;
    }
}