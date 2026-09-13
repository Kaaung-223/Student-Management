package com.example.student_management_system.Controller.Teacher;

import com.example.student_management_system.Controller.DAO.TeacherSubjectsDAO;
import com.example.student_management_system.Controller.Model.Subject;
import com.example.student_management_system.Controller.Model.TeacherInfo;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TeacherSubjectsController implements Initializable {

    // Filter
    @FXML private TextField txtSearch;

    // Table
    @FXML private TableView<Subject> subjectTable;
    @FXML private TableColumn<Subject, String> colName;
    @FXML private TableColumn<Subject, String> colCode;
    @FXML private TableColumn<Subject, String> colClasses;
    @FXML private Label lblSubjectCount;

    // Details card
    @FXML private Label lblSubjectName;
    @FXML private Label lblSubjectCode;
    @FXML private Label lblClasses;
    @FXML private Label lblStudentCount;
    @FXML private Label lblAvgScore;

    private final TeacherSubjectsDAO dao = new TeacherSubjectsDAO();
    private TeacherInfo teacherInfo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        colName.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));
        colClasses.setCellValueFactory(new PropertyValueFactory<>("classesUsingIt"));

        subjectTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        subjectTable.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> showDetails(b));

        // Debounced search
        PauseTransition debounce = new PauseTransition(Duration.millis(250));
        debounce.setOnFinished(e -> refresh());
        txtSearch.textProperty().addListener((o, a, b) -> debounce.play());
    }

    /** Called by TeacherDashboardController. */
    public void setTeacherInfo(TeacherInfo info) {
        this.teacherInfo = info;
        refresh();
    }

    @FXML
    public void refresh() {
        if (teacherInfo == null) return;

        List<Subject> list = dao.findTeacherSubjects(
                teacherInfo.getTeacherId(), txtSearch.getText());

        subjectTable.setItems(FXCollections.observableArrayList(list));
        lblSubjectCount.setText(list.size() + " subject(s)");

        if (!list.isEmpty()) subjectTable.getSelectionModel().selectFirst();
        else                 clearDetails();
    }

    // -------------------------------------------------
    private void showDetails(Subject s) {
        if (s == null) { clearDetails(); return; }

        lblSubjectName.setText(s.getSubjectName());
        lblSubjectCode.setText("Code: " + safe(s.getSubjectCode()));
        lblClasses.setText("Classes: " + safe(s.getClassesUsingIt()));

        int students = dao.getStudentCountForSubject(s.getId());
        double avg = dao.getAverageForSubject(s.getId());

        lblStudentCount.setText(String.valueOf(students));
        lblAvgScore.setText(String.format("%.1f%%", avg));
    }

    private void clearDetails() {
        lblSubjectName.setText("No subject selected");
        lblSubjectCode.setText("Select a row to view details");
        lblClasses.setText("Classes: --");
        lblStudentCount.setText("0");
        lblAvgScore.setText("0.0%");
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "--" : v;
    }
}