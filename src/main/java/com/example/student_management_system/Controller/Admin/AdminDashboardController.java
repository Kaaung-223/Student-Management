package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.*;
import com.example.student_management_system.Controller.Model.*;

import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AdminDashboardController implements Initializable {

    @FXML
    private Button btnDashboard,
            btnStudents,
            btnTeachers,
            btnClasses,
            btnClassFees,
            btnSubjects,
            btnExams,
            btnGrades,
            btnAttendance,
            btnLeave,
            btnAnnouncements,
            btnProfile,
            btnLogout;

    @FXML
    private StackPane contentPane;

    @FXML
    private ScrollPane dashboardScrollPane;

    @FXML
    private AnchorPane adminPane;

    @FXML
    private Label lblAdminName,
            lblTotalStudents,
            lblTotalTeachers,
            lblTotalClasses,
            lblTotalSubjects,
            lblPassCount,
            lblFailCount,
            lblPresentCount,
            lblAbsentCount,
            lblLateCount,
            lblDashboardWelcome,
            lblFinanceExpected,
            lblFinanceCollected,
            lblFinanceOutstanding,
            lblFinancePaidUnpaid;

    @FXML
    private VBox welcomeToast;

    @FXML
    private ComboBox<String> cmbAttendancePeriod;

    @FXML
    private ComboBox<Batch> cmbAttendanceBatch,
            cmbStudentsBatch,
            cmbLeaveBatch,
            cmbFinanceBatch;

    @FXML
    private ComboBox<ExamOption> cmbExam;

    @FXML
    private PieChart attendancePieChart,
            examPieChart,
            financePieChart;

    @FXML
    private BarChart<String, Number> financeBarChart;

    @FXML
    private TableView<Student> studentTable;

    @FXML
    private TableColumn<Student, String> studentCodeColumn,
            studentNameColumn,
            studentEmailColumn,
            studentBatchColumn,
            studentStatusColumn;

    @FXML
    private TableView<LeaveRequest> leaveTable;

    @FXML
    private TableColumn<LeaveRequest, String> leaveStudentColumn,
            leaveBatchColumn,
            leaveFromColumn,
            leaveToColumn,
            leaveStatusColumn;


    // =========================================================
    // DAO
    // =========================================================

    private final AdminDAO adminDAO = new AdminDAO();

    private final StudentDao studentDAO = new StudentDao();

    private final TeacherDAO teacherDAO = new TeacherDAO();

    private final ClassDAO classDAO = new ClassDAO();

    private final SubjectDAO subjectDAO = new SubjectDAO();

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    private final ExamDAO examDAO = new ExamDAO();

    private final LeaveDAO leaveDAO = new LeaveDAO();

    private final FeeDAO feeDAO = new FeeDAO();


    // =========================================================
    // CONSTANTS
    // =========================================================

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final Batch ALL_BATCHES =
            new Batch(-1, "All Batches");

    private static final ExamOption ALL_EXAMS =
            new ExamOption(-1, "All Exams");

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


    // =========================================================
    // WELCOME TOAST TIMER
    // =========================================================

    private PauseTransition welcomeTimer;


    // =========================================================
    // INITIALIZE
    // =========================================================

    @Override
    public void initialize(URL u, ResourceBundle r) {

        setupTableColumns();

        setupComboBoxes();

        studentTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        leaveTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        setActiveButton(btnDashboard);

        refreshDashboard();
    }


    // =========================================================
    // TABLE COLUMNS
    // =========================================================

    private void setupTableColumns() {

        studentCodeColumn.setCellValueFactory(
                new PropertyValueFactory<>("studentCode")
        );

        studentNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("studentName")
        );

        studentEmailColumn.setCellValueFactory(
                new PropertyValueFactory<>("email")
        );

        studentBatchColumn.setCellValueFactory(
                new PropertyValueFactory<>("batchName")
        );

        studentStatusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );


        leaveStudentColumn.setCellValueFactory(
                new PropertyValueFactory<>("studentName")
        );

        leaveBatchColumn.setCellValueFactory(
                new PropertyValueFactory<>("batchName")
        );

        leaveStatusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );


        leaveFromColumn.setCellValueFactory(
                x -> new SimpleStringProperty(
                        x.getValue().getLeaveFrom() == null
                                ? ""
                                : x.getValue()
                                .getLeaveFrom()
                                .format(DATE_FMT)
                )
        );


        leaveToColumn.setCellValueFactory(
                x -> new SimpleStringProperty(
                        x.getValue().getLeaveTo() == null
                                ? ""
                                : x.getValue()
                                .getLeaveTo()
                                .format(DATE_FMT)
                )
        );
    }


    // =========================================================
    // COMBO BOXES
    // =========================================================

    private void setupComboBoxes() {

        cmbAttendancePeriod.setItems(
                FXCollections.observableArrayList(
                        "This Week",
                        "This Month",
                        "This Year",
                        "All Time"
                )
        );

        cmbAttendancePeriod.setValue("This Month");

        cmbAttendancePeriod.setOnAction(
                e -> loadAttendanceChart()
        );


        List<Batch> b = classDAO.getAllBatches();


        cmbAttendanceBatch.setItems(
                FXCollections.observableArrayList()
        );

        cmbAttendanceBatch.getItems().add(
                ALL_BATCHES
        );

        cmbAttendanceBatch.getItems().addAll(b);

        cmbAttendanceBatch.setValue(
                ALL_BATCHES
        );

        cmbAttendanceBatch.setOnAction(
                e -> loadAttendanceChart()
        );


        for (ComboBox<Batch> box :
                List.of(
                        cmbStudentsBatch,
                        cmbLeaveBatch,
                        cmbFinanceBatch
                )) {

            box.setItems(
                    FXCollections.observableArrayList()
            );

            box.getItems().add(
                    ALL_BATCHES
            );

            box.getItems().addAll(b);

            box.setValue(
                    ALL_BATCHES
            );
        }


        cmbStudentsBatch.setOnAction(
                e -> loadStudentsByBatch()
        );

        cmbLeaveBatch.setOnAction(
                e -> loadLeaveTable()
        );

        cmbFinanceBatch.setOnAction(
                e -> loadFinanceChart()
        );


        cmbExam.setItems(
                FXCollections.observableArrayList()
        );

        cmbExam.getItems().add(
                ALL_EXAMS
        );

        cmbExam.getItems().addAll(
                examDAO.getAllExams()
        );

        cmbExam.setValue(
                ALL_EXAMS
        );

        cmbExam.setOnAction(
                e -> loadExamChart()
        );
    }


    // =========================================================
    // SET LOGGED IN ADMIN - ID
    // =========================================================

    public void setLoggedInAdmin(int id) {

        Admin a = adminDAO.getAdminById(id);

        if (a != null) {

            lblAdminName.setText(
                    a.getFullName()
            );

            showWelcomeToast(
                    a.getFullName()
            );
        }
    }


    // =========================================================
    // SET LOGGED IN ADMIN - USERNAME
    // =========================================================

    public void setLoggedInAdmin(String username) {

        Admin a =
                adminDAO.getAdminByUsername(username);

        String name =
                a == null
                        ? username
                        : a.getFullName();


        lblAdminName.setText(name);

        showWelcomeToast(name);
    }


    // =========================================================
    // WELCOME TOAST
    // =========================================================

    // =========================================================
