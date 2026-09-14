package com.example.student_management_system.Controller.Teacher;

import com.example.student_management_system.Controller.DAO.TeacherProfileDAO;
import com.example.student_management_system.Controller.Model.TeacherInfo;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class TeacherProfileController implements Initializable {

    // Left card
    @FXML private ImageView profilePhoto;
    @FXML private Label lblDisplayName;
    @FXML private Label lblTeacherCode;
    @FXML private Label lblEmail;
    @FXML private Label lblPhone;
    @FXML private Label lblRole;

    // Name form
    @FXML private TextField txtFullName;

    // Password form — hidden + visible pair
    @FXML private PasswordField txtCurrentPwd;
    @FXML private TextField     txtCurrentPwdVisible;
    @FXML private Button        btnToggleCurrent;

    @FXML private PasswordField txtNewPwd;
    @FXML private TextField     txtNewPwdVisible;
    @FXML private Button        btnToggleNew;

    @FXML private PasswordField txtConfirmPwd;
    @FXML private TextField     txtConfirmPwdVisible;
    @FXML private Button        btnToggleConfirm;

    // Password rules
    @FXML private Label ruleLength;
    @FXML private Label ruleUpper;
    @FXML private Label ruleLower;
    @FXML private Label ruleSpecial;

    // Toast
    @FXML private VBox profileToast;
    @FXML private Label lblToastTitle;
    @FXML private Label lblToastHeading;
    @FXML private Label lblToastMessage;

    private final TeacherProfileDAO dao = new TeacherProfileDAO();
    private TeacherInfo teacherInfo;
    private PauseTransition toastTimer;

    private boolean currentVisible = false;
    private boolean newVisible     = false;
    private boolean confirmVisible = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Keep the hidden and visible fields in sync
        bindPair(txtCurrentPwd, txtCurrentPwdVisible);
        bindPair(txtNewPwd,     txtNewPwdVisible);
        bindPair(txtConfirmPwd, txtConfirmPwdVisible);

        // Live password rules
        txtNewPwd.textProperty().addListener((o, a, b) -> updateRules(b));
        txtNewPwdVisible.textProperty().addListener((o, a, b) -> updateRules(b));

        updateRules("");
    }

    // ==================================================
    //  TOGGLE HANDLERS
    // ==================================================
    @FXML
    public void toggleCurrent() {
        currentVisible = !currentVisible;
        applyToggle(txtCurrentPwd, txtCurrentPwdVisible, btnToggleCurrent, currentVisible);
    }

    @FXML
    public void toggleNew() {
        newVisible = !newVisible;
        applyToggle(txtNewPwd, txtNewPwdVisible, btnToggleNew, newVisible);
    }

    @FXML
    public void toggleConfirm() {
        confirmVisible = !confirmVisible;
        applyToggle(txtConfirmPwd, txtConfirmPwdVisible, btnToggleConfirm, confirmVisible);
    }

    private void applyToggle(PasswordField hidden, TextField visible, Button btn, boolean showVisible) {
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
    //  LOAD
    // ==================================================
    public void setTeacherInfo(TeacherInfo info) {
        this.teacherInfo = info;

        TeacherInfo fresh = dao.loadProfile(info.getTeacherId());
        if (fresh != null) teacherInfo = fresh;

        lblDisplayName.setText(teacherInfo.getDisplayName());
        lblTeacherCode.setText("Code: " + safe(teacherInfo.getTeacherCode()));
        lblEmail.setText("Email: " + safe(teacherInfo.getEmail()));
        lblPhone.setText("Phone: " + safe(teacherInfo.getPhone()));
        lblRole.setText("Teacher");

        txtFullName.setText(teacherInfo.getDisplayName());

        loadPhoto(teacherInfo.getPhotoPath());
    }

    private void loadPhoto(String path) {
        try {
            if (path != null && !path.isBlank()) {
                if (path.startsWith("http")) {
                    profilePhoto.setImage(new Image(path, true));
                    return;
                }
                File f = new File(path);
                if (f.exists()) {
                    profilePhoto.setImage(new Image(f.toURI().toString()));
                    return;
                }
            }
        } catch (Exception ignored) {}
        try {
            profilePhoto.setImage(new Image(getClass().getResourceAsStream(
                    "/com/example/student_management_system/Images/default_avatar.png")));
        } catch (Exception e) {
            profilePhoto.setImage(null);
        }
    }

    // ==================================================
    //  UPDATE NAME
    // ==================================================
    @FXML
    public void updateName() {
        if (teacherInfo == null) return;

        String newName = txtFullName.getText() == null ? "" : txtFullName.getText().trim();

        if (newName.isEmpty()) {
            toast("UPDATE FAILED", "Missing name", "Please enter your full name.", false);
            return;
        }
        if (newName.length() < 3) {
            toast("UPDATE FAILED", "Name too short", "Name must be at least 3 characters.", false);
            return;
        }

        boolean ok = dao.updateName(teacherInfo.getUserId(), teacherInfo.getTeacherId(), newName);

        if (ok) {
            teacherInfo.setFullName(newName);
            teacherInfo.setTeacherName(newName);
            lblDisplayName.setText(newName);
            toast("UPDATE SUCCESSFUL", "Name updated",
                    "Your display name is now \"" + newName + "\".", true);
        } else {
            toast("UPDATE FAILED", "Database error",
                    "Could not update your name. Try again.", false);
        }
    }

    // ==================================================
    //  UPDATE PASSWORD
    // ==================================================
    @FXML
    public void updatePassword() {
        if (teacherInfo == null) return;

        String current = txtCurrentPwd.getText();
        String newPwd  = txtNewPwd.getText();
        String confirm = txtConfirmPwd.getText();

        if (current == null || current.isEmpty()) {
            toast("PASSWORD FAILED", "Missing current password",
                    "Enter your current password to continue.", false);
            return;
        }
        if (newPwd == null || newPwd.isEmpty()) {
            toast("PASSWORD FAILED", "Missing new password",
                    "Enter a new password.", false);
            return;
        }
        if (!newPwd.equals(confirm)) {
            toast("PASSWORD FAILED", "Passwords do not match",
                    "The two new passwords must be identical.", false);
            return;
        }
        if (!isStrong(newPwd)) {
            toast("PASSWORD FAILED", "Weak password",
                    "Must be 8+ chars with upper, lower, and a special character.", false);
            return;
        }
        if (!dao.checkPassword(teacherInfo.getUserId(), current)) {
            toast("PASSWORD FAILED", "Wrong current password",
                    "The current password you entered is incorrect.", false);
            return;
        }

        boolean ok = dao.updatePassword(teacherInfo.getUserId(), newPwd);

        if (ok) {
            resetToggle(txtCurrentPwd, txtCurrentPwdVisible, btnToggleCurrent);
            resetToggle(txtNewPwd,     txtNewPwdVisible,     btnToggleNew);
            resetToggle(txtConfirmPwd, txtConfirmPwdVisible, btnToggleConfirm);

            currentVisible = newVisible = confirmVisible = false;

            updateRules("");
            toast("PASSWORD UPDATED", "Success",
                    "Your password has been changed.", true);
        } else {
            toast("PASSWORD FAILED", "Database error",
                    "Could not update your password.", false);
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
    //  HELPERS
    // ==================================================
    private boolean isStrong(String pwd) {
        if (pwd == null || pwd.length() < 8) return false;

        boolean upper = false, lower = false, special = false;
        for (char c : pwd.toCharArray()) {
            if (Character.isUpperCase(c)) upper = true;
            else if (Character.isLowerCase(c)) lower = true;
            else if (!Character.isLetterOrDigit(c)) special = true;
        }
        return upper && lower && special;
    }

    private void updateRules(String pwd) {
        if (pwd == null) pwd = "";

        boolean length  = pwd.length() >= 8;
        boolean upper   = pwd.chars().anyMatch(Character::isUpperCase);
        boolean lower   = pwd.chars().anyMatch(Character::isLowerCase);
        boolean special = pwd.chars().anyMatch(c -> !Character.isLetterOrDigit(c));

        styleRule(ruleLength,  "At least 8 characters",        length);
        styleRule(ruleUpper,   "At least 1 uppercase letter",  upper);
        styleRule(ruleLower,   "At least 1 lowercase letter",  lower);
        styleRule(ruleSpecial, "At least 1 special character", special);
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
        if (profileToast == null) return;

        profileToast.setPrefWidth(420);
        profileToast.setMinWidth(420);
        profileToast.setMaxWidth(420);
        profileToast.setPrefHeight(125);
        profileToast.setMinHeight(125);
        profileToast.setMaxHeight(125);

        String bg  = success ? "#dcfce7" : "#fee2e2";
        String fg  = success ? "#16a34a" : "#dc2626";
        String bar = success ? "#22c55e" : "#ef4444";
        String mk  = success ? "✓" : "!";

        lblToastTitle.setText(title);
        lblToastHeading.setText(heading);
        lblToastMessage.setText(msg);

        var iconBox = profileToast.lookup(".toast-icon");
        if (iconBox != null)
            iconBox.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:19;");

        var iconLbl = profileToast.lookup(".toast-icon-label");
        if (iconLbl instanceof Label l) {
            l.setText(mk);
            l.setStyle("-fx-text-fill:" + fg + "; -fx-font-size:17px; -fx-font-weight:bold;");
        }

        var barNode = profileToast.lookup(".toast-bar");
        if (barNode != null)
            barNode.setStyle("-fx-background-color:" + bar + "; -fx-background-radius:3;");

        profileToast.setManaged(true);
        profileToast.setVisible(true);

        if (toastTimer != null) toastTimer.stop();
        toastTimer = new PauseTransition(Duration.seconds(3));
        toastTimer.setOnFinished(e -> {
            profileToast.setVisible(false);
            profileToast.setManaged(false);
        });
        toastTimer.play();
    }
}