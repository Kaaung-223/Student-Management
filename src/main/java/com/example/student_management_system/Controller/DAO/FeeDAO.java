package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.FeeFinancialSummary;
import com.example.student_management_system.Controller.Model.StudentFeeRow;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FeeDAO {

    private static final String STUDENT_FEE_SQL =
            "SELECT " +
                    "s.student_id, " +
                    "s.student_code, " +
                    "s.student_name, " +
                    "c.class_id, " +
                    "c.class_name, " +
                    "COALESCE(c.course_fee, 0) AS class_fee, " +
                    "COALESCE(paid.total_paid, 0) AS amount_paid, " +
                    "GREATEST(COALESCE(c.course_fee, 0) - COALESCE(paid.total_paid, 0), 0) AS balance_due, " +
                    "CASE " +
                    "WHEN COALESCE(c.course_fee, 0) <= 0 THEN 'NO FEE' " +
                    "WHEN COALESCE(paid.total_paid, 0) >= COALESCE(c.course_fee, 0) THEN 'PAID' " +
                    "WHEN COALESCE(paid.total_paid, 0) > 0 THEN 'PARTIAL' " +
                    "WHEN EXISTS ( " +
                    "SELECT 1 " +
                    "FROM student_enrollments se " +
                    "JOIN fee_installments fi ON fi.enrollment_id = se.enrollment_id " +
                    "WHERE se.student_id = s.student_id " +
                    "AND fi.payment_status = 'OVERDUE' " +
                    ") THEN 'OVERDUE' " +
                    "ELSE 'UNPAID' " +
                    "END AS payment_status " +
                    "FROM students s " +
                    "LEFT JOIN classes c ON s.class_id = c.class_id " +
                    "LEFT JOIN ( " +
                    "SELECT student_id, SUM(amount) AS total_paid " +
                    "FROM student_payments " +
                    "GROUP BY student_id " +
                    ") paid ON paid.student_id = s.student_id " +
                    "WHERE s.status = 'ACTIVE' ";

    public List<StudentFeeRow> getStudentFeeRows(
            int classId,
            String statusFilter,
            String searchQuery
    ) {

        List<StudentFeeRow> rows = new ArrayList<>();

        String sql = STUDENT_FEE_SQL;
        List<Object> params = new ArrayList<>();

        if (classId != -1) {
            sql += "AND c.class_id = ? ";
            params.add(classId);
        }

        if (
                statusFilter != null &&
                        !statusFilter.trim().isEmpty() &&
                        !"ALL".equalsIgnoreCase(statusFilter)
        ) {
            sql += "AND (CASE " +
                    "WHEN COALESCE(c.course_fee, 0) <= 0 THEN 'NO FEE' " +
                    "WHEN COALESCE(paid.total_paid, 0) >= COALESCE(c.course_fee, 0) THEN 'PAID' " +
                    "WHEN COALESCE(paid.total_paid, 0) > 0 THEN 'PARTIAL' " +
                    "WHEN EXISTS ( " +
                    "SELECT 1 " +
                    "FROM student_enrollments se " +
                    "JOIN fee_installments fi ON fi.enrollment_id = se.enrollment_id " +
                    "WHERE se.student_id = s.student_id " +
                    "AND fi.payment_status = 'OVERDUE' " +
                    ") THEN 'OVERDUE' " +
                    "ELSE 'UNPAID' " +
                    "END) = ? ";
            params.add(statusFilter.toUpperCase());
        }

        if (
                searchQuery != null &&
                        !searchQuery.trim().isEmpty()
        ) {
            sql += "AND (s.student_name LIKE ? " +
                    "OR s.student_code LIKE ? " +
                    "OR c.class_name LIKE ?) ";
            String search = "%" + searchQuery.trim() + "%";
            params.add(search);
            params.add(search);
            params.add(search);
        }

        sql += "ORDER BY c.class_name, s.student_name";

        try (Connection con = DBConnention.getConnection()) {

            if (con == null) {
                return rows;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        rows.add(mapStudentFeeRow(rs));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rows;
    }

    public FeeFinancialSummary getFinancialSummary(int classId) {

        List<StudentFeeRow> rows =
                getStudentFeeRows(classId, "ALL", null);

        int totalStudents = rows.size();
        int paidStudents = 0;
        int unpaidStudents = 0;
        int partialStudents = 0;

        BigDecimal totalExpected = BigDecimal.ZERO;
        BigDecimal totalCollected = BigDecimal.ZERO;
        BigDecimal totalOutstanding = BigDecimal.ZERO;

        for (StudentFeeRow row : rows) {

            BigDecimal classFee =
                    row.getClassFee() == null
                            ? BigDecimal.ZERO
                            : row.getClassFee();
            BigDecimal amountPaid =
                    row.getAmountPaid() == null
                            ? BigDecimal.ZERO
                            : row.getAmountPaid();
            BigDecimal balanceDue =
                    row.getBalanceDue() == null
                            ? BigDecimal.ZERO
                            : row.getBalanceDue();

            totalExpected = totalExpected.add(classFee);
            totalCollected = totalCollected.add(amountPaid);
            totalOutstanding = totalOutstanding.add(balanceDue);

            String status = row.getPaymentStatus();

            if ("PAID".equalsIgnoreCase(status)) {
                paidStudents++;
            } else if ("PARTIAL".equalsIgnoreCase(status)) {
                partialStudents++;
            } else if (
                    "UNPAID".equalsIgnoreCase(status) ||
                            "OVERDUE".equalsIgnoreCase(status)
            ) {
                unpaidStudents++;
            }
        }

        return new FeeFinancialSummary(
                totalStudents,
                paidStudents,
                unpaidStudents,
                partialStudents,
                totalExpected,
                totalCollected,
                totalOutstanding
        );
    }

    private StudentFeeRow mapStudentFeeRow(ResultSet rs)
            throws SQLException {

        return new StudentFeeRow(
                rs.getInt("student_id"),
                rs.getString("student_code"),
                rs.getString("student_name"),
                rs.getInt("class_id"),
                rs.getString("class_name"),
                rs.getBigDecimal("class_fee"),
                rs.getBigDecimal("amount_paid"),
                rs.getBigDecimal("balance_due"),
                rs.getString("payment_status")
        );
    }
}
