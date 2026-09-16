package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.StaffDAO;
import com.example.student_management_system.Controller.Model.Staff;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class AdminStaffController implements Initializable {

    @FXML private AnchorPane rootPane;
    @FXML private TextField txtSearchStaff;
    @FXML private Label lblStaffCount;
    @FXML private Button btnAddStaff;
    @FXML private FlowPane staffCardContainer;
    @FXML private VBox emptyStateBox;

    private final StaffDAO staffDAO = new StaffDAO();

    private static final NumberFormat SALARY_FORMAT =
            NumberFormat.getNumberInstance(Locale.US);

    private PauseTransition toastTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtSearchStaff.textProperty()
                .addListener((o, oldV, newV) -> loadStaff(newV));
        loadStaff(null);
    }

    // =========================================================
    // LOAD
    // =========================================================
    private void loadStaff(String query) {
        List<Staff> staff = staffDAO.searchStaff(query);
        staffCardContainer.getChildren().clear();

        for (Staff s : staff) {
            staffCardContainer.getChildren().add(buildStaffCard(s));
        }

        lblStaffCount.setText(staff.size() + (staff.size() == 1 ? " member" : " members"));

        boolean empty = staff.isEmpty();
        emptyStateBox.setVisible(empty);
        emptyStateBox.setManaged(empty);
        staffCardContainer.setVisible(!empty);
        staffCardContainer.setManaged(!empty);
    }

    // =========================================================
    // BUILD CARD
    // =========================================================
    private VBox buildStaffCard(Staff s) {
        VBox card = new VBox(10);
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        card.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-padding: 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0, 0, 4);");

        // ---- avatar + name/code ----
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Node avatar = buildAvatar(s);

        VBox nameBox = new VBox(2);
        Label nameLabel = new Label(valueOrDefault(s.getStaffName(), "Unknown Staff"));
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-text-fill: #172033; -fx-font-size: 15px; -fx-font-weight: bold;");

        Label codeLabel = new Label("Code: " + valueOrDefault(s.getStaffCode(), "-"));
        codeLabel.setStyle("-fx-text-fill: #8995aa; -fx-font-size: 11px;");

        nameBox.getChildren().addAll(nameLabel, codeLabel);
        topRow.getChildren().addAll(avatar, nameBox);
        card.getChildren().add(topRow);

        // ---- login account ----
        VBox accountBox = new VBox(2);
        Label accountTitle = new Label("LOGIN ACCOUNT");
        accountTitle.setStyle("-fx-text-fill: #8995aa; -fx-font-size: 10px; -fx-font-weight: bold;");

        String username = s.getUsername();
        Label usernameLabel = new Label(username == null || username.isBlank()
                ? "Username not available"
                : "@" + username);
        usernameLabel.setStyle("-fx-text-fill: #4f46e5; -fx-font-size: 12px; -fx-font-weight: bold;");

        accountBox.getChildren().addAll(accountTitle, usernameLabel);
        card.getChildren().add(accountBox);

        // ---- badges ----
        HBox badgeRow = new HBox(6);
        boolean active = "ACTIVE".equalsIgnoreCase(s.getStatus());
        badgeRow.getChildren().add(makeBadge(active ? "Active" : "Inactive",
                active ? "#eafaf0" : "#f2f2f2",
                active ? "#1c8a52" : "#666666"));
        card.getChildren().add(badgeRow);

        // ---- contact ----
        VBox contactBox = new VBox(2);
        Label emailLabel = new Label("✉ " + valueOrDefault(s.getEmail(), "-"));
        emailLabel.setStyle("-fx-text-fill: #5b6b85; -fx-font-size: 12px;");

        Label phoneLabel = new Label("☎ " + valueOrDefault(s.getPhone(), "-"));
        phoneLabel.setStyle("-fx-text-fill: #5b6b85; -fx-font-size: 12px;");

        contactBox.getChildren().addAll(emailLabel, phoneLabel);
        card.getChildren().add(contactBox);

        // ---- ✅ salary ----
        if (s.getSalary() != null) {
            Label salaryLabel = new Label("Salary: " + SALARY_FORMAT.format(s.getSalary()) + " MMK/mo");
            salaryLabel.setStyle("-fx-text-fill: #172033; -fx-font-size: 12px; -fx-font-weight: bold;");
            card.getChildren().add(salaryLabel);
        }

        // ---- hire date ----
        if (s.getHireDate() != null) {
            Label hireLabel = new Label("Hired: " + s.getHireDate());
            hireLabel.setStyle("-fx-text-fill: #5b6b85; -fx-font-size: 11px;");
            card.getChildren().add(hireLabel);
        }

        // ---- actions ----
        HBox actionRow = new HBox(8);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Button viewBtn   = makeCardButton("View",   "#f0f2f6", "#344563");
        Button updateBtn = makeCardButton("Update", "#4f46e5", "white");
        Button deleteBtn = makeCardButton("Delete", "#fee2e2", "#dc2626");

        viewBtn.setOnAction(e -> openStaffDialog(s, AddStaffDialogController.DialogMode.VIEW));
        updateBtn.setOnAction(e -> openStaffDialog(s, AddStaffDialogController.DialogMode.EDIT));
        deleteBtn.setOnAction(e -> confirmDelete(s));

        actionRow.getChildren().addAll(viewBtn, updateBtn, deleteBtn);
        card.getChildren().add(actionRow);

        card.setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof Node clicked)) return;
            if (isButtonInHierarchy(clicked)) return;
            openStaffDialog(s, AddStaffDialogController.DialogMode.VIEW);
        });
        card.setStyle(card.getStyle() + "-fx-cursor: hand;");

        return card;
    }

    // =========================================================
    // AVATAR — photo circle (with initials fallback)
    // =========================================================
    private Node buildAvatar(Staff s) {
        final double SIZE = 52;
        final double RADIUS = SIZE / 2;

        double dpiScale = 1.0;
        try { dpiScale = Screen.getPrimary().getOutputScaleX(); } catch (Exception ignored) {}
        double loadScale = Math.max(2.0, dpiScale) * 1.5;
        final double LOAD_SIZE = SIZE * loadScale;

        StackPane wrap = new StackPane();
        wrap.setPrefSize(SIZE, SIZE);
        wrap.setMinSize(SIZE, SIZE);
        wrap.setMaxSize(SIZE, SIZE);
        wrap.setSnapToPixel(true);

        Image image = loadAvatarImage(s, LOAD_SIZE);

        if (image != null) {
            ImageView iv = new ImageView(image);

            double iw = image.getWidth();
            double ih = image.getHeight();
            if (iw > 0 && ih > 0) {
                double side = Math.min(iw, ih);
                iv.setViewport(new Rectangle2D((iw - side) / 2, (ih - side) / 2, side, side));
            }

            iv.setFitWidth(SIZE);
            iv.setFitHeight(SIZE);
            iv.setPreserveRatio(false);
            iv.setSmooth(true);
            iv.setCache(true);

            Circle clip = new Circle(RADIUS, RADIUS, RADIUS);
            iv.setClip(clip);

            wrap.getChildren().add(iv);

            Circle ring = new Circle(RADIUS - 0.5);
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(Color.web("#e2e8f0"));
            ring.setStrokeWidth(1);
            ring.setMouseTransparent(true);
            wrap.getChildren().add(ring);

        } else {
            Circle circle = new Circle(RADIUS, deriveColor(s.getStaffName()));
            Label initials = new Label(initialsOf(s.getStaffName()));
            initials.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
            wrap.getChildren().addAll(circle, initials);
        }

        return wrap;
    }

    private Image loadAvatarImage(Staff s, double size) {
        String photoPath = s.getPhotoPath();
        if (photoPath == null || photoPath.isBlank()) return null;

        try {
            File f = new File(photoPath);
            if (f.exists() && f.isFile())
                return new Image(f.toURI().toString(), size, size, false, true, true);

            File rel = new File(System.getProperty("user.dir"), photoPath);
            if (rel.exists() && rel.isFile())
                return new Image(rel.toURI().toString(), size, size, false, true, true);

            if (photoPath.startsWith("http://") || photoPath.startsWith("https://"))
                return new Image(photoPath, size, size, false, true, true);
        } catch (Exception ignored) {}

        return null;
    }

    private String initialsOf(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++)
            if (!parts[i].isEmpty()) sb.append(Character.toUpperCase(parts[i].charAt(0)));
        return sb.length() > 0 ? sb.toString() : "?";
    }

    private Color deriveColor(String name) {
        int hash = name != null ? name.hashCode() : 0;
        return Color.hsb(Math.abs(hash % 360), 0.55, 0.75);
    }

    // =========================================================
    // HELPERS
    // =========================================================
    private boolean isButtonInHierarchy(Node n) {
        while (n != null) {
            if (n instanceof Button) return true;
            n = n.getParent();
        }
        return false;
    }

    private Button makeCardButton(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + bg + ";" +
                "-fx-text-fill: " + fg + ";" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 6 12;" +
                "-fx-cursor: hand;");
        return b;
    }

    private Label makeBadge(String text, String bg, String fg) {
        Label l = new Label(text);
        l.setStyle("-fx-background-color: " + bg + ";" +
                "-fx-text-fill: " + fg + ";" +
                "-fx-font-size: 10px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 3 8;");
        return l;
    }

    private String valueOrDefault(String v, String d) {
        return (v == null || v.isBlank()) ? d : v;
    }

    // =========================================================
    // DIALOG
    // =========================================================
    @FXML
    public void openAddStaffDialog() {
        openStaffDialog(null, AddStaffDialogController.DialogMode.ADD);
    }

    private void openStaffDialog(Staff staff, AddStaffDialogController.DialogMode mode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Admin/AdminStaffDialog.fxml"));
            Parent root = loader.load();

            Stage dlg = new Stage();
            String title = (mode == AddStaffDialogController.DialogMode.ADD) ? "Add Staff"
                    : (mode == AddStaffDialogController.DialogMode.EDIT) ? "Update Staff"
                    : "Staff Details";
            dlg.setTitle(title);
            dlg.initModality(Modality.APPLICATION_MODAL);
            dlg.setScene(new Scene(root, 620, 780));
            dlg.setMinWidth(460);
            dlg.setMinHeight(620);
            dlg.setResizable(true);

            AddStaffDialogController ctrl = loader.getController();
            ctrl.setDialogStage(dlg);
            ctrl.setDialogMode(mode);
            if (staff != null && mode != AddStaffDialogController.DialogMode.ADD) {
                ctrl.loadStaff(staff);
            }

            dlg.showAndWait();

            if (ctrl.wasSaved()) {
                String msg = (mode == AddStaffDialogController.DialogMode.ADD)
                        ? "Staff member added successfully."
                        : "Staff member updated successfully.";
                showToast(true, "SUCCESS", "Staff saved", msg);
                loadStaff(txtSearchStaff.getText());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showToast(false, "ERROR", "Dialog error", "Could not open staff form.");
        }
    }

    // =========================================================
    // DELETE
    // =========================================================
    private void confirmDelete(Staff s) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Staff");
        confirm.setHeaderText("Delete " + valueOrDefault(s.getStaffName(), "this staff member") + "?");
        confirm.setContentText("This will permanently remove the staff profile and login account.");

        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                boolean ok = staffDAO.deleteStaff(s.getId(), s.getUserId());
                if (ok) {
                    showToast(true, "DELETED", "Staff removed",
                            s.getStaffName() + " has been deleted.");
                    loadStaff(txtSearchStaff.getText());
                } else {
                    showToast(false, "DELETE FAILED", "Could not delete",
                            "An error occurred while deleting the staff member.");
                }
            }
        });
    }

    // =========================================================
    // TOAST
    // =========================================================
    private void showToast(boolean success, String title, String heading, String msg) {
        hideToast();

        VBox toast = new VBox(8);
        toast.setMaxWidth(350);
        toast.setPrefWidth(350);
        toast.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 15;" +
                "-fx-padding: 15 17 15 15;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,.20), 22, 0, 0, 6);");

        HBox topBox = new HBox(12);
        topBox.setAlignment(Pos.CENTER_LEFT);

        StackPane iconWrap = new StackPane();
        iconWrap.setMinSize(38, 38);
        iconWrap.setMaxSize(38, 38);
        iconWrap.setStyle(success
                ? "-fx-background-color: #dcfce7; -fx-background-radius: 19;"
                : "-fx-background-color: #fee2e2; -fx-background-radius: 19;");

        Label iconLabel = new Label(success ? "✓" : "✕");
        iconLabel.setStyle(success
                ? "-fx-text-fill: #16a34a; -fx-font-size: 20px; -fx-font-weight: bold;"
                : "-fx-text-fill: #dc2626; -fx-font-size: 18px; -fx-font-weight: bold;");
        iconWrap.getChildren().add(iconLabel);

        VBox textBox = new VBox(3);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 9px; -fx-font-weight: bold;");
        Label headLbl = new Label(heading);
        headLbl.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 14px; -fx-font-weight: bold;");
        Label msgLbl = new Label(msg);
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(260);
        msgLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        textBox.getChildren().addAll(titleLbl, headLbl, msgLbl);

        topBox.getChildren().addAll(iconWrap, textBox);

        Region accent = new Region();
        accent.setMinHeight(3);
        accent.setMaxHeight(3);
        accent.setStyle(success
                ? "-fx-background-color: #16a34a; -fx-background-radius: 3;"
                : "-fx-background-color: #ef4444; -fx-background-radius: 3;");

        toast.getChildren().addAll(topBox, accent);

        StackPane.setAlignment(toast, Pos.TOP_RIGHT);
        StackPane.setMargin(toast, new Insets(24, 24, 0, 0));

        rootPane.getChildren().add(toast);

        toastTimer = new PauseTransition(Duration.seconds(3));
        toastTimer.setOnFinished(e -> rootPane.getChildren().remove(toast));
        toastTimer.play();
    }

    private void hideToast() {
        if (toastTimer != null) { toastTimer.stop(); toastTimer = null; }
        rootPane.getChildren().removeIf(n ->
                n instanceof VBox && n.getStyle().contains("fx-background-color: white;"));
    }
}