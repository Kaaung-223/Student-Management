package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.ClassDAO;
import com.example.student_management_system.Controller.DAO.TeacherDAO;
import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.Teacher;

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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminClassController implements Initializable {

    @FXML private AnchorPane rootPane;   // root for toast overlay

    @FXML private TextField txtClassName;
    @FXML private TextField txtAcademicYear;
    @FXML private TextField txtRoomNo;
    @FXML private TextField txtDuration;
    @FXML private TextField txtFees;
    @FXML private ComboBox<Teacher> cmbTeacher;
    @FXML private ComboBox<String> cmbStatus;

    @FXML private TableView<Batch> classTable;
    @FXML private TableColumn<Batch, Integer> idColumn;
    @FXML private TableColumn<Batch, String> nameColumn;
    @FXML private TableColumn<Batch, String> yearColumn;
    @FXML private TableColumn<Batch, String> roomColumn;
    @FXML private TableColumn<Batch, String> teacherColumn;
    @FXML private TableColumn<Batch, Integer> durationColumn;
    @FXML private TableColumn<Batch, BigDecimal> feesColumn;
    @FXML private TableColumn<Batch, String> statusColumn;
    @FXML private TableColumn<Batch, Void> actionColumn;

    @FXML private TextField txtSearch;
    @FXML private Label lblFormTitle;
    @FXML private Button btnSave;

    private final ClassDAO classDAO = new ClassDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private int selectedClassId = -1;

    private PauseTransition toastTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupStatus();
        loadTeachers();
        loadClasses();
        setupTableSelection();
        showCreateMode();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("academicYear"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNo"));
        teacherColumn.setCellValueFactory(
                new PropertyValueFactory<>("classTeacherName")
        );
        durationColumn.setCellValueFactory(
                new PropertyValueFactory<>("durationMonths")
        );
        feesColumn.setCellValueFactory(new PropertyValueFactory<>("fees"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        classTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupActionColumn();
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(8, editButton, deleteButton);

            {
                editButton.setStyle(
                        "-fx-background-color:#4f46e5;" +
                                "-fx-text-fill:white;" +
                                "-fx-background-radius:6;" +
                                "-fx-cursor:hand;"
                );
                deleteButton.setStyle(
                        "-fx-background-color:#dc2626;" +
                                "-fx-text-fill:white;" +
                                "-fx-background-radius:6;" +
                                "-fx-cursor:hand;"
                );

                editButton.setOnAction(event -> {
                    if (getIndex() >= 0 &&
                            getIndex() < getTableView().getItems().size()) {
                        editClass(getTableView().getItems().get(getIndex()));
                    }
                });

                deleteButton.setOnAction(event -> {
                    if (getIndex() >= 0 &&
                            getIndex() < getTableView().getItems().size()) {
                        deleteClass(getTableView().getItems().get(getIndex()));
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void setupStatus() {
        cmbStatus.setItems(FXCollections.observableArrayList(
                "ACTIVE", "INACTIVE"
        ));
        cmbStatus.setValue("ACTIVE");
    }

    private void loadTeachers() {
        List<Teacher> teachers = teacherDAO.getAllTeachers();
        cmbTeacher.setItems(FXCollections.observableArrayList(teachers));

        cmbTeacher.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Teacher teacher, boolean empty) {
                super.updateItem(teacher, empty);
                setText(empty || teacher == null
                        ? null : teacher.getTeacherName());
            }
        });

        cmbTeacher.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Teacher teacher, boolean empty) {
                super.updateItem(teacher, empty);
                setText(empty || teacher == null
                        ? null : teacher.getTeacherName());
            }
        });
    }

    private void loadClasses() {
        classTable.setItems(FXCollections.observableArrayList(
                classDAO.getAllClasses()
        ));
    }

    private void setupTableSelection() {
        classTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        fillForm(newValue);
                    }
                }
        );
    }

    private void fillForm(Batch batch) {
        selectedClassId = batch.getId();
        txtClassName.setText(batch.getName());
        txtAcademicYear.setText(batch.getAcademicYear());
        txtRoomNo.setText(batch.getRoomNo());
        txtDuration.setText(String.valueOf(batch.getDurationMonths()));
        txtFees.setText(batch.getFees() == null
                ? "0.00" : batch.getFees().setScale(2).toPlainString());
        cmbStatus.setValue(batch.getStatus());
        selectTeacher(batch.getClassTeacherId());
        lblFormTitle.setText("Update Class");
        btnSave.setText("Update Class");
    }

    private void selectTeacher(int teacherId) {
        cmbTeacher.setValue(null);
        if (teacherId <= 0) {
            return;
        }

        for (Teacher teacher : cmbTeacher.getItems()) {
            if (teacher.getId() == teacherId) {
                cmbTeacher.setValue(teacher);
                return;
            }
        }
    }

    @FXML
    private void handleCreateClass(ActionEvent event) {
        clearForm();
        showCreateMode();
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        String className = txtClassName.getText().trim();
        String academicYear = txtAcademicYear.getText().trim();
        String roomNo = txtRoomNo.getText().trim();
        int duration = Integer.parseInt(txtDuration.getText().trim());
        BigDecimal fees = parseFees();
        Teacher teacher = cmbTeacher.getValue();
        int teacherId = teacher == null ? -1 : teacher.getId();
        String status = cmbStatus.getValue();

        boolean success;
        if (selectedClassId != -1) {
            success = classDAO.updateClass(
                    selectedClassId,
                    className,
                    academicYear,
                    roomNo,
                    teacherId,
                    duration,
                    fees,
                    status
            );
        } else {
            success = classDAO.createClass(
                    className,
                    academicYear,
                    roomNo,
                    teacherId,
                    duration,
                    fees,
                    status
            );
        }

        if (!success) {
            showToast(false, "SAVE FAILED", "Could not save",
                    "Check connection or duplicate class name.");
            return;
        }

        showToast(true, "SUCCESS", selectedClassId == -1
                        ? "Class created" : "Class updated",
                selectedClassId == -1
                        ? "The class has been created successfully."
                        : "The class has been updated successfully.");

        loadClasses();
        clearForm();
        showCreateMode();
    }

    private void editClass(Batch batch) {
        fillForm(batch);
        classTable.getSelectionModel().select(batch);
    }

    private void deleteClass(Batch batch) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Class");
        confirm.setHeaderText("Delete " + batch.getName() + "?");
        confirm.setContentText(
                "This may fail if students or enrollments still reference it."
        );

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        if (classDAO.deleteClass(batch.getId())) {
            showToast(true, "DELETED", "Class removed",
                    batch.getName() + " has been deleted.");
            loadClasses();
            clearForm();
            showCreateMode();
        } else {
            showToast(false, "DELETE FAILED", "Cannot delete",
                    "This class may have students or enrollments.");
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadClasses();
            return;
        }
        classTable.setItems(FXCollections.observableArrayList(
                classDAO.searchClasses(keyword)
        ));
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        txtSearch.clear();
        loadClasses();
        loadTeachers();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        clearForm();
        showCreateMode();
    }

    private void showCreateMode() {
        selectedClassId = -1;
        lblFormTitle.setText("Create New Class");
        btnSave.setText("Save Class");
    }

    private void clearForm() {
        selectedClassId = -1;
        txtClassName.clear();
        txtAcademicYear.clear();
        txtRoomNo.clear();
        txtDuration.setText("12");
        txtFees.setText("0.00");
        cmbTeacher.setValue(null);
        cmbStatus.setValue("ACTIVE");
        classTable.getSelectionModel().clearSelection();
    }

    private boolean validateForm() {
        if (isBlank(txtClassName)) {
            showToast(false, "VALIDATION", "Missing class name",
                    "Please enter a class name.");
            txtClassName.requestFocus();
            return false;
        }
        if (isBlank(txtAcademicYear)) {
            showToast(false, "VALIDATION", "Missing academic year",
                    "Please enter an academic year.");
            txtAcademicYear.requestFocus();
            return false;
        }
        if (isBlank(txtRoomNo)) {
            showToast(false, "VALIDATION", "Missing room number",
                    "Please enter a room number.");
            txtRoomNo.requestFocus();
            return false;
        }

        try {
            int duration = Integer.parseInt(txtDuration.getText().trim());
            if (duration <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            showToast(false, "VALIDATION", "Invalid duration",
                    "Duration must be a positive whole number.");
            txtDuration.requestFocus();
            return false;
        }

        try {
            BigDecimal fees = parseFees();
            if (fees.signum() < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            showToast(false, "VALIDATION", "Invalid fees",
                    "Fees must be a valid number >= 0.");
            txtFees.requestFocus();
            return false;
        }

        if (cmbStatus.getValue() == null) {
            cmbStatus.setValue("ACTIVE");
        }
        return true;
    }

    private BigDecimal parseFees() {
        String value = txtFees.getText().trim();
        if (value.isEmpty()) {
            return BigDecimal.ZERO.setScale(2);
        }
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isBlank(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
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