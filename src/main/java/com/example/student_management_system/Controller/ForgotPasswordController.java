package com.example.student_management_system.Controller;

import com.example.student_management_system.Controller.Util.EmailService;
import com.example.student_management_system.Controller.Util.PasswordResetService;
import com.example.student_management_system.Controller.Util.PasswordResetService.UserInfo;
import com.example.student_management_system.Controller.Util.PasswordResetService.VerifyResult;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ForgotPasswordController {

    @FXML private StackPane stepContainer;
    @FXML private VBox stepEmailPane;
    @FXML private VBox stepOtpPane;
    @FXML private VBox stepPasswordPane;

    @FXML private TextField     txtEmail;
    @FXML private TextField     txtOtp;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;

    @FXML private Button btnSendOtp;
    @FXML private Button btnVerifyOtp;
    @FXML private Button btnResetPassword;

    @FXML private Label      lblCountdown;
    @FXML private Label      lblMessage;
    @FXML private Hyperlink  lnkResend;

    private String   currentEmail;
    private Timeline countdown;
    private int      secondsLeft;

    @FXML
    public void initialize() {
        txtEmail.setOnAction(e -> handleSendOtp(null));
        txtOtp.setOnAction(e -> handleVerifyOtp(null));
        txtConfirmPassword.setOnAction(e -> handleResetPassword(null));
        setMessage("", false);
        showStep(1);
    }

    // -------------------------------------------------------
    // STEP 1 — send the OTP (works for ADMIN / TEACHER / STAFF)
    // -------------------------------------------------------
    @FXML
    private void handleSendOtp(ActionEvent e) {
        String email = txtEmail.getText() == null ? "" : txtEmail.getText().trim();

        if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
            setMessage("Please enter a valid email address.", true);
            return;
        }

        btnSendOtp.setDisable(true);
        setMessage("Sending OTP...", false);

        new Thread(() -> {
            try {
                UserInfo user = PasswordResetService.findUserByEmail(email);
                if (user == null) {
                    Platform.runLater(() -> {
                        btnSendOtp.setDisable(false);
                        setMessage("No active account found with that email.", true);
                    });
                    return;
                }

                String otp = PasswordResetService.createOtp(user);
                EmailService.sendOtpEmail(user.email, user.fullName, otp,
                        PasswordResetService.OTP_VALID_SECONDS);

                Platform.runLater(() -> {
                    btnSendOtp.setDisable(false);
                    currentEmail = user.email;
                    setMessage("OTP sent to " + maskEmail(user.email)
                                    + " for role " + user.role
                                    + " (expires in " + PasswordResetService.OTP_VALID_SECONDS + "s).",
                            false);
                    showStep(2);
                    startCountdown(PasswordResetService.OTP_VALID_SECONDS);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    btnSendOtp.setDisable(false);
                    setMessage("Failed to send email: " + ex.getMessage(), true);
                });
            }
        }, "otp-email-thread").start();
    }

    // -------------------------------------------------------
    // Resend
    // -------------------------------------------------------
    @FXML
    private void handleResendOtp(ActionEvent e) {
        if (currentEmail == null || currentEmail.isEmpty()) {
            showStep(1);
            return;
        }

        lnkResend.setDisable(true);
        setMessage("Sending new OTP...", false);

        new Thread(() -> {
            try {
                UserInfo user = PasswordResetService.findUserByEmail(currentEmail);
                if (user == null) {
                    Platform.runLater(() -> {
                        lnkResend.setDisable(false);
                        setMessage("Account not found.", true);
                        showStep(1);
                    });
                    return;
                }

                String otp = PasswordResetService.createOtp(user);
                EmailService.sendOtpEmail(user.email, user.fullName, otp,
                        PasswordResetService.OTP_VALID_SECONDS);

                Platform.runLater(() -> {
                    lnkResend.setDisable(false);
                    txtOtp.clear();
                    setMessage("New OTP sent.", false);
                    startCountdown(PasswordResetService.OTP_VALID_SECONDS);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    lnkResend.setDisable(false);
                    setMessage("Failed to resend: " + ex.getMessage(), true);
                });
            }
        }, "otp-resend-thread").start();
    }

    // -------------------------------------------------------
    // STEP 2 — verify
    // -------------------------------------------------------
    @FXML
    private void handleVerifyOtp(ActionEvent e) {
        String code = txtOtp.getText() == null ? "" : txtOtp.getText().trim();

        if (!code.matches("\\d{6}")) {
            setMessage("Enter the 6-digit code.", true);
            return;
        }
        if (secondsLeft <= 0) {
            setMessage("Code has expired. Please resend a new one.", true);
            return;
        }

        try {
            VerifyResult r = PasswordResetService.verifyOtp(currentEmail, code);
            switch (r) {
                case OK:
                    stopCountdown();
                    setMessage("Code verified. Choose a new password.", false);
                    showStep(3);
                    break;
                case EXPIRED:
                    stopCountdown();
                    setMessage("Code has expired. Please resend a new one.", true);
                    break;
                case WRONG:
                    setMessage("Incorrect code. Try again.", true);
                    break;
                case TOO_MANY:
                    setMessage("Too many attempts. Please resend a new code.", true);
                    break;
                case NO_OTP:
                default:
                    setMessage("No active code. Please resend a new one.", true);
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            setMessage("Verification failed: " + ex.getMessage(), true);
        }
    }

    // -------------------------------------------------------
    // STEP 3 — reset
    // -------------------------------------------------------
    @FXML
    private void handleResetPassword(ActionEvent e) {
        String p1 = txtNewPassword.getText() == null ? "" : txtNewPassword.getText();
        String p2 = txtConfirmPassword.getText() == null ? "" : txtConfirmPassword.getText();

        if (p1.length() < 6) {
            setMessage("Password must be at least 6 characters.", true);
            return;
        }
        if (!p1.equals(p2)) {
            setMessage("Passwords do not match.", true);
            return;
        }

        try {
            boolean ok = PasswordResetService.resetPassword(currentEmail, p1);
            if (ok) {
                setMessage("Password updated successfully!", false);

                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setHeaderText("Password Reset");
                a.setContentText("Your password has been reset. "
                        + "Please sign in with your new password.");
                a.showAndWait();

                closeWindow();
            } else {
                setMessage("Reset session expired. Please start again.", true);
                showStep(1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            setMessage("Reset failed: " + ex.getMessage(), true);
        }
    }

    // -------------------------------------------------------
    // Navigation / helpers
    // -------------------------------------------------------
    @FXML
    private void handleBackToEmail(ActionEvent e) {
        stopCountdown();
        showStep(1);
    }

    @FXML
    private void handleBackToLogin(ActionEvent e) {
        closeWindow();
    }

    private void showStep(int step) {
        stepEmailPane.setVisible(step == 1);
        stepEmailPane.setManaged(step == 1);

        stepOtpPane.setVisible(step == 2);
        stepOtpPane.setManaged(step == 2);

        stepPasswordPane.setVisible(step == 3);
        stepPasswordPane.setManaged(step == 3);

        if (step == 1) Platform.runLater(txtEmail::requestFocus);
        if (step == 2) Platform.runLater(txtOtp::requestFocus);
        if (step == 3) Platform.runLater(txtNewPassword::requestFocus);
    }

    private void startCountdown(int seconds) {
        stopCountdown();
        secondsLeft = seconds;
        updateCountdownLabel();
        btnVerifyOtp.setDisable(false);

        countdown = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            secondsLeft--;
            updateCountdownLabel();
            if (secondsLeft <= 0) {
                stopCountdown();
                lblCountdown.setText("Code expired");
                btnVerifyOtp.setDisable(true);
            }
        }));
        countdown.setCycleCount(seconds);
        countdown.play();
    }

    private void stopCountdown() {
        if (countdown != null) {
            countdown.stop();
            countdown = null;
        }
    }

    private void updateCountdownLabel() {
        lblCountdown.setText("Expires in " + secondsLeft + "s");
        lblCountdown.setStyle("-fx-font-size:12px;-fx-text-fill:#dc2626;-fx-font-weight:bold;");
    }

    private void setMessage(String msg, boolean isError) {
        lblMessage.setText(msg == null ? "" : msg);
        lblMessage.setStyle("-fx-font-size:12px;-fx-text-fill:"
                + (isError ? "#dc2626" : "#16a34a") + ";");
    }

    private String maskEmail(String email) {
        if (email == null) return "";
        int at = email.indexOf('@');
        if (at <= 2) return email;
        return email.charAt(0) + "***" + email.substring(at - 1);
    }

    private void closeWindow() {
        stopCountdown();
        Stage stage = (Stage) txtEmail.getScene().getWindow();
        stage.close();
    }
}