package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.AdminDAO;
import com.example.student_management_system.Controller.Model.Admin;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AdminProfileController implements Initializable {

    // Left card
    @FXML private ImageView imgProfilePhoto;
    @FXML private Label lblDisplayName;
    @FXML private Label lblUsername;
    @FXML private Label lblRole;
    @FXML private Button btnChoosePhoto;

    // Account form
    @FXML private TextField txtFullName;
    @FXML private TextField txtUsername;

    // Password form — hidden + visible pairs
    @FXML private PasswordField txtOldPassword;
    @FXML private TextField     visibleOldPassword;
    @FXML private Button        btnOldPasswordEye;

    @FXML private PasswordField txtNewPassword;
    @FXML private TextField     visibleNewPassword;
    @FXML private Button        btnNewPasswordEye;

    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField     visibleConfirmPassword;
    @FXML private Button        btnConfirmPasswordEye;

    // Password rules
    @FXML private Label ruleLength;
    @FXML private Label ruleUpper;
    @FXML private Label ruleLower;
    @FXML private Label ruleSpecial;

    // Toast
    @FXML private VBox adminProfileToast;
    @FXML private Label lblToastTitle;
    @FXML private Label lblToastHeading;
    @FXML private Label lblToastMessage;

    // DAO + state
    private final AdminDAO adminDAO = new AdminDAO();
    private static final String PHOTO_STORAGE_DIR = "uploads/admin_photos";

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>_\\-+=~`\\[\\]/;']).{8,}$"
    );

    private Admin currentAdmin;
    private String selectedPhotoAbsolutePath;
    private int adminId = -1;
    private PauseTransition toastTimer;

    private boolean oldVisible = false;
    private boolean newVisible = false;
    private boolean confirmVisible = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Keep hidden and visible fields synced
        bindPair(txtOldPassword,     visibleOldPassword);
        bindPair(txtNewPassword,     visibleNewPassword);
        bindPair(txtConfirmPassword, visibleConfirmPassword);

        // Live rules on new password
        txtNewPassword.textProperty().addListener((o, a, b) -> updateRules(b));
        visibleNewPassword.textProperty().addListener((o, a, b) -> updateRules(b));

        updateRules("");
    }

    /** Called by AdminDashboardController after loading this view. */
    public void setAdminId(int userId) {
        this.adminId = userId;
        if (currentAdmin == null) {
            loadAdmin(userId);
        }
    }

    // ==================================================
    //  LOAD
    // ==================================================
    private void loadAdmin(int userId) {
        currentAdmin = adminDAO.getAdminProfileById(userId);
        if (currentAdmin == null) {
            toast("LOAD FAILED", "Profile unavailable",
                    "Could not load admin profile. Please log in again.", false);
            return;
        }

        lblDisplayName.setText(currentAdmin.getFullName());
        lblUsername.setText("Username: " + safe(currentAdmin.getUsername()));
        lblRole.setText("Administrator");

        txtFullName.setText(currentAdmin.getFullName());
        txtUsername.setText(currentAdmin.getUsername());

        loadPhotoIntoView(currentAdmin.getPhotoPath());
    }

    private void loadPhotoIntoView(String photoPath) {
        try {
            if (photoPath != null && !photoPath.isBlank()) {
                if (photoPath.startsWith("http")) {
                    imgProfilePhoto.setImage(new Image(photoPath, true));
                    return;
                }
                File f = new File(photoPath);
                if (f.exists()) {
                    imgProfilePhoto.setImage(new Image(f.toURI().toString()));
                    return;
                }
            }
        } catch (Exception ignored) {}
        try {
            imgProfilePhoto.setImage(new Image(getClass().getResourceAsStream(
                    "/com/example/student_management_system/Images/default_avatar.png")));
        } catch (Exception e) {
            imgProfilePhoto.setImage(null);
        }
    }

    // ==================================================
    //  PHOTO
    // ==================================================
    @FXML
    private void handleChoosePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Profile Photo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        Stage stage = (Stage) btnChoosePhoto.getScene().getWindow();
        File chosen = chooser.showOpenDialog(stage);

        if (chosen != null) {
            selectedPhotoAbsolutePath = chosen.getAbsolutePath();
            imgProfilePhoto.setImage(new Image(chosen.toURI().toString()));
        }
    }

    private String copyPhotoToStorage(String sourceAbsolutePath, int userId) throws IOException {
        Path sourcePath = Paths.get(sourceAbsolutePath);
        Path targetDir = Paths.get(PHOTO_STORAGE_DIR);
        Files.createDirectories(targetDir);

        String extension = "";
        String name = sourcePath.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot >= 0) extension = name.substring(dot);

        String targetFileName = "admin_" + userId + "_" + System.currentTimeMillis() + extension;
        Path targetPath = targetDir.resolve(targetFileName);

        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath.toString();
    }

    // ==================================================
    //  UPDATE ACCOUNT (name + username + photo)
    // ==================================================
    @FXML
    private void handleSaveProfile() {
        try {
            if (currentAdmin == null) {
                toast("UPDATE FAILED", "No profile",
                        "Please log in again.", false);
                return;
            }

            String fullName = txtFullName.getText() == null ? "" : txtFullName.getText().trim();
            String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();

            if (fullName.isEmpty()) {
                toast("UPDATE FAILED", "Missing name",
                        "Full name cannot be empty.", false);
                return;
            }
            if (fullName.length() < 3) {
                toast("UPDATE FAILED", "Name too short",
                        "Name must be at least 3 characters.", false);
                return;
            }
            if (username.isEmpty()) {
                toast("UPDATE FAILED", "Missing username",
                        "Username cannot be empty.", false);
                return;
            }
            if (adminDAO.isUsernameTakenByOthers(username, currentAdmin.getUserId())) {
                toast("UPDATE FAILED", "Username taken",
                        "That username is already in use.", false);
                return;
            }

            String newStoredPhotoPath = null;
            if (selectedPhotoAbsolutePath != null) {
                try {
                    newStoredPhotoPath = copyPhotoToStorage(selectedPhotoAbsolutePath,
                            currentAdmin.getUserId());
                } catch (IOException e) {
                    toast("UPDATE FAILED", "Photo error",
                            "Failed to save the selected photo.", false);
                    return;
                }
            }

            boolean ok = adminDAO.updateProfile(currentAdmin.getUserId(),
                    fullName, username, newStoredPhotoPath);

            if (ok) {
                currentAdmin.setFullName(fullName);
                currentAdmin.setUsername(username);
                if (newStoredPhotoPath != null) {
                    currentAdmin.setPhotoPath(newStoredPhotoPath);
                }
                selectedPhotoAbsolutePath = null;

                lblDisplayName.setText(fullName);
                lblUsername.setText("Username: " + username);
                loadPhotoIntoView(currentAdmin.getPhotoPath());

                toast("UPDATE SUCCESSFUL", "Profile updated",
                        "Your account details have been saved.", true);
            } else {
                toast("UPDATE FAILED", "Database error",
                        "Could not update your profile. Try again.", false);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            toast("UPDATE FAILED", "Unexpected error",
                    ex.getMessage(), false);
        }
    }

    // ==================================================
    //  UPDATE PASSWORD
    // ==================================================
    @FXML
    private void handleChangePassword() {
        try {
            if (currentAdmin == null) {
                toast("PASSWORD FAILED", "No profile",
                        "Please log in again.", false);
                return;
            }

            String oldPwd     = txtOldPassword.getText();
            String newPwd     = txtNewPassword.getText();
            String confirmPwd = txtConfirmPassword.getText();

            if (oldPwd == null || oldPwd.isEmpty()) {
                toast("PASSWORD FAILED", "Missing current password",
                        "Enter your current password to continue.", false);
                return;
            }
            if (newPwd == null || newPwd.isEmpty()) {
                toast("PASSWORD FAILED", "Missing new password",
                        "Enter a new password.", false);
                return;
            }
            if (!newPwd.equals(confirmPwd)) {
                toast("PASSWORD FAILED", "Passwords do not match",
                        "The two new passwords must be identical.", false);
                return;
            }
            if (!PASSWORD_PATTERN.matcher(newPwd).matches()) {
                toast("PASSWORD FAILED", "Weak password",
                        "Must be 8+ chars with upper, lower, and a special character.", false);
                return;
            }
            if (newPwd.equals(oldPwd)) {
                toast("PASSWORD FAILED", "Same password",
                        "New password must be different from the current one.", false);
                return;
            }
            if (!adminDAO.verifyCurrentPassword(currentAdmin.getUserId(), oldPwd)) {
                toast("PASSWORD FAILED", "Wrong current password",
                        "The current password you entered is incorrect.", false);
                return;
            }

            boolean ok = adminDAO.updatePassword(currentAdmin.getUserId(), newPwd);

            if (ok) {
                resetToggle(txtOldPassword,     visibleOldPassword,     btnOldPasswordEye);
                resetToggle(txtNewPassword,     visibleNewPassword,     btnNewPasswordEye);
                resetToggle(txtConfirmPassword, visibleConfirmPassword, btnConfirmPasswordEye);

                oldVisible = newVisible = confirmVisible = false;
                updateRules("");

                toast("PASSWORD UPDATED", "Success",
                        "Your password has been changed.", true);
            } else {
                toast("PASSWORD FAILED", "Database error",
                        "Could not update your password.", false);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            toast("PASSWORD FAILED", "Unexpected error",
                    ex.getMessage(), false);
        }
    }

    private void resetToggle(PasswordField hidden, TextField visible, Button btn) {
        hidden.clear();
        visible.clear();
        hidden.setVisible(true);
        hidden.setManaged(true);
        visible.setVisible(false);
        visible.setManaged(false);
        btn.setText("👁");
    }

    // ==================================================
    //  EYE TOGGLES
    // ==================================================
    @FXML public void toggleOldPasswordVisibility() {
        oldVisible = !oldVisible;
        applyToggle(txtOldPassword, visibleOldPassword, btnOldPasswordEye, oldVisible);
    }

    @FXML public void toggleNewPasswordVisibility() {
        newVisible = !newVisible;
        applyToggle(txtNewPassword, visibleNewPassword, btnNewPasswordEye, newVisible);
    }

    @FXML public void toggleConfirmPasswordVisibility() {
        confirmVisible = !confirmVisible;
        applyToggle(txtConfirmPassword, visibleConfirmPassword,
                btnConfirmPasswordEye, confirmVisible);
    }

    private void applyToggle(PasswordField hidden, TextField visible,
                             Button btn, boolean showVisible) {
        if (showVisible) {
            visible.setText(hidden.getText());
            hidden.setVisible(false);
            hidden.setManaged(false);
            visible.setVisible(true);
            visible.setManaged(true);
            visible.positionCaret(visible.getText().length());
            visible.requestFocus();
            btn.setText("🙈");
        } else {
            hidden.setText(visible.getText());
            visible.setVisible(false);
            visible.setManaged(false);
            hidden.setVisible(true);
            hidden.setManaged(true);
            hidden.positionCaret(hidden.getText().length());
            hidden.requestFocus();
            btn.setText("👁");
        }
    }

    private void bindPair(PasswordField hidden, TextField visible) {
        hidden.textProperty().addListener((o, a, b) -> {
            if (visible.isVisible()) visible.setText(b);
        });
        visible.textProperty().addListener((o, a, b) -> {
            if (hidden.isVisible()) hidden.setText(b);
        });
    }

    // ==================================================
    //  RULES
    // ==================================================
    private void updateRules(String pwd) {
        if (pwd == null) pwd = "";

        boolean length  = pwd.length() >= 8;
        boolean upper   = pwd.chars().anyMatch(Character::isUpperCase);
        boolean lower   = pwd.chars().anyMatch(Character::isLowerCase);
        boolean special = pwd.chars().anyMatch(c -> !Character.isLetterOrDigit(c));

        styleRule(ruleLength,  "At least 8 characters",         length);
        styleRule(ruleUpper,   "At least 1 uppercase letter",   upper);
        styleRule(ruleLower,   "At least 1 lowercase letter",   lower);
        styleRule(ruleSpecial, "At least 1 special character",  special);
    }

    private void styleRule(Label lbl, String text, boolean pass) {
        if (lbl == null) return;
        lbl.setText((pass ? "✓  " : "•  ") + text);
        lbl.setStyle("-fx-font-size:11px; -fx-font-weight:bold;"
                + "-fx-text-fill:" + (pass ? "#16a34a" : "#94a3b8") + ";");
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "--" : v;
    }

    // ==================================================
    //  TOAST
    // ==================================================
    private void toast(String title, String heading, String msg, boolean success) {
        if (adminProfileToast == null) return;

        adminProfileToast.setPrefWidth(420);
        adminProfileToast.setMinWidth(420);
        adminProfileToast.setMaxWidth(420);
        adminProfileToast.setPrefHeight(125);
        adminProfileToast.setMinHeight(125);
        adminProfileToast.setMaxHeight(125);

        String bg  = success ? "#dcfce7" : "#fee2e2";
        String fg  = success ? "#16a34a" : "#dc2626";
        String bar = success ? "#22c55e" : "#ef4444";
        String mk  = success ? "✓" : "!";

        lblToastTitle.setText(title);
        lblToastHeading.setText(heading);
        lblToastMessage.setText(msg);

        var iconBox = adminProfileToast.lookup(".toast-icon");
        if (iconBox != null)
            iconBox.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:19;");

        var iconLbl = adminProfileToast.lookup(".toast-icon-label");
        if (iconLbl instanceof Label l) {
            l.setText(mk);
            l.setStyle("-fx-text-fill:" + fg + "; -fx-font-size:17px; -fx-font-weight:bold;");
        }

        var barNode = adminProfileToast.lookup(".toast-bar");
        if (barNode != null)
            barNode.setStyle("-fx-background-color:" + bar + "; -fx-background-radius:3;");

        adminProfileToast.setManaged(true);
        adminProfileToast.setVisible(true);

        if (toastTimer != null) toastTimer.stop();
        toastTimer = new PauseTransition(Duration.seconds(3));
        toastTimer.setOnFinished(e -> {
            adminProfileToast.setVisible(false);
            adminProfileToast.setManaged(false);
        });
        toastTimer.play();
    }
}