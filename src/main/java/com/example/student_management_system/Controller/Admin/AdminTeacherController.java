package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.TeacherDAO;
import com.example.student_management_system.Controller.Model.Teacher;

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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

public class AdminTeacherController implements Initializable {

    @FXML private AnchorPane rootPane;
    @FXML private TextField txtSearchTeacher;
    @FXML private Label lblTeacherCount;
    @FXML private Button btnAddTeacher;
    @FXML private FlowPane teacherCardContainer;
    @FXML private VBox emptyStateBox;

    private final TeacherDAO teacherDAO = new TeacherDAO();

    private static final NumberFormat SALARY_FORMAT =
            NumberFormat.getNumberInstance(Locale.US);

    private PauseTransition toastTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtSearchTeacher.textProperty()
                .addListener((obs, oldVal, newVal) -> loadTeachers(newVal));

        loadTeachers(null);
    }

    // =========================================================
    // LOAD TEACHERS
    // =========================================================

    private void loadTeachers(String searchQuery) {
        List<Teacher> teachers = teacherDAO.searchTeachers(searchQuery);

        teacherCardContainer.getChildren().clear();

        for (Teacher teacher : teachers) {
            VBox card = buildTeacherCard(teacher);
            teacherCardContainer.getChildren().add(card);
        }

        lblTeacherCount.setText(teachers.size() +
                (teachers.size() == 1 ? " teacher" : " teachers"));

        boolean isEmpty = teachers.isEmpty();
        emptyStateBox.setVisible(isEmpty);
        emptyStateBox.setManaged(isEmpty);
        teacherCardContainer.setVisible(!isEmpty);
        teacherCardContainer.setManaged(!isEmpty);
    }

    // =========================================================
    // BUILD TEACHER CARD
    // =========================================================

    private VBox buildTeacherCard(Teacher teacher) {
        VBox card = new VBox(10);
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        card.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-padding: 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0, 0, 4);");

        // ---- top row: avatar + name/code ----
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Node avatar = buildAvatar(teacher);

        VBox nameBox = new VBox(2);
        Label nameLabel = new Label(valueOrDefault(teacher.getTeacherName(), "Unknown Teacher"));
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-text-fill: #172033; -fx-font-size: 15px; -fx-font-weight: bold;");

        Label codeLabel = new Label("Code: " + valueOrDefault(teacher.getTeacherCode(), "-"));
        codeLabel.setStyle("-fx-text-fill: #8995aa; -fx-font-size: 11px;");

        nameBox.getChildren().addAll(nameLabel, codeLabel);
        topRow.getChildren().addAll(avatar, nameBox);
        card.getChildren().add(topRow);

        // ---- login account ----
        VBox accountBox = new VBox(2);
        Label accountTitle = new Label("LOGIN ACCOUNT");
        accountTitle.setStyle("-fx-text-fill: #8995aa; -fx-font-size: 10px; -fx-font-weight: bold;");

        String username = teacher.getUsername();
        Label usernameLabel = new Label(username == null || username.isBlank()
                ? "Username not available"
                : "@" + username);
        usernameLabel.setStyle("-fx-text-fill: #4f46e5; -fx-font-size: 12px; -fx-font-weight: bold;");

        accountBox.getChildren().addAll(accountTitle, usernameLabel);
        card.getChildren().add(accountBox);

        // ---- badges ----
        HBox badgeRow = new HBox(6);
        boolean active = "ACTIVE".equalsIgnoreCase(teacher.getStatus());

        Label statusBadge = makeBadge(active ? "Active" : "Inactive",
                active ? "#eafaf0" : "#f2f2f2",
                active ? "#1c8a52" : "#666666");
        badgeRow.getChildren().add(statusBadge);

        if (teacher.getClassLeaderOf() != null && !teacher.getClassLeaderOf().isBlank()) {
            Label leaderBadge = makeBadge("Class Leader · " + teacher.getClassLeaderOf(),
                    "#eaf1fd", "#2563eb");
            badgeRow.getChildren().add(leaderBadge);
        }
        card.getChildren().add(badgeRow);

        // ---- subjects ----
        VBox subjectsBox = new VBox(2);
        Label subjectsTitle = new Label("TEACHING");
        subjectsTitle.setStyle("-fx-text-fill: #8995aa; -fx-font-size: 10px; -fx-font-weight: bold;");

        String subjects = teacher.getSubjectsTaught();
        Label subjectsValue = new Label(subjects == null || subjects.isBlank()
                ? "No subjects assigned"
                : subjects);
        subjectsValue.setWrapText(true);
        subjectsValue.setStyle("-fx-text-fill: #344563; -fx-font-size: 12px;");

        subjectsBox.getChildren().addAll(subjectsTitle, subjectsValue);
        card.getChildren().add(subjectsBox);

        // ---- contact ----
        VBox contactBox = new VBox(2);
        Label emailLabel = new Label("✉ " + valueOrDefault(teacher.getEmail(), "-"));
        emailLabel.setStyle("-fx-text-fill: #5b6b85; -fx-font-size: 12px;");

        Label phoneLabel = new Label("☎ " + valueOrDefault(teacher.getPhone(), "-"));
        phoneLabel.setStyle("-fx-text-fill: #5b6b85; -fx-font-size: 12px;");

        contactBox.getChildren().addAll(emailLabel, phoneLabel);
        card.getChildren().add(contactBox);

        // ---- salary ----
        if (teacher.getSalary() != null) {
            Label salaryLabel = new Label("Salary: " + formatSalary(teacher.getSalary()) + " MMK/mo");
            salaryLabel.setStyle("-fx-text-fill: #172033; -fx-font-size: 12px; -fx-font-weight: bold;");
            card.getChildren().add(salaryLabel);
        }

        // ---- action buttons ----
        HBox actionRow = new HBox(8);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Button viewButton = makeCardButton("View", "#f0f2f6", "#344563");
        Button updateButton = makeCardButton("Update", "#4f46e5", "white");
        Button deleteButton = makeCardButton("Delete", "#fee2e2", "#dc2626");

        viewButton.setOnAction(e -> openTeacherDialog(teacher, AddTeacherDialogController.DialogMode.VIEW));
        updateButton.setOnAction(e -> openTeacherDialog(teacher, AddTeacherDialogController.DialogMode.EDIT));
        deleteButton.setOnAction(e -> confirmDeleteTeacher(teacher));

        actionRow.getChildren().addAll(viewButton, updateButton, deleteButton);
        card.getChildren().add(actionRow);

        // ---- click card -> view ----
        card.setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof Node clicked)) return;
            if (isButtonInHierarchy(clicked)) return;
            openTeacherDialog(teacher, AddTeacherDialogController.DialogMode.VIEW);
        });
        card.setStyle(card.getStyle() + "-fx-cursor: hand;");

        return card;
    }

    // =========================================================
    // AVATAR  —  HIGH-QUALITY FULL CIRCLE PHOTO
    // =========================================================

    private Node buildAvatar(Teacher teacher) {
        final double SIZE = 52;
        final double RADIUS = SIZE / 2;

        // Load at 3× the display size so the avatar stays sharp on HiDPI
        // displays and when the window is scaled. At least 2×; on Retina
        // screens we follow the actual OS output scale.
        double dpiScale = 1.0;
        try {
            dpiScale = Screen.getPrimary().getOutputScaleX();
        } catch (Exception ignored) { /* headless / unsupported */ }

        double loadScale = Math.max(2.0, dpiScale) * 1.5;   // ~3× typical
        final double LOAD_SIZE = SIZE * loadScale;

        StackPane avatarWrap = new StackPane();
        avatarWrap.setPrefSize(SIZE, SIZE);
        avatarWrap.setMinSize(SIZE, SIZE);
        avatarWrap.setMaxSize(SIZE, SIZE);
        avatarWrap.setSnapToPixel(true);

        Image image = loadAvatarImage(teacher, LOAD_SIZE);

        if (image != null) {
            ImageView iv = new ImageView(image);

            // Center-crop the source to a square so faces stay in proportion
            double iw = image.getWidth();
            double ih = image.getHeight();
            if (iw > 0 && ih > 0) {
                double side = Math.min(iw, ih);
                iv.setViewport(new Rectangle2D(
                        (iw - side) / 2,
                        (ih - side) / 2,
                        side, side));
            }

            // Display at the avatar's actual size
            iv.setFitWidth(SIZE);
            iv.setFitHeight(SIZE);
            iv.setPreserveRatio(false);
            iv.setSmooth(true);      // bilinear downscale — anti-aliased edges
            iv.setCache(true);       // cache the scaled bitmap

            // Perfect circle clip
            Circle clip = new Circle(RADIUS, RADIUS, RADIUS);
            iv.setClip(clip);

            avatarWrap.getChildren().add(iv);

            // Thin ring for polish — mouse-transparent so it doesn't eat clicks
            Circle ring = new Circle(RADIUS - 0.5);
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(Color.web("#e2e8f0"));
            ring.setStrokeWidth(1);
            ring.setMouseTransparent(true);
            avatarWrap.getChildren().add(ring);

        } else {
            // Fallback: colored circle with initials
            Circle circle = new Circle(RADIUS, deriveColor(teacher.getTeacherName()));
            Label initialsLabel = new Label(initialsOf(teacher.getTeacherName()));
            initialsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
            avatarWrap.getChildren().addAll(circle, initialsLabel);
        }

        return avatarWrap;
    }

    /** Loads the teacher's photo from disk / URL at the requested resolution. */
    private Image loadAvatarImage(Teacher teacher, double size) {
        String photoPath = teacher.getPhotoPath();
        if (photoPath == null || photoPath.isBlank()) return null;

        try {
            // 1) Absolute path on disk
            File f = new File(photoPath);
            if (f.exists() && f.isFile()) {
                return new Image(f.toURI().toString(), size, size, false, true, true);
            }

            // 2) Path relative to the project working directory
            File rel = new File(System.getProperty("user.dir"), photoPath);
            if (rel.exists() && rel.isFile()) {
                return new Image(rel.toURI().toString(), size, size, false, true, true);
            }

            // 3) Remote URL
            if (photoPath.startsWith("http://") || photoPath.startsWith("https://")) {
                return new Image(photoPath, size, size, false, true, true);
            }
        } catch (Exception ignored) {}

        return null;
    }

    private String initialsOf(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++) {
            if (!parts[i].isEmpty())
                sb.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return sb.length() > 0 ? sb.toString() : "?";
    }

    private Color deriveColor(String name) {
        int hash = name != null ? name.hashCode() : 0;
        return Color.hsb(Math.abs(hash % 360), 0.55, 0.75);
    }

    // =========================================================
    // SMALL HELPERS
    // =========================================================

    private boolean isButtonInHierarchy(Node node) {
        while (node != null) {
            if (node instanceof Button) return true;
            node = node.getParent();
        }
        return false;
    }

    private Button makeCardButton(String text, String bg, String fg) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + bg + ";" +
                "-fx-text-fill: " + fg + ";" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 6 12;" +
                "-fx-cursor: hand;");
        return btn;
    }

    private Label makeBadge(String text, String bg, String fg) {
        Label badge = new Label(text);
        badge.setStyle("-fx-background-color: " + bg + ";" +
                "-fx-text-fill: " + fg + ";" +
                "-fx-font-size: 10px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 3 8;");
        return badge;
    }

    private String formatSalary(BigDecimal salary) {
        return SALARY_FORMAT.format(salary);
    }

    private String valueOrDefault(String value, String defaultValue) {
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    // =========================================================
    // OPEN DIALOG
    // =========================================================

    @FXML
    public void openAddTeacherDialog() {
        openTeacherDialog(null, AddTeacherDialogController.DialogMode.ADD);
    }

    private void openTeacherDialog(Teacher teacher,
                                   AddTeacherDialogController.DialogMode mode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Admin/AdminTeacherDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            String title = (mode == AddTeacherDialogController.DialogMode.ADD) ? "Add Teacher"
                    : (mode == AddTeacherDialogController.DialogMode.EDIT) ? "Update Teacher"
                    : "Teacher Details";
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 620, 760);
            dialogStage.setScene(scene);
            dialogStage.setMinWidth(460);
            dialogStage.setMinHeight(600);
            dialogStage.setResizable(true);

            AddTeacherDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setDialogMode(mode);
            if (teacher != null && mode != AddTeacherDialogController.DialogMode.ADD) {
                controller.loadTeacher(teacher);
            }

            dialogStage.showAndWait();

            if (controller.wasSaved()) {
                String msg = (mode == AddTeacherDialogController.DialogMode.ADD)
                        ? "Teacher added successfully."
                        : "Teacher updated successfully.";
                showToast(true, "SUCCESS", "Teacher saved", msg);

                loadTeachers(txtSearchTeacher.getText());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showToast(false, "ERROR", "Dialog error", "Could not open teacher form.");
        }
    }

    // =========================================================
    // DELETE
    // =========================================================

    private void confirmDeleteTeacher(Teacher teacher) {
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Teacher");
        confirm.setHeaderText("Delete " + valueOrDefault(teacher.getTeacherName(), "this teacher") + "?");
        confirm.setContentText("This will permanently remove the teacher profile and login account.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                boolean deleted = teacherDAO.deleteTeacher(teacher.getId());
                if (deleted) {
                    showToast(true, "DELETED", "Teacher removed",
                            teacher.getTeacherName() + " has been deleted.");
                    loadTeachers(txtSearchTeacher.getText());
                } else {
                    showToast(false, "DELETE FAILED", "Could not delete",
                            "An error occurred while deleting the teacher.");
                }
            }
        });
    }

    // =========================================================
    // TOAST
    // =========================================================

    private void showToast(boolean success, String title, String heading, String message) {
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
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 9px; -fx-font-weight: bold;");

        Label headingLabel = new Label(heading);
        headingLabel.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(260);
        messageLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        textBox.getChildren().addAll(titleLabel, headingLabel, messageLabel);
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
        if (toastTimer != null) {
            toastTimer.stop();
            toastTimer = null;
        }
        rootPane.getChildren().removeIf(node ->
                node instanceof VBox && node.getStyle().contains("fx-background-color: white;")
        );
    }
}