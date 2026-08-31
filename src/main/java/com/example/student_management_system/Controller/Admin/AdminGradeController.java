package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.ClassDAO;
import com.example.student_management_system.Controller.DAO.ExamDAO;
import com.example.student_management_system.Controller.DAO.GradeDAO;
import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.BatchExamRate;
import com.example.student_management_system.Controller.Model.Exam;
import com.example.student_management_system.Controller.Model.ExamOption;
import com.example.student_management_system.Controller.Model.GradeSummary;
import com.example.student_management_system.Controller.Model.StudentGradeRow;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AdminGradeController implements Initializable {

    @FXML
    private Label lblPassCount;
    @FXML
    private Label lblFailCount;
    @FXML
    private Label lblPassRate;
    @FXML
    private Label lblAverageGpa;
    @FXML
    private Label lblStudentCount;

    @FXML
    private TextField txtSearchStudent;
    @FXML
    private ComboBox<Batch> cmbBatchFilter;
    @FXML
    private ComboBox<ExamOption> cmbExamFilter;

    @FXML
    private TableView<BatchExamRate> examRateTable;
    @FXML
    private TableColumn<BatchExamRate, String> colExamName;
    @FXML
    private TableColumn<BatchExamRate, String> colExamBatch;
    @FXML
    private TableColumn<BatchExamRate, String> colExamDate;
    @FXML
    private TableColumn<BatchExamRate, String> colGraded;
    @FXML
    private TableColumn<BatchExamRate, String> colExamPass;
    @FXML
    private TableColumn<BatchExamRate, String> colExamFail;
    @FXML
    private TableColumn<BatchExamRate, String> colExamPassRate;

    @FXML
    private TableView<StudentGradeRow> studentGradeTable;
    @FXML
    private TableColumn<StudentGradeRow, String> colStudentCode;
    @FXML
    private TableColumn<StudentGradeRow, String> colStudentName;
    @FXML
    private TableColumn<StudentGradeRow, String> colStudentBatch;
    @FXML
    private TableColumn<StudentGradeRow, String> colStudentExam;
    @FXML
    private TableColumn<StudentGradeRow, String> colMarks;
    @FXML
    private TableColumn<StudentGradeRow, String> colPercentage;
    @FXML
    private TableColumn<StudentGradeRow, String> colLetter;
    @FXML
    private TableColumn<StudentGradeRow, String> colStudentPass;
    @FXML
    private TableColumn<StudentGradeRow, String> colStudentFail;
    @FXML
    private TableColumn<StudentGradeRow, String> colResult;
    @FXML
    private TableColumn<StudentGradeRow, String> colGpa;

    private final GradeDAO gradeDAO = new GradeDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final ExamDAO examDAO = new ExamDAO();

    private static final Batch ALL_BATCHES = new Batch(-1, "All Batches");
    private static final ExamOption ALL_EXAMS = new ExamOption(-1, "All Exams");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private boolean loadingFilters;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBatchFilter();
        setupExamFilter();
        setupExamRateTable();
        setupStudentTable();
        setupListeners();
        loadData();
    }

    private void setupBatchFilter() {
        List<Batch> items = new ArrayList<>();
        items.add(ALL_BATCHES);
        items.addAll(classDAO.getAllBatches());
        cmbBatchFilter.setItems(FXCollections.observableArrayList(items));
        cmbBatchFilter.getSelectionModel().select(ALL_BATCHES);
    }

    private void setupExamFilter() {
        reloadExamFilter();
    }

    private void reloadExamFilter() {
        loadingFilters = true;

        Batch selectedBatch = cmbBatchFilter.getValue();
        int classId = selectedBatch == null ? -1 : selectedBatch.getId();

        List<ExamOption> exams = new ArrayList<>();
        exams.add(ALL_EXAMS);

        for (Exam exam : examDAO.getExamsByBatch(classId)) {
            exams.add(new ExamOption(exam.getId(), exam.getExamName()));
        }

        cmbExamFilter.setItems(FXCollections.observableArrayList(exams));
        cmbExamFilter.getSelectionModel().select(ALL_EXAMS);

        loadingFilters = false;
    }

    private void setupExamRateTable() {
        colExamName.setCellValueFactory(data ->
                new SimpleStringProperty(valueOrDash(data.getValue().getExamName())));
        colExamBatch.setCellValueFactory(data ->
                new SimpleStringProperty(valueOrDash(data.getValue().getClassName())));
        colExamDate.setCellValueFactory(data -> {
            if (data.getValue().getExamDate() == null) {
                return new SimpleStringProperty("-");
            }
            return new SimpleStringProperty(data.getValue().getExamDate().format(DATE_FMT));
        });
        colGraded.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getGradedCount())));
        colExamPass.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getPassCount())));
        colExamFail.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getFailCount())));
        colExamPassRate.setCellValueFactory(data ->
                new SimpleStringProperty(formatPassRate(data.getValue())));

        examRateTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupStudentTable() {
        colStudentCode.setCellValueFactory(data ->
                new SimpleStringProperty(valueOrDash(data.getValue().getStudentCode())));
        colStudentName.setCellValueFactory(data ->
                new SimpleStringProperty(valueOrDash(data.getValue().getStudentName())));
        colStudentBatch.setCellValueFactory(data ->
                new SimpleStringProperty(valueOrDash(data.getValue().getClassName())));
        colStudentExam.setCellValueFactory(data ->
                new SimpleStringProperty(valueOrDash(data.getValue().getExamName())));
        colMarks.setCellValueFactory(data ->
                new SimpleStringProperty(formatNullableInt(data.getValue().getObtainedMarks())));
        colPercentage.setCellValueFactory(data ->
                new SimpleStringProperty(formatPercentage(data.getValue().getPercentage())));
        colLetter.setCellValueFactory(data ->
                new SimpleStringProperty(valueOrDash(data.getValue().getLetterGrade())));
        colStudentPass.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getPassCount())));
        colStudentFail.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getFailCount())));
        colGpa.setCellValueFactory(data ->
                new SimpleStringProperty(formatGpa(data.getValue().getGpa(), data.getValue().getExamsTaken())));

        colResult.setCellValueFactory(data ->
                new SimpleStringProperty(formatResultLabel(data.getValue().getResult())));
        colResult.setCellFactory(makeResultBadgeCellFactory());

        studentGradeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupListeners() {
        txtSearchStudent.textProperty().addListener((obs, oldValue, newValue) -> loadData());

        cmbBatchFilter.valueProperty().addListener((obs, oldValue, newValue) -> {
            reloadExamFilter();
            loadData();
        });

        cmbExamFilter.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!loadingFilters) {
                loadData();
            }
        });
    }

    private void loadData() {
        int classId = selectedClassId();
        int examId = selectedExamId();

        GradeSummary summary = gradeDAO.getGradeSummary(classId, examId);
        lblPassCount.setText(String.valueOf(summary.getPassCount()));
        lblFailCount.setText(String.valueOf(summary.getFailCount()));
        lblPassRate.setText(String.format("%.1f%%", summary.getPassRate()));
        lblAverageGpa.setText(String.format("%.2f", summary.getAverageGpa()));

        examRateTable.setItems(
                FXCollections.observableArrayList(gradeDAO.getBatchExamRates(classId))
        );

        List<StudentGradeRow> students = gradeDAO.getStudentGradeRows(
                classId,
                examId,
                txtSearchStudent.getText()
        );
        studentGradeTable.setItems(FXCollections.observableArrayList(students));
        lblStudentCount.setText(
                students.size() + (students.size() == 1 ? " student" : " students")
        );
    }

    private int selectedClassId() {
        Batch batch = cmbBatchFilter.getValue();
        return batch == null ? -1 : batch.getId();
    }

    private int selectedExamId() {
        ExamOption exam = cmbExamFilter.getValue();
        return exam == null ? -1 : exam.getId();
    }

    private Callback<TableColumn<StudentGradeRow, String>, TableCell<StudentGradeRow, String>>
    makeResultBadgeCellFactory() {
        return column -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                badge.setText(item);
                applyResultBadgeStyle(badge, item);
                setGraphic(badge);
            }
        };
    }

    private void applyResultBadgeStyle(Label badge, String result) {
        String upper = result.toUpperCase();
        if (upper.contains("PASS")) {
            badge.setStyle(
                    "-fx-background-color: #eafaf0;" +
                            "-fx-text-fill: #1c8a52;" +
                            "-fx-font-size: 10px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 6;" +
                            "-fx-padding: 4 10;"
            );
        } else if (upper.contains("FAIL")) {
            badge.setStyle(
                    "-fx-background-color: #fee2e2;" +
                            "-fx-text-fill: #dc2626;" +
                            "-fx-font-size: 10px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 6;" +
                            "-fx-padding: 4 10;"
            );
        } else {
            badge.setStyle(
                    "-fx-background-color: #f1f5f9;" +
                            "-fx-text-fill: #64748b;" +
                            "-fx-font-size: 10px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 6;" +
                            "-fx-padding: 4 10;"
            );
        }
    }

    private String formatPassRate(BatchExamRate rate) {
        if (rate.getGradedCount() == 0) {
            return "-";
        }
        return String.format("%.1f%%", rate.getPassRate());
    }

    private String formatResultLabel(String result) {
        if (result == null || result.isBlank() || "NOT GRADED".equalsIgnoreCase(result)) {
            return "Not graded";
        }
        if ("PASS".equalsIgnoreCase(result)) {
            return "Pass";
        }
        if ("FAIL".equalsIgnoreCase(result)) {
            return "Fail";
        }
        return result;
    }

    private String formatGpa(double gpa, int examsTaken) {
        if (examsTaken == 0) {
            return "-";
        }
        return String.format("%.2f", gpa);
    }

    private String formatNullableInt(Integer value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String formatPercentage(Double value) {
        return value == null ? "-" : String.format("%.1f", value);
    }

    private String valueOrDash(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }
}