// WELCOME TOAST
// =========================================================

    private void showWelcomeToast(String name) {

        if (name == null || name.trim().isEmpty()) {
            name = "Administrator";
        }

        // =====================================================
        // TOAST SIZE
        // =====================================================
        // Prevent the welcome message from filling the whole page
        welcomeToast.setPrefWidth(420);
        welcomeToast.setMinWidth(420);
        welcomeToast.setMaxWidth(420);

        welcomeToast.setPrefHeight(125);
        welcomeToast.setMinHeight(125);
        welcomeToast.setMaxHeight(125);

        // =====================================================
        // WELCOME MESSAGE
        // =====================================================
        lblDashboardWelcome.setText(
                "Welcome, " + name +
                        ". You are signed in successfully."
        );

        // =====================================================
        // SHOW TOAST
        // =====================================================
        welcomeToast.setManaged(true);
        welcomeToast.setVisible(true);

        // =====================================================
        // STOP PREVIOUS TIMER
        // =====================================================
        if (welcomeTimer != null) {
            welcomeTimer.stop();
        }

        // =====================================================
        // 5 SECONDS TIMER
        // =====================================================
        welcomeTimer = new PauseTransition(
                Duration.seconds(5)
        );

        welcomeTimer.setOnFinished(e -> {

            welcomeToast.setVisible(false);
            welcomeToast.setManaged(false);

        });

        welcomeTimer.play();
    }


    // =========================================================
    // REFRESH DASHBOARD BUTTON
    // =========================================================

    @FXML
    public void refreshDashboard(ActionEvent e) {

        showDashboard();

        setActiveButton(btnDashboard);

        refreshDashboard();
    }


    // =========================================================
    // REFRESH DASHBOARD
    // =========================================================

    public void refreshDashboard() {

        loadStatCards();

        loadAttendanceChart();

        loadExamChart();

        loadFinanceChart();

        loadStudentsByBatch();

        loadLeaveTable();
    }


    // =========================================================
    // STAT CARDS
    // =========================================================

    private void loadStatCards() {

        lblTotalStudents.setText(
                "" + studentDAO.getTotalStudents()
        );

        lblTotalTeachers.setText(
                "" + teacherDAO.getTotalTeachers()
        );

        lblTotalClasses.setText(
                "" + classDAO.getTotalClasses()
        );

        lblTotalSubjects.setText(
                "" + subjectDAO.getTotalSubjects()
        );
    }


    // =========================================================
    // ATTENDANCE CHART
    // =========================================================

    private void loadAttendanceChart() {

        Batch b =
                cmbAttendanceBatch.getValue();


        AttendanceSummary s =
                attendanceDAO.getAttendanceSummary(
                        cmbAttendancePeriod.getValue(),
                        b == null ? -1 : b.getId()
                );


        int p = s.getPresentCount();

        int a = s.getAbsentCount();

        int l = s.getLateCount();


        lblPresentCount.setText(
                "" + p
        );

        lblAbsentCount.setText(
                "" + a
        );

        lblLateCount.setText(
                "" + l
        );


        if (p + a + l == 0) {

            attendancePieChart.setData(
                    FXCollections.observableArrayList(
                            new PieChart.Data(
                                    "No Data",
                                    1
                            )
                    )
            );

        } else {

            attendancePieChart.setData(
                    FXCollections.observableArrayList(
                            new PieChart.Data(
                                    "Present",
                                    p
                            ),

                            new PieChart.Data(
                                    "Absent",
                                    a
                            ),

                            new PieChart.Data(
                                    "Late",
                                    l
                            )
                    )
            );
        }


        styleNoData(
                attendancePieChart
        );
    }


    // =========================================================
    // EXAM CHART
    // =========================================================

    private void loadExamChart() {

        ExamOption e =
                cmbExam.getValue();


        ExamResultSummary s =
                examDAO.getExamResultSummary(
                        e == null
                                ? -1
                                : e.getId()
                );


        int p =
                s.getPassCount();

        int f =
                s.getFailCount();


        lblPassCount.setText(
                "" + p
        );

        lblFailCount.setText(
                "" + f
        );


        if (p + f == 0) {

            examPieChart.setData(
                    FXCollections.observableArrayList(
                            new PieChart.Data(
                                    "No Grades Yet",
                                    1
                            )
                    )
            );

        } else {

            examPieChart.setData(
                    FXCollections.observableArrayList(
                            new PieChart.Data(
                                    "Pass",
                                    p
                            ),

                            new PieChart.Data(
                                    "Fail",
                                    f
                            )
                    )
            );
        }


        styleNoData(
                examPieChart
        );
    }


    // =========================================================
    // FINANCIAL STATEMENT CHART
    // =========================================================

    private void loadFinanceChart() {

        Batch batch = cmbFinanceBatch.getValue();
        int classId = batch == null ? -1 : batch.getId();

        FeeFinancialSummary summary = feeDAO.getFinancialSummary(classId);

        lblFinanceExpected.setText(formatMoney(summary.getTotalExpected()) + " MMK");
        lblFinanceCollected.setText(formatMoney(summary.getTotalCollected()) + " MMK");
        lblFinanceOutstanding.setText(formatMoney(summary.getTotalOutstanding()) + " MMK");
        lblFinancePaidUnpaid.setText(
                summary.getPaidStudents() + " / " + summary.getUnpaidStudents()
        );

        double collected = toChartValue(summary.getTotalCollected());
        double outstanding = toChartValue(summary.getTotalOutstanding());

        if (collected + outstanding == 0) {
            financePieChart.setData(
                    FXCollections.observableArrayList(
                            new PieChart.Data("No Fee Data", 1)
                    )
            );
        } else {
            financePieChart.setData(
                    FXCollections.observableArrayList(
                            new PieChart.Data("Collected", collected),
                            new PieChart.Data("Outstanding", outstanding)
                    )
            );
        }

        styleFinancePie();

        XYChart.Series<String, Number> expectedSeries = new XYChart.Series<>();
        expectedSeries.setName("Expected");
        XYChart.Series<String, Number> collectedSeries = new XYChart.Series<>();
        collectedSeries.setName("Collected");
        XYChart.Series<String, Number> outstandingSeries = new XYChart.Series<>();
        outstandingSeries.setName("Outstanding");

        List<FeeClassBreakdown> breakdown = feeDAO.getFeeBreakdownByClass();
        if (classId != -1) {
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
                if (x == null) {
                    return;
                }
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
        if (amount == null) {
            return 0.0;
        }
        return amount.doubleValue();
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        return MONEY_FORMAT.format(amount);
    }


    // =========================================================
    // NO DATA PIE STYLE
    // =========================================================

    private void styleNoData(PieChart c) {

        for (PieChart.Data d : c.getData()) {

            if (d.getName().startsWith("No ")) {

                d.nodeProperty().addListener(
                        (o, n, x) -> {

                            if (x != null) {

                                x.setStyle(
                                        "-fx-pie-color:#cbd5e1;"
                                );
                            }
                        }
                );
            }
        }
    }


    // =========================================================
    // STUDENTS
    // =========================================================

    private void loadStudentsByBatch() {

        Batch b =
                cmbStudentsBatch.getValue();


        studentTable.setItems(
                FXCollections.observableArrayList(
                        studentDAO.getStudentsByBatch(
                                b == null
                                        ? -1
                                        : b.getId()
                        )
                )
        );
    }


    // =========================================================
    // LEAVE TABLE
    // =========================================================

    private void loadLeaveTable() {

        Batch b =
                cmbLeaveBatch.getValue();


        leaveTable.setItems(
                FXCollections.observableArrayList(
                        leaveDAO.getPendingLeaveRequestsByBatch(
                                b == null
                                        ? -1
                                        : b.getId()
                        )
                )
        );
    }


    // =========================================================
    // ACTIVE MENU
    // =========================================================

    private void setActiveButton(Button selected) {

        for (Button b :
                List.of(
                        btnDashboard,
                        btnStudents,
                        btnTeachers,
                        btnClasses,
                        btnClassFees,
                        btnSubjects,
                        btnExams,
                        btnGrades,
                        btnAttendance,

                        btnAnnouncements,
                        btnProfile
                )) {

            b.setStyle(
                    b == selected
                            ? ACTIVE
                            : NORMAL
            );
        }
    }


    // =========================================================
    // SHOW ADMIN PAGE
    // =========================================================

    private void showAdminPane() {

        dashboardScrollPane.setVisible(false);

        adminPane.setVisible(true);
    }


    // =========================================================
    // SHOW DASHBOARD
    // =========================================================

    private void showDashboard() {

        adminPane.setVisible(false);

        dashboardScrollPane.setVisible(true);
    }


    // =========================================================
    // STUDENTS
    // =========================================================

    public void openStudents(ActionEvent event) {
        try {
            setActiveButton(btnStudents);
            showAdminPane();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Admin/AdminStudent.fxml"
            ));

            Parent studentPage = loader.load();

            AnchorPane.setTopAnchor(studentPage, 0.0);
            AnchorPane.setRightAnchor(studentPage, 0.0);
            AnchorPane.setBottomAnchor(studentPage, 0.0);
            AnchorPane.setLeftAnchor(studentPage, 0.0);

            adminPane.getChildren().setAll(studentPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // =========================================================
    // TEACHERS
    // =========================================================

    @FXML
    public void openTeachers(ActionEvent e) {
        try {
            setActiveButton(btnTeachers);
            showAdminPane();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Admin/AdminTeacher.fxml"
            ));

            Parent teacherPage = loader.load();

            AnchorPane.setTopAnchor(teacherPage, 0.0);
            AnchorPane.setRightAnchor(teacherPage, 0.0);
            AnchorPane.setBottomAnchor(teacherPage, 0.0);
            AnchorPane.setLeftAnchor(teacherPage, 0.0);

            adminPane.getChildren().setAll(teacherPage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    // =========================================================
    // CLASSES
    // =========================================================

    @FXML
    public void openClasses(ActionEvent e) {
        try {
            setActiveButton(btnClasses);
            showAdminPane();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Admin/AdminClass.fxml"
            ));

            Parent classPage = loader.load();

            AnchorPane.setTopAnchor(classPage, 0.0);
            AnchorPane.setRightAnchor(classPage, 0.0);
            AnchorPane.setBottomAnchor(classPage, 0.0);
            AnchorPane.setLeftAnchor(classPage, 0.0);

            adminPane.getChildren().setAll(classPage);
        } catch (Exception ec) {
            ec.printStackTrace();
        }
    }


    // =========================================================
    // CLASS FEES
    // =========================================================

    @FXML
    public void openClassFees(ActionEvent e) {

        try {
            setActiveButton(btnClassFees);
            showAdminPane();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Admin/AdminClassFee.fxml"
            ));

            Parent classFeePage = loader.load();

            AnchorPane.setTopAnchor(classFeePage, 0.0);
            AnchorPane.setRightAnchor(classFeePage, 0.0);
            AnchorPane.setBottomAnchor(classFeePage, 0.0);
            AnchorPane.setLeftAnchor(classFeePage, 0.0);

            adminPane.getChildren().setAll(classFeePage);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    // =========================================================
    // SUBJECTS
    // =========================================================

    @FXML
    public void openSubjects(ActionEvent e) {

        try {
            setActiveButton(btnSubjects);
            showAdminPane();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Admin/AdminSubject.fxml"
            ));

            Parent subjectPage = loader.load();

            AnchorPane.setTopAnchor(subjectPage, 0.0);
            AnchorPane.setRightAnchor(subjectPage, 0.0);
            AnchorPane.setBottomAnchor(subjectPage, 0.0);
            AnchorPane.setLeftAnchor(subjectPage, 0.0);

            adminPane.getChildren().setAll(subjectPage);
        } catch (Exception ec) {
            ec.printStackTrace();
        }
    }


    // =========================================================
    // EXAMS
    // =========================================================

    @FXML
    public void openExams(ActionEvent e) {

        try {
            setActiveButton(btnExams);
            showAdminPane();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Admin/AdminExam.fxml"
            ));

            Parent examPage = loader.load();

            AnchorPane.setTopAnchor(examPage, 0.0);
            AnchorPane.setRightAnchor(examPage, 0.0);
            AnchorPane.setBottomAnchor(examPage, 0.0);
            AnchorPane.setLeftAnchor(examPage, 0.0);

            adminPane.getChildren().setAll(examPage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    // =========================================================
    // GRADES
    // =========================================================

    @FXML
    public void openGrades(ActionEvent e) {

        try {
            setActiveButton(btnGrades);
            showAdminPane();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Admin/AdminGrade.fxml"
            ));

            Parent gradePage = loader.load();

            AnchorPane.setTopAnchor(gradePage, 0.0);
            AnchorPane.setRightAnchor(gradePage, 0.0);
            AnchorPane.setBottomAnchor(gradePage, 0.0);
            AnchorPane.setLeftAnchor(gradePage, 0.0);

            adminPane.getChildren().setAll(gradePage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    // =========================================================
    // ATTENDANCE
    // =========================================================

    @FXML
    public void openAttendance(ActionEvent e) {

        try {
            setActiveButton(btnAttendance);
            showAdminPane();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Admin/AdminAttendance.fxml"
            ));

            Parent attendancePage = loader.load();

            AnchorPane.setTopAnchor(attendancePage, 0.0);
            AnchorPane.setRightAnchor(attendancePage, 0.0);
            AnchorPane.setBottomAnchor(attendancePage, 0.0);
            AnchorPane.setLeftAnchor(attendancePage, 0.0);

            adminPane.getChildren().setAll(attendancePage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    // =========================================================
    // LEAVE
    // =========================================================




    // =========================================================
    // ANNOUNCEMENTS
    // =========================================================

    @FXML
    public void openAnnouncements(ActionEvent e) {

        setActiveButton(
                btnAnnouncements
        );

        showAdminPane();
    }


    // =========================================================
    // PROFILE
    // =========================================================

    @FXML
    public void openProfile(ActionEvent e) {

        setActiveButton(
                btnProfile
        );

        showAdminPane();
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    @FXML
    public void logout(ActionEvent e) {

    }
}
