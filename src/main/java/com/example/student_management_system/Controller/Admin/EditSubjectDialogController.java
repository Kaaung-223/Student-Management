package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.SubjectDAO;
import com.example.student_management_system.Controller.Model.Subject;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditSubjectDialogController {

    @FXML private TextField txtSubjectName;
    @FXML private TextField txtSubjectCode;
    @FXML private Label lblFormError;
    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    private final SubjectDAO subjectDAO = new SubjectDAO();

    private Stage dialogStage;
    private int subjectId;
    private boolean saved = false;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    /** Pre-fills the form from the given subject. Call after setDialogStage(). */
    public void loadSubject(Subject subject) {
        this.subjectId = subject.getId();
        txtSubjectName.setText(subject.getSubjectName());
        txtSubjectCode.setText(subject.getSubjectCode());
    }

    /** True if the user actually saved changes (vs. cancelling). */
    public boolean wasSaved() {
        return saved;
    }

    @FXML
    private void save() {
        String name = txtSubjectName.getText() != null ? txtSubjectName.getText().trim() : "";
        String code = txtSubjectCode.getText() != null ? txtSubjectCode.getText().trim() : "";

        if (name.isEmpty()) {
            showError("Subject name is required.");
            return;
        }

        boolean success = subjectDAO.updateSubject(subjectId, name, code.isEmpty() ? null : code);
        if (!success) {
            showError("Could not save — the name or code may already be in use.");
            return;
        }

        saved = true;
        dialogStage.close();
    }

    @FXML
    private void cancel() {
        dialogStage.close();
    }

    private void showError(String message) {
        lblFormError.setText(message);
        lblFormError.setVisible(true);
        lblFormError.setManaged(true);
    }
}
