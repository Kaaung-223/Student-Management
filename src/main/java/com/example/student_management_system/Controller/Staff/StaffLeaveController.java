package com.example.student_management_system.Controller.Staff;

import com.example.student_management_system.Controller.DAO.StaffLeaveDAO;
import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.LeaveRequestRow;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class StaffLeaveController implements Initializable {

    // Filter
    @FXML private ComboBox<Batch> cmbBatch;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private TextField txtSearch;

    // Header
    @FXML private Label lblLeaveCount;
    @FXML private Button btnAddLeave;

    // Table
    @FXML private TableView<LeaveRequestRow> leaveTable;
    @FXML private TableColumn<LeaveRequestRow, String> colCode;
    @FXML private TableColumn<LeaveRequestRow, String> colName;
    @FXML private TableColumn<LeaveRequestRow, String> colBatch;
    @FXML private TableColumn<LeaveRequestRow, String> colFrom;
    @FXML private TableColumn<LeaveRequestRow, String> colTo;
    @FXML private TableColumn<LeaveRequestRow, String> colStatus;

    // Details card
    @FXML private Label lblAvatar;
    @FXML private Label lblStudentName;
    @FXML private Label lblStudentCode;
    @FXML private Label lblBatch;
    @FXML private Label lblDates;
    @FXML private Label lblDays;
    @FXML private Label lblStatus;
    @FXML private Label lblApprovedBy;
    @FXML private TextArea txtReason;

    // Buttons
    @FXML private Button btnApprove;
    @FXML private Button btnReject;

    // Counters
    @FXML private Label lblTotal;
    @FXML private Label lblPending;
    @FXML private Label lblApproved;
    @FXML private Label lblRejected;

    // Toast
    @FXML private VBox leaveToast;
    @FXML private Label lblToastTitle;
    @FXML private Label lblToastHeading;
    @FXML private Label lblToastMessage;

    private final StaffLeaveDAO dao = new StaffLeaveDAO();
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private LeaveRequestRow selectedLeave;
    private PauseTransition toastTimer;

    // Approver user id — admin fallback
    private int approverUserId = 1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Table columns
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

        // Batch combo
        cmbBatch.getItems().add(new Batch(-1, "All Batches"));
        cmbBatch.getItems().addAll(dao.getAllBatches());
        cmbBatch.getSelectionModel().selectFirst();
        cmbBatch.setOnAction(e -> refresh());

        // Status combo
        cmbStatus.setItems(FXCollections.observableArrayList(
                "All", "Pending", "Approved", "Rejected"));
        cmbStatus.setValue("All");
        cmbStatus.setOnAction(e -> refresh());

        // Debounced search
        PauseTransition debounce = new PauseTransition(Duration.millis(250));
        debounce.setOnFinished(e -> refresh());
        txtSearch.textProperty().addListener((o, a, b) -> debounce.play());
    }

    public void setStaffInfo(Object staffInfo) {
        refresh();
    }

    // =========================================================
    //  LOAD
    // =========================================================
    @FXML
    public void refresh() {
        Batch b = cmbBatch.getValue();

        List<LeaveRequestRow> list = dao.findLeaveRequests(
                b == null ? -1 : b.getId(),
                cmbStatus.getValue(),
                txtSearch.getText());

        leaveTable.setItems(FXCollections.observableArrayList(list));
        lblLeaveCount.setText(list.size() + " request(s)");

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

    @FXML
    public void resetFilters() {
        if (!cmbBatch.getItems().isEmpty())
            cmbBatch.getSelectionModel().selectFirst();
        cmbStatus.setValue("All");
        txtSearch.clear();
        refresh();
    }

    // =========================================================
    //  ADD LEAVE REQUEST
    // =========================================================
    @FXML
    public void openAddLeaveDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Staff/StaffLeaveDialog.fxml"));
            Parent root = loader.load();

            AddLeaveRequestDialogController ctrl = loader.getController();

            Stage dlg = new Stage();
            dlg.setTitle("New Leave Request");
            dlg.initModality(Modality.APPLICATION_MODAL);
            dlg.setScene(new Scene(root, 620, 780));
            dlg.setMinWidth(520);
            dlg.setMinHeight(620);
            dlg.setResizable(true);

            ctrl.setDialogStage(dlg);

            dlg.showAndWait();

            if (ctrl.wasSaved()) {
                showToast("LEAVE SUBMITTED", "New request created",
                        "The leave request has been saved and is pending approval.", true);
                refresh();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showToast("CREATE FAILED", "Could not open form",
                    "An error occurred while opening the dialog.", false);
        }
    }

    // =========================================================
    //  DETAILS
    // =========================================================
    private void showDetails(LeaveRequestRow r) {
        if (r == null) { clearDetails(); return; }
        selectedLeave = r;

        lblAvatar.setText(r.getInitials());
        lblStudentName.setText(r.getStudentName());
        lblStudentCode.setText(r.getStudentCode() + "  |  " + safe(r.getBatchName()));
        lblBatch.setText("Batch: " + safe(r.getBatchName()));

        String fromStr = r.getLeaveFrom() == null ? "--" : r.getLeaveFrom().format(DATE);
        String toStr   = r.getLeaveTo()   == null ? "--" : r.getLeaveTo().format(DATE);
        lblDates.setText("From: " + fromStr + "    To: " + toStr);

        lblDays.setText(r.getDaysCount() > 0
                ? r.getDaysCount() + " day(s)"
                : "--");

        lblStatus.setText("Status: " + safe(r.getStatus()));
        lblStatus.setStyle("-fx-font-size:12px;-fx-font-weight:bold;"
                + "-fx-text-fill:" + statusColor(r.getStatus()) + ";");

        lblApprovedBy.setText("Handled by: " + safe(r.getApprovedBy()));

        txtReason.setText(r.getReason() == null ? "" : r.getReason());

        // Enable buttons only for Pending requests
        boolean pending = r.isPending();
        btnApprove.setDisable(!pending);
        btnReject.setDisable(!pending);
    }

    private String statusColor(String status) {
        if (status == null) return "#64748b";
        switch (status) {
            case "Pending":  return "#d97706";
            case "Approved": return "#16a34a";
            case "Rejected": return "#dc2626";
            default:         return "#64748b";
        }
    }

    private void clearDetails() {
        selectedLeave = null;

        lblAvatar.setText("--");
        lblStudentName.setText("No request selected");
        lblStudentCode.setText("Select a row to view details");
        lblBatch.setText("Batch: --");
        lblDates.setText("From: --    To: --");
        lblDays.setText("--");
        lblStatus.setText("Status: --");
        lblStatus.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#64748b;");
        lblApprovedBy.setText("Handled by: --");
        txtReason.setText("");

        btnApprove.setDisable(true);
        btnReject.setDisable(true);
    }

    // =========================================================
    //  APPROVE / REJECT
    // =========================================================
    @FXML
    public void approve() {
        handleAction("Approved");
    }

    @FXML
    public void reject() {
        handleAction("Rejected");
    }

    private void handleAction(String newStatus) {
        if (selectedLeave == null) {
            showToast("NO REQUEST", "Select a request first",
                    "Choose a leave request from the list.", false);
            return;
        }
        if (!selectedLeave.isPending()) {
            showToast("ALREADY HANDLED", "Request is not pending",
                    "Only pending requests can be approved or rejected.", false);
            return;
        }

        boolean approving = "Approved".equals(newStatus);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(approving ? "Approve Leave" : "Reject Leave");
        confirm.setHeaderText((approving ? "Approve " : "Reject ")
                + selectedLeave.getStudentName() + "'s leave request?");
        confirm.setContentText(approving
                ? "The student will be marked as on leave for the chosen dates."
                : "The student's leave request will be rejected.");

        confirm.showAndWait().ifPresent(resp -> {
            if (resp != ButtonType.OK) return;

            boolean ok = dao.updateStatus(
                    selectedLeave.getLeaveId(),
                    newStatus,
                    approverUserId);

            if (!ok) {
                showToast("UPDATE FAILED", "Could not update request",
                        "Try again later.", false);
                return;
            }

            showToast(
                    "LEAVE " + newStatus.toUpperCase(),
                    approving ? "Leave approved" : "Leave rejected",
                    selectedLeave.getStudentName() + "'s request has been "
                            + newStatus.toLowerCase() + ".",
                    approving);

            int selectedId = selectedLeave.getLeaveId();
            refresh();
            for (LeaveRequestRow r : leaveTable.getItems()) {
                if (r.getLeaveId() == selectedId) {
                    leaveTable.getSelectionModel().select(r);
                    break;
                }
            }
        });
    }

    // =========================================================
    //  TOAST
    // =========================================================
    private void showToast(String title, String heading, String msg, boolean success) {
        if (leaveToast == null) return;

        leaveToast.setPrefWidth(420);
        leaveToast.setMinWidth(420);
        leaveToast.setMaxWidth(420);
        leaveToast.setPrefHeight(125);
        leaveToast.setMinHeight(125);
        leaveToast.setMaxHeight(125);

        String bg  = success ? "#dcfce7" : "#fee2e2";
        String fg  = success ? "#16a34a" : "#dc2626";
        String bar = success ? "#22c55e" : "#ef4444";
        String mk  = success ? "✓" : "!";

        lblToastTitle.setText(title);
        lblToastHeading.setText(heading);
        lblToastMessage.setText(msg);

        var iconBox = leaveToast.lookup(".toast-icon");
        if (iconBox != null)
            iconBox.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:19;");

        var iconLbl = leaveToast.lookup(".toast-icon-label");
        if (iconLbl instanceof Label l) {
            l.setText(mk);
            l.setStyle("-fx-text-fill:" + fg + "; -fx-font-size:17px; -fx-font-weight:bold;");
        }

        var barNode = leaveToast.lookup(".toast-bar");
        if (barNode != null)
            barNode.setStyle("-fx-background-color:" + bar + "; -fx-background-radius:3;");

        leaveToast.setManaged(true);
        leaveToast.setVisible(true);

        if (toastTimer != null) toastTimer.stop();
        toastTimer = new PauseTransition(Duration.seconds(3));
        toastTimer.setOnFinished(e -> {
            leaveToast.setVisible(false);
            leaveToast.setManaged(false);
        });
        toastTimer.play();
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "--" : v;
    }
}