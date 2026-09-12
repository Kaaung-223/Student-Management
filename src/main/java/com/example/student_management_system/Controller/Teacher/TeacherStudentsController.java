package com.example.student_management_system.Controller.Teacher;

import com.example.student_management_system.Controller.DAO.TeacherStudentsDAO;
import com.example.student_management_system.Controller.Model.BatchFilter;
import com.example.student_management_system.Controller.Model.PerformancePeriod;
import com.example.student_management_system.Controller.Model.TeacherInfo;
import com.example.student_management_system.Controller.Model.TeacherStudent;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class TeacherStudentsController implements Initializable {

    // Filter row
    @FXML private ComboBox<BatchFilter> cmbBatch;
    @FXML private TextField txtSearch;

    // Table
    @FXML private TableView<TeacherStudent> studentTable;
    @FXML private TableColumn<TeacherStudent, String> colCode, colName, colBatch, colEmail, colStatus;
    @FXML private Label lblStudentCount;

    // Profile card
    @FXML private Label lblAvatar, lblStudentName, lblStudentCode, lblBatchName,
            lblContact, lblPhone, lblAdmission,
            lblPresent, lblAbsent, lblLate, lblPass, lblFail, lblLeave,
            lblLatestResult;

    // Performance
    @FXML private ComboBox<PerformancePeriod> cmbPerformancePeriod;
    @FXML private Label lblAttendanceRate, lblGpa, lblPerformancePeriod;

    private final TeacherStudentsDAO dao = new TeacherStudentsDAO();
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private TeacherInfo teacherInfo;
    private TeacherStudent selectedStudent;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colCode.setCellValueFactory(new PropertyValueFactory<>("studentCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colBatch.setCellValueFactory(new PropertyValueFactory<>("batchName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        cmbBatch.setOnAction(e -> refreshStudents());
        txtSearch.textProperty().addListener((o, a, b) -> searchAfterTyping());
        studentTable.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> showProfile(b));
        cmbPerformancePeriod.setOnAction(event -> loadPerformance());
    }

    public void setTeacherInfo(TeacherInfo info) {
        this.teacherInfo = info;
        loadBatches();
        refreshStudents();
    }

    private void loadBatches() {
        if (teacherInfo == null) return;
        cmbBatch.getItems().clear();
        cmbBatch.getItems().add(new BatchFilter(-1, "All Batches"));
        cmbBatch.getItems().addAll(dao.findTeacherBatches(teacherInfo.getTeacherId()));
        cmbBatch.getSelectionModel().selectFirst();
    }

    private void searchAfterTyping() {
        PauseTransition d = new PauseTransition(Duration.millis(250));
        d.setOnFinished(e -> refreshStudents());
        d.play();
    }

    @FXML
    public void refreshStudents() {
        if (teacherInfo == null) return;
        BatchFilter b = cmbBatch.getValue();

        studentTable.setItems(FXCollections.observableArrayList(
                dao.findStudents(
                        teacherInfo.getTeacherId(),
                        b == null ? -1 : b.getId(),
                        txtSearch.getText())));

        lblStudentCount.setText(studentTable.getItems().size() + " student(s)");

        if (!studentTable.getItems().isEmpty()) studentTable.getSelectionModel().selectFirst();
        else                                    clearProfile();
    }

    private void loadPerformance() {
        if (selectedStudent == null) return;

        PerformancePeriod period = cmbPerformancePeriod.getValue();
        if (period == null) return;

        double[] result = dao.getStudentPerformance(
                selectedStudent.getStudentId(),
                period.getYear(),
                period.getMonth());

        lblPerformancePeriod.setText("Performance: " + period);
        lblAttendanceRate.setText(String.format("%.1f%%", result[0]));
        lblGpa.setText(String.format("%.2f", result[1]));
    }

    private void showProfile(TeacherStudent student) {
        if (student == null) {
            clearProfile();
            return;
        }
        selectedStudent = student;

        lblAvatar.setText(student.getInitials());
        lblStudentName.setText(student.getStudentName());
        lblStudentCode.setText(student.getStudentCode() + "  |  " + value(student.getStatus()));

        lblBatchName.setText("Batch: " + value(student.getBatchName()));
        lblContact.setText("Email: " + value(student.getEmail()));
        lblPhone.setText("Phone: " + value(student.getPhone()));

        lblAdmission.setText("Admission: " +
                (student.getAdmissionDate() == null
                        ? "--"
                        : student.getAdmissionDate().format(DATE)));

        lblPresent.setText(String.valueOf(student.getPresentCount()));
        lblAbsent.setText(String.valueOf(student.getAbsentCount()));
        lblLate.setText(String.valueOf(student.getLateCount()));
        lblPass.setText(String.valueOf(student.getPassCount()));
        lblFail.setText(String.valueOf(student.getFailCount()));
        lblLeave.setText(String.valueOf(student.getLeaveCount()));

        lblLatestResult.setText("Latest exam result: " + value(student.getLatestResult()));

        cmbPerformancePeriod.setItems(FXCollections.observableArrayList(
                dao.getPerformancePeriods(student.getStudentId())));
        cmbPerformancePeriod.getSelectionModel().selectFirst();

        loadPerformance();
    }

    private String value(String v) {
        return (v == null || v.isBlank()) ? "--" : v;
    }

    private void clearProfile() {
        selectedStudent = null;

        lblAvatar.setText("--");
        lblStudentName.setText("No student found");
        lblStudentCode.setText("Student profile");
        lblBatchName.setText("Batch: --");
        lblContact.setText("Email: --");
        lblPhone.setText("Phone: --");
        lblAdmission.setText("Admission: --");
        lblPresent.setText("0");
        lblAbsent.setText("0");
        lblLate.setText("0");
        lblPass.setText("0");
        lblFail.setText("0");
        lblLeave.setText("0");
        lblLatestResult.setText("Latest exam result: --");
        lblAttendanceRate.setText("0.0%");
        lblGpa.setText("0.00");
        lblPerformancePeriod.setText("Performance: --");
        cmbPerformancePeriod.getItems().clear();
    }
}