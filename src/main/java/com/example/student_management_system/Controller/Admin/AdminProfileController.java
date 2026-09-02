package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.AdminDAO;
import com.example.student_management_system.Controller.Model.Admin;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

public class AdminProfileController {

    // ---- Profile section ----
    @FXML private ImageView imgProfilePhoto;
    @FXML private TextField txtFullName;
    @FXML private TextField txtUsername;
    @FXML private Button btnChoosePhoto;
    @FXML private Button btnSaveProfile;

    // ---- Password section ----
    @FXML private PasswordField txtOldPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Button btnChangePassword;

    // ---- Feedback ----
    @FXML private Label lblMessage;

    private final AdminDAO adminDAO = new AdminDAO();

    // Folder (relative to the running app) where uploaded profile photos are copied.
    private static final String PHOTO_STORAGE_DIR = "uploads/admin_photos";

    // At least 8 chars overall, at least 1 lowercase, 1 uppercase, 1 special character.
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>_\\-+=~`\\[\\]/;']).{8,}$"
    );

    // ---- Notification banner styles (shared base + variant) ----
    private static final String NOTI_BASE =
            "-fx-padding: 10 14 10 14; -fx-background-radius: 8; -fx-border-radius: 8; " +
                    "-fx-font-size: 12.5px; -fx-font-weight: 600; -fx-border-width: 0 0 0 3;";
    private static final String NOTI_SUCCESS =
            NOTI_BASE + "-fx-background-color: #E7F6EE; -fx-text-fill: #1F7A4D; -fx-border-color: #1F7A4D;";
    private static final String NOTI_ERROR =
            NOTI_BASE + "-fx-background-color: #FDECEC; -fx-text-fill: #B3261E; -fx-border-color: #B3261E;";

    private Admin currentAdmin;
    private String selectedPhotoAbsolutePath; // staged file on disk, chosen but not yet saved

    @FXML
    public void initialize() {
        // Start with no banner showing.
        hideMessage();

        // Fallback: if this screen is opened without loadAdmin(userId) being called
        // from your login/session flow (e.g. testing it standalone), load the
        // default 'admin' account so the screen still works instead of crashing
        // with a NullPointerException on save.
        if (currentAdmin == null) {
            Admin fallback = adminDAO.getAdminByUsername("admin");
            if (fallback != null) {
                loadAdmin(fallback.getUserId());
            }
        }
    }

    /**
     * Call this right after loading the FXML, passing the logged-in admin's user_id
     * from your login/session code, e.g.:
     *   FXMLLoader loader = new FXMLLoader(getClass().getResource("admin_profile.fxml"));
     *   Parent root = loader.load();
     *   loader.<AdminProfileController>getController().loadAdmin(loggedInUserId);
     */
    public void loadAdmin(int userId) {
        currentAdmin = adminDAO.getAdminProfileById(userId);
        if (currentAdmin == null) {
            showMessage("Could not load admin profile.", true);
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
            // Optional: set a default placeholder image here.
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
        // ---- Guard: no admin loaded yet ----
        if (currentAdmin == null) {
            showMessage("No admin profile loaded. Please log in again.", true);
            return;
        }

        String fullName = txtFullName.getText() == null ? "" : txtFullName.getText().trim();
        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();

        // ---- Required field checks ----
        if (fullName.isEmpty()) {
            showMessage("Full name cannot be empty.", true);
            return;
        }
        if (username.isEmpty()) {
            showMessage("Username cannot be empty.", true);
            return;
        }

        // ---- Uniqueness check for username ----
        if (adminDAO.isUsernameTakenByOthers(username, currentAdmin.getUserId())) {
            showMessage("That username is already taken.", true);
            return;
        }

        // ---- Handle photo (only copy/persist if a new one was chosen) ----
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
        } else {
            showMessage("Failed to update profile. Please try again.", true);
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
            extension = name.substring(dot); // includes the dot
        }

        String targetFileName = "admin_" + userId + "_" + System.currentTimeMillis() + extension;
        Path targetPath = targetDir.resolve(targetFileName);

        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toString();
    }

    @FXML
    private void handleChangePassword() {
        // ---- Guard: no admin loaded yet ----
        if (currentAdmin == null) {
            showMessage("No admin profile loaded. Please log in again.", true);
            return;
        }

        String oldPassword = txtOldPassword.getText() == null ? "" : txtOldPassword.getText();
        String newPassword = txtNewPassword.getText() == null ? "" : txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText() == null ? "" : txtConfirmPassword.getText();

        // ---- Required field checks ----
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Please fill in all three password fields.", true);
            return;
        }

        // ---- Confirm current password is correct ----
        if (!adminDAO.verifyCurrentPassword(currentAdmin.getUserId(), oldPassword)) {
            showMessage("Current password is incorrect.", true);
            return;
        }

        // ---- New == Confirm ----
        if (!newPassword.equals(confirmPassword)) {
            showMessage("New password and confirm password do not match.", true);
            return;
        }

        // ---- Strength check: 8+ chars, 1 lowercase, 1 uppercase, 1 special char ----
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            showMessage(
                    "Password must be at least 8 characters and include at least one " +
                            "uppercase letter, one lowercase letter, and one special character.",
                    true
            );
            return;
        }

        // ---- Don't allow reusing the same password ----
        if (newPassword.equals(oldPassword)) {
            showMessage("New password must be different from the current password.", true);
            return;
        }

        boolean success = adminDAO.updatePassword(currentAdmin.getUserId(), newPassword);

        if (success) {
            txtOldPassword.clear();
            txtNewPassword.clear();
            txtConfirmPassword.clear();
            showMessage("Password changed successfully.", false);
        } else {
            showMessage("Failed to change password. Please try again.", true);
        }
    }

    /** Shows a colored notification banner (green = success, red = error) above/below the forms. */
    private void showMessage(String text, boolean isError) {
        if (lblMessage != null) {
            lblMessage.setText(text);
            lblMessage.setStyle(isError ? NOTI_ERROR : NOTI_SUCCESS);
            lblMessage.setVisible(true);
            lblMessage.setManaged(true);
        } else {
            // Fallback if no inline label is wired up in the FXML.
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