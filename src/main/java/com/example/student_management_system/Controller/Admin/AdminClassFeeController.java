package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.ClassDAO;
import com.example.student_management_system.Controller.DAO.FeeDAO;
import com.example.student_management_system.Controller.Model.Batch;
import com.example.student_management_system.Controller.Model.FeeFinancialSummary;
import com.example.student_management_system.Controller.Model.StudentFeeRow;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class AdminClassFeeController implements Initializable {

    @FXML
    private Label lblTotalExpected;
    @FXML
    private Label lblTotalCollected;
    @FXML
    private Label lblTotalOutstanding;
    @FXML
    private Label lblPaidUnpaidCount;
    @FXML
    private TextField txtSearchStudent;
    @FXML
    private ComboBox<Batch> cmbClassFilter;
    @FXML
    private ComboBox<String> cmbStatusFilter;
    @FXML
    private Label lblStudentFeeCount;
    @FXML
    private TableView<StudentFeeRow> feeTable;
    @FXML
    private TableColumn<StudentFeeRow, String> colStudentCode;
    @FXML
    private TableColumn<StudentFeeRow, String> colStudentName;
    @FXML
    private TableColumn<StudentFeeRow, String> colClassName;
    @FXML
    private TableColumn<StudentFeeRow, String> colClassFee;
    @FXML
    private TableColumn<StudentFeeRow, String> colAmountPaid;
    @FXML
    private TableColumn<StudentFeeRow, String> colBalanceDue;
    @FXML
    private TableColumn<StudentFeeRow, String> colPaymentStatus;

    private final FeeDAO feeDAO = new FeeDAO();
    private final ClassDAO classDAO = new ClassDAO();

    private static final Batch ALL_CLASSES =
            new Batch(-1, "All Classes");

    private static final NumberFormat MONEY_FORMAT =
            NumberFormat.getNumberInstance(Locale.US);

    @Override
    public void initialize(
            URL location,
            ResourceBundle resources
    ) {

        setupFilters();
        setupTableColumns();
        setupListeners();
        loadData();
    }

    private void setupFilters() {

        List<Batch> classItems = new ArrayList<>();
        classItems.add(ALL_CLASSES);
        classItems.addAll(classDAO.getAllBatches());

        cmbClassFilter.setItems(
                FXCollections.observableArrayList(classItems)
        );
        cmbClassFilter.getSelectionModel().select(ALL_CLASSES);

        cmbStatusFilter.setItems(
                FXCollections.observableArrayList(
                        "ALL",
                        "PAID",
                        "UNPAID",
                        "PARTIAL",
                        "OVERDUE",
                        "NO FEE"
                )
        );
        cmbStatusFilter.getSelectionModel().select("ALL");
    }

    private void setupTableColumns() {

        colStudentCode.setCellValueFactory(data ->
                new SimpleStringProperty(
                        valueOrDash(
                                data.getValue().getStudentCode()
                        )
                )
        );

        colStudentName.setCellValueFactory(data ->
                new SimpleStringProperty(
                        valueOrDash(
                                data.getValue().getStudentName()
                        )
                )
        );

        colClassName.setCellValueFactory(data ->
                new SimpleStringProperty(
                        valueOrDash(
                                data.getValue().getClassName()
                        )
                )
        );

        colClassFee.setCellValueFactory(data ->
                new SimpleStringProperty(
                        formatMoney(
                                data.getValue().getClassFee()
                        )
                )
        );

        colAmountPaid.setCellValueFactory(data ->
                new SimpleStringProperty(
                        formatMoney(
                                data.getValue().getAmountPaid()
                        )
                )
        );

        colBalanceDue.setCellValueFactory(data ->
                new SimpleStringProperty(
                        formatMoney(
                                data.getValue().getBalanceDue()
                        )
                )
        );

        colPaymentStatus.setCellFactory(
                makeStatusBadgeCellFactory()
        );

        colPaymentStatus.setCellValueFactory(data ->
                new SimpleStringProperty(
                        formatStatusLabel(
                                data.getValue().getPaymentStatus()
                        )
                )
        );

        feeTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
    }

    private void setupListeners() {

        txtSearchStudent.textProperty().addListener(
                (obs, oldValue, newValue) -> loadData()
        );

        cmbClassFilter.valueProperty().addListener(
                (obs, oldValue, newValue) -> loadData()
        );

        cmbStatusFilter.valueProperty().addListener(
                (obs, oldValue, newValue) -> loadData()
        );
    }

    private void loadData() {

        Batch selectedClass =
                cmbClassFilter.getValue();
        int classId =
                selectedClass == null
                        ? -1
                        : selectedClass.getId();

        String status =
                cmbStatusFilter.getValue() == null
                        ? "ALL"
                        : cmbStatusFilter.getValue();

        String search =
                txtSearchStudent.getText();

        List<StudentFeeRow> rows =
                feeDAO.getStudentFeeRows(
                        classId,
                        status,
                        search
                );

        feeTable.setItems(
                FXCollections.observableArrayList(rows)
        );

        lblStudentFeeCount.setText(
                rows.size() +
                        (rows.size() == 1
                                ? " student"
                                : " students")
        );

        FeeFinancialSummary summary =
                feeDAO.getFinancialSummary(classId);

        lblTotalExpected.setText(
                formatMoney(summary.getTotalExpected()) +
                        " MMK"
        );
        lblTotalCollected.setText(
                formatMoney(summary.getTotalCollected()) +
                        " MMK"
        );
        lblTotalOutstanding.setText(
                formatMoney(summary.getTotalOutstanding()) +
                        " MMK"
        );
        lblPaidUnpaidCount.setText(
                summary.getPaidStudents() +
                        " / " +
                        summary.getUnpaidStudents()
        );
    }

    private Callback<
            TableColumn<StudentFeeRow, String>,
            TableCell<StudentFeeRow, String>
            > makeStatusBadgeCellFactory() {

        return column -> new TableCell<>() {

            private final Label badge = new Label();

            {
                badge.setStyle(
                        "-fx-font-size: 10px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 6;" +
                                "-fx-padding: 4 10;"
                );
            }

            @Override
            protected void updateItem(
                    String item,
                    boolean empty
            ) {

                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                badge.setText(item);
                applyBadgeStyle(badge, item);
                setGraphic(badge);
            }
        };
    }

    private void applyBadgeStyle(
            Label badge,
            String status
    ) {

        switch (status.toUpperCase()) {

            case "PAID" -> badge.setStyle(
                    "-fx-background-color: #eafaf0;" +
                            "-fx-text-fill: #1c8a52;" +
                            "-fx-font-size: 10px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 6;" +
                            "-fx-padding: 4 10;"
            );

            case "PARTIAL" -> badge.setStyle(
                    "-fx-background-color: #fff7ed;" +
                            "-fx-text-fill: #c2410c;" +
                            "-fx-font-size: 10px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 6;" +
                            "-fx-padding: 4 10;"
            );

            case "OVERDUE" -> badge.setStyle(
                    "-fx-background-color: #fee2e2;" +
                            "-fx-text-fill: #dc2626;" +
                            "-fx-font-size: 10px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 6;" +
                            "-fx-padding: 4 10;"
            );

            case "NO FEE" -> badge.setStyle(
                    "-fx-background-color: #f1f5f9;" +
                            "-fx-text-fill: #64748b;" +
                            "-fx-font-size: 10px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 6;" +
                            "-fx-padding: 4 10;"
            );

            default -> badge.setStyle(
                    "-fx-background-color: #fef3c7;" +
                            "-fx-text-fill: #b45309;" +
                            "-fx-font-size: 10px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 6;" +
                            "-fx-padding: 4 10;"
            );
        }
    }

    private String formatStatusLabel(String status) {

        if (status == null || status.isBlank()) {
            return "Unpaid";
        }

        return switch (status.toUpperCase()) {
            case "PAID" -> "Paid";
            case "PARTIAL" -> "Partial";
            case "OVERDUE" -> "Overdue";
            case "NO FEE" -> "No Fee";
            default -> "Unpaid";
        };
    }

    private String formatMoney(BigDecimal amount) {

        if (amount == null) {
            return "0";
        }

        return MONEY_FORMAT.format(amount);
    }

    private String valueOrDash(String value) {

        if (value == null || value.isBlank()) {
            return "-";
        }

        return value;
    }
}
