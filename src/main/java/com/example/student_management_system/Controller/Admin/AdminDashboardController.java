package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.AdminDAO;
import com.example.student_management_system.Controller.DAO.AttendanceDAO;
import com.example.student_management_system.Controller.DAO.ClassDAO;
import com.example.student_management_system.Controller.DAO.ExamDAO;
import com.example.student_management_system.Controller.DAO.LeaveDAO;
import com.example.student_management_system.Controller.DAO.StudentDao;
import com.example.student_management_system.Controller.DAO.SubjectDAO;
import com.example.student_management_system.Controller.DAO.TeacherDAO;
import com.example.student_management_system.Controller.Model.Admin;
import com.example.student_management_system.Controller.Model.AttendanceSummary;
import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.ExamOption;
import com.example.student_management_system.Controller.Model.ExamResultSummary;
import com.example.student_management_system.Controller.Model.LeaveRequest;
import com.example.student_management_system.Controller.Model.Student;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

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

    @FXML private StackPane contentPane;
    @FXML private ScrollPane dashboardScrollPane;
    @FXML private AnchorPane adminPane;

    @FXML private Label lblAdminName;

    @FXML private Label lblTotalStudents;
    @FXML private Label lblTotalTeachers;
    @FXML private Label lblTotalClasses;
    @FXML private Label lblTotalSubjects;

    @FXML private ComboBox<String> cmbAttendancePeriod;
    @FXML private ComboBox<Batch> cmbAttendanceBatch;
    @FXML private PieChart attendancePieChart;

    @FXML private ComboBox<ExamOption> cmbExam;
    @FXML private PieChart examPieChart;
    @FXML private Label lblPassCount;
    @FXML private Label lblFailCount;

    @FXML private ComboBox<Batch> cmbStudentsBatch;
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> studentCodeColumn;
    @FXML private TableColumn<Student, String> studentNameColumn;
    @FXML private TableColumn<Student, String> studentEmailColumn;
    @FXML private TableColumn<Student, String> studentBatchColumn;
    @FXML private TableColumn<Student, String> studentStatusColumn;

    @FXML private ComboBox<Batch> cmbLeaveBatch;
    @FXML private TableView<LeaveRequest> leaveTable;
    @FXML private TableColumn<LeaveRequest, String> leaveStudentColumn;
    @FXML private TableColumn<LeaveRequest, String> leaveBatchColumn;
    @FXML private TableColumn<LeaveRequest, String> leaveFromColumn;
    @FXML private TableColumn<LeaveRequest, String> leaveToColumn;
    @FXML private TableColumn<LeaveRequest, String> leaveStatusColumn;
    // Add these fields in AdminDashboardController.java

    @FXML private Label lblPresentCount;
    @FXML private Label lblAbsentCount;
    @FXML private Label lblLateCount;
    private final AdminDAO adminDAO = new AdminDAO();
    private final StudentDao studentDAO = new StudentDao();
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final ExamDAO examDAO = new ExamDAO();
    private final LeaveDAO leaveDAO = new LeaveDAO();

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final Batch ALL_BATCHES =
            new Batch(-1, "All Batches");

    private static final ExamOption ALL_EXAMS =
            new ExamOption(-1, "All Exams");

    private static final String ACTIVE_MENU_STYLE =
            "-fx-background-color: #4f46e5; " +
                    "-fx-background-radius: 10; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 13px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-alignment: CENTER_LEFT; " +
                    "-fx-padding: 0 16; " +
                    "-fx-cursor: hand;";

    private static final String NORMAL_MENU_STYLE =
            "-fx-background-color: transparent; " +
                    "-fx-background-radius: 10; " +
                    "-fx-text-fill: #cbd5e1; " +
                    "-fx-font-size: 13px; " +
                    "-fx-alignment: CENTER_LEFT; " +
                    "-fx-padding: 0 16; " +
                    "-fx-cursor: hand;";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupComboBoxes();
        setActiveButton(btnDashboard);
        refreshDashboard();
    }

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

        leaveFromColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getLeaveFrom() != null
                                ? cellData.getValue().getLeaveFrom().format(DATE_FMT)
                                : ""
                )
        );

        leaveToColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getLeaveTo() != null
                                ? cellData.getValue().getLeaveTo().format(DATE_FMT)
                                : ""
                )
        );
    }

    private void setupComboBoxes() {
        cmbAttendancePeriod.setItems(
                FXCollections.observableArrayList(
                        "This Week",
                        "This Month",
                        "This Year",
                        "All Time"
                )
        );

        cmbAttendancePeriod.getSelectionModel().select("This Month");
        cmbAttendancePeriod.setOnAction(event -> loadAttendanceChart());

        List<Batch> batches = classDAO.getAllBatches();

        ObservableList<Batch> attendanceBatchItems =
                FXCollections.observableArrayList();

        attendanceBatchItems.add(ALL_BATCHES);
        attendanceBatchItems.addAll(batches);

        cmbAttendanceBatch.setItems(attendanceBatchItems);
        cmbAttendanceBatch.getSelectionModel().select(ALL_BATCHES);
        cmbAttendanceBatch.setOnAction(event -> loadAttendanceChart());

        ObservableList<Batch> studentBatchItems =
                FXCollections.observableArrayList();

        studentBatchItems.add(ALL_BATCHES);
        studentBatchItems.addAll(batches);

        cmbStudentsBatch.setItems(studentBatchItems);
        cmbStudentsBatch.getSelectionModel().select(ALL_BATCHES);
        cmbStudentsBatch.setOnAction(event -> loadStudentsByBatch());

        ObservableList<Batch> leaveBatchItems =
                FXCollections.observableArrayList();

        leaveBatchItems.add(ALL_BATCHES);
        leaveBatchItems.addAll(batches);

        cmbLeaveBatch.setItems(leaveBatchItems);
        cmbLeaveBatch.getSelectionModel().select(ALL_BATCHES);
        cmbLeaveBatch.setOnAction(event -> loadLeaveTable());

        List<ExamOption> exams = examDAO.getAllExams();

        ObservableList<ExamOption> examItems =
                FXCollections.observableArrayList();

        examItems.add(ALL_EXAMS);
        examItems.addAll(exams);

        cmbExam.setItems(examItems);
        cmbExam.getSelectionModel().select(ALL_EXAMS);
        cmbExam.setOnAction(event -> loadExamChart());
    }

    public void setLoggedInAdmin(int adminId) {
        Admin admin = adminDAO.getAdminById(adminId);

        if (admin != null) {
            lblAdminName.setText(admin.getFullName());
        }
    }

    public void setLoggedInAdmin(String username) {
        Admin admin = adminDAO.getAdminByUsername(username);

        if (admin != null) {
            lblAdminName.setText(admin.getFullName());
        }
    }

    @FXML
    public void refreshDashboard(ActionEvent event) {
        showDashboard();
        setActiveButton(btnDashboard);
        refreshDashboard();
    }

    public void refreshDashboard() {
        loadStatCards();
        loadAttendanceChart();
        loadExamChart();
        loadStudentsByBatch();
        loadLeaveTable();
    }

    private void loadStatCards() {
        lblTotalStudents.setText(
                String.valueOf(studentDAO.getTotalStudents())
        );

        lblTotalTeachers.setText(
                String.valueOf(teacherDAO.getTotalTeachers())
        );

        lblTotalClasses.setText(
                String.valueOf(classDAO.getTotalClasses())
        );

        lblTotalSubjects.setText(
                String.valueOf(subjectDAO.getTotalSubjects())
        );
    }

    private void loadAttendanceChart() {
        String period = cmbAttendancePeriod.getValue();
        Batch batch = cmbAttendanceBatch.getValue();

        int batchId = batch == null ? -1 : batch.getId();

        AttendanceSummary summary =
                attendanceDAO.getAttendanceSummary(period, batchId);

        int present = summary.getPresentCount();
        int absent = summary.getAbsentCount();
        int late = summary.getLateCount();

        ObservableList<PieChart.Data> data;

        if (present + absent + late == 0) {
            data = FXCollections.observableArrayList(
                    new PieChart.Data("No Data", 1)
            );
        } else {
            data = FXCollections.observableArrayList(
                    new PieChart.Data("Present", present),
                    new PieChart.Data("Absent", absent),
                    new PieChart.Data("Late", late)
            );
        }

        attendancePieChart.setData(data);
        attendancePieChart.setTitle(null);
        // Add these lines inside loadAttendanceChart(), after present, absent, and late are declared

        lblPresentCount.setText(String.valueOf(present));
        lblAbsentCount.setText(String.valueOf(absent));
        lblLateCount.setText(String.valueOf(late));
        styleNoDataSliceIfPresent(attendancePieChart);
    }

    private void loadExamChart() {
        ExamOption exam = cmbExam.getValue();

        if (exam == null) {
            lblPassCount.setText("0");
            lblFailCount.setText("0");

            examPieChart.setData(
                    FXCollections.observableArrayList(
                            new PieChart.Data("No Exam Selected", 1)
                    )
            );

            examPieChart.setTitle(null);
            styleNoDataSliceIfPresent(examPieChart);
            return;
        }

        ExamResultSummary summary =
                examDAO.getExamResultSummary(exam.getId());

        int pass = summary.getPassCount();
        int fail = summary.getFailCount();

        lblPassCount.setText(String.valueOf(pass));
        lblFailCount.setText(String.valueOf(fail));

        ObservableList<PieChart.Data> data;

        if (pass + fail == 0) {
            data = FXCollections.observableArrayList(
                    new PieChart.Data("No Grades Yet", 1)
            );
        } else {
            data = FXCollections.observableArrayList(
                    new PieChart.Data("Pass", pass),
                    new PieChart.Data("Fail", fail)
            );
        }

        examPieChart.setData(data);
        examPieChart.setTitle(null);

        styleNoDataSliceIfPresent(examPieChart);
    }

    private void styleNoDataSliceIfPresent(PieChart chart) {
        for (PieChart.Data data : chart.getData()) {
            if (data.getName() != null && data.getName().startsWith("No ")) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-pie-color: #cbd5e1;");
                } else {
                    data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            newNode.setStyle("-fx-pie-color: #cbd5e1;");
                        }
                    });
                }
            }
        }
    }

    private void loadStudentsByBatch() {
        Batch batch = cmbStudentsBatch.getValue();
        int batchId = batch == null ? -1 : batch.getId();

        List<Student> students =
                studentDAO.getStudentsByBatch(batchId);

        studentTable.setItems(
                FXCollections.observableArrayList(students)
        );
    }

    private void loadLeaveTable() {
        Batch batch = cmbLeaveBatch.getValue();
        int batchId = batch == null ? -1 : batch.getId();

        List<LeaveRequest> pending =
                leaveDAO.getPendingLeaveRequestsByBatch(batchId);

        leaveTable.setItems(
                FXCollections.observableArrayList(pending)
        );
    }

    private void setActiveButton(Button selectedButton) {
        Button[] menuButtons = {
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

        for (Button button : menuButtons) {
            if (button == selectedButton) {
                button.setStyle(ACTIVE_MENU_STYLE);
            } else {
                button.setStyle(NORMAL_MENU_STYLE);
            }
        }
    }

    @FXML
    public void openStudents(ActionEvent event) {
        setActiveButton(btnStudents);
        showAdminPane();
        // TODO: load Students.fxml into adminPane
    }

    @FXML
    public void openTeachers(ActionEvent event) {
        setActiveButton(btnTeachers);
        showAdminPane();
        // TODO: load Teachers.fxml into adminPane
    }

    @FXML
    public void openClasses(ActionEvent event) {
        setActiveButton(btnClasses);
        showAdminPane();
        // TODO: load Classes.fxml into adminPane
    }

    @FXML
    public void openSubjects(ActionEvent event) {
        setActiveButton(btnSubjects);
        showAdminPane();
        // TODO: load Subjects.fxml into adminPane
    }

    @FXML
    public void openExams(ActionEvent event) {
        setActiveButton(btnExams);
        showAdminPane();
        // TODO: load Exams.fxml into adminPane
    }

    @FXML
    public void openGrades(ActionEvent event) {
        setActiveButton(btnGrades);
        showAdminPane();
        // TODO: load Grades.fxml into adminPane
    }

    @FXML
    public void openAttendance(ActionEvent event) {
        setActiveButton(btnAttendance);
        showAdminPane();
        // TODO: load Attendance.fxml into adminPane
    }

    @FXML
    public void openLeaveRequests(ActionEvent event) {
        setActiveButton(btnLeave);
        showAdminPane();
        // TODO: load LeaveRequests.fxml into adminPane
    }

    @FXML
    public void openAnnouncements(ActionEvent event) {
        setActiveButton(btnAnnouncements);
        showAdminPane();
        // TODO: load Announcements.fxml into adminPane
    }

    @FXML
    public void openProfile(ActionEvent event) {
        setActiveButton(btnProfile);
        showAdminPane();
        // TODO: load Profile.fxml into adminPane
    }

    @FXML
    public void logout(ActionEvent event) {
        // TODO: clear session and navigate back to login screen
    }

    private void showAdminPane() {
        dashboardScrollPane.setVisible(false);
        adminPane.setVisible(true);
    }

    private void showDashboard() {
        adminPane.setVisible(false);
        dashboardScrollPane.setVisible(true);
    }
}