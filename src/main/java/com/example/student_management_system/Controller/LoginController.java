package com.example.student_management_system.Controller;

import com.example.student_management_system.Controller.Admin.AdminDashboardController;
import com.example.student_management_system.Controller.DAO.DBConnention;

import javafx.animation.PauseTransition;
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
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnEye;

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label lblLoginMessage;

    @FXML
    private FontIcon eyeIcon;

    private boolean showPassword = false;

    @FXML
    public void initialize() {

        /*
         * Keep both password fields synchronized.
         */
        visiblePasswordField.textProperty()
                .bindBidirectional(passwordField.textProperty());

        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        eyeIcon.setIconLiteral("fas-eye");

        /*
         * Pressing ENTER in the username field
         * clicks/fires the login button.
         */
        txtUsername.setOnAction(event -> btnLogin.fire());

        /*
         * Pressing ENTER in the password field
         * clicks/fires the login button.
         */
        passwordField.setOnAction(event -> btnLogin.fire());

        /*
         * Pressing ENTER in the visible password field
         * also clicks/fires the login button.
         */
        visiblePasswordField.setOnAction(event -> btnLogin.fire());

        /*
         * Makes the login button respond to ENTER
         * when another control has focus.
         */
        btnLogin.setDefaultButton(true);
    }

    @FXML
    private void togglePassword() {

        showPassword = !showPassword;

        passwordField.setVisible(!showPassword);
        passwordField.setManaged(!showPassword);

        visiblePasswordField.setVisible(showPassword);
        visiblePasswordField.setManaged(showPassword);

        eyeIcon.setIconLiteral(
                showPassword ? "fas-eye-slash" : "fas-eye"
        );
    }

    @FXML
    private void login(ActionEvent event) {

        String username = txtUsername.getText().trim();

        String password = showPassword
                ? visiblePasswordField.getText()
                : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter your username and password.");
            return;
        }

        String sql =
                "SELECT * FROM users " +
                        "WHERE username = ? " +
                        "AND password = ? " +
                        "AND status = 'ACTIVE'";

        try (Connection con = DBConnention.getConnection()) {

            if (con == null) {
                showError("Database connection is unavailable.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, username);
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        showError("Incorrect username or password.");
                        return;
                    }

                    String role = rs.getString("role");

                    if ("ADMIN".equalsIgnoreCase(role)) {

                        openAdminDashboard(username);

                    } else if ("TEACHER".equalsIgnoreCase(role)) {

                        showError("Teacher dashboard is coming soon.");

                    } else {

                        showError("Your account role is not supported.");
                    }
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            showError("Please check your database connection.");
        }
    }

    private void showError(String message) {

        lblLoginMessage.setText(message);

        lblLoginMessage.setManaged(true);
        lblLoginMessage.setVisible(true);

        PauseTransition delay =
                new PauseTransition(Duration.seconds(4));

        delay.setOnFinished(event -> {
            lblLoginMessage.setVisible(false);
            lblLoginMessage.setManaged(false);
        });

        delay.play();
    }

    private void openAdminDashboard(String username) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/com/example/student_management_system/View/Admin/AdminDashboard.fxml"
                            )
                    );

            Parent root = loader.load();

            AdminDashboardController controller =
                    loader.getController();

            controller.setLoggedInAdmin(username);

            Stage stage =
                    (Stage) txtUsername.getScene().getWindow();

            Rectangle2D bounds =
                    Screen.getPrimary().getVisualBounds();

            Scene scene =
                    new Scene(
                            root,
                            bounds.getWidth(),
                            bounds.getHeight()
                    );

            stage.setScene(scene);

            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());

            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());

            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            showError("Unable to open the dashboard.");
        }
    }

    @FXML
    private void handleLoginPress() {

        btnLogin.setStyle(
                "-fx-background-color:#3730a3;" +
                        "-fx-text-fill:white;" +
                        "-fx-background-radius:10;" +
                        "-fx-font-size:13px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-cursor:hand;" +
                        "-fx-scale-x:.98;" +
                        "-fx-scale-y:.98;"
        );
    }

    @FXML
    private void handleLoginRelease() {

        btnLogin.setStyle(
                "-fx-background-color:#4f46e5;" +
                        "-fx-text-fill:white;" +
                        "-fx-background-radius:10;" +
                        "-fx-font-size:13px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-cursor:hand;" +
                        "-fx-scale-x:1;" +
                        "-fx-scale-y:1;"
        );
    }
}