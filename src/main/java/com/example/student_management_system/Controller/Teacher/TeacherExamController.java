package com.example.student_management_system.Controller.Teacher;

import com.example.student_management_system.Controller.DAO.TeacherExamDAO;
import com.example.student_management_system.Controller.Model.BatchFilter;
import com.example.student_management_system.Controller.Model.ExamItem;
import com.example.student_management_system.Controller.Model.StudentMarkRow;
import com.example.student_management_system.Controller.Model.TeacherInfo;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class TeacherExamController implements Initializable {

    // Filter
    @FXML private ComboBox<BatchFilter> cmbBatch;
    @FXML private ComboBox<BatchFilter> cmbSubject;

    // Create form
    @FXML private TextField txtExamName;
    @FXML private DatePicker dpExamDate;
    @FXML private TextField txtTotalMarks;
    @FXML private ComboBox<BatchFilter> cmbCreateBatch;

    // Exams list
    @FXML private TableView<ExamItem> examTable;
    @FXML private TableColumn<ExamItem, String>    colExamName;
    @FXML private TableColumn<ExamItem, String>    colExamBatch;
    @FXML private TableColumn<ExamItem, Integer>   colExamTotal;
    @FXML private TableColumn<ExamItem, LocalDate> colExamDate;
    @FXML private Label lblExamCount;

    // Marks entry
    @FXML private TableView<StudentMarkRow> marksTable;
    @FXML private TableColumn<StudentMarkRow, String>  colCode;
    @FXML private TableColumn<StudentMarkRow, String>  colName;
    @FXML private TableColumn<StudentMarkRow, Integer> colMarks;
    @FXML private TableColumn<StudentMarkRow, String>  colRemarks;
    @FXML private Label lblMarksSubtitle;
    @FXML private Label lblMarkedCount;

    // Toast
    @FXML private VBox examToast;
    @FXML private Label lblToastTitle;
    @FXML private Label lblToastHeading;
    @FXML private Label lblToastMessage;

    private final TeacherExamDAO dao = new TeacherExamDAO();
    private TeacherInfo teacherInfo;
    private ExamItem selectedExam;
    private PauseTransition toastTimer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // ===== Exams table =====
        colExamName.setCellValueFactory(new PropertyValueFactory<>("examName"));
        colExamBatch.setCellValueFactory(new PropertyValueFactory<>("className"));
        colExamTotal.setCellValueFactory(new PropertyValueFactory<>("totalMarks"));
        colExamDate.setCellValueFactory(new PropertyValueFactory<>("examDate"));
        examTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        examTable.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> onExamSelected(b));

        // ===== Marks table =====
        colCode.setCellValueFactory(new PropertyValueFactory<>("studentCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("studentName"));

        colMarks.setCellFactory(col -> new TableCell<>() {
            private final TextField tf = new TextField();
            {
                tf.setPrefWidth(90);
                tf.textProperty().addListener((o, old, nw) -> {
                    if (!nw.matches("\\d*")) tf.setText(nw.replaceAll("[^\\d]", ""));
                });
                tf.focusedProperty().addListener((o, was, is) -> {
                    if (!is) commit();
                });
            }
            private void commit() {
                StudentMarkRow row = getTableRow() == null ? null : getTableRow().getItem();
                if (row == null) return;
                String v = tf.getText().trim();
                if (v.isEmpty()) row.setMarks(null);
                else {
                    try { row.setMarks(Integer.parseInt(v)); }
                    catch (NumberFormatException e) { row.setMarks(null); }
                }
                updateMarkedCount();
            }
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                tf.setText(item == null ? "" : String.valueOf(item));
                setGraphic(tf);
            }
        });
        colMarks.setCellValueFactory(new PropertyValueFactory<>("marks"));

        colRemarks.setCellFactory(col -> new TableCell<>() {
            private final TextField tf = new TextField();
            {
                tf.setPromptText("Optional note...");
                tf.focusedProperty().addListener((o, was, is) -> {
                    if (!is) {
                        StudentMarkRow row = getTableRow() == null ? null : getTableRow().getItem();
                        if (row != null) row.setRemarks(tf.getText());
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                tf.setText(item == null ? "" : item);
                setGraphic(tf);
            }
        });
        colRemarks.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        marksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ===== Filters =====
        cmbBatch.setOnAction(e -> refreshExams());
        cmbSubject.setOnAction(e -> refreshMarks());
    }

    public void setTeacherInfo(TeacherInfo info) {
        this.teacherInfo = info;

        cmbBatch.getItems().clear();
        cmbBatch.getItems().add(new BatchFilter(-1, "All Batches"));
        cmbBatch.getItems().addAll(dao.findTeacherBatches(info.getTeacherId()));
        cmbBatch.getSelectionModel().selectFirst();

        cmbCreateBatch.getItems().clear();
        cmbCreateBatch.getItems().addAll(dao.findTeacherBatches(info.getTeacherId()));
        if (!cmbCreateBatch.getItems().isEmpty())
            cmbCreateBatch.getSelectionModel().selectFirst();

        cmbSubject.getItems().clear();
        cmbSubject.getItems().addAll(dao.findTeacherSubjects(info.getTeacherId()));
        if (!cmbSubject.getItems().isEmpty())
            cmbSubject.getSelectionModel().selectFirst();

        dpExamDate.setValue(LocalDate.now());
        refreshExams();
    }

    // -------------------------------------------------
    @FXML
    public void createExam() {
        if (teacherInfo == null) return;

        String name = txtExamName.getText().trim();
        LocalDate date = dpExamDate.getValue();
        String totalStr = txtTotalMarks.getText().trim();
        BatchFilter batch = cmbCreateBatch.getValue();

        if (name.isEmpty())      { toast("CREATE FAILED", "Missing name",  "Enter an exam name.", false); return; }
        if (date == null)        { toast("CREATE FAILED", "Missing date",  "Pick an exam date.", false); return; }
        if (totalStr.isEmpty())  { toast("CREATE FAILED", "Missing total", "Enter total marks.", false); return; }
        if (batch == null || batch.getId() == -1)
        { toast("CREATE FAILED", "Missing batch", "Select a batch.", false); return; }

        int total;
        try { total = Integer.parseInt(totalStr); }
        catch (NumberFormatException e) {
            toast("CREATE FAILED", "Invalid total", "Total marks must be a number.", false);
            return;
        }

        if (dao.createExam(name, date, total, batch.getId())) {
            toast("EXAM CREATED", "Success", "Exam \"" + name + "\" was created.", true);
            clearCreateForm();
            refreshExams();
        } else {
            toast("CREATE FAILED", "Database error", "Could not create the exam.", false);
        }
    }

    private void clearCreateForm() {
        txtExamName.clear();
        txtTotalMarks.clear();
        dpExamDate.setValue(LocalDate.now());

        if (!cmbCreateBatch.getItems().isEmpty())
            cmbCreateBatch.getSelectionModel().selectFirst();
        else
            cmbCreateBatch.getSelectionModel().clearSelection();

        txtExamName.requestFocus();
    }

    // -------------------------------------------------
    @FXML
    public void refreshExams() {
        if (teacherInfo == null) return;
        BatchFilter b = cmbBatch.getValue();
        List<ExamItem> exams = dao.findExams(
                teacherInfo.getTeacherId(), b == null ? -1 : b.getId());

        examTable.setItems(FXCollections.observableArrayList(exams));
        lblExamCount.setText(exams.size() + " exam(s)");

        if (!exams.isEmpty()) examTable.getSelectionModel().selectFirst();
        else                  clearMarks();
    }

    private void onExamSelected(ExamItem exam) {
        selectedExam = exam;
        if (exam == null) { clearMarks(); return; }

        lblMarksSubtitle.setText("Exam: " + exam.getExamName()
                + "   |   Batch: " + safe(exam.getClassName())
                + "   |   Total: " + exam.getTotalMarks());
        refreshMarks();
    }

    // -------------------------------------------------
    @FXML
    public void refreshMarks() {
        if (selectedExam == null || cmbSubject.getValue() == null) {
            clearMarks();
            return;
        }
        int subjectId = cmbSubject.getValue().getId();
        List<StudentMarkRow> rows = dao.findStudentsWithMarks(
                selectedExam.getClassId(), selectedExam.getExamId(), subjectId);

        marksTable.setItems(FXCollections.observableArrayList(rows));
        updateMarkedCount();
    }

    @FXML
    public void markAllFull() {
        if (selectedExam == null) return;
        int total = selectedExam.getTotalMarks();
        for (StudentMarkRow r : marksTable.getItems()) r.setMarks(total);
        marksTable.refresh();
        updateMarkedCount();
    }

    @FXML
    public void saveMarks() {
        if (teacherInfo == null || selectedExam == null || cmbSubject.getValue() == null) {
            toast("SAVE FAILED", "No exam selected", "Select an exam and subject first.", false);
            return;
        }

        int subjectId = cmbSubject.getValue().getId();
        int total = selectedExam.getTotalMarks();

        int saved = dao.saveMarks(
                teacherInfo.getTeacherId(),
                selectedExam.getExamId(),
                subjectId,
                total,
                marksTable.getItems());

        if (saved == 0) {
            toast("SAVE SKIPPED", "Nothing to save",
                    "Enter marks for at least one student.", false);
        } else {
            toast("SAVE SUCCESSFUL", "Marks saved",
                    "Saved " + saved + " mark(s).", true);
            refreshMarks();
        }
    }

    // -------------------------------------------------
    private void updateMarkedCount() {
        int marked = 0;
        for (StudentMarkRow r : marksTable.getItems())
            if (r.getMarks() != null) marked++;
        lblMarkedCount.setText(marked + " / " + marksTable.getItems().size() + " marked");
    }

    private void clearMarks() {
        selectedExam = null;
        marksTable.setItems(FXCollections.observableArrayList());
        lblMarksSubtitle.setText("Select an exam to begin.");
        lblMarkedCount.setText("0 / 0 marked");
    }

    private String safe(String v) { return (v == null || v.isBlank()) ? "--" : v; }

    // -------------------------------------------------
    private void toast(String title, String heading, String msg, boolean success) {
        if (examToast == null) return;

        examToast.setPrefWidth(420);
        examToast.setMinWidth(420);
        examToast.setMaxWidth(420);
        examToast.setPrefHeight(125);
        examToast.setMinHeight(125);
        examToast.setMaxHeight(125);

        String bg  = success ? "#dcfce7" : "#fee2e2";
        String fg  = success ? "#16a34a" : "#dc2626";
        String bar = success ? "#22c55e" : "#ef4444";
        String mk  = success ? "✓" : "!";

        lblToastTitle.setText(title);
        lblToastHeading.setText(heading);
        lblToastMessage.setText(msg);

        var iconBox = examToast.lookup(".toast-icon");
        if (iconBox != null)
            iconBox.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:19;");

        var iconLbl = examToast.lookup(".toast-icon-label");
        if (iconLbl instanceof Label l) {
            l.setText(mk);
            l.setStyle("-fx-text-fill:" + fg + "; -fx-font-size:17px; -fx-font-weight:bold;");
        }

        var barNode = examToast.lookup(".toast-bar");
        if (barNode != null)
            barNode.setStyle("-fx-background-color:" + bar + "; -fx-background-radius:3;");

        examToast.setManaged(true);
        examToast.setVisible(true);

        if (toastTimer != null) toastTimer.stop();
        toastTimer = new PauseTransition(Duration.seconds(3));
        toastTimer.setOnFinished(e -> {
            examToast.setVisible(false);
            examToast.setManaged(false);
        });
        toastTimer.play();
    }
}