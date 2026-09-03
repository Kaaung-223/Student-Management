package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.ClassDAO;
import com.example.student_management_system.Controller.DAO.SubjectDAO;
import com.example.student_management_system.Controller.DAO.TeacherDAO;
import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.SubjectOption;
import com.example.student_management_system.Controller.Model.Teacher;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AddTeacherDialogController implements Initializable {

    public enum DialogMode { ADD, EDIT, VIEW }

    @FXML private Label lblDialogTitle;
    @FXML private Label lblDialogSubtitle;
    @FXML private Circle photoPlaceholderCircle;
    @FXML private ImageView photoPreview;
    @FXML private Button btnChoosePhoto;
    @FXML private Label lblPhotoFileName;
    @FXML private Label lblPhotoError;
    @FXML private TextField txtFullName;
    @FXML private Label lblFullNameError;
    @FXML private TextField txtTeacherCode;
    @FXML private Label lblTeacherCodeError;
    @FXML private TextField txtUsername;
    @FXML private Label lblUsernameError;

    // ---- Password fields (with visible toggles) ----
    @FXML private PasswordField txtPassword;
    @FXML private TextField visiblePasswordField;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField visibleConfirmPasswordField;
    @FXML private Button btnEye;
    @FXML private Button btnConfirmEye;
    @FXML private FontIcon eyeIcon;
    @FXML private FontIcon confirmEyeIcon;

    @FXML private Label lblPasswordHint;
    @FXML private Label lblPasswordRules;
    @FXML private Label lblPasswordError;
    @FXML private Label lblConfirmPasswordError;
    @FXML private TextField txtEmail;
    @FXML private Label lblEmailError;
    @FXML private TextField txtPhone;
    @FXML private Label lblPhoneError;
    @FXML private ComboBox<String> cmbGender;
    @FXML private DatePicker dpHireDate;
    @FXML private Label lblHireDateError;
    @FXML private TextField txtSalary;
    @FXML private Label lblSalaryError;
    @FXML private ComboBox<Batch> cmbClassLeaderOf;
    @FXML private TextArea txtAddress;
    @FXML private VBox subjectsBox;
    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final ClassDAO classDAO = new ClassDAO();

    private static final Batch NO_CLASS_LEADER = new Batch(-1, "None");

    private Stage dialogStage;
    private DialogMode dialogMode = DialogMode.ADD;
    private int editingTeacherId = -1;
    private int editingUserId = -1;
    private String selectedPhotoPath;
    private String existingPhotoPath;
    private List<SubjectOption> subjectOptions;
    private boolean saved;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{3,30}$");
    private static final Pattern TEACHER_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9-]{2,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(09|\\+959)[0-9\\s-]{7,15}$");

    public void setDialogStage(Stage stage) { this.dialogStage = stage; }
    public void setDialogMode(DialogMode mode) { this.dialogMode = mode; applyDialogMode(); }
    public boolean wasSaved() { return saved; }

    public void loadTeacher(Teacher teacher) {
        if (teacher == null) return;
        editingTeacherId = teacher.getId();
        editingUserId = teacher.getUserId();

        txtFullName.setText(valueOrEmpty(teacher.getTeacherName()));
        txtTeacherCode.setText(valueOrEmpty(teacher.getTeacherCode()));
        txtUsername.setText(valueOrEmpty(teacher.getUsername()));
        txtEmail.setText(valueOrEmpty(teacher.getEmail()));
        txtPhone.setText(valueOrEmpty(teacher.getPhone()));
        txtAddress.setText(valueOrEmpty(teacher.getAddress()));

        if (teacher.getGender() != null && !teacher.getGender().isBlank()) {
            cmbGender.setValue(teacher.getGender());
        }
        dpHireDate.setValue(teacher.getHireDate());
        if (teacher.getSalary() != null) {
            txtSalary.setText(teacher.getSalary().toPlainString());
        } else {
            txtSalary.clear();
        }

        existingPhotoPath = teacher.getPhotoPath();
        selectedPhotoPath = existingPhotoPath;
        showPhotoPreview(existingPhotoPath);

        List<Integer> selectedSubjectIds = teacherDAO.getTeacherSubjectIds(teacher.getId());
        if (subjectOptions != null) {
            for (SubjectOption option : subjectOptions) {
                option.setSelected(selectedSubjectIds.contains(option.getSubjectId()));
            }
        }

        int classLeaderId = teacherDAO.getClassLeaderClassId(teacher.getId());
        selectClassLeader(classLeaderId);

        applyDialogMode();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbGender.setItems(FXCollections.observableArrayList("Male", "Female"));

        List<Batch> batches = classDAO.getAllBatches();
        List<Batch> classLeaderItems = new ArrayList<>();
        classLeaderItems.add(NO_CLASS_LEADER);
        classLeaderItems.addAll(batches);
        cmbClassLeaderOf.setItems(FXCollections.observableArrayList(classLeaderItems));
        cmbClassLeaderOf.getSelectionModel().select(NO_CLASS_LEADER);

        subjectOptions = subjectDAO.getAllSubjects();
        subjectsBox.getChildren().clear();
        for (SubjectOption option : subjectOptions) {
            CheckBox checkBox = new CheckBox(option.getSubjectName());
            checkBox.setStyle("-fx-font-size: 12px; -fx-text-fill: #344563;");
            checkBox.selectedProperty().bindBidirectional(option.selectedProperty());
            subjectsBox.getChildren().add(checkBox);
        }

        // Bind visible password fields with real password fields
        visiblePasswordField.textProperty().bindBidirectional(txtPassword.textProperty());
        visibleConfirmPasswordField.textProperty().bindBidirectional(txtConfirmPassword.textProperty());

        btnSave.setDefaultButton(true);
        txtConfirmPassword.setOnAction(event -> save());

        setupFieldErrorClearing();
        applyDialogMode();
    }

    // ---- Password visibility toggles ----
    @FXML
    private void togglePasswordVisibility() {
        boolean show = !visiblePasswordField.isVisible();
        visiblePasswordField.setVisible(show);
        visiblePasswordField.setManaged(show);
        txtPassword.setVisible(!show);
        txtPassword.setManaged(!show);
        eyeIcon.setIconLiteral(show ? "fas-eye-slash" : "fas-eye");
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        boolean show = !visibleConfirmPasswordField.isVisible();
        visibleConfirmPasswordField.setVisible(show);
        visibleConfirmPasswordField.setManaged(show);
        txtConfirmPassword.setVisible(!show);
        txtConfirmPassword.setManaged(!show);
        confirmEyeIcon.setIconLiteral(show ? "fas-eye-slash" : "fas-eye");
    }

    // ---- rest of the controller (unchanged except small tweaks) ----
    private void setupFieldErrorClearing() {
        bindClearError(txtFullName, lblFullNameError);
        bindClearError(txtTeacherCode, lblTeacherCodeError);
        bindClearError(txtUsername, lblUsernameError);
        bindClearError(txtPassword, lblPasswordError);
        bindClearError(txtConfirmPassword, lblConfirmPasswordError);
        bindClearError(txtEmail, lblEmailError);
        bindClearError(txtPhone, lblPhoneError);
        bindClearError(txtSalary, lblSalaryError);
        dpHireDate.valueProperty().addListener((obs, oldVal, newVal) -> clearFieldError(lblHireDateError));
    }

    private void bindClearError(TextInputControl field, Label errorLabel) {
        field.textProperty().addListener((obs, oldVal, newVal) -> clearFieldError(errorLabel));
    }

    private void applyDialogMode() {
        boolean isView = (dialogMode == DialogMode.VIEW);
        boolean isEdit = (dialogMode == DialogMode.EDIT);
        boolean isAdd = (dialogMode == DialogMode.ADD);

        if (lblDialogTitle != null) {
            if (isView) lblDialogTitle.setText("Teacher Details");
            else if (isEdit) lblDialogTitle.setText("Update Teacher");
            else lblDialogTitle.setText("Add New Teacher");
        }
        if (lblDialogSubtitle != null) {
            if (isView) lblDialogSubtitle.setText("View teacher profile and account information");
            else if (isEdit) lblDialogSubtitle.setText("Update teacher profile and login account");
            else lblDialogSubtitle.setText("Create a teacher account and profile");
        }
        if (lblPasswordHint != null) {
            lblPasswordHint.setText(isEdit ? "Leave blank to keep current password" : "Minimum 8 characters");
        }
        if (lblPasswordRules != null) {
            lblPasswordRules.setManaged(isAdd || isEdit);
            lblPasswordRules.setVisible(isAdd || isEdit);
        }
        if (btnSave != null) {
            btnSave.setText(isEdit ? "Update Teacher" : "Save Teacher");
            btnSave.setVisible(!isView);
            btnSave.setManaged(!isView);
        }
        if (btnCancel != null) {
            btnCancel.setText(isView ? "Close" : "Cancel");
        }
        setFormEditable(!isView);
    }

    private void setFormEditable(boolean editable) {
        btnChoosePhoto.setDisable(!editable);
        txtFullName.setEditable(editable);
        txtTeacherCode.setEditable(editable);
        txtUsername.setEditable(editable);
        txtPassword.setEditable(editable);
        txtConfirmPassword.setEditable(editable);
        txtEmail.setEditable(editable);
        txtPhone.setEditable(editable);
        cmbGender.setDisable(!editable);
        dpHireDate.setDisable(!editable);
        txtSalary.setEditable(editable);
        cmbClassLeaderOf.setDisable(!editable);
        txtAddress.setEditable(editable);
        for (javafx.scene.Node node : subjectsBox.getChildren()) {
            if (node instanceof CheckBox) node.setDisable(!editable);
        }
    }

    @FXML
    private void choosePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Teacher Photo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = chooser.showOpenDialog(dialogStage);
        if (selectedFile == null) return;

        try {
            Path photoDirectory = Paths.get(System.getProperty("user.dir"), "teacher_photos");
            Files.createDirectories(photoDirectory);
            String originalFileName = selectedFile.getName();
            int extensionIndex = originalFileName.lastIndexOf(".");
            String extension = extensionIndex >= 0 ? originalFileName.substring(extensionIndex) : ".png";
            String newFileName = "teacher_" + System.currentTimeMillis() + extension;
            Path destination = photoDirectory.resolve(newFileName);
            Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            selectedPhotoPath = destination.toAbsolutePath().toString();
            showPhotoPreview(selectedPhotoPath);
            lblPhotoFileName.setText(selectedFile.getName());
        } catch (IOException e) {
            e.printStackTrace();
            showFieldError(lblPhotoError, btnChoosePhoto, "Could not copy the selected photo.");
        }
    }

    @FXML
    private void save() {
        if (dialogMode == DialogMode.VIEW) return;
        hideAllErrors();

        String fullName = getText(txtFullName);
        String teacherCode = getText(txtTeacherCode);
        String username = getText(txtUsername);
        String password = txtPassword.getText() == null ? "" : txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText() == null ? "" : txtConfirmPassword.getText();
        String email = getText(txtEmail);
        String phone = getText(txtPhone);
        String gender = cmbGender.getValue();
        LocalDate hireDate = dpHireDate.getValue();
        String salaryText = getText(txtSalary);
        String address = emptyToNull(txtAddress.getText());

        if (fullName.isEmpty()) {
            showFieldError(lblFullNameError, txtFullName, "Full name is required.");
            return;
        }
        if (username.isEmpty()) {
            showFieldError(lblUsernameError, txtUsername, "Username is required.");
            return;
        }

        boolean isEdit = (dialogMode == DialogMode.EDIT);
        if (!isEdit) {
            if (password.isEmpty()) {
                showFieldError(lblPasswordError, txtPassword, "Password is required.");
                return;
            }
            if (confirmPassword.isEmpty()) {
                showFieldError(lblConfirmPasswordError, txtConfirmPassword, "Confirm password is required.");
                return;
            }
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            showFieldError(lblUsernameError, txtUsername,
                    "Username must contain 3 to 30 characters. Use only letters, numbers, dot, underscore, or hyphen.");
            return;
        }
        int excludeUserId = isEdit ? editingUserId : -1;
        if (teacherDAO.isUsernameTaken(username, excludeUserId)) {
            showFieldError(lblUsernameError, txtUsername, "This username is already taken.");
            return;
        }

        String passwordToSave = null;
        if (!password.isEmpty()) {
            String passwordError = validatePassword(password);
            if (passwordError != null) {
                showFieldError(lblPasswordError, txtPassword, passwordError);
                return;
            }
            if (!password.equals(confirmPassword)) {
                showFieldError(lblConfirmPasswordError, txtConfirmPassword,
                        "Password and confirm password do not match.");
                return;
            }
            passwordToSave = password;
        } else if (!isEdit) {
            showFieldError(lblPasswordError, txtPassword, "Password is required.");
            return;
        }

        if (!teacherCode.isEmpty()) {
            if (!TEACHER_CODE_PATTERN.matcher(teacherCode).matches()) {
                showFieldError(lblTeacherCodeError, txtTeacherCode,
                        "Teacher code must contain 2 to 20 letters, numbers, or hyphens.");
                return;
            }
            int excludeTeacherId = isEdit ? editingTeacherId : -1;
            if (teacherDAO.isTeacherCodeTaken(teacherCode, excludeTeacherId)) {
                showFieldError(lblTeacherCodeError, txtTeacherCode, "This teacher code is already in use.");
                return;
            }
        }

        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            showFieldError(lblEmailError, txtEmail,
                    "Please enter a valid email address, for example teacher@gmail.com.");
            return;
        }
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            showFieldError(lblPhoneError, txtPhone, "Please enter a valid phone number.");
            return;
        }
        if (hireDate != null && hireDate.isAfter(LocalDate.now())) {
            showFieldError(lblHireDateError, dpHireDate, "Hire date cannot be in the future.");
            return;
        }

        BigDecimal salary = null;
        if (!salaryText.isEmpty()) {
            try {
                salary = new BigDecimal(salaryText);
                if (salary.compareTo(BigDecimal.ZERO) < 0) {
                    showFieldError(lblSalaryError, txtSalary, "Salary cannot be negative.");
                    return;
                }
            } catch (NumberFormatException e) {
                showFieldError(lblSalaryError, txtSalary,
                        "Salary must be a valid number, for example 750000.");
                return;
            }
        }

        List<Integer> subjectIds = new ArrayList<>();
        if (subjectOptions != null) {
            for (SubjectOption option : subjectOptions) {
                if (option.isSelected()) subjectIds.add(option.getSubjectId());
            }
        }

        Batch classLeader = cmbClassLeaderOf.getValue();
        int classLeaderId = (classLeader == null) ? -1 : classLeader.getId();

        String photoPath = (selectedPhotoPath != null) ? selectedPhotoPath : existingPhotoPath;

        boolean success;
        if (isEdit) {
            success = teacherDAO.updateTeacher(
                    editingTeacherId, editingUserId,
                    fullName, username, passwordToSave,
                    emptyToNull(teacherCode),
                    emptyToNull(email),
                    emptyToNull(phone),
                    gender, address, hireDate, salary,
                    photoPath, subjectIds, classLeaderId
            );
            if (!success) {
                showFieldError(lblUsernameError, txtUsername,
                        "Could not update this teacher. The username or teacher code may already exist.");
                return;
            }
        } else {
            if (passwordToSave == null) {
                showFieldError(lblPasswordError, txtPassword, "Password is required.");
                return;
            }
            int newTeacherId = teacherDAO.addTeacher(
                    fullName, username, passwordToSave,
                    emptyToNull(teacherCode),
                    emptyToNull(email),
                    emptyToNull(phone),
                    gender, address, hireDate, salary,
                    photoPath, subjectIds, classLeaderId
            );
            if (newTeacherId == -1) {
                showFieldError(lblUsernameError, txtUsername,
                        "Could not save this teacher. The username or teacher code may already exist.");
                return;
            }
        }

        saved = true;
        if (dialogStage != null) dialogStage.close();
    }

    private String validatePassword(String password) {
        if (password.length() < 8) return "Password must contain at least 8 characters.";
        if (!password.matches(".*[A-Z].*")) return "Password must contain at least one uppercase letter.";
        if (!password.matches(".*[a-z].*")) return "Password must contain at least one lowercase letter.";
        if (!password.matches(".*\\d.*")) return "Password must contain at least one number.";
        if (!password.matches(".*[^A-Za-z0-9\\s].*")) return "Password must contain at least one special character.";
        return null;
    }

    private void showPhotoPreview(String photoPath) {
        if (photoPath == null || photoPath.isBlank()) {
            photoPreview.setImage(null);
            photoPreview.setVisible(false);
            photoPlaceholderCircle.setVisible(true);
            lblPhotoFileName.setText("No photo selected");
            return;
        }
        File imageFile = new File(photoPath);
        if (!imageFile.exists()) {
            photoPreview.setImage(null);
            photoPreview.setVisible(false);
            photoPlaceholderCircle.setVisible(true);
            lblPhotoFileName.setText("Photo file not found");
            return;
        }
        Image image = new Image(imageFile.toURI().toString(), 72, 72, true, true);
        photoPreview.setImage(image);
        photoPreview.setClip(new Circle(36, 36, 36));
        photoPreview.setVisible(true);
        photoPlaceholderCircle.setVisible(false);
        lblPhotoFileName.setText(imageFile.getName());
    }

    private void selectClassLeader(int classLeaderId) {
        if (classLeaderId == -1) {
            cmbClassLeaderOf.getSelectionModel().select(NO_CLASS_LEADER);
            return;
        }
        for (Batch batch : cmbClassLeaderOf.getItems()) {
            if (batch.getId() == classLeaderId) {
                cmbClassLeaderOf.getSelectionModel().select(batch);
                return;
            }
        }
        cmbClassLeaderOf.getSelectionModel().select(NO_CLASS_LEADER);
    }

    private String getText(TextField field) {
        return (field == null || field.getText() == null) ? "" : field.getText().trim();
    }

    private String emptyToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private void hideAllErrors() {
        clearFieldError(lblPhotoError);
        clearFieldError(lblFullNameError);
        clearFieldError(lblTeacherCodeError);
        clearFieldError(lblUsernameError);
        clearFieldError(lblPasswordError);
        clearFieldError(lblConfirmPasswordError);
        clearFieldError(lblEmailError);
        clearFieldError(lblPhoneError);
        clearFieldError(lblHireDateError);
        clearFieldError(lblSalaryError);
    }

    private void clearFieldError(Label errorLabel) {
        if (errorLabel == null) return;
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void showFieldError(Label errorLabel, javafx.scene.Node focusNode, String message) {
        hideAllErrors();
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        if (focusNode != null) focusNode.requestFocus();
    }

    @FXML
    private void cancel() {
        if (dialogStage != null) dialogStage.close();
    }
}