package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.ClassDAO;
import com.example.student_management_system.Controller.DAO.SubjectDAO;
import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.Subject;
import com.example.student_management_system.Controller.Model.SubjectOption;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminSubjectController implements Initializable {

    @FXML private AnchorPane rootPane;   // root for toast overlay

    @FXML private ComboBox<Batch> cmbClass;
    @FXML private VBox curriculumChecklistBox;
    @FXML private Label lblCurriculumHint;
    @FXML private Button btnSaveCurriculum;

    @FXML private TextField txtNewSubjectName;
    @FXML private TextField txtNewSubjectCode;
    @FXML private Label lblAddSubjectError;
    @FXML private Button btnAddSubject;

    @FXML private Label lblSubjectCount;
    @FXML private TableView<Subject> subjectsTable;
    @FXML private TableColumn<Subject, String> subjectCodeColumn;
    @FXML private TableColumn<Subject, String> subjectNameColumn;
    @FXML private TableColumn<Subject, String> subjectClassesColumn;
    @FXML private TableColumn<Subject, Void> subjectActionColumn;

    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final ClassDAO classDAO = new ClassDAO();

    private List<SubjectOption> currentCurriculumOptions;

    private static final String DEFAULT_CURRICULUM_HINT = "Select a class above to edit its subjects.";

    private PauseTransition toastTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbClass.setItems(FXCollections.observableArrayList(classDAO.getAllBatches()));
        cmbClass.setOnAction(e -> loadCurriculumChecklist());

        setupSubjectsTable();
        loadSubjectsTable();
    }

    // ===================================================================
    // CLASS CURRICULUM
    // ===================================================================

    private void loadCurriculumChecklist() {
        Batch selected = cmbClass.getValue();
        curriculumChecklistBox.getChildren().clear();

        if (selected == null) {
            btnSaveCurriculum.setDisable(true);
            lblCurriculumHint.setText(DEFAULT_CURRICULUM_HINT);
            return;
        }

        currentCurriculumOptions = subjectDAO.getAllSubjects();
        List<Integer> assignedIds = subjectDAO.getSubjectIdsForClass(selected.getId());

        if (currentCurriculumOptions.isEmpty()) {
            lblCurriculumHint.setText("No subjects exist yet — add one on the right first.");
            btnSaveCurriculum.setDisable(true);
            return;
        }

        for (SubjectOption option : currentCurriculumOptions) {
            CheckBox cb = new CheckBox(option.getSubjectName());
            cb.setStyle("-fx-font-size: 12px; -fx-text-fill: #344563;");
            if (assignedIds.contains(option.getSubjectId())) {
                option.setSelected(true);
            }
            cb.selectedProperty().bindBidirectional(option.selectedProperty());
            curriculumChecklistBox.getChildren().add(cb);
        }

        lblCurriculumHint.setText("Editing curriculum for: " + selected.getName());
        btnSaveCurriculum.setDisable(false);
    }

    @FXML
    private void saveCurriculum() {
        Batch selected = cmbClass.getValue();
        if (selected == null || currentCurriculumOptions == null) return;

        List<Integer> selectedIds = currentCurriculumOptions.stream()
                .filter(SubjectOption::isSelected)
                .map(SubjectOption::getSubjectId)
                .toList();

        boolean success = subjectDAO.saveClassSubjects(selected.getId(), selectedIds);

        loadSubjectsTable(); // "Used In" column may have changed

        // Auto-clear: reset the whole curriculum panel back to its empty state after saving,
        // so the next class the admin picks starts from a clean slate rather than showing
        // the previous class's checkmarks for a moment.
        curriculumChecklistBox.getChildren().clear();
        currentCurriculumOptions = null;
        cmbClass.getSelectionModel().clearSelection();
        cmbClass.setValue(null);
        btnSaveCurriculum.setDisable(true);

        if (success) {
            showToast(true, "CURRICULUM SAVED",
                    selected.getName() + " updated",
                    "Now has " + selectedIds.size() + " subject(s).");
            lblCurriculumHint.setText("Saved — " + selected.getName() + " now has " + selectedIds.size() + " subject(s).");
        } else {
            showToast(false, "SAVE FAILED", "Could not save",
                    "Please check the database connection.");
            lblCurriculumHint.setText("Could not save. Please try again.");
        }
    }

    // ===================================================================
    // ADD SUBJECT
    // ===================================================================

    @FXML
    private void addSubject() {
        String name = txtNewSubjectName.getText() != null ? txtNewSubjectName.getText().trim() : "";
        String code = txtNewSubjectCode.getText() != null ? txtNewSubjectCode.getText().trim() : "";

        if (name.isEmpty()) {
            showAddSubjectError("Subject name is required.");
            return;
        }

        int newId = subjectDAO.createSubject(name, code.isEmpty() ? null : code);
        if (newId == -1) {
            showAddSubjectError("Could not add this subject — the name or code may already be in use.");
            return;
        }

        // Clear error and fields
        lblAddSubjectError.setVisible(false);
        lblAddSubjectError.setManaged(false);
        txtNewSubjectName.clear();
        txtNewSubjectCode.clear();

        showToast(true, "SUBJECT ADDED", name + " created",
                "You can now assign it to classes and teachers.");

        loadSubjectsTable();
        if (cmbClass.getValue() != null) {
            loadCurriculumChecklist(); // new subject should appear (unchecked) if a class is selected
        }
    }

    private void showAddSubjectError(String message) {
        lblAddSubjectError.setText(message);
        lblAddSubjectError.setVisible(true);
        lblAddSubjectError.setManaged(true);
    }

    // ===================================================================
    // ALL SUBJECTS TABLE
    // ===================================================================

    private void setupSubjectsTable() {
        subjectCodeColumn.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));
        subjectNameColumn.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        subjectClassesColumn.setCellValueFactory(cellData -> {
            String classes = cellData.getValue().getClassesUsingIt();
            return new javafx.beans.property.SimpleStringProperty(
                    classes != null && !classes.isEmpty() ? classes : "Not assigned to any class");
        });

        subjectActionColumn.setCellFactory(makeActionButtonsCellFactory());
    }

    private Callback<TableColumn<Subject, Void>, TableCell<Subject, Void>> makeActionButtonsCellFactory() {
        return column -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(6, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color:#4f46e5;" +
                        "-fx-text-fill:white;" +
                        "-fx-background-radius:6;" +
                        "-fx-cursor:hand;");
                deleteButton.setStyle("-fx-background-color:#dc2626;" +
                        "-fx-text-fill:white;" +
                        "-fx-background-radius:6;" +
                        "-fx-cursor:hand;");

                editButton.setOnAction(e -> {
                    Subject subject = getTableView().getItems().get(getIndex());
                    openEditSubjectDialog(subject);
                });

                deleteButton.setOnAction(e -> {
                    Subject subject = getTableView().getItems().get(getIndex());
                    confirmDeleteSubject(subject);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        };
    }

    private void confirmDeleteSubject(Subject subject) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Subject");
        confirm.setHeaderText("Delete " + subject.getSubjectName() + "?");
        confirm.setContentText("This will remove the subject from all classes and teachers.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        boolean deleted = subjectDAO.deleteSubject(subject.getId());
        if (deleted) {
            showToast(true, "DELETED", "Subject removed",
                    subject.getSubjectName() + " has been deleted.");
            loadSubjectsTable();
            if (cmbClass.getValue() != null) {
                loadCurriculumChecklist(); // in case the deleted subject was in the current class's list
            }
        } else {
            showToast(false, "DELETE FAILED", "Could not delete",
                    "This subject may still be referenced by classes or teachers.");
        }
    }

    private void openEditSubjectDialog(Subject subject) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Admin/EditSubjectDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Subject");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            EditSubjectDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.loadSubject(subject);

            dialogStage.showAndWait();

            if (controller.wasSaved()) {
                showToast(true, "SUBJECT UPDATED", subject.getSubjectName() + " updated",
                        "Changes have been saved.");
                loadSubjectsTable();
                if (cmbClass.getValue() != null) {
                    loadCurriculumChecklist(); // reflect the renamed subject in the checklist
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showToast(false, "ERROR", "Could not open dialog",
                    "An error occurred while trying to edit the subject.");
        }
    }

    private void loadSubjectsTable() {
        List<Subject> subjects = subjectDAO.getAllSubjectsWithClasses();
        subjectsTable.setItems(FXCollections.observableArrayList(subjects));
        lblSubjectCount.setText(subjects.size() + (subjects.size() == 1 ? " subject" : " subjects"));
    }

    // ===================================================================
    // TOAST OVERLAY (styled like login toast)
    // ===================================================================

    private void showToast(boolean success, String title, String heading, String message) {
        // Remove any existing toast
        hideToast();

        // Build toast container
        VBox toast = new VBox(8);
        toast.setMaxWidth(350);
        toast.setPrefWidth(350);
        toast.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 15;" +
                "-fx-padding: 15 17 15 15;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,.20), 22, 0, 0, 6);");

        // Icon area
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

        // Text block
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

        // Accent line
        Region accent = new Region();
        accent.setMinHeight(3);
        accent.setMaxHeight(3);
        accent.setStyle(success
                ? "-fx-background-color: #16a34a; -fx-background-radius: 3;"
                : "-fx-background-color: #ef4444; -fx-background-radius: 3;");

        toast.getChildren().addAll(topBox, accent);

        // Position at top-right of the root pane
        StackPane.setAlignment(toast, Pos.TOP_RIGHT);
        StackPane.setMargin(toast, new Insets(24, 24, 0, 0));

        // Add to root
        rootPane.getChildren().add(toast);

        // Auto-hide after 3 seconds
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