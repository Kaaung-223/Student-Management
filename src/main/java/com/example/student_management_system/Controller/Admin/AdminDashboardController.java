package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.*;
import com.example.student_management_system.Controller.Model.*;

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
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class AdminDashboardController implements Initializable {
    @FXML private Label lblToastTitle;
    @FXML private Label lblToastHeading;
    @FXML private Button btnDashboard, btnStudents, btnTeachers, btnClasses,
            btnClassFees, btnSubjects, btnExams, btnGrades,
            btnAttendance, btnAnnouncements, btnProfile, btnLogout;

    @FXML private StackPane contentPane;
    @FXML private ScrollPane dashboardScrollPane;
    @FXML private AnchorPane adminPane;
    @FXML private Label lblAdminName, lblTotalStudents, lblTotalTeachers,
            lblTotalClasses, lblTotalSubjects, lblPassCount, lblFailCount,
            lblPresentCount, lblAbsentCount, lblLateCount, lblDashboardWelcome,
            lblFinanceExpected, lblFinanceCollected, lblFinanceOutstanding,
            lblFinancePaidUnpaid;
    @FXML private VBox welcomeToast;
    @FXML private ImageView adminPhoto;
    @FXML private ComboBox<String> cmbAttendancePeriod;
    @FXML private ComboBox<Batch> cmbAttendanceBatch, cmbFinanceBatch;
    @FXML private ComboBox<ExamOption> cmbExam;
    @FXML private PieChart attendancePieChart, examPieChart, financePieChart;
    @FXML private BarChart<String, Number> financeBarChart;

    private final AdminDAO adminDAO = new AdminDAO();
    private final StudentDao studentDAO = new StudentDao();
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final ExamDAO examDAO = new ExamDAO();
    private final LeaveDAO leaveDAO = new LeaveDAO();
    private final FeeDAO feeDAO = new FeeDAO();

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final Batch ALL_BATCHES = new Batch(-1, "All Batches");
    private static final ExamOption ALL_EXAMS = new ExamOption(-1, "All Exams");
    private static final NumberFormat MONEY_FORMAT =
            NumberFormat.getNumberInstance(Locale.US);
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

    private PauseTransition welcomeTimer;
    private int currentAdminId = -1;

    @Override
    public void initialize(URL u, ResourceBundle r) {
        setupComboBoxes();
        setActiveButton(btnDashboard);
        refreshDashboard();
    }

    private void setupComboBoxes() {
        cmbAttendancePeriod.setItems(
                FXCollections.observableArrayList(
                        "This Week", "This Month", "This Year", "All Time"
                )
        );
        cmbAttendancePeriod.setValue("This Month");
        cmbAttendancePeriod.setOnAction(e -> loadAttendanceChart());

        List<Batch> batches = classDAO.getAllBatches();

        cmbAttendanceBatch.setItems(FXCollections.observableArrayList());
        cmbAttendanceBatch.getItems().add(ALL_BATCHES);
        if (batches != null) cmbAttendanceBatch.getItems().addAll(batches);
        cmbAttendanceBatch.setValue(ALL_BATCHES);
        cmbAttendanceBatch.setOnAction(e -> loadAttendanceChart());

        cmbFinanceBatch.setItems(FXCollections.observableArrayList());
        cmbFinanceBatch.getItems().add(ALL_BATCHES);
        if (batches != null) cmbFinanceBatch.getItems().addAll(batches);
        cmbFinanceBatch.setValue(ALL_BATCHES);
        cmbFinanceBatch.setOnAction(e -> loadFinanceChart());

        cmbExam.setItems(FXCollections.observableArrayList());
        cmbExam.getItems().add(ALL_EXAMS);
        List<ExamOption> exams = examDAO.getAllExams();
        if (exams != null) cmbExam.getItems().addAll(exams);
        cmbExam.setValue(ALL_EXAMS);
        cmbExam.setOnAction(e -> loadExamChart());
    }

    public void setLoggedInAdmin(int id) {
        currentAdminId = id;
        Admin a = adminDAO.getAdminById(id);
        if (a != null) {
            lblAdminName.setText(a.getFullName());
            showWelcomeToast(a.getFullName());
            loadAdminPhoto(id);
        }
    }

    public void setLoggedInAdmin(String username) {
        Admin a = adminDAO.getAdminByUsername(username);
        String name = (a == null) ? username : a.getFullName();
        lblAdminName.setText(name);
        showWelcomeToast(name);
        if (a != null) {
            currentAdminId = a.getUserId();
            loadAdminPhoto(currentAdminId);
        }
    }

    private void loadAdminPhoto(int userId) {
        Admin profile = adminDAO.getAdminProfileById(userId);
        if (profile != null && profile.getPhotoPath() != null && !profile.getPhotoPath().trim().isEmpty()) {
            try {
                File imgFile = new File(profile.getPhotoPath());
                if (imgFile.exists()) {
                    Image img = new Image(imgFile.toURI().toString(), true);
                    adminPhoto.setImage(img);
                } else {
                    setDefaultAdminPhoto();
                }
            } catch (Exception e) {
                setDefaultAdminPhoto();
            }
        } else {
            setDefaultAdminPhoto();
        }
    }

    public void refreshAdminPhoto() {
        if (currentAdminId != -1) {
            loadAdminPhoto(currentAdminId);
        }
    }

    private void setDefaultAdminPhoto() {
        try {
            Image defaultImg = new Image(
                    getClass().getResourceAsStream("/com/example/student_management_system/Images/default_avatar.png")
            );
            adminPhoto.setImage(defaultImg);
        } catch (Exception e) {
            adminPhoto.setImage(null);
        }
    }

    private void showWelcomeToast(String name) {
        if (name == null || name.trim().isEmpty()) name = "Administrator";

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
        refreshDashboard();
    }

    public void refreshDashboard() {
        loadStatCards();
        loadAttendanceChart();
        loadExamChart();
        loadFinanceChart();
        refreshAdminPhoto();
    }

    private void loadStatCards() {
        lblTotalStudents.setText("" + studentDAO.getTotalStudents());
        lblTotalTeachers.setText("" + teacherDAO.getTotalTeachers());
        lblTotalClasses.setText("" + classDAO.getTotalClasses());
        lblTotalSubjects.setText("" + subjectDAO.getTotalSubjects());
    }

    private void loadAttendanceChart() {
        Batch b = cmbAttendanceBatch.getValue();
        AttendanceSummary s = attendanceDAO.getAttendanceSummary(
                cmbAttendancePeriod.getValue(),
                b == null ? -1 : b.getId()
        );
        int p = (s != null) ? s.getPresentCount() : 0;
        int a = (s != null) ? s.getAbsentCount() : 0;
        int l = (s != null) ? s.getLateCount() : 0;

        lblPresentCount.setText("" + p);
        lblAbsentCount.setText("" + a);
        lblLateCount.setText("" + l);

        if (p + a + l == 0) {
            attendancePieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("No Data", 1)
            ));
        } else {
            attendancePieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Present", p),
                    new PieChart.Data("Absent", a),
                    new PieChart.Data("Late", l)
            ));
        }
        styleNoData(attendancePieChart);
    }

    private void loadExamChart() {
        ExamOption e = cmbExam.getValue();
        ExamResultSummary s = examDAO.getExamResultSummary(e == null ? -1 : e.getId());
        int p = (s != null) ? s.getPassCount() : 0;
        int f = (s != null) ? s.getFailCount() : 0;
        lblPassCount.setText("" + p);
        lblFailCount.setText("" + f);

        if (p + f == 0) {
            examPieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("No Grades Yet", 1)
            ));
        } else {
            examPieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Pass", p),
                    new PieChart.Data("Fail", f)
            ));
        }
        styleNoData(examPieChart);
    }

    private void loadFinanceChart() {
        Batch batch = cmbFinanceBatch.getValue();
        int classId = batch == null ? -1 : batch.getId();

        FeeFinancialSummary summary = feeDAO.getFinancialSummary(classId);
        BigDecimal expected = (summary != null) ? summary.getTotalExpected() : BigDecimal.ZERO;
        BigDecimal collectedAmt = (summary != null) ? summary.getTotalCollected() : BigDecimal.ZERO;
        BigDecimal outstandingAmt = (summary != null) ? summary.getTotalOutstanding() : BigDecimal.ZERO;
        int paidStudents = (summary != null) ? summary.getPaidStudents() : 0;
        int unpaidStudents = (summary != null) ? summary.getUnpaidStudents() : 0;

        lblFinanceExpected.setText(formatMoney(expected) + " MMK");
        lblFinanceCollected.setText(formatMoney(collectedAmt) + " MMK");
        lblFinanceOutstanding.setText(formatMoney(outstandingAmt) + " MMK");
        lblFinancePaidUnpaid.setText(paidStudents + " / " + unpaidStudents);

        double collected = toChartValue(collectedAmt);
        double outstanding = toChartValue(outstandingAmt);

        if (collected + outstanding == 0) {
            financePieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("No Fee Data", 1)
            ));
        } else {
            financePieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Collected", collected),
                    new PieChart.Data("Outstanding", outstanding)
            ));
        }
        styleFinancePie();

        XYChart.Series<String, Number> expectedSeries = new XYChart.Series<>();
        expectedSeries.setName("Expected");
        XYChart.Series<String, Number> collectedSeries = new XYChart.Series<>();
        collectedSeries.setName("Collected");
        XYChart.Series<String, Number> outstandingSeries = new XYChart.Series<>();
        outstandingSeries.setName("Outstanding");

        List<FeeClassBreakdown> breakdown = feeDAO.getFeeBreakdownByClass();
        if (breakdown == null) breakdown = new ArrayList<>();

        if (classId != -1 && batch != null) {
            String selectedName = batch.getName();
            breakdown = breakdown.stream()
                    .filter(row -> selectedName.equals(row.getClassName()))
                    .toList();
        }

        if (breakdown.isEmpty()) {
            expectedSeries.getData().add(new XYChart.Data<>("No Data", 0));
            collectedSeries.getData().add(new XYChart.Data<>("No Data", 0));
            outstandingSeries.getData().add(new XYChart.Data<>("No Data", 0));
        } else {
            for (FeeClassBreakdown row : breakdown) {
                String name = row.getClassName() == null ? "Unassigned" : row.getClassName();
                expectedSeries.getData().add(new XYChart.Data<>(name, toChartValue(row.getExpected())));
                collectedSeries.getData().add(new XYChart.Data<>(name, toChartValue(row.getCollected())));
                outstandingSeries.getData().add(new XYChart.Data<>(name, toChartValue(row.getOutstanding())));
            }
        }

        financeBarChart.getData().setAll(expectedSeries, collectedSeries, outstandingSeries);
    }

    private void styleFinancePie() {
        for (PieChart.Data d : financePieChart.getData()) {
            d.nodeProperty().addListener((o, n, x) -> {
                if (x == null) return;
                String name = d.getName();
                if (name.startsWith("Collected")) {
                    x.setStyle("-fx-pie-color:#16a34a;");
                } else if (name.startsWith("Outstanding")) {
                    x.setStyle("-fx-pie-color:#dc2626;");
                } else {
                    x.setStyle("-fx-pie-color:#cbd5e1;");
                }
            });
        }
    }

    private double toChartValue(BigDecimal amount) {
        return amount == null ? 0.0 : amount.doubleValue();
    }

    private String formatMoney(BigDecimal amount) {
        return amount == null ? "0" : MONEY_FORMAT.format(amount);
    }

    private void styleNoData(PieChart c) {
        for (PieChart.Data d : c.getData()) {
            if (d.getName().startsWith("No ")) {
                d.nodeProperty().addListener((o, n, x) -> {
                    if (x != null) x.setStyle("-fx-pie-color:#cbd5e1;");
                });
            }
        }
    }

    private void setActiveButton(Button selected) {
        for (Button b : List.of(
                btnDashboard, btnStudents, btnTeachers, btnClasses,
                btnClassFees, btnSubjects, btnExams, btnGrades,
                btnAttendance, btnAnnouncements, btnProfile
        )) {
            if (b != null) {
                b.setStyle(b == selected ? ACTIVE : NORMAL);
            }
        }
    }

    private void showAdminPane() {
        dashboardScrollPane.setVisible(false);
        adminPane.setVisible(true);
    }

    private void showDashboard() {
        adminPane.setVisible(false);
        dashboardScrollPane.setVisible(true);
    }

    @FXML public void openStudents(ActionEvent event) { loadPage("/com/example/student_management_system/View/Admin/AdminStudent.fxml", btnStudents); }
    @FXML public void openTeachers(ActionEvent e) { loadPage("/com/example/student_management_system/View/Admin/AdminTeacher.fxml", btnTeachers); }
    @FXML public void openClasses(ActionEvent e) { loadPage("/com/example/student_management_system/View/Admin/AdminClass.fxml", btnClasses); }
    @FXML public void openClassFees(ActionEvent e) { loadPage("/com/example/student_management_system/View/Admin/AdminClassFee.fxml", btnClassFees); }
    @FXML public void openSubjects(ActionEvent e) { loadPage("/com/example/student_management_system/View/Admin/AdminSubject.fxml", btnSubjects); }
    @FXML public void openExams(ActionEvent e) { loadPage("/com/example/student_management_system/View/Admin/AdminExam.fxml", btnExams); }
    @FXML public void openGrades(ActionEvent e) { loadPage("/com/example/student_management_system/View/Admin/AdminGrade.fxml", btnGrades); }
    @FXML public void openAttendance(ActionEvent e) { loadPage("/com/example/student_management_system/View/Admin/AdminAttendance.fxml", btnAttendance); }
    @FXML public void openAnnouncements(ActionEvent e) { loadPage("/com/example/student_management_system/View/Admin/AdminAnnouncement.fxml", btnAnnouncements); }

    @FXML
    public void openProfile(ActionEvent e) {
        try {
            setActiveButton(btnProfile);
            showAdminPane();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/student_management_system/View/Admin/AdminProfile.fxml")
            );
            Parent page = loader.load();
            AdminProfileController controller = loader.getController();
            if (controller != null && currentAdminId != -1) {
                controller.setAdminId(currentAdminId);
            }
            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            adminPane.getChildren().setAll(page);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadPage(String fxml, Button btn) {
        try {
            setActiveButton(btn);
            showAdminPane();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent page = loader.load();
            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            adminPane.getChildren().setAll(page);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logout(ActionEvent e) {
        // 1. Synchronously obtain the target Stage reference before starting the timer
        Stage stage = null;
        if (e != null && e.getSource() instanceof Node) {
            stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        } else if (btnLogout != null && btnLogout.getScene() != null) {
            stage = (Stage) btnLogout.getScene().getWindow();
        }

        if (stage == null) {
            System.err.println("Logout Error: Target Stage reference could not be determined.");
            return;
        }

        final Stage currentStage = stage;

        // 2. Show logout toast feedback
        if (lblToastTitle != null) lblToastTitle.setText("LOGOUT SUCCESS");
        if (lblToastHeading != null) lblToastHeading.setText("Signed out");
        if (lblDashboardWelcome != null) lblDashboardWelcome.setText("You have been logged out successfully.");

        if (welcomeToast != null) {
            welcomeToast.setManaged(true);
            welcomeToast.setVisible(true);
        }
        if (welcomeTimer != null) welcomeTimer.stop();

        // 3. Delayed scene switch to Login window
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