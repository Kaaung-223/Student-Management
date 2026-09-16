package com.example.student_management_system.Controller.Staff;

import com.example.student_management_system.Controller.DAO.StaffDashboardDAO;
import com.example.student_management_system.Controller.Model.StaffInfo;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class StaffDashboardController implements Initializable {

    // Sidebar buttons
    @FXML private Button btnDashboard, btnStudents, btnPayments, btnLeaveRequests,
            btnAttendance, btnAnnouncements, btnProfile, btnLogout;

    // Center content
    @FXML private StackPane contentPane;
    @FXML private ScrollPane dashboardScrollPane;
    @FXML private AnchorPane staffPane;

    // Welcome toast
    @FXML private VBox welcomeToast;
    @FXML private Label lblToastTitle, lblToastHeading, lblDashboardWelcome;

    // Header
    @FXML private ImageView staffPhoto;
    @FXML private Label lblStaffName;

    // Stat cards
    @FXML private Label lblTotalStudents, lblTotalClasses,
            lblCollectedThisMonth, lblPendingLeaves;

    // Charts
    @FXML private PieChart feePieChart;
    @FXML private PieChart methodPieChart;

    // Finance summary
    @FXML private Label lblPaidCount, lblUnpaidCount, lblTotalCollected;

    // DAO + session
    private final StaffDashboardDAO dao = new StaffDashboardDAO();
    private StaffInfo staffInfo;
    private PauseTransition welcomeTimer;

    private static final NumberFormat MONEY =
            NumberFormat.getNumberInstance(Locale.US);

    private static final String ACTIVE =
            "-fx-background-color:#4f46e5; -fx-background-radius:10; -fx-text-fill:white;" +
                    "-fx-font-size:13px; -fx-font-weight:bold; -fx-alignment:CENTER_LEFT;" +
                    "-fx-padding:0 16; -fx-cursor:hand;";
    private static final String NORMAL =
            "-fx-background-color:transparent; -fx-text-fill:#cbd5e1; -fx-font-size:13px;" +
                    "-fx-alignment:CENTER_LEFT; -fx-padding:0 16; -fx-cursor:hand;";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setActiveButton(btnDashboard);
    }

    /** Called from LoginController after a successful STAFF login. */
    public void setLoggedInStaff(String username) {
        staffInfo = dao.getStaffByUsername(username);

        if (staffInfo != null) {
            lblStaffName.setText(staffInfo.getDisplayName());
            loadPhoto(staffInfo.getPhotoPath());
            loadDashboardData();
            showWelcomeToast(staffInfo.getDisplayName());
        } else {
            lblStaffName.setText(username);
            loadDefaultPhoto();
            showWelcomeToast(username);
        }
    }

    // -------------------------------------------------
    //  Photo
    // -------------------------------------------------
    private void loadPhoto(String path) {
        try {
            if (path != null && !path.isBlank()) {
                if (path.startsWith("http")) {
                    staffPhoto.setImage(new Image(path, true));
                    return;
                }
                File f = new File(path);
                if (f.exists()) {
                    staffPhoto.setImage(new Image(f.toURI().toString()));
                    return;
                }
            }
        } catch (Exception ignored) {}
        loadDefaultPhoto();
    }

    private void loadDefaultPhoto() {
        try {
            Image img = new Image(getClass().getResourceAsStream(
                    "/com/example/student_management_system/Images/default_avatar.png"));
            staffPhoto.setImage(img);
        } catch (Exception e) {
            staffPhoto.setImage(null);
        }
    }

    // -------------------------------------------------
    //  Data
    // -------------------------------------------------
    private void loadDashboardData() {
        // Stat cards
        lblTotalStudents.setText(String.valueOf(dao.getTotalStudents()));
        lblTotalClasses.setText(String.valueOf(dao.getTotalClasses()));

        BigDecimal month = dao.getPaymentsThisMonth();
        lblCollectedThisMonth.setText(MONEY.format(month) + " MMK");

        lblPendingLeaves.setText(String.valueOf(dao.getPendingLeaveCount()));

        // Fee pie
        Map<String, Integer> feeStatus = dao.getFeePaymentStatus();
        int paid   = feeStatus.getOrDefault("Paid", 0);
        int unpaid = feeStatus.getOrDefault("Unpaid", 0);

        if (paid + unpaid == 0) {
            feePieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("No Data", 1)));
        } else {
            feePieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Paid", paid),
                    new PieChart.Data("Unpaid", unpaid)));
            applyPieColor(feePieChart, "Paid", "#16a34a");
            applyPieColor(feePieChart, "Unpaid", "#ef4444");
        }

        lblPaidCount.setText(String.valueOf(paid));
        lblUnpaidCount.setText(String.valueOf(unpaid));

        BigDecimal totalCollected = dao.getTotalCollected();
        lblTotalCollected.setText(MONEY.format(totalCollected) + " MMK");

        // Payment methods pie
        Map<String, Integer> methods = dao.getPaymentMethods();
        if (methods.isEmpty()) {
            methodPieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("No Payments", 1)));
        } else {
            methodPieChart.setData(FXCollections.observableArrayList());
            for (Map.Entry<String, Integer> e : methods.entrySet()) {
                methodPieChart.getData().add(new PieChart.Data(e.getKey(), e.getValue()));
            }
        }
    }

    private void applyPieColor(PieChart chart, String name, String color) {
        for (PieChart.Data d : chart.getData()) {
            if (d.getName().equals(name)) {
                d.nodeProperty().addListener((o, oldN, newN) -> {
                    if (newN != null) newN.setStyle("-fx-pie-color:" + color + ";");
                });
                if (d.getNode() != null) d.getNode().setStyle("-fx-pie-color:" + color + ";");
            }
        }
    }

    // -------------------------------------------------
    //  Welcome toast
    // -------------------------------------------------
    private void showWelcomeToast(String name) {
        if (name == null || name.trim().isEmpty()) name = "Staff";

        welcomeToast.setPrefWidth(420);
        welcomeToast.setMinWidth(420);
        welcomeToast.setMaxWidth(420);
        welcomeToast.setPrefHeight(125);
        welcomeToast.setMinHeight(125);
        welcomeToast.setMaxHeight(125);

        lblToastTitle.setText("LOGIN SUCCESSFUL");
        lblToastHeading.setText("Welcome back!");
        lblDashboardWelcome.setText("Welcome, " + name + ". You are signed in successfully.");

        welcomeToast.setManaged(true);
        welcomeToast.setVisible(true);

        if (welcomeTimer != null) welcomeTimer.stop();
        welcomeTimer = new PauseTransition(Duration.seconds(5));
        welcomeTimer.setOnFinished(e -> {
            welcomeToast.setVisible(false);
            welcomeToast.setManaged(false);
        });
        welcomeTimer.play();
    }

    // -------------------------------------------------
    //  Navigation
    // -------------------------------------------------
    @FXML
    public void refreshDashboard(ActionEvent e) {
        setActiveButton(btnDashboard);
        dashboardScrollPane.setVisible(true);
        staffPane.setVisible(false);
        staffPane.getChildren().clear();
        loadDashboardData();
    }

    @FXML public void openStudents(ActionEvent e)      { openSubView("/com/example/student_management_system/View/Staff/StaffStudents.fxml",   btnStudents); }
    @FXML public void openPayments(ActionEvent e)      { openSubView("/com/example/student_management_system/View/Staff/StaffPayments.fxml",   btnPayments); }
    @FXML public void openLeaveRequests(ActionEvent e) { openSubView("/com/example/student_management_system/View/Staff/StaffLeave.fxml",      btnLeaveRequests); }
    @FXML public void openAttendance(ActionEvent e)    { openSubView("/com/example/student_management_system/View/Staff/StaffAttendance.fxml", btnAttendance); }
    @FXML public void openAnnouncements(ActionEvent e) { openSubView("/com/example/student_management_system/View/Staff/StaffAnnouncements.fxml", btnAnnouncements); }
    @FXML public void openProfile(ActionEvent e)       { openSubView("/com/example/student_management_system/View/Staff/StaffProfile.fxml",    btnProfile); }

    private void openSubView(String fxmlPath, Button btn) {
        setActiveButton(btn);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object ctrl = loader.getController();
            try {
                var m = ctrl.getClass().getMethod("setStaffInfo", StaffInfo.class);
                m.invoke(ctrl, staffInfo);
            } catch (NoSuchMethodException ignored) {
                // controller doesn't need staffInfo
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            staffPane.getChildren().setAll(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

            dashboardScrollPane.setVisible(false);
            staffPane.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setActiveButton(Button selected) {
        for (Button b : List.of(
                btnDashboard, btnStudents, btnPayments, btnLeaveRequests,
                btnAttendance, btnAnnouncements, btnProfile)) {
            if (b != null) b.setStyle(b == selected ? ACTIVE : NORMAL);
        }
    }

    // -------------------------------------------------
    //  Logout
    // -------------------------------------------------
    @FXML
    public void logout(ActionEvent e) {
        Stage stage = null;
        if (e != null && e.getSource() instanceof Node) {
            stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        } else if (btnLogout != null && btnLogout.getScene() != null) {
            stage = (Stage) btnLogout.getScene().getWindow();
        }
        if (stage == null) {
            System.err.println("Logout Error: Target Stage could not be determined.");
            return;
        }
        final Stage currentStage = stage;

        lblToastTitle.setText("LOGOUT SUCCESS");
        lblToastHeading.setText("Signed out");
        lblDashboardWelcome.setText("You have been logged out successfully.");

        welcomeToast.setManaged(true);
        welcomeToast.setVisible(true);
        if (welcomeTimer != null) welcomeTimer.stop();

        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/example/student_management_system/View/Login.fxml"));
                Parent loginRoot = loader.load();
                currentStage.setScene(new Scene(loginRoot));
                currentStage.setMaximized(true);
                currentStage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        delay.play();
    }
}