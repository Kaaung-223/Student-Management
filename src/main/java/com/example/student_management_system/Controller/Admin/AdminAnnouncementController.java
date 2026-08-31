package com.example.student_management_system.Controller.Admin;



import com.example.student_management_system.Controller.DAO.AnnouncementDAO;
import com.example.student_management_system.Controller.Model.Announcement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AdminAnnouncementController implements Initializable {

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

    // Default logged-in User ID (e.g., admin)
    private int currentUserId = 1;

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
            showAlert(Alert.AlertType.INFORMATION, "Success", "Announcement posted successfully!");
            clearForm();
            loadAnnouncementData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save announcement.");
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedAnnouncement == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select an announcement to update.");
            return;
        }

        if (!validateInput()) return;

        selectedAnnouncement.setTitle(txtTitle.getText().trim());
        selectedAnnouncement.setReason(txtReason.getText().trim());
        selectedAnnouncement.setAnnouncementDate(dpAnnouncementDate.getValue());
        selectedAnnouncement.setTargetAudience(cbTargetAudience.getValue());

        if (announcementDAO.update(selectedAnnouncement)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Announcement updated successfully!");
            clearForm();
            loadAnnouncementData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update announcement.");
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedAnnouncement == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select an announcement to delete.");
            return;
        }

        if (announcementDAO.delete(selectedAnnouncement.getAnnouncementId())) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Announcement deleted!");
            clearForm();
            loadAnnouncementData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete announcement.");
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
        if (txtTitle.getText().trim().isEmpty() || txtReason.getText().trim().isEmpty() || dpAnnouncementDate.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Title, Reason, and Date fields are required.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }
}
