package com.example.student_management_system.Controller.Admin;

import com.example.student_management_system.Controller.DAO.AnnouncementDAO;
import com.example.student_management_system.Controller.Model.Announcement;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AdminAnnouncementController implements Initializable {

    @FXML private AnchorPane rootPane;   // root for toast overlay

    @FXML private TextField txtTitle;
    @FXML private TextArea txtReason;
    @FXML private DatePicker dpAnnouncementDate;
    @FXML private ComboBox<String> cbTargetAudience;

    @FXML private TableView<Announcement> tableAnnouncements;
    @FXML private TableColumn<Announcement, Integer> colId;
    @FXML private TableColumn<Announcement, String> colTitle;
    @FXML private TableColumn<Announcement, String> colTarget;
    @FXML private TableColumn<Announcement, LocalDate> colDate;
    @FXML private TableColumn<Announcement, String> colReason;
    @FXML private TableColumn<Announcement, String> colCreatedBy;

    private final AnnouncementDAO announcementDAO = new AnnouncementDAO();
    private final ObservableList<Announcement> announcementList = FXCollections.observableArrayList();
    private Announcement selectedAnnouncement = null;

    private int currentUserId = 1;

    private PauseTransition toastTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbTargetAudience.setItems(FXCollections.observableArrayList("TEACHER", "STAFF", "ALL"));
        cbTargetAudience.getSelectionModel().select("ALL");
        dpAnnouncementDate.setValue(LocalDate.now());

        setupTableColumns();
        loadAnnouncementData();

        tableAnnouncements.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedAnnouncement = newSelection;
                populateForm(selectedAnnouncement);
            }
        });
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("announcementId"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTarget.setCellValueFactory(new PropertyValueFactory<>("targetAudience"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("announcementDate"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colCreatedBy.setCellValueFactory(new PropertyValueFactory<>("createdByName"));
    }

    private void loadAnnouncementData() {
        announcementList.setAll(announcementDAO.getAllAnnouncements());
        tableAnnouncements.setItems(announcementList);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateInput()) return;

        String title = txtTitle.getText().trim();
        String reason = txtReason.getText().trim();
        LocalDate date = dpAnnouncementDate.getValue();
        String target = cbTargetAudience.getValue();

        Announcement announcement = new Announcement(title, reason, date, target, currentUserId);

        if (announcementDAO.insert(announcement)) {
            showToast(true, "POSTED", "Announcement saved",
                    "Your announcement was posted successfully.");
            clearForm();
            loadAnnouncementData();
        } else {
            showToast(false, "SAVE FAILED", "Could not save",
                    "An error occurred while posting the announcement.");
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedAnnouncement == null) {
            showToast(false, "UPDATE", "No selection",
                    "Please select an announcement to update.");
            return;
        }

        if (!validateInput()) return;

        selectedAnnouncement.setTitle(txtTitle.getText().trim());
        selectedAnnouncement.setReason(txtReason.getText().trim());
        selectedAnnouncement.setAnnouncementDate(dpAnnouncementDate.getValue());
        selectedAnnouncement.setTargetAudience(cbTargetAudience.getValue());

        if (announcementDAO.update(selectedAnnouncement)) {
            showToast(true, "UPDATED", "Announcement updated",
                    "Changes have been saved.");
            clearForm();
            loadAnnouncementData();
        } else {
            showToast(false, "UPDATE FAILED", "Could not update",
                    "An error occurred while updating the announcement.");
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedAnnouncement == null) {
            showToast(false, "DELETE", "No selection",
                    "Please select an announcement to delete.");
            return;
        }

        // Confirmation dialog (remains as Alert)
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Announcement");
        confirm.setHeaderText("Delete \"" + selectedAnnouncement.getTitle() + "\"?");
        confirm.setContentText("This action cannot be undone.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        if (announcementDAO.delete(selectedAnnouncement.getAnnouncementId())) {
            showToast(true, "DELETED", "Announcement removed",
                    "The announcement has been deleted.");
            clearForm();
            loadAnnouncementData();
        } else {
            showToast(false, "DELETE FAILED", "Could not delete",
                    "An error occurred while deleting the announcement.");
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
    }

    private void populateForm(Announcement announcement) {
        txtTitle.setText(announcement.getTitle());
        txtReason.setText(announcement.getReason());
        dpAnnouncementDate.setValue(announcement.getAnnouncementDate());
        cbTargetAudience.setValue(announcement.getTargetAudience());
    }

    private void clearForm() {
        txtTitle.clear();
        txtReason.clear();
        dpAnnouncementDate.setValue(LocalDate.now());
        cbTargetAudience.getSelectionModel().select("ALL");
        tableAnnouncements.getSelectionModel().clearSelection();
        selectedAnnouncement = null;
    }

    private boolean validateInput() {
        if (txtTitle.getText().trim().isEmpty()) {
            showToast(false, "VALIDATION", "Missing title",
                    "Please enter an announcement title.");
            txtTitle.requestFocus();
            return false;
        }
        if (txtReason.getText().trim().isEmpty()) {
            showToast(false, "VALIDATION", "Missing details",
                    "Please enter the reason or details.");
            txtReason.requestFocus();
            return false;
        }
        if (dpAnnouncementDate.getValue() == null) {
            showToast(false, "VALIDATION", "Missing date",
                    "Please select an announcement date.");
            dpAnnouncementDate.requestFocus();
            return false;
        }
        return true;
    }

    // =========================================================
    // TOAST OVERLAY (styled like login toast)
    // =========================================================

    private void showToast(boolean success, String title, String heading, String message) {
        hideToast();

        VBox toast = new VBox(8);
        toast.setMaxWidth(350);
        toast.setPrefWidth(350);
        toast.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 15;" +
                "-fx-padding: 15 17 15 15;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,.20), 22, 0, 0, 6);");

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

        Region accent = new Region();
        accent.setMinHeight(3);
        accent.setMaxHeight(3);
        accent.setStyle(success
                ? "-fx-background-color: #16a34a; -fx-background-radius: 3;"
                : "-fx-background-color: #ef4444; -fx-background-radius: 3;");

        toast.getChildren().addAll(topBox, accent);

        StackPane.setAlignment(toast, Pos.TOP_RIGHT);
        StackPane.setMargin(toast, new Insets(24, 24, 0, 0));

        rootPane.getChildren().add(toast);

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

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }
}