package com.example.student_management_system.Controller.Staff;

import com.example.student_management_system.Controller.DAO.StaffLeaveCreateDAO;
import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.PaymentStudent;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AddLeaveRequestDialogController implements Initializable {

    @FXML private Label lblDialogTitle;
    @FXML private Label lblDialogSubtitle;

    // Student picker
    @FXML private ComboBox<Batch> cmbBatch;
    @FXML private TextField txtSearch;
    @FXML private ListView<PaymentStudent> studentList;

    // Dates
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;
    @FXML private Label lblDatesError;

    // Reason
    @FXML private TextArea txtReason;
    @FXML private Label lblReasonError;

    // Actions
    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    // Live summary
    @FXML private Label lblStudentError;
    @FXML private Label lblDaysBadge;

    private final StaffLeaveCreateDAO dao = new StaffLeaveCreateDAO();

    private Stage dialogStage;
    private boolean saved;
    private PaymentStudent selectedStudent;
    private PauseTransition debounce;

    public void setDialogStage(Stage stage) { this.dialogStage = stage; }
    public boolean wasSaved() { return saved; }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Batch combo
        cmbBatch.getItems().add(new Batch(-1, "All Batches"));
        cmbBatch.getItems().addAll(dao.getAllBatches());
        cmbBatch.getSelectionModel().selectFirst();
        cmbBatch.setOnAction(e -> loadStudents());

        // Student list
        studentList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(PaymentStudent s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setText(null);
                } else {
                    setText(s.getStudentName() + "   ·   " + safe(s.getStudentCode())
                            + "   ·   " + safe(s.getBatchName()));
                    setStyle("-fx-padding:8 12; -fx-font-size:12px;");
                }
            }
        });
        studentList.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> {
                    selectedStudent = b;
                    clear(lblStudentError);
                });

        // Debounced search
        debounce = new PauseTransition(Duration.millis(250));
        debounce.setOnFinished(e -> loadStudents());
        txtSearch.textProperty().addListener((o, a, b) -> debounce.play());

        // Date pickers update the day badge
        dpFrom.valueProperty().addListener((o, a, b) -> updateDaysBadge());
        dpTo.valueProperty().addListener((o, a, b) -> updateDaysBadge());

        // Defaults
        dpFrom.setValue(LocalDate.now());
        dpTo.setValue(LocalDate.now());

        btnSave.setDefaultButton(true);

        loadStudents();
    }

    // =========================================================
    //  STUDENT PICKER
    // =========================================================
    private void loadStudents() {
        Batch b = cmbBatch.getValue();
        List<PaymentStudent> list = dao.findActiveStudents(
                b == null ? -1 : b.getId(),
                txtSearch.getText());

        studentList.setItems(FXCollections.observableArrayList(list));
    }

    private void updateDaysBadge() {
        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();

        if (from == null || to == null || to.isBefore(from)) {
            lblDaysBadge.setText("--");
            return;
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1;
        lblDaysBadge.setText(days + " day(s)");
    }

    // =========================================================
    //  SAVE
    // =========================================================
    @FXML
    private void save() {
        hideAllErrors();

        if (selectedStudent == null) {
            show(lblStudentError, "Please select a student.");
            return;
        }

        LocalDate from = dpFrom.getValue();
        LocalDate to   = dpTo.getValue();

        if (from == null) {
            show(lblDatesError, "Please select a start date.");
            return;
        }
        if (to == null) {
            show(lblDatesError, "Please select an end date.");
            return;
        }
        if (to.isBefore(from)) {
            show(lblDatesError, "End date cannot be before start date.");
            return;
        }

        String reason = txtReason.getText() == null ? "" : txtReason.getText().trim();
        if (reason.isEmpty()) {
            show(lblReasonError, "Please provide a reason.");
            return;
        }
        if (reason.length() > 500) {
            show(lblReasonError, "Reason is too long (max 500 chars).");
            return;
        }

        // Overlap check
        if (dao.hasOverlap(selectedStudent.getStudentId(), from, to)) {
            show(lblDatesError,
                    "This student already has a pending or approved leave in that period.");
            return;
        }

        int newId = dao.addLeaveRequest(
                selectedStudent.getStudentId(), from, to, reason);

        if (newId == -1) {
            show(lblReasonError, "Could not save. Try again later.");
            return;
        }

        saved = true;
        if (dialogStage != null) dialogStage.close();
    }

    @FXML
    private void cancel() {
        if (dialogStage != null) dialogStage.close();
    }

    // =========================================================
    private void hideAllErrors() {
        clear(lblStudentError);
        clear(lblDatesError);
        clear(lblReasonError);
    }

    private void clear(Label l) {
        if (l == null) return;
        l.setText(""); l.setVisible(false); l.setManaged(false);
    }

    private void show(Label l, String msg) {
        if (l == null) return;
        l.setText(msg); l.setVisible(true); l.setManaged(true);
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "--" : v;
    }
}