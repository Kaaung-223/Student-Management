package com.example.student_management_system.Controller.Staff;

import com.example.student_management_system.Controller.DAO.StaffAnnouncementsDAO;
import com.example.student_management_system.Controller.Model.StaffAnnouncementRow;
import com.example.student_management_system.Controller.Model.StaffInfo;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class StaffAnnouncementsController implements Initializable {

    // Filter
    @FXML private TextField txtSearch;

    // List
    @FXML private ListView<StaffAnnouncementRow> announcementList;
    @FXML private Label lblAnnouncementCount;

    // Details
    @FXML private Label lblTitle;
    @FXML private Label lblMeta;
    @FXML private Label lblAudience;
    @FXML private Label lblPostedBy;
    @FXML private TextArea txtReason;

    private final StaffAnnouncementsDAO dao = new StaffAnnouncementsDAO();
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private StaffInfo staffInfo;
    private PauseTransition debounce;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        announcementList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(StaffAnnouncementRow a, boolean empty) {
                super.updateItem(a, empty);
                if (empty || a == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                String date = a.getAnnouncementDate() == null
                        ? "--" : a.getAnnouncementDate().format(DATE);
                setText(a.getTitle() + "   •   " + date + "   •   " + a.getAudienceLabel());
                setStyle("-fx-padding:10 14; -fx-font-size:12px;");
            }
        });

        announcementList.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> showDetails(b));

        debounce = new PauseTransition(Duration.millis(250));
        debounce.setOnFinished(e -> refresh());
        txtSearch.textProperty().addListener((o, a, b) -> debounce.play());
    }

    /** Called by StaffDashboardController via reflection. */
    public void setStaffInfo(StaffInfo info) {
        this.staffInfo = info;
        refresh();
    }

    @FXML
    public void refresh() {
        List<StaffAnnouncementRow> list = dao.findAnnouncements(txtSearch.getText());

        announcementList.setItems(FXCollections.observableArrayList(list));
        lblAnnouncementCount.setText(list.size() + " announcement(s)");

        if (!list.isEmpty()) announcementList.getSelectionModel().selectFirst();
        else                 clearDetails();

        // Mark all as read — clears the login toast on next login
        if (staffInfo != null && staffInfo.getUserId() > 0) {
            dao.markAllAsRead(staffInfo.getUserId());
        }
    }

    // -------------------------------------------------
    private void showDetails(StaffAnnouncementRow a) {
        if (a == null) { clearDetails(); return; }

        lblTitle.setText(a.getTitle());

        String date = a.getAnnouncementDate() == null
                ? "--" : a.getAnnouncementDate().format(DATE);
        lblMeta.setText("Posted on " + date);

        lblAudience.setText("Audience: " + a.getAudienceLabel());
        lblPostedBy.setText("Posted by: " + safe(a.getCreatedBy()));

        txtReason.setText(a.getReason() == null ? "" : a.getReason());
    }

    private void clearDetails() {
        lblTitle.setText("No announcement selected");
        lblMeta.setText("Select an announcement to view details");
        lblAudience.setText("Audience: --");
        lblPostedBy.setText("Posted by: --");
        txtReason.setText("");
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "--" : v;
    }
}