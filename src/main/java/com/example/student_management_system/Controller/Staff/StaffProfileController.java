package com.example.student_management_system.Controller.Staff;

import com.example.student_management_system.Controller.DAO.StaffProfileDAO;
import com.example.student_management_system.Controller.Model.StaffInfo;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.util.Duration;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class StaffProfileController implements Initializable {

    // Left card
    @FXML private ImageView profilePhoto;
    @FXML private Label lblDisplayName;
    @FXML private Label lblStaffCode;
    @FXML private Label lblEmail;
    @FXML private Label lblPhone;
    @FXML private Label lblRole;
    @FXML private Label lblSalary;

    // Name form
    @FXML private TextField txtFullName;

    // Password fields — hidden + visible pair
    @FXML private PasswordField txtCurrentPwd;
    @FXML private TextField     txtCurrentPwdVisible;
    @FXML private Button        btnToggleCurrent;

    @FXML private PasswordField txtNewPwd;
    @FXML private TextField     txtNewPwdVisible;
    @FXML private Button        btnToggleNew;

    @FXML private PasswordField txtConfirmPwd;
    @FXML private TextField     txtConfirmPwdVisible;
    @FXML private Button        btnToggleConfirm;

    // Live rules
    @FXML private Label ruleLength;
    @FXML private Label ruleUpper;
    @FXML private Label ruleLower;
    @FXML private Label ruleSpecial;

    // Toast
    @FXML private VBox profileToast;
    @FXML private Label lblToastTitle;
    @FXML private Label lblToastHeading;
    @FXML private Label lblToastMessage;

    private final StaffProfileDAO dao = new StaffProfileDAO();
    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(Locale.US);

    private StaffInfo staffInfo;
    private PauseTransition toastTimer;

    private boolean currentVisible = false;
    private boolean newVisible     = false;
    private boolean confirmVisible = false;

    // =========================================================
    //  INIT
    // =========================================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bindPair(txtCurrentPwd, txtCurrentPwdVisible);
        bindPair(txtNewPwd,     txtNewPwdVisible);
        bindPair(txtConfirmPwd, txtConfirmPwdVisible);

        txtNewPwd.textProperty().addListener((o, a, b) -> updateRules(b));
        txtNewPwdVisible.textProperty().addListener((o, a, b) -> updateRules(b));

        updateRules("");
    }

    /** Called by StaffDashboardController. */
    public void setStaffInfo(StaffInfo info) {
        this.staffInfo = info;

        StaffInfo fresh = dao.loadProfile(info.getStaffId());
        if (fresh != null) staffInfo = fresh;

        lblDisplayName.setText(staffInfo.getDisplayName());
        lblStaffCode.setText("Code: " + safe(staffInfo.getStaffCode()));
        lblEmail.setText("Email: " + safe(staffInfo.getEmail()));
        lblPhone.setText("Phone: " + safe(staffInfo.getPhone()));
        lblRole.setText("Staff");

        BigDecimal sal = staffInfo.getSalary();
        lblSalary.setText(sal == null ? "Salary: --" : "Salary: " + MONEY.format(sal) + " MMK");

        txtFullName.setText(staffInfo.getDisplayName());

        loadPhoto(staffInfo.getPhotoPath());
    }

    // =========================================================
    //  HIGH-QUALITY PHOTO — HiDPI aware, center-cropped, circular
    // =========================================================
    private void loadPhoto(String path) {
        if (profilePhoto == null) return;

        final double SIZE = 110;
        final double RADIUS = SIZE / 2;

        double dpiScale = 1.0;
        try { dpiScale = Screen.getPrimary().getOutputScaleX(); }
        catch (Exception ignored) {}
        double loadScale = Math.max(2.0, dpiScale) * 1.5;
        final double LOAD_SIZE = SIZE * loadScale;

        Image image = loadImageFromPath(path, LOAD_SIZE);
        if (image == null) image = loadDefaultAvatar(LOAD_SIZE);

        if (image != null) {
            double iw = image.getWidth();
            double ih = image.getHeight();
            Rectangle2D viewport = null;
            if (iw > 0 && ih > 0) {
                double side = Math.min(iw, ih);
                viewport = new Rectangle2D(
                        (iw - side) / 2,
                        (ih - side) / 2,
                        side, side);
            }

            profilePhoto.setImage(image);
            if (viewport != null) profilePhoto.setViewport(viewport);
            profilePhoto.setFitWidth(SIZE);
            profilePhoto.setFitHeight(SIZE);
            profilePhoto.setPreserveRatio(false);
            profilePhoto.setSmooth(true);
            profilePhoto.setCache(true);
            profilePhoto.setClip(new Circle(RADIUS, RADIUS, RADIUS));
        } else {
            profilePhoto.setImage(null);
        }
    }

    /** Loads the packaged default avatar (5-param InputStream ctor). */
    private Image loadDefaultAvatar(double size) {
        try (var in = getClass().getResourceAsStream(
                "/com/example/student_management_system/Images/default_avatar.png")) {
            if (in == null) return null;
            return new Image(in, size, size, false, true);
        } catch (Exception e) {
            return null;
        }
    }

    /** Loads an image from disk (absolute or relative) or URL. */
    private Image loadImageFromPath(String path, double size) {
        if (path == null || path.isBlank()) return null;
        try {
            if (path.startsWith("http")) {
                return new Image(path, size, size, false, true, true);
            }
            File f = new File(path);
            if (f.exists() && f.isFile()) {
                return new Image(f.toURI().toString(), size, size, false, true, true);
            }
            File rel = new File(System.getProperty("user.dir"), path);
            if (rel.exists() && rel.isFile()) {
                return new Image(rel.toURI().toString(), size, size, false, true, true);
            }
        } catch (Exception ignored) {}
        return null;
    }

    // =========================================================
    //  UPDATE NAME  →  users table ONLY
    // =========================================================
    @FXML
    public void updateName() {
        if (staffInfo == null) return;

        String newName = txtFullName.getText() == null ? "" : txtFullName.getText().trim();

        if (newName.isEmpty()) {
            toast("UPDATE FAILED", "Missing name", "Please enter your full name.", false);
            return;
        }
        if (newName.length() < 3) {
            toast("UPDATE FAILED", "Name too short",
                    "Name must be at least 3 characters.", false);
            return;
        }

        boolean ok = dao.updateName(staffInfo.getUserId(), newName);

        if (ok) {
            staffInfo.setFullName(newName);
            staffInfo.setStaffName(newName);   // keep in-memory copy consistent
            lblDisplayName.setText(newName);
            toast("UPDATE SUCCESSFUL", "Name updated",
                    "Your display name is now \"" + newName + "\".", true);
        } else {
            toast("UPDATE FAILED", "Database error",
                    "Could not update your name. Try again.", false);
        }
    }

    // =========================================================
    //  UPDATE PASSWORD
    // =========================================================
    @FXML
    public void updatePassword() {
        if (staffInfo == null) return;

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
        if (!dao.checkPassword(staffInfo.getUserId(), current)) {
            toast("PASSWORD FAILED", "Wrong current password",
                    "The current password you entered is incorrect.", false);
            return;
        }

        boolean ok = dao.updatePassword(staffInfo.getUserId(), newPwd);

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

    // =========================================================
    //  EYE TOGGLES
    // =========================================================
    @FXML public void toggleCurrent() {
        currentVisible = !currentVisible;
        applyToggle(txtCurrentPwd, txtCurrentPwdVisible, btnToggleCurrent, currentVisible);
    }

    @FXML public void toggleNew() {
        newVisible = !newVisible;
        applyToggle(txtNewPwd, txtNewPwdVisible, btnToggleNew, newVisible);
    }

    @FXML public void toggleConfirm() {
        confirmVisible = !confirmVisible;
        applyToggle(txtConfirmPwd, txtConfirmPwdVisible, btnToggleConfirm, confirmVisible);
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

    // =========================================================
    //  PASSWORD RULES
    // =========================================================
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

    // =========================================================
    //  TOAST
    // =========================================================
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