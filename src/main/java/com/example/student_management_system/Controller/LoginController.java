package com.example.student_management_system.Controller;

import com.example.student_management_system.Controller.Admin.AdminDashboardController;
import com.example.student_management_system.Controller.Staff.StaffDashboardController;
import com.example.student_management_system.Controller.Teacher.TeacherDashboardController;
import com.example.student_management_system.Controller.DAO.DBConnention;
import com.example.student_management_system.Controller.Util.PasswordHasher;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCKOUT_SECONDS = 10;

    private static final String BTN_NORMAL_STYLE =
            "-fx-background-color:#4f46e5;-fx-text-fill:white;-fx-background-radius:10;" +
                    "-fx-font-size:13px;-fx-font-weight:bold;-fx-cursor:hand;-fx-opacity:1;";

    private static final String BTN_PRESSED_STYLE =
            "-fx-background-color:#3730a3;-fx-text-fill:white;-fx-background-radius:10;" +
                    "-fx-font-size:13px;-fx-font-weight:bold;-fx-cursor:hand;" +
                    "-fx-scale-x:.98;-fx-scale-y:.98;-fx-opacity:1;";

    private static final String BTN_DISABLED_STYLE =
            "-fx-background-color:#cbd5e1;-fx-text-fill:#94a3b8;-fx-background-radius:10;" +
                    "-fx-font-size:13px;-fx-font-weight:bold;-fx-cursor:default;-fx-opacity:0.85;";

    @FXML private Button btnLogin;
    @FXML private Button btnEye;
    @FXML private TextField txtUsername;
    @FXML private TextField visiblePasswordField;
    @FXML private PasswordField passwordField;
    @FXML private VBox loginToast;
    @FXML private StackPane loginToastIconWrap;
    @FXML private Label loginToastIcon;
    @FXML private Label lblLoginToastTitle;
    @FXML private Label lblLoginToastHeading;
    @FXML private Label lblLoginToastMessage;
    @FXML private Region loginToastAccent;
    @FXML private FontIcon eyeIcon;

    private boolean showPassword = false;
    private int failedAttempts = 0;
    private boolean lockoutActive = false;
    private PauseTransition toastTimer;
    private Timeline lockoutTimeline;
    private boolean loginInProgress = false;

    @FXML
    public void initialize() {
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);
        eyeIcon.setIconLiteral("fas-eye");

        txtUsername.setOnAction(e -> loginWithButtonEffect());
        passwordField.setOnAction(e -> loginWithButtonEffect());
        visiblePasswordField.setOnAction(e -> loginWithButtonEffect());

        btnLogin.setDefaultButton(true);
        applyLoginButtonStyle(true);
    }

    @FXML
    private void handleForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/ForgotPassword.fxml"));
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.initOwner(txtUsername.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Reset Password");
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorToast("ERROR", "Unable to open reset window", "Please try again later.");
        }
    }

    @FXML
    private void togglePassword() {
        showPassword = !showPassword;
        passwordField.setVisible(!showPassword);
        passwordField.setManaged(!showPassword);
        visiblePasswordField.setVisible(showPassword);
        visiblePasswordField.setManaged(showPassword);
        eyeIcon.setIconLiteral(showPassword ? "fas-eye-slash" : "fas-eye");
    }

    @FXML
    private void login(ActionEvent event) {
        if (loginInProgress) return;
        performLogin();
    }

    private void loginWithButtonEffect() {
        if (loginInProgress) return;
        if (lockoutActive) { showErrorToast("SIGN IN LOCKED", "Please wait", "Try again shortly."); return; }
        if (btnLogin.isDisabled()) return;

        loginInProgress = true;
        btnLogin.setStyle(BTN_PRESSED_STYLE);
        PauseTransition pressEffect = new PauseTransition(Duration.millis(160));
        pressEffect.setOnFinished(e -> {
            try {
                if (!lockoutActive && !btnLogin.isDisabled()) applyLoginButtonStyle(true);
                performLogin();
            } finally { loginInProgress = false; }
        });
        pressEffect.play();
    }

    private void performLogin() {
        if (lockoutActive || btnLogin.isDisabled()) return;

        String username = txtUsername.getText().trim();
        String password = showPassword ? visiblePasswordField.getText() : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showErrorToast("LOGIN REQUIRED", "Missing details", "Enter username and password.");
            return;
        }

        String sql = "SELECT user_id, username, password, role, status "
                + "FROM users WHERE username = ? AND status = 'ACTIVE'";

        try (Connection con = DBConnention.getConnection()) {
            if (con == null) {
                showErrorToast("CONNECTION ERROR", "Unavailable", "Check database connection.");
                return;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { handleFailedLogin(); return; }

                    int    userId         = rs.getInt("user_id");
                    String storedPassword = rs.getString("password");
                    String role           = rs.getString("role");

                    boolean passwordOk;
                    boolean needsUpgrade = false;

                    if (PasswordHasher.isHashed(storedPassword)) {
                        passwordOk = PasswordHasher.verify(password, storedPassword);
                    } else {
                        passwordOk = storedPassword.equals(password);
                        needsUpgrade = passwordOk;
                    }

                    if (!passwordOk) { handleFailedLogin(); return; }

                    if (needsUpgrade) {
                        try (PreparedStatement up = con.prepareStatement(
                                "UPDATE users SET password = ? WHERE user_id = ?")) {
                            up.setString(1, PasswordHasher.hash(password));
                            up.setInt(2, userId);
                            up.executeUpdate();
                            System.out.println("[Login] Auto-upgraded '" + username + "' to hashed.");
                        } catch (Exception ex) {
                            System.err.println("[Login] Auto-upgrade failed: " + ex.getMessage());
                        }
                    }

                    failedAttempts = 0;
                    if ("ADMIN".equalsIgnoreCase(role))        openAdminDashboard(username);
                    else if ("TEACHER".equalsIgnoreCase(role)) openTeacherDashboard(username);
                    else if ("STAFF".equalsIgnoreCase(role))   openStaffDashboard(username);
                    else showErrorToast("ACCESS DENIED", "Not supported", "Contact administrator.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorToast("CONNECTION ERROR", "Try again", "Database connection failed.");
        }
    }

    private void handleFailedLogin() {
        failedAttempts++;
        int remaining = MAX_FAILED_ATTEMPTS - failedAttempts;
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) { startLockout(); return; }
        String msg = (remaining == 1) ? "1 attempt left." : remaining + " attempts left.";
        showErrorToast("LOGIN FAILED", "Invalid credentials", msg);
    }

    private void startLockout() {
        lockoutActive = true; failedAttempts = 0; setLoginEnabled(false);
        showErrorToast("SIGN IN LOCKED", "Please wait",
                "Retry in " + LOCKOUT_SECONDS + " seconds.", LOCKOUT_SECONDS);
        if (lockoutTimeline != null) lockoutTimeline.stop();
        final int[] secondsLeft = {LOCKOUT_SECONDS};
        updateLockoutButtonText(secondsLeft[0]);
        lockoutTimeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            secondsLeft[0]--;
            if (secondsLeft[0] > 0) updateLockoutButtonText(secondsLeft[0]);
            else endLockout();
        }));
        lockoutTimeline.setCycleCount(LOCKOUT_SECONDS);
        lockoutTimeline.play();
    }

    private void endLockout() {
        lockoutActive = false;
        if (lockoutTimeline != null) { lockoutTimeline.stop(); lockoutTimeline = null; }
        setLoginEnabled(true);
        btnLogin.setText("SIGN IN");
    }

    private void updateLockoutButtonText(int s) { btnLogin.setText("SIGN IN (" + s + "s)"); }
    private void setLoginEnabled(boolean e) { btnLogin.setDisable(!e); applyLoginButtonStyle(e); }
    private void applyLoginButtonStyle(boolean e) {
        btnLogin.setStyle(e ? BTN_NORMAL_STYLE : BTN_DISABLED_STYLE);
    }

    private void showErrorToast(String title, String heading, String message) {
        showErrorToast(title, heading, message, 5);
    }

    private void showErrorToast(String title, String heading,
                                String message, int hideAfterSeconds) {
        loginToastIconWrap.setStyle("-fx-background-color:#fee2e2;-fx-background-radius:19;");
        loginToastIcon.setText("✕");
        loginToastIcon.setStyle("-fx-text-fill:#dc2626;-fx-font-size:17px;-fx-font-weight:bold;");
        loginToastAccent.setStyle("-fx-background-color:#ef4444;-fx-background-radius:3;");
        lblLoginToastTitle.setText(title);
        lblLoginToastHeading.setText(heading);
        lblLoginToastMessage.setText(message);
        loginToast.setPrefSize(350, 125);
        loginToast.setMinSize(350, 125);
        loginToast.setMaxSize(350, 125);
        loginToast.setManaged(true);
        loginToast.setVisible(true);
        if (toastTimer != null) toastTimer.stop();
        toastTimer = new PauseTransition(Duration.seconds(hideAfterSeconds));
        toastTimer.setOnFinished(ev -> { loginToast.setVisible(false); loginToast.setManaged(false); });
        toastTimer.play();
    }

    private void swapScene(Parent root) {
        Stage stage = (Stage) txtUsername.getScene().getWindow();
        Rectangle2D b = Screen.getPrimary().getVisualBounds();
        stage.setScene(new Scene(root, b.getWidth(), b.getHeight()));
        stage.setX(b.getMinX()); stage.setY(b.getMinY());
        stage.setWidth(b.getWidth()); stage.setHeight(b.getHeight());
        stage.setMaximized(true); stage.show();
    }

    private void openAdminDashboard(String username) {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Admin/AdminDashboard.fxml"));
            Parent root = l.load();
            AdminDashboardController c = l.getController();
            c.setLoggedInAdmin(username);
            swapScene(root);
        } catch (Exception e) { e.printStackTrace();
            showErrorToast("DASHBOARD ERROR", "Unable to open admin dashboard", "Please try again."); }
    }

    private void openTeacherDashboard(String username) {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Teacher/TeacherDashboard.fxml"));
            Parent root = l.load();
            TeacherDashboardController c = l.getController();
            c.setLoggedInTeacher(username);
            swapScene(root);
        } catch (Exception e) { e.printStackTrace();
            showErrorToast("DASHBOARD ERROR", "Unable to open teacher dashboard", "Please try again."); }
    }

    private void openStaffDashboard(String username) {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Staff/StaffDashboard.fxml"));
            Parent root = l.load();
            StaffDashboardController c = l.getController();
            c.setLoggedInStaff(username);
            swapScene(root);
        } catch (Exception e) { e.printStackTrace();
            showErrorToast("DASHBOARD ERROR", "Unable to open staff dashboard", "Please try again."); }
    }

    @FXML
    private void handleLoginPress() {
        if (btnLogin.isDisabled() || lockoutActive) return;
        btnLogin.setStyle(BTN_PRESSED_STYLE);
    }

    @FXML
    private void handleLoginRelease() {
        if (btnLogin.isDisabled() || lockoutActive) { applyLoginButtonStyle(false); return; }
        applyLoginButtonStyle(true);
    }
}