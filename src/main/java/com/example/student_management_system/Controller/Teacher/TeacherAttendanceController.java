package com.example.student_management_system.Controller.Teacher;

import com.example.student_management_system.Controller.DAO.TeacherAttendanceDAO;
import com.example.student_management_system.Controller.Model.AttendanceRow;
import com.example.student_management_system.Controller.Model.BatchFilter;
import com.example.student_management_system.Controller.Model.TeacherInfo;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class TeacherAttendanceController implements Initializable {

    // Filter bar
    @FXML private ComboBox<BatchFilter> cmbBatch;
    @FXML private DatePicker dpDate;

    // Table
    @FXML private TableView<AttendanceRow> attendanceTable;
    @FXML private TableColumn<AttendanceRow, String> colCode;
    @FXML private TableColumn<AttendanceRow, String> colName;
    @FXML private TableColumn<AttendanceRow, String> colStatus;
    @FXML private TableColumn<AttendanceRow, String> colRemarks;

    // Summary
    @FXML private Label lblStudentCount;
    @FXML private Label lblPresentCount;
    @FXML private Label lblAbsentCount;
    @FXML private Label lblLateCount;
    @FXML private Label lblUnmarkedCount;

    // Toast
    @FXML private VBox attendanceToast;
    @FXML private Label lblToastTitle;
    @FXML private Label lblToastHeading;
    @FXML private Label lblToastMessage;

    private final TeacherAttendanceDAO dao = new TeacherAttendanceDAO();
    private TeacherInfo teacherInfo;
    private PauseTransition toastTimer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Columns
        colCode.setCellValueFactory(new PropertyValueFactory<>("studentCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("studentName"));

        // Status cell — ComboBox
        colStatus.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> combo = new ComboBox<>(
                    FXCollections.observableArrayList("Present", "Absent", "Late"));

            {
                combo.setPrefWidth(120);
                combo.setOnAction(e -> {
                    AttendanceRow row = getTableRow() == null ? null : getTableRow().getItem();
                    if (row != null) {
                        row.setStatus(combo.getValue());
                        updateSummary();
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    combo.setValue(item);
                    setGraphic(combo);
                }
            }
        });
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Remarks cell — TextField
        colRemarks.setCellFactory(col -> new TableCell<>() {
            private final TextField tf = new TextField();

            {
                tf.setPromptText("Optional note...");
                tf.focusedProperty().addListener((o, was, is) -> {
                    if (!is) {
                        AttendanceRow row = getTableRow() == null ? null : getTableRow().getItem();
                        if (row != null) row.setRemarks(tf.getText());
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    tf.setText(item == null ? "" : item);
                    setGraphic(tf);
                }
            }
        });
        colRemarks.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        attendanceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Filter events
        cmbBatch.setOnAction(e -> loadStudents());
        dpDate.valueProperty().addListener((o, a, b) -> loadStudents());
    }

    public void setTeacherInfo(TeacherInfo info) {
        this.teacherInfo = info;

        cmbBatch.getItems().clear();
        cmbBatch.getItems().add(new BatchFilter(-1, "All Batches"));
        cmbBatch.getItems().addAll(dao.findTeacherBatches(info.getTeacherId()));
        cmbBatch.getSelectionModel().selectFirst();

        dpDate.setValue(LocalDate.now());
        loadStudents();
    }

    // -------------------------------------------------
    @FXML
    public void loadStudents() {
        if (teacherInfo == null) return;

        BatchFilter b = cmbBatch.getValue();
        LocalDate date = dpDate.getValue();
        if (date == null) return;

        List<AttendanceRow> rows = dao.findStudentsForDate(
                teacherInfo.getTeacherId(),
                b == null ? -1 : b.getId(),
                date);

        attendanceTable.setItems(FXCollections.observableArrayList(rows));
        lblStudentCount.setText(rows.size() + " student(s)");
        updateSummary();
    }

    @FXML
    public void markAllPresent() {
        for (AttendanceRow r : attendanceTable.getItems()) {
            r.setStatus("Present");
        }
        attendanceTable.refresh();
        updateSummary();
    }

    @FXML
    public void saveAttendance() {
        if (teacherInfo == null) return;

        LocalDate date = dpDate.getValue();
        if (date == null) {
            showToast("SAVE FAILED", "Missing date", "Please pick a date first.", false);
            return;
        }

        int saved = dao.saveAll(teacherInfo.getTeacherId(), date, attendanceTable.getItems());

        if (saved == 0) {
            showToast("SAVE SKIPPED", "Nothing to save",
                    "Mark at least one student before saving.", false);
        } else {
            showToast("SAVE SUCCESSFUL", "Attendance saved",
                    "Saved " + saved + " record(s) for " + date + ".", true);
            loadStudents();
        }
    }

    // -------------------------------------------------
    private void updateSummary() {
        int present = 0, absent = 0, late = 0, unmarked = 0;

        for (AttendanceRow r : attendanceTable.getItems()) {
            if (r.getStatus() == null || r.getStatus().isBlank()) unmarked++;
            else if (r.getStatus().equals("Present")) present++;
            else if (r.getStatus().equals("Absent"))  absent++;
            else if (r.getStatus().equals("Late"))    late++;
        }

        lblPresentCount.setText(String.valueOf(present));
        lblAbsentCount.setText(String.valueOf(absent));
        lblLateCount.setText(String.valueOf(late));
        lblUnmarkedCount.setText(String.valueOf(unmarked));
    }

    // -------------------------------------------------
    //  Toast — same style as the dashboard welcome message
    // -------------------------------------------------
    private void showToast(String title, String heading, String message, boolean success) {
        if (attendanceToast == null) return;

        // Fixed size (same as welcome toast)
        attendanceToast.setPrefWidth(420);
        attendanceToast.setMinWidth(420);
        attendanceToast.setMaxWidth(420);
        attendanceToast.setPrefHeight(125);
        attendanceToast.setMinHeight(125);
        attendanceToast.setMaxHeight(125);

        // Icon background / accent color by success flag
        String bg   = success ? "#dcfce7" : "#fee2e2";
        String fg   = success ? "#16a34a" : "#dc2626";
        String bar  = success ? "#22c55e" : "#ef4444";
        String mark = success ? "✓" : "!";

        lblToastTitle.setText(title);
        lblToastHeading.setText(heading);
        lblToastMessage.setText(message);

        // Update icon + accent bar dynamically
        attendanceToast.setStyle(
                "-fx-background-color:white; -fx-background-radius:15;" +
                        "-fx-padding:15 17 15 15; -fx-border-color:#e2e8f0;" +
                        "-fx-border-radius:15;" +
                        "-fx-effect:dropshadow(gaussian,rgba(15,23,42,.20),22,0,0,6);");

        // icon circle
        var iconBox = (javafx.scene.layout.StackPane) attendanceToast.getChildren().get(0)
                .lookup(".toast-icon");
        if (iconBox != null) iconBox.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:19;");

        var iconLbl = attendanceToast.lookup(".toast-icon-label");
        if (iconLbl != null) {
            iconLbl.setStyle("-fx-text-fill:" + fg + "; -fx-font-size:17px; -fx-font-weight:bold;");
            ((Label) iconLbl).setText(mark);
        }

        var barNode = attendanceToast.lookup(".toast-bar");
        if (barNode != null) {
            barNode.setStyle("-fx-background-color:" + bar + "; -fx-background-radius:3;");
        }

        // Show
        attendanceToast.setManaged(true);
        attendanceToast.setVisible(true);

        if (toastTimer != null) toastTimer.stop();
        toastTimer = new PauseTransition(Duration.seconds(3));
        toastTimer.setOnFinished(e -> {
            attendanceToast.setVisible(false);
            attendanceToast.setManaged(false);
        });
        toastTimer.play();
    }
}