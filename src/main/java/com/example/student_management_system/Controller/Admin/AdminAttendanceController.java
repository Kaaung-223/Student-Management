package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.AttendanceDAO;
import com.example.student_management_system.Controller.DAO.ClassDAO;
import com.example.student_management_system.Controller.DAO.LeaveDAO;
import com.example.student_management_system.Controller.DAO.StudentDao;
import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.DateRangeOption;
import com.example.student_management_system.Controller.Model.LeaveRequest;
import com.example.student_management_system.Controller.Model.Student;
import com.example.student_management_system.Controller.Model.StudentAttendanceRow;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AdminAttendanceController implements Initializable {

    private static final String PERIOD_OVERALL = "Overall";
    private static final String PERIOD_MONTH = "Month";
    private static final String PERIOD_SIX_MONTHS = "6 Months";
    private static final String PERIOD_YEAR = "Year";

    @FXML private Label lblPresent;
    @FXML private Label lblAbsent;
    @FXML private Label lblLate;
    @FXML private Label lblRate;
    @FXML private Label lblRequests;
    @FXML private Label lblExtraAbsent;
    @FXML private Label lblStudentCount;

    @FXML private ComboBox<Batch> cmbBatch;
    @FXML private ComboBox<Student> cmbStudent;
    @FXML private ComboBox<String> cmbPeriodType;
    @FXML private ComboBox<DateRangeOption> cmbPeriodValue;

    @FXML private TableView<StudentAttendanceRow> attendanceTable;
    @FXML private TableColumn<StudentAttendanceRow, String> colCode;
    @FXML private TableColumn<StudentAttendanceRow, String> colName;
    @FXML private TableColumn<StudentAttendanceRow, String> colBatch;
    @FXML private TableColumn<StudentAttendanceRow, String> colPresent;
    @FXML private TableColumn<StudentAttendanceRow, String> colAbsent;
    @FXML private TableColumn<StudentAttendanceRow, String> colLate;
    @FXML private TableColumn<StudentAttendanceRow, String> colRate;
    @FXML private TableColumn<StudentAttendanceRow, String> colRequests;
    @FXML private TableColumn<StudentAttendanceRow, String> colRequestedDays;
    @FXML private TableColumn<StudentAttendanceRow, String> colExtra;

    @FXML private TableView<LeaveRequest> leaveTable;
    @FXML private TableColumn<LeaveRequest, String> colLeaveCode;
    @FXML private TableColumn<LeaveRequest, String> colLeaveStudent;
    @FXML private TableColumn<LeaveRequest, String> colLeaveBatch;
    @FXML private TableColumn<LeaveRequest, String> colLeaveFrom;
    @FXML private TableColumn<LeaveRequest, String> colLeaveTo;
    @FXML private TableColumn<LeaveRequest, String> colLeaveDays;
    @FXML private TableColumn<LeaveRequest, String> colLeaveStatus;
    @FXML private TableColumn<LeaveRequest, String> colLeaveReason;

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final LeaveDAO leaveDAO = new LeaveDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final StudentDao studentDao = new StudentDao();

    private static final Batch ALL_BATCHES = new Batch(-1, "All Batches");
    private static final Student ALL_STUDENTS =
            new Student(-1, "", "All Students", "", "ACTIVE", -1, "");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private boolean loadingFilters;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBatchCombo();
        setupStudentCombo();
        setupPeriodCombos();
        setupAttendanceTable();
        setupLeaveTable();
        setupListeners();
        loadData();
    }

    private void setupBatchCombo() {
        List<Batch> batches = new ArrayList<>();
        batches.add(ALL_BATCHES);
        batches.addAll(classDAO.getAllBatches());
        cmbBatch.setItems(FXCollections.observableArrayList(batches));
        cmbBatch.getSelectionModel().select(ALL_BATCHES);
    }

    private void setupStudentCombo() {
        cmbStudent.setConverter(new StringConverter<>() {
            @Override
            public String toString(Student student) {
                if (student == null || student.getId() == -1) {
                    return "All Students";
                }
                String code = student.getStudentCode();
                if (code == null || code.isBlank()) {
                    return student.getStudentName();
                }
                return code + "  " + student.getStudentName();
            }

            @Override
            public Student fromString(String string) {
                return null;
            }
        });
        reloadStudents();
    }

    private void reloadStudents() {
        loadingFilters = true;
        int classId = selectedClassId();
        List<Student> students = new ArrayList<>();
        students.add(ALL_STUDENTS);
        students.addAll(studentDao.getStudentsByBatch(classId));
        cmbStudent.setItems(FXCollections.observableArrayList(students));
        cmbStudent.getSelectionModel().select(ALL_STUDENTS);
        loadingFilters = false;
    }

    private void setupPeriodCombos() {
        cmbPeriodType.setItems(FXCollections.observableArrayList(
                PERIOD_OVERALL,
                PERIOD_MONTH,
                PERIOD_SIX_MONTHS,
                PERIOD_YEAR
        ));
        cmbPeriodType.setValue(PERIOD_OVERALL);
        reloadPeriodValues();
    }

    private void reloadPeriodValues() {
        loadingFilters = true;
        String type = cmbPeriodType.getValue();
        List<DateRangeOption> values = new ArrayList<>();

        if (PERIOD_MONTH.equals(type)) {
            values.addAll(attendanceDAO.getMonthOptions());
        } else if (PERIOD_SIX_MONTHS.equals(type)) {
            values.addAll(attendanceDAO.getSixMonthOptions());
        } else if (PERIOD_YEAR.equals(type)) {
            values.addAll(attendanceDAO.getYearOptions());
        }

        cmbPeriodValue.setItems(FXCollections.observableArrayList(values));
        cmbPeriodValue.setDisable(PERIOD_OVERALL.equals(type) || values.isEmpty());
        if (!values.isEmpty()) {
            cmbPeriodValue.getSelectionModel().selectFirst();
        } else {
            cmbPeriodValue.setValue(null);
        }
        loadingFilters = false;
    }

    private void setupAttendanceTable() {
        colCode.setCellValueFactory(d -> new SimpleStringProperty(dash(d.getValue().getStudentCode())));
        colName.setCellValueFactory(d -> new SimpleStringProperty(dash(d.getValue().getStudentName())));
        colBatch.setCellValueFactory(d -> new SimpleStringProperty(dash(d.getValue().getClassName())));
        colPresent.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getPresentCount())));
        colAbsent.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getAbsentCount())));
        colLate.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getLateCount())));
        colRate.setCellValueFactory(d -> new SimpleStringProperty(formatRate(d.getValue())));
        colRequests.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getRequestCount())));
        colRequestedDays.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getRequestedDays())));
        colExtra.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getExtraAbsentDays())));
        colExtra.setCellFactory(column -> extraAbsentCell());
        attendanceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupLeaveTable() {
        colLeaveCode.setCellValueFactory(d -> new SimpleStringProperty(dash(d.getValue().getStudentCode())));
        colLeaveStudent.setCellValueFactory(d -> new SimpleStringProperty(dash(d.getValue().getStudentName())));
        colLeaveBatch.setCellValueFactory(d -> new SimpleStringProperty(dash(d.getValue().getBatchName())));
        colLeaveFrom.setCellValueFactory(d -> new SimpleStringProperty(formatDate(d.getValue().getLeaveFrom())));
        colLeaveTo.setCellValueFactory(d -> new SimpleStringProperty(formatDate(d.getValue().getLeaveTo())));
        colLeaveDays.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getLeaveDays())));
        colLeaveStatus.setCellValueFactory(d -> new SimpleStringProperty(dash(d.getValue().getStatus())));
        colLeaveStatus.setCellFactory(column -> leaveStatusCell());
        colLeaveReason.setCellValueFactory(d -> new SimpleStringProperty(dash(d.getValue().getReason())));
        leaveTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupListeners() {
        cmbBatch.valueProperty().addListener((obs, oldValue, newValue) -> {
            reloadStudents();
            loadData();
        });
        cmbStudent.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!loadingFilters) {
                loadData();
            }
        });
        cmbPeriodType.valueProperty().addListener((obs, oldValue, newValue) -> {
            reloadPeriodValues();
            loadData();
        });
        cmbPeriodValue.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!loadingFilters) {
                loadData();
            }
        });
    }

    private void loadData() {
        int classId = selectedClassId();
        int studentId = selectedStudentId();
        LocalDate from = null;
        LocalDate to = null;

        if (!PERIOD_OVERALL.equals(cmbPeriodType.getValue())) {
            DateRangeOption range = cmbPeriodValue.getValue();
            if (range != null) {
                from = range.getFrom();
                to = range.getTo();
            }
        }

        List<LeaveRequest> leaves = leaveDAO.getLeaveRequests(classId, studentId, from, to);
        leaveTable.setItems(FXCollections.observableArrayList(leaves));
        lblRequests.setText(String.valueOf(leaves.size()));

        List<StudentAttendanceRow> rows =
                attendanceDAO.getStudentAttendanceRows(classId, studentId, from, to, leaves);
        attendanceTable.setItems(FXCollections.observableArrayList(rows));
        lblStudentCount.setText(rows.size() + (rows.size() == 1 ? " student" : " students"));

        int present = 0;
        int absent = 0;
        int late = 0;
        int extra = 0;
        for (StudentAttendanceRow row : rows) {
            present += row.getPresentCount();
            absent += row.getAbsentCount();
            late += row.getLateCount();
            extra += row.getExtraAbsentDays();
        }

        lblPresent.setText(String.valueOf(present));
        lblAbsent.setText(String.valueOf(absent));
        lblLate.setText(String.valueOf(late));
        int total = present + absent + late;
        lblRate.setText(total == 0 ? "0.0" : String.format("%.1f", (present * 100.0) / total));
        lblExtraAbsent.setText(String.valueOf(extra));
    }

    private int selectedClassId() {
        Batch batch = cmbBatch.getValue();
        return batch == null ? -1 : batch.getId();
    }

    private int selectedStudentId() {
        Student student = cmbStudent.getValue();
        return student == null ? -1 : student.getId();
    }

    private TableCell<StudentAttendanceRow, String> extraAbsentCell() {
        return new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                badge.setText(item);
                boolean extra = !"0".equals(item);
                badge.setStyle(
                        extra
                                ? "-fx-background-color:#fee2e2;-fx-text-fill:#dc2626;-fx-font-size:10px;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:4 10;"
                                : "-fx-background-color:#eafaf0;-fx-text-fill:#15803d;-fx-font-size:10px;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:4 10;"
                );
                setGraphic(badge);
            }
        };
    }

    private TableCell<LeaveRequest, String> leaveStatusCell() {
        return new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                badge.setText(item);
                String upper = item.toUpperCase();
                if (upper.contains("APPROVED")) {
                    badge.setStyle("-fx-background-color:#eafaf0;-fx-text-fill:#15803d;-fx-font-size:10px;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:4 10;");
                } else if (upper.contains("REJECTED")) {
                    badge.setStyle("-fx-background-color:#fee2e2;-fx-text-fill:#dc2626;-fx-font-size:10px;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:4 10;");
                } else {
                    badge.setStyle("-fx-background-color:#fef3c7;-fx-text-fill:#b45309;-fx-font-size:10px;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:4 10;");
                }
                setGraphic(badge);
            }
        };
    }

    private String formatRate(StudentAttendanceRow row) {
        if (row.getRecordedDays() == 0) {
            return "-";
        }
        return String.format("%.1f", row.getAttendanceRate());
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : date.format(DATE_FMT);
    }

    private String dash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
