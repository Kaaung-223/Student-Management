package com.example.student_management_system.Controller.Staff;

import com.example.student_management_system.Controller.DAO.StaffDashboardDAO;
import com.example.student_management_system.Controller.DAO.StaffDashboardDAO.AnnouncementNotice;
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
            btnAnnouncements, btnProfile, btnLogout;

    // Center content
    @FXML private StackPane contentPane;
    @FXML private ScrollPane dashboardScrollPane;
    @FXML private AnchorPane staffPane;

    // Welcome toast
    @FXML private VBox welcomeToast;
    @FXML private Label lblToastTitle, lblToastHeading, lblDashboardWelcome;

    // Announcement toast
    @FXML private VBox announcementToast;
    @FXML private Label lblAnnToastTitle, lblAnnToastHeading, lblAnnToastMessage;

    // Header
    @FXML private ImageView staffPhoto;
    @FXML private Label lblStaffName;

    // Stat cards
    @FXML private Label lblTotalStudents, lblTotalClasses,
            lblCollectedThisMonth, lblPendingLeaves;

    // Charts
    @FXML private PieChart feePieChart;
    @FXML private PieChart methodPieChart;

    // Finance labels
    @FXML private Label lblPaidCount, lblUnpaidCount, lblTotalCollected;

    // DAO + session
    private final StaffDashboardDAO dao = new StaffDashboardDAO();
    private StaffInfo staffInfo;

    private PauseTransition welcomeTimer;
    private PauseTransition announcementTimer;

    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(Locale.US);

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

    // =========================================================
    //  PHOTO
    // =========================================================
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

    // =========================================================
    //  DASHBOARD DATA
    // =========================================================
    private void loadDashboardData() {
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

        BigDecimal total = dao.getTotalCollected();
        lblTotalCollected.setText(MONEY.format(total) + " MMK");

        // Methods pie
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

    // =========================================================
    //  WELCOME TOAST → chains ANNOUNCEMENT TOAST
    // =========================================================
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
            showAnnouncementToastIfAny();
        });

        welcomeTimer.play();
    }

    private void showAnnouncementToastIfAny() {
        if (announcementToast == null || staffInfo == null) return;

        AnnouncementNotice notice = dao.getUnreadAnnouncementNotice(staffInfo.getUserId());
        if (notice == null || notice.getCount() == 0) return;

        announcementToast.setPrefWidth(420);
        announcementToast.setMinWidth(420);
        announcementToast.setMaxWidth(420);
        announcementToast.setPrefHeight(125);
        announcementToast.setMinHeight(125);
        announcementToast.setMaxHeight(125);

        String heading = (notice.getCount() == 1)
                ? "You have 1 new announcement"
                : "You have " + notice.getCount() + " new announcements";

        lblAnnToastTitle.setText("NEW ANNOUNCEMENT");
        lblAnnToastHeading.setText(heading);
        lblAnnToastMessage.setText(notice.getTitle());

        announcementToast.setManaged(true);
        announcementToast.setVisible(true);

        if (announcementTimer != null) announcementTimer.stop();
        announcementTimer = new PauseTransition(Duration.seconds(5));
        announcementTimer.setOnFinished(e -> {
            announcementToast.setVisible(false);
            announcementToast.setManaged(false);
        });
        announcementTimer.play();
    }

    // =========================================================
    //  NAVIGATION
    // =========================================================
    @FXML
    public void refreshDashboard(ActionEvent e) {
        setActiveButton(btnDashboard);
        dashboardScrollPane.setVisible(true);
        staffPane.setVisible(false);
        staffPane.getChildren().clear();
        loadDashboardData();
    }

    @FXML public void openStudents(ActionEvent e) {
        setActiveButton(btnStudents);
        openSubView("/com/example/student_management_system/View/Staff/StaffStudent.fxml");
    }
    @FXML public void openPayments(ActionEvent e) {
        setActiveButton(btnPayments);
        openSubView("/com/example/student_management_system/View/Staff/StaffPayment.fxml");
    }
    @FXML public void openLeaveRequests(ActionEvent e) {
        setActiveButton(btnLeaveRequests);
        openSubView("/com/example/student_management_system/View/Staff/StaffLeave.fxml");
    }

    @FXML public void openAnnouncements(ActionEvent e) {
        setActiveButton(btnAnnouncements);
        openSubView("/com/example/student_management_system/View/Staff/StaffAnnouncement.fxml");
    }
    @FXML public void openProfile(ActionEvent e) {
        setActiveButton(btnProfile);
        openSubView("/com/example/student_management_system/View/Staff/StaffProfile.fxml");
    }

    private void openSubView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object ctrl = loader.getController();
            if (ctrl != null && staffInfo != null) {
                try {
                    var m = ctrl.getClass().getMethod("setStaffInfo", StaffInfo.class);
                    m.invoke(ctrl, staffInfo);
                } catch (NoSuchMethodException ignored) {
                    try {
                        var m = ctrl.getClass().getMethod("setStaffInfo", Object.class);
                        m.invoke(ctrl, staffInfo);
                    } catch (Exception ignored2) { /* fine */ }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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
                 btnAnnouncements, btnProfile)) {
            if (b != null) b.setStyle(b == selected ? ACTIVE : NORMAL);
        }
    }

    // =========================================================
    //  LOGOUT
    // =========================================================
    @FXML
    public void logout(ActionEvent e) {
        Stage stage = null;
        if (e != null && e.getSource() instanceof Node) {
            stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        } else if (btnLogout != null && btnLogout.getScene() != null) {
            stage = (Stage) btnLogout.getScene().getWindow();
        }
        if (stage == null) return;

        final Stage currentStage = stage;

        lblToastTitle.setText("LOGOUT SUCCESS");
        lblToastHeading.setText("Signed out");
        lblDashboardWelcome.setText("You have been logged out successfully.");

        welcomeToast.setManaged(true);
        welcomeToast.setVisible(true);
        if (welcomeTimer != null) welcomeTimer.stop();
        if (announcementTimer != null) announcementTimer.stop();

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