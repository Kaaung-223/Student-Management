package com.example.student_management_system.Controller.DAO;

import com.example.student_management_system.Controller.Model.AttendanceSummary;
import com.example.student_management_system.Controller.Model.DateRangeOption;
import com.example.student_management_system.Controller.Model.LeaveRequest;
import com.example.student_management_system.Controller.Model.StudentAttendanceRow;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Matches: attendance(attendance_id, student_id, teacher_id, attendance_date, status, remarks)
 *          status ENUM('Present','Absent','Late')
 *
 * attendance has no class_id of its own, so filtering by batch means joining
 * through students.class_id.
 *
 * period is one of: "This Week", "This Month", "This Year", "All Time"
 * (matches whatever you put in cmbAttendancePeriod's items). batchId = -1 means all batches.
 */
public class AttendanceDAO {

    public AttendanceSummary getAttendanceSummary(String period, int batchId) {
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "  SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) AS present_count, " +
                        "  SUM(CASE WHEN a.status = 'Absent'  THEN 1 ELSE 0 END) AS absent_count, " +
                        "  SUM(CASE WHEN a.status = 'Late'    THEN 1 ELSE 0 END) AS late_count " +
                        "FROM attendance a " +
                        "JOIN students s ON a.student_id = s.student_id " +
                        "WHERE 1=1 ");

        if (period != null) {
            switch (period) {
                case "This Week":
                    sql.append("AND a.attendance_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) ");
                    break;
                case "This Month":
                    sql.append("AND MONTH(a.attendance_date) = MONTH(CURDATE()) AND YEAR(a.attendance_date) = YEAR(CURDATE()) ");
                    break;
                case "This Year":
                    sql.append("AND YEAR(a.attendance_date) = YEAR(CURDATE()) ");
                    break;
                default: // "All Time" or null -> no extra filter
                    break;
            }
        }
        if (batchId != -1) {
            sql.append("AND s.class_id = ? ");
        }

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            if (batchId != -1) {
                ps.setInt(1, batchId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AttendanceSummary(
                            rs.getInt("present_count"),
                            rs.getInt("absent_count"),
                            rs.getInt("late_count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new AttendanceSummary(0, 0, 0);
    }

    public List<DateRangeOption> getMonthOptions() {
        List<DateRangeOption> options = new ArrayList<>();
        String sql =
                "SELECT DISTINCT YEAR(d) AS y, MONTH(d) AS m " +
                        "FROM ( " +
                        "  SELECT attendance_date AS d FROM attendance " +
                        "  UNION " +
                        "  SELECT leave_from FROM leave_requests " +
                        "  UNION " +
                        "  SELECT leave_to FROM leave_requests " +
                        ") dates " +
                        "WHERE d IS NOT NULL " +
                        "ORDER BY y DESC, m DESC";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int year = rs.getInt("y");
                int month = rs.getInt("m");
                YearMonth ym = YearMonth.of(year, month);
                options.add(new DateRangeOption(
                        ym.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        ym.atDay(1),
                        ym.atEndOfMonth()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (options.isEmpty()) {
            YearMonth now = YearMonth.now();
            options.add(new DateRangeOption(
                    now.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    now.atDay(1),
                    now.atEndOfMonth()
            ));
        }
        return options;
    }

    public List<DateRangeOption> getSixMonthOptions() {
        List<DateRangeOption> options = new ArrayList<>();
        LocalDate today = LocalDate.now();
        options.add(new DateRangeOption(
                "Last 6 months",
                today.minusMonths(6),
                today
        ));

        int currentYear = today.getYear();
        for (int year = currentYear; year >= currentYear - 3; year--) {
            options.add(new DateRangeOption(
                    "Jan - Jun " + year,
                    LocalDate.of(year, Month.JANUARY, 1),
                    LocalDate.of(year, Month.JUNE, 30)
            ));
            options.add(new DateRangeOption(
                    "Jul - Dec " + year,
                    LocalDate.of(year, Month.JULY, 1),
                    LocalDate.of(year, Month.DECEMBER, 31)
            ));
        }
        return options;
    }

    public List<DateRangeOption> getYearOptions() {
        List<DateRangeOption> options = new ArrayList<>();
        String sql =
                "SELECT DISTINCT YEAR(d) AS y " +
                        "FROM ( " +
                        "  SELECT attendance_date AS d FROM attendance " +
                        "  UNION " +
                        "  SELECT leave_from FROM leave_requests " +
                        "  UNION " +
                        "  SELECT leave_to FROM leave_requests " +
                        ") dates " +
                        "WHERE d IS NOT NULL " +
                        "ORDER BY y DESC";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int year = rs.getInt("y");
                options.add(new DateRangeOption(
                        String.valueOf(year),
                        LocalDate.of(year, 1, 1),
                        LocalDate.of(year, 12, 31)
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (options.isEmpty()) {
            int year = LocalDate.now().getYear();
            options.add(new DateRangeOption(
                    String.valueOf(year),
                    LocalDate.of(year, 1, 1),
                    LocalDate.of(year, 12, 31)
            ));
        }
        return options;
    }

    public List<StudentAttendanceRow> getStudentAttendanceRows(
            int classId,
            int studentId,
            LocalDate from,
            LocalDate to,
            List<LeaveRequest> leaveRequests
    ) {
        Map<Integer, int[]> leaveByStudent = aggregateLeaveDays(leaveRequests, from, to);
        List<StudentAttendanceRow> rows = new ArrayList<>();

        String sql =
                "SELECT s.student_id, s.student_code, s.student_name, " +
                        "COALESCE(c.class_name, 'Not assigned') AS class_name, " +
                        "COALESCE(SUM(a.status = 'Present'), 0) AS present_count, " +
                        "COALESCE(SUM(a.status = 'Absent'), 0) AS absent_count, " +
                        "COALESCE(SUM(a.status = 'Late'), 0) AS late_count " +
                        "FROM students s " +
                        "LEFT JOIN classes c ON c.class_id = s.class_id " +
                        "LEFT JOIN attendance a ON a.student_id = s.student_id " +
                        "AND (? IS NULL OR a.attendance_date >= ?) " +
                        "AND (? IS NULL OR a.attendance_date <= ?) " +
                        "WHERE (? = -1 OR s.class_id = ?) " +
                        "AND (? = -1 OR s.student_id = ?) " +
                        "GROUP BY s.student_id, s.student_code, s.student_name, c.class_name " +
                        "ORDER BY c.class_name, s.student_name";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            setNullableDate(ps, 1, from);
            setNullableDate(ps, 2, from);
            setNullableDate(ps, 3, to);
            setNullableDate(ps, 4, to);
            ps.setInt(5, classId);
            ps.setInt(6, classId);
            ps.setInt(7, studentId);
            ps.setInt(8, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("student_id");
                    int absent = rs.getInt("absent_count");
                    int[] leave = leaveByStudent.getOrDefault(id, new int[]{0, 0});
                    int requestCount = leave[0];
                    int requestedDays = leave[1];
                    int extraAbsent = Math.max(0, absent - requestedDays);

                    rows.add(new StudentAttendanceRow(
                            id,
                            rs.getString("student_code"),
                            rs.getString("student_name"),
                            rs.getString("class_name"),
                            rs.getInt("present_count"),
                            absent,
                            rs.getInt("late_count"),
                            requestCount,
                            requestedDays,
                            extraAbsent
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    private Map<Integer, int[]> aggregateLeaveDays(
            List<LeaveRequest> leaveRequests,
            LocalDate from,
            LocalDate to
    ) {
        Map<Integer, int[]> map = new HashMap<>();
        if (leaveRequests == null) {
            return map;
        }
        for (LeaveRequest leave : leaveRequests) {
            if (leave.getStudentId() <= 0) {
                continue;
            }
            int days = 0;
            if (!"Rejected".equalsIgnoreCase(leave.getStatus())) {
                days = overlappingDays(leave.getLeaveFrom(), leave.getLeaveTo(), from, to);
            }
            int[] values = map.computeIfAbsent(leave.getStudentId(), k -> new int[]{0, 0});
            values[0] += 1;
            values[1] += days;
        }
        return map;
    }

    private static int overlappingDays(LocalDate leaveFrom, LocalDate leaveTo, LocalDate from, LocalDate to) {
        if (leaveFrom == null || leaveTo == null) {
            return 0;
        }
        LocalDate start = from == null ? leaveFrom : leaveFrom.isAfter(from) ? leaveFrom : from;
        LocalDate end = to == null ? leaveTo : leaveTo.isBefore(to) ? leaveTo : to;
        if (end.isBefore(start)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(start, end) + 1;
    }

    private static void setNullableDate(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date == null) {
            ps.setNull(index, java.sql.Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(date));
        }
    }
}
