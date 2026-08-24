package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.DBConnention;
import com.example.student_management_system.Controller.DAO.StudentProfileDAO;
import com.example.student_management_system.Controller.Model.BatchFilter;
import com.example.student_management_system.Controller.Model.PerformancePeriod;
import com.example.student_management_system.Controller.Model.StudentProfile;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.net.URL;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import com.example.student_management_system.Controller.Model.PerformancePeriod;
import com.example.student_management_system.Controller.Model.StudentPerformance;

public class AdminStudentsController implements Initializable {
    @FXML private ComboBox<PerformancePeriod> cmbPerformancePeriod;
    @FXML private Label lblAttendanceRate;
    @FXML private Label lblGpa;
    @FXML private Label lblPerformancePeriod;

    private StudentProfile selectedStudent;
    @FXML
    private ComboBox<BatchFilter> cmbBatch;
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<StudentProfile> studentTable;
    @FXML
    private TableColumn<StudentProfile, String> colCode, colName, colBatch, colEmail, colStatus;
    @FXML
    private Label lblStudentCount, lblAvatar, lblStudentName, lblStudentCode, lblBatchName, lblContact, lblPhone, lblAdmission, lblPresent, lblAbsent, lblLate, lblPass, lblFail, lblLeave, lblLatestResult;
    private final StudentProfileDAO dao = new StudentProfileDAO();
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colCode.setCellValueFactory(new PropertyValueFactory<>("studentCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colBatch.setCellValueFactory(new PropertyValueFactory<>("batchName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        loadBatches();
        cmbBatch.setOnAction(e -> refreshStudents());
        txtSearch.textProperty().addListener((o, a, b) -> searchAfterTyping());
        studentTable.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> showProfile(b));
        cmbPerformancePeriod.setOnAction(event -> loadPerformance());
        refreshStudents();

    }

    private void loadBatches() {
        cmbBatch.getItems().add(new BatchFilter(-1, "All Batches"));
        try (Connection con = DBConnention.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT class_id,class_name FROM classes ORDER BY class_name"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cmbBatch.getItems().add(new BatchFilter(rs.getInt(1), rs.getString(2)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        cmbBatch.getSelectionModel().selectFirst();
    }

    private void searchAfterTyping() {
        PauseTransition d = new PauseTransition(Duration.millis(250));
        d.setOnFinished(e -> refreshStudents());
        d.play();
    }

    @FXML
    public void refreshStudents() {
        BatchFilter b = cmbBatch.getValue();
        studentTable.setItems(FXCollections.observableArrayList(dao.findStudents(b == null ? -1 : b.getId(), txtSearch.getText())));
        lblStudentCount.setText(studentTable.getItems().size() + " student(s)");
        if (!studentTable.getItems().isEmpty()) studentTable.getSelectionModel().selectFirst();
        else clearProfile();
    }

    private void loadPerformance() {
        if (selectedStudent == null) {
            return;
        }

        PerformancePeriod period = cmbPerformancePeriod.getValue();

        if (period == null) {
            return;
        }

        StudentPerformance performance =
                dao.getStudentPerformance(
                        selectedStudent.getStudentId(),
                        period.getYear(),
                        period.getMonth()
                );

        lblPerformancePeriod.setText("Performance: " + period);

        lblAttendanceRate.setText(
                String.format("%.1f%%", performance.getAttendanceRate())
        );

        lblGpa.setText(
                String.format("%.2f", performance.getGpa())
        );
    }

    private void showProfile(StudentProfile student) {
        if (student == null) {
            clearProfile();
            return;
        }

        selectedStudent = student;

        lblAvatar.setText(student.getInitials());
        lblStudentName.setText(student.getStudentName());
        lblStudentCode.setText(student.getStudentCode() + "  |  " + student.getStatus());

        lblBatchName.setText("Batch: " + value(student.getBatchName()));
        lblContact.setText("Email: " + value(student.getEmail()));
        lblPhone.setText("Phone: " + value(student.getPhone()));

        lblAdmission.setText(
                "Admission: " +
                        (student.getAdmissionDate() == null
                                ? "--"
                                : student.getAdmissionDate().format(DATE))
        );

        lblPresent.setText(String.valueOf(student.getPresentCount()));
        lblAbsent.setText(String.valueOf(student.getAbsentCount()));
        lblLate.setText(String.valueOf(student.getLateCount()));
        lblPass.setText(String.valueOf(student.getPassCount()));
        lblFail.setText(String.valueOf(student.getFailCount()));
        lblLeave.setText(String.valueOf(student.getLeaveCount()));

        lblLatestResult.setText(
                "Latest exam result: " + value(student.getLatestResult())
        );

        cmbPerformancePeriod.setItems(
                FXCollections.observableArrayList(
                        dao.getPerformancePeriods(student.getStudentId())
                )
        );

        cmbPerformancePeriod.getSelectionModel().selectFirst();

        loadPerformance();
    }

    private String value(String v) {
        return v == null || v.isBlank() ? "--" : v;
    }

    private void clearProfile() {
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
    }
}