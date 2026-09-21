package com.example.student_management_system.Controller;

import com.example.student_management_system.Controller.DAO.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

/**
 * Reusable Change-Password dialog for ADMIN / TEACHER / STAFF.
 * Call setUsername(...) from the dashboard before showing it.
 */
public class ChangePasswordController {

    @FXML private PasswordField txtOldPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label         lblMessage;

    private String username;

    public void setUsername(String username) { this.username = username; }

    @FXML
    private void handleSave(ActionEvent e) {
        String oldPw = txtOldPassword.getText() == null ? "" : txtOldPassword.getText();
        String newPw = txtNewPassword.getText() == null ? "" : txtNewPassword.getText();
        String conPw = txtConfirmPassword.getText() == null ? "" : txtConfirmPassword.getText();

        if (username == null || username.isBlank()) { setMessage("No logged-in user.", true); return; }
        if (oldPw.isEmpty() || newPw.isEmpty() || conPw.isEmpty()) {
            setMessage("Please fill in all fields.", true); return;
        }
        if (newPw.length() < 6) { setMessage("New password must be at least 6 characters.", true); return; }
        if (!newPw.equals(conPw)) { setMessage("Passwords do not match.", true); return; }
        if (newPw.equals(oldPw)) { setMessage("New password must differ from current.", true); return; }

        try {
            boolean ok = UserService.changeOwnPassword(username, oldPw, newPw);
            if (!ok) { setMessage("Current password is incorrect.", true); return; }

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText("Password Updated");
            a.setContentText("Your password has been changed successfully.");
            a.showAndWait();
            closeWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            setMessage("Error: " + ex.getMessage(), true);
        }
    }

    @FXML
    private void handleCancel(ActionEvent e) { closeWindow(); }

    private void setMessage(String msg, boolean error) {
        lblMessage.setText(msg);
        lblMessage.setStyle("-fx-font-size:12px;-fx-text-fill:"
                + (error ? "#dc2626" : "#16a34a") + ";");
    }

    private void closeWindow() {
        ((Stage) txtOldPassword.getScene().getWindow()).close();
    }
}