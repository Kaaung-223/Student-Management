package com.example.student_management_system.Controller;

import com.example.student_management_system.Controller.Util.EmailService;
import com.example.student_management_system.Controller.Util.PasswordResetService;
import com.example.student_management_system.Controller.Util.PasswordResetService.UserInfo;
import com.example.student_management_system.Controller.Util.PasswordResetService.VerifyResult;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.regex.Pattern;

public class ForgotPasswordController {

    // ---- Password policy ----
    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPER   = Pattern.compile("[A-Z]");
    private static final Pattern LOWER   = Pattern.compile("[a-z]");
    private static final Pattern SPECIAL = Pattern.compile("[^a-zA-Z0-9\\s]");

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
    @FXML private Hyperlink  lnkResend;

    // Password requirement labels
    @FXML private Label reqLength;
    @FXML private Label reqUpper;
    @FXML private Label reqLower;
    @FXML private Label reqSpecial;

    // ---- Toast (same style as login) ----
    @FXML private VBox      fpToast;
    @FXML private StackPane fpToastIconWrap;
    @FXML private Label     fpToastIcon;
    @FXML private Label     fpToastTitle;
    @FXML private Label     fpToastHeading;
    @FXML private Label     fpToastMessage;
    @FXML private Region    fpToastAccent;

    private String   currentEmail;
    private Timeline countdown;
    private int      secondsLeft;
    private PauseTransition toastTimer;

    @FXML
    public void initialize() {
        txtEmail.setOnAction(e -> handleSendOtp(null));
        txtOtp.setOnAction(e -> handleVerifyOtp(null));
        txtConfirmPassword.setOnAction(e -> handleResetPassword(null));

        // Live password checklist
        txtNewPassword.textProperty().addListener(
                (obs, oldV, newV) -> updatePasswordRequirements(newV));
        updatePasswordRequirements("");

        hideToast();
        showStep(1);
    }

    // =======================================================
    // STEP 1 — send OTP
    // =======================================================
    @FXML
    private void handleSendOtp(ActionEvent e) {
        String email = txtEmail.getText() == null ? "" : txtEmail.getText().trim();

        if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
            showErrorToast("FORGOT PASSWORD", "Invalid email",
                    "Please enter a valid email address.");
            return;
        }

        btnSendOtp.setDisable(true);
        showInfoToast("FORGOT PASSWORD", "Sending…",
                "Please wait while we send your code.");

