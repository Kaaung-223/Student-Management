package com.example.student_management_system.Controller.Staff;

import com.example.student_management_system.Controller.DAO.StudentDao;
import com.example.student_management_system.Controller.DAO.StaffStudentDetailsDAO;
import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.StaffStudentDetails;
import com.example.student_management_system.Controller.Model.Student;

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

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class StaffStudentsController implements Initializable {

    // Filter row
    @FXML private ComboBox<Batch> cmbBatch;
    @FXML private TextField txtSearch;

    // Header
    @FXML private Button btnAddStudent;
    @FXML private Label lblStudentCount;

    // Table
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colCode;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colBatch;
    @FXML private TableColumn<Student, String> colEmail;
    @FXML private TableColumn<Student, String> colStatus;

    // Details card
    @FXML private Label lblAvatar;
    @FXML private Label lblStudentName;
    @FXML private Label lblStudentCode;
    @FXML private Label lblBatch;
    @FXML private Label lblEmail;
    @FXML private Label lblPhone;
    @FXML private Label lblAdmission;

    @FXML private Label lblPresent;
    @FXML private Label lblAbsent;
    @FXML private Label lblLate;
    @FXML private Label lblPass;
    @FXML private Label lblFail;
    @FXML private Label lblLeave;
    @FXML private Label lblLatestResult;

    @FXML private Label lblFee;
    @FXML private Label lblPaid;
    @FXML private Label lblOutstanding;
    @FXML private Label lblPaymentStatus;

    // ✅ NEW: status toggle button
    @FXML private Button btnToggleStatus;

    // Toast
    @FXML private VBox studentToast;
    @FXML private Label lblToastTitle;
    @FXML private Label lblToastHeading;
    @FXML private Label lblToastMessage;

    private final StudentDao studentDao = new StudentDao();
    private final StaffStudentDetailsDAO detailsDAO = new StaffStudentDetailsDAO();

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(Locale.US);

    private PauseTransition toastTimer;
    private Student selectedStudent;

    // =========================================================
    //  INIT
    // =========================================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colCode.setCellValueFactory(new PropertyValueFactory<>("studentCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colBatch.setCellValueFactory(new PropertyValueFactory<>("className"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        studentTable.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> showDetails(b));

        cmbBatch.getItems().add(new Batch(-1, "All Batches"));
        cmbBatch.getItems().addAll(loadBatches());
        cmbBatch.getSelectionModel().selectFirst();
        cmbBatch.setOnAction(e -> refresh());

        PauseTransition debounce = new PauseTransition(Duration.millis(250));
        debounce.setOnFinished(e -> refresh());
        txtSearch.textProperty().addListener((o, a, b) -> debounce.play());
    }

    public void setStaffInfo(Object staffInfo) {
        refresh();
    }

    // =========================================================
    //  ADD STUDENT
    // =========================================================
    @FXML
    public void openAddStudentDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/student_management_system/View/Staff/StaffStudentDialog.fxml"));
            Parent root = loader.load();

            Stage dlg = new Stage();
            dlg.setTitle("Add Student");
            dlg.initModality(Modality.APPLICATION_MODAL);
            dlg.setScene(new Scene(root, 640, 780));
            dlg.setMinWidth(500);
            dlg.setMinHeight(600);
            dlg.setResizable(true);

            AddStudentDialogController ctrl = loader.getController();
            ctrl.setDialogStage(dlg);
            ctrl.setDialogMode(AddStudentDialogController.DialogMode.ADD);

            dlg.showAndWait();

            if (ctrl.wasSaved()) {
                showToast("STUDENT ADDED", "New student created",
                        "The student has been added successfully.", true);
                refresh();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showToast("ADD FAILED", "Could not add student",
                    "An error occurred while opening the form.", false);
        }
    }

    // =========================================================
    //  ✅ TOGGLE STATUS (ACTIVE <-> INACTIVE)
    // =========================================================
    @FXML
    public void toggleStudentStatus() {
        if (selectedStudent == null) {
            showToast("NO SELECTION", "Select a student",
                    "Choose a student first to update status.", false);
            return;
        }

        String current = selectedStudent.getStatus() == null
                ? "ACTIVE" : selectedStudent.getStatus().toUpperCase();
        boolean isActive = "ACTIVE".equals(current);
        String newStatus = isActive ? "INACTIVE" : "ACTIVE";

        // confirm
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(isActive ? "Deactivate Student" : "Activate Student");
        confirm.setHeaderText((isActive ? "Deactivate " : "Activate ")
                + selectedStudent.getStudentName() + "?");
        confirm.setContentText(isActive
                ? "The student will be marked as INACTIVE."
                : "The student will be marked as ACTIVE.");

        confirm.showAndWait().ifPresent(resp -> {
            if (resp != ButtonType.OK) return;

            boolean ok = detailsDAO.updateStatus(selectedStudent.getStudentId(), newStatus);

            if (ok) {
                // Update local object so UI reflects immediately
                selectedStudent.setStatus(newStatus);
                refresh();   // reload list to reflect new status
                showToast(
                        "STATUS UPDATED",
                        newStatus.equals("ACTIVE") ? "Student activated" : "Student deactivated",
                        selectedStudent.getStudentName() + " is now " + newStatus + ".",
                        true);
            } else {
                showToast("UPDATE FAILED", "Could not update status",
                        "An error occurred while updating the student.", false);
            }
        });
    }

    // =========================================================
    //  LOAD
    // =========================================================
    @FXML
    public void refresh() {
        Batch b = cmbBatch.getValue();
        int batchId = (b == null) ? -1 : b.getId();

        List<Student> all = studentDao.getStudentsByBatch(batchId);

        String q = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        List<Student> filtered = new ArrayList<>();
        if (q.isEmpty()) {
            filtered.addAll(all);
        } else {
            for (Student s : all) {
                if (containsIgnoreCase(s.getStudentName(), q)
                        || containsIgnoreCase(s.getStudentCode(), q)
                        || containsIgnoreCase(s.getEmail(), q)) {
                    filtered.add(s);
                }
            }
        }

        studentTable.setItems(FXCollections.observableArrayList(filtered));
        lblStudentCount.setText(filtered.size() + " student(s)");

        if (!filtered.isEmpty()) studentTable.getSelectionModel().selectFirst();
        else                     clearDetails();
    }

    private List<Batch> loadBatches() {
        List<Batch> list = new ArrayList<>();
        String sql = "SELECT class_id, class_name FROM classes ORDER BY class_name";

        try (java.sql.Connection con = com.example.student_management_system
                .Controller.DAO.DBConnention.getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Batch(rs.getInt("class_id"), rs.getString("class_name")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // =========================================================
    //  DETAILS
    // =========================================================
    private void showDetails(Student s) {
        if (s == null) { clearDetails(); return; }

        selectedStudent = s;

        lblAvatar.setText(initialsOf(s.getStudentName()));
        lblStudentName.setText(s.getStudentName());
        lblStudentCode.setText(s.getStudentCode() + "  |  " + safe(s.getStatus()));
        lblBatch.setText("Batch: " + safe(s.getClassName()));
        lblEmail.setText("Email: " + safe(s.getEmail()));

        // Update toggle button label based on current status
        updateToggleButton(s.getStatus());

        StaffStudentDetails d = detailsDAO.getDetails(s.getStudentId());

        lblPhone.setText("Phone: " + safe(d.getPhone()));
        lblAdmission.setText("Admission: " +
                (d.getAdmissionDate() == null ? "--" : d.getAdmissionDate().format(DATE)));

        lblPresent.setText(String.valueOf(d.getPresentCount()));
        lblAbsent.setText(String.valueOf(d.getAbsentCount()));
        lblLate.setText(String.valueOf(d.getLateCount()));
        lblPass.setText(String.valueOf(d.getPassCount()));
        lblFail.setText(String.valueOf(d.getFailCount()));
        lblLeave.setText(String.valueOf(d.getLeaveCount()));

        lblLatestResult.setText("Latest exam result: " + safe(d.getLatestResult()));

        BigDecimal fee  = d.getTotalFee()  == null ? BigDecimal.ZERO : d.getTotalFee();
        BigDecimal paid = d.getTotalPaid() == null ? BigDecimal.ZERO : d.getTotalPaid();
        BigDecimal out  = d.getOutstanding();

        lblFee.setText("Fee: " + MONEY.format(fee) + " MMK");
        lblPaid.setText("Paid: " + MONEY.format(paid) + " MMK");
        lblOutstanding.setText("Outstanding: " + MONEY.format(out) + " MMK");
        lblPaymentStatus.setText("Status: " + d.getPaymentStatus());
    }

    /** Updates the toggle button text and colors based on current status. */
    private void updateToggleButton(String status) {
        if (btnToggleStatus == null) return;

        boolean isActive = "ACTIVE".equalsIgnoreCase(status);

        if (isActive) {
            btnToggleStatus.setText("Deactivate Student");
            btnToggleStatus.setStyle(
                    "-fx-background-color:#fee2e2;" +
                            "-fx-text-fill:#dc2626;" +
                            "-fx-background-radius:9;" +
                            "-fx-font-size:12px;" +
                            "-fx-font-weight:bold;" +
                            "-fx-padding:0 20;" +
                            "-fx-cursor:hand;" +
                            "-fx-border-color:#fecaca;" +
                            "-fx-border-radius:9;");
        } else {
            btnToggleStatus.setText("Activate Student");
            btnToggleStatus.setStyle(
                    "-fx-background-color:#dcfce7;" +
                            "-fx-text-fill:#16a34a;" +
                            "-fx-background-radius:9;" +
                            "-fx-font-size:12px;" +
                            "-fx-font-weight:bold;" +
                            "-fx-padding:0 20;" +
                            "-fx-cursor:hand;" +
                            "-fx-border-color:#bbf7d0;" +
                            "-fx-border-radius:9;");
        }
    }

    private void clearDetails() {
        selectedStudent = null;

        lblAvatar.setText("--");
        lblStudentName.setText("No student selected");
        lblStudentCode.setText("Select a row to view details");
        lblBatch.setText("Batch: --");
        lblEmail.setText("Email: --");
        lblPhone.setText("Phone: --");
        lblAdmission.setText("Admission: --");
        lblPresent.setText("0");
        lblAbsent.setText("0");
        lblLate.setText("0");
        lblPass.setText("0");
        lblFail.setText("0");
        lblLeave.setText("0");
        lblLatestResult.setText("Latest exam result: --");
        lblFee.setText("Fee: --");
        lblPaid.setText("Paid: --");
        lblOutstanding.setText("Outstanding: --");
        lblPaymentStatus.setText("Status: --");

        if (btnToggleStatus != null) {
            btnToggleStatus.setText("Deactivate Student");
            btnToggleStatus.setStyle(
                    "-fx-background-color:#fee2e2;" +
                            "-fx-text-fill:#dc2626;" +
                            "-fx-background-radius:9;" +
                            "-fx-font-size:12px;" +
                            "-fx-font-weight:bold;" +
                            "-fx-padding:0 20;" +
                            "-fx-cursor:hand;" +
                            "-fx-border-color:#fecaca;" +
                            "-fx-border-radius:9;");
        }
    }

    // =========================================================
    //  TOAST
    // =========================================================
    private void showToast(String title, String heading, String msg, boolean success) {
        if (studentToast == null) return;

        studentToast.setPrefWidth(420);
        studentToast.setMinWidth(420);
        studentToast.setMaxWidth(420);
        studentToast.setPrefHeight(125);
        studentToast.setMinHeight(125);
        studentToast.setMaxHeight(125);

        String bg  = success ? "#dcfce7" : "#fee2e2";
        String fg  = success ? "#16a34a" : "#dc2626";
        String bar = success ? "#22c55e" : "#ef4444";
        String mk  = success ? "✓" : "!";

        lblToastTitle.setText(title);
        lblToastHeading.setText(heading);
        lblToastMessage.setText(msg);

        var iconBox = studentToast.lookup(".toast-icon");
        if (iconBox != null)
            iconBox.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:19;");

        var iconLbl = studentToast.lookup(".toast-icon-label");
        if (iconLbl instanceof Label l) {
            l.setText(mk);
            l.setStyle("-fx-text-fill:" + fg + "; -fx-font-size:17px; -fx-font-weight:bold;");
        }

        var barNode = studentToast.lookup(".toast-bar");
        if (barNode != null)
            barNode.setStyle("-fx-background-color:" + bar + "; -fx-background-radius:3;");

        studentToast.setManaged(true);
        studentToast.setVisible(true);

        if (toastTimer != null) toastTimer.stop();
        toastTimer = new PauseTransition(Duration.seconds(3));
        toastTimer.setOnFinished(e -> {
            studentToast.setVisible(false);
            studentToast.setManaged(false);
        });
        toastTimer.play();
    }

    // =========================================================
    //  HELPERS
    // =========================================================
    private String initialsOf(String name) {
        if (name == null || name.isBlank()) return "--";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private boolean containsIgnoreCase(String haystack, String needleLower) {
        return haystack != null && haystack.toLowerCase().contains(needleLower);
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "--" : v;
    }
}