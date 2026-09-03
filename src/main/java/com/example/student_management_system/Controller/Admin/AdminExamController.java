package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.ClassDAO;
import com.example.student_management_system.Controller.DAO.ExamDAO;
import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.Exam;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AdminExamController implements Initializable {

    // =========================================================
    // ROOT FOR TOAST OVERLAY
    // =========================================================

    @FXML private AnchorPane rootPane;


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

    private PauseTransition toastTimer;


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
                showToast(true, "UPDATED", "Exam updated",
                        examName + " has been updated.");
            } else {
                showToast(false, "UPDATE FAILED", "Could not update",
                        "An error occurred while updating the exam.");
                return;
            }

        } else {

            int newId = examDAO.createExam(examName, classId, examDate, totalMarks);

            if (newId != -1) {
                showToast(true, "CREATED", "Exam created",
                        examName + " has been created.");
            } else {
                showToast(false, "CREATE FAILED", "Could not create",
                        "An error occurred while creating the exam.");
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
                showToast(true, "DELETED", "Exam removed",
                        exam.getExamName() + " has been deleted.");
                loadExams();
                clearForm();
                showCreateMode();
            } else {
                showToast(false, "DELETE FAILED", "Could not delete",
                        "An error occurred while deleting the exam.");
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
    // VALIDATION (using toast)
    // =========================================================

    private boolean validateForm() {

        if (txtExamName.getText().trim().isEmpty()) {
            showToast(false, "VALIDATION", "Missing exam name",
                    "Please enter an exam name.");
            txtExamName.requestFocus();
            return false;
        }

        if (cmbBatchForExam.getValue() == null) {
            showToast(false, "VALIDATION", "Missing batch",
                    "Please select which batch this exam is for.");
            cmbBatchForExam.requestFocus();
            return false;
        }

        if (dpExamDate.getValue() == null) {
            showToast(false, "VALIDATION", "Missing date",
                    "Please pick an exam date.");
            dpExamDate.requestFocus();
            return false;
        }

        if (txtTotalMarks.getText().trim().isEmpty()) {
            showToast(false, "VALIDATION", "Missing marks",
                    "Please enter total marks.");
            txtTotalMarks.requestFocus();
            return false;
        }

        try {
            int marks = Integer.parseInt(txtTotalMarks.getText().trim());
            if (marks <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showToast(false, "VALIDATION", "Invalid marks",
                    "Total marks must be a positive number.");
            txtTotalMarks.requestFocus();
            return false;
        }

        return true;
    }


    // =========================================================
    // TOAST OVERLAY (styled like login toast)
    // =========================================================

    private void showToast(boolean success, String title, String heading, String message) {
        // Remove any existing toast
        hideToast();

        // Build toast container
        VBox toast = new VBox(8);
        toast.setMaxWidth(350);
        toast.setPrefWidth(350);
        toast.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 15;" +
                "-fx-padding: 15 17 15 15;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,.20), 22, 0, 0, 6);");

        // Icon area
        HBox topBox = new HBox(12);
        topBox.setAlignment(Pos.CENTER_LEFT);

        StackPane iconWrap = new StackPane();
        iconWrap.setMinSize(38, 38);
        iconWrap.setMaxSize(38, 38);
        iconWrap.setStyle(success
                ? "-fx-background-color: #dcfce7; -fx-background-radius: 19;"
                : "-fx-background-color: #fee2e2; -fx-background-radius: 19;");

        Label iconLabel = new Label(success ? "✓" : "✕");
        iconLabel.setStyle(success
                ? "-fx-text-fill: #16a34a; -fx-font-size: 20px; -fx-font-weight: bold;"
                : "-fx-text-fill: #dc2626; -fx-font-size: 18px; -fx-font-weight: bold;");
        iconWrap.getChildren().add(iconLabel);

        // Text block
        VBox textBox = new VBox(3);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 9px; -fx-font-weight: bold;");
        Label headingLabel = new Label(heading);
        headingLabel.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 14px; -fx-font-weight: bold;");
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(260);
        messageLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        textBox.getChildren().addAll(titleLabel, headingLabel, messageLabel);

        topBox.getChildren().addAll(iconWrap, textBox);

        // Accent line
        Region accent = new Region();
        accent.setMinHeight(3);
        accent.setMaxHeight(3);
        accent.setStyle(success
                ? "-fx-background-color: #16a34a; -fx-background-radius: 3;"
                : "-fx-background-color: #ef4444; -fx-background-radius: 3;");

        toast.getChildren().addAll(topBox, accent);

        // Position at top-right of the root pane
        StackPane.setAlignment(toast, Pos.TOP_RIGHT);
        StackPane.setMargin(toast, new Insets(24, 24, 0, 0));

        // Add to root
        rootPane.getChildren().add(toast);

        // Auto-hide after 3 seconds
        toastTimer = new PauseTransition(Duration.seconds(3));
        toastTimer.setOnFinished(e -> rootPane.getChildren().remove(toast));
        toastTimer.play();
    }

    private void hideToast() {
        if (toastTimer != null) {
            toastTimer.stop();
            toastTimer = null;
        }
        rootPane.getChildren().removeIf(node ->
                node instanceof VBox && node.getStyle().contains("fx-background-color: white;")
        );
    }
}