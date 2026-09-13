package com.example.student_management_system.Controller.Teacher;

import com.example.student_management_system.Controller.DAO.TeacherDashboardDAO;
import com.example.student_management_system.Controller.Model.TeacherInfo;   // <-- fixed

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class TeacherDashboardController implements Initializable {

    // Sidebar buttons
    @FXML private Button btnDashboard, btnMyStudents, btnAttendance, btnExams,
            btnResults, btnMySubjects, btnMyClasses, btnLeaveRequests,
            btnAnnouncements, btnProfile, btnLogout;

    // Center content
    @FXML private StackPane contentPane;
    @FXML private ScrollPane dashboardScrollPane;
    @FXML private AnchorPane teacherPane;
    @FXML private VBox welcomeToast;
    @FXML private Label lblToastTitle, lblToastHeading, lblDashboardWelcome;

    // Header
    @FXML private ImageView teacherPhoto;
    @FXML private Label lblTeacherName;

    // Stat cards
    @FXML private Label lblTotalStudents, lblTotalClasses, lblTotalSubjects, lblPendingLeave;

    // Attendance
    @FXML private ComboBox<String> cmbAttendancePeriod;
    @FXML private ComboBox<String> cmbAttendanceClass;
    @FXML private PieChart attendancePieChart;
    @FXML private Label lblPresentCount, lblAbsentCount, lblLateCount;

    // Exam
    @FXML private ComboBox<String> cmbExam;
    @FXML private PieChart examPieChart;
    @FXML private Label lblPassCount, lblFailCount;

    // Performance
    @FXML private ComboBox<String> cmbPerformanceExam;
    @FXML private BarChart<String, Number> performanceBarChart;

    // DAO + session
    private final TeacherDashboardDAO dao = new TeacherDashboardDAO();
    private TeacherInfo teacherInfo;
    private PauseTransition welcomeTimer;
    private Map<Integer, String> teacherClassMap;

    private static final String ACTIVE =
            "-fx-background-color:#4f46e5; -fx-background-radius:10; -fx-text-fill:white;" +
                    "-fx-font-size:13px; -fx-font-weight:bold; -fx-alignment:CENTER_LEFT;" +
                    "-fx-padding:0 16; -fx-cursor:hand;";
    private static final String NORMAL =
            "-fx-background-color:transparent; -fx-text-fill:#cbd5e1; -fx-font-size:13px;" +
                    "-fx-alignment:CENTER_LEFT; -fx-padding:0 16; -fx-cursor:hand;";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbAttendancePeriod.setItems(FXCollections.observableArrayList(
                "This Week", "This Month", "This Year", "All Time"));
        cmbAttendancePeriod.setValue("This Month");

        cmbAttendanceClass.setItems(FXCollections.observableArrayList("All Classes"));
        cmbAttendanceClass.setValue("All Classes");

        cmbExam.setItems(FXCollections.observableArrayList("All Exams"));
        cmbExam.setValue("All Exams");

        cmbPerformanceExam.setItems(FXCollections.observableArrayList("All Exams"));
        cmbPerformanceExam.setValue("All Exams");

        cmbAttendancePeriod.setOnAction(e -> reloadAttendance());
        cmbAttendanceClass .setOnAction(e -> reloadAttendance());
        cmbExam            .setOnAction(e -> reloadExamStats());
        cmbPerformanceExam .setOnAction(e -> reloadPerformance());

        setActiveButton(btnDashboard);
    }

    /** Called from LoginController. */
    public void setLoggedInTeacher(String username) {
        teacherInfo = dao.getTeacherByUsername(username);

        if (teacherInfo != null) {
            lblTeacherName.setText(teacherInfo.getDisplayName());
            loadTeacherPhoto(teacherInfo.getPhotoPath());
            loadTeacherCombos();
            loadDashboardData();
            showWelcomeToast(teacherInfo.getDisplayName());
        } else {
            lblTeacherName.setText(username);
            loadDefaultPhoto();
            showWelcomeToast(username);
        }
    }

    private void loadTeacherPhoto(String photoPath) {
        try {
            if (photoPath != null && !photoPath.isBlank()) {
                if (photoPath.startsWith("http")) {
                    teacherPhoto.setImage(new Image(photoPath, true));
                    return;
                }
                File f = new File(photoPath);
                if (f.exists()) {
                    teacherPhoto.setImage(new Image(f.toURI().toString()));
                    return;
                }
            }
        } catch (Exception ignored) {}
        loadDefaultPhoto();
    }

    private void loadDefaultPhoto() {
        try {
            Image img = new Image(getClass().getResourceAsStream(
                    "/com/example/student_management_system/Images/default_avatar.png"));
            teacherPhoto.setImage(img);
        } catch (Exception e) {
            teacherPhoto.setImage(null);
        }
    }

    private void loadTeacherCombos() {
        if (teacherInfo == null) return;

        teacherClassMap = dao.getTeacherClassMap(teacherInfo.getTeacherId());

        var classItems = FXCollections.observableArrayList("All Classes");
        classItems.addAll(teacherClassMap.values());
        cmbAttendanceClass.setItems(classItems);
        cmbAttendanceClass.setValue("All Classes");

        List<String> exams = dao.getTeacherExamNames(teacherInfo.getTeacherId());
        var examItems = FXCollections.observableArrayList("All Exams");
        examItems.addAll(exams);

        cmbExam.setItems(FXCollections.observableArrayList(examItems));
        cmbExam.setValue("All Exams");
        cmbPerformanceExam.setItems(FXCollections.observableArrayList(examItems));
        cmbPerformanceExam.setValue("All Exams");
    }

    private void loadDashboardData() {
        if (teacherInfo == null) return;
        int tid = teacherInfo.getTeacherId();

        lblTotalStudents.setText(String.valueOf(dao.getTotalStudents(tid)));
        lblTotalClasses .setText(String.valueOf(dao.getTotalClasses(tid)));
        lblTotalSubjects.setText(String.valueOf(dao.getTotalSubjects(tid)));
        lblPendingLeave .setText(String.valueOf(dao.getPendingLeaveCount(tid)));

        reloadAttendance();
        reloadExamStats();
        reloadPerformance();
    }

    private void reloadAttendance() {
        if (teacherInfo == null) return;

        String period = cmbAttendancePeriod.getValue();
        String cls    = cmbAttendanceClass.getValue();
        Integer classId = null;
        if (cls != null && !cls.equals("All Classes") && teacherClassMap != null) {
            classId = teacherClassMap.entrySet().stream()
                    .filter(e -> e.getValue().equals(cls))
                    .map(Map.Entry::getKey).findFirst().orElse(null);
        }

        Map<String, Integer> stats = dao.getAttendanceStats(teacherInfo.getTeacherId(), period, classId);
        int present = stats.getOrDefault("Present", 0);
        int absent  = stats.getOrDefault("Absent", 0);
        int late    = stats.getOrDefault("Late", 0);

        if (present + absent + late == 0) {
            attendancePieChart.setData(FXCollections.observableArrayList());
        } else {
            attendancePieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Present", present),
                    new PieChart.Data("Absent", absent),
                    new PieChart.Data("Late", late)));
            applyPieColor(attendancePieChart, "Present", "#16a34a");
            applyPieColor(attendancePieChart, "Absent",  "#ef4444");
            applyPieColor(attendancePieChart, "Late",    "#f59e0b");
        }

        lblPresentCount.setText(String.valueOf(present));
        lblAbsentCount .setText(String.valueOf(absent));
        lblLateCount   .setText(String.valueOf(late));
    }

    private void reloadExamStats() {
        if (teacherInfo == null) return;
        String examName = cmbExam.getValue();
        Map<String, Integer> stats = dao.getExamStats(teacherInfo.getTeacherId(), examName);
        int pass = stats.getOrDefault("PASS", 0);
        int fail = stats.getOrDefault("FAIL", 0);

        if (pass + fail == 0) {
            examPieChart.setData(FXCollections.observableArrayList());
        } else {
            examPieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Pass", pass),
                    new PieChart.Data("Fail", fail)));
            applyPieColor(examPieChart, "Pass", "#16a34a");
            applyPieColor(examPieChart, "Fail", "#ef4444");
        }

        lblPassCount.setText(String.valueOf(pass));
        lblFailCount.setText(String.valueOf(fail));
    }

    private void reloadPerformance() {
        if (teacherInfo == null) return;
        String examName = cmbPerformanceExam.getValue();
        Map<String, Map<String, Double>> data =
                dao.getPerformanceData(teacherInfo.getTeacherId(), examName);

        performanceBarChart.getData().clear();
        for (Map.Entry<String, Map<String, Double>> e : data.entrySet()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(e.getKey());
            for (Map.Entry<String, Double> sub : e.getValue().entrySet()) {
                series.getData().add(new XYChart.Data<>(sub.getKey(), sub.getValue()));
            }
            performanceBarChart.getData().add(series);
        }
    }

    private void applyPieColor(PieChart chart, String name, String color) {
        for (PieChart.Data d : chart.getData()) {
            if (d.getName().equals(name)) {
                d.nodeProperty().addListener((obs, o, n) -> {
                    if (n != null) n.setStyle("-fx-pie-color:" + color + ";");
                });
                if (d.getNode() != null) d.getNode().setStyle("-fx-pie-color:" + color + ";");
            }
        }
    }

    private void showWelcomeToast(String name) {
        if (name == null || name.trim().isEmpty()) name = "Teacher";
        welcomeToast.setPrefWidth(420);
        welcomeToast.setMinWidth(420);
        welcomeToast.setMaxWidth(420);
        welcomeToast.setPrefHeight(125);
        welcomeToast.setMinHeight(125);
        welcomeToast.setMaxHeight(125);

        lblToastTitle.setText("LOGIN SUCCESSFUL");
        lblToastHeading.setText("Welcome back!");
        lblDashboardWelcome.setText("Welcome, " + name + ". You are signed in successfully.");
        welcomeToast.setManaged(true);
        welcomeToast.setVisible(true);

        if (welcomeTimer != null) welcomeTimer.stop();
        welcomeTimer = new PauseTransition(Duration.seconds(5));
        welcomeTimer.setOnFinished(e -> {
            welcomeToast.setVisible(false);
            welcomeToast.setManaged(false);
        });
        welcomeTimer.play();
    }

    @FXML
    public void refreshDashboard(ActionEvent e) {
        setActiveButton(btnDashboard);
        dashboardScrollPane.setVisible(true);
        teacherPane.setVisible(false);
        teacherPane.getChildren().clear();
        loadDashboardData();
    }

    private void setActiveButton(Button selected) {
        for (Button b : List.of(
                btnDashboard, btnMyStudents, btnAttendance, btnExams,
                btnResults, btnMySubjects, btnMyClasses, btnLeaveRequests,
                btnAnnouncements, btnProfile)) {
            if (b != null) b.setStyle(b == selected ? ACTIVE : NORMAL);
        }
    }

    private void showDashboard() {
        teacherPane.setVisible(false);
        dashboardScrollPane.setVisible(true);
    }

    @FXML
    public void openMyStudents(ActionEvent e) {
        setActiveButton(btnMyStudents);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Teacher/TeacherStudents.fxml"));
            Parent view = loader.load();

            TeacherStudentsController ctrl = loader.getController();
            ctrl.setTeacherInfo(teacherInfo);

            teacherPane.getChildren().setAll(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

            dashboardScrollPane.setVisible(false);
            teacherPane.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    @FXML
    public void openAttendance(ActionEvent e) {
        setActiveButton(btnAttendance);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Teacher/TeacherAttendance.fxml"));
            Parent view = loader.load();

            TeacherAttendanceController ctrl = loader.getController();
            ctrl.setTeacherInfo(teacherInfo);

            teacherPane.getChildren().setAll(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

            dashboardScrollPane.setVisible(false);
            teacherPane.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    @FXML
    public void openExams(ActionEvent e) {
        setActiveButton(btnExams);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Teacher/TeacherExam.fxml"));
            Parent view = loader.load();

            TeacherExamController ctrl = loader.getController();
            ctrl.setTeacherInfo(teacherInfo);

            teacherPane.getChildren().setAll(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

            dashboardScrollPane.setVisible(false);
            teacherPane.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    @FXML
    public void openResults(ActionEvent e) {
        setActiveButton(btnResults);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Teacher/TeacherResult.fxml"));
            Parent view = loader.load();

            TeacherResultsController ctrl = loader.getController();
            ctrl.setTeacherInfo(teacherInfo);

            teacherPane.getChildren().setAll(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

            dashboardScrollPane.setVisible(false);
            teacherPane.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    @FXML
    public void openMySubjects(ActionEvent e) {
        setActiveButton(btnMySubjects);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Teacher/TeacherSubjects.fxml"));
            Parent view = loader.load();

            TeacherSubjectsController ctrl = loader.getController();
            ctrl.setTeacherInfo(teacherInfo);

            teacherPane.getChildren().setAll(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

            dashboardScrollPane.setVisible(false);
            teacherPane.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    @FXML public void openMyClasses(ActionEvent e)    { }
    @FXML public void openLeaveRequests(ActionEvent e){ }
    @FXML public void openAnnouncements(ActionEvent e){ }
    @FXML public void openProfile(ActionEvent e)      { }

    @FXML
    public void logout(ActionEvent e) {
        Stage stage = null;
        if (e != null && e.getSource() instanceof Node) {
            stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        } else if (btnLogout != null && btnLogout.getScene() != null) {
            stage = (Stage) btnLogout.getScene().getWindow();
        }
        if (stage == null) {
            System.err.println("Logout Error: Target Stage could not be determined.");
            return;
        }
        final Stage currentStage = stage;

        lblToastTitle.setText("LOGOUT SUCCESS");
        lblToastHeading.setText("Signed out");
        lblDashboardWelcome.setText("You have been logged out successfully.");

        welcomeToast.setManaged(true);
        welcomeToast.setVisible(true);
        if (welcomeTimer != null) welcomeTimer.stop();

        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/student_management_system/View/Login.fxml"));
                Parent loginRoot = loader.load();
                currentStage.setScene(new Scene(loginRoot));
                currentStage.setMaximized(true);
                currentStage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        delay.play();
    }
}