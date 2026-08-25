package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.ClassDAO;
import com.example.student_management_system.Controller.DAO.TeacherDAO;
import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.Teacher;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminClassController implements Initializable {

    // =========================================================
    // FORM
    // =========================================================

    @FXML
    private TextField txtClassName;

    @FXML
    private TextField txtAcademicYear;

    @FXML
    private TextField txtRoomNo;

    @FXML
    private TextField txtDuration;

    @FXML
    private ComboBox<Teacher> cmbTeacher;

    @FXML
    private ComboBox<String> cmbStatus;


    // =========================================================
    // TABLE
    // =========================================================

    @FXML
    private TableView<Batch> classTable;

    @FXML
    private TableColumn<Batch, Integer> idColumn;

    @FXML
    private TableColumn<Batch, String> nameColumn;

    @FXML
    private TableColumn<Batch, String> yearColumn;

    @FXML
    private TableColumn<Batch, String> roomColumn;

    @FXML
    private TableColumn<Batch, String> teacherColumn;

    @FXML
    private TableColumn<Batch, Integer> durationColumn;

    @FXML
    private TableColumn<Batch, String> statusColumn;

    @FXML
    private TableColumn<Batch, Void> actionColumn;


    // =========================================================
    // OTHER CONTROLS
    // =========================================================

    @FXML
    private TextField txtSearch;

    @FXML
    private Label lblFormTitle;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;


    // =========================================================
    // DAO
    // =========================================================

    private final ClassDAO classDAO = new ClassDAO();

    private final TeacherDAO teacherDAO = new TeacherDAO();


    // =========================================================
    // STATE
    // =========================================================

    private int selectedClassId = -1;


    // =========================================================
    // INITIALIZE
    // =========================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        setupTable();

        setupStatus();

        loadTeachers();

        loadClasses();

        setupTableSelection();

        showCreateMode();
    }


    // =========================================================
    // TABLE SETUP
    // =========================================================

    private void setupTable() {

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        yearColumn.setCellValueFactory(
                new PropertyValueFactory<>("academicYear")
        );

        roomColumn.setCellValueFactory(
                new PropertyValueFactory<>("roomNo")
        );

        teacherColumn.setCellValueFactory(
                new PropertyValueFactory<>("classTeacherName")
        );

        durationColumn.setCellValueFactory(
                new PropertyValueFactory<>("durationMonths")
        );

        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        classTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        setupActionColumn();
    }


    // =========================================================
    // ACTION COLUMN
    // =========================================================

    private void setupActionColumn() {

        actionColumn.setCellFactory(column -> new TableCell<>() {

            private final Button editButton =
                    new Button("Edit");

            private final Button deleteButton =
                    new Button("Delete");

            private final HBox box =
                    new HBox(8, editButton, deleteButton);

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

                    Batch batch =
                            getTableView()
                                    .getItems()
                                    .get(getIndex());

                    editClass(batch);
                });

                deleteButton.setOnAction(event -> {

                    Batch batch =
                            getTableView()
                                    .getItems()
                                    .get(getIndex());

                    deleteClass(batch);
                });
            }

            @Override
            protected void updateItem(
                    Void item,
                    boolean empty
            ) {

                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });
    }


    // =========================================================
    // STATUS
    // =========================================================

    private void setupStatus() {

        cmbStatus.setItems(
                FXCollections.observableArrayList(
                        "ACTIVE",
                        "INACTIVE"
                )
        );

        cmbStatus.setValue("ACTIVE");
    }


    // =========================================================
    // LOAD TEACHERS
    // =========================================================

    private void loadTeachers() {

        List<Teacher> teachers =
                teacherDAO.getAllTeachers();

        cmbTeacher.setItems(
                FXCollections.observableArrayList(
                        teachers
                )
        );
    }


    // =========================================================
    // LOAD CLASSES
    // =========================================================

    private void loadClasses() {

        List<Batch> classes =
                classDAO.getAllClasses();

        classTable.setItems(
                FXCollections.observableArrayList(
                        classes
                )
        );
    }


    // =========================================================
    // TABLE SELECTION
    // =========================================================

    private void setupTableSelection() {

        classTable.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {

                            if (newValue != null) {
                                fillForm(newValue);
                            }

                        }
                );
    }


    // =========================================================
    // FILL FORM
    // =========================================================

    private void fillForm(Batch batch) {

        selectedClassId =
                batch.getId();

        txtClassName.setText(
                batch.getName()
        );

        txtAcademicYear.setText(
                batch.getAcademicYear()
        );

        txtRoomNo.setText(
                batch.getRoomNo()
        );

        txtDuration.setText(
                String.valueOf(
                        batch.getDurationMonths()
                )
        );

        cmbStatus.setValue(
                batch.getStatus()
        );

        selectTeacher(
                batch.getClassTeacherId()
        );

        lblFormTitle.setText(
                "Update Class"
        );

        btnSave.setText(
                "Update Class"
        );
    }


    // =========================================================
    // SELECT TEACHER
    // =========================================================

    private void selectTeacher(int teacherId) {

        if (teacherId <= 0) {

            cmbTeacher.setValue(null);

            return;
        }

        for (Teacher teacher :
                cmbTeacher.getItems()) {

            if (teacher.getId()
                    == teacherId) {

                cmbTeacher.setValue(
                        teacher
                );

                break;
            }
        }
    }


    // =========================================================
    // CREATE BUTTON
    // =========================================================

    @FXML
    private void handleCreateClass(ActionEvent event) {

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

        String className =
                txtClassName.getText().trim();

        String academicYear =
                txtAcademicYear.getText().trim();

        String roomNo =
                txtRoomNo.getText().trim();

        int duration =
                Integer.parseInt(
                        txtDuration.getText().trim()
                );

        Teacher teacher =
                cmbTeacher.getValue();

        int teacherId =
                teacher == null
                        ? -1
                        : teacher.getId();

        String status =
                cmbStatus.getValue();


        boolean success;


        // =====================================================
        // UPDATE
        // =====================================================

        if (selectedClassId != -1) {

            success =
                    classDAO.updateClass(
                            selectedClassId,
                            className,
                            academicYear,
                            roomNo,
                            teacherId,
                            duration,
                            status
                    );

            if (success) {

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Class updated successfully."
                );

            } else {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Error",
                        "Failed to update class."
                );

                return;
            }

        }

        // =====================================================
        // CREATE
        // =====================================================

        else {

            success =
                    classDAO.createClass(
                            className,
                            academicYear,
                            roomNo,
                            teacherId,
                            duration,
                            status
                    );

            if (success) {

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Class created successfully."
                );

            } else {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Error",
                        "Failed to create class.\n" +
                                "Class name may already exist."
                );

                return;
            }
        }


        loadClasses();

        clearForm();

        showCreateMode();
    }


    // =========================================================
    // EDIT
    // =========================================================

    private void editClass(Batch batch) {

        fillForm(batch);

        classTable.getSelectionModel()
                .select(batch);
    }


    // =========================================================
    // DELETE
    // =========================================================

    private void deleteClass(Batch batch) {

        Alert confirm =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirm.setTitle(
                "Delete Class"
        );

        confirm.setHeaderText(
                "Delete " + batch.getName() + "?"
        );

        confirm.setContentText(
                "Are you sure you want to delete this class?"
        );

        if (confirm.showAndWait()
                .orElse(ButtonType.CANCEL)
                == ButtonType.OK) {

            boolean success =
                    classDAO.deleteClass(
                            batch.getId()
                    );

            if (success) {

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
                        "Delete Failed",
                        "Cannot delete this class.\n" +
                                "It may already have students or enrollments."
                );
            }
        }
    }


    // =========================================================
    // SEARCH
    // =========================================================

    @FXML
    private void handleSearch(ActionEvent event) {

        String keyword =
                txtSearch.getText().trim();

        if (keyword.isEmpty()) {

            loadClasses();

            return;
        }

        classTable.setItems(
                FXCollections.observableArrayList(
                        classDAO.searchClasses(keyword)
                )
        );
    }


    // =========================================================
    // REFRESH
    // =========================================================

    @FXML
    private void handleRefresh(ActionEvent event) {

        txtSearch.clear();

        loadClasses();

        loadTeachers();
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

        selectedClassId = -1;

        lblFormTitle.setText(
                "Create New Class"
        );

        btnSave.setText(
                "Save Class"
        );
    }


    // =========================================================
    // CLEAR FORM
    // =========================================================

    private void clearForm() {

        selectedClassId = -1;

        txtClassName.clear();

        txtAcademicYear.clear();

        txtRoomNo.clear();

        txtDuration.setText("12");

        cmbTeacher.setValue(null);

        cmbStatus.setValue("ACTIVE");

        classTable.getSelectionModel()
                .clearSelection();
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private boolean validateForm() {

        if (txtClassName.getText()
                .trim()
                .isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation",
                    "Please enter class name."
            );

            txtClassName.requestFocus();

            return false;
        }


        if (txtAcademicYear.getText()
                .trim()
                .isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation",
                    "Please enter academic year."
            );

            txtAcademicYear.requestFocus();

            return false;
        }


        if (txtRoomNo.getText()
                .trim()
                .isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation",
                    "Please enter room number."
            );

            txtRoomNo.requestFocus();

            return false;
        }


        if (txtDuration.getText()
                .trim()
                .isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation",
                    "Please enter duration."
            );

            txtDuration.requestFocus();

            return false;
        }


        try {

            int duration =
                    Integer.parseInt(
                            txtDuration.getText().trim()
                    );

            if (duration <= 0) {

                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation",
                    "Duration must be a positive number."
            );

            txtDuration.requestFocus();

            return false;
        }

        return true;
    }


    // =========================================================
    // ALERT
    // =========================================================

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {

        Alert alert =
                new Alert(type);

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}