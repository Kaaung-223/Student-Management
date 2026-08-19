package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DataBase.AdminDashboardDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AdminDashboardController {

    @FXML private StackPane contentPane;
    @FXML private ScrollPane dashboardScrollPane;
    @FXML private AnchorPane adminPane;

    @FXML private Button btnDashboard;
    @FXML private Button btnStudents;
    @FXML private Button btnTeachers;
    @FXML private Button btnStaff;
    @FXML private Button btnClasses;
    @FXML private Button btnSubjects;
    @FXML private Button btnExams;
    @FXML private Button btnGrades;
    @FXML private Button btnAttendance;
    @FXML private Button btnLeave;
    @FXML private Button btnAnnouncements;
    @FXML private Button btnProfile;

    @FXML private Label lblAdminName;
    @FXML private Label lblTotalStudents;
    @FXML private Label lblTotalTeachers;
    @FXML private Label lblTotalClasses;
    @FXML private Label lblTotalSubjects;

    // Charts
    @FXML private ComboBox<String> attendancePeriodCombo;
    @FXML private BarChart<String, Number> attendanceChart;
    @FXML private ComboBox<String> examCombo;
    @FXML private PieChart examChart;

    // Batch students
    @FXML private ComboBox<String> batchCombo;
    @FXML private TableView<AdminDashboardDAO.Student> studentTable;
    @FXML private TableColumn<AdminDashboardDAO.Student, String> studentCodeColumn;
    @FXML private TableColumn<AdminDashboardDAO.Student, String> studentNameColumn;
    @FXML private TableColumn<AdminDashboardDAO.Student, String> studentEmailColumn;
    @FXML private TableColumn<AdminDashboardDAO.Student, String> studentStatusColumn;

    // Leave requests
    @FXML private DatePicker leaveDatePicker;
    @FXML private TableView<AdminDashboardDAO.LeaveRequest> leaveTable;
    @FXML private TableColumn<AdminDashboardDAO.LeaveRequest, String> leaveStudentColumn;
    @FXML private TableColumn<AdminDashboardDAO.LeaveRequest, String> leaveFromColumn;
    @FXML private TableColumn<AdminDashboardDAO.LeaveRequest, String> leaveToColumn;
    @FXML private TableColumn<AdminDashboardDAO.LeaveRequest, String> leaveStatusColumn;

    private final AdminDashboardDAO dashboardDAO = new AdminDashboardDAO();

    private final String ACTIVE_STYLE =
            "-fx-background-color: #2d4b6e; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding: 0 14; -fx-background-radius: 8; -fx-cursor: hand;";
    private final String NORMAL_STYLE =
            "-fx-background-color: transparent; -fx-text-fill: #a8b9d0; -fx-font-size: 13px; -fx-alignment: CENTER_LEFT; -fx-padding: 0 14; -fx-background-radius: 8; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        setActiveButton(btnDashboard);

        // Attendance period combo
        attendancePeriodCombo.setItems(FXCollections.observableArrayList("Today", "This Month", "This Year"));
        attendancePeriodCombo.getSelectionModel().selectFirst();
        attendancePeriodCombo.setOnAction(e -> updateAttendanceChart());

        // Exams combo
        loadExams();
        examCombo.setOnAction(e -> updateExamChart());

        // Batch students
        loadClasses();
        batchCombo.setOnAction(e -> loadBatchStudents());

        // Leave date picker
        leaveDatePicker.setValue(LocalDate.now());
        leaveDatePicker.setOnAction(e -> loadPendingLeaves());

        // Load initial data
        loadDashboard();
        updateAttendanceChart();
        updateExamChart();
        loadBatchStudents();
        loadPendingLeaves();
    }

    // =========================================================
    // DASHBOARD STATISTICS
    // =========================================================

    @FXML
    public void refreshDashboard(ActionEvent event) {
        setActiveButton(btnDashboard);
        dashboardScrollPane.setVisible(true);
        dashboardScrollPane.setManaged(true);
        adminPane.setVisible(false);
        adminPane.setManaged(false);
        loadDashboard();
        updateAttendanceChart();
        updateExamChart();
        loadBatchStudents();
        loadPendingLeaves();
    }

    private void loadDashboard() {
        lblAdminName.setText(dashboardDAO.getAdminName());
        lblTotalStudents.setText(String.valueOf(dashboardDAO.getTotalStudents()));
        lblTotalTeachers.setText(String.valueOf(dashboardDAO.getTotalTeachers()));
        lblTotalClasses.setText(String.valueOf(dashboardDAO.getTotalClasses()));
        lblTotalSubjects.setText(String.valueOf(dashboardDAO.getTotalSubjects()));
    }

    // =========================================================
    // ATTENDANCE CHART
    // =========================================================

    private void updateAttendanceChart() {
        String period = attendancePeriodCombo.getValue();
        Map<String, Integer> counts = dashboardDAO.getAttendanceCounts(period);

        attendanceChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Attendance");

        series.getData().add(new XYChart.Data<>("Present", counts.getOrDefault("Present", 0)));
        series.getData().add(new XYChart.Data<>("Absent", counts.getOrDefault("Absent", 0)));
        series.getData().add(new XYChart.Data<>("Late", counts.getOrDefault("Late", 0)));

        attendanceChart.getData().add(series);
    }

    // =========================================================
    // EXAM CHART
    // =========================================================

    private void loadExams() {
        List<String> examNames = dashboardDAO.getExamNames();
        examCombo.setItems(FXCollections.observableArrayList(examNames));
        if (!examNames.isEmpty()) {
            examCombo.getSelectionModel().selectFirst();
        }
    }

    private void updateExamChart() {
        String selectedExam = examCombo.getValue();
        if (selectedExam == null) return;

        int examId = dashboardDAO.getExamIdByName(selectedExam);
        if (examId == -1) return;

        Map<String, Integer> results = dashboardDAO.getExamPassFailCounts(examId);
        int pass = results.getOrDefault("PASS", 0);
        int fail = results.getOrDefault("FAIL", 0);

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Pass", pass),
                new PieChart.Data("Fail", fail)
        );
        examChart.setData(pieData);
    }

    // =========================================================
    // BATCH STUDENTS
    // =========================================================

    private void loadClasses() {
        List<String> classNames = dashboardDAO.getClassNames();
        batchCombo.setItems(FXCollections.observableArrayList(classNames));
        if (!classNames.isEmpty()) {
            batchCombo.getSelectionModel().selectFirst();
        }
    }

    private void loadBatchStudents() {
        String selectedClass = batchCombo.getValue();
        if (selectedClass == null) return;

        int classId = dashboardDAO.getClassIdByName(selectedClass);
        if (classId == -1) return;

        List<AdminDashboardDAO.Student> students = dashboardDAO.getStudentsByClassId(classId);
        ObservableList<AdminDashboardDAO.Student> data = FXCollections.observableArrayList(students);

        // Set up columns (assuming Student class has properties)
        studentCodeColumn.setCellValueFactory(cellData -> cellData.getValue().studentCodeProperty());
        studentNameColumn.setCellValueFactory(cellData -> cellData.getValue().studentNameProperty());
        studentEmailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        studentStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        studentTable.setItems(data);
    }

    // =========================================================
    // PENDING LEAVE REQUESTS
    // =========================================================

    private void loadPendingLeaves() {
        LocalDate selectedDate = leaveDatePicker.getValue();
        if (selectedDate == null) return;

        List<AdminDashboardDAO.LeaveRequest> leaves = dashboardDAO.getPendingLeavesByDate(selectedDate);
        ObservableList<AdminDashboardDAO.LeaveRequest> data = FXCollections.observableArrayList(leaves);

        // Set up columns
        leaveStudentColumn.setCellValueFactory(cellData -> cellData.getValue().studentNameProperty());
        leaveFromColumn.setCellValueFactory(cellData -> cellData.getValue().leaveFromProperty());
        leaveToColumn.setCellValueFactory(cellData -> cellData.getValue().leaveToProperty());
        leaveStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        leaveTable.setItems(data);
    }

    // =========================================================
    // PAGE NAVIGATION (unchanged)
    // =========================================================

    @FXML public void openStudents(ActionEvent event) { setActiveButton(btnStudents); loadPage("/com/example/student_management_system/View/Admin/Students.fxml"); }
    @FXML public void openTeachers(ActionEvent event) { setActiveButton(btnTeachers); loadPage("/com/example/student_management_system/View/Admin/Teachers.fxml"); }
    @FXML public void openStaff(ActionEvent event) { setActiveButton(btnStaff); loadPage("/com/example/student_management_system/View/Admin/Staff.fxml"); }
    @FXML public void openClasses(ActionEvent event) { setActiveButton(btnClasses); loadPage("/com/example/student_management_system/View/Admin/Classes.fxml"); }
    @FXML public void openSubjects(ActionEvent event) { setActiveButton(btnSubjects); loadPage("/com/example/student_management_system/View/Admin/Subjects.fxml"); }
    @FXML public void openExams(ActionEvent event) { setActiveButton(btnExams); loadPage("/com/example/student_management_system/View/Admin/Exams.fxml"); }
    @FXML public void openGrades(ActionEvent event) { setActiveButton(btnGrades); loadPage("/com/example/student_management_system/View/Admin/Grades.fxml"); }
    @FXML public void openAttendance(ActionEvent event) { setActiveButton(btnAttendance); loadPage("/com/example/student_management_system/View/Admin/Attendance.fxml"); }
    @FXML public void openLeaveRequests(ActionEvent event) { setActiveButton(btnLeave); loadPage("/com/example/student_management_system/View/Admin/LeaveRequests.fxml"); }
    @FXML public void openAnnouncements(ActionEvent event) { setActiveButton(btnAnnouncements); loadPage("/com/example/student_management_system/View/Admin/Announcements.fxml"); }
    @FXML public void openProfile(ActionEvent event) { setActiveButton(btnProfile); loadPage("/com/example/student_management_system/View/Admin/Profile.fxml"); }

    private void loadPage(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent page = loader.load();
            adminPane.getChildren().clear();
            adminPane.getChildren().add(page);
            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);
            dashboardScrollPane.setVisible(false);
            dashboardScrollPane.setManaged(false);
            adminPane.setVisible(true);
            adminPane.setManaged(true);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Page Error");
            alert.setHeaderText("Cannot load page");
            alert.setContentText("FXML file not found:\n" + path);
            alert.showAndWait();
        }
    }

    private void setActiveButton(Button activeButton) {
        Button[] buttons = {btnDashboard, btnStudents, btnTeachers, btnStaff, btnClasses,
                btnSubjects, btnExams, btnGrades, btnAttendance, btnLeave, btnAnnouncements, btnProfile};
        for (Button button : buttons) {
            if (button != null) button.setStyle(NORMAL_STYLE);
        }
        if (activeButton != null) activeButton.setStyle(ACTIVE_STYLE);
    }

    @FXML
    public void logout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Logout Confirmation");
        alert.setContentText("Are you sure you want to logout?");
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/com/example/student_management_system/View/Login.fxml"));
                    javafx.stage.Stage stage = (javafx.stage.Stage) btnDashboard.getScene().getWindow();
                    stage.setScene(new javafx.scene.Scene(root));
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Inner classes for data (if needed) – define them or use existing models.
    // For simplicity, we assume Student and LeaveRequest classes exist with properties.
}