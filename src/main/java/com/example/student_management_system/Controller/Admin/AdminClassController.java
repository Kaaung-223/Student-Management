package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.ClassDAO;
import com.example.student_management_system.Controller.DAO.TeacherDAO;
import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.Teacher;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminClassController implements Initializable {

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
            showAlert(
                    Alert.AlertType.ERROR,
                    "Save failed",
                    "The class could not be saved. Check the database " +
                            "connection and make sure the class name is unique."
            );
            return;
        }

        showAlert(
                Alert.AlertType.INFORMATION,
                "Success",
                selectedClassId == -1
                        ? "Class created successfully."
                        : "Class updated successfully."
        );
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
            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Deleted",
                    "Class deleted successfully."
            );
            loadClasses();
            clearForm();
            showCreateMode();
        } else {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Delete failed",
                    "Cannot delete this class because it may have " +
                            "students or enrollments."
            );
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
            showValidation("Please enter class name.", txtClassName);
            return false;
        }
        if (isBlank(txtAcademicYear)) {
            showValidation("Please enter academic year.", txtAcademicYear);
            return false;
        }
        if (isBlank(txtRoomNo)) {
            showValidation("Please enter room number.", txtRoomNo);
            return false;
        }

        try {
            int duration = Integer.parseInt(txtDuration.getText().trim());
            if (duration <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            showValidation(
                    "Duration must be a positive whole number.",
                    txtDuration
            );
            return false;
        }

        try {
            BigDecimal fees = parseFees();
            if (fees.signum() < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            showValidation(
                    "Fees must be a valid number greater than or equal to 0.",
                    txtFees
            );
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

    private void showValidation(String message, TextField field) {
        showAlert(Alert.AlertType.WARNING, "Validation", message);
        field.requestFocus();
    }

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}