package com.example.student_management_system.Controller.Teacher;

import com.example.student_management_system.Controller.DAO.TeacherClassesDAO;
import com.example.student_management_system.Controller.Model.TeacherClass;
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

public class TeacherClassesController implements Initializable {

    // Filter
    @FXML private TextField txtSearch;

    // Table
    @FXML private TableView<TeacherClass> classTable;
    @FXML private TableColumn<TeacherClass, String>  colName;
    @FXML private TableColumn<TeacherClass, String>  colYear;
    @FXML private TableColumn<TeacherClass, String>  colRoom;
    @FXML private TableColumn<TeacherClass, Integer> colStudents;
    @FXML private TableColumn<TeacherClass, String>  colRole;
    @FXML private Label lblClassCount;

    // Details card
    @FXML private Label lblClassName;
    @FXML private Label lblClassMeta;
    @FXML private Label lblRole;
    @FXML private Label lblSubjects;
    @FXML private Label lblStudentCount;
    @FXML private Label lblSubjectCount;

    private final TeacherClassesDAO dao = new TeacherClassesDAO();
    private TeacherInfo teacherInfo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        colName.setCellValueFactory(new PropertyValueFactory<>("className"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("academicYear"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomNo"));
        colStudents.setCellValueFactory(new PropertyValueFactory<>("studentCount"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("roleLabel"));

        classTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        classTable.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> showDetails(b));

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

        List<TeacherClass> list = dao.findTeacherClasses(
                teacherInfo.getTeacherId(), txtSearch.getText());

        classTable.setItems(FXCollections.observableArrayList(list));
        lblClassCount.setText(list.size() + " class(es)");

        if (!list.isEmpty()) classTable.getSelectionModel().selectFirst();
        else                 clearDetails();
    }

    // -------------------------------------------------
    private void showDetails(TeacherClass tc) {
        if (tc == null) { clearDetails(); return; }

        lblClassName.setText(tc.getClassName());
        lblClassMeta.setText(
                "Year: " + safe(tc.getAcademicYear()) +
                        "   |   Room: " + safe(tc.getRoomNo()));
        lblRole.setText("Role: " + tc.getRoleLabel());
        lblSubjects.setText("Subjects you teach: " + safe(tc.getSubjectsTaught()));

        int students = dao.getStudentCount(tc.getClassId());
        int subjects = dao.getSubjectCount(teacherInfo.getTeacherId(), tc.getClassId());

        lblStudentCount.setText(String.valueOf(students));
        lblSubjectCount.setText(String.valueOf(subjects));
    }

    private void clearDetails() {
        lblClassName.setText("No class selected");
        lblClassMeta.setText("Select a row to view details");
        lblRole.setText("Role: --");
        lblSubjects.setText("Subjects you teach: --");
        lblStudentCount.setText("0");
        lblSubjectCount.setText("0");
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "--" : v;
    }
}