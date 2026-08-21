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

    // ---- Sidebar ----
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

    // ---- Layout ----
    @FXML private StackPane contentPane;
    @FXML private ScrollPane dashboardScrollPane;
    @FXML private AnchorPane adminPane;

    // ---- Header ----
    @FXML private Label lblAdminName;

    // ---- Stat cards ----
    @FXML private Label lblTotalStudents;
    @FXML private Label lblTotalTeachers;
    @FXML private Label lblTotalClasses;
    @FXML private Label lblTotalSubjects;

    // ---- Attendance chart ----
    @FXML private ComboBox<String> cmbAttendancePeriod;
    @FXML private ComboBox<Batch> cmbAttendanceBatch;
    @FXML private PieChart attendancePieChart;

    // ---- Exam chart ----
    @FXML private ComboBox<ExamOption> cmbExam;
    @FXML private PieChart examPieChart;

    // ---- Students by batch ----
    @FXML private ComboBox<Batch> cmbStudentsBatch;
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> studentCodeColumn;
    @FXML private TableColumn<Student, String> studentNameColumn;
    @FXML private TableColumn<Student, String> studentEmailColumn;
    @FXML private TableColumn<Student, String> studentBatchColumn;
    @FXML private TableColumn<Student, String> studentStatusColumn;

    // ---- Leave table ----
    @FXML private ComboBox<Batch> cmbLeaveBatch;
    @FXML private TableView<LeaveRequest> leaveTable;
    @FXML private TableColumn<LeaveRequest, String> leaveStudentColumn;
    @FXML private TableColumn<LeaveRequest, String> leaveBatchColumn;
    @FXML private TableColumn<LeaveRequest, String> leaveFromColumn;
    @FXML private TableColumn<LeaveRequest, String> leaveToColumn;
    @FXML private TableColumn<LeaveRequest, String> leaveStatusColumn;

    // ---- DAOs ----
    private final AdminDAO adminDAO = new AdminDAO();
    private final StudentDao studentDAO = new StudentDao();
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final ExamDAO examDAO = new ExamDAO();
    private final LeaveDAO leaveDAO = new LeaveDAO();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    // "All Batches" sentinel used in both batch combo boxes
    // "All Batches" / "All Exams" sentinels used across the various dropdowns
    private static final Batch ALL_BATCHES = new Batch(-1, "All Batches");
    private static final ExamOption ALL_EXAMS = new ExamOption(-1, "All Exams");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupComboBoxes();
        refreshDashboard();
    }

    private void setupTableColumns() {
        studentCodeColumn.setCellValueFactory(new PropertyValueFactory<>("studentCode"));
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        studentEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        studentBatchColumn.setCellValueFactory(new PropertyValueFactory<>("batchName"));
        studentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        leaveStudentColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        leaveBatchColumn.setCellValueFactory(new PropertyValueFactory<>("batchName"));
        leaveStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        leaveFromColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getLeaveFrom() != null
                                ? cellData.getValue().getLeaveFrom().format(DATE_FMT) : ""));
        leaveToColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getLeaveTo() != null
                                ? cellData.getValue().getLeaveTo().format(DATE_FMT) : ""));
    }

    private void setupComboBoxes() {
        // Attendance period options
        cmbAttendancePeriod.setItems(FXCollections.observableArrayList(
                "This Week", "This Month", "This Year", "All Time"));
        cmbAttendancePeriod.getSelectionModel().select("This Month");
        cmbAttendancePeriod.setOnAction(e -> loadAttendanceChart());

        // Batches for attendance filter
        List<Batch> batches = classDAO.getAllBatches();
        ObservableList<Batch> attendanceBatchItems = FXCollections.observableArrayList();
        attendanceBatchItems.add(ALL_BATCHES);
        attendanceBatchItems.addAll(batches);
        cmbAttendanceBatch.setItems(attendanceBatchItems);
        cmbAttendanceBatch.getSelectionModel().select(ALL_BATCHES);
        cmbAttendanceBatch.setOnAction(e -> loadAttendanceChart());

        // Batches for "Students by Batch" table
        ObservableList<Batch> studentBatchItems = FXCollections.observableArrayList();
        studentBatchItems.add(ALL_BATCHES);
        studentBatchItems.addAll(batches);
        cmbStudentsBatch.setItems(studentBatchItems);
        cmbStudentsBatch.getSelectionModel().select(ALL_BATCHES);
        cmbStudentsBatch.setOnAction(e -> loadStudentsByBatch());

        // Batches for "Pending Leave Requests" filter
        ObservableList<Batch> leaveBatchItems = FXCollections.observableArrayList();
        leaveBatchItems.add(ALL_BATCHES);
        leaveBatchItems.addAll(batches);
        cmbLeaveBatch.setItems(leaveBatchItems);
        cmbLeaveBatch.getSelectionModel().select(ALL_BATCHES);
        cmbLeaveBatch.setOnAction(e -> loadLeaveTable());

        // Exams
        List<ExamOption> exams = examDAO.getAllExams();
        ObservableList<ExamOption> examItems = FXCollections.observableArrayList();
        examItems.add(ALL_EXAMS);
        examItems.addAll(exams);
        cmbExam.setItems(examItems);
        cmbExam.getSelectionModel().select(ALL_EXAMS);
        cmbExam.setOnAction(e -> loadExamChart());
    }

    // ===================================================================
    // ADMIN SESSION
    // ===================================================================

    /**
     * Call this from your LoginController right after a successful login, e.g.:
     *   AdminDashboardController controller = loader.getController();
     *   controller.setLoggedInAdmin(adminId);
     * This looks the admin up and fills in the "Kaung Min Khant" placeholder name.
     */
    public void setLoggedInAdmin(int adminId) {
        Admin admin = adminDAO.getAdminById(adminId);
        if (admin != null) {
            lblAdminName.setText(admin.getFullName());
        }
    }

    /** Alternative entry point if your session tracks username instead of an id. */
    public void setLoggedInAdmin(String username) {
        Admin admin = adminDAO.getAdminByUsername(username);
        if (admin != null) {
            lblAdminName.setText(admin.getFullName());
        }
    }

    // ===================================================================
    // DASHBOARD REFRESH
    // ===================================================================

    @FXML
    public void refreshDashboard(ActionEvent event) {
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
        lblTotalStudents.setText(String.valueOf(studentDAO.getTotalStudents()));
        lblTotalTeachers.setText(String.valueOf(teacherDAO.getTotalTeachers()));
        lblTotalClasses.setText(String.valueOf(classDAO.getTotalClasses()));
        lblTotalSubjects.setText(String.valueOf(subjectDAO.getTotalSubjects()));
    }

    private void loadAttendanceChart() {
        String period = cmbAttendancePeriod.getValue();
        Batch batch = cmbAttendanceBatch.getValue();
        int batchId = (batch == null) ? -1 : batch.getId();

        AttendanceSummary summary = attendanceDAO.getAttendanceSummary(period, batchId);

        int present = summary.getPresentCount();
        int absent = summary.getAbsentCount();
        int late = summary.getLateCount();

        ObservableList<PieChart.Data> data;
        if (present + absent + late == 0) {
            // No attendance records match this filter — show a single placeholder
            // slice instead of a broken/degenerate chart with three zero-value wedges.
            data = FXCollections.observableArrayList(new PieChart.Data("No Data", 1));
        } else {
            data = FXCollections.observableArrayList(
                    new PieChart.Data("Present", present),
                    new PieChart.Data("Absent", absent),
                    new PieChart.Data("Late", late)
            );
        }
        attendancePieChart.setData(data);
        attendancePieChart.setTitle(null);
        styleNoDataSliceIfPresent(attendancePieChart);
    }

    private void loadExamChart() {
        ExamOption exam = cmbExam.getValue();
        if (exam == null) {
            examPieChart.setData(FXCollections.observableArrayList(new PieChart.Data("No Exam Selected", 1)));
            examPieChart.setTitle(null);
            styleNoDataSliceIfPresent(examPieChart);
            return;
        }
        ExamResultSummary summary = examDAO.getExamResultSummary(exam.getId());

        int pass = summary.getPassCount();
        int fail = summary.getFailCount();

        ObservableList<PieChart.Data> data;
        if (pass + fail == 0) {
            // No grades recorded yet for this exam — show a placeholder slice instead
            // of two zero-value wedges (which JavaFX can't render as a proper circle).
            data = FXCollections.observableArrayList(new PieChart.Data("No Grades Yet", 1));
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

    /** Colors any "No Data"-style placeholder slice a neutral gray so it doesn't pick up a random palette color. */
    /** Colors any "No Data"-style placeholder slice a neutral gray so it doesn't pick up a random palette color. */
    private void styleNoDataSliceIfPresent(PieChart chart) {
        for (PieChart.Data d : chart.getData()) {
            if (d.getName() != null && d.getName().startsWith("No ")) {
                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-pie-color: #cfd6e3;");
                } else {
                    d.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            newNode.setStyle("-fx-pie-color: #cfd6e3;");
                        }
                    });
                }
            }
        }
    }

    private void loadStudentsByBatch() {
        Batch batch = cmbStudentsBatch.getValue();
        int batchId = (batch == null) ? -1 : batch.getId();

        List<Student> students = studentDAO.getStudentsByBatch(batchId);
        studentTable.setItems(FXCollections.observableArrayList(students));
    }

    private void loadLeaveTable() {
        Batch batch = cmbLeaveBatch.getValue();
        int batchId = (batch == null) ? -1 : batch.getId();

        List<LeaveRequest> pending = leaveDAO.getPendingLeaveRequestsByBatch(batchId);
        leaveTable.setItems(FXCollections.observableArrayList(pending));
    }

    // ===================================================================
    // SIDEBAR NAVIGATION — wire these to your existing page-loading logic
    // (e.g. swapping adminPane's content, or loading another FXML into it)
    // ===================================================================

    @FXML
    public void openStudents(ActionEvent event) {
        showAdminPane();
        // TODO: load Students.fxml into adminPane
    }

    @FXML
    public void openTeachers(ActionEvent event) {
        showAdminPane();
        // TODO: load Teachers.fxml into adminPane
    }

    @FXML
    public void openClasses(ActionEvent event) {
        showAdminPane();
        // TODO: load Classes.fxml into adminPane
    }

    @FXML
    public void openSubjects(ActionEvent event) {
        showAdminPane();
        // TODO: load Subjects.fxml into adminPane
    }

    @FXML
    public void openExams(ActionEvent event) {
        showAdminPane();
        // TODO: load Exams.fxml into adminPane
    }

    @FXML
    public void openGrades(ActionEvent event) {
        showAdminPane();
        // TODO: load Grades.fxml into adminPane
    }

    @FXML
    public void openAttendance(ActionEvent event) {
        showAdminPane();
        // TODO: load Attendance.fxml into adminPane
    }

    @FXML
    public void openLeaveRequests(ActionEvent event) {
        showAdminPane();
        // TODO: load LeaveRequests.fxml into adminPane
    }

    @FXML
    public void openAnnouncements(ActionEvent event) {
        showAdminPane();
        // TODO: load Announcements.fxml into adminPane
    }

    @FXML
    public void openProfile(ActionEvent event) {
        showAdminPane();
        // TODO: load Profile.fxml into adminPane
    }

    @FXML
    public void logout(ActionEvent event) {
        // TODO: clear session, navigate back to login screen
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
