module com.example.student_management_system {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;

    // JavaFX can launch your Main class
    exports com.example.student_management_system.Controller;

    // FXMLLoader can access your controllers
    opens com.example.student_management_system.Controller to javafx.fxml;

    exports com.example.student_management_system.Controller.Admin;
    opens com.example.student_management_system.Controller.Admin to javafx.fxml;
}