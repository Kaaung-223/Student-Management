package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.StaffDAO;
import com.example.student_management_system.Controller.Model.Staff;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AddStaffDialogController implements Initializable {

    public enum DialogMode { ADD, EDIT, VIEW }

    // Header
    @FXML private Label lblDialogTitle;
    @FXML private Label lblDialogSubtitle;

    // ✅ Photo
    @FXML private Circle photoPlaceholderCircle;
    @FXML private ImageView photoPreview;
    @FXML private Button btnChoosePhoto;
    @FXML private Label lblPhotoFileName;
    @FXML private Label lblPhotoError;

    // Account
    @FXML private TextField txtFullName;
    @FXML private Label lblFullNameError;
    @FXML private TextField txtStaffCode;
    @FXML private Label lblStaffCodeError;
    @FXML private TextField txtUsername;
    @FXML private Label lblUsernameError;

    @FXML private PasswordField txtPassword;
    @FXML private TextField     visiblePasswordField;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField     visibleConfirmPasswordField;
    @FXML private Button btnEye;
    @FXML private Button btnConfirmEye;
    @FXML private FontIcon eyeIcon;
    @FXML private FontIcon confirmEyeIcon;

    @FXML private Label lblPasswordHint;
    @FXML private Label lblPasswordRules;
    @FXML private Label lblPasswordError;
    @FXML private Label lblConfirmPasswordError;

    // Profile
    @FXML private TextField txtEmail;
    @FXML private Label lblEmailError;
    @FXML private TextField txtPhone;
    @FXML private Label lblPhoneError;
    @FXML private ComboBox<String> cmbGender;
    @FXML private DatePicker dpHireDate;
    @FXML private Label lblHireDateError;
    @FXML private TextField txtSalary;              // ✅ NEW
    @FXML private Label lblSalaryError;             // ✅ NEW
    @FXML private TextArea txtAddress;

    // Actions
    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    private final StaffDAO staffDAO = new StaffDAO();

    private Stage dialogStage;
    private DialogMode dialogMode = DialogMode.ADD;
    private int editingStaffId = -1;
    private int editingUserId = -1;
    private String selectedPhotoPath;
    private String existingPhotoPath;
    private boolean saved;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{3,30}$");
    private static final Pattern STAFF_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9-]{2,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(09|\\+959)[0-9\\s-]{7,15}$");

    public void setDialogStage(Stage stage) { this.dialogStage = stage; }
    public void setDialogMode(DialogMode mode) { this.dialogMode = mode; applyMode(); }
    public boolean wasSaved() { return saved; }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbGender.setItems(FXCollections.observableArrayList("Male", "Female"));

        visiblePasswordField.textProperty().bindBidirectional(txtPassword.textProperty());
        visibleConfirmPasswordField.textProperty().bindBidirectional(txtConfirmPassword.textProperty());

        btnSave.setDefaultButton(true);
        txtConfirmPassword.setOnAction(e -> save());

        setupErrorClearing();
        applyMode();
    }

    // ---------------- password visibility ----------------
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

    // ---------------- photo ----------------
    @FXML
    private void choosePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Staff Photo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File selectedFile = chooser.showOpenDialog(dialogStage);
        if (selectedFile == null) return;

        try {
            Path photoDirectory = Paths.get(System.getProperty("user.dir"), "staff_photos");
            Files.createDirectories(photoDirectory);

            String originalFileName = selectedFile.getName();
            int dot = originalFileName.lastIndexOf(".");
            String extension = dot >= 0 ? originalFileName.substring(dot) : ".png";
            String newFileName = "staff_" + System.currentTimeMillis() + extension;
            Path destination = photoDirectory.resolve(newFileName);

            Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            selectedPhotoPath = destination.toAbsolutePath().toString();
            showPhotoPreview(selectedPhotoPath);
            lblPhotoFileName.setText(selectedFile.getName());

        } catch (IOException e) {
            e.printStackTrace();
            showError(lblPhotoError, btnChoosePhoto, "Could not copy the selected photo.");
        }
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
        Image image = new Image(imageFile.toURI().toString(), 160, 160, false, true, true);
        photoPreview.setImage(image);
        photoPreview.setVisible(true);
        photoPlaceholderCircle.setVisible(false);
        lblPhotoFileName.setText(imageFile.getName());
    }

    // ---------------- load ----------------
    public void loadStaff(Staff s) {
        if (s == null) return;

        editingStaffId = s.getId();
        editingUserId  = s.getUserId();

        txtFullName.setText(valueOrEmpty(s.getStaffName()));
        txtStaffCode.setText(valueOrEmpty(s.getStaffCode()));
        txtUsername.setText(valueOrEmpty(s.getUsername()));
        txtEmail.setText(valueOrEmpty(s.getEmail()));
        txtPhone.setText(valueOrEmpty(s.getPhone()));
        txtAddress.setText(valueOrEmpty(s.getAddress()));

        if (s.getGender() != null && !s.getGender().isBlank())
            cmbGender.setValue(s.getGender());

        dpHireDate.setValue(s.getHireDate());

        if (s.getSalary() != null) txtSalary.setText(s.getSalary().toPlainString());
        else                       txtSalary.clear();

        existingPhotoPath = s.getPhotoPath();
        selectedPhotoPath = existingPhotoPath;
        showPhotoPreview(existingPhotoPath);

        applyMode();
    }

    // ---------------- mode ----------------
    private void applyMode() {
        boolean view = dialogMode == DialogMode.VIEW;
        boolean edit = dialogMode == DialogMode.EDIT;

        if (lblDialogTitle != null) {
            lblDialogTitle.setText(view ? "Staff Details" : edit ? "Update Staff" : "Add New Staff");
        }
        if (lblDialogSubtitle != null) {
            lblDialogSubtitle.setText(view ? "View staff profile and account"
                    : edit ? "Update staff profile and login account"
                    : "Create a staff account and profile");
        }
        if (lblPasswordHint != null) {
            lblPasswordHint.setText(edit ? "Leave blank to keep current password" : "Minimum 8 characters");
        }
        if (lblPasswordRules != null) {
            lblPasswordRules.setVisible(!view);
            lblPasswordRules.setManaged(!view);
        }
        if (btnSave != null) {
            btnSave.setText(edit ? "Update Staff" : "Save Staff");
            btnSave.setVisible(!view);
            btnSave.setManaged(!view);
        }
        if (btnCancel != null) btnCancel.setText(view ? "Close" : "Cancel");

        setEditable(!view);
    }

    private void setEditable(boolean e) {
        btnChoosePhoto.setDisable(!e);
        txtFullName.setEditable(e);
        txtStaffCode.setEditable(e);
        txtUsername.setEditable(e);
        txtPassword.setEditable(e);
        txtConfirmPassword.setEditable(e);
        txtEmail.setEditable(e);
        txtPhone.setEditable(e);
        cmbGender.setDisable(!e);
        dpHireDate.setDisable(!e);
        txtSalary.setEditable(e);
        txtAddress.setEditable(e);
        btnEye.setDisable(!e);
        btnConfirmEye.setDisable(!e);
    }

    // ---------------- save ----------------
    @FXML
    private void save() {
        if (dialogMode == DialogMode.VIEW) return;
        hideAllErrors();

        String fullName = getText(txtFullName);
        String code = getText(txtStaffCode);
        String username = getText(txtUsername);
        String password = txtPassword.getText() == null ? "" : txtPassword.getText();
        String confirm  = txtConfirmPassword.getText() == null ? "" : txtConfirmPassword.getText();
        String email = getText(txtEmail);
        String phone = getText(txtPhone);
        String gender = cmbGender.getValue();
        LocalDate hireDate = dpHireDate.getValue();
        String salaryText = getText(txtSalary);
        String address = emptyToNull(txtAddress.getText());

        boolean edit = dialogMode == DialogMode.EDIT;

        if (fullName.isEmpty()) {
            showError(lblFullNameError, txtFullName, "Full name is required.");
            return;
        }
        if (username.isEmpty()) {
            showError(lblUsernameError, txtUsername, "Username is required.");
            return;
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            showError(lblUsernameError, txtUsername,
                    "Username must be 3-30 chars: letters, numbers, dot, underscore, hyphen.");
            return;
        }

        int excludeUserId = edit ? editingUserId : -1;
        if (staffDAO.isUsernameTaken(username, excludeUserId)) {
            showError(lblUsernameError, txtUsername, "This username is already taken.");
            return;
        }

        String passwordToSave = null;
        if (!password.isEmpty()) {
            String err = validatePassword(password);
            if (err != null) {
                showError(lblPasswordError, txtPassword, err);
                return;
            }
            if (!password.equals(confirm)) {
                showError(lblConfirmPasswordError, txtConfirmPassword, "Passwords do not match.");
                return;
            }
            passwordToSave = password;
        } else if (!edit) {
            showError(lblPasswordError, txtPassword, "Password is required.");
            return;
        }

        if (!code.isEmpty()) {
            if (!STAFF_CODE_PATTERN.matcher(code).matches()) {
                showError(lblStaffCodeError, txtStaffCode,
                        "Staff code must be 2-20 letters, numbers, or hyphens.");
                return;
            }
            int excludeStaffId = edit ? editingStaffId : -1;
            if (staffDAO.isStaffCodeTaken(code, excludeStaffId)) {
                showError(lblStaffCodeError, txtStaffCode, "This staff code is already in use.");
                return;
            }
        }

        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            showError(lblEmailError, txtEmail, "Enter a valid email, e.g. staff@gmail.com.");
            return;
        }
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            showError(lblPhoneError, txtPhone, "Enter a valid phone number.");
            return;
        }
        if (hireDate != null && hireDate.isAfter(LocalDate.now())) {
            showError(lblHireDateError, dpHireDate, "Hire date cannot be in the future.");
            return;
        }

        // ✅ salary validation
        BigDecimal salary = null;
        if (!salaryText.isEmpty()) {
            try {
                salary = new BigDecimal(salaryText);
                if (salary.compareTo(BigDecimal.ZERO) < 0) {
                    showError(lblSalaryError, txtSalary, "Salary cannot be negative.");
                    return;
                }
            } catch (NumberFormatException e) {
                showError(lblSalaryError, txtSalary, "Salary must be a valid number, e.g. 500000.");
                return;
            }
        }

        String photoPath = (selectedPhotoPath != null) ? selectedPhotoPath : existingPhotoPath;

        boolean ok;
        if (edit) {
            ok = staffDAO.updateStaff(
                    editingStaffId, editingUserId,
                    fullName, username, passwordToSave,
                    emptyToNull(code), emptyToNull(email), emptyToNull(phone),
                    gender, address, photoPath, salary, hireDate);
            if (!ok) {
                showError(lblUsernameError, txtUsername,
                        "Could not update. Username or staff code may already exist.");
                return;
            }
        } else {
            int newId = staffDAO.addStaff(
                    fullName, username, passwordToSave,
                    emptyToNull(code), emptyToNull(email), emptyToNull(phone),
                    gender, address, photoPath, salary, hireDate);
            if (newId == -1) {
                showError(lblUsernameError, txtUsername,
                        "Could not save. Username or staff code may already exist.");
                return;
            }
        }

        saved = true;
        if (dialogStage != null) dialogStage.close();
    }

    @FXML
    private void cancel() {
        if (dialogStage != null) dialogStage.close();
    }

    // ---------------- helpers ----------------
    private String validatePassword(String pwd) {
        if (pwd.length() < 8) return "Password must be at least 8 characters.";
        if (!pwd.matches(".*[A-Z].*")) return "Must include an uppercase letter.";
        if (!pwd.matches(".*[a-z].*")) return "Must include a lowercase letter.";
        if (!pwd.matches(".*\\d.*")) return "Must include a number.";
        if (!pwd.matches(".*[^A-Za-z0-9\\s].*")) return "Must include a special character.";
        return null;
    }

    private void setupErrorClearing() {
        bindClear(txtFullName, lblFullNameError);
        bindClear(txtStaffCode, lblStaffCodeError);
        bindClear(txtUsername, lblUsernameError);
        bindClear(txtPassword, lblPasswordError);
        bindClear(txtConfirmPassword, lblConfirmPasswordError);
        bindClear(txtEmail, lblEmailError);
        bindClear(txtPhone, lblPhoneError);
        bindClear(txtSalary, lblSalaryError);   // ✅
        dpHireDate.valueProperty().addListener((o, a, b) -> clear(lblHireDateError));
    }

    private void bindClear(TextInputControl f, Label l) {
        f.textProperty().addListener((o, a, b) -> clear(l));
    }

    private void clear(Label l) {
        if (l == null) return;
        l.setText(""); l.setVisible(false); l.setManaged(false);
    }

    private void hideAllErrors() {
        clear(lblPhotoError);
        clear(lblFullNameError); clear(lblStaffCodeError); clear(lblUsernameError);
        clear(lblPasswordError); clear(lblConfirmPasswordError);
        clear(lblEmailError); clear(lblPhoneError); clear(lblHireDateError);
        clear(lblSalaryError);   // ✅
    }

    private void showError(Label l, Node focus, String msg) {
        hideAllErrors();
        l.setText(msg); l.setVisible(true); l.setManaged(true);
        if (focus != null) focus.requestFocus();
    }

    private String getText(TextField f) {
        return (f == null || f.getText() == null) ? "" : f.getText().trim();
    }

    private String emptyToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private String valueOrEmpty(String v) { return v == null ? "" : v; }
}