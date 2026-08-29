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

    @FXML
    private Circle photoPlaceholderCircle;

    @FXML
    private ImageView photoPreview;

    @FXML
    private Button btnChoosePhoto;

    @FXML
    private Label lblPhotoFileName;

    @FXML
    private TextField txtFullName;

    @FXML
    private TextField txtTeacherCode;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtPhone;

    @FXML
    private ComboBox<String> cmbGender;

    @FXML
    private DatePicker dpHireDate;

    @FXML
    private TextField txtSalary;

    @FXML
    private ComboBox<Batch> cmbClassLeaderOf;

    @FXML
    private TextArea txtAddress;

    @FXML
    private javafx.scene.layout.VBox subjectsBox;

    @FXML
    private Label lblFormError;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnSave;

    private final TeacherDAO teacherDAO =
            new TeacherDAO();

    private final SubjectDAO subjectDAO =
            new SubjectDAO();

    private final ClassDAO classDAO =
            new ClassDAO();

    private static final Batch NO_CLASS_LEADER =
            new Batch(-1, "None");

    private Stage dialogStage;

    private String selectedPhotoPath;

    private List<SubjectOption> subjectOptions;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @Override
    public void initialize(
            URL location,
            ResourceBundle resources
    ) {

        cmbGender.setItems(
                FXCollections.observableArrayList(
                        "Male",
                        "Female"
                )
        );

        List<Batch> batches =
                classDAO.getAllBatches();

        List<Batch> classLeaderItems =
                new ArrayList<>();

        classLeaderItems.add(NO_CLASS_LEADER);

        if (batches != null) {
            classLeaderItems.addAll(batches);
        }

        cmbClassLeaderOf.setItems(
                FXCollections.observableArrayList(
                        classLeaderItems
                )
        );

        cmbClassLeaderOf
                .getSelectionModel()
                .select(NO_CLASS_LEADER);

        subjectOptions =
                subjectDAO.getAllSubjects();

        subjectsBox.getChildren().clear();

        if (subjectOptions != null) {

            for (SubjectOption option : subjectOptions) {

                CheckBox checkBox =
                        new CheckBox(
                                option.getSubjectName()
                        );

                checkBox.setStyle(
                        "-fx-font-size: 12px;" +
                                "-fx-text-fill: #344563;"
                );

                checkBox.selectedProperty()
                        .bindBidirectional(
                                option.selectedProperty()
                        );

                subjectsBox.getChildren()
                        .add(checkBox);
            }
        }
    }

    @FXML
    private void choosePhoto() {

        FileChooser chooser =
                new FileChooser();

        chooser.setTitle(
                "Choose Teacher Photo"
        );

        chooser.getExtensionFilters()
                .add(
                        new FileChooser.ExtensionFilter(
                                "Image Files",
                                "*.png",
                                "*.jpg",
                                "*.jpeg"
                        )
                );

        File chosen =
                chooser.showOpenDialog(dialogStage);

        if (chosen == null) {
            return;
        }

        try {

            Path photosDirectory =
                    Paths.get(
                            System.getProperty("user.dir"),
                            "teacher_photos"
                    );

            Files.createDirectories(
                    photosDirectory
            );

            String originalName =
                    chosen.getName();

            int extensionIndex =
                    originalName.lastIndexOf('.');

            String extension =
                    extensionIndex >= 0
                            ? originalName.substring(extensionIndex)
                            : ".png";

            String newFileName =
                    "teacher_" +
                            System.currentTimeMillis() +
                            extension;

            Path destination =
                    photosDirectory.resolve(
                            newFileName
                    );

            Files.copy(
                    chosen.toPath(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );

            selectedPhotoPath =
                    destination.toAbsolutePath()
                            .toString();

            photoPreview.setImage(
                    new Image(
                            destination.toUri().toString(),
                            72,
                            72,
                            true,
                            true
                    )
            );

            photoPreview.setClip(
                    new Circle(36, 36, 36)
            );

            photoPreview.setVisible(true);
            photoPlaceholderCircle.setVisible(false);

            lblPhotoFileName.setText(
                    chosen.getName()
            );

        } catch (IOException e) {

            e.printStackTrace();

            showError(
                    "Could not copy the selected photo."
            );
        }
    }

    @FXML
    private void save() {

        String fullName =
                getText(txtFullName);

        String username =
                getText(txtUsername);

        String password =
                txtPassword.getText() != null
                        ? txtPassword.getText()
                        : "";

        String teacherCode =
                emptyToNull(
                        txtTeacherCode.getText()
                );

        String email =
                emptyToNull(
                        txtEmail.getText()
                );

        String phone =
                emptyToNull(
                        txtPhone.getText()
                );

        String gender =
                cmbGender.getValue();

        String address =
                emptyToNull(
                        txtAddress.getText()
                );

        LocalDate hireDate =
                dpHireDate.getValue();

        if (fullName.isEmpty()) {
            showError("Full name is required.");
            return;
        }

        if (username.isEmpty()) {
            showError("Username is required.");
            return;
        }

        if (password.isEmpty()) {
            showError("Password is required.");
            return;
        }

        String passwordError =
                validatePassword(password);

        if (passwordError != null) {
            showError(passwordError);
            return;
        }

        BigDecimal salary =
                null;

        String salaryText =
                getText(txtSalary);

        if (!salaryText.isEmpty()) {

            try {

                salary =
                        new BigDecimal(salaryText);

                if (salary.compareTo(
                        BigDecimal.ZERO
                ) < 0) {

                    showError(
                            "Salary cannot be negative."
                    );

                    return;
                }

            } catch (NumberFormatException e) {

                showError(
                        "Salary must be a valid number, " +
                                "for example 750000."
                );

                return;
            }
        }

        List<Integer> subjectIds =
                new ArrayList<>();

        if (subjectOptions != null) {

            for (SubjectOption option : subjectOptions) {

                if (option.isSelected()) {

                    subjectIds.add(
                            option.getSubjectId()
                    );
                }
            }
        }

        Batch selectedClassLeader =
                cmbClassLeaderOf.getValue();

        int classLeaderOfClassId =
                selectedClassLeader == null
                        ? -1
                        : selectedClassLeader.getId();

        int newTeacherId =
                teacherDAO.addTeacher(
                        fullName,
                        username,
                        password,
                        teacherCode,
                        email,
                        phone,
                        gender,
                        address,
                        hireDate,
                        salary,
                        selectedPhotoPath,
                        subjectIds,
                        classLeaderOfClassId
                );

        if (newTeacherId == -1) {

            showError(
                    "Could not save this teacher. " +
                            "The username or teacher code may already exist."
            );

            return;
        }

        dialogStage.close();
    }

    /*
     * Password requirements:
     *
     * 1. At least 8 characters
     * 2. At least one uppercase letter
     * 3. At least one lowercase letter
     * 4. At least one number
     * 5. At least one special character
     */
    private String validatePassword(String password) {

        if (password.length() < 8) {
            return "Password must contain at least 8 characters.";
        }

        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter.";
        }

        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter.";
        }

        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one number.";
        }

        if (!password.matches(
                ".*[^A-Za-z0-9\\s].*"
        )) {
            return "Password must contain at least one special character.";
        }

        return null;
    }

    @FXML
    private void cancel() {

        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void showError(String message) {

        lblFormError.setText(message);
        lblFormError.setVisible(true);
        lblFormError.setManaged(true);
    }

    private String getText(TextField field) {

        if (field == null || field.getText() == null) {
            return "";
        }

        return field.getText().trim();
    }

    private String emptyToNull(String value) {

        if (value == null) {
            return null;
        }

        String trimmed =
                value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
}