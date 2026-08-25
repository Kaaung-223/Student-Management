package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.ClassDAO;
import com.example.student_management_system.Controller.DAO.SubjectDAO;
import com.example.student_management_system.Controller.DAO.TeacherDAO;
import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.SubjectOption;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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

public class AddTeacherDialogController implements Initializable {

    @FXML private Circle photoPlaceholderCircle;
    @FXML private ImageView photoPreview;
    @FXML private Button btnChoosePhoto;
    @FXML private Label lblPhotoFileName;

    @FXML private TextField txtFullName;
    @FXML private TextField txtTeacherCode;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private ComboBox<String> cmbGender;
    @FXML private DatePicker dpHireDate;
    @FXML private TextField txtSalary;
    @FXML private ComboBox<Batch> cmbClassLeaderOf;
    @FXML private TextArea txtAddress;
    @FXML private javafx.scene.layout.VBox subjectsBox;
    @FXML private Label lblFormError;
    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final ClassDAO classDAO = new ClassDAO();

    private static final Batch NO_CLASS_LEADER = new Batch(-1, "None");

    private Stage dialogStage;
    private String selectedPhotoPath = null; // absolute path to the COPY inside teacher_photos/, not the original file
    private List<SubjectOption> subjectOptions;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
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
            CheckBox cb = new CheckBox(option.getSubjectName());
            cb.setStyle("-fx-font-size: 12px; -fx-text-fill: #344563;");
            cb.selectedProperty().bindBidirectional(option.selectedProperty());
            subjectsBox.getChildren().add(cb);
        }
    }

    @FXML
    private void choosePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Teacher Photo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File chosen = chooser.showOpenDialog(dialogStage);
        if (chosen == null) return;

        try {
            Path photosDir = Paths.get(System.getProperty("user.dir"), "teacher_photos");
            Files.createDirectories(photosDir);

            String ext = chosen.getName().substring(chosen.getName().lastIndexOf('.'));
            String newFileName = "teacher_" + System.currentTimeMillis() + ext;
            Path destination = photosDir.resolve(newFileName);

            Files.copy(chosen.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            selectedPhotoPath = destination.toAbsolutePath().toString();

            photoPreview.setImage(new Image(destination.toUri().toString(), 72, 72, true, true));
            photoPreview.setClip(new Circle(36, 36, 36));
            photoPreview.setVisible(true);
            photoPlaceholderCircle.setVisible(false);
            lblPhotoFileName.setText(chosen.getName());

        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not copy the selected photo. Please try a different file.");
        }
    }

    @FXML
    private void save() {
        String fullName = txtFullName.getText() != null ? txtFullName.getText().trim() : "";
        String username = txtUsername.getText() != null ? txtUsername.getText().trim() : "";
        String password = txtPassword.getText() != null ? txtPassword.getText() : "";
        String teacherCode = emptyToNull(txtTeacherCode.getText());
        String email = emptyToNull(txtEmail.getText());
        String phone = emptyToNull(txtPhone.getText());
        String gender = cmbGender.getValue();
        String address = emptyToNull(txtAddress.getText());
        LocalDate hireDate = dpHireDate.getValue();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showError("Full name, username, and password are required.");
            return;
        }

        BigDecimal salary = null;
        String salaryText = txtSalary.getText() != null ? txtSalary.getText().trim() : "";
        if (!salaryText.isEmpty()) {
            try {
                salary = new BigDecimal(salaryText);
            } catch (NumberFormatException e) {
                showError("Salary must be a valid number, e.g. 750000.");
                return;
            }
        }

        List<Integer> subjectIds = new ArrayList<>();
        for (SubjectOption option : subjectOptions) {
            if (option.isSelected()) {
                subjectIds.add(option.getSubjectId());
            }
        }

        Batch classLeaderSelection = cmbClassLeaderOf.getValue();
        int classLeaderOfClassId = (classLeaderSelection == null) ? -1 : classLeaderSelection.getId();

        int newTeacherId = teacherDAO.addTeacher(
                fullName, username, password, teacherCode, email, phone, gender, address,
                hireDate, salary, selectedPhotoPath, subjectIds, classLeaderOfClassId);

        if (newTeacherId == -1) {
            showError("Could not save this teacher. The username or teacher code may already be in use.");
            return;
        }

        dialogStage.close();
    }

    @FXML
    private void cancel() {
        dialogStage.close();
    }

    private void showError(String message) {
        lblFormError.setText(message);
        lblFormError.setVisible(true);
        lblFormError.setManaged(true);
    }

    private String emptyToNull(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
