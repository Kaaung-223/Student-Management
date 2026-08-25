package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.TeacherDAO;
import com.example.student_management_system.Controller.Model.Teacher;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class AdminTeacherController implements Initializable {

    @FXML private TextField txtSearchTeacher;
    @FXML private Label lblTeacherCount;
    @FXML private Button btnAddTeacher;
    @FXML private FlowPane teacherCardContainer;
    @FXML private VBox emptyStateBox;

    private final TeacherDAO teacherDAO = new TeacherDAO();

    private static final NumberFormat SALARY_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtSearchTeacher.textProperty().addListener((obs, oldVal, newVal) -> loadTeachers(newVal));
        loadTeachers(null);
    }

    private void loadTeachers(String searchQuery) {
        List<Teacher> teachers = teacherDAO.searchTeachers(searchQuery);

        teacherCardContainer.getChildren().clear();
        for (Teacher t : teachers) {
            teacherCardContainer.getChildren().add(buildTeacherCard(t));
        }

        lblTeacherCount.setText(teachers.size() + (teachers.size() == 1 ? " teacher" : " teachers"));

        boolean empty = teachers.isEmpty();
        emptyStateBox.setVisible(empty);
        emptyStateBox.setManaged(empty);
        teacherCardContainer.setVisible(!empty);
        teacherCardContainer.setManaged(!empty);
    }

    /** Builds one photo card for a teacher: avatar, name, code, class-leader badge, subjects, contact, salary. */
    private VBox buildTeacherCard(Teacher t) {
        VBox card = new VBox(10);
        card.setPrefWidth(260);
        card.setMaxWidth(260);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0, 0, 4);");

        // ---- Avatar + name row ----
        HBox topRow = new HBox(12);
        topRow.setStyle("-fx-alignment: CENTER_LEFT;");

        javafx.scene.Node avatar = buildAvatar(t);

        VBox nameBox = new VBox(2);
        Label nameLabel = new Label(t.getTeacherName());
        nameLabel.setStyle("-fx-text-fill: #172033; -fx-font-size: 15px; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);

        Label codeLabel = new Label(t.getTeacherCode() != null ? t.getTeacherCode() : "-");
        codeLabel.setStyle("-fx-text-fill: #8995aa; -fx-font-size: 11px;");

        nameBox.getChildren().addAll(nameLabel, codeLabel);
        topRow.getChildren().addAll(avatar, nameBox);

        card.getChildren().add(topRow);

        // ---- Status + Class Leader badges ----
        HBox badgeRow = new HBox(6);
        badgeRow.getChildren().add(makeBadge(
                "ACTIVE".equalsIgnoreCase(t.getStatus()) ? "Active" : "Inactive",
                "ACTIVE".equalsIgnoreCase(t.getStatus()) ? "#eafaf0" : "#f2f2f2",
                "ACTIVE".equalsIgnoreCase(t.getStatus()) ? "#1c8a52" : "#666"));

        if (t.getClassLeaderOf() != null && !t.getClassLeaderOf().isEmpty()) {
            badgeRow.getChildren().add(makeBadge("Class Leader · " + t.getClassLeaderOf(), "#eaf1fd", "#2563EB"));
        }
        card.getChildren().add(badgeRow);

        // ---- Subjects taught ----
        VBox subjectsBox = new VBox(2);
        Label subjectsHeader = new Label("TEACHING");
        subjectsHeader.setStyle("-fx-text-fill: #8995aa; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label subjectsValue = new Label(
                t.getSubjectsTaught() != null && !t.getSubjectsTaught().isEmpty()
                        ? t.getSubjectsTaught() : "No subjects assigned");
        subjectsValue.setWrapText(true);
        subjectsValue.setStyle("-fx-text-fill: #344563; -fx-font-size: 12px;");
        subjectsBox.getChildren().addAll(subjectsHeader, subjectsValue);
        card.getChildren().add(subjectsBox);

        // ---- Contact ----
        VBox contactBox = new VBox(2);
        Label emailLabel = new Label("✉ " + (t.getEmail() != null ? t.getEmail() : "-"));
        emailLabel.setStyle("-fx-text-fill: #5b6b85; -fx-font-size: 12px;");
        Label phoneLabel = new Label("☎ " + (t.getPhone() != null ? t.getPhone() : "-"));
        phoneLabel.setStyle("-fx-text-fill: #5b6b85; -fx-font-size: 12px;");
        contactBox.getChildren().addAll(emailLabel, phoneLabel);
        card.getChildren().add(contactBox);

        // ---- Salary ----
        if (t.getSalary() != null) {
            Label salaryLabel = new Label("Salary: " + formatSalary(t.getSalary()) + " MMK/mo");
            salaryLabel.setStyle("-fx-text-fill: #172033; -fx-font-size: 12px; -fx-font-weight: bold;");
            card.getChildren().add(salaryLabel);
        }

        return card;
    }

    /** Circular photo if photo_path resolves to a real file, otherwise a colored circle with initials. */
    private javafx.scene.Node buildAvatar(Teacher t) {
        double size = 52;

        if (t.getPhotoPath() != null && !t.getPhotoPath().isEmpty()) {
            File file = new File(t.getPhotoPath());
            if (file.exists()) {
                ImageView iv = new ImageView(new Image(file.toURI().toString(), size, size, true, true));
                Circle clip = new Circle(size / 2, size / 2, size / 2);
                iv.setClip(clip);
                return iv;
            }
        }

        // Fallback: colored circle with initials
        String initials = initialsOf(t.getTeacherName());
        Circle circle = new Circle(size / 2, deriveColor(t.getTeacherName()));
        Label initialsLabel = new Label(initials);
        initialsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        javafx.scene.layout.StackPane avatarStack = new javafx.scene.layout.StackPane(circle, initialsLabel);
        avatarStack.setPrefSize(size, size);
        avatarStack.setMaxSize(size, size);
        return avatarStack;
    }

    private String initialsOf(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++) {
            if (!parts[i].isEmpty()) sb.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return sb.length() > 0 ? sb.toString() : "?";
    }

    /** Deterministic color per teacher, so the same teacher always gets the same avatar color. */
    private Color deriveColor(String name) {
        int hash = name != null ? name.hashCode() : 0;
        double hue = Math.abs(hash % 360);
        return Color.hsb(hue, 0.55, 0.75);
    }

    private Label makeBadge(String text, String bgColor, String fgColor) {
        Label badge = new Label(text);
        badge.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + fgColor + "; " +
                "-fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 3 8;");
        return badge;
    }

    private String formatSalary(BigDecimal salary) {
        return SALARY_FORMAT.format(salary);
    }

    @FXML
    public void openAddTeacherDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Admin/AdminTeacherDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Teacher");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            AddTeacherDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            // Refresh regardless of whether a teacher was actually added — cheap and always correct.
            loadTeachers(txtSearchTeacher.getText());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
