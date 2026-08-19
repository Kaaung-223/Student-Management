package com.example.student_management_system.Controller;

import com.example.student_management_system.Controller.DataBase.DataBase_Connection;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {
    @FXML
    private Button btnLogin;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private Button btnEye;

    private boolean showPassword = false;


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);
    }


    // =========================================================
    // TOGGLE PASSWORD
    // =========================================================

    @FXML
    private void togglePassword() {

        showPassword = !showPassword;

        if (showPassword) {

            passwordField.setVisible(false);
            passwordField.setManaged(false);

            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);

            btnEye.setText("🙈");

        } else {

            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);

            passwordField.setVisible(true);
            passwordField.setManaged(true);

            btnEye.setText("👁");
        }
    }


    // =========================================================
    // LOGIN
    // =========================================================

    @FXML
    private void login(ActionEvent event) {

        String username = txtUsername.getText().trim();

        String password;

        if (showPassword) {
            password = visiblePasswordField.getText();
        } else {
            password = passwordField.getText();
        }


        // =====================================================
        // EMPTY CHECK
        // =====================================================

        if (username.isEmpty() || password.isEmpty()) {

            Alert alert =
                    new Alert(Alert.AlertType.WARNING);

            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Please enter username and password"
            );

            alert.showAndWait();

            return;
        }


        // =====================================================
        // DATABASE
        // =====================================================

        try {

            Connection con =
                    DataBase_Connection.getConnection();

            if (con == null) {

                Alert alert =
                        new Alert(Alert.AlertType.ERROR);

                alert.setTitle("Database Error");
                alert.setHeaderText(null);
                alert.setContentText(
                        "Database Connection Failed"
                );

                alert.showAndWait();

                return;
            }


            String sql =
                    "SELECT * FROM users " +
                            "WHERE username=? " +
                            "AND password=? " +
                            "AND status='ACTIVE'";


            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setString(1, username);
            ps.setString(2, password);


            ResultSet rs =
                    ps.executeQuery();


            // =================================================
            // LOGIN SUCCESS
            // =================================================

            if (rs.next()) {

                String role =
                        rs.getString("role");


                // =============================================
                // ADMIN
                // =============================================

                if (role.equals("ADMIN")) {
                    Alert alert=new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Welcome Admin");
                    alert.setContentText("Welcome Admin");
                    alert.showAndWait();
                    openAdminDashboard();

                }


                // =============================================
                // TEACHER
                // =============================================

                else if (role.equals("TEACHER")) {

                    Alert alert =
                            new Alert(
                                    Alert.AlertType.INFORMATION
                            );

                    alert.setTitle("Teacher Login");
                    alert.setHeaderText(null);
                    alert.setContentText(
                            "Teacher Login"
                    );

                    alert.showAndWait();

                    // Teacher dashboard later
                }

            } else {

                // =================================================
                // LOGIN FAILED
                // =================================================

                Alert alert =
                        new Alert(
                                Alert.AlertType.ERROR
                        );

                alert.setTitle("Login Failed");
                alert.setHeaderText(null);
                alert.setContentText(
                        "Incorrect Username or Password"
                );

                alert.showAndWait();
            }


            rs.close();
            ps.close();
            con.close();


        } catch (Exception e) {

            e.printStackTrace();

            Alert alert =
                    new Alert(
                            Alert.AlertType.ERROR
                    );

            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(
                    e.getMessage()
            );

            alert.showAndWait();
        }
    }


    // =========================================================
    // OPEN ADMIN DASHBOARD
    // =========================================================

    private void openAdminDashboard() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/example/student_management_system/View/Admin/AdminDashboard.fxml"
                    )
            );

            Parent root = loader.load();

            Stage stage = (Stage) txtUsername.getScene().getWindow();

            // Get the full visible screen area (excludes taskbar/dock)
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

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Dashboard Error");
            alert.setHeaderText("Cannot Open Admin Dashboard");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    @FXML
    private void handleLoginPress() {
        // Darker background and slight shrink when pressed
        btnLogin.setStyle("-fx-background-color: #1E4ED8; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-font-size: 16; -fx-font-weight: bold; " +
                "-fx-scale-x: 0.98; -fx-scale-y: 0.98; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
    }

    @FXML
    private void handleLoginRelease() {
        // Restore original style
        btnLogin.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-font-size: 16; -fx-font-weight: bold; " +
                "-fx-scale-x: 1.0; -fx-scale-y: 1.0; " +
                "-fx-effect: null;");
    }
}