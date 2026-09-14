package com.example.student_management_system.Controller.Teacher;

import com.example.student_management_system.Controller.DAO.TeacherAnnouncementsDAO;
import com.example.student_management_system.Controller.Model.AnnouncementRow;
import com.example.student_management_system.Controller.Model.TeacherInfo;

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

public class TeacherAnnouncementsController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private ListView<AnnouncementRow> announcementList;
    @FXML private Label lblAnnouncementCount;

    @FXML private Label lblTitle;
    @FXML private Label lblMeta;
    @FXML private Label lblAudience;
    @FXML private Label lblPostedBy;
    @FXML private TextArea txtReason;

    private final TeacherAnnouncementsDAO dao = new TeacherAnnouncementsDAO();
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private TeacherInfo teacherInfo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        announcementList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(AnnouncementRow a, boolean empty) {
                super.updateItem(a, empty);
                if (empty || a == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                String date = a.getAnnouncementDate() == null
                        ? "--" : a.getAnnouncementDate().format(DATE);
                setText(a.getTitle() + "   •   " + date);
                setStyle("-fx-padding:10 14; -fx-font-size:12px;");
            }
        });

        announcementList.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> showDetails(b));

        PauseTransition debounce = new PauseTransition(Duration.millis(250));
        debounce.setOnFinished(e -> refresh());
        txtSearch.textProperty().addListener((o, a, b) -> debounce.play());
    }

    public void setTeacherInfo(TeacherInfo info) {
        this.teacherInfo = info;
        refresh();
    }

    @FXML
    public void refresh() {
        List<AnnouncementRow> list = dao.findAnnouncements(txtSearch.getText());

        announcementList.setItems(FXCollections.observableArrayList(list));
        lblAnnouncementCount.setText(list.size() + " announcement(s)");

        if (!list.isEmpty()) announcementList.getSelectionModel().selectFirst();
        else                 clearDetails();

        // ✅ Mark all as read for this teacher after viewing
        if (teacherInfo != null) {
            dao.markAllAsRead(teacherInfo.getUserId());
        }
    }

    // -------------------------------------------------
    private void showDetails(AnnouncementRow a) {
        if (a == null) { clearDetails(); return; }

        lblTitle.setText(a.getTitle());

        String date = a.getAnnouncementDate() == null
                ? "--" : a.getAnnouncementDate().format(DATE);
        lblMeta.setText("Posted on " + date);

        lblAudience.setText("Audience: " + safe(a.getTargetAudience()));
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