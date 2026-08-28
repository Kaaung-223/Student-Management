package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.ClassDAO;
import com.example.student_management_system.Controller.DAO.SubjectDAO;
import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.Subject;
import com.example.student_management_system.Controller.Model.SubjectOption;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminSubjectController implements Initializable {

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

        lblCurriculumHint.setText(success
                ? "Saved — " + selected.getName() + " now has " + selectedIds.size() + " subject(s)."
                : "Could not save. Please try again.");
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

        lblAddSubjectError.setVisible(false);
        lblAddSubjectError.setManaged(false);
        txtNewSubjectName.clear();
        txtNewSubjectCode.clear();

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

    /** Builds an Edit + Delete button pair for each row of the subjects table. */
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
                    subjectDAO.deleteSubject(subject.getId());
                    loadSubjectsTable();
                    if (cmbClass.getValue() != null) {
                        loadCurriculumChecklist(); // in case the deleted subject was in the current class's list
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        };
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
                loadSubjectsTable();
                if (cmbClass.getValue() != null) {
                    loadCurriculumChecklist(); // reflect the renamed subject in the checklist
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSubjectsTable() {
        List<Subject> subjects = subjectDAO.getAllSubjectsWithClasses();
        subjectsTable.setItems(FXCollections.observableArrayList(subjects));
        lblSubjectCount.setText(subjects.size() + (subjects.size() == 1 ? " subject" : " subjects"));
    }
}
