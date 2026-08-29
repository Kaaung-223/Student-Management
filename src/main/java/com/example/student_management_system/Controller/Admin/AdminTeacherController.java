package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.TeacherDAO;
import com.example.student_management_system.Controller.Model.Teacher;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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

    @FXML
    private TextField txtSearchTeacher;

    @FXML
    private Label lblTeacherCount;

    @FXML
    private Button btnAddTeacher;

    @FXML
    private FlowPane teacherCardContainer;

    @FXML
    private VBox emptyStateBox;

    private final TeacherDAO teacherDAO =
            new TeacherDAO();

    private static final NumberFormat SALARY_FORMAT =
            NumberFormat.getNumberInstance(Locale.US);

    @Override
    public void initialize(
            URL location,
            ResourceBundle resources
    ) {

        /*
         * Search teachers whenever the search text changes.
         */
        txtSearchTeacher.textProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                loadTeachers(newValue)
                );

        /*
         * Load all teachers when the page opens.
         */
        loadTeachers(null);
    }

    private void loadTeachers(String searchQuery) {

        List<Teacher> teachers =
                teacherDAO.searchTeachers(searchQuery);

        teacherCardContainer.getChildren().clear();

        for (Teacher teacher : teachers) {

            VBox teacherCard =
                    buildTeacherCard(teacher);

            teacherCardContainer
                    .getChildren()
                    .add(teacherCard);
        }

        lblTeacherCount.setText(
                teachers.size() +
                        (
                                teachers.size() == 1
                                        ? " teacher"
                                        : " teachers"
                        )
        );

        boolean isEmpty =
                teachers.isEmpty();

        emptyStateBox.setVisible(isEmpty);
        emptyStateBox.setManaged(isEmpty);

        teacherCardContainer.setVisible(!isEmpty);
        teacherCardContainer.setManaged(!isEmpty);
    }

    private VBox buildTeacherCard(Teacher teacher) {

        VBox card =
                new VBox(10);

        card.setPrefWidth(280);
        card.setMaxWidth(280);

        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(" +
                        "gaussian, rgba(0,0,0,0.08), 15, 0, 0, 4);"
        );

        /*
         * Teacher photo, name, and teacher code.
         */
        HBox topRow =
                new HBox(12);

        topRow.setStyle(
                "-fx-alignment: CENTER_LEFT;"
        );

        Node avatar =
                buildAvatar(teacher);

        VBox nameBox =
                new VBox(2);

        Label nameLabel =
                new Label(
                        valueOrDefault(
                                teacher.getTeacherName(),
                                "Unknown Teacher"
                        )
                );

        nameLabel.setWrapText(true);

        nameLabel.setStyle(
                "-fx-text-fill: #172033;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;"
        );

        Label codeLabel =
                new Label(
                        "Code: " +
                                valueOrDefault(
                                        teacher.getTeacherCode(),
                                        "-"
                                )
                );

        codeLabel.setStyle(
                "-fx-text-fill: #8995aa;" +
                        "-fx-font-size: 11px;"
        );

        nameBox.getChildren()
                .addAll(
                        nameLabel,
                        codeLabel
                );

        topRow.getChildren()
                .addAll(
                        avatar,
                        nameBox
                );

        card.getChildren()
                .add(topRow);

        /*
         * Teacher login username.
         *
         * The password is intentionally never shown.
         */
        VBox accountBox =
                new VBox(2);

        Label accountTitle =
                new Label("LOGIN ACCOUNT");

        accountTitle.setStyle(
                "-fx-text-fill: #8995aa;" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;"
        );

        String username =
                teacher.getUsername();

        Label usernameLabel =
                new Label(
                        username == null || username.isBlank()
                                ? "Username not available"
                                : "@" + username
                );

        usernameLabel.setStyle(
                "-fx-text-fill: #4f46e5;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );

        accountBox.getChildren()
                .addAll(
                        accountTitle,
                        usernameLabel
                );

        card.getChildren()
                .add(accountBox);

        /*
         * Status and class leader badges.
         */
        HBox badgeRow =
                new HBox(6);

        boolean active =
                "ACTIVE".equalsIgnoreCase(
                        teacher.getStatus()
                );

        Label statusBadge =
                makeBadge(
                        active
                                ? "Active"
                                : "Inactive",
                        active
                                ? "#eafaf0"
                                : "#f2f2f2",
                        active
                                ? "#1c8a52"
                                : "#666666"
                );

        badgeRow.getChildren()
                .add(statusBadge);

        if (
                teacher.getClassLeaderOf() != null &&
                        !teacher.getClassLeaderOf().isBlank()
        ) {

            Label classLeaderBadge =
                    makeBadge(
                            "Class Leader · " +
                                    teacher.getClassLeaderOf(),
                            "#eaf1fd",
                            "#2563eb"
                    );

            badgeRow.getChildren()
                    .add(classLeaderBadge);
        }

        card.getChildren()
                .add(badgeRow);

        /*
         * Subjects taught.
         */
        VBox subjectsBox =
                new VBox(2);

        Label subjectsTitle =
                new Label("TEACHING");

        subjectsTitle.setStyle(
                "-fx-text-fill: #8995aa;" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;"
        );

        String subjects =
                teacher.getSubjectsTaught();

        Label subjectsValue =
                new Label(
                        subjects == null || subjects.isBlank()
                                ? "No subjects assigned"
                                : subjects
                );

        subjectsValue.setWrapText(true);

        subjectsValue.setStyle(
                "-fx-text-fill: #344563;" +
                        "-fx-font-size: 12px;"
        );

        subjectsBox.getChildren()
                .addAll(
                        subjectsTitle,
                        subjectsValue
                );

        card.getChildren()
                .add(subjectsBox);

        /*
         * Email and phone.
         */
        VBox contactBox =
                new VBox(2);

        Label emailLabel =
                new Label(
                        "✉ " +
                                valueOrDefault(
                                        teacher.getEmail(),
                                        "-"
                                )
                );

        emailLabel.setStyle(
                "-fx-text-fill: #5b6b85;" +
                        "-fx-font-size: 12px;"
        );

        Label phoneLabel =
                new Label(
                        "☎ " +
                                valueOrDefault(
                                        teacher.getPhone(),
                                        "-"
                                )
                );

        phoneLabel.setStyle(
                "-fx-text-fill: #5b6b85;" +
                        "-fx-font-size: 12px;"
        );

        contactBox.getChildren()
                .addAll(
                        emailLabel,
                        phoneLabel
                );

        card.getChildren()
                .add(contactBox);

        /*
         * Salary.
         */
        if (teacher.getSalary() != null) {

            Label salaryLabel =
                    new Label(
                            "Salary: " +
                                    formatSalary(
                                            teacher.getSalary()
                                    ) +
                                    " MMK/mo"
                    );

            salaryLabel.setStyle(
                    "-fx-text-fill: #172033;" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;"
            );

            card.getChildren()
                    .add(salaryLabel);
        }

        return card;
    }

    private Node buildAvatar(Teacher teacher) {

        double size = 52;

        String photoPath =
                teacher.getPhotoPath();

        /*
         * Show uploaded teacher photo.
         */
        if (
                photoPath != null &&
                        !photoPath.isBlank()
        ) {

            File imageFile =
                    new File(photoPath);

            if (imageFile.exists()) {

                ImageView imageView =
                        new ImageView(
                                new Image(
                                        imageFile.toURI().toString(),
                                        size,
                                        size,
                                        true,
                                        true
                                )
                        );

                Circle clip =
                        new Circle(
                                size / 2,
                                size / 2,
                                size / 2
                        );

                imageView.setClip(clip);

                return imageView;
            }
        }

        /*
         * If no photo exists, display initials.
         */
        String initials =
                initialsOf(
                        teacher.getTeacherName()
                );

        Circle circle =
                new Circle(
                        size / 2,
                        deriveColor(
                                teacher.getTeacherName()
                        )
                );

        Label initialsLabel =
                new Label(initials);

        initialsLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;"
        );

        StackPane avatar =
                new StackPane(
                        circle,
                        initialsLabel
                );

        avatar.setPrefSize(
                size,
                size
        );

        avatar.setMaxSize(
                size,
                size
        );

        return avatar;
    }

    private String initialsOf(String name) {

        if (name == null || name.isBlank()) {
            return "?";
        }

        String[] parts =
                name.trim()
                        .split("\\s+");

        StringBuilder initials =
                new StringBuilder();

        for (
                int i = 0;
                i < Math.min(2, parts.length);
                i++
        ) {

            if (!parts[i].isEmpty()) {

                initials.append(
                        Character.toUpperCase(
                                parts[i].charAt(0)
                        )
                );
            }
        }

        return initials.length() > 0
                ? initials.toString()
                : "?";
    }

    private Color deriveColor(String name) {

        int hash =
                name != null
                        ? name.hashCode()
                        : 0;

        double hue =
                Math.abs(hash % 360);

        return Color.hsb(
                hue,
                0.55,
                0.75
        );
    }

    private Label makeBadge(
            String text,
            String backgroundColor,
            String foregroundColor
    ) {

        Label badge =
                new Label(text);

        badge.setStyle(
                "-fx-background-color: " +
                        backgroundColor +
                        ";" +
                        "-fx-text-fill: " +
                        foregroundColor +
                        ";" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 3 8;"
        );

        return badge;
    }

    private String formatSalary(
            BigDecimal salary
    ) {

        return SALARY_FORMAT.format(salary);
    }

    private String valueOrDefault(
            String value,
            String defaultValue
    ) {

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }

    @FXML
    public void openAddTeacherDialog() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/com/example/" +
                                            "student_management_system/" +
                                            "View/Admin/" +
                                            "AdminTeacherDialog.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Stage dialogStage =
                    new Stage();

            dialogStage.setTitle(
                    "Add Teacher"
            );

            dialogStage.initModality(
                    Modality.APPLICATION_MODAL
            );

            dialogStage.setScene(
                    new Scene(root)
            );

            dialogStage.setResizable(false);

            AddTeacherDialogController controller =
                    loader.getController();

            controller.setDialogStage(
                    dialogStage
            );

            dialogStage.showAndWait();

            /*
             * Refresh the teacher cards after
             * the dialog closes.
             */
            loadTeachers(
                    txtSearchTeacher.getText()
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}