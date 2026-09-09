package com.example.student_management_system.Controller.Teacher;

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

import java.net.URL;
import java.util.List;
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

    // Attendance section
    @FXML private ComboBox<String> cmbAttendancePeriod;
    @FXML private ComboBox<String> cmbAttendanceClass;  // simple String combo
    @FXML private PieChart attendancePieChart;
    @FXML private Label lblPresentCount, lblAbsentCount, lblLateCount;

    // Exam section
    @FXML private ComboBox<String> cmbExam;
    @FXML private PieChart examPieChart;
    @FXML private Label lblPassCount, lblFailCount;

    // Performance chart
    @FXML private ComboBox<String> cmbPerformanceExam;
    @FXML private BarChart<String, Number> performanceBarChart;

    private String teacherUsername;
    private PauseTransition welcomeTimer;

    // Button styles
    private static final String ACTIVE =
            "-fx-background-color:#4f46e5;" +
                    "-fx-background-radius:10;" +
                    "-fx-text-fill:white;" +
                    "-fx-font-size:13px;" +
                    "-fx-font-weight:bold;" +
                    "-fx-alignment:CENTER_LEFT;" +
                    "-fx-padding:0 16;" +
                    "-fx-cursor:hand;";
    private static final String NORMAL =
            "-fx-background-color:transparent;" +
                    "-fx-text-fill:#cbd5e1;" +
                    "-fx-font-size:13px;" +
                    "-fx-alignment:CENTER_LEFT;" +
                    "-fx-padding:0 16;" +
                    "-fx-cursor:hand;";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Populate combo boxes with static dummy items
        cmbAttendancePeriod.setItems(FXCollections.observableArrayList(
                "This Week", "This Month", "This Year", "All Time"
        ));
        cmbAttendancePeriod.setValue("This Month");

        cmbAttendanceClass.setItems(FXCollections.observableArrayList(
                "All Classes", "Class 10A", "Class 10B", "Class 11A"
        ));
        cmbAttendanceClass.setValue("All Classes");

        cmbExam.setItems(FXCollections.observableArrayList(
                "All Exams", "Midterm 2026", "Final 2026", "Quarterly"
        ));
        cmbExam.setValue("All Exams");

        cmbPerformanceExam.setItems(FXCollections.observableArrayList(
                "All Exams", "Midterm 2026", "Final 2026", "Quarterly"
        ));
        cmbPerformanceExam.setValue("All Exams");

        // Set active button
        setActiveButton(btnDashboard);
    }

    /**
     * Called from LoginController after login with the teacher's username.
     */
    public void setLoggedInTeacher(String username) {
        teacherUsername = username;
        lblTeacherName.setText(username);  // or you can load full name from DB later
        showWelcomeToast(username);
        loadDefaultPhoto();
        loadDummyData();  // fill stats and charts with dummy numbers
    }

    private void loadDefaultPhoto() {
        try {
            Image img = new Image(
                    getClass().getResourceAsStream("/com/example/student_management_system/Images/default_avatar.png")
            );
            teacherPhoto.setImage(img);
        } catch (Exception e) {
            teacherPhoto.setImage(null);
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
        showDashboard();
        setActiveButton(btnDashboard);
        loadDummyData();
    }

    private void loadDummyData() {
        // Stat cards – dummy numbers
        lblTotalStudents.setText("42");
        lblTotalClasses.setText("3");
        lblTotalSubjects.setText("5");
        lblPendingLeave.setText("2");

        // Attendance pie chart
        attendancePieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Present", 28),
                new PieChart.Data("Absent", 6),
                new PieChart.Data("Late", 4)
        ));
        // Set colors manually
        for (PieChart.Data d : attendancePieChart.getData()) {
            if (d.getName().equals("Present")) d.getNode().setStyle("-fx-pie-color:#16a34a;");
            else if (d.getName().equals("Absent")) d.getNode().setStyle("-fx-pie-color:#ef4444;");
            else d.getNode().setStyle("-fx-pie-color:#f59e0b;");
        }
        lblPresentCount.setText("28");
        lblAbsentCount.setText("6");
        lblLateCount.setText("4");

        // Exam pie chart
        examPieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Pass", 34),
                new PieChart.Data("Fail", 8)
        ));
        for (PieChart.Data d : examPieChart.getData()) {
            if (d.getName().equals("Pass")) d.getNode().setStyle("-fx-pie-color:#16a34a;");
            else d.getNode().setStyle("-fx-pie-color:#ef4444;");
        }
        lblPassCount.setText("34");
        lblFailCount.setText("8");

        // Performance bar chart – dummy data by class
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName("Class 10A");
        series1.getData().add(new XYChart.Data<>("Math", 78.5));
        series1.getData().add(new XYChart.Data<>("Science", 82.0));
        series1.getData().add(new XYChart.Data<>("English", 74.3));

        XYChart.Series<String, Number> series2 = new XYChart.Series<>();
        series2.setName("Class 10B");
        series2.getData().add(new XYChart.Data<>("Math", 65.2));
        series2.getData().add(new XYChart.Data<>("Science", 70.8));
        series2.getData().add(new XYChart.Data<>("English", 68.1));

        performanceBarChart.getData().setAll(series1, series2);
    }

    private void setActiveButton(Button selected) {
        for (Button b : List.of(
                btnDashboard, btnMyStudents, btnAttendance, btnExams,
                btnResults, btnMySubjects, btnMyClasses, btnLeaveRequests,
                btnAnnouncements, btnProfile
        )) {
            if (b != null) {
                b.setStyle(b == selected ? ACTIVE : NORMAL);
            }
        }
    }

    private void showDashboard() {
        teacherPane.setVisible(false);
        dashboardScrollPane.setVisible(true);
    }

    // ---------- Navigation methods (empty stubs) ----------
    @FXML public void openMyStudents(ActionEvent e) { /* later */ }
    @FXML public void openAttendance(ActionEvent e) { /* later */ }
    @FXML public void openExams(ActionEvent e) { /* later */ }
    @FXML public void openResults(ActionEvent e) { /* later */ }
    @FXML public void openMySubjects(ActionEvent e) { /* later */ }
    @FXML public void openMyClasses(ActionEvent e) { /* later */ }
    @FXML public void openLeaveRequests(ActionEvent e) { /* later */ }
    @FXML public void openAnnouncements(ActionEvent e) { /* later */ }

    @FXML
    public void openProfile(ActionEvent e) {
        // You can load a profile view later
    }

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

        // Show logout toast
        if (lblToastTitle != null) lblToastTitle.setText("LOGOUT SUCCESS");
        if (lblToastHeading != null) lblToastHeading.setText("Signed out");
        if (lblDashboardWelcome != null) lblDashboardWelcome.setText("You have been logged out successfully.");

        welcomeToast.setManaged(true);
        welcomeToast.setVisible(true);
        if (welcomeTimer != null) welcomeTimer.stop();

        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/student_management_system/View/Login.fxml")
                );
                Parent loginRoot = loader.load();
                Scene scene = new Scene(loginRoot);
                currentStage.setScene(scene);
                currentStage.setMaximized(true);
                currentStage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        delay.play();
    }
}