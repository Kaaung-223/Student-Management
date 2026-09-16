package com.example.student_management_system.Controller.Staff;

import com.example.student_management_system.Controller.DAO.DBConnention;
import com.example.student_management_system.Controller.DAO.StaffStudentCreateDAO;
import com.example.student_management_system.Controller.Model.Batch;

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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AddStudentDialogController implements Initializable {

    public enum DialogMode { ADD, EDIT, VIEW }

    // Header
    @FXML private Label lblDialogTitle;
    @FXML private Label lblDialogSubtitle;

    // Photo
    @FXML private Circle photoPlaceholderCircle;
    @FXML private ImageView photoPreview;
    @FXML private Button btnChoosePhoto;
    @FXML private Label lblPhotoFileName;
    @FXML private Label lblPhotoError;

    // Form
    @FXML private TextField txtStudentCode;
    @FXML private Label lblStudentCodeError;

    @FXML private TextField txtStudentName;
    @FXML private Label lblStudentNameError;

    @FXML private TextField txtEmail;
    @FXML private Label lblEmailError;

    @FXML private TextField txtPhone;
    @FXML private Label lblPhoneError;

    @FXML private TextField txtAge;
    @FXML private Label lblAgeError;

    @FXML private ComboBox<String> cmbGender;

    @FXML private ComboBox<Batch> cmbClass;
    @FXML private Label lblClassError;

    @FXML private DatePicker dpAdmissionDate;
    @FXML private Label lblAdmissionDateError;

    @FXML private ComboBox<String> cmbStatus;

    @FXML private TextArea txtAddress;

    // Actions
    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    private final StaffStudentCreateDAO createDAO = new StaffStudentCreateDAO();

    private Stage dialogStage;
    private DialogMode dialogMode = DialogMode.ADD;
    private String selectedPhotoPath;
    private boolean saved;

    private static final Pattern CODE_PATTERN  = Pattern.compile("^[A-Za-z0-9-]{2,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(09|\\+959)[0-9\\s-]{7,15}$");

    public void setDialogStage(Stage stage) { this.dialogStage = stage; }
    public void setDialogMode(DialogMode mode) { this.dialogMode = mode; applyMode(); }
    public boolean wasSaved() { return saved; }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbGender.setItems(FXCollections.observableArrayList("Male", "Female"));
        cmbStatus.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));
        cmbStatus.setValue("ACTIVE");

        cmbClass.setItems(FXCollections.observableArrayList(loadBatches()));

        dpAdmissionDate.setValue(LocalDate.now());

        btnSave.setDefaultButton(true);
        setupErrorClearing();
        applyMode();
    }

    private List<Batch> loadBatches() {
        List<Batch> list = new ArrayList<>();
        String sql = "SELECT class_id, class_name FROM classes ORDER BY class_name";
        try (Connection con = DBConnention.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new Batch(rs.getInt("class_id"), rs.getString("class_name")));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private void applyMode() {
        boolean view = dialogMode == DialogMode.VIEW;

        if (lblDialogTitle != null)
            lblDialogTitle.setText(view ? "Student Details" : "Add New Student");
        if (lblDialogSubtitle != null)
            lblDialogSubtitle.setText(view ? "View student information"
                    : "Create a new student profile");

        if (btnSave != null) {
            btnSave.setVisible(!view);
            btnSave.setManaged(!view);
        }
        if (btnCancel != null) btnCancel.setText(view ? "Close" : "Cancel");

        setEditable(!view);
    }

    private void setEditable(boolean e) {
        btnChoosePhoto.setDisable(!e);
        txtStudentCode.setEditable(e);
        txtStudentName.setEditable(e);
        txtEmail.setEditable(e);
        txtPhone.setEditable(e);
        txtAge.setEditable(e);
        cmbGender.setDisable(!e);
        cmbClass.setDisable(!e);
        dpAdmissionDate.setDisable(!e);
        cmbStatus.setDisable(!e);
        txtAddress.setEditable(e);
    }

    // =========================================================
    //  PHOTO CHOOSER
    // =========================================================
    @FXML
    private void choosePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Student Photo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File selectedFile = chooser.showOpenDialog(dialogStage);
        if (selectedFile == null) return;

        try {
            Path photoDirectory = Paths.get(System.getProperty("user.dir"), "student_photos");
            Files.createDirectories(photoDirectory);

            String originalFileName = selectedFile.getName();
            int dot = originalFileName.lastIndexOf(".");
            String extension = dot >= 0 ? originalFileName.substring(dot) : ".png";
            String newFileName = "student_" + System.currentTimeMillis() + extension;
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

    // =========================================================
    //  SAVE
    // =========================================================
    @FXML
    private void save() {
        if (dialogMode == DialogMode.VIEW) return;
        hideAllErrors();

        String code   = getText(txtStudentCode);
        String name   = getText(txtStudentName);
        String email  = getText(txtEmail);
        String phone  = getText(txtPhone);
        String ageStr = getText(txtAge);
        String gender = cmbGender.getValue();
        Batch cls     = cmbClass.getValue();
        LocalDate ad  = dpAdmissionDate.getValue();
        String status = cmbStatus.getValue();
        String addr   = txtAddress.getText();

        // --- validation ---
        if (code.isEmpty()) {
            showError(lblStudentCodeError, txtStudentCode, "Student code is required.");
            return;
        }
        if (!CODE_PATTERN.matcher(code).matches()) {
            showError(lblStudentCodeError, txtStudentCode,
                    "Code must be 2-20 letters, numbers, or hyphens.");
            return;
        }
        if (createDAO.isStudentCodeTaken(code)) {
            showError(lblStudentCodeError, txtStudentCode, "This student code is already in use.");
            return;
        }

        if (name.isEmpty()) {
            showError(lblStudentNameError, txtStudentName, "Student name is required.");
            return;
        }

        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            showError(lblEmailError, txtEmail, "Enter a valid email, e.g. student@gmail.com.");
            return;
        }
        if (!email.isEmpty() && createDAO.isEmailTaken(email)) {
            showError(lblEmailError, txtEmail, "This email is already in use.");
            return;
        }

        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            showError(lblPhoneError, txtPhone, "Enter a valid phone number.");
            return;
        }

        Integer age = null;
        if (!ageStr.isEmpty()) {
            try {
                age = Integer.parseInt(ageStr);
                if (age < 5 || age > 100) {
                    showError(lblAgeError, txtAge, "Age must be between 5 and 100.");
                    return;
                }
            } catch (NumberFormatException e) {
                showError(lblAgeError, txtAge, "Age must be a number.");
                return;
            }
        }

        if (cls == null || cls.getId() == -1) {
            showError(lblClassError, cmbClass, "Please select a batch.");
            return;
        }

        if (ad != null && ad.isAfter(LocalDate.now())) {
            showError(lblAdmissionDateError, dpAdmissionDate,
                    "Admission date cannot be in the future.");
            return;
        }

        // --- insert ---
        int newId = createDAO.addStudent(
                code, name, email, phone, age, gender, addr,
                cls.getId(), ad, status, selectedPhotoPath);

        if (newId == -1) {
            showError(lblStudentCodeError, txtStudentCode,
                    "Could not save student. The code or email may already exist.");
            return;
        }

        saved = true;
        if (dialogStage != null) dialogStage.close();
    }

    @FXML
    private void cancel() {
        if (dialogStage != null) dialogStage.close();
    }

    // =========================================================
    //  Helpers
    // =========================================================
    private void setupErrorClearing() {
        bindClear(txtStudentCode, lblStudentCodeError);
        bindClear(txtStudentName, lblStudentNameError);
        bindClear(txtEmail, lblEmailError);
        bindClear(txtPhone, lblPhoneError);
        bindClear(txtAge, lblAgeError);
        dpAdmissionDate.valueProperty().addListener((o, a, b) -> clear(lblAdmissionDateError));
        cmbClass.valueProperty().addListener((o, a, b) -> clear(lblClassError));
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
        clear(lblStudentCodeError); clear(lblStudentNameError);
        clear(lblEmailError); clear(lblPhoneError); clear(lblAgeError);
        clear(lblClassError); clear(lblAdmissionDateError);
    }

    private void showError(Label l, Node focus, String msg) {
        hideAllErrors();
        l.setText(msg); l.setVisible(true); l.setManaged(true);
        if (focus != null) focus.requestFocus();
    }

    private String getText(TextField f) {
        return (f == null || f.getText() == null) ? "" : f.getText().trim();
    }
}