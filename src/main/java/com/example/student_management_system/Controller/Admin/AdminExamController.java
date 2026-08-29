package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.ClassDAO;
import com.example.student_management_system.Controller.DAO.ExamDAO;
import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.Exam;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AdminExamController implements Initializable {

    // =========================================================
    // BATCH FILTER
    // =========================================================

    @FXML
    private ComboBox<Batch> cmbBatchFilter;


    // =========================================================
    // FORM
    // =========================================================

    @FXML
    private TextField txtExamName;

    @FXML
    private ComboBox<Batch> cmbBatchForExam;

    @FXML
    private DatePicker dpExamDate;

    @FXML
    private TextField txtTotalMarks;

    @FXML
    private Label lblFormTitle;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;


    // =========================================================
    // TABLE
    // =========================================================

    @FXML
    private TableView<Exam> examTable;

    @FXML
    private TableColumn<Exam, Integer> idColumn;

    @FXML
    private TableColumn<Exam, String> nameColumn;

    @FXML
    private TableColumn<Exam, String> batchColumn;

    @FXML
    private TableColumn<Exam, String> dateColumn;

    @FXML
    private TableColumn<Exam, Integer> marksColumn;

    @FXML
    private TableColumn<Exam, Integer> gradesColumn;

    @FXML
    private TableColumn<Exam, Void> actionColumn;


    // =========================================================
    // DAO
    // =========================================================

    private final ExamDAO examDAO = new ExamDAO();
    private final ClassDAO classDAO = new ClassDAO();

    private static final Batch ALL_BATCHES = new Batch(-1, "All Batches");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");


    // =========================================================
    // STATE
    // =========================================================

    private int selectedExamId = -1;


    // =========================================================
    // INITIALIZE
    // =========================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        setupTable();
        loadBatchCombos();
        loadExams();
        setupTableSelection();
        showCreateMode();
    }


    // =========================================================
    // BATCH COMBOS
    // =========================================================

    private void loadBatchCombos() {

        List<Batch> batches = classDAO.getAllBatches();

        List<Batch> filterItems = new ArrayList<>();
        filterItems.add(ALL_BATCHES);
        filterItems.addAll(batches);
        cmbBatchFilter.setItems(FXCollections.observableArrayList(filterItems));
        cmbBatchFilter.setValue(ALL_BATCHES);
        cmbBatchFilter.setOnAction(e -> loadExams());

        // Form's batch picker has no "All Batches" — every exam must belong to exactly one batch
        cmbBatchForExam.setItems(FXCollections.observableArrayList(batches));
    }


    // =========================================================
    // TABLE SETUP
    // =========================================================

    private void setupTable() {

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("examName"));

        batchColumn.setCellValueFactory(cellData -> {
            String className = cellData.getValue().getClassName();
            return new javafx.beans.property.SimpleStringProperty(
                    className != null ? className : "Not assigned");
        });

        dateColumn.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getExamDate();
            return new javafx.beans.property.SimpleStringProperty(
                    date != null ? date.format(DATE_FMT) : "-");
        });

        marksColumn.setCellValueFactory(new PropertyValueFactory<>("totalMarks"));
        gradesColumn.setCellValueFactory(new PropertyValueFactory<>("gradesRecorded"));

        examTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        setupActionColumn();
    }


    // =========================================================
    // ACTION COLUMN
    // =========================================================

    private void setupActionColumn() {

        actionColumn.setCellFactory(column -> new TableCell<>() {

            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox box = new HBox(8, editButton, deleteButton);

            {
                editButton.setStyle(
                        "-fx-background-color:#4f46e5;" +
                                "-fx-text-fill:white;" +
                                "-fx-font-size:11px;" +
                                "-fx-background-radius:6;" +
                                "-fx-padding:5 10;" +
                                "-fx-cursor:hand;"
                );

                deleteButton.setStyle(
                        "-fx-background-color:#dc2626;" +
                                "-fx-text-fill:white;" +
                                "-fx-font-size:11px;" +
                                "-fx-background-radius:6;" +
                                "-fx-padding:5 10;" +
                                "-fx-cursor:hand;"
                );

                editButton.setOnAction(event -> {
                    Exam exam = getTableView().getItems().get(getIndex());
                    editExam(exam);
                });

                deleteButton.setOnAction(event -> {
                    Exam exam = getTableView().getItems().get(getIndex());
                    deleteExam(exam);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }


    // =========================================================
    // LOAD EXAMS (batch-filtered)
    // =========================================================

    private void loadExams() {
        Batch selected = cmbBatchFilter.getValue();
        int classId = (selected == null) ? -1 : selected.getId();

        List<Exam> exams = examDAO.getExamsByBatch(classId);
        examTable.setItems(FXCollections.observableArrayList(exams));
    }


    // =========================================================
    // TABLE SELECTION
    // =========================================================

    private void setupTableSelection() {

        examTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        fillForm(newValue);
                    }
                });
    }


    // =========================================================
    // FILL FORM
    // =========================================================

    private void fillForm(Exam exam) {

        selectedExamId = exam.getId();

        txtExamName.setText(exam.getExamName());
        dpExamDate.setValue(exam.getExamDate());
        txtTotalMarks.setText(String.valueOf(exam.getTotalMarks()));

        for (Batch b : cmbBatchForExam.getItems()) {
            if (b.getId() == exam.getClassId()) {
                cmbBatchForExam.setValue(b);
                break;
            }
        }

        lblFormTitle.setText("Update Exam");
        btnSave.setText("Update Exam");
    }


    // =========================================================
    // CREATE BUTTON
    // =========================================================

    @FXML
    private void handleCreateExam(ActionEvent event) {
        clearForm();
        showCreateMode();
    }


    // =========================================================
    // SAVE
    // =========================================================

    @FXML
    private void handleSave(ActionEvent event) {

        if (!validateForm()) {
            return;
        }

        String examName = txtExamName.getText().trim();
        Batch batch = cmbBatchForExam.getValue();
        int classId = batch.getId();
        LocalDate examDate = dpExamDate.getValue();
        int totalMarks = Integer.parseInt(txtTotalMarks.getText().trim());

        boolean success;

        if (selectedExamId != -1) {

            success = examDAO.updateExam(selectedExamId, examName, classId, examDate, totalMarks);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Exam updated successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update exam.");
                return;
            }

        } else {

            int newId = examDAO.createExam(examName, classId, examDate, totalMarks);

            if (newId != -1) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Exam created successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create exam.");
                return;
            }
        }

        loadExams();
        clearForm();
        showCreateMode();
    }


    // =========================================================
    // EDIT
    // =========================================================

    private void editExam(Exam exam) {
        fillForm(exam);
        examTable.getSelectionModel().select(exam);
    }


    // =========================================================
    // DELETE
    // =========================================================

    private void deleteExam(Exam exam) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Exam");
        confirm.setHeaderText("Delete " + exam.getExamName() + "?");
        confirm.setContentText(
                exam.getGradesRecorded() > 0
                        ? "This exam has " + exam.getGradesRecorded() + " grade(s) recorded. " +
                        "Deleting it will also delete those grades. Continue?"
                        : "Are you sure you want to delete this exam?"
        );

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {

            boolean success = examDAO.deleteExam(exam.getId());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Exam deleted successfully.");
                loadExams();
                clearForm();
                showCreateMode();
            } else {
                showAlert(Alert.AlertType.ERROR, "Delete Failed", "Could not delete this exam.");
            }
        }
    }


    // =========================================================
    // REFRESH
    // =========================================================

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadBatchCombos();
        loadExams();
    }


    // =========================================================
    // CANCEL
    // =========================================================

    @FXML
    private void handleCancel(ActionEvent event) {
        clearForm();
        showCreateMode();
    }


    // =========================================================
    // CREATE MODE
    // =========================================================

    private void showCreateMode() {
        selectedExamId = -1;
        lblFormTitle.setText("Create New Exam");
        btnSave.setText("Save Exam");
    }


    // =========================================================
    // CLEAR FORM
    // =========================================================

    private void clearForm() {
        selectedExamId = -1;
        txtExamName.clear();
        cmbBatchForExam.setValue(null);
        dpExamDate.setValue(null);
        txtTotalMarks.setText("100");
        examTable.getSelectionModel().clearSelection();
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private boolean validateForm() {

        if (txtExamName.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Please enter an exam name.");
            txtExamName.requestFocus();
            return false;
        }

        if (cmbBatchForExam.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Please select which batch this exam is for.");
            cmbBatchForExam.requestFocus();
            return false;
        }

        if (dpExamDate.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Please pick an exam date.");
            dpExamDate.requestFocus();
            return false;
        }

        if (txtTotalMarks.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Please enter total marks.");
            txtTotalMarks.requestFocus();
            return false;
        }

        try {
            int marks = Integer.parseInt(txtTotalMarks.getText().trim());
            if (marks <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Total marks must be a positive number.");
            txtTotalMarks.requestFocus();
            return false;
        }

        return true;
    }


    // =========================================================
    // ALERT
    // =========================================================

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
