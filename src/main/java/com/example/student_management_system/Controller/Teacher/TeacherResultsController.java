package com.example.student_management_system.Controller.Teacher;

import com.example.student_management_system.Controller.DAO.TeacherResultsDAO;
import com.example.student_management_system.Controller.Model.BatchFilter;
import com.example.student_management_system.Controller.Model.ExamResultRow;
import com.example.student_management_system.Controller.Model.SubjectResult;
import com.example.student_management_system.Controller.Model.TeacherInfo;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TeacherResultsController implements Initializable {

    // Filters
    @FXML private ComboBox<BatchFilter> cmbExam;
    @FXML private ComboBox<BatchFilter> cmbBatch;
    @FXML private TextField txtSearch;

    // Main table
    @FXML private TableView<ExamResultRow> resultTable;
    @FXML private TableColumn<ExamResultRow, String>  colCode;
    @FXML private TableColumn<ExamResultRow, String>  colName;
    @FXML private TableColumn<ExamResultRow, String>  colBatch;
    @FXML private TableColumn<ExamResultRow, Integer> colSubjects;
    @FXML private TableColumn<ExamResultRow, Double>  colAvg;
    @FXML private TableColumn<ExamResultRow, Double>  colGpa;
    @FXML private TableColumn<ExamResultRow, String>  colResult;
    @FXML private Label lblStudentCount;

    // Profile
    @FXML private Label lblAvatar;
    @FXML private Label lblStudentName;
    @FXML private Label lblStudentCode;
    @FXML private Label lblBatch;

    // Summary cards
    @FXML private Label lblTotalStudents;
    @FXML private Label lblTotalPass;
    @FXML private Label lblTotalFail;
    @FXML private Label lblPassRate;
    @FXML private Label lblAvgGpa;
    @FXML private Label lblAvgPercent;

    // Subject marks
    @FXML private TableView<SubjectResult> subjectTable;
    @FXML private TableColumn<SubjectResult, String>  colSubject;
    @FXML private TableColumn<SubjectResult, Integer> colMarks;
    @FXML private TableColumn<SubjectResult, Double>  colPercent;
    @FXML private TableColumn<SubjectResult, String>  colGrade;
    @FXML private TableColumn<SubjectResult, String>  colMarkResult;

    private final TeacherResultsDAO dao = new TeacherResultsDAO();
    private TeacherInfo teacherInfo;
    private ExamResultRow selectedStudent;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // ===== Main table =====
        colCode.setCellValueFactory(new PropertyValueFactory<>("studentCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colBatch.setCellValueFactory(new PropertyValueFactory<>("batchName"));
        colSubjects.setCellValueFactory(new PropertyValueFactory<>("subjectCount"));
        colAvg.setCellValueFactory(new PropertyValueFactory<>("avgPercentage"));
        colGpa.setCellValueFactory(new PropertyValueFactory<>("gpa"));
        colResult.setCellValueFactory(new PropertyValueFactory<>("overallResult"));

        colAvg.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%.1f%%", v));
            }
        });
        colGpa.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%.2f", v));
            }
        });

        resultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        resultTable.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> showStudent(b));

        // ===== Subject table =====
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        colMarks.setCellValueFactory(new PropertyValueFactory<>("obtainedMarks"));
        colPercent.setCellValueFactory(new PropertyValueFactory<>("percentage"));
        colGrade.setCellValueFactory(new PropertyValueFactory<>("letterGrade"));
        colMarkResult.setCellValueFactory(new PropertyValueFactory<>("result"));

        colPercent.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%.1f", v));
            }
        });

        subjectTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ===== Filters =====
        cmbExam.setOnAction(e -> refresh());
        cmbBatch.setOnAction(e -> refresh());

        PauseTransition debounce = new PauseTransition(Duration.millis(250));
        debounce.setOnFinished(e -> refresh());
        txtSearch.textProperty().addListener((o, a, b) -> debounce.play());
    }

    /** Called by TeacherDashboardController. */
    public void setTeacherInfo(TeacherInfo info) {
        this.teacherInfo = info;

        // Exams
        cmbExam.getItems().clear();
        cmbExam.getItems().add(new BatchFilter(-1, "All Exams"));
        cmbExam.getItems().addAll(dao.findTeacherExams(info.getTeacherId()));
        cmbExam.getSelectionModel().selectFirst();

        // Batches
        cmbBatch.getItems().clear();
        cmbBatch.getItems().add(new BatchFilter(-1, "All Batches"));
        cmbBatch.getItems().addAll(dao.findTeacherBatches(info.getTeacherId()));
        cmbBatch.getSelectionModel().selectFirst();

        refresh();
    }

    @FXML
    public void refresh() {
        if (teacherInfo == null) return;

        BatchFilter ex = cmbExam.getValue();
        BatchFilter ba = cmbBatch.getValue();

        List<ExamResultRow> list = dao.findStudentResults(
                teacherInfo.getTeacherId(),
                ex == null ? -1 : ex.getId(),
                ba == null ? -1 : ba.getId(),
                txtSearch.getText());

        resultTable.setItems(FXCollections.observableArrayList(list));
        lblStudentCount.setText(list.size() + " student(s)");

        // Summary
        int total = list.size();
        int pass = 0, fail = 0;
        double sumGpa = 0, sumPct = 0;

        for (ExamResultRow r : list) {
            if ("PASS".equals(r.getOverallResult())) pass++;
            else                                     fail++;
            sumGpa += r.getGpa();
            sumPct += r.getAvgPercentage();
        }

        lblTotalStudents.setText(String.valueOf(total));
        lblTotalPass.setText(String.valueOf(pass));
        lblTotalFail.setText(String.valueOf(fail));
        lblPassRate.setText(total == 0 ? "0.0%" :
                String.format("%.1f%%", (pass * 100.0) / total));
        lblAvgGpa.setText(total == 0 ? "0.00" :
                String.format("%.2f", sumGpa / total));
        lblAvgPercent.setText(total == 0 ? "0.0%" :
                String.format("%.1f%%", sumPct / total));

        if (!list.isEmpty()) resultTable.getSelectionModel().selectFirst();
        else                 clearProfile();
    }

    // -------------------------------------------------
    private void showStudent(ExamResultRow s) {
        if (s == null) { clearProfile(); return; }
        selectedStudent = s;

        lblAvatar.setText(s.getInitials());
        lblStudentName.setText(s.getStudentName());
        lblStudentCode.setText(s.getStudentCode() + "  |  " + s.getOverallResult());
        lblBatch.setText("Batch: " + safe(s.getBatchName()));

        BatchFilter ex = cmbExam.getValue();
        int examId = ex == null ? -1 : ex.getId();

        subjectTable.setItems(FXCollections.observableArrayList(
                dao.findSubjectResults(s.getStudentId(), examId)));
    }

    private void clearProfile() {
        selectedStudent = null;
        lblAvatar.setText("--");
        lblStudentName.setText("No student selected");
        lblStudentCode.setText("Select a row to view details");
        lblBatch.setText("Batch: --");
        subjectTable.setItems(FXCollections.observableArrayList());
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "--" : v;
    }
}