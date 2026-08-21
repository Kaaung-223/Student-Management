//package com.example.student_management_system.Controller.Admin;
//
//import com.example.student_management_system.Controller.DAO.StudentDao;
//
//import javafx.beans.property.SimpleStringProperty;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.scene.control.*;
//import javafx.scene.control.cell.PropertyValueFactory;
//import javafx.scene.layout.GridPane;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//
//public class StudentController {
//
//    @FXML private TableView<StudentRecord> studentTable;
//    @FXML private TableColumn<StudentRecord, String> colCode;
//    @FXML private TableColumn<StudentRecord, String> colName;
//    @FXML private TableColumn<StudentRecord, String> colEmail;
//    @FXML private TableColumn<StudentRecord, String> colPhone;
//    @FXML private TableColumn<StudentRecord, Integer> colAge;
//    @FXML private TableColumn<StudentRecord, String> colGender;
//    @FXML private TableColumn<StudentRecord, String> colAddress;
//    @FXML private TableColumn<StudentRecord, String> colClass;
//    @FXML private TableColumn<StudentRecord, LocalDate> colAdmissionDate;
//    @FXML private TableColumn<StudentRecord, String> colStatus;
//
//    @FXML private TextField searchField;
//    @FXML private Label lblTotalStudents;
//
//    private final StudentDao studentDao = new StudentDao();
//    private final ObservableList<StudentRecord> studentList = FXCollections.observableArrayList();
//
//    @FXML
//    public void initialize() {
//        colCode.setCellValueFactory(new PropertyValueFactory<>("studentCode"));
//        colName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
//        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
//        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
//        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
//        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
//        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
//        colClass.setCellValueFactory(cellData -> {
//            String className = cellData.getValue().getClassName();
//            return new SimpleStringProperty(className != null ? className : "N/A");
//        });
//        colAdmissionDate.setCellValueFactory(new PropertyValueFactory<>("admissionDate"));
//        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
//
//        loadStudents();
//    }
//
//    private void loadStudents() {
//        List<StudentRecord> students = studentDao.getAllStudents();
//        studentList.setAll(students);
//        studentTable.setItems(studentList);
//        updateTotalLabel();
//    }
//
//    private void updateTotalLabel() {
//        lblTotalStudents.setText(String.valueOf(studentList.size()));
//    }
//
//    @FXML
//    private void handleSearch() {
//        String keyword = searchField.getText().trim();
//        if (keyword.isEmpty()) {
//            loadStudents();
//            return;
//        }
//        List<StudentRecord> results = studentDao.searchStudents(keyword);
//        studentList.setAll(results);
//        studentTable.setItems(studentList);
//        updateTotalLabel();
//    }
//
//    @FXML
//    private void handleRefresh() {
//        searchField.clear();
//        loadStudents();
//    }
//
//    @FXML
//    private void handleAddStudent() {
//        showStudentDialog(null, "Add New Student");
//    }
//
//    @FXML
//    private void handleEditStudent() {
//        StudentRecord selected = studentTable.getSelectionModel().getSelectedItem();
//        if (selected == null) {
//            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student to edit.");
//            return;
//        }
//        showStudentDialog(selected, "Edit Student");
//    }
//
//    @FXML
//    private void handleDeleteStudent() {
//        StudentRecord selected = studentTable.getSelectionModel().getSelectedItem();
//        if (selected == null) {
//            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student to delete.");
//            return;
//        }
//
//        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
//        confirm.setTitle("Confirm Delete");
//        confirm.setHeaderText("Delete Student");
//        confirm.setContentText("Are you sure you want to delete student: " + selected.getStudentName() + "?");
//        Optional<ButtonType> result = confirm.showAndWait();
//        if (result.isPresent() && result.get() == ButtonType.OK) {
//            boolean deleted = studentDao.deleteStudent(selected.getStudentId());
//            if (deleted) {
//                loadStudents();
//                showAlert(Alert.AlertType.INFORMATION, "Success", "Student deleted successfully.");
//            } else {
//                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete student.");
//            }
//        }
//    }
//
//    private void showStudentDialog(StudentRecord existingStudent, String title) {
//        Dialog<StudentRecord> dialog = new Dialog<>();
//        dialog.setTitle(title);
//        dialog.setHeaderText(null);
//
//        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
//        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
//
//        GridPane grid = new GridPane();
//        grid.setHgap(10);
//        grid.setVgap(10);
//        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
//
//        TextField codeField = new TextField();
//        TextField nameField = new TextField();
//        TextField emailField = new TextField();
//        TextField phoneField = new TextField();
//        TextField ageField = new TextField();
//        ComboBox<String> genderCombo = new ComboBox<>();
//        genderCombo.getItems().addAll("Male", "Female");
//        TextArea addressArea = new TextArea();
//        addressArea.setPrefRowCount(2);
//        ComboBox<String> classCombo = new ComboBox<>();
//        classCombo.setItems(FXCollections.observableArrayList(studentDao.getAllClassNames()));
//        DatePicker admissionDatePicker = new DatePicker();
//        ComboBox<String> statusCombo = new ComboBox<>();
//        statusCombo.getItems().addAll("ACTIVE", "INACTIVE");
//
//        if (existingStudent != null) {
//            codeField.setText(existingStudent.getStudentCode());
//            nameField.setText(existingStudent.getStudentName());
//            emailField.setText(existingStudent.getEmail());
//            phoneField.setText(existingStudent.getPhone());
//            ageField.setText(String.valueOf(existingStudent.getAge()));
//            genderCombo.setValue(existingStudent.getGender());
//            addressArea.setText(existingStudent.getAddress());
//            classCombo.setValue(existingStudent.getClassName());
//            admissionDatePicker.setValue(existingStudent.getAdmissionDate());
//            statusCombo.setValue(existingStudent.getStatus());
//            codeField.setEditable(false);
//        } else {
//            admissionDatePicker.setValue(LocalDate.now());
//            statusCombo.setValue("ACTIVE");
//        }
//
//        grid.add(new Label("Student Code:"), 0, 0);
//        grid.add(codeField, 1, 0);
//        grid.add(new Label("Full Name:"), 0, 1);
//        grid.add(nameField, 1, 1);
//        grid.add(new Label("Email:"), 0, 2);
//        grid.add(emailField, 1, 2);
//        grid.add(new Label("Phone:"), 0, 3);
//        grid.add(phoneField, 1, 3);
//        grid.add(new Label("Age:"), 0, 4);
//        grid.add(ageField, 1, 4);
//        grid.add(new Label("Gender:"), 0, 5);
//        grid.add(genderCombo, 1, 5);
//        grid.add(new Label("Address:"), 0, 6);
//        grid.add(addressArea, 1, 6);
//        grid.add(new Label("Class:"), 0, 7);
//        grid.add(classCombo, 1, 7);
//        grid.add(new Label("Admission Date:"), 0, 8);
//        grid.add(admissionDatePicker, 1, 8);
//        grid.add(new Label("Status:"), 0, 9);
//        grid.add(statusCombo, 1, 9);
//
//        dialog.getDialogPane().setContent(grid);
//
//        final Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
//        saveButton.setDisable(true);
//        nameField.textProperty().addListener((obs, old, newVal) ->
//                saveButton.setDisable(newVal.trim().isEmpty() || codeField.getText().trim().isEmpty()));
//        codeField.textProperty().addListener((obs, old, newVal) ->
//                saveButton.setDisable(newVal.trim().isEmpty() || nameField.getText().trim().isEmpty()));
//
//        dialog.setResultConverter(dialogButton -> {
//            if (dialogButton == saveButtonType) {
//                StudentRecord student = new StudentRecord();
//                student.setStudentCode(codeField.getText().trim());
//                student.setStudentName(nameField.getText().trim());
//                student.setEmail(emailField.getText().trim());
//                student.setPhone(phoneField.getText().trim());
//                try {
//                    student.setAge(Integer.parseInt(ageField.getText().trim()));
//                } catch (NumberFormatException e) {
//                    student.setAge(0);
//                }
//                student.setGender(genderCombo.getValue());
//                student.setAddress(addressArea.getText().trim());
//                student.setClassName(classCombo.getValue());
//                student.setAdmissionDate(admissionDatePicker.getValue());
//                student.setStatus(statusCombo.getValue());
//                if (existingStudent != null) {
//                    student.setStudentId(existingStudent.getStudentId());
//                }
//                return student;
//            }
//            return null;
//        });
//
//        Optional<StudentRecord> result = dialog.showAndWait();
//        result.ifPresent(student -> {
//            boolean success;
//            if (existingStudent == null) {
//                success = studentDao.addStudent(student);
//            } else {
//                success = studentDao.updateStudent(student);
//            }
//            if (success) {
//                loadStudents();
//                showAlert(Alert.AlertType.INFORMATION, "Success", "Student saved successfully.");
//            } else {
//                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save student.");
//            }
//        });
//    }
//
//    private void showAlert(Alert.AlertType type, String title, String content) {
//        Alert alert = new Alert(type);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(content);
//        alert.showAndWait();
//    }
//}