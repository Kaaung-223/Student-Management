package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DataBase.AdminDashboardDAO;
import com.example.student_management_system.Controller.DataBase.AdminDashboardDAO.AttendanceData;
import com.example.student_management_system.Controller.DataBase.AdminDashboardDAO.ExamResultData;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Node;

import javafx.scene.chart.PieChart;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDashboardController {

    // =========================================================
    // DAO
    // =========================================================

    private final AdminDashboardDAO dashboardDAO =
            new AdminDashboardDAO();


    // =========================================================
    // MAIN
    // =========================================================

    @FXML
    private StackPane contentPane;

    @FXML
    private AnchorPane adminPane;


    // =========================================================
    // SIDEBAR BUTTONS
    // =========================================================

    @FXML private Button btnDashboard;
    @FXML private Button btnStudents;
    @FXML private Button btnTeachers;
    @FXML private Button btnClasses;
    @FXML private Button btnSubjects;
    @FXML private Button btnExams;
    @FXML private Button btnGrades;
    @FXML private Button btnAttendance;
    @FXML private Button btnLeave;
    @FXML private Button btnAnnouncements;
    @FXML private Button btnProfile;
    @FXML private Button btnLogout;


    // =========================================================
    // DASHBOARD LABELS
    // =========================================================

    @FXML private Label lblAdminName;

    @FXML private Label lblTotalStudents;
    @FXML private Label lblTotalTeachers;
    @FXML private Label lblTotalClasses;
    @FXML private Label lblTotalSubjects;


    // =========================================================
    // ATTENDANCE
    // =========================================================

    @FXML
    private PieChart attendancePieChart;

    @FXML
    private ComboBox<String> cmbAttendancePeriod;

    @FXML
    private ComboBox<ClassItem> cmbAttendanceBatch;


    // =========================================================
    // EXAM
    // =========================================================

    @FXML
    private PieChart examPieChart;

    @FXML
    private ComboBox<ExamItem> cmbExam;


    // =========================================================
    // TABLES
    // =========================================================

    @FXML private TableView<?> studentTable;

    @FXML private TableColumn<?, ?> studentCodeColumn;
    @FXML private TableColumn<?, ?> studentNameColumn;
    @FXML private TableColumn<?, ?> studentEmailColumn;
    @FXML private TableColumn<?, ?> studentStatusColumn;

    @FXML private TableView<?> leaveTable;

    @FXML private TableColumn<?, ?> leaveStudentColumn;
    @FXML private TableColumn<?, ?> leaveFromColumn;
    @FXML private TableColumn<?, ?> leaveToColumn;
    @FXML private TableColumn<?, ?> leaveStatusColumn;


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        loadAdminName();

        loadStatistics();

        setupAttendanceFilters();

        setupExamFilter();

        loadAttendanceChart();

        loadExamChart();

        setDashboardActive();
    }


    // =========================================================
    // ADMIN NAME
    // =========================================================

    private void loadAdminName() {

        if (lblAdminName != null) {

            lblAdminName.setText(
                    dashboardDAO.getAdminName()
            );
        }
    }


    // =========================================================
    // STATISTICS
    // =========================================================

    private void loadStatistics() {

        if (lblTotalStudents != null) {

            lblTotalStudents.setText(
                    String.valueOf(
                            dashboardDAO.getTotalStudents()
                    )
            );
        }


        if (lblTotalTeachers != null) {

            lblTotalTeachers.setText(
                    String.valueOf(
                            dashboardDAO.getTotalTeachers()
                    )
            );
        }


        if (lblTotalClasses != null) {

            lblTotalClasses.setText(
                    String.valueOf(
                            dashboardDAO.getTotalClasses()
                    )
            );
        }


        if (lblTotalSubjects != null) {

            lblTotalSubjects.setText(
                    String.valueOf(
                            dashboardDAO.getTotalSubjects()
                    )
            );
        }
    }


    // =========================================================
    // ATTENDANCE FILTER
    // =========================================================

    private void setupAttendanceFilters() {

        cmbAttendancePeriod.setItems(
                FXCollections.observableArrayList(
                        "TODAY",
                        "WEEK",
                        "MONTH",
                        "YEAR"
                )
        );

        cmbAttendancePeriod.setValue("TODAY");


        cmbAttendancePeriod.setOnAction(e ->
                loadAttendanceChart()
        );


        loadBatches();
    }


    // =========================================================
    // LOAD BATCHES
    // =========================================================

    private void loadBatches() {

        ObservableList<ClassItem> list =
                FXCollections.observableArrayList();

        list.add(
                new ClassItem(
                        0,
                        "All Batches"
                )
        );


        try (
                ResultSet rs =
                        dashboardDAO.getClasses()
        ) {

            while (rs.next()) {

                list.add(
                        new ClassItem(
                                rs.getInt("class_id"),
                                rs.getString("class_name")
                        )
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }


        cmbAttendanceBatch.setItems(list);

        cmbAttendanceBatch.setValue(
                list.get(0)
        );


        cmbAttendanceBatch.setOnAction(e ->
                loadAttendanceChart()
        );
    }


    // =========================================================
    // ATTENDANCE PIE CHART
    // =========================================================

    private void loadAttendanceChart() {

        if (attendancePieChart == null) {
            return;
        }


        String period =
                cmbAttendancePeriod.getValue();

        if (period == null) {
            period = "TODAY";
        }


        int classId = 0;

        if (cmbAttendanceBatch.getValue() != null) {

            classId =
                    cmbAttendanceBatch
                            .getValue()
                            .getClassId();
        }


        AttendanceData data =
                dashboardDAO.getAttendance(
                        period,
                        classId
                );


        ObservableList<PieChart.Data> chartData =
                FXCollections.observableArrayList();


        if (data.getPresent() > 0) {

            chartData.add(
                    new PieChart.Data(
                            "Present (" +
                                    data.getPresent() +
                                    ")",
                            data.getPresent()
                    )
            );
        }


        if (data.getAbsent() > 0) {

            chartData.add(
                    new PieChart.Data(
                            "Absent (" +
                                    data.getAbsent() +
                                    ")",
                            data.getAbsent()
                    )
            );
        }


        if (data.getLate() > 0) {

            chartData.add(
                    new PieChart.Data(
                            "Late (" +
                                    data.getLate() +
                                    ")",
                            data.getLate()
                    )
            );
        }


        if (chartData.isEmpty()) {

            chartData.add(
                    new PieChart.Data(
                            "No attendance data",
                            1
                    )
            );
        }


        attendancePieChart
                .setData(chartData);

        attendancePieChart
                .setTitle("");

        attendancePieChart
                .setLegendVisible(true);

        attendancePieChart
                .setLabelsVisible(true);
    }


    // =========================================================
    // EXAM FILTER
    // =========================================================

    private void setupExamFilter() {

        loadExams();

        cmbExam.setOnAction(e ->
                loadExamChart()
        );
    }


    // =========================================================
    // LOAD EXAMS
    // =========================================================

    private void loadExams() {

        ObservableList<ExamItem> list =
                FXCollections.observableArrayList();


        list.add(
                new ExamItem(
                        0,
                        "All Exams"
                )
        );


        try (
                ResultSet rs =
                        dashboardDAO.getExams()
        ) {

            while (rs.next()) {

                list.add(
                        new ExamItem(
                                rs.getInt("exam_id"),
                                rs.getString("exam_name")
                        )
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }


        cmbExam.setItems(list);

        cmbExam.setValue(list.get(0));
    }


    // =========================================================
    // EXAM PIE CHART
    // =========================================================

    private void loadExamChart() {

        if (examPieChart == null) {
            return;
        }


        int examId = 0;


        if (cmbExam.getValue() != null) {

            examId =
                    cmbExam
                            .getValue()
                            .getExamId();
        }


        ExamResultData data =
                dashboardDAO.getExamResults(
                        examId
                );


        ObservableList<PieChart.Data> chartData =
                FXCollections.observableArrayList();


        if (data.getPass() > 0) {

            chartData.add(
                    new PieChart.Data(
                            "PASS (" +
                                    data.getPass() +
                                    ")",
                            data.getPass()
                    )
            );
        }


        if (data.getFail() > 0) {

            chartData.add(
                    new PieChart.Data(
                            "FAIL (" +
                                    data.getFail() +
                                    ")",
                            data.getFail()
                    )
            );
        }


        if (chartData.isEmpty()) {

            chartData.add(
                    new PieChart.Data(
                            "No exam data",
                            1
                    )
            );
        }


        examPieChart.setData(
                chartData
        );

        examPieChart.setTitle("");

        examPieChart.setLegendVisible(true);

        examPieChart.setLabelsVisible(true);
    }


    // =========================================================
    // REFRESH
    // =========================================================

    @FXML
    public void refreshDashboard() {

        loadAdminName();

        loadStatistics();

        loadAttendanceChart();

        loadExamChart();

        setDashboardActive();
    }


    // =========================================================
    // ACTIVE DASHBOARD BUTTON
    // =========================================================

    private void setDashboardActive() {

        resetButtonStyles();

        if (btnDashboard != null) {

            btnDashboard.setStyle(
                    "-fx-background-color: #344563;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-alignment: CENTER_LEFT;" +
                            "-fx-padding: 0 14;" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;"
            );
        }
    }


    // =========================================================
    // RESET BUTTON
    // =========================================================

    private void resetButtonStyles() {

        Button[] buttons = {

                btnDashboard,
                btnStudents,
                btnTeachers,
                btnClasses,
                btnSubjects,
                btnExams,
                btnGrades,
                btnAttendance,
                btnLeave,
                btnAnnouncements,
                btnProfile
        };


        for (Button button : buttons) {

            if (button != null) {

                button.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: #b2bdcf;" +
                                "-fx-font-size: 13px;" +
                                "-fx-alignment: CENTER_LEFT;" +
                                "-fx-padding: 0 14;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;"
                );
            }
        }
    }


    // =========================================================
    // OPEN STUDENTS
    // =========================================================

    @FXML
    public void openStudents(ActionEvent event) {

        setActiveButton(btnStudents);

        // မင်းရဲ့ existing Students page loading code ကို
        // ဒီနေရာမှာထားပါ။
    }


    // =========================================================
    // OPEN TEACHERS
    // =========================================================

    @FXML
    public void openTeachers(ActionEvent event) {

        setActiveButton(btnTeachers);
    }


    // =========================================================
    // OPEN CLASSES
    // =========================================================

    @FXML
    public void openClasses(ActionEvent event) {

        setActiveButton(btnClasses);
    }


    // =========================================================
    // OPEN SUBJECTS
    // =========================================================

    @FXML
    public void openSubjects(ActionEvent event) {

        setActiveButton(btnSubjects);
    }


    // =========================================================
    // OPEN EXAMS
    // =========================================================

    @FXML
    public void openExams(ActionEvent event) {

        setActiveButton(btnExams);
    }


    // =========================================================
    // OPEN GRADES
    // =========================================================

    @FXML
    public void openGrades(ActionEvent event) {

        setActiveButton(btnGrades);
    }


    // =========================================================
    // OPEN ATTENDANCE
    // =========================================================

    @FXML
    public void openAttendance(ActionEvent event) {

        setActiveButton(btnAttendance);
    }


    // =========================================================
    // OPEN LEAVE
    // =========================================================

    @FXML
    public void openLeaveRequests(ActionEvent event) {

        setActiveButton(btnLeave);
    }


    // =========================================================
    // OPEN ANNOUNCEMENTS
    // =========================================================

    @FXML
    public void openAnnouncements(ActionEvent event) {

        setActiveButton(btnAnnouncements);
    }


    // =========================================================
    // OPEN PROFILE
    // =========================================================

    @FXML
    public void openProfile(ActionEvent event) {

        setActiveButton(btnProfile);
    }


    // =========================================================
    // ACTIVE BUTTON
    // =========================================================

    private void setActiveButton(Button button) {

        resetButtonStyles();

        if (button != null) {

            button.setStyle(
                    "-fx-background-color: #344563;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-alignment: CENTER_LEFT;" +
                            "-fx-padding: 0 14;" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;"
            );
        }
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    @FXML
    public void logout(ActionEvent event) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/com/example/student_management_system/Login.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Node source =
                    (Node) event.getSource();

            javafx.stage.Stage stage =
                    (javafx.stage.Stage)
                            source.getScene().getWindow();

            stage.getScene().setRoot(root);

        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    // =========================================================
    // CLASS ITEM
    // =========================================================

    public static class ClassItem {

        private final int classId;

        private final String className;


        public ClassItem(
                int classId,
                String className
        ) {

            this.classId = classId;

            this.className = className;
        }


        public int getClassId() {

            return classId;
        }


        @Override
        public String toString() {

            return className;
        }
    }


    // =========================================================
    // EXAM ITEM
    // =========================================================

    public static class ExamItem {

        private final int examId;

        private final String examName;


        public ExamItem(
                int examId,
                String examName
        ) {

            this.examId = examId;

            this.examName = examName;
        }


        public int getExamId() {

            return examId;
        }


        @Override
        public String toString() {

            return examName;
        }
    }
}