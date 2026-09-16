package com.example.student_management_system.Controller.Staff;

import com.example.student_management_system.Controller.DAO.StaffPaymentsDAO;
import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.PaymentRow;
import com.example.student_management_system.Controller.Model.PaymentStudent;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.Desktop;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class StaffPaymentsController implements Initializable {

    @FXML private ComboBox<Batch> cmbBatch;
    @FXML private TextField txtSearch;
    @FXML private CheckBox chkOnlyActive;

    @FXML private TableView<PaymentStudent> studentTable;
    @FXML private TableColumn<PaymentStudent, String> colCode;
    @FXML private TableColumn<PaymentStudent, String> colName;
    @FXML private TableColumn<PaymentStudent, String> colBatch;
    @FXML private TableColumn<PaymentStudent, String> colStatus;
    @FXML private Label lblStudentCount;

    // Batch price card
    @FXML private VBox  batchPriceCard;
    @FXML private Label lblBatchPriceBatch;
    @FXML private Label lblBatchPriceAmount;
    @FXML private Label lblBatchPricePerMonth;
    @FXML private Label lblBatchPriceHint;

    @FXML private Label lblSelectedStudent;
    @FXML private Label lblSelectedStudentCode;
    @FXML private Label lblFeeSummary;

    @FXML private TextField txtAmount;
    @FXML private Label lblAmountError;

    @FXML private ComboBox<String> cmbMethod;
    @FXML private ComboBox<String> cmbPeriod;
    @FXML private DatePicker dpPaymentDate;
    @FXML private TextField txtReceiptNo;
    @FXML private TextArea txtNote;

    @FXML private Button btnSavePayment;
    @FXML private Button btnToggleStatus;
    @FXML private Button btnDownloadReceipt;
    @FXML private Button btnReset;

    @FXML private TableView<PaymentRow> historyTable;
    @FXML private TableColumn<PaymentRow, String> colHReceipt;
    @FXML private TableColumn<PaymentRow, String> colHDate;
    @FXML private TableColumn<PaymentRow, String> colHMethod;
    @FXML private TableColumn<PaymentRow, String> colHPeriod;
    @FXML private TableColumn<PaymentRow, String> colHAmount;
    @FXML private TableColumn<PaymentRow, String> colHNote;

    @FXML private VBox paymentToast;
    @FXML private Label lblToastTitle;
    @FXML private Label lblToastHeading;
    @FXML private Label lblToastMessage;

    private final StaffPaymentsDAO dao = new StaffPaymentsDAO();

    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(Locale.US);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final String[] METHOD_KEYS   = {"CASH","CREDIT_CARD","BANK","AYA_PAY","KBZ_PAY"};
    private static final String[] METHOD_LABELS = {"Cash","Credit Card","Bank Transfer","AYA Pay","KBZ Pay"};
    private static final String[] PERIOD_KEYS   = {"FIRST_6_MONTHS","SECOND_6_MONTHS"};
    private static final String[] PERIOD_LABELS = {"First 6 Months","Second 6 Months"};

    private PaymentStudent selectedStudent;
    private PaymentRow     selectedPayment;
    private PauseTransition toastTimer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colCode.setCellValueFactory(new PropertyValueFactory<>("studentCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colBatch.setCellValueFactory(new PropertyValueFactory<>("batchName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        studentTable.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> onStudentSelected(b));

        colHReceipt.setCellValueFactory(new PropertyValueFactory<>("receiptNo"));
        colHDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPaymentDate() == null ? "--" : c.getValue().getPaymentDate().format(DATE)));
        colHMethod.setCellValueFactory(new PropertyValueFactory<>("methodLabel"));
        colHPeriod.setCellValueFactory(new PropertyValueFactory<>("periodLabel"));
        colHAmount.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getAmount() == null ? "0" : MONEY.format(c.getValue().getAmount()) + " MMK"));
        colHNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> selectedPayment = b);

        cmbBatch.getItems().add(new Batch(-1, "All Batches"));
        cmbBatch.getItems().addAll(dao.getAllBatches());
        cmbBatch.getSelectionModel().selectFirst();
        cmbBatch.setOnAction(e -> refreshStudents());

        chkOnlyActive.setSelected(true);
        chkOnlyActive.setOnAction(e -> refreshStudents());

        PauseTransition debounce = new PauseTransition(Duration.millis(250));
        debounce.setOnFinished(e -> refreshStudents());
        txtSearch.textProperty().addListener((o, a, b) -> debounce.play());

        cmbMethod.setItems(FXCollections.observableArrayList(METHOD_LABELS));
        cmbMethod.setValue(METHOD_LABELS[0]);

        cmbPeriod.setItems(FXCollections.observableArrayList(PERIOD_LABELS));
        cmbPeriod.setValue(PERIOD_LABELS[0]);

        dpPaymentDate.setValue(LocalDate.now());
        txtReceiptNo.setText(dao.generateReceiptNo());
        txtReceiptNo.setEditable(false);

        if (batchPriceCard != null) {
            batchPriceCard.setVisible(false);
            batchPriceCard.setManaged(false);
        }

        refreshStudents();
    }

    public void setStaffInfo(Object staffInfo) { refreshStudents(); }

    @FXML
    public void refreshStudents() {
        Batch b = cmbBatch.getValue();
        List<PaymentStudent> list = dao.findStudents(
                b == null ? -1 : b.getId(),
                txtSearch.getText(),
                chkOnlyActive.isSelected());

        studentTable.setItems(FXCollections.observableArrayList(list));
        lblStudentCount.setText(list.size() + " student(s)");

        if (!list.isEmpty()) studentTable.getSelectionModel().selectFirst();
        else                 clearForm();
    }

    @FXML
    public void resetFilters() {
        if (!cmbBatch.getItems().isEmpty())
            cmbBatch.getSelectionModel().selectFirst();
        txtSearch.clear();
        chkOnlyActive.setSelected(true);
        refreshStudents();
    }

    private void onStudentSelected(PaymentStudent s) {
        selectedStudent = s;
        selectedPayment = null;

        if (s == null) { clearForm(); return; }

        lblSelectedStudent.setText(s.getStudentName());
        lblSelectedStudentCode.setText(s.getStudentCode() + "  |  " + safe(s.getBatchName())
                + "  |  " + safe(s.getStatus()));

        BigDecimal paid = s.getTotalPaid() == null ? BigDecimal.ZERO : s.getTotalPaid();
        BigDecimal out  = s.getOutstanding();

        lblFeeSummary.setText(
                "Paid: " + MONEY.format(paid) + " MMK    •    " +
                        "Outstanding: " + MONEY.format(out) + " MMK");

        updateBatchPriceCard(s);

        btnSavePayment.setDisable(!s.isActive());
        updateToggleButton(s.getStatus());

        List<PaymentRow> history = dao.getPaymentHistory(s.getStudentId());
        historyTable.setItems(FXCollections.observableArrayList(history));

        txtAmount.clear();
        clearError();
        txtReceiptNo.setText(dao.generateReceiptNo());
    }

    private void updateBatchPriceCard(PaymentStudent s) {
        if (batchPriceCard == null) return;

        BigDecimal fee = s.getTotalFee();

        if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0) {
            batchPriceCard.setVisible(false);
            batchPriceCard.setManaged(false);
            return;
        }

        lblBatchPriceBatch.setText("Batch: " + safe(s.getBatchName()));
        lblBatchPriceAmount.setText(MONEY.format(fee) + " MMK");

        BigDecimal perMonth = fee.divide(BigDecimal.valueOf(12), 0, BigDecimal.ROUND_HALF_UP);
        lblBatchPricePerMonth.setText("≈ " + MONEY.format(perMonth) + " MMK / month");
        lblBatchPriceHint.setText("Full course fee for this batch");

        batchPriceCard.setVisible(true);
        batchPriceCard.setManaged(true);
    }

    private void updateToggleButton(String status) {
        boolean active = "ACTIVE".equalsIgnoreCase(status);

        btnToggleStatus.setText(active ? "Deactivate Student" : "Activate Student");
        btnToggleStatus.setStyle(
                "-fx-background-color:" + (active ? "#fee2e2" : "#dcfce7") + ";" +
                        "-fx-text-fill:" + (active ? "#dc2626" : "#16a34a") + ";" +
                        "-fx-background-radius:9;-fx-font-size:12px;-fx-font-weight:bold;" +
                        "-fx-padding:0 22;-fx-cursor:hand;" +
                        "-fx-border-color:" + (active ? "#fecaca" : "#bbf7d0") + ";-fx-border-radius:9;");
    }

    @FXML
    public void savePayment() {
        if (selectedStudent == null) {
            showToast("NO STUDENT", "Select a student first",
                    "Pick a student from the list.", false);
            return;
        }
        if (!selectedStudent.isActive()) {
            showToast("INACTIVE STUDENT", "Cannot record payment",
                    "Reactivate the student before recording payments.", false);
            return;
        }

        String amountText = txtAmount.getText() == null ? "" : txtAmount.getText().trim();
        if (amountText.isEmpty()) { showError("Amount is required."); return; }

        BigDecimal amount;
        try { amount = new BigDecimal(amountText); }
        catch (NumberFormatException e) {
            showError("Amount must be a valid number."); return;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            showError("Amount must be greater than zero."); return;
        }

        LocalDate date = dpPaymentDate.getValue();
        if (date == null) {
            showToast("DATE MISSING", "Pick a payment date", "Select the payment date.", false);
            return;
        }
        if (date.isAfter(LocalDate.now())) {
            showToast("DATE INVALID", "Future date not allowed",
                    "Payment date cannot be in the future.", false);
            return;
        }

        Integer feeId = dao.getClassFeeId(selectedStudent.getClassId());
        if (feeId == null) {
            showToast("NO CLASS FEE", "Fee not configured",
                    "Set up the class fee for this batch first.", false);
            return;
        }

        String methodKey = METHOD_KEYS[cmbMethod.getSelectionModel().getSelectedIndex()];
        String periodKey = PERIOD_KEYS[cmbPeriod.getSelectionModel().getSelectedIndex()];

        String receipt = txtReceiptNo.getText();
        if (receipt == null || receipt.isBlank()) receipt = dao.generateReceiptNo();

        int paymentId = dao.addPayment(
                selectedStudent.getStudentId(), feeId, periodKey,
                amount, date, methodKey, receipt,
                txtNote.getText(), 0);

        if (paymentId == -1) {
            showToast("PAYMENT FAILED", "Could not save payment",
                    "Check the database and try again.", false);
            return;
        }

        showToast("PAYMENT SUCCESSFUL", "Payment recorded",
                MONEY.format(amount) + " MMK paid via " + cmbMethod.getValue() + ".", true);

        int selectedId = selectedStudent.getStudentId();
        refreshStudents();
        for (PaymentStudent ps : studentTable.getItems()) {
            if (ps.getStudentId() == selectedId) {
                studentTable.getSelectionModel().select(ps);
                break;
            }
        }
    }

    @FXML
    public void toggleStatus() {
        if (selectedStudent == null) {
            showToast("NO STUDENT", "Select a student first",
                    "Pick a student from the list.", false);
            return;
        }

        boolean active = selectedStudent.isActive();
        String newStatus = active ? "INACTIVE" : "ACTIVE";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(active ? "Deactivate Student" : "Activate Student");
        confirm.setHeaderText((active ? "Deactivate " : "Activate ")
                + selectedStudent.getStudentName() + "?");
        confirm.setContentText(active
                ? "Payments will be blocked until the student is reactivated."
                : "The student will be able to receive payments again.");

        confirm.showAndWait().ifPresent(resp -> {
            if (resp != ButtonType.OK) return;

            boolean ok = dao.updateStudentStatus(selectedStudent.getStudentId(), newStatus);
            if (!ok) {
                showToast("UPDATE FAILED", "Could not update status",
                        "Try again later.", false);
                return;
            }

            showToast("STATUS UPDATED",
                    active ? "Student deactivated" : "Student activated",
                    selectedStudent.getStudentName() + " is now " + newStatus + ".", true);

            refreshStudents();
        });
    }

    @FXML
    public void downloadReceipt() {
        if (selectedPayment == null) {
            showToast("NO RECEIPT", "Select a payment",
                    "Choose a payment in the history list first.", false);
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Receipt");
        chooser.setInitialFileName("receipt_" + safe(selectedPayment.getReceiptNo()) + ".html");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));

        Stage stage = (Stage) historyTable.getScene().getWindow();
        File file = chooser.showSaveDialog(stage);
        if (file == null) return;

        try {
            String html = buildReceiptHtml(selectedPayment);
            Files.write(file.toPath(), html.getBytes(StandardCharsets.UTF_8));

            showToast("RECEIPT SAVED", "Download complete",
                    "Saved as " + file.getName(), true);

            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(file.toURI());
                }
            } catch (Exception ignored) {}

        } catch (Exception ex) {
            ex.printStackTrace();
            showToast("RECEIPT FAILED", "Could not save receipt",
                    "Check that the folder is writable.", false);
        }
    }

    private String buildReceiptHtml(PaymentRow p) {
        String amount   = p.getAmount() == null ? "0" : MONEY.format(p.getAmount());
        String date     = p.getPaymentDate() == null ? "--" : p.getPaymentDate().format(DATE);
        String method   = p.getMethodLabel();
        String period   = p.getPeriodLabel();
        String receipt  = p.getReceiptNo() == null ? "--" : p.getReceiptNo();
        String note     = p.getNote() == null || p.getNote().isBlank() ? "-" : p.getNote();

        return "<!DOCTYPE html>"
                + "<html><head><meta charset='UTF-8'><title>Receipt " + receipt + "</title>"
                + "<style>"
                + "body{font-family:'Segoe UI',Arial,sans-serif;background:#f6f8fc;padding:40px;}"
                + ".card{max-width:600px;margin:auto;background:#fff;border-radius:16px;"
                + "      box-shadow:0 6px 24px rgba(15,23,42,.08);padding:32px;}"
                + "h1{margin:0;font-size:22px;color:#0f172a;}"
                + ".sub{color:#64748b;font-size:13px;margin-top:4px;}"
                + ".row{display:flex;justify-content:space-between;padding:10px 0;"
                + "     border-bottom:1px dashed #e2e8f0;font-size:14px;}"
                + ".row span:first-child{color:#64748b;}"
                + ".total{font-size:20px;font-weight:bold;color:#4f46e5;margin-top:16px;}"
                + ".footer{margin-top:24px;font-size:11px;color:#94a3b8;text-align:center;}"
                + "</style></head><body>"
                + "<div class='card'>"
                + "<h1>Payment Receipt</h1>"
                + "<div class='sub'>StudentHub Management System</div>"
                + "<div style='margin-top:24px;'>"
                + "<div class='row'><span>Receipt No.</span><b>" + receipt + "</b></div>"
                + "<div class='row'><span>Student</span><b>" + safe(p.getStudentName())
                + " (" + safe(p.getStudentCode()) + ")</b></div>"
                + "<div class='row'><span>Batch</span><b>" + safe(p.getBatchName()) + "</b></div>"
                + "<div class='row'><span>Payment Date</span><b>" + date + "</b></div>"
                + "<div class='row'><span>Method</span><b>" + method + "</b></div>"
                + "<div class='row'><span>Period</span><b>" + period + "</b></div>"
                + "<div class='row'><span>Note</span><b>" + note + "</b></div>"
                + "</div>"
                + "<div class='total'>Amount Paid: " + amount + " MMK</div>"
                + "<div class='footer'>Thank you. Please keep this receipt for your records.</div>"
                + "</div></body></html>";
    }

    private void clearForm() {
        selectedStudent = null;
        selectedPayment = null;

        lblSelectedStudent.setText("No student selected");
        lblSelectedStudentCode.setText("Choose a student to record a payment");
        lblFeeSummary.setText("Paid: --    •    Outstanding: --");
        txtAmount.clear();
        clearError();
        txtReceiptNo.setText(dao.generateReceiptNo());
        historyTable.setItems(FXCollections.observableArrayList());
        btnSavePayment.setDisable(true);

        updateToggleButton("ACTIVE");

        if (batchPriceCard != null) {
            batchPriceCard.setVisible(false);
            batchPriceCard.setManaged(false);
        }
    }

    private void showError(String msg) {
        lblAmountError.setText(msg);
        lblAmountError.setVisible(true);
        lblAmountError.setManaged(true);
    }

    private void clearError() {
        lblAmountError.setText("");
        lblAmountError.setVisible(false);
        lblAmountError.setManaged(false);
    }

    private void showToast(String title, String heading, String msg, boolean success) {
        if (paymentToast == null) return;

        paymentToast.setPrefWidth(420);
        paymentToast.setMinWidth(420);
        paymentToast.setMaxWidth(420);
        paymentToast.setPrefHeight(125);
        paymentToast.setMinHeight(125);
        paymentToast.setMaxHeight(125);

        String bg  = success ? "#dcfce7" : "#fee2e2";
        String fg  = success ? "#16a34a" : "#dc2626";
        String bar = success ? "#22c55e" : "#ef4444";
        String mk  = success ? "✓" : "!";

        lblToastTitle.setText(title);
        lblToastHeading.setText(heading);
        lblToastMessage.setText(msg);

        var iconBox = paymentToast.lookup(".toast-icon");
        if (iconBox != null)
            iconBox.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:19;");

        var iconLbl = paymentToast.lookup(".toast-icon-label");
        if (iconLbl instanceof Label l) {
            l.setText(mk);
            l.setStyle("-fx-text-fill:" + fg + "; -fx-font-size:17px; -fx-font-weight:bold;");
        }

        var barNode = paymentToast.lookup(".toast-bar");
        if (barNode != null)
            barNode.setStyle("-fx-background-color:" + bar + "; -fx-background-radius:3;");

        paymentToast.setManaged(true);
        paymentToast.setVisible(true);

        if (toastTimer != null) toastTimer.stop();
        toastTimer = new PauseTransition(Duration.seconds(3));
        toastTimer.setOnFinished(e -> {
            paymentToast.setVisible(false);
            paymentToast.setManaged(false);
        });
        toastTimer.play();
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "--" : v;
    }
}