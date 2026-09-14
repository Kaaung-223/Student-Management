package com.example.student_management_system.Controller.Teacher;

import com.example.student_management_system.Controller.DAO.TeacherLeaveRequestsDAO;
import com.example.student_management_system.Controller.Model.BatchFilter;
import com.example.student_management_system.Controller.Model.LeaveRequestRow;
import com.example.student_management_system.Controller.Model.TeacherInfo;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class TeacherLeaveRequestsController implements Initializable {

    // Filter bar
    @FXML private ComboBox<BatchFilter> cmbBatch;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private TextField txtSearch;

    // Table
    @FXML private TableView<LeaveRequestRow> leaveTable;
    @FXML private TableColumn<LeaveRequestRow, String>  colCode;
    @FXML private TableColumn<LeaveRequestRow, String>  colName;
    @FXML private TableColumn<LeaveRequestRow, String>  colBatch;
    @FXML private TableColumn<LeaveRequestRow, String>  colFrom;
    @FXML private TableColumn<LeaveRequestRow, String>  colTo;
    @FXML private TableColumn<LeaveRequestRow, String>  colStatus;
    @FXML private Label lblRequestCount;

    // Details card
    @FXML private Label lblStudentName;
    @FXML private Label lblStudentCode;
    @FXML private Label lblBatch;
    @FXML private Label lblDates;
    @FXML private Label lblDays;
    @FXML private Label lblStatus;
    @FXML private Label lblApprovedBy;
    @FXML private TextArea txtReason;

    // Counters
    @FXML private Label lblTotal;
    @FXML private Label lblPending;
    @FXML private Label lblApproved;
    @FXML private Label lblRejected;

    private final TeacherLeaveRequestsDAO dao = new TeacherLeaveRequestsDAO();
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private TeacherInfo teacherInfo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // ===== Table =====
        colCode.setCellValueFactory(new PropertyValueFactory<>("studentCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colBatch.setCellValueFactory(new PropertyValueFactory<>("batchName"));

        colFrom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getLeaveFrom() == null ? "--" : c.getValue().getLeaveFrom().format(DATE)));
        colTo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getLeaveTo() == null ? "--" : c.getValue().getLeaveTo().format(DATE)));

        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        leaveTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        leaveTable.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> showDetails(b));

        // ===== Filters =====
        cmbStatus.setItems(FXCollections.observableArrayList(
                "All", "Pending", "Approved", "Rejected"));
        cmbStatus.setValue("All");

        cmbBatch.setOnAction(e -> refresh());
        cmbStatus.setOnAction(e -> refresh());

        PauseTransition debounce = new PauseTransition(Duration.millis(250));
        debounce.setOnFinished(e -> refresh());
        txtSearch.textProperty().addListener((o, a, b) -> debounce.play());
    }

    /** Called by TeacherDashboardController. */
    public void setTeacherInfo(TeacherInfo info) {
        this.teacherInfo = info;

        cmbBatch.getItems().clear();
        cmbBatch.getItems().add(new BatchFilter(-1, "All Batches"));
        cmbBatch.getItems().addAll(dao.findTeacherBatches(info.getTeacherId()));
        cmbBatch.getSelectionModel().selectFirst();

        refresh();
    }

    @FXML
    public void refresh() {
        if (teacherInfo == null) return;

        BatchFilter b = cmbBatch.getValue();

        List<LeaveRequestRow> list = dao.findLeaveRequests(
                teacherInfo.getTeacherId(),
                b == null ? -1 : b.getId(),
                cmbStatus.getValue(),
                txtSearch.getText());

        leaveTable.setItems(FXCollections.observableArrayList(list));
        lblRequestCount.setText(list.size() + " request(s)");

        // Counters
        int pending = 0, approved = 0, rejected = 0;
        for (LeaveRequestRow r : list) {
            if ("Pending".equalsIgnoreCase(r.getStatus()))       pending++;
            else if ("Approved".equalsIgnoreCase(r.getStatus())) approved++;
            else if ("Rejected".equalsIgnoreCase(r.getStatus())) rejected++;
        }

        lblTotal.setText(String.valueOf(list.size()));
        lblPending.setText(String.valueOf(pending));
        lblApproved.setText(String.valueOf(approved));
        lblRejected.setText(String.valueOf(rejected));

        if (!list.isEmpty()) leaveTable.getSelectionModel().selectFirst();
        else                 clearDetails();
    }

    // -------------------------------------------------
    private void showDetails(LeaveRequestRow r) {
        if (r == null) { clearDetails(); return; }

        lblStudentName.setText(r.getStudentName());
        lblStudentCode.setText(r.getStudentCode() + "  |  " + safe(r.getBatchName()));
        lblBatch.setText("Batch: " + safe(r.getBatchName()));

        String fromStr = r.getLeaveFrom() == null ? "--" : r.getLeaveFrom().format(DATE);
        String toStr   = r.getLeaveTo()   == null ? "--" : r.getLeaveTo().format(DATE);
        lblDates.setText("From: " + fromStr + "    To: " + toStr);

        lblDays.setText(r.getDaysCount() > 0 ? r.getDaysCount() + " day(s)" : "--");

        lblStatus.setText("Status: " + safe(r.getStatus()));
        lblApprovedBy.setText("Handled by: " + safe(r.getApprovedBy()));

        txtReason.setText(r.getReason() == null ? "" : r.getReason());
    }

    private void clearDetails() {
        lblStudentName.setText("No request selected");
        lblStudentCode.setText("Select a row to view details");
        lblBatch.setText("Batch: --");
        lblDates.setText("From: --    To: --");
        lblDays.setText("--");
        lblStatus.setText("Status: --");
        lblApprovedBy.setText("Handled by: --");
        txtReason.setText("");
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "--" : v;
    }
}