        new Thread(() -> {
            try {
                UserInfo user = PasswordResetService.findUserByEmail(email);
                if (user == null) {
                    Platform.runLater(() -> {
                        btnSendOtp.setDisable(false);
                        showErrorToast("FORGOT PASSWORD", "Account not found",
                                "No active account with that email.");
                    });
                    return;
                }

                String otp = PasswordResetService.createOtp(user);
                EmailService.sendOtpEmail(user.email, user.fullName, otp,
                        PasswordResetService.OTP_VALID_SECONDS);

                Platform.runLater(() -> {
                    btnSendOtp.setDisable(false);
                    currentEmail = user.email;
                    showSuccessToast("FORGOT PASSWORD", "OTP sent",
                            "Code sent to " + maskEmail(user.email)
                                    + " (expires in "
                                    + PasswordResetService.OTP_VALID_SECONDS + "s).");
                    showStep(2);
                    startCountdown(PasswordResetService.OTP_VALID_SECONDS);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    btnSendOtp.setDisable(false);
                    showErrorToast("EMAIL FAILED", "Could not send",
                            ex.getMessage() == null ? "Unknown error." : ex.getMessage());
                });
            }
        }, "otp-email-thread").start();
    }

    // =======================================================
    // Resend
    // =======================================================
    @FXML
    private void handleResendOtp(ActionEvent e) {
        if (currentEmail == null || currentEmail.isEmpty()) {
            showStep(1);
            return;
        }

        lnkResend.setDisable(true);
        showInfoToast("FORGOT PASSWORD", "Sending new OTP…",
                "Please wait a moment.");

        new Thread(() -> {
            try {
                UserInfo user = PasswordResetService.findUserByEmail(currentEmail);
                if (user == null) {
                    Platform.runLater(() -> {
                        lnkResend.setDisable(false);
                        showErrorToast("FORGOT PASSWORD", "Account not found",
                                "Please start again from the email step.");
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
                    showSuccessToast("FORGOT PASSWORD", "New OTP sent",
                            "Check your inbox for the new code.");
                    startCountdown(PasswordResetService.OTP_VALID_SECONDS);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    lnkResend.setDisable(false);
                    showErrorToast("EMAIL FAILED", "Could not resend",
                            ex.getMessage() == null ? "Unknown error." : ex.getMessage());
                });
            }
        }, "otp-resend-thread").start();
    }

    // =======================================================
    // STEP 2 — verify OTP
    // =======================================================
    @FXML
    private void handleVerifyOtp(ActionEvent e) {
        String code = txtOtp.getText() == null ? "" : txtOtp.getText().trim();

        if (!code.matches("\\d{6}")) {
            showErrorToast("FORGOT PASSWORD", "Invalid code",
                    "Enter the 6-digit code we emailed you.");
            return;
        }
        if (secondsLeft <= 0) {
            showErrorToast("CODE EXPIRED", "Please resend",
                    "The code has expired. Tap Resend to get a new one.");
            return;
        }

        try {
            VerifyResult r = PasswordResetService.verifyOtp(currentEmail, code);
            switch (r) {
                case OK:
                    stopCountdown();
                    showSuccessToast("FORGOT PASSWORD", "Code verified",
                            "Please choose a new password.");
                    showStep(3);
                    break;
                case EXPIRED:
                    stopCountdown();
                    showErrorToast("CODE EXPIRED", "Please resend",
                            "The code has expired. Tap Resend to get a new one.");
                    break;
                case WRONG:
                    showErrorToast("FORGOT PASSWORD", "Incorrect code",
                            "That code doesn't match. Please try again.");
                    break;
                case TOO_MANY:
                    showErrorToast("FORGOT PASSWORD", "Too many attempts",
                            "Please request a new code.");
                    break;
                case NO_OTP:
                default:
                    showErrorToast("FORGOT PASSWORD", "No active code",
                            "Please request a new code.");
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorToast("VERIFY FAILED", "Try again",
                    ex.getMessage() == null ? "Unknown error." : ex.getMessage());
        }
    }

    // =======================================================
    // STEP 3 — reset password
    // =======================================================
    @FXML
    private void handleResetPassword(ActionEvent e) {
        String p1 = txtNewPassword.getText() == null ? "" : txtNewPassword.getText();
        String p2 = txtConfirmPassword.getText() == null ? "" : txtConfirmPassword.getText();

        String pwError = validatePassword(p1);
        if (pwError != null) {
            showErrorToast("FORGOT PASSWORD", "Weak password", pwError);
            return;
        }
        if (!p1.equals(p2)) {
            showErrorToast("FORGOT PASSWORD", "Passwords don't match",
                    "The two passwords must be identical.");
            return;
        }

        try {
            boolean ok = PasswordResetService.resetPassword(currentEmail, p1);
            if (ok) {
                showSuccessToast("FORGOT PASSWORD", "Password updated",
                        "You can now sign in with your new password.");

                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setHeaderText("Password Reset");
                a.setContentText("Your password has been reset. "
                        + "Please sign in with your new password.");
                a.showAndWait();

                closeWindow();
            } else {
                showErrorToast("RESET EXPIRED", "Please start again",
                        "Your verification session has expired.");
                showStep(1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorToast("RESET FAILED", "Try again",
                    ex.getMessage() == null ? "Unknown error." : ex.getMessage());
        }
    }

    // =======================================================
    // Password policy
    // =======================================================
    private String validatePassword(String pw) {
        if (pw == null || pw.length() < MIN_LENGTH) {
            return "Password must be at least " + MIN_LENGTH + " characters.";
        }
        if (!UPPER.matcher(pw).find()) {
            return "Password must contain at least one uppercase letter (A–Z).";
        }
        if (!LOWER.matcher(pw).find()) {
            return "Password must contain at least one lowercase letter (a–z).";
        }
        if (!SPECIAL.matcher(pw).find()) {
            return "Password must contain at least one special character (e.g. !@#$).";
        }
        return null;
    }

    private void updatePasswordRequirements(String pw) {
        if (pw == null) pw = "";
        markRequirement(reqLength,  pw.length() >= MIN_LENGTH,
                "At least " + MIN_LENGTH + " characters");
        markRequirement(reqUpper,   UPPER.matcher(pw).find(),
                "At least 1 uppercase letter (A–Z)");
        markRequirement(reqLower,   LOWER.matcher(pw).find(),
                "At least 1 lowercase letter (a–z)");
        markRequirement(reqSpecial, SPECIAL.matcher(pw).find(),
                "At least 1 special character (!@#$…)");
    }

    private void markRequirement(Label lbl, boolean ok, String text) {
        lbl.setText((ok ? "  ✓  " : "  •  ") + text);
        lbl.setStyle("-fx-font-size:11px;-fx-text-fill:"
                + (ok ? "#16a34a" : "#94a3b8") + ";");
    }

    // =======================================================
    // Navigation / helpers
    // =======================================================
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
        lblCountdown.setStyle(
                "-fx-font-size:12px;-fx-text-fill:#dc2626;-fx-font-weight:bold;");
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

    // =======================================================
    // TOAST — identical layout to LoginController's toast
    // =======================================================
    /** Red toast — for errors, exactly like the login page. */
    private void showErrorToast(String title, String heading, String message) {
        fpToastIconWrap.setStyle("-fx-background-color:#fee2e2;-fx-background-radius:19;");
        fpToastIcon.setText("✕");
        fpToastIcon.setStyle("-fx-text-fill:#dc2626;-fx-font-size:17px;-fx-font-weight:bold;");
        fpToastAccent.setStyle("-fx-background-color:#ef4444;-fx-background-radius:3;");

        showToast(title, heading, message, 5);
    }

    /** Green toast — for successes, same layout as the error toast. */
    private void showSuccessToast(String title, String heading, String message) {
        fpToastIconWrap.setStyle("-fx-background-color:#dcfce7;-fx-background-radius:19;");
        fpToastIcon.setText("✓");
        fpToastIcon.setStyle("-fx-text-fill:#16a34a;-fx-font-size:17px;-fx-font-weight:bold;");
        fpToastAccent.setStyle("-fx-background-color:#22c55e;-fx-background-radius:3;");

        showToast(title, heading, message, 3);
    }

    /** Neutral blue-ish toast — for "sending…" style info messages. */
    private void showInfoToast(String title, String heading, String message) {
        fpToastIconWrap.setStyle("-fx-background-color:#e0e7ff;-fx-background-radius:19;");
        fpToastIcon.setText("i");
        fpToastIcon.setStyle("-fx-text-fill:#4f46e5;-fx-font-size:17px;-fx-font-weight:bold;");
        fpToastAccent.setStyle("-fx-background-color:#6366f1;-fx-background-radius:3;");

        showToast(title, heading, message, 3);
    }

    private void showToast(String title, String heading, String message, int hideAfterSeconds) {
        fpToastTitle.setText(title);
        fpToastHeading.setText(heading);
        fpToastMessage.setText(message);

        // Fixed size — identical to login toast
        fpToast.setPrefSize(350, 125);
        fpToast.setMinSize(350, 125);
        fpToast.setMaxSize(350, 125);

        fpToast.setManaged(true);
        fpToast.setVisible(true);

        if (toastTimer != null) toastTimer.stop();
        toastTimer = new PauseTransition(Duration.seconds(hideAfterSeconds));
        toastTimer.setOnFinished(ev -> hideToast());
        toastTimer.play();
    }

    private void hideToast() {
        fpToast.setVisible(false);
        fpToast.setManaged(false);
    }
}