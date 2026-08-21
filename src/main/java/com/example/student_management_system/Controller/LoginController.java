package com.example.student_management_system.Controller;

import com.example.student_management_system.Controller.Admin.AdminDashboardController;
import com.example.student_management_system.Controller.DAO.DBConnention;

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
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private Button btnLogin;
    @FXML private TextField txtUsername;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private StackPane eyeToggle;
    @FXML private Label eyeLabel;

    @FXML private StackPane messageOverlay;
    @FXML private Label lblWelcomeMessage;
    @FXML private Button btnMessageAction;

    private boolean showPassword = false;
    private Runnable messageAction;

    @FXML
    public void initialize() {
        visiblePasswordField.textProperty()
                .bindBidirectional(passwordField.textProperty());

        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        messageOverlay.setVisible(false);
        messageOverlay.setManaged(false);
    }

    @FXML
    private void togglePassword() {
        showPassword = !showPassword;

        passwordField.setVisible(!showPassword);
        passwordField.setManaged(!showPassword);

        visiblePasswordField.setVisible(showPassword);
        visiblePasswordField.setManaged(showPassword);

        eyeLabel.setText(showPassword ? "🙈" : "👁");
    }

    @FXML
    private void login(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = showPassword
                ? visiblePasswordField.getText()
                : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter your username and password.", null);
            return;
        }

        String sql = "SELECT * FROM users " +
                "WHERE username=? AND password=? AND status='ACTIVE'";

        try (
                Connection con = DBConnention.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            if (con == null) {
                showMessage("Database connection unavailable.", null);
                return;
            }

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    showMessage("Incorrect username or password.", null);
                    return;
                }

                String role = rs.getString("role");

                if ("ADMIN".equalsIgnoreCase(role)) {
                    showMessage("Welcome back, " + username + "!", () -> openAdminDashboard(username));
                } else if ("TEACHER".equalsIgnoreCase(role)) {
                    showMessage("Welcome back, " + username + "!", null); // add teacher dashboard later
                } else {
                    showMessage("Role not supported.", null);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Check database connection.", null);
        }
    }

    private void showMessage(String message, Runnable action) {
        lblWelcomeMessage.setText(message);
        messageAction = action;

        messageOverlay.setManaged(true);
        messageOverlay.setVisible(true);
    }

    @FXML
    private void handleMessageAction() {
        messageOverlay.setVisible(false);
        messageOverlay.setManaged(false);

        if (messageAction != null) {
            Runnable action = messageAction;
            messageAction = null;
            action.run();
        }
    }

    private void openAdminDashboard(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/example/student_management_system/View/Admin/AdminDashboard.fxml"
                    )
            );

            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.setLoggedInAdmin(username);

            Stage stage = (Stage) txtUsername.getScene().getWindow();

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            Scene scene = new Scene(
                    root,
                    screenBounds.getWidth(),
                    screenBounds.getHeight()
            );

            stage.setScene(scene);
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Unable to open dashboard.", null);
        }
    }

    @FXML
    private void handleLoginPress() {
        btnLogin.setStyle(
                "-fx-background-color: #3730a3; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 10; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-scale-x: 0.98; " +
                        "-fx-scale-y: 0.98;"
        );
    }

    @FXML
    private void handleLoginRelease() {
        btnLogin.setStyle(
                "-fx-background-color: #4f46e5; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 10; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-scale-x: 1; " +
                        "-fx-scale-y: 1;"
        );
    }
}