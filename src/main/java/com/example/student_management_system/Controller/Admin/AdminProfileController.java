package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.AdminDAO;
import com.example.student_management_system.Controller.Model.Admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

public class AdminProfileController {

    @FXML private ImageView imgProfilePhoto;
    @FXML private TextField txtFullName;
    @FXML private TextField txtUsername;
    @FXML private Button btnChoosePhoto;
    @FXML private Button btnSaveProfile;
    @FXML private PasswordField txtOldPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField visibleOldPassword;
    @FXML private TextField visibleNewPassword;
    @FXML private TextField visibleConfirmPassword;
    @FXML private Button btnOldPasswordEye;
    @FXML private Button btnNewPasswordEye;
    @FXML private Button btnConfirmPasswordEye;
    @FXML private FontIcon oldPasswordEyeIcon;
    @FXML private FontIcon newPasswordEyeIcon;
    @FXML private FontIcon confirmPasswordEyeIcon;
    @FXML private Label lblMessage;

    private final AdminDAO adminDAO = new AdminDAO();
    private static final String PHOTO_STORAGE_DIR = "uploads/admin_photos";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>_\\-+=~`\\[\\]/;']).{8,}$"
    );

    private static final String NOTI_BASE =
            "-fx-padding: 10 14 10 14; -fx-background-radius: 8; -fx-border-radius: 8; " +
                    "-fx-font-size: 12.5px; -fx-font-weight: 600; -fx-border-width: 0 0 0 3;";
    private static final String NOTI_SUCCESS =
            NOTI_BASE + "-fx-background-color: #E7F6EE; -fx-text-fill: #1F7A4D; -fx-border-color: #1F7A4D;";
    private static final String NOTI_ERROR =
            NOTI_BASE + "-fx-background-color: #FDECEC; -fx-text-fill: #B3261E; -fx-border-color: #B3261E;";

    private Admin currentAdmin;
    private String selectedPhotoAbsolutePath;
    private int adminId = -1;

    @FXML
    public void initialize() {
        hideMessage();
        visibleOldPassword.textProperty().bindBidirectional(txtOldPassword.textProperty());
        visibleNewPassword.textProperty().bindBidirectional(txtNewPassword.textProperty());
        visibleConfirmPassword.textProperty().bindBidirectional(txtConfirmPassword.textProperty());

        visibleOldPassword.setVisible(false);
        visibleOldPassword.setManaged(false);
        visibleNewPassword.setVisible(false);
        visibleNewPassword.setManaged(false);
        visibleConfirmPassword.setVisible(false);
        visibleConfirmPassword.setManaged(false);

        if (adminId != -1) {
            loadAdmin(adminId);
        }
    }

    public void setAdminId(int userId) {
        this.adminId = userId;
        if (currentAdmin == null) {
            loadAdmin(userId);
        }
    }

    @FXML
    private void toggleOldPasswordVisibility() {
        togglePasswordVisibility(txtOldPassword, visibleOldPassword, oldPasswordEyeIcon);
    }

    @FXML
    private void toggleNewPasswordVisibility() {
        togglePasswordVisibility(txtNewPassword, visibleNewPassword, newPasswordEyeIcon);
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        togglePasswordVisibility(txtConfirmPassword, visibleConfirmPassword, confirmPasswordEyeIcon);
    }

    private void togglePasswordVisibility(PasswordField passwordField, TextField visibleField, FontIcon eyeIcon) {
        boolean show = !visibleField.isVisible();
        visibleField.setVisible(show);
        visibleField.setManaged(show);
        passwordField.setVisible(!show);
        passwordField.setManaged(!show);
        eyeIcon.setIconLiteral(show ? "fas-eye-slash" : "fas-eye");
    }

    private void loadAdmin(int userId) {
        currentAdmin = adminDAO.getAdminProfileById(userId);
        if (currentAdmin == null) {
            showMessage("Could not load admin profile. Please log in again.", true);
            return;
        }
        txtFullName.setText(currentAdmin.getFullName());
        txtUsername.setText(currentAdmin.getUsername());
        loadPhotoIntoView(currentAdmin.getPhotoPath());
    }

    private void loadPhotoIntoView(String photoPath) {
        if (photoPath != null && !photoPath.isBlank() && new File(photoPath).exists()) {
            imgProfilePhoto.setImage(new Image(new File(photoPath).toURI().toString()));
        } else {
            imgProfilePhoto.setImage(null);
        }
    }

    @FXML
    private void handleChoosePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Profile Photo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) btnChoosePhoto.getScene().getWindow();
        File chosen = chooser.showOpenDialog(stage);

        if (chosen != null) {
            selectedPhotoAbsolutePath = chosen.getAbsolutePath();
            imgProfilePhoto.setImage(new Image(chosen.toURI().toString()));
        }
    }

    @FXML
    private void handleSaveProfile() {
        try {
            if (currentAdmin == null) {
                showMessage("No admin profile loaded. Please log in again.", true);
                return;
            }

            String fullName = txtFullName.getText() == null ? "" : txtFullName.getText().trim();
            String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();

            if (fullName.isEmpty()) {
                showMessage("Full name cannot be empty.", true);
                return;
            }
            if (username.isEmpty()) {
                showMessage("Username cannot be empty.", true);
                return;
            }

            if (adminDAO.isUsernameTakenByOthers(username, currentAdmin.getUserId())) {
                showMessage("That username is already taken.", true);
                return;
            }

            String newStoredPhotoPath = null;
            if (selectedPhotoAbsolutePath != null) {
                try {
                    newStoredPhotoPath = copyPhotoToStorage(selectedPhotoAbsolutePath, currentAdmin.getUserId());
                } catch (IOException e) {
                    showMessage("Failed to save the selected photo: " + e.getMessage(), true);
                    return;
                }
            }

            boolean success = adminDAO.updateProfile(currentAdmin.getUserId(), fullName, username, newStoredPhotoPath);

            if (success) {
                currentAdmin.setFullName(fullName);
                currentAdmin.setUsername(username);
                if (newStoredPhotoPath != null) {
                    currentAdmin.setPhotoPath(newStoredPhotoPath);
                }
                selectedPhotoAbsolutePath = null;
                showMessage("Profile updated successfully.", false);
                loadPhotoIntoView(currentAdmin.getPhotoPath());
            } else {
                showMessage("Failed to update profile. Please check the database connection and try again.", true);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Something went wrong while saving your profile: " + ex.getMessage(), true);
        }
    }

    private String copyPhotoToStorage(String sourceAbsolutePath, int userId) throws IOException {
        Path sourcePath = Paths.get(sourceAbsolutePath);
        Path targetDir = Paths.get(PHOTO_STORAGE_DIR);
        Files.createDirectories(targetDir);

        String extension = "";
        String name = sourcePath.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            extension = name.substring(dot);
        }

        String targetFileName = "admin_" + userId + "_" + System.currentTimeMillis() + extension;
        Path targetPath = targetDir.resolve(targetFileName);

        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath.toString();
    }

    @FXML
    private void handleChangePassword() {
        try {
            if (currentAdmin == null) {
                showMessage("No admin profile loaded. Please log in again.", true);
                return;
            }

            String oldPassword = txtOldPassword.isVisible() ? txtOldPassword.getText() : visibleOldPassword.getText();
            String newPassword = txtNewPassword.isVisible() ? txtNewPassword.getText() : visibleNewPassword.getText();
            String confirmPassword = txtConfirmPassword.isVisible() ? txtConfirmPassword.getText() : visibleConfirmPassword.getText();

            if (oldPassword == null || oldPassword.isEmpty() ||
                    newPassword == null || newPassword.isEmpty() ||
                    confirmPassword == null || confirmPassword.isEmpty()) {
                showMessage("Please fill in all three password fields.", true);
                return;
            }

            if (!adminDAO.verifyCurrentPassword(currentAdmin.getUserId(), oldPassword)) {
                showMessage("Current password is incorrect.", true);
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showMessage("New password and confirm password do not match.", true);
                return;
            }

            if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
                showMessage(
                        "Password must be at least 8 characters and include at least one " +
                                "uppercase letter, one lowercase letter, and one special character.",
                        true
                );
                return;
            }

            if (newPassword.equals(oldPassword)) {
                showMessage("New password must be different from the current password.", true);
                return;
            }

            boolean success = adminDAO.updatePassword(currentAdmin.getUserId(), newPassword);

            if (success) {
                txtOldPassword.clear();
                txtNewPassword.clear();
                txtConfirmPassword.clear();
                visibleOldPassword.clear();
                visibleNewPassword.clear();
                visibleConfirmPassword.clear();
                showMessage("Password changed successfully.", false);
            } else {
                showMessage("Failed to change password. Please check the database connection and try again.", true);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Something went wrong while changing your password: " + ex.getMessage(), true);
        }
    }

    private void showMessage(String text, boolean isError) {
        if (lblMessage != null) {
            lblMessage.setText(text);
            lblMessage.setStyle(isError ? NOTI_ERROR : NOTI_SUCCESS);
            lblMessage.setVisible(true);
            lblMessage.setManaged(true);
        } else {
            Alert alert = new Alert(isError ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
            alert.setTitle(isError ? "Error" : "Success");
            alert.setHeaderText(null);
            alert.setContentText(text);
            alert.showAndWait();
        }
    }

    private void hideMessage() {
        if (lblMessage != null) {
            lblMessage.setText("");
            lblMessage.setStyle("");
            lblMessage.setVisible(false);
            lblMessage.setManaged(false);
        }
    }
